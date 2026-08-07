/**
 * DOM 元素缓存
 *
 * - 页面元素很多，集中管理能减少 querySelector/getElementById 的散落。
 * - 注意：这里仅负责“获取引用”，不要在这里写业务逻辑。
 */

export const elements = {};

export function getElements() {
  return {
    // 容器
    fileGrid: document.getElementById('file-grid'),
    fileList: document.getElementById('file-list'),
    breadcrumb: document.getElementById('breadcrumb'),
    loadingSkeleton: document.getElementById('loading-skeleton'),
    emptyState: document.getElementById('empty-state'),
    errorState: document.getElementById('error-state'),
    errorMessage: document.getElementById('error-message'),
    fileContainer: document.getElementById('file-container'),

    // 移动端 Tab 面板
    mobileTransferPanel: document.getElementById('mobile-transfer-panel'),
    mobileChatPanel: document.getElementById('mobile-chat-panel'),

    // 工具栏
    mainToolbar: document.getElementById('main-toolbar'),
    backBtn: document.getElementById('back-btn'),
    refreshBtn: document.getElementById('refresh-btn'),
    uploadBtn: document.getElementById('upload-btn'),
    viewGrid: document.getElementById('view-grid'),
    viewList: document.getElementById('view-list'),
    newFolderBtn: document.getElementById('new-folder-btn'),
    searchBtn: document.getElementById('search-btn'),
    sortBtn: document.getElementById('sort-btn'),
    sortMenu: document.getElementById('sort-menu'),
    searchBar: document.getElementById('search-bar'),
    searchInput: document.getElementById('search-input'),
    searchClose: document.getElementById('search-close'),

    // 文件统计
    fileStats: document.getElementById('file-stats'),
    folderCount: document.getElementById('folder-count'),
    fileCount: document.getElementById('file-count'),
    totalSize: document.getElementById('total-size'),

    // 多选
    multiSelectBar: document.getElementById('multi-select-bar'),
    selectCount: document.getElementById('select-count'),
    cancelSelect: document.getElementById('cancel-select'),
    selectAll: document.getElementById('select-all'),
    downloadSelected: document.getElementById('download-selected'),
    deleteSelected: document.getElementById('delete-selected'),

    // 登录
    loginOverlay: document.getElementById('login-overlay'),
    passwordInput: document.getElementById('password-input'),
    loginBtn: document.getElementById('login-btn'),
    loginError: document.getElementById('login-error'),

    // 存储信息
    storageBarMobile: document.getElementById('storage-bar-mobile'),
    storageTextMobile: document.getElementById('storage-text-mobile'),
    storageBarDesktop: document.getElementById('storage-bar-desktop'),
    storageTextDesktop: document.getElementById('storage-text-desktop'),
    deviceName: document.getElementById('device-name'),

    // 上传
    uploadModal: document.getElementById('upload-modal'),
    uploadDropZone: document.getElementById('upload-drop-zone'),
    fileInput: document.getElementById('file-input'),
    uploadFileList: document.getElementById('upload-file-list'),
    closeUpload: document.getElementById('close-upload'),
    dropOverlay: document.getElementById('drop-overlay'),
    dropZoneSidebar: document.getElementById('drop-zone-sidebar'),

    // 右侧栏（桌面端）
    rightPanel: document.getElementById('right-panel'),
    rightTabChat: document.getElementById('right-tab-chat'),
    rightTabTransfer: document.getElementById('right-tab-transfer'),

    // 传输面板
    transferPanel: document.getElementById('transfer-panel'),
    transferList: document.getElementById('transfer-list'),
    transferEmpty: document.getElementById('transfer-empty'),
    mobileTransferList: document.getElementById('mobile-transfer-list'),
    mobileTransferEmpty: document.getElementById('mobile-transfer-empty'),

    // 聊天（桌面端 + 移动端）
    chatPanel: document.getElementById('chat-panel'),
    mobileChatMessages: document.getElementById('mobile-chat-messages'),
    mobileChatInput: document.getElementById('mobile-chat-input'),
    mobileSendChatBtn: document.getElementById('mobile-send-chat-btn'),
    mobileChatFileBtn: document.getElementById('mobile-chat-file-btn'),
    mobileChatFileInput: document.getElementById('mobile-chat-file-input'),

    chatMessages: document.getElementById('chat-messages'),
    chatInput: document.getElementById('chat-input'),
    sendChatBtn: document.getElementById('send-chat-btn'),
    chatFileBtn: document.getElementById('chat-file-btn'),
    chatFileInput: document.getElementById('chat-file-input'),

    // 导航（移动端）
    navFilesMobile: document.getElementById('nav-files-mobile'),
    navTransferMobile: document.getElementById('nav-transfer-mobile'),
    navChatMobile: document.getElementById('nav-chat-mobile'),

    // 删除确认
    deleteConfirmModal: document.getElementById('delete-confirm-modal'),
    deleteConfirmText: document.getElementById('delete-confirm-text'),
    cancelDelete: document.getElementById('cancel-delete'),
    confirmDelete: document.getElementById('confirm-delete'),

    // 新建文件夹
    newFolderModal: document.getElementById('new-folder-modal'),
    newFolderName: document.getElementById('new-folder-name'),
    cancelNewFolder: document.getElementById('cancel-new-folder'),
    confirmNewFolder: document.getElementById('confirm-new-folder'),

    // 重命名
    renameModal: document.getElementById('rename-modal'),
    renameInput: document.getElementById('rename-input'),
    cancelRename: document.getElementById('cancel-rename'),
    confirmRename: document.getElementById('confirm-rename'),

    // 右键菜单
    contextMenu: document.getElementById('context-menu'),

    // Toast
    toast: document.getElementById('toast'),
    toastMessage: document.getElementById('toast-message'),

    // 图片查看
    imageViewer: document.getElementById('image-viewer'),
    imageViewerImg: document.getElementById('image-viewer-img'),
    imageViewerTitle: document.getElementById('image-viewer-title'),
    imageViewerClose: document.getElementById('image-viewer-close'),
    imageViewerDownload: document.getElementById('image-viewer-download'),

    // preview modal
    previewModal: document.getElementById('preview-modal'),
    previewTitle: document.getElementById('preview-title'),
    previewContent: document.getElementById('preview-content'),
    previewDownload: document.getElementById('preview-download'),
    previewClose: document.getElementById('preview-close'),

    // 徽标（未读）
    chatBadge: document.getElementById('chat-badge'),
    navChatBadge: document.getElementById('nav-chat-badge'),
    rightTabChatBadge: document.getElementById('right-tab-chat-badge'),

    // 文件详情
    infoModal: document.getElementById('info-modal'),
    closeInfo: document.getElementById('close-info'),
    infoContent: document.getElementById('info-content'),

    // 聊天：新消息提示
    desktopNewMessages: document.getElementById('desktop-new-messages'),
    mobileNewMessages: document.getElementById('mobile-new-messages'),
  };
}

export function initElements() {
  Object.assign(elements, getElements());
  return elements;
}
