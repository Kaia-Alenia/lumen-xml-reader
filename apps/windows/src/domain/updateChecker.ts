export interface AppUpdateInfo {
  currentVersion: string;
  latestVersion: string;
  releaseTitle: string;
  releaseNotes: string;
  downloadUrl: string;
  releasePageUrl: string;
  isUpdateAvailable: boolean;
}

function parseVersionParts(raw: string): number[] | null {
  const cleaned = raw
    .trim()
    .replace(/^[vV]/, '')
    .split('-')[0]
    .trim();

  if (!cleaned) return null;

  const parts = cleaned
    .split('.')
    .map((segment) => {
      const digits = segment.replace(/\D/g, '');
      return digits ? Number.parseInt(digits, 10) : Number.NaN;
    })
    .filter((num) => Number.isFinite(num));

  return parts.length > 0 ? parts : null;
}

export function isNewerVersion(current: string, remote: string): boolean {
  const currentParts = parseVersionParts(current);
  const remoteParts = parseVersionParts(remote);
  if (!currentParts || !remoteParts) return false;

  const maxLength = Math.max(currentParts.length, remoteParts.length);
  for (let i = 0; i < maxLength; i += 1) {
    const curr = currentParts[i] ?? 0;
    const rem = remoteParts[i] ?? 0;
    if (rem > curr) return true;
    if (rem < curr) return false;
  }
  return false;
}
