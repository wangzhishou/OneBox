/**
 * 应用入口（ES Module）
 *
 * 目标：
 * - 把原来 3000+ 行的 app.js 拆成可维护的模块
 * - 继续兼容 index.html 里少量 inline onclick="..."（比如面包屑、重试）
 */

import { initElements, elements } from './core/dom.js';
import { state } from './core/state.js';
import { i18n, detectLang } from './core/i18n.js';
import { debounce } from './core/utils.js';

import { initTheme } from './features/theme.js';
import { checkAuth, initAuthBindings } from './features/auth.js';

import { loadFiles } from './features/files/files.js';
import { renderFiles } from './features/files/render.js';
import { renderBreadcrumb } from './features/files/navigation.js';
import { handleFileClick, handleContextMenu, closeImageViewer, openPreviewModal, isImageFile } from './features/files/interactions.js';
import { toggleSelect, clearSelection, updateMultiSelectBar, selectAllVisible } from './features/files/selection.js';
import { downloadFile, downloadFileWithToast } from './features/files/download.js';
import { createFolder, showRenameModal, renameFile, deleteFiles } from './features/files/ops.js';
import { showFileInfoModal, hideFileInfoModal } from './features/files/infoModal.js';

import { setActiveNav, updateRightPanelForNav } from './features/navigation/nav.js';

import { initUploadBindings, handleFiles as handleUploadFiles } from './features/upload/upload.js';
import { renderTransfers } from './features/transfer/transfers.js';

import { connectChat } from './features/chat/ws.js';
import { sendChatMessage } from './features/chat/messages.js';
import { sendChatFiles, cancelChatUpload, retryChatUpload } from './features/chat/fileUpload.js';
import { updateChatBadge } from './features/chat/ui.js';
import { initChatTextareaAutosize, bindNewMessagesHintActions } from './features/chat/behavior.js';

import { showToast } from './ui/toast.js';

function exposeGlobalsForInlineHandlers() {
  // 给 HTML 里的 onclick 使用（兼容）
  window.state = state;

  window.loadFiles = loadFiles;
  window.renderFiles = renderFiles;
  window.renderBreadcrumb = renderBreadcrumb;

  window.handleFileClick = handleFileClick;
  window.handleContextMenu = handleContextMenu;

  window.toggleSelect = toggleSelect;
  window.clearSelection = clearSelection;

  // 原逻辑里 downloadFile 被多个地方调用；现在统一到一处
  window.downloadFile = downloadFile;
  window.downloadFileWithToast = downloadFileWithToast;

  window.createFolder = createFolder;
  window.showRenameModal = showRenameModal;
  window.renameFile = renameFile;
  window.deleteFiles = deleteFiles;

  window.setActiveNav = setActiveNav;
  window.updateRightPanelForNav = updateRightPanelForNav;

  // 聊天文件消息里的按钮
  window.cancelChatUpload = cancelChatUpload;
  window.retryChatUpload = retryChatUpload;

  // 文件信息弹窗
  window.showFileInfoModal = showFileInfoModal;
}

function initViewMode() {
  // 默认 grid
  state.viewMode = state.viewMode || 'grid';
}

function setViewMode(mode) {
  state.viewMode = mode;

  if (mode === 'grid') {
    elements.viewGrid?.classList.add('bg-surface-container-high', 'text-on-surface');
    elements.viewList?.classList.remove('bg-surface-container-high', 'text-on-surface');
    elements.fileGrid?.classList.remove('hidden');
    elements.fileList?.classList.add('hidden');
  } else {
    elements.viewList?.classList.add('bg-surface-container-high', 'text-on-surface');
    elements.viewGrid?.classList.remove('bg-surface-container-high', 'text-on-surface');
    elements.fileList?.classList.remove('hidden');
    elements.fileGrid?.classList.add('hidden');
  }

  renderFiles();
}

function initSearch() {
  const doSearch = () => {
    const query = elements.searchInput?.value.trim() || '';
    state.searchQuery = query;

    if (query) {
      state.filteredFiles = (state.files || []).filter((file) => (file.name || '').includes(query));
    } else {
      state.filteredFiles = [];
    }

    renderFiles();
  };

  // UX 改进： debounce，避免大目录下疯狂渲染
  const debounced = debounce(doSearch, 200);

  elements.searchBtn?.addEventListener('click', doSearch);
  elements.searchInput?.addEventListener('input', debounced);

  elements.searchClose?.addEventListener('click', () => {
    if (elements.searchInput) elements.searchInput.value = '';
    state.searchQuery = '';
    state.filteredFiles = [];
    renderFiles();
  });
}

