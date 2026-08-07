/**
 * 文件操作：新建文件夹、重命名、删除
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { showToast } from '../../ui/toast.js';
import * as api from '../../core/api.js';
import { loadFiles } from './files.js';

export async function createFolder() {
  const folderName = elements.newFolderName?.value.trim();

  if (!folderName) {
    showToast(i18n.t('toast.folder.nameRequired', '请输入文件夹名称'), 'error');
    return;
  }

  if (folderName.includes('/') || folderName.includes('\\')) {
    showToast(i18n.t('toast.folder.invalidName', '文件夹名称不能包含 / 或 \\'), 'error');
    return;
  }

  try {
    const { data } = await api.mkdir(state.currentPath, folderName);

    if (data?.success) {
      showToast(i18n.t('toast.folder.created', '文件夹创建成功'));
      elements.newFolderModal?.classList.add('hidden');
      if (elements.newFolderName) elements.newFolderName.value = '';
      loadFiles(state.currentPath);
    } else {
      showToast(data?.message || i18n.t('toast.folder.createFailed', '创建失败'), 'error');
    }
  } catch (e) {
    console.error('Create folder failed', e);
    showToast(i18n.t('toast.networkError', '网络错误'), 'error');
  }
}

export function showRenameModal(file) {
  state.renameTarget = file;
  if (elements.renameInput) {
    elements.renameInput.value = file.name;
    elements.renameInput.focus();
    elements.renameInput.select();
  }
  elements.renameModal?.classList.remove('hidden');
}

export async function renameFile() {
  const newName = elements.renameInput?.value.trim();
  const target = state.renameTarget;

  if (!target) return;

  if (!newName) {
    showToast(i18n.t('toast.rename.nameRequired', '请输入新名称'), 'error');
    return;
  }

  if (newName.includes('/') || newName.includes('\\')) {
    showToast(i18n.t('toast.rename.invalidName', '名称不能包含 / 或 \\'), 'error');
    return;
  }

  try {
    const { data } = await api.rename(target.path, newName);

    if (data?.success) {
      showToast(i18n.t('toast.rename.success', '重命名成功'));
      elements.renameModal?.classList.add('hidden');
      state.renameTarget = null;
      loadFiles(state.currentPath);
    } else {
      showToast(data?.message || i18n.t('toast.rename.failed', '重命名失败'), 'error');
    }
  } catch (e) {
    console.error('Rename failed', e);
    showToast(i18n.t('toast.networkError', '网络错误'), 'error');
  }
}

export async function deleteFiles(paths) {
  try {
    const { data } = await api.deletePaths(paths);

    if (data?.success) {
      showToast(i18n.t('toast.delete.success', '删除成功'));
      elements.deleteConfirmModal?.classList.add('hidden');
      state.selectedFiles.clear();
      state.contextMenuTarget = null;
      loadFiles(state.currentPath);
    } else {
      showToast(data?.message || i18n.t('toast.delete.failed', '删除失败'), 'error');
    }
  } catch (e) {
    console.error('Delete failed', e);
    showToast(i18n.t('toast.networkError', '网络错误'), 'error');
  }
}

