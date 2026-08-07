/**
 * 设备信息（存储空间、设备名、是否允许上传等）
 */

import { elements } from '../../core/dom.js';
import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { formatSize } from '../../core/utils.js';
import * as api from '../../core/api.js';

export async function loadDeviceInfo() {
  try {
    const { data } = await api.deviceInfo();

    if (data?.requiresAuth) return;

    state.deviceInfo = data;

    const used = data.totalSpace - data.freeSpace;
    const percent = Math.min(100, (used / data.totalSpace) * 100);
    const usedText = `${formatSize(used)} / ${formatSize(data.totalSpace)}`;

    // 更新桌面端和移动端存储显示
    if (elements.storageBarMobile) elements.storageBarMobile.style.width = `${percent}%`;
    if (elements.storageTextMobile) elements.storageTextMobile.textContent = `${Math.round(percent)}%`;
    if (elements.storageBarDesktop) elements.storageBarDesktop.style.width = `${percent}%`;
    if (elements.storageTextDesktop) elements.storageTextDesktop.textContent = usedText;
    if (elements.deviceName) elements.deviceName.textContent = data.deviceName || i18n.t('device.defaultName', '设备');

    // UX：如果服务端关闭上传权限，直接禁用上传入口（按钮/拖拽区域）
    const allowUpload = data.allowUpload !== false;
    if (!allowUpload) {
      if (elements.uploadBtn) {
        elements.uploadBtn.setAttribute('disabled', 'disabled');
        elements.uploadBtn.classList.add('opacity-50', 'cursor-not-allowed');
        elements.uploadBtn.title = i18n.t('toast.upload.disabled', '当前设备已关闭上传权限');
      }
      if (elements.dropZoneSidebar) {
        elements.dropZoneSidebar.classList.add('opacity-50', 'cursor-not-allowed');
      }
    } else {
      if (elements.uploadBtn) {
        elements.uploadBtn.removeAttribute('disabled');
        elements.uploadBtn.classList.remove('opacity-50', 'cursor-not-allowed');
      }
      if (elements.dropZoneSidebar) {
        elements.dropZoneSidebar.classList.remove('opacity-50', 'cursor-not-allowed');
      }
    }

    return data;
  } catch (e) {
    console.error('Load device info failed', e);
    return null;
  }
}
