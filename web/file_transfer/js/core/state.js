/**
 * 全局状态（单例）
 *
 * 说明：
 * - 这里集中管理页面运行时状态，便于模块间共享。
 * - 尽量只存“状态”，不要在这里放 DOM 或业务逻辑。
 */

export const state = {
  // 文件列表
  currentPath: '',
  files: [],
  filteredFiles: [],
  parentPath: null,
  canGoUp: false,

  // 设备信息（/api/info）
  deviceInfo: null,

  // 选择/视图
  selectedFiles: new Set(),
  viewMode: 'grid', // 'grid' | 'list'
  isLoading: false,
  searchQuery: '',
  sortBy: 'name', // 'name' | 'size' | 'date'
  sortOrder: 'asc', // 'asc' | 'desc'

  // 右键菜单/弹窗
  contextMenuTarget: null,
  renameTarget: null,

  // 传输任务
  transfers: [],

  // 聊天
  chatMessages: [],
  chatWebSocket: null,
  isChatOpen: false,
  unreadMessages: 0,

  // 聊天：文件上传队列
  chatUploadQueue: [],
  chatUploadTasks: {},
  chatUploadActiveCount: 0,
  chatUploadConcurrency: 2,

  // 页面导航
  activeNav: 'files', // 'files' | 'transfer' | 'chat'
};

