/**
 * 下载相关
 *
 * 体验优化：
 * - 统一 downloadFile 实现（原 app.js 里有多个重复定义）。
 * - downloadFileWithToast 会在下载前探测 404/401 并提示更友好。
 */

import { i18n } from '../../core/i18n.js';
import * as api from '../../core/api.js';
import { showToast } from '../../ui/toast.js';

export function downloadFile(filePath, fileName) {
  const encodedPath = encodeURIComponent(filePath);
  const link = document.createElement('a');
  link.href = `/api/download?path=${encodedPath}`;
  link.download = fileName || 'file';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}

export async function downloadFileWithToast(filePath, fileName) {
  if (!filePath) {
    showToast(i18n.t('toast.download.noLink', '文件未生成下载链接'), 'warning');
    return;
  }

  try {
    const resp = await api.downloadProbe(filePath);

    if (!resp.ok) {
      if (resp.status === 404) {
        showToast(i18n.t('toast.download.notFound', '文件不存在或已被删除'), 'error');
      } else if (resp.status === 401 || resp.status === 403) {
        showToast(i18n.t('toast.download.noPermission', '没有权限下载该文件'), 'error');
      } else {
        showToast(i18n.t('toast.download.failed', '下载失败（{status}）').replace('{status}', resp.status), 'error');
      }
      return;
    }

    downloadFile(filePath, fileName || 'file');
  } catch (e) {
    console.error('downloadFileWithToast error', e);
    showToast(i18n.t('toast.download.networkError', '下载失败：网络错误'), 'error');
  }
}

