/**
 * 文件项交互：点击、右键、图片预览
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { toggleSelect } from './selection.js';
import { loadFiles } from './files.js';
import { downloadFile } from './download.js';

export function handleFileClick(_event, file) {
  // 已进入多选模式：点击=切换选中
  if (state.selectedFiles.size > 0) {
    toggleSelect(file.path);
    return;
  }

  if (file.isDirectory) {
    loadFiles(file.path);
    return;
  }

  if (isImageFile(file)) {
    openPreviewModal(file);
  } else {
    downloadFile(file.path, file.name);
  }
}

export function handleContextMenu(event, file) {
  event.preventDefault();
  state.contextMenuTarget = file;

  const menu = elements.contextMenu;
  if (!menu) return;

  menu.classList.remove('hidden');

  const x = Math.min(event.clientX, window.innerWidth - 180);
  const y = Math.min(event.clientY, window.innerHeight - 150);
  menu.style.left = `${x}px`;
  menu.style.top = `${y}px`;
}

export function isImageFile(file) {
  if (!file) return false;
  if (file.mimeType?.startsWith('image/')) return true;

  const name = (file.name || '').toLowerCase();
  return ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.avif', '.heic'].some((ext) => name.endsWith(ext));
}

export function openPreviewModal(file) {
  // 兼容旧 UI：优先 imageViewer，其次 previewModal
  const viewer = elements.imageViewer;
  const img = elements.imageViewerImg;

  if (viewer && img) {
    viewer.classList.remove('hidden');
    if (elements.imageViewerTitle) elements.imageViewerTitle.textContent = file.name || '';

    img.src = `/api/download?path=${encodeURIComponent(file.path)}`;

    // 下载按钮
    if (elements.imageViewerDownload) {
      elements.imageViewerDownload.onclick = () => {
        downloadFile(file.path, file.name);
      };
    }

    return;
  }

  // fallback preview modal
  if (elements.previewModal && elements.previewContent) {
    elements.previewModal.classList.remove('hidden');
    if (elements.previewTitle) elements.previewTitle.textContent = file.name || '';

    elements.previewContent.innerHTML = `<img src="/api/download?path=${encodeURIComponent(file.path)}" class="max-w-full max-h-[70vh] mx-auto rounded-lg" />`;
    if (elements.previewDownload) {
      elements.previewDownload.onclick = () => downloadFile(file.path, file.name);
    }
  }
}

export function closeImageViewer() {
  elements.imageViewer?.classList.add('hidden');
}

