/**
 * 通用工具函数
 */

export function escapeHtml(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

export function generateId() {
  return 'id-' + Math.random().toString(36).slice(2, 18);
}

export function formatSize(bytes) {
  const n = Number(bytes);
  if (!Number.isFinite(n) || n < 0) return '0 B';

  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let value = n;
  let unitIndex = 0;

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024;
    unitIndex += 1;
  }

  const digits = unitIndex === 0 ? 0 : value < 10 ? 1 : 0;
  return `${value.toFixed(digits)} ${units[unitIndex]}`;
}

export function formatTime(timestamp) {
  const t = Number(timestamp);
  if (!Number.isFinite(t) || t <= 0) return '';

  const d = new Date(t);
  if (Number.isNaN(d.getTime())) return '';

  try {
    const now = Date.now();
    const diff = Math.abs(now - d.getTime());
    const oneDay = 24 * 60 * 60 * 1000;

    if (diff < oneDay * 2) {
      // 近两天：只显示时间，信息更“轻”
      return d.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
    }

    return d.toLocaleDateString(undefined, { year: 'numeric', month: '2-digit', day: '2-digit' });
  } catch (_) {
    const pad = (x) => String(x).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }
}

/**
 * 简单 debounce：避免输入框高频触发导致反复渲染
 */
export function debounce(fn, waitMs = 200) {
  let t = null;
  return (...args) => {
    if (t) clearTimeout(t);
    t = setTimeout(() => fn(...args), waitMs);
  };
}

