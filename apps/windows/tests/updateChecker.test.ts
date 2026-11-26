import { describe, expect, it } from 'vitest';
import { isNewerVersion } from '../src/domain/updateChecker';

describe('isNewerVersion', () => {
  it('detects newer patch, minor, and major versions', () => {
    expect(isNewerVersion('1.2.0', '1.2.1')).toBe(true);
    expect(isNewerVersion('1.2.0', 'v1.3.0')).toBe(true);
    expect(isNewerVersion('1.2.0', '2.0.0')).toBe(true);
  });

  it('returns false for equal, older, or non-semver tags', () => {
    expect(isNewerVersion('1.2.0', '1.2.0')).toBe(false);
    expect(isNewerVersion('1.2.0', 'v1.2.0')).toBe(false);
    expect(isNewerVersion('1.2.0', '1.1.9')).toBe(false);
    expect(isNewerVersion('1.2.0', 'latest')).toBe(false);
    expect(isNewerVersion('1.2.0', '')).toBe(false);
  });
});