function initSorting() {
  elements.sortBtn?.addEventListener('click', () => {
    elements.sortMenu?.classList.toggle('hidden');
  });

  document.querySelectorAll('.sort-option').forEach((item) => {
    item.addEventListener('click', (e) => {
      const sortBy = e.currentTarget.dataset.sortBy;
      const sortOrder = e.currentTarget.dataset.sortOrder;

      state.sortBy = sortBy;
      state.sortOrder = sortOrder;

      // 更新排序按钮文本（保持原 UI）
      const byEl = elements.sortBtn?.querySelector('.sort-by');
      const orderEl = elements.sortBtn?.querySelector('.sort-order');
      if (byEl) byEl.textContent = sortBy === 'name' ? i18n.t('sort.name', '名称') : sortBy === 'size' ? i18n.t('sort.size', '大小') : i18n.t('sort.date', '日期');
      if (orderEl) orderEl.textContent = sortOrder === 'asc' ? i18n.t('sort.asc', '升序') : i18n.t('sort.desc', '降序');

      elements.sortMenu?.classList.add('hidden');
      renderFiles();
    });
  });

  // 点击页面其他地方关闭排序菜单
  document.addEventListener('click', (e) => {
    const target = e.target;
    if (!elements.sortMenu || !elements.sortBtn) return;
    if (elements.sortBtn.contains(target) || elements.sortMenu.contains(target)) return;
    elements.sortMenu.classList.add('hidden');
  });
}

function initContextMenu() {
  // 点击页面关闭右键菜单
  document.addEventListener('click', () => {
    elements.contextMenu?.classList.add('hidden');
  });

  document.querySelectorAll('.context-menu-item').forEach((item) => {
    item.addEventListener('click', (e) => {
      const action = e.currentTarget.dataset.action;
      const file = state.contextMenuTarget;
      if (!file) return;

      switch (action) {
        case 'open':
          // 与列表点击一致：目录进入；图片预览；其他下载
          if (file.isDirectory) loadFiles(file.path);
          else if (window.isImageFile ? window.isImageFile(file) : (file.mimeType?.startsWith('image/'))) {
            // 复用 interactions.js 的预览逻辑
            window.openPreviewModal?.(file);
          } else {
            downloadFile(file.path, file.name);
          }
          break;
        case 'download':
          downloadFile(file.path, file.name);
          break;
        case 'rename':
          showRenameModal(file);
          break;
        case 'info':
          showFileInfoModal(file);
          break;
        case 'delete':
          if (elements.deleteConfirmText) {
            elements.deleteConfirmText.textContent = i18n.t('deleteModal.confirmOne', '确定要删除 "{name}" 吗？此操作不可撤销。').replace('{name}', file.name);
          }
          elements.deleteConfirmModal?.classList.remove('hidden');
          break;
      }

      // action 后关闭菜单
      elements.contextMenu?.classList.add('hidden');
    });
  });
}

function initKeyboardShortcuts() {
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      elements.uploadModal?.classList.add('hidden');
      elements.deleteConfirmModal?.classList.add('hidden');
      elements.newFolderModal?.classList.add('hidden');
      elements.renameModal?.classList.add('hidden');
      elements.contextMenu?.classList.add('hidden');
      state.renameTarget = null;
      if (state.selectedFiles.size > 0) clearSelection();
    }

    // Ctrl/Cmd + A 全选当前可见
    if ((e.ctrlKey || e.metaKey) && e.key === 'a') {
      if (e.target && e.target.matches && e.target.matches('input, textarea')) return;
      e.preventDefault();
      selectAllVisible();
    }

    // Backspace 返回上级（避免在输入框里触发）
    if (e.key === 'Backspace' && !(e.target && e.target.matches && e.target.matches('input, textarea'))) {
      if (state.canGoUp && state.parentPath != null) {
        loadFiles(state.parentPath);
      }
    }
  });
}

function initModals() {
  // 新建文件夹
  elements.newFolderBtn?.addEventListener('click', () => {
    elements.newFolderModal?.classList.remove('hidden');
    elements.newFolderName?.focus();
  });

  elements.cancelNewFolder?.addEventListener('click', () => {
    elements.newFolderModal?.classList.add('hidden');
    if (elements.newFolderName) elements.newFolderName.value = '';
  });

  elements.confirmNewFolder?.addEventListener('click', createFolder);

  elements.newFolderName?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') createFolder();
  });

  elements.newFolderModal?.addEventListener('click', (e) => {
    if (e.target === elements.newFolderModal) {
      elements.newFolderModal.classList.add('hidden');
      if (elements.newFolderName) elements.newFolderName.value = '';
    }
  });

  // 重命名
  elements.cancelRename?.addEventListener('click', () => {
    elements.renameModal?.classList.add('hidden');
    state.renameTarget = null;
  });

  elements.confirmRename?.addEventListener('click', renameFile);
  elements.renameInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') renameFile();
  });
  elements.renameModal?.addEventListener('click', (e) => {
    if (e.target === elements.renameModal) {
      elements.renameModal.classList.add('hidden');
      state.renameTarget = null;
    }
  });

  // 删除确认
  elements.deleteSelected?.addEventListener('click', () => {
    const count = state.selectedFiles.size;
    if (elements.deleteConfirmText) {
      elements.deleteConfirmText.textContent = i18n.t('deleteModal.confirmMany', '确定要删除选中的 {count} 个文件吗？此操作不可撤销。').replace('{count}', count);
    }
    elements.deleteConfirmModal?.classList.remove('hidden');
  });

  elements.cancelDelete?.addEventListener('click', () => {
    elements.deleteConfirmModal?.classList.add('hidden');
  });

  elements.confirmDelete?.addEventListener('click', () => {
    const pathsToDelete = Array.from(state.selectedFiles);
    if (pathsToDelete.length > 0) deleteFiles(pathsToDelete);
    else if (state.contextMenuTarget) deleteFiles([state.contextMenuTarget.path]);
  });

  elements.deleteConfirmModal?.addEventListener('click', (e) => {
    if (e.target === elements.deleteConfirmModal) {
      elements.deleteConfirmModal.classList.add('hidden');
    }
  });

  // 文件信息弹窗
  elements.closeInfo?.addEventListener('click', hideFileInfoModal);
  elements.infoModal?.addEventListener('click', (e) => {
    if (e.target === elements.infoModal) hideFileInfoModal();
  });
}

