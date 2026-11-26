use serde::{Deserialize, Serialize};
use std::fs;
use std::io::Write;
use std::path::Path;
use std::sync::Mutex;
use std::time::Duration;
use tauri::{AppHandle, State};

const GITHUB_RELEASE_API_URL: &str =
    "https://api.github.com/repos/Kaia-Alenia/lumen-xml-reader/releases/latest";
const GITHUB_RELEASES_PAGE_URL: &str =
    "https://github.com/Kaia-Alenia/lumen-xml-reader/releases";

struct LaunchFileState(Mutex<Option<String>>);

#[derive(Serialize)]
struct LoadedXmlFile {
    path: String,
    file_name: String,
    size_bytes: u64,
    content: String,
}

#[derive(Serialize)]
#[serde(rename_all = "camelCase")]
struct ReleaseCheckResult {
    current_version: String,
    latest_version: String,
    release_title: String,
    release_notes: String,
    download_url: String,
    release_page_url: String,
    is_update_available: bool,
}

#[derive(Deserialize)]
struct GitHubAsset {
    name: String,
    browser_download_url: String,
}

#[derive(Deserialize)]
struct GitHubRelease {
    tag_name: Option<String>,
    name: Option<String>,
    body: Option<String>,
    html_url: Option<String>,
    assets: Option<Vec<GitHubAsset>>,
}

fn parse_version_parts(raw: &str) -> Option<Vec<u32>> {
    let cleaned = raw
        .trim()
        .trim_start_matches(['v', 'V'])
        .split('-')
        .next()
        .unwrap_or("")
        .trim();

    if cleaned.is_empty() {
        return None;
    }

    let parts: Vec<u32> = cleaned
        .split('.')
        .filter_map(|segment| {
            let digits: String = segment.chars().filter(|c| c.is_ascii_digit()).collect();
            if digits.is_empty() {
                None
            } else {
                digits.parse::<u32>().ok()
            }
        })
        .collect();

    if parts.is_empty() {
        None
    } else {
        Some(parts)
    }
}

pub fn is_newer_version(current: &str, remote: &str) -> bool {
    let Some(curr_parts) = parse_version_parts(current) else {
        return false;
    };
    let Some(rem_parts) = parse_version_parts(remote) else {
        return false;
    };

    let max_len = curr_parts.len().max(rem_parts.len());
    for idx in 0..max_len {
        let curr = *curr_parts.get(idx).unwrap_or(&0);
        let rem = *rem_parts.get(idx).unwrap_or(&0);
        if rem > curr {
            return true;
        }
        if rem < curr {
            return false;
        }
    }
    false
}

fn select_windows_installer_url(assets: &[GitHubAsset], fallback_url: &str) -> String {
    if let Some(exe) = assets
        .iter()
        .find(|a| a.name.to_ascii_lowercase().ends_with(".exe"))
    {
        return exe.browser_download_url.clone();
    }
    if let Some(msi) = assets
        .iter()
        .find(|a| a.name.to_ascii_lowercase().ends_with(".msi"))
    {
        return msi.browser_download_url.clone();
    }
    fallback_url.to_string()
}

fn is_allowed_download_url(url: &str) -> bool {
    url.starts_with("https://github.com/Kaia-Alenia/lumen-xml-reader/")
        || url.starts_with("https://objects.githubusercontent.com/")
        || url.starts_with("https://release-assets.githubusercontent.com/")
}

#[tauri::command]
fn read_xml_file(path: String) -> Result<LoadedXmlFile, String> {
    let file_path = Path::new(&path);
    if !file_path.exists() {
        return Err(format!("File not found: {}", path));
    }

    let metadata = fs::metadata(file_path).map_err(|e| format!("Metadata error: {}", e))?;
    let bytes = fs::read(file_path).map_err(|e| format!("Read error: {}", e))?;

    let content = match String::from_utf8(bytes) {
        Ok(utf8) => utf8.trim_start_matches('\u{feff}').to_string(),
        Err(err) => {
            let raw = err.into_bytes();
            String::from_utf8_lossy(&raw)
                .trim_start_matches('\u{feff}')
                .to_string()
        }
    };

    let file_name = file_path
        .file_name()
        .and_then(|n| n.to_str())
        .unwrap_or("document.xml")
        .to_string();

    Ok(LoadedXmlFile {
        path,
        file_name,
        size_bytes: metadata.len(),
        content,
    })
}

#[tauri::command]
fn get_launch_file(state: State<LaunchFileState>) -> Option<String> {
    state.0.lock().ok().and_then(|mut guard| guard.take())
}

