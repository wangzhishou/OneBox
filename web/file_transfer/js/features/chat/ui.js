/**
 * 聊天 UI 渲染与未读角标
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { escapeHtml, formatTime, formatSize } from '../../core/utils.js';
import { isNearChatBottom, showNewMessagesHint, hideNewMessagesHint } from './behavior.js';

export function updateChatBadge() {
  const n = state.unreadMessages || 0;

  // 这些元素在不同布局下可能不存在，因此都做 Optional 处理
  const badges = [elements.chatBadge, elements.navChatBadge, elements.rightTabChatBadge].filter(Boolean);
  badges.forEach((el) => {
    if (n > 0) {
      el.classList.remove('hidden');
      el.textContent = n > 99 ? '99+' : String(n);
    } else {
      el.classList.add('hidden');
      el.textContent = '';
    }
  });
}

export function renderChatMessages() {
  const containers = [elements.chatMessages, elements.mobileChatMessages].filter(Boolean);
  if (containers.length === 0) return;

  const html = (state.chatMessages || []).map((m) => renderMessage(m)).join('');
  containers.forEach((c) => {
    c.innerHTML = html;
  });
}

export function scrollChatToBottom() {
  const containers = [elements.chatMessages, elements.mobileChatMessages].filter(Boolean);
  containers.forEach((c) => {
    try {
      c.scrollTop = c.scrollHeight;
    } catch (_) {}
  });
  hideNewMessagesHint();
}

export function maybeAutoScrollOnNewMessage() {
  // 如果任一可见容器在底部附近，则滚到底
  const containers = [elements.chatMessages, elements.mobileChatMessages].filter(Boolean);
  const shouldScroll = containers.some((c) => isNearChatBottom(c));

  if (shouldScroll) {
    scrollChatToBottom();
  } else {
    showNewMessagesHint();
  }
}

export function updateChatMessage(messageId, patch) {
  const idx = (state.chatMessages || []).findIndex((m) => m.id === messageId);
  if (idx < 0) return;

  state.chatMessages[idx] = { ...state.chatMessages[idx], ...patch };
  renderChatMessages();
}

function renderMessage(message) {
  const isMine = message.sender === 'browser';
  const alignCls = isMine ? 'justify-end' : 'justify-start';
  const bubbleCls = isMine
    ? 'bg-primary text-on-primary'
    : 'bg-surface-container-high text-on-surface';

  const time = formatTime(message.timestamp);

  if (message.type === 'file') {
    return `
      <div class="flex ${alignCls}">
        <div class="max-w-[80%] rounded-xl px-4 py-2 ${bubbleCls}">
          <div class="text-sm font-medium truncate">${escapeHtml(message.fileName || 'file')}</div>
          <div class="text-xs opacity-80 mt-1">
            ${message.fileSize != null ? escapeHtml(formatSize(message.fileSize)) : ''}
            ${time ? ' · ' + escapeHtml(time) : ''}
          </div>
          ${renderFileStatus(message)}
          ${message.filePath ? `<div class="mt-2"><a class="underline text-sm" href="/api/download?path=${encodeURIComponent(message.filePath)}" download="${escapeHtml(message.fileName || 'file')}">${i18n.t('common.download', '下载')}</a></div>` : ''}
          ${renderFileActions(message)}
        </div>
      </div>
    `;
  }

  // text
  return `
    <div class="flex ${alignCls}">
      <div class="max-w-[80%] rounded-xl px-4 py-2 ${bubbleCls}">
        <div class="text-sm whitespace-pre-wrap break-words">${escapeHtml(message.content || '')}</div>
        <div class="text-xs opacity-70 mt-1">${escapeHtml(time)}</div>
      </div>
    </div>
  `;
}

function renderFileStatus(message) {
  const status = message.status;
  if (!status) return '';

  if (status === 'uploading') {
    const pct = Math.max(0, Math.min(100, Number(message.progress) || 0));
    return `
      <div class="mt-2">
        <div class="h-1.5 bg-white/30 rounded overflow-hidden">
          <div class="h-full bg-white/80" style="width:${pct}%"></div>
        </div>
        <div class="text-xs opacity-80 mt-1">${i18n.t('chat.upload.progress', '上传中 {pct}%').replace('{pct}', pct)}</div>
      </div>
    `;
  }

  if (status === 'queued') return `<div class="text-xs opacity-80 mt-2">${i18n.t('chat.upload.queued', '等待上传…')}</div>`;
  if (status === 'completed') return `<div class="text-xs opacity-80 mt-2">${i18n.t('chat.upload.completed', '已发送')}</div>`;
  if (status === 'canceled') return `<div class="text-xs opacity-80 mt-2">${i18n.t('chat.upload.canceled', '已取消')}</div>`;

  if (status === 'error') {
    return `<div class="text-xs text-red-200 mt-2">${i18n.t('chat.upload.failedPrefix', '失败：')}${escapeHtml(message.errorMessage || '')}</div>`;
  }

  return '';
}

function renderFileActions(message) {
  if (!message.id) return '';

  if (message.status === 'queued' || message.status === 'uploading') {
    return `<div class="mt-2"><button class="text-xs underline" onclick="cancelChatUpload('${escapeHtml(message.id)}')">${i18n.t('common.cancel', '取消')}</button></div>`;
  }

  if (message.status === 'error') {
    return `<div class="mt-2"><button class="text-xs underline" onclick="retryChatUpload('${escapeHtml(message.id)}')">${i18n.t('common.retry', '重试')}</button></div>`;
  }

  return '';
}
