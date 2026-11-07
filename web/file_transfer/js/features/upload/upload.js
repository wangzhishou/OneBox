/**
 * 上传：弹窗 + 拖拽 + 队列（并发/取消/重试）+ 保存位置提示
 *
 * 设计目标（P0/P1）：
 * - 上传前：明确告诉用户"保存到哪个目录"，根目录给出强提示
 * - 上传中：弹窗里每个文件都有真实进度，可取消；失败可重试；最多 3 个并发
 * - 上传后：列表滚动定位并高亮，toast 带文件名和路径，传输面板可"定位"
 */

import { elements } from '../../core/dom.js';
import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { showToast } from '../../ui/toast.js';
import { escapeHtml, formatSize, generateId } from '../../core/utils.js';
import * as api from '../../core/api.js';
import { loadFiles } from '../files/files.js';
import { addTransferItem, getTransfer, formatDir } from '../transfer/transfers.js';

let refreshTimer = null;
let dragRow = null;

/* ------------------------------------------------------------------ */
/* 保存位置展示                                                        */
/* ------------------------------------------------------------------ */

export function updateUploadDestination(dirOverride) {
  const currentDir = state.currentPath || '';
  const dir = typeof dirOverride === 'string' ? dirOverride : currentDir;
  const label = formatDir(dir);

  if (elements.uploadDestinationPath) {
    elements.uploadDestinationPath.textContent = label;
    elements.uploadDestinationPath.title = label;
  }

  if (elements.uploadRootTip) {
    elements.uploadRootTip.classList.toggle('hidden', dir !== '');
    elements.uploadRootTip.classList.toggle('flex', dir === '');
  }

  if (elements.uploadDestinationHintText) {
    elements.uploadDestinationHintText.textContent = i18n
      .t('toolbar.uploadTo', '上传到 {path}')
      .replace('{path}', formatDir(currentDir));
  }
  if (elements.uploadDestinationHint) {
    elements.uploadDestinationHint.title =
      i18n.t('uploadModal.destination', '保存到') + ' ' + formatDir(currentDir);
  }
  if (elements.uploadBtn) {
    elements.uploadBtn.title = i18n
      .t('toolbar.uploadTo', '上传到 {path}')
      .replace('{path}', formatDir(currentDir));
  }
}

/* ------------------------------------------------------------------ */
/* 弹窗                                                                */
/* ------------------------------------------------------------------ */

export function showUploadModal() {
  if (state.deviceInfo && state.deviceInfo.allowUpload === false) {
    showToast(i18n.t('toast.upload.disabled', '当前设备已关闭上传权限'), 'warning');
    return;
  }

  // 还有上传在跑：沿用那批的目标目录；否则跟随当前浏览目录
  const activeBatch = (state.uploadBatches || []).find((b) => !b.finished);
  updateUploadDestination(activeBatch ? activeBatch.dir : state.currentPath || '');

  // 上一批已经全部结束：清掉弹窗里的旧列表，避免"打开就是上次的记录"
  if (!activeBatch && state.uploadBatchId) {
    const finished = (state.uploadBatches || []).find((b) => b.id === state.uploadBatchId);
    if (finished?.finished) {
      state.uploadBatchId = null;
    }
  }

  renderUploadModalList();
  elements.uploadModal?.classList.remove('hidden');
}

export function hideUploadModal() {
  elements.uploadModal?.classList.add('hidden');
}

/* ------------------------------------------------------------------ */
/* 队列                                                                */
/* ------------------------------------------------------------------ */

