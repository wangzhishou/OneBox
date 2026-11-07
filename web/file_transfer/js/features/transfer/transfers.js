/**
 * 传输任务列表（上传进度 / 保存位置 / 定位）
 *
 * 设计目标：
 * - 每张卡都要回答"这个文件现在在哪"：保存目录 + 完成时间 + 一键定位
 * - 上传中可取消、失败可重试、完成后可移除，顶部可一键清空
 * - 同名同大小同目录的重复上传合并为 ×N，避免一屏四条一模一样的记录
 */

import { elements } from '../../core/dom.js';
import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { escapeHtml, formatSize, formatTime } from '../../core/utils.js';
import { loadFiles } from '../files/files.js';
import { renderFiles } from '../files/render.js';
import { downloadFile } from '../files/download.js';
import { showToast } from '../../ui/toast.js';

let bindingsReady = false;
let persistTimer = null;

const STORAGE_KEY = 'ft-transfers-v1';
const MAX_PERSISTED = 20;
const MAX_AGE_MS = 7 * 24 * 60 * 60 * 1000;

/**
 * 传输记录持久化：刷新页面后还能知道"刚才传到哪了"
 */
function schedulePersist() {
  if (persistTimer) clearTimeout(persistTimer);
  persistTimer = setTimeout(() => {
    persistTimer = null;
    try {
      const items = (state.transfers || [])
        .filter((t) => t.status === 'completed' || t.status === 'error' || t.status === 'canceled')
        .slice(0, MAX_PERSISTED)
        .map((t) => ({
          id: t.id,
          name: t.name,
          size: t.size,
          status: t.status,
          dir: t.dir,
          filePath: t.filePath,
          timestamp: t.timestamp,
          count: t.count,
        }));

      localStorage.setItem(STORAGE_KEY, JSON.stringify(items));
    } catch (_) {
      // localStorage 不可用不影响上传
    }
  }, 400);
}

export function loadPersistedTransfers() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return;

    const now = Date.now();
    const items = JSON.parse(raw);
    if (!Array.isArray(items)) return;

    const restored = items
      .filter((t) => t && t.name && Number(t.timestamp) > now - MAX_AGE_MS)
      .slice(0, MAX_PERSISTED)
      .map((t) => ({ ...t, restored: true }));

    // 已有内存记录（比如刚上传过）时，不覆盖，只补历史
    const known = new Set((state.transfers || []).map((t) => t.id));
    state.transfers = [...(state.transfers || []), ...restored.filter((t) => !known.has(t.id))];
  } catch (_) {
    // ignore
  }
}

/**
 * 保存目录的展示文案：'' = 存储根目录
 */
export function formatDir(dir) {
  const d = String(dir || '')
    .replace(/^\/+/, '')
    .replace(/\/+$/, '');
  return d ? `/${d}` : i18n.t('path.root', '手机存储根目录');
}

function statusText(t) {
  switch (t.status) {
    case 'completed':
      return i18n.t('transfer.status.completed', '完成');
    case 'error':
      return i18n.t('transfer.status.error', '失败');
    case 'uploading':
      return i18n.t('transfer.status.uploading', '上传中');
    case 'queued':
      return i18n.t('transfer.status.queued', '排队中');
    case 'canceled':
      return i18n.t('transfer.status.canceled', '已取消');
    default:
      return t.status || '';
  }
}

export function addTransferItem({
  id,
  name,
  size,
  status,
  progress,
  errorMessage,
  dir,
  filePath,
  updateOnly = false,
}) {
  if (!state.transfers) state.transfers = [];

  const idx = state.transfers.findIndex((t) => t.id === id);

  if (idx >= 0) {
    const item = state.transfers[idx];
    if (name != null) item.name = name;
    if (size != null) item.size = size;
    if (status != null) item.status = status;
    if (progress != null) item.progress = progress;
    if (errorMessage != null) item.errorMessage = errorMessage;
    if (dir != null) item.dir = dir;
    if (filePath != null) item.filePath = filePath;
    if (status != null && status !== 'uploading') item.timestamp = Date.now();
  } else {
    state.transfers.unshift({
      id,
      name,
      size,
      status,
      progress: progress ?? 0,
      errorMessage: errorMessage ?? null,
      dir: dir ?? state.currentPath ?? '',
      filePath: filePath ?? null,
      timestamp: Date.now(),
      count: 1,
      batchId: state.uploadBatchId,
    });
  }

  if (status === 'completed') mergeCompletedDuplicates(id);

  renderTransfers();
}

