/**
 * 传输任务列表（上传进度等）
 */

import { elements } from '../../core/dom.js';
import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { escapeHtml, formatSize } from '../../core/utils.js';

export function addTransferItem({ id, name, size, status, progress, errorMessage, updateOnly = false }) {
  // 统一在 state.transfers 里维护
  if (!state.transfers) state.transfers = [];

  const idx = state.transfers.findIndex((t) => t.id === id);

  if (idx >= 0) {
    state.transfers[idx] = {
      ...state.transfers[idx],
      ...(name != null ? { name } : {}),
      ...(size != null ? { size } : {}),
      ...(status != null ? { status } : {}),
      ...(progress != null ? { progress } : {}),
      ...(errorMessage != null ? { errorMessage } : {}),
    };
  } else {
    state.transfers.unshift({ id, name, size, status, progress: progress ?? 0, errorMessage: errorMessage ?? null });
  }

  renderTransfers();
}

export function renderTransfers() {
  const listEls = [elements.transferList, elements.mobileTransferList].filter(Boolean);
  const emptyEls = [elements.transferEmpty, elements.mobileTransferEmpty].filter(Boolean);

  const transfers = state.transfers || [];

  if (transfers.length === 0) {
    listEls.forEach((el) => {
      el.classList.add('hidden');
      el.innerHTML = '';
    });
    emptyEls.forEach((el) => el.classList.remove('hidden'));
    return;
  }

  emptyEls.forEach((el) => el.classList.add('hidden'));

  const html = transfers.map(renderItem).join('');
  listEls.forEach((el) => {
    el.classList.remove('hidden');
    el.innerHTML = html;
  });
}

function renderItem(t) {
  const statusText =
    t.status === 'completed'
      ? i18n.t('transfer.status.completed', '完成')
      : t.status === 'error'
        ? i18n.t('transfer.status.error', '失败')
        : t.status === 'uploading'
          ? i18n.t('transfer.status.uploading', '上传中')
          : t.status || '';

  const pct = Math.max(0, Math.min(100, Number(t.progress) || 0));

  return `
    <div class="bg-surface border border-outline-variant rounded-xl p-3">
      <div class="flex items-start justify-between gap-2">
        <div class="min-w-0">
          <div class="text-sm font-medium truncate">${escapeHtml(t.name || '')}</div>
          <div class="text-xs text-on-surface-variant mt-0.5">${t.size != null ? escapeHtml(formatSize(t.size)) : ''} · ${escapeHtml(statusText)}</div>
        </div>
        <div class="text-xs text-on-surface-variant">${pct}%</div>
      </div>

      <div class="h-1.5 bg-surface-container-high rounded overflow-hidden mt-2">
        <div class="h-full ${t.status === 'error' ? 'bg-red-500' : 'bg-primary'}" style="width:${pct}%"></div>
      </div>

      ${t.errorMessage ? `<div class="text-xs text-red-500 mt-1">${escapeHtml(t.errorMessage)}</div>` : ''}
    </div>
  `;
}