export async function handleFiles(fileList, targetDir) {
  const files = Array.from(fileList || []);
  if (files.length === 0) return;

  if (state.deviceInfo && state.deviceInfo.allowUpload === false) {
    showToast(i18n.t('toast.upload.disabled', '当前设备已关闭上传权限'), 'warning');
    return;
  }

  const dir = typeof targetDir === 'string' ? targetDir : state.currentPath || '';

  // 空间校验：传 3GB 到只剩 2GB 的机器，别等最后一秒才失败
  const totalBytes = files.reduce((sum, f) => sum + (Number(f.size) || 0), 0);
  const free = Number(state.deviceInfo?.freeSpace);
  if (Number.isFinite(free) && free > 0 && totalBytes > free) {
    showToast(
      i18n
        .t('toast.upload.noSpace', '手机剩余空间不足：需要 {need}，仅剩 {free}')
        .replace('{need}', formatSize(totalBytes))
        .replace('{free}', formatSize(free)),
      'error'
    );
    return;
  }

  // 同名冲突：只在"目标目录 = 当前浏览目录"时能本地判断
  let overwrite = false;
  const isSameDir = dir === (state.currentPath || '');
  const conflicts = isSameDir
    ? files.filter((f) => (state.files || []).some((x) => !x.isDirectory && x.name === f.name))
    : [];

  // 先打开弹窗，让用户看到"保存到哪"，再问冲突
  updateUploadDestination(dir);
  elements.uploadModal?.classList.remove('hidden');

  if (conflicts.length > 0) {
    const choice = await askConflict(conflicts);
    if (!choice) {
      if (!(state.transfers || []).some((t) => t.status === 'uploading')) {
        hideUploadModal();
      }
      return;
    }
    overwrite = choice === 'overwrite';
  }

  if (files.length > 20) {
    showToast(
      i18n.t('toast.upload.manyFiles', '将上传 {count} 个文件').replace('{count}', files.length),
      'warning'
    );
  }

  const batch = {
    id: generateId(),
    dir,
    total: files.length,
    done: 0,
    failed: 0,
    canceled: 0,
    finished: false,
  };

  if (!state.uploadBatches) state.uploadBatches = [];
  state.uploadBatches.push(batch);
  state.uploadBatchId = batch.id;

  files.forEach((file) => enqueueUpload(file, dir, overwrite, batch));

  renderUploadModalList();
  pumpUploadQueue();
}

function enqueueUpload(file, dir, overwrite, batch) {
  const id = generateId();

  addTransferItem({
    id,
    name: file.name,
    size: file.size,
    status: 'queued',
    progress: 0,
    dir,
    filePath: null,
  });

  // addTransferItem 里用的是 state.uploadBatchId，这里补上批次
  const item = getTransfer(id);
  if (item) item.batchId = batch.id;

  state.uploadRuntime[id] = {
    file,
    dir,
    overwrite,
    xhr: null,
    batch,
    finished: false,
    lastPct: -1,
    lastAt: 0,
  };

  state.uploadQueue.push(id);
}

function pumpUploadQueue() {
  while (state.uploadActive < state.uploadConcurrency && state.uploadQueue.length > 0) {
    const id = state.uploadQueue.shift();
    const rt = state.uploadRuntime[id];
    if (!rt || !rt.file) continue;
    startUpload(id, rt);
  }
}

function startUpload(id, rt) {
  state.uploadActive += 1;

  addTransferItem({ id, status: 'uploading', progress: 0, errorMessage: null });
  renderUploadModalList();

  const { promise, xhr } = api.uploadFileViaXhr({
    path: rt.dir,
    file: rt.file,
    overwrite: rt.overwrite,
    onProgress: (pct) => {
      const now = Date.now();
      // 进度节流：整块列表重渲染，别被每个 progress event 打爆
      if (pct === rt.lastPct) return;
      if (pct < 100 && now - rt.lastAt < 150) return;
      rt.lastPct = pct;
      rt.lastAt = now;
      addTransferItem({ id, progress: pct, status: 'uploading' });
      renderUploadModalList();
    },
  });

  rt.xhr = xhr;

  promise
    .then((payload) => {
      const finalName = payload?.fileName || rt.file.name;
      const finalPath =
        payload?.filePath || (rt.dir ? `${rt.dir}/${finalName}` : finalName);

      addTransferItem({
        id,
        name: finalName,
        filePath: finalPath,
        status: 'completed',
        progress: 100,
      });

      const sameDir = (state.currentPath || '') === (rt.dir || '');
      if (sameDir) {
        state.pendingReveal = finalPath;
        state.pendingRevealSince = 0;
        scheduleRefresh(rt.dir);
      }

      finishTask(id, true, false);
    })
    .catch((e) => {
      const aborted = e?.message === 'aborted';
      addTransferItem({
        id,
        status: aborted ? 'canceled' : 'error',
        errorMessage: aborted ? null : e?.message || i18n.t('toast.upload.failed', '上传失败'),
      });
      finishTask(id, false, aborted);
    })
    .finally(() => {
      state.uploadActive = Math.max(0, state.uploadActive - 1);
      if (state.uploadRuntime[id]) state.uploadRuntime[id].xhr = null;
      pumpUploadQueue();
      renderUploadModalList();
    });
}

