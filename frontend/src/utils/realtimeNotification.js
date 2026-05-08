const PREFIX = 'smart-community-muted-notice:';

export function muteRealtimeNotification(title, ttlMs = 5000) {
  if (!title || typeof window === 'undefined') {
    return;
  }
  window.sessionStorage.setItem(`${PREFIX}${title}`, String(Date.now() + ttlMs));
}

export function shouldMuteRealtimeNotification(title) {
  if (!title || typeof window === 'undefined') {
    return false;
  }

  const key = `${PREFIX}${title}`;
  const value = Number(window.sessionStorage.getItem(key) || 0);
  if (!value) {
    return false;
  }

  window.sessionStorage.removeItem(key);
  return value >= Date.now();
}
