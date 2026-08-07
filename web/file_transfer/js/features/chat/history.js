/**
 * 聊天历史
 */

import { state } from '../../core/state.js';
import * as api from '../../core/api.js';
import { ensureChatChannelId, storeChatChannelId } from './channel.js';
import { renderChatMessages, scrollChatToBottom } from './ui.js';

export async function loadChatHistory() {
  try {
    const storedChannelId = ensureChatChannelId();
    const { data } = await api.chatHistory(storedChannelId);

    if (data && data.channelId) {
      storeChatChannelId(data.channelId);
    }

    if (data?.success && data.messages) {
      state.chatMessages = data.messages;
      renderChatMessages();
      scrollChatToBottom();
    }
  } catch (error) {
    console.error('Failed to load chat history:', error);
  }
}

