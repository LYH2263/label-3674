function pad(value) {
  return String(value).padStart(2, '0');
}

function normalizeToDate(value) {
  if (!value) {
    return null;
  }
  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value;
  }
  if (Array.isArray(value)) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value;
    const date = new Date(year, (month || 1) - 1, day || 1, hour, minute, second);
    return Number.isNaN(date.getTime()) ? null : date;
  }
  if (typeof value === 'string') {
    const normalized = value.includes('T') ? value : value.replace(' ', 'T');
    const date = new Date(normalized);
    return Number.isNaN(date.getTime()) ? null : date;
  }
  return null;
}

export function formatDateTime(value, { withSeconds = true } = {}) {
  const date = normalizeToDate(value);
  if (!date) {
    return '-';
  }
  const year = date.getFullYear();
  const month = pad(date.getMonth() + 1);
  const day = pad(date.getDate());
  const hour = pad(date.getHours());
  const minute = pad(date.getMinutes());
  const second = pad(date.getSeconds());
  if (!withSeconds) {
    return `${year}-${month}-${day} ${hour}:${minute}`;
  }
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`;
}

export function formatRelativeTime(value) {
  const date = normalizeToDate(value);
  if (!date) {
    return '-';
  }

  const diffMs = Date.now() - date.getTime();
  const diffMinutes = Math.round(diffMs / 60000);
  const absMinutes = Math.abs(diffMinutes);

  if (absMinutes < 1) {
    return '刚刚';
  }
  if (absMinutes < 60) {
    return diffMinutes >= 0 ? `${absMinutes} 分钟前` : `${absMinutes} 分钟后`;
  }

  const diffHours = Math.round(absMinutes / 60);
  if (diffHours < 24) {
    return diffMinutes >= 0 ? `${diffHours} 小时前` : `${diffHours} 小时后`;
  }

  const diffDays = Math.round(diffHours / 24);
  return diffMinutes >= 0 ? `${diffDays} 天前` : `${diffDays} 天后`;
}
