/**
 * 面包屑与返回按钮
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { escapeHtml } from '../../core/utils.js';

export function renderBreadcrumb() {
  const parts = (state.currentPath || '').split('/').filter((p) => p);

  let html = `<span class="px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer text-gray-600 dark:text-gray-300 whitespace-nowrap" onclick="loadFiles('')">${i18n.t('breadcrumb.home', '首页')}</span>`;

  let currentPath = '';
  parts.forEach((part, index) => {
    currentPath += (currentPath ? '/' : '') + part;
    const path = currentPath;

    html += `
      <svg class="w-4 h-4 text-gray-400 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
      </svg>
      <span class="px-2 py-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer text-gray-600 dark:text-gray-300 whitespace-nowrap ${index === parts.length - 1 ? 'font-medium text-gray-900 dark:text-white' : ''}"
            onclick="loadFiles('${escapeHtml(path)}')">${escapeHtml(part)}</span>
    `;
  });

  if (elements.breadcrumb) elements.breadcrumb.innerHTML = html;
}

export function updateNavigation() {
  if (!elements.backBtn) return;
  elements.backBtn.disabled = !state.canGoUp;
}