function finishTask(id, ok, canceled = false) {
  const rt = state.uploadRuntime[id];
  if (!rt || rt.finished) return;
  rt.finished = true;

  const batch = rt.batch;
  if (!batch) return;

  if (ok) batch.done += 1;
  else if (canceled) batch.canceled += 1;
  else batch.failed += 1;

  if (batch.done + batch.failed + batch.canceled >= batch.total && !batch.finished) {
    batch.finished = true;
    onBatchFinished(batch);
  }
}

function onBatchFinished(batch) {
  const dirLabel = formatDir(batch.dir);

  // 全部取消：不提示失败
  if (batch.done === 0 && batch.failed === 0) return;

  if (batch.failed === 0) {
    const msg =
      batch.total === 1
        ? i18n
            .t('toast.upload.savedOne', '“{name}”已保存到 {path}')
            .replace('{name}', lastCompletedName(batch))
            .replace('{path}', dirLabel)
        : i18n
            .t('toast.upload.savedMany', '{count} 个文件已保存到 {path}')
            .replace('{count}', batch.done)
            .replace('{path}', dirLabel);
    showToast(msg, 'success');
  } else if (batch.done > 0) {
    showToast(
      i18n
        .t('toast.upload.partial', '{ok} 个成功，{failed} 个失败')
        .replace('{ok}', batch.done)
        .replace('{failed}', batch.failed),
      'warning'
    );
  } else {
    showToast(i18n.t('toast.upload.allFailed', '上传失败'), 'error');
  }

  renderUploadModalList();
}

function lastCompletedName(batch) {
  const item = (state.transfers || []).find((t) => t.batchId === batch.id && t.status === 'completed');
  return item?.name || '';
}

function scheduleRefresh(dir) {
  if (refreshTimer) clearTimeout(refreshTimer);
  refreshTimer = setTimeout(() => {
    refreshTimer = null;
    if ((state.currentPath || '') === (dir || '')) {
      loadFiles(dir);
    }
  }, 400);
}

/* ------------------------------------------------------------------ */
/* 取消 / 重试                                                         */
/* ------------------------------------------------------------------ */

export function cancelUpload(id) {
  const rt = state.uploadRuntime[id];

  if (rt?.xhr) {
    try {
      rt.xhr.abort();
    } catch (_) {
      // ignore
    }
    return;
  }

  // 还在排队：直接移出队列
  state.uploadQueue = (state.uploadQueue || []).filter((x) => x !== id);
  addTransferItem({ id, status: 'canceled', progress: 0 });
  finishTask(id, false, true);
  renderUploadModalList();
}

export function retryUpload(id) {
  const rt = state.uploadRuntime[id];
  const item = getTransfer(id);

  if (!rt || !rt.file || !item) {
    showToast(i18n.t('toast.upload.retryUnavailable', '无法重试：请重新选择文件上传'), 'warning');
    return;
  }

  rt.finished = false;
  rt.lastPct = -1;
  rt.lastAt = 0;

  addTransferItem({ id, status: 'queued', progress: 0, errorMessage: null });
  state.uploadQueue.push(id);
  renderUploadModalList();
  pumpUploadQueue();
}

/* ------------------------------------------------------------------ */
/* 弹窗列表                                                            */
/* ------------------------------------------------------------------ */

function uploadStatusLabel(t) {
  switch (t.status) {
    case 'queued':
      return i18n.t('uploadModal.statusQueued', '等待上传');
    case 'uploading':
      return i18n.t('uploadModal.statusUploading', '上传中');
    case 'completed':
      return i18n.t('uploadModal.statusCompleted', '已保存');
    case 'error':
      return i18n.t('uploadModal.statusError', '上传失败');
    case 'canceled':
      return i18n.t('uploadModal.statusCanceled', '已取消');
    default:
      return t.status || '';
  }
}

