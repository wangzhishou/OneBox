/**
 * 上传：弹窗 + 拖拽 + XHR 上传
 *
 * 说明：
 * - 复用服务端 /api/upload?path= 接口
 * - 上传完成后刷新当前目录
 */

import { elements } from '../../core/dom.js';
import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { showToast } from '../../ui/toast.js';
import * as api from '../../core/api.js';
import { loadFiles } from '../files/files.js';
import { addTransferItem } from '../transfer/transfers.js';

function renderUploadFileList(files) {
  if (!elements.uploadFileList) return;

  const html = files
    .map((f) => {
      const size = f.size != null ? `${(f.size / 1024 / 1024).toFixed(2)} MB` : '';
      return `
        <div class="flex items-center justify-between gap-3 bg-gray-50 dark:bg-gray-700/50 border border-gray-200 dark:border-gray-700 rounded-lg px-3 py-2">
          <div class="min-w-0">
            <div class="text-sm text-gray-900 dark:text-white truncate">${f.name}</div>
            <div class="text-xs text-gray-500 dark:text-gray-400">${size}</div>
          </div>
          <div class="text-xs text-gray-500 dark:text-gray-400">${i18n.t('uploadModal.pending', '待上传')}</div>
        </div>
      `;
    })
    .join('');

  elements.uploadFileList.innerHTML = html;
}

export function showUploadModal() {
  // 体验优化：服务端声明不允许上传时，直接给 toast，并禁用按钮视觉态
  if (state.deviceInfo && state.deviceInfo.allowUpload === false) {
    showToast(i18n.t('toast.upload.disabled', '当前设备已关闭上传权限'), 'warning');
    return;
  }

  elements.uploadModal?.classList.remove('hidden');
}

export function hideUploadModal() {
  elements.uploadModal?.classList.add('hidden');
}

export function handleFiles(fileList) {
  const files = Array.from(fileList || []);
  if (files.length === 0) return;

  // 在弹窗里显示选中的待上传列表
  renderUploadFileList(files);

  // 如果文件很多，给一个提示
  if (files.length > 20) {
    showToast(i18n.t('toast.upload.manyFiles', '将上传 {count} 个文件').replace('{count}', files.length), 'warning');
  }

  files.forEach((file) => uploadFile(file));
}

export function uploadFile(file) {
  const id = `${Date.now()}-${Math.random().toString(16).slice(2)}`;

  // transfer item
  addTransferItem({
    id,
    name: file.name,
    size: file.size,
    status: 'uploading',
    progress: 0,
  });

  api
    .uploadFileViaXhr({
      path: state.currentPath,
      file,
      onProgress: (pct) => {
        addTransferItem({ id, progress: pct, status: 'uploading', updateOnly: true });
      },
    })
    .then(() => {
      addTransferItem({ id, progress: 100, status: 'completed', updateOnly: true });
      showToast(i18n.t('toast.upload.done', '上传成功'), 'success');
      loadFiles(state.currentPath);
    })
    .catch((e) => {
      console.error('upload failed', e);
      addTransferItem({ id, status: 'error', errorMessage: e?.message || i18n.t('toast.upload.failed', '上传失败'), updateOnly: true });
      showToast(i18n.t('toast.upload.failed', '上传失败'), 'error');
    });
}

export function initUploadBindings() {
  elements.uploadBtn?.addEventListener('click', showUploadModal);
  elements.closeUpload?.addEventListener('click', hideUploadModal);

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
  const openModalAndHandle = (files) => {
    if (state.deviceInfo && state.deviceInfo.allowUpload === false) {
      showToast(i18n.t('toast.upload.disabled', '当前设备已关闭上传权限'), 'warning');
      return;
    }
    elements.uploadModal?.classList.remove('hidden');
    handleFiles(files);
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

  elements.dropZoneSidebar?.addEventListener('dragleave', (e) => {
    e.currentTarget.classList.remove('drop-highlight');
  });

  elements.dropZoneSidebar?.addEventListener('drop', (e) => {
    e.preventDefault();
    e.currentTarget.classList.remove('drop-highlight');
    openModalAndHandle(e.dataTransfer.files);
  });
}
