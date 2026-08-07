/**
 * 多选相关
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { renderFiles } from './render.js';

export function toggleSelect(path) {
  if (state.selectedFiles.has(path)) state.selectedFiles.delete(path);
  else state.selectedFiles.add(path);

  renderFiles();
  updateMultiSelectBar();
}

export function clearSelection() {
  state.selectedFiles.clear();
  renderFiles();
  updateMultiSelectBar();
}

export function updateMultiSelectBar() {
  const count = state.selectedFiles.size;
  if (!elements.multiSelectBar || !elements.selectCount) return;

  if (count > 0) {
    elements.multiSelectBar.classList.remove('hidden');
    elements.selectCount.textContent = i18n.t('selection.count', '已选择 {count} 项').replace('{count}', count);
  } else {
    elements.multiSelectBar.classList.add('hidden');
  }
}

export function selectAllVisible() {
  const filesToRender = state.searchQuery ? state.filteredFiles : state.files;
  (filesToRender || []).forEach((f) => state.selectedFiles.add(f.path));
  renderFiles();
  updateMultiSelectBar();
}