function initFileToolbar() {
  elements.backBtn?.addEventListener('click', () => {
    if (state.canGoUp && state.parentPath != null) loadFiles(state.parentPath);
  });

  elements.refreshBtn?.addEventListener('click', () => loadFiles(state.currentPath));

  elements.viewGrid?.addEventListener('click', () => setViewMode('grid'));
  elements.viewList?.addEventListener('click', () => setViewMode('list'));

  // 多选
  elements.cancelSelect?.addEventListener('click', clearSelection);
  elements.downloadSelected?.addEventListener('click', () => {
    state.selectedFiles.forEach((path) => {
      const f = state.files.find((x) => x.path === path);
      if (f && !f.isDirectory) downloadFile(f.path, f.name);
    });
  });
}

function initImageViewerBindings() {
  elements.imageViewerClose?.addEventListener('click', closeImageViewer);
  elements.imageViewer?.addEventListener('click', (e) => {
    if (e.target === elements.imageViewer) closeImageViewer();
  });

  // preview modal fallback（如果页面仍保留）
  elements.previewClose?.addEventListener('click', () => elements.previewModal?.classList.add('hidden'));
  elements.previewModal?.addEventListener('click', (e) => {
    if (e.target === elements.previewModal) elements.previewModal?.classList.add('hidden');
  });
}

function initNavBindings() {
  elements.navFilesMobile?.addEventListener('click', () => setActiveNav('files'));
  elements.navTransferMobile?.addEventListener('click', () => setActiveNav('transfer'));
  elements.navChatMobile?.addEventListener('click', () => setActiveNav('chat'));

  elements.rightTabChat?.addEventListener('click', () => {
    // 切换右侧栏 tab 不影响主区域文件页
    connectChat();
    state.unreadMessages = 0;
    updateChatBadge();
    updateRightPanelForNav('chat');
  });

  elements.rightTabTransfer?.addEventListener('click', () => updateRightPanelForNav('transfer'));
}

function initChatBindings() {
  // 桌面端
  elements.sendChatBtn?.addEventListener('click', () => sendChatMessage('desktop'));
  elements.chatInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendChatMessage('desktop');
    }
  });

  elements.chatFileBtn?.addEventListener('click', () => elements.chatFileInput?.click());
  elements.chatFileInput?.addEventListener('change', (e) => {
    if (e.target.files?.length > 0) sendChatFiles(Array.from(e.target.files));
    e.target.value = '';
  });

  // 移动端
  elements.mobileSendChatBtn?.addEventListener('click', () => sendChatMessage('mobile'));
  elements.mobileChatInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendChatMessage('mobile');
    }
  });

  elements.mobileChatFileBtn?.addEventListener('click', () => elements.mobileChatFileInput?.click());
  elements.mobileChatFileInput?.addEventListener('change', (e) => {
    if (e.target.files?.length > 0) sendChatFiles(Array.from(e.target.files));
    e.target.value = '';
  });
}

// ---- main ----
document.addEventListener('DOMContentLoaded', async () => {
  initElements();
  exposeGlobalsForInlineHandlers();

  await i18n.load(detectLang());
  initTheme();
  initViewMode();

  initAuthBindings();
  initUploadBindings();

  initNavBindings();
  initFileToolbar();
  initSorting();
  initSearch();
  initContextMenu();
  initKeyboardShortcuts();
  initModals();
  initImageViewerBindings();
  initChatBindings();

  // 聊天体验增强
  initChatTextareaAutosize({ maxLines: 6 });
  bindNewMessagesHintActions(() => {
    // 优先滚动到当前可见容器/最近一个容器
    const c = (elements.mobileChatPanel && !elements.mobileChatPanel.classList.contains('hidden'))
      ? elements.mobileChatMessages
      : elements.chatMessages;
    try {
      c.scrollTop = c.scrollHeight;
    } catch (_) {}
  });

  // 默认：文件页
  setActiveNav('files');

  // 初始化 transfers 面板
  renderTransfers();

  // 首次进入先检查是否需要密码，再加载文件/设备信息
  await checkAuth();

  // 体验优化：页面首次打开就预连接聊天（但不打扰用户）
  state.isChatOpen = false;
  connectChat();

  // 让 interactions.js 的图片判断/预览也可以被 context menu 等地方复用
  window.openPreviewModal = window.openPreviewModal || openPreviewModal;
  window.isImageFile = window.isImageFile || isImageFile;
});
