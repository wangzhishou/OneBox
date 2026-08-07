/**
 * Loading / Error 状态切换
 */

import { state } from '../core/state.js';
import { elements } from '../core/dom.js';

export function showLoading(show) {
  state.isLoading = show;
  if (show) {
    elements.loadingSkeleton?.classList.remove('hidden');
    elements.fileGrid?.classList.add('hidden');
    elements.fileList?.classList.add('hidden');
  } else {
    elements.loadingSkeleton?.classList.add('hidden');
    if (state.viewMode === 'grid') {
      elements.fileGrid?.classList.remove('hidden');
    } else {
      elements.fileList?.classList.remove('hidden');
    }
  }
}

export function showError(message) {
  if (!elements.errorMessage || !elements.errorState || !elements.emptyState) return;
  elements.errorMessage.textContent = message;
  elements.errorState.classList.remove('hidden');
  elements.emptyState.classList.add('hidden');
}

export function hideError() {
  elements.errorState?.classList.add('hidden');
}

