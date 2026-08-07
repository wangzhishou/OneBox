/**
 * 文件统计（左侧栏：文件夹数、文件数、总大小）
 *
 * 原页面 HTML 里已经有 #file-stats / #folder-count / #file-count / #total-size
 * 但之前模块化时还没补上，这里补齐。
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { formatSize } from '../../core/utils.js';

export function updateFileStats() {
  const files = state.files || [];

  if (!elements.fileStats || !elements.folderCount || !elements.fileCount || !elements.totalSize) {
    return;
  }

  let folderCount = 0;
  let fileCount = 0;
  let totalBytes = 0;

  for (const f of files) {
    if (f?.isDirectory) folderCount++;
    else {
      fileCount++;
      totalBytes += Number(f?.size) || 0;
    }
  }

  // 只有加载成功后才展示统计
  elements.fileStats.classList.remove('hidden');
  elements.folderCount.textContent = String(folderCount);
  elements.fileCount.textContent = String(fileCount);

  // #total-size 节点内部包含 <span class="font-medium">0 B</span>（另一个是 i18n 文案）
  const sizeSpan = elements.totalSize.querySelector('span.font-medium');
  if (sizeSpan) sizeSpan.textContent = formatSize(totalBytes);
}

