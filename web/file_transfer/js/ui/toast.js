/**
 * Toast 提示
 *
 * 体验优化：
 * - 连续 showToast 时会重置计时器，避免“突然消失”。
 * - 支持 type: success | warning | error
 */

import { elements } from '../core/dom.js';

let hideTimer = null;

export function showToast(message, type = 'success') {
  if (!elements.toast || !elements.toastMessage) {
    console.log('[toast]', type, message);
    return;
  }

  elements.toastMessage.textContent = message;

  // 可选：根据类型切换样式（HTML 里如果已有统一样式，这里不强改）
  elements.toast.dataset.type = type;

  elements.toast.classList.remove('hidden');

  if (hideTimer) clearTimeout(hideTimer);
  hideTimer = setTimeout(() => {
    elements.toast?.classList.add('hidden');
  }, 3000);
}

