/**
 * 聊天消息：接收/发送
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { generateId } from '../../core/utils.js';
import { showToast } from '../../ui/toast.js';
import { waitForChatWebSocketOpen } from './ws.js';
import { renderChatMessages, scrollChatToBottom, updateChatMessage, updateChatBadge, maybeAutoScrollOnNewMessage } from './ui.js';

export function handleChatMessage(message) {
  const exists = (state.chatMessages || []).find((m) => m.id === message.id);
  if (exists) return;

  state.chatMessages.push(message);

  if (!state.isChatOpen && message.sender === 'mobile') {
    state.unreadMessages++;
    updateChatBadge();
  }

  renderChatMessages();
  // UX：只有当用户在底部附近才自动滚动，否则提示“有新消息”
  maybeAutoScrollOnNewMessage();
}

export async function sendChatMessage(from = 'desktop') {
  const input = from === 'mobile' ? elements.mobileChatInput : elements.chatInput;
  const text = input?.value.trim();
  if (!text) return;

  const ok = await waitForChatWebSocketOpen(2500);
  if (!ok) {
    showToast(i18n.t('toast.chat.cannotConnect', '无法建立聊天连接'), 'error');
    return;
  }

  const message = {
    id: generateId(),
    type: 'text',
    content: text,
    sender: 'browser',
    timestamp: Date.now(),
  };

  try {
    state.chatWebSocket.send(JSON.stringify(message));
    input.value = '';
    handleChatMessage(message);
    // 自己发送后默认滚到底，确保看到最新一条（含自动增高后布局变化）
    scrollChatToBottom();
  } catch (error) {
    console.error('Send chat message error:', error);
    showToast(i18n.t('toast.send.failed', '发送失败'), 'error');
  }
}

export { updateChatMessage };
