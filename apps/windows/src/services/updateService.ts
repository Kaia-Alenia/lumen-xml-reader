import { invoke } from '@tauri-apps/api/core';
import { AppUpdateInfo, isNewerVersion } from '../domain/updateChecker';
import { isTauriEnvironment } from './fileService';

const CURRENT_FALLBACK_VERSION = '1.2.1';
const GITHUB_RELEASE_API_URL =
  'https://api.github.com/repos/Kaia-Alenia/lumen-xml-reader/releases/latest';
const GITHUB_RELEASES_PAGE_URL =
  'https://github.com/Kaia-Alenia/lumen-xml-reader/releases';

interface GitHubApiAsset {
  name?: string;
  browser_download_url?: string;
}

interface GitHubApiRelease {
  tag_name?: string;
  name?: string;
  body?: string;
  html_url?: string;
  assets?: GitHubApiAsset[];
}

export async function checkForUpdates(): Promise<AppUpdateInfo | null> {
  if (isTauriEnvironment()) {
    try {
      return await invoke<AppUpdateInfo>('check_github_update');
    } catch {
      return null;
    }
  }

  try {
    const res = await fetch(GITHUB_RELEASE_API_URL, {
      headers: { Accept: 'application/vnd.github.v3+json' },
    });
    if (!res.ok) return null;

    const json = (await res.json()) as GitHubApiRelease;
    const rawTag = (json.tag_name ?? '').trim();
    const cleanRemote = rawTag.replace(/^[vV]/, '').trim();
    const releasePageUrl = json.html_url?.trim() || GITHUB_RELEASES_PAGE_URL;
    const assets = json.assets ?? [];

    const exeAsset = assets.find((a) =>
      (a.name ?? '').toLowerCase().endsWith('.exe')
    );
    const msiAsset = assets.find((a) =>
      (a.name ?? '').toLowerCase().endsWith('.msi')
    );
    const downloadUrl =
      exeAsset?.browser_download_url ||
      msiAsset?.browser_download_url ||
      releasePageUrl;

    return {
      currentVersion: CURRENT_FALLBACK_VERSION,
      latestVersion: cleanRemote || rawTag,
      releaseTitle: json.name?.trim() || 'Actualización disponible',
      releaseNotes: (json.body ?? '').trim(),
      downloadUrl,
      releasePageUrl,
      isUpdateAvailable: isNewerVersion(CURRENT_FALLBACK_VERSION, cleanRemote),
    };
  } catch {
    return null;
  }
}

export async function installUpdate(downloadUrl: string): Promise<string> {
  if (isTauriEnvironment()) {
    return invoke<string>('download_and_install_update', { downloadUrl });
  }
  window.open(downloadUrl, '_blank', 'noopener,noreferrer');
  return downloadUrl;
}
