/**
 * 文件信息弹窗（最小可用版）
 *
 * 原版右键菜单里“详细信息”只 toast，这里补齐成弹窗：
 * - 名称、类型、大小、修改时间、路径
 *
 * 不依赖后端新接口，纯前端使用 file 对象。
 */

import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { escapeHtml, formatSize, formatTime } from '../../core/utils.js';

export function showFileInfoModal(file) {
  if (!elements.infoModal || !elements.infoContent) {
    // 如果某些版本 HTML 没有 info-modal，就不报错
    return;
  }

  const rows = [];

  rows.push(row(i18n.t('infoModal.name', '名称'), escapeHtml(file?.name || '')));
  rows.push(row(i18n.t('infoModal.type', '类型'), file?.isDirectory ? i18n.t('fileType.folder', '文件夹') : (escapeHtml(file?.mimeType || i18n.t('fileType.file', '文件')))));
  if (!file?.isDirectory) rows.push(row(i18n.t('infoModal.size', '大小'), escapeHtml(formatSize(file?.size || 0))));
  if (file?.lastModified) rows.push(row(i18n.t('infoModal.modified', '修改时间'), escapeHtml(formatTime(file.lastModified))));
  if (file?.path) rows.push(row(i18n.t('infoModal.path', '路径'), `<div class="break-all text-xs">${escapeHtml(file.path)}</div>`));

  elements.infoContent.innerHTML = rows.join('');
  elements.infoModal.classList.remove('hidden');
}

export function hideFileInfoModal() {
  elements.infoModal?.classList.add('hidden');
}

function row(label, valueHtml) {
  return `
    <div class="flex items-start justify-between gap-4">
      <div class="text-sm text-gray-500 dark:text-gray-400 whitespace-nowrap">${escapeHtml(label)}</div>
      <div class="text-sm text-gray-900 dark:text-white text-right">${valueHtml}</div>
    </div>
  `;
}

