/**
 * 聊天交互增强：
 * 1) 输入框 textarea 自动增高（最多 N 行）
 * 2) 新消息到来时，如果用户不在底部，不强制滚动；显示“新消息”提示条
 *
 * 设计要点：
 * - isNearBottom 使用阈值判断（默认 24px），避免小幅滚动误判。
 * - 自动增高通过 scrollHeight 计算，并限制最大高度（maxLines * lineHeight）。
 */

import { elements } from '../../core/dom.js';
import { state } from '../../core/state.js';

const DEFAULT_MAX_LINES = 6;
const DEFAULT_BOTTOM_THRESHOLD_PX = 24;

function getComputedLineHeightPx(textarea) {
  const style = window.getComputedStyle(textarea);
  const lh = parseFloat(style.lineHeight);
  if (Number.isFinite(lh)) return lh;

  const fs = parseFloat(style.fontSize);
  return Number.isFinite(fs) ? fs * 1.4 : 20;
}

function autosizeTextarea(textarea, { maxLines = DEFAULT_MAX_LINES } = {}) {
  if (!textarea) return;

  // reset -> measure -> apply
  textarea.style.height = 'auto';

  const lineHeight = getComputedLineHeightPx(textarea);
  const maxHeight = Math.round(lineHeight * maxLines + 16); // + padding buffer

  const next = Math.min(textarea.scrollHeight, maxHeight);
  textarea.style.height = `${next}px`;
  textarea.style.overflowY = textarea.scrollHeight > maxHeight ? 'auto' : 'hidden';
}

export function initChatTextareaAutosize({ maxLines = DEFAULT_MAX_LINES } = {}) {
  const targets = [elements.chatInput, elements.mobileChatInput].filter(Boolean);

  targets.forEach((ta) => {
    // 初始
    autosizeTextarea(ta, { maxLines });

    // 输入时
    ta.addEventListener('input', () => autosizeTextarea(ta, { maxLines }));

    // 粘贴（部分浏览器 input 前不触发）
    ta.addEventListener('paste', () => setTimeout(() => autosizeTextarea(ta, { maxLines }), 0));

    // 发送后会清空 value，这里也要收缩回去
    ta.addEventListener('change', () => autosizeTextarea(ta, { maxLines }));
  });
}

export function isNearChatBottom(container, thresholdPx = DEFAULT_BOTTOM_THRESHOLD_PX) {
  if (!container) return true;
  const distance = container.scrollHeight - container.scrollTop - container.clientHeight;
  return distance <= thresholdPx;
}

export function hideNewMessagesHint() {
  state.chatHasPendingNewMessages = false;
  if (elements.desktopNewMessages) elements.desktopNewMessages.classList.add('hidden');
  if (elements.mobileNewMessages) elements.mobileNewMessages.classList.add('hidden');
}

export function showNewMessagesHint() {
  state.chatHasPendingNewMessages = true;

  // 只针对当前可见的容器显示，但两端都显示也没问题
  if (elements.desktopNewMessages) elements.desktopNewMessages.classList.remove('hidden');
  if (elements.mobileNewMessages) elements.mobileNewMessages.classList.remove('hidden');
}

export function bindNewMessagesHintActions(scrollToBottomFn) {
  const onClick = () => {
    hideNewMessagesHint();
    scrollToBottomFn?.();
  };

  elements.desktopNewMessages?.addEventListener('click', onClick);
  elements.mobileNewMessages?.addEventListener('click', onClick);

  // 用户手动滚到底部时也自动隐藏
  const containers = [elements.chatMessages, elements.mobileChatMessages].filter(Boolean);
  containers.forEach((c) => {
    c.addEventListener(
      'scroll',
      () => {
        if (isNearChatBottom(c)) hideNewMessagesHint();
      },
      { passive: true },
    );
  });
}

