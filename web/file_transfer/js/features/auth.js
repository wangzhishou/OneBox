/**
 * 鉴权流程
 *
 * 规则：
 * - 首次进入先尝试 /api/files?path= 以判断是否需要密码
 * - 需要密码则显示 overlay
 */

import { elements } from '../core/dom.js';
import { state } from '../core/state.js';
import { i18n } from '../core/i18n.js';
import * as api from '../core/api.js';
import { showLoading, showError, hideError } from '../ui/loading.js';
import { showToast } from '../ui/toast.js';
import { handleFilesResponse, loadFiles } from './files/files.js';
import { loadDeviceInfo } from './device/deviceInfo.js';

export async function checkAuth() {
  console.log('checkAuth - starting...');

  try {
    const { resp, data } = await api.listFiles('');
    console.log('checkAuth - response status:', resp.status);
    console.log('checkAuth - response data:', data);

    if (data?.requiresAuth) {
      showLogin();
      return;
    }

    if (data?.success) {
      elements.loginOverlay.style.display = 'none';
      // 某些流程 loading skeleton 可能还在，确保 UI 解锁
      hideError();
      showLoading(false);
      handleFilesResponse(data);
      loadDeviceInfo();
      return;
    }

    showLoading(false);
    showError(data?.message || i18n.t('files.loadFailed', '加载失败'));
  } catch (e) {
    console.error('Auth check failed', e);
    showLoading(false);
    showError(i18n.t('files.networkError', '网络连接失败'));
  }
}

export function showLogin() {
  elements.loginOverlay.style.display = 'flex';
  elements.passwordInput?.focus();
}

export async function login() {
  const password = elements.passwordInput?.value;
  if (!password) {
    elements.loginError.textContent = i18n.t('login.error.emptyPassword', '请输入密码');
    return;
  }

  try {
    const { data } = await api.auth(password);

    if (data?.success) {
      elements.loginOverlay.style.display = 'none';
      elements.loginError.textContent = '';
      // 登录成功后重新加载根目录
      await loadFiles('');
      loadDeviceInfo();
      return;
    }

    elements.loginError.textContent = data?.message || i18n.t('login.error.wrongPassword', '密码错误');
    elements.passwordInput?.select();
  } catch (e) {
    console.error('login failed', e);
    elements.loginError.textContent = i18n.t('login.error.failed', '登录失败，请重试');
    showToast(i18n.t('login.error.failed', '登录失败，请重试'), 'error');
  }
}

export function initAuthBindings() {
  // Enter 提交
  elements.passwordInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') login();
  });

  elements.loginBtn?.addEventListener('click', () => login());
}

