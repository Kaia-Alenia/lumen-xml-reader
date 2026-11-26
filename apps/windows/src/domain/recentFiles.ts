import { RecentFileEntry } from './types';

const STORAGE_KEY = 'lumen.recentFiles';
const MAX_RECENT_FILES = 15;

export function loadRecentFiles(): RecentFileEntry[] {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY);
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
}

export function saveRecentFile(entry: RecentFileEntry): RecentFileEntry[] {
  const existing = loadRecentFiles().filter(
    (item) => item.path !== entry.path || item.fileName !== entry.fileName
  );
  const updated = [entry, ...existing].slice(0, MAX_RECENT_FILES);
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
  } catch {
    // Ignore storage quota errors
  }
  return updated;
}

export function removeRecentFile(path: string, fileName: string): RecentFileEntry[] {
  const updated = loadRecentFiles().filter(
    (item) => !(item.path === path && item.fileName === fileName)
  );
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
  } catch {
    // Ignore storage quota errors
  }
  return updated;
}

export function clearRecentFiles(): RecentFileEntry[] {
  try {
    window.localStorage.removeItem(STORAGE_KEY);
  } catch {
    // Ignore storage errors
  }
  return [];
}
