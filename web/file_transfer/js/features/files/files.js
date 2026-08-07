/**
 * 文件列表：加载 + 状态更新
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { showLoading, showError, hideError } from '../../ui/loading.js';
import * as api from '../../core/api.js';
import { renderFiles } from './render.js';
import { renderBreadcrumb, updateNavigation } from './navigation.js';
import { updateMultiSelectBar } from './selection.js';
import { updateFileStats } from './stats.js';
import { showLogin } from '../auth.js';

export async function loadFiles(path) {
  showLoading(true);
  hideError();

  try {
    const { data } = await api.listFiles(path);

    if (data?.requiresAuth) {
      showLogin();
      return;
    }

    if (data?.success) {
      handleFilesResponse(data);
    } else {
      showError(data?.message || i18n.t('files.loadFailed', '加载失败'));
    }
  } catch (e) {
    console.error('Load files failed', e);
    showError(i18n.t('files.networkError', '网络连接失败'));
  } finally {
    showLoading(false);
  }
}

export function handleFilesResponse(data) {
  state.files = data.files || [];
  state.currentPath = data.currentPath || '';
  state.parentPath = data.parentPath;
  state.canGoUp = data.canGoUp;
  state.selectedFiles.clear();

  hideError();

  renderFiles();
  renderBreadcrumb();
  updateNavigation();
  updateMultiSelectBar();
  updateFileStats();
}