/**
 * 完成后合并重复项：同名 + 同大小 + 同目录 → ×N（只保留一条，时间取最新）
 */
function mergeCompletedDuplicates(id) {
  const idx = state.transfers.findIndex((t) => t.id === id);
  if (idx < 0) return;

  const item = state.transfers[idx];
  if (item.status !== 'completed') return;

  const dupIdx = state.transfers.findIndex(
    (t, i) =>
      i !== idx &&
      t.status === 'completed' &&
      t.name === item.name &&
      Number(t.size || 0) === Number(item.size || 0) &&
      String(t.dir || '') === String(item.dir || '')
  );
  if (dupIdx < 0) return;

  // 保留"这一次"的记录（它带着当前批次的 batchId，上传弹窗还能显示），
  // 把旧记录的次数合并进来。
  const dup = state.transfers[dupIdx];
  item.count = (item.count || 1) + (dup.count || 1);
  item.timestamp = item.timestamp || Date.now();
  item.filePath = item.filePath || dup.filePath;

  state.transfers.splice(dupIdx, 1);
  delete state.uploadRuntime[dup.id];

  state.transfers = [item, ...state.transfers.filter((t) => t !== item)];
}

export function getTransfer(id) {
  return (state.transfers || []).find((t) => t.id === id) || null;
}

export function removeTransfer(id) {
  state.transfers = (state.transfers || []).filter((t) => t.id !== id);
  delete state.uploadRuntime[id];
  state.uploadQueue = (state.uploadQueue || []).filter((job) => job.id !== id);
  renderTransfers();
}

export function clearFinishedTransfers() {
  state.transfers = (state.transfers || []).filter(
    (t) => t.status === 'uploading' || t.status === 'queued'
  );
  renderTransfers();
  schedulePersist();
}

/**
 * 定位：跳到文件所在目录并高亮
 */
export async function locateTransfer(id) {
  const t = getTransfer(id);
  if (!t) return;

  const dir = t.dir || '';
  const target = t.filePath || t.name;

  state.pendingReveal = target;
  state.pendingRevealSince = 0;

  if ((state.currentPath || '') !== dir) {
    await loadFiles(dir);
    // loadFiles 内部会 renderFiles 并消费 pendingReveal
  } else {
    // 同目录：直接重渲染触发定位
    renderFiles();
  }
}

function renderActions(t) {
  const btn = (action, label, extra = '') =>
    `<button data-action="${action}" class="px-2 py-1 rounded-md text-xs ${extra} hover:bg-surface-container-high transition-colors">${label}</button>`;

  const items = [];

  if (t.status === 'uploading' || t.status === 'queued') {
    items.push(
      btn(
        'cancel',
        i18n.t('transfer.action.cancel', '取消'),
        'text-on-surface-variant'
      )
    );
  } else {
    if (t.filePath || t.name) {
      items.push(btn('locate', i18n.t('transfer.action.locate', '定位'), 'text-primary'));
    }
    if (t.status === 'completed') {
      items.push(btn('download', i18n.t('transfer.action.download', '下载'), 'text-on-surface-variant'));
    }
    if ((t.status === 'error' || t.status === 'canceled') && !t.restored) {
      items.push(btn('retry', i18n.t('transfer.action.retry', '重试'), 'text-primary'));
    }
    items.push(btn('remove', i18n.t('transfer.action.remove', '移除'), 'text-on-surface-variant'));
  }

  return items.join('');
}

