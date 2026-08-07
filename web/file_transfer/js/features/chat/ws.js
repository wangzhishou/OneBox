/**
 * WebSocket 聊天连接
 *
 * 体验优化：
 * - 仅在聊天面板打开时提示断开/重连，避免后台预连接导致 toast 打扰。
 */

import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { showToast } from '../../ui/toast.js';
import { ensureChatChannelId, storeChatChannelId, getBrowserDeviceName } from './channel.js';
import { loadChatHistory } from './history.js';
import { handleChatMessage } from './messages.js';

export function connectChat() {
  const storedChannelId = ensureChatChannelId();

  // 加载聊天历史
  loadChatHistory();

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const deviceName = encodeURIComponent(getBrowserDeviceName());

  const channelQuery = storedChannelId
    ? `?channelId=${encodeURIComponent(storedChannelId)}&deviceName=${deviceName}`
    : `?deviceName=${deviceName}`;

  const wsUrl = `${protocol}//${window.location.host}/ws/chat${channelQuery}`;
  console.log('Connecting to WebSocket:', wsUrl);

  try {
    state.chatWebSocket = new WebSocket(wsUrl);

    state.chatWebSocket.onopen = () => {
      console.log('WebSocket connected');
      if (state.isChatOpen) showToast(i18n.t('toast.chat.connected', '聊天连接成功'), 'success');
    };

    state.chatWebSocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);

        if (data.type === 'system') {
          if (data.event === 'connected' && data.channelId) {
            storeChatChannelId(data.channelId);
          }
          return;
        }

        if (data.type === 'error') {
          console.error('WebSocket error:', data.message);
          if (state.isChatOpen) showToast(data.message, 'error');
          return;
        }

        handleChatMessage(data);
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error);
      }
    };

    state.chatWebSocket.onerror = (error) => {
      console.error('WebSocket error:', error);
      if (state.isChatOpen) showToast(i18n.t('toast.chat.error', '聊天连接错误'), 'error');
    };

    state.chatWebSocket.onclose = (event) => {
      console.log('WebSocket closed:', event.code, event.reason);

      if (state.isChatOpen) showToast(i18n.t('toast.chat.disconnected', '聊天连接已断开'), 'warning');

      // 5秒后自动重连（仅当聊天面板打开时）
      setTimeout(() => {
        if (state.isChatOpen && (!state.chatWebSocket || state.chatWebSocket.readyState === WebSocket.CLOSED)) {
          connectChat();
        }
      }, 5000);
    };
  } catch (error) {
    console.error('Failed to create WebSocket:', error);
    if (state.isChatOpen) showToast(i18n.t('toast.chat.cannotConnect', '无法建立聊天连接'), 'error');
  }
}

export async function waitForChatWebSocketOpen(timeoutMs = 2500) {
  const start = Date.now();

  while (Date.now() - start < timeoutMs) {
    const ws = state.chatWebSocket;
    if (ws && ws.readyState === WebSocket.OPEN) return true;

    if (!ws || ws.readyState === WebSocket.CLOSED) {
      connectChat();
    }

    await new Promise((r) => setTimeout(r, 80));
  }

  return false;
}