export function renderUploadModalList() {
  if (!elements.uploadFileList) return;

  const batchId = state.uploadBatchId;
  // state.transfers 是"新的在前"，弹窗里按选择顺序展示更自然
  const items = (state.transfers || [])
    .filter((t) => t.batchId === batchId)
    .slice()
    .reverse();

  if (items.length === 0) {
    elements.uploadFileList.innerHTML = '';
    elements.uploadModalFooter?.classList.add('hidden');
    return;
  }

  elements.uploadModalFooter?.classList.remove('hidden');

  const html = items
    .map((t) => {
      const pct = Math.max(0, Math.min(100, Number(t.progress) || 0));
      const inFlight = t.status === 'uploading' || t.status === 'queued';
      const statusLabel = uploadStatusLabel(t);
      const statusLine =
        t.status === 'uploading'
          ? `${statusLabel} ${pct}%`
          : t.status === 'error'
            ? `${statusLabel}${t.errorMessage ? '：' + t.errorMessage : ''}`
            : statusLabel;

      const action = inFlight
        ? `<button data-upload-action="cancel" class="shrink-0 text-xs text-on-surface-variant hover:text-red-500 px-2 py-1">${i18n.t(
            'uploadModal.cancel',
            '取消'
          )}</button>`
        : t.status === 'error' || t.status === 'canceled'
          ? `<button data-upload-action="retry" class="shrink-0 text-xs text-primary px-2 py-1">${i18n.t(
              'uploadModal.retry',
              '重试'
            )}</button>`
          : `<span class="shrink-0 text-xs text-primary px-2 py-1">✓</span>`;

      return `
        <div class="flex items-center justify-between gap-3 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2" data-upload-id="${escapeHtml(
          t.id
        )}">
          <div class="min-w-0 flex-1">
            <div class="text-sm text-gray-900 dark:text-white truncate">${escapeHtml(t.name || '')}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400 truncate">${
              t.size != null ? escapeHtml(formatSize(t.size)) + ' · ' : ''
            }${escapeHtml(statusLine)}</div>
            ${
              t.status === 'uploading'
                ? `<div class="h-1 bg-gray-200 dark:bg-gray-600 rounded overflow-hidden mt-1">
                     <div class="h-full bg-primary rounded" style="width:${pct}%"></div>
                   </div>`
                : ''
            }
          </div>
          ${action}
        </div>
      `;
    })
    .join('');

  elements.uploadFileList.innerHTML = html;

  if (elements.uploadSummary) {
    const done = items.filter((t) => t.status === 'completed').length;
    const failed = items.filter((t) => t.status === 'error').length;
    const inFlight = items.some((t) => t.status === 'uploading' || t.status === 'queued');

    let summary;
    if (inFlight) {
      summary = i18n
        .t('uploadModal.summaryProgress', '{done}/{total} 已完成')
        .replace('{done}', done)
        .replace('{total}', items.length);
    } else if (failed === 0) {
      summary = i18n
        .t('uploadModal.summaryDone', '已保存到 {path}')
        .replace('{path}', formatDir(items[0]?.dir || ''));
    } else {
      summary = i18n
        .t('uploadModal.summaryFailed', '{failed} 个失败，可重试')
        .replace('{failed}', failed);
    }
    elements.uploadSummary.textContent = summary;
  }
}

/* ------------------------------------------------------------------ */
/* 同名冲突                                                            */
/* ------------------------------------------------------------------ */

function askConflict(files) {
  return new Promise((resolve) => {
    if (!elements.conflictModal) {
      resolve(null);
      return;
    }

    if (elements.conflictList) {
      elements.conflictList.innerHTML = files
        .map((f) => `<div class="truncate">${escapeHtml(f.name)}</div>`)
        .join('');
    }

    elements.conflictModal.classList.remove('hidden');

    const cleanup = () => {
      elements.conflictCancel?.removeEventListener('click', onCancel);
      elements.conflictKeep?.removeEventListener('click', onKeep);
      elements.conflictOverwrite?.removeEventListener('click', onOverwrite);
      elements.conflictModal?.classList.add('hidden');
    };

    const onCancel = () => {
      cleanup();
      resolve(null);
    };
    const onKeep = () => {
      cleanup();
      resolve('keep');
    };
    const onOverwrite = () => {
      cleanup();
      resolve('overwrite');
    };

    elements.conflictCancel?.addEventListener('click', onCancel);
    elements.conflictKeep?.addEventListener('click', onKeep);
    elements.conflictOverwrite?.addEventListener('click', onOverwrite);
  });
}

/* ------------------------------------------------------------------ */
/* 绑定                                                                */
/* ------------------------------------------------------------------ */