#[tauri::command]
fn check_github_update() -> Result<ReleaseCheckResult, String> {
    let current_version = env!("CARGO_PKG_VERSION").to_string();

    let client = reqwest::blocking::Client::builder()
        .timeout(Duration::from_secs(10))
        .user_agent("LUMEN-Desktop")
        .build()
        .map_err(|e| format!("HTTP client error: {}", e))?;

    let response = client
        .get(GITHUB_RELEASE_API_URL)
        .header("Accept", "application/vnd.github.v3+json")
        .send()
        .map_err(|e| format!("Update check failed: {}", e))?;

    if !response.status().is_success() {
        return Err(format!("GitHub API returned status {}", response.status()));
    }

    let release: GitHubRelease = response
        .json()
        .map_err(|e| format!("Invalid GitHub release payload: {}", e))?;

    let raw_tag = release.tag_name.unwrap_or_default().trim().to_string();
    let clean_remote = raw_tag.trim_start_matches(['v', 'V']).trim().to_string();
    let release_page_url = release
        .html_url
        .filter(|u| !u.trim().is_empty())
        .unwrap_or_else(|| GITHUB_RELEASES_PAGE_URL.to_string());

    let assets = release.assets.unwrap_or_default();
    let download_url = select_windows_installer_url(&assets, &release_page_url);
    let is_update_available = is_newer_version(&current_version, &clean_remote);

    Ok(ReleaseCheckResult {
        current_version,
        latest_version: if clean_remote.is_empty() {
            raw_tag
        } else {
            clean_remote
        },
        release_title: release
            .name
            .filter(|n| !n.trim().is_empty())
            .unwrap_or_else(|| "Actualización disponible".to_string()),
        release_notes: release.body.unwrap_or_default().trim().to_string(),
        download_url,
        release_page_url,
        is_update_available,
    })
}

#[tauri::command]
fn download_and_install_update(app: AppHandle, download_url: String) -> Result<String, String> {
    if !is_allowed_download_url(&download_url) {
        return Err("Untrusted update download URL".to_string());
    }

    let is_msi = download_url.to_ascii_lowercase().ends_with(".msi");
    let is_exe = download_url.to_ascii_lowercase().ends_with(".exe");
    if !is_exe && !is_msi {
        return Err("No Windows installer (.exe or .msi) found in release".to_string());
    }

    let client = reqwest::blocking::Client::builder()
        .timeout(Duration::from_secs(120))
        .user_agent("LUMEN-Desktop")
        .build()
        .map_err(|e| format!("HTTP client error: {}", e))?;

    let response = client
        .get(&download_url)
        .send()
        .map_err(|e| format!("Download failed: {}", e))?;

    if !response.status().is_success() {
        return Err(format!("Download returned status {}", response.status()));
    }

    let file_name = if is_msi {
        "LUMEN-windows.msi"
    } else {
        "LUMEN-windows-setup.exe"
    };
    let installer_path = std::env::temp_dir().join(file_name);

    let mut file = fs::File::create(&installer_path)
        .map_err(|e| format!("Failed to create installer file: {}", e))?;
    let bytes = response
        .bytes()
        .map_err(|e| format!("Failed to read installer bytes: {}", e))?;
    file.write_all(&bytes)
        .map_err(|e| format!("Failed to write installer file: {}", e))?;
    drop(file);

    #[cfg(target_os = "windows")]
    {
        if is_msi {
            std::process::Command::new("msiexec")
                .args(["/i", &installer_path.to_string_lossy()])
                .spawn()
                .map_err(|e| format!("Failed to launch MSI installer: {}", e))?;
        } else {
            std::process::Command::new(&installer_path)
                .spawn()
                .map_err(|e| format!("Failed to launch setup installer: {}", e))?;
        }
        app.exit(0);
    }

    #[cfg(not(target_os = "windows"))]
    let _ = app;

    Ok(installer_path.to_string_lossy().to_string())
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let launch_arg = std::env::args()
        .nth(1)
        .filter(|arg| !arg.starts_with('-') && Path::new(arg).exists());

    tauri::Builder::default()
        .manage(LaunchFileState(Mutex::new(launch_arg)))
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_fs::init())
        .invoke_handler(tauri::generate_handler![
            read_xml_file,
            get_launch_file,
            check_github_update,
            download_and_install_update
        ])
        .run(tauri::generate_context!())
        .expect("Error running LUMEN desktop application");
}

#[cfg(test)]
mod tests {
    use super::is_newer_version;

    #[test]
    fn detects_newer_semver_versions() {
        assert!(is_newer_version("1.2.0", "1.2.1"));
        assert!(is_newer_version("1.2.0", "v1.3.0"));
        assert!(is_newer_version("1.2.0", "2.0.0"));
    }

    #[test]
    fn ignores_equal_older_or_non_numeric_tags() {
        assert!(!is_newer_version("1.2.0", "1.2.0"));
        assert!(!is_newer_version("1.2.0", "v1.2.0"));
        assert!(!is_newer_version("1.2.0", "1.1.9"));
        assert!(!is_newer_version("1.2.0", "latest"));
        assert!(!is_newer_version("1.2.0", ""));
    }
}