function renderItem(t) {
  const pct = Math.max(0, Math.min(100, Number(t.progress) || 0));
  const inFlight = t.status === 'uploading' || t.status === 'queued';
  const meta = [t.size != null ? formatSize(t.size) : '', statusText(t), t.timestamp ? formatTime(t.timestamp) : '']
    .filter(Boolean)
    .join(' · ');

  return `
    <div class="bg-surface border border-outline-variant rounded-xl p-3" data-transfer-id="${escapeHtml(t.id)}">
      <div class="flex items-start justify-between gap-2">
        <div class="min-w-0 flex-1">
          <div class="flex items-center gap-2 min-w-0">
            <p class="text-sm font-medium truncate" title="${escapeHtml(t.name || '')}">${escapeHtml(t.name || '')}</p>
            ${
              (t.count || 1) > 1
                ? `<span class="shrink-0 text-[10px] px-1.5 py-0.5 rounded-full bg-surface-container-high text-on-surface-variant">×${t.count}</span>`
                : ''
            }
          </div>
          <div class="text-xs text-on-surface-variant mt-0.5">${escapeHtml(meta)}</div>
          <div class="flex items-center gap-1 text-xs text-on-surface-variant mt-0.5 min-w-0">
            <svg class="w-3.5 h-3.5 shrink-0" viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/>
            </svg>
            <span class="truncate" title="${escapeHtml(formatDir(t.dir))}">${escapeHtml(formatDir(t.dir))}</span>
          </div>
        </div>
        ${inFlight ? `<div class="text-xs text-on-surface-variant shrink-0">${pct}%</div>` : ''}
      </div>

      ${
        inFlight
          ? `<div class="h-1.5 bg-surface-container-high rounded overflow-hidden mt-2">
               <div class="h-full bg-primary transition-[width]" style="width:${pct}%"></div>
             </div>`
          : ''
      }

      ${
        t.errorMessage
          ? `<div class="text-xs text-red-500 mt-1 break-words">${escapeHtml(t.errorMessage)}</div>`
          : ''
      }

      <div class="mt-1 -ml-2 flex items-center gap-0.5">${renderActions(t)}</div>
    </div>
  `;
}

export function renderTransfers() {
  const listEls = [elements.transferList, elements.mobileTransferList].filter(Boolean);
  const emptyEls = [elements.transferEmpty, elements.mobileTransferEmpty].filter(Boolean);

  const transfers = state.transfers || [];

  if (elements.transferClear) {
    const hasFinished = transfers.some((t) => t.status !== 'uploading' && t.status !== 'queued');
    elements.transferClear.classList.toggle('hidden', !hasFinished);
  }
  if (elements.mobileTransferClear) {
    const hasFinished = transfers.some((t) => t.status !== 'uploading' && t.status !== 'queued');
    elements.mobileTransferClear.classList.toggle('hidden', !hasFinished);
  }

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

  const hasInFlight = transfers.some((t) => t.status === 'uploading' || t.status === 'queued');
  if (!hasInFlight) schedulePersist();
}

/**
 * 绑定事件（事件委托，列表重渲染后依然有效）
 */
export function initTransferBindings() {
  if (bindingsReady) return;
  bindingsReady = true;

  const onClick = async (e) => {
    const btn = e.target.closest('button[data-action]');
    if (!btn) return;

    const card = btn.closest('[data-transfer-id]');
    const id = card?.dataset.transferId;
    if (!id) return;

    const t = getTransfer(id);
    if (!t) return;

    switch (btn.dataset.action) {
      case 'cancel':
        window.cancelUpload?.(id);
        break;
      case 'retry':
        window.retryUpload?.(id);
        break;
      case 'remove':
        removeTransfer(id);
        break;
      case 'download':
        if (t.filePath) downloadFile(t.filePath, t.name);
        break;
      case 'locate':
        await locateTransfer(id);
        break;
      default:
        break;
    }
  };

  loadPersistedTransfers();
  renderTransfers();

  elements.transferList?.addEventListener('click', onClick);
  elements.mobileTransferList?.addEventListener('click', onClick);

  elements.transferClear?.addEventListener('click', () => {
    clearFinishedTransfers();
    showToast(i18n.t('transfer.cleared', '已清除完成的传输任务'), 'success');
  });
  elements.mobileTransferClear?.addEventListener('click', () => {
    clearFinishedTransfers();
    showToast(i18n.t('transfer.cleared', '已清除完成的传输任务'), 'success');
  });
}