export function initUploadBindings() {
  elements.uploadBtn?.addEventListener('click', showUploadModal);
  elements.closeUpload?.addEventListener('click', hideUploadModal);
  elements.uploadDone?.addEventListener('click', hideUploadModal);

  elements.uploadDropZone?.addEventListener('click', () => elements.fileInput?.click());

  elements.fileInput?.addEventListener('change', (e) => {
    if (e.target.files?.length > 0) handleFiles(e.target.files);
    e.target.value = '';
  });

  // 上传弹窗拖拽
  elements.uploadDropZone?.addEventListener('dragover', (e) => {
    e.preventDefault();
    e.currentTarget.classList.add('border-primary', 'bg-primary-container');
  });
  elements.uploadDropZone?.addEventListener('dragleave', () => {
    elements.uploadDropZone?.classList.remove('border-primary', 'bg-primary-container');
  });
  elements.uploadDropZone?.addEventListener('drop', (e) => {
    e.preventDefault();
    elements.uploadDropZone?.classList.remove('border-primary', 'bg-primary-container');
    handleFiles(e.dataTransfer.files);
  });

  // 点击外部关闭
  elements.uploadModal?.addEventListener('click', (e) => {
    if (e.target === elements.uploadModal) hideUploadModal();
  });

  // 弹窗列表内：取消 / 重试
  elements.uploadFileList?.addEventListener('click', (e) => {
    const btn = e.target.closest('button[data-upload-action]');
    if (!btn) return;

    const row = btn.closest('[data-upload-id]');
    const id = row?.dataset.uploadId;
    if (!id) return;

    if (btn.dataset.uploadAction === 'cancel') cancelUpload(id);
    if (btn.dataset.uploadAction === 'retry') retryUpload(id);
  });

  // 根目录强提示里的一键去 Download
  elements.uploadGoDownload?.addEventListener('click', async () => {
    await loadFiles('Download');
    updateUploadDestination();
  });

  // 文件列表变化时同步"上传到"提示
  document.addEventListener('ft:path-changed', () => updateUploadDestination());

  // 全局拖拽（Drop anywhere 上传）
  document.addEventListener('dragover', (e) => {
    e.preventDefault();
    if (e.dataTransfer?.types?.includes('Files')) {
      elements.dropOverlay?.classList.remove('hidden');
    }
  });

  document.addEventListener('dragleave', (e) => {
    if (e.target === document.body || e.target === document.documentElement) {
      elements.dropOverlay?.classList.add('hidden');
    }
  });

  // 当用户在侧边栏/全局拖拽触发上传时，也保证能打开弹窗看到列表（更明确）
  const openModalAndHandle = (files, dir) => {
    if (state.deviceInfo && state.deviceInfo.allowUpload === false) {
      showToast(i18n.t('toast.upload.disabled', '当前设备已关闭上传权限'), 'warning');
      return;
    }
    elements.uploadModal?.classList.remove('hidden');
    handleFiles(files, dir);
  };

  document.addEventListener('drop', (e) => {
    e.preventDefault();
    elements.dropOverlay?.classList.add('hidden');
    if (e.dataTransfer?.files?.length > 0) openModalAndHandle(e.dataTransfer.files);
  });

  // 侧边栏拖拽区域
  elements.dropZoneSidebar?.addEventListener('dragover', (e) => {
    e.preventDefault();
    e.currentTarget.classList.add('drop-highlight');
  });

  elements.dropZoneSidebar?.addEventListener('dragleave', () => {
    elements.dropZoneSidebar?.classList.remove('drop-highlight');
  });

  elements.dropZoneSidebar?.addEventListener('drop', (e) => {
    e.preventDefault();
    elements.dropZoneSidebar.classList.remove('drop-highlight');
    openModalAndHandle(e.dataTransfer.files);
  });

  initDragToFolder();
  updateUploadDestination();
}

/**
 * 拖到文件夹行 = 上传到该文件夹
 */
function initDragToFolder() {
  const container = elements.fileContainer;
  if (!container) return;

  const rowFromEvent = (e) => {
    const row = e.target?.closest?.('.file-item');
    return row && row.dataset.isDir === 'true' ? row : null;
  };

  const clearRow = () => {
    dragRow?.classList.remove('drop-target-row');
    dragRow = null;
  };

  container.addEventListener('dragover', (e) => {
    if (!e.dataTransfer?.types?.includes('Files')) return;

    const row = rowFromEvent(e);
    if (row !== dragRow) {
      dragRow?.classList.remove('drop-target-row');
      dragRow = row;
      dragRow?.classList.add('drop-target-row');
    }

    if (row) {
      e.preventDefault();
      e.dataTransfer.dropEffect = 'copy';
    }
  });

  container.addEventListener('dragleave', (e) => {
    if (!rowFromEvent(e)) clearRow();
  });

  container.addEventListener('drop', (e) => {
    const row = rowFromEvent(e);
    clearRow();

    if (!row) return;

    e.preventDefault();
    e.stopPropagation();

    const dir = row.dataset.path || '';
    if (e.dataTransfer?.files?.length > 0) {
      elements.uploadModal?.classList.remove('hidden');
      handleFiles(e.dataTransfer.files, dir);
    }
  });
}
