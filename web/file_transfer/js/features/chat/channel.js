/**
 * 聊天 channelId 管理
 *
 * 设计：
 * - sessionStorage 优先：不同 tab 不共享历史（更符合预期）
 * - localStorage 兜底：兼容旧版本
 */

export const CHAT_CHANNEL_STORAGE_KEY = 'wanbaohe_chat_channel_id';

export function generateChatChannelId() {
  return 'ch-' + Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10);
}

export function getStoredChatChannelId() {
  try {
    const sid = sessionStorage.getItem(CHAT_CHANNEL_STORAGE_KEY);
    if (sid && sid.trim()) return sid.trim();
  } catch (_) {}

  try {
    const id = localStorage.getItem(CHAT_CHANNEL_STORAGE_KEY);
    return id && id.trim() ? id.trim() : null;
  } catch (_) {
    return null;
  }
}

export function storeChatChannelId(channelId) {
  try {
    if (channelId && channelId.trim()) {
      sessionStorage.setItem(CHAT_CHANNEL_STORAGE_KEY, channelId.trim());
    }
  } catch (_) {}

  try {
    if (channelId && channelId.trim()) {
      localStorage.setItem(CHAT_CHANNEL_STORAGE_KEY, channelId.trim());
    }
  } catch (_) {}
}

export function ensureChatChannelId() {
  let id = getStoredChatChannelId();
  if (!id) {
    id = generateChatChannelId();
    storeChatChannelId(id);
  }
  return id;
}

export function getBrowserDeviceName() {
  try {
    const ua = navigator.userAgent || '';
    const low = ua.toLowerCase();

    let os = 'Unknown';
    if (low.includes('windows')) os = 'Windows';
    else if (low.includes('mac os') || low.includes('macintosh')) os = 'macOS';
    else if (low.includes('android')) os = 'Android';
    else if (low.includes('iphone') || low.includes('ipad')) os = 'iOS';
    else if (low.includes('linux')) os = 'Linux';

    let browser = 'Browser';
    if (low.includes('edg/')) browser = 'Edge';
    else if (low.includes('chrome/')) browser = 'Chrome';
    else if (low.includes('safari/') && !low.includes('chrome/')) browser = 'Safari';
    else if (low.includes('firefox/')) browser = 'Firefox';

    return `${os} ${browser}`;
  } catch (_) {
    return 'Browser';
  }
}

