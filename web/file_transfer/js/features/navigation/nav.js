/**
 * 顶层导航：files / transfer / chat
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { loadFiles } from '../files/files.js';
import { connectChat } from '../chat/ws.js';
import { updateChatBadge } from '../chat/ui.js';

export function setActiveNav(nav) {
  state.activeNav = nav;

  const isDesktopLg = window.innerWidth >= 1024;

  // 主内容切换：移动端让 files/transfer/chat 都能占满主区域
  if (!isDesktopLg) {
    elements.fileContainer?.classList.toggle('hidden', nav !== 'files');
    elements.mobileTransferPanel?.classList.toggle('hidden', nav !== 'transfer');
    elements.mobileChatPanel?.classList.toggle('hidden', nav !== 'chat');

    // 移动端聊天时隐藏顶部工具栏
    elements.mainToolbar?.classList.toggle('hidden', nav === 'chat');
  } else {
    // 桌面端：主内容始终是 files
    elements.fileContainer?.classList.remove('hidden');
    elements.mainToolbar?.classList.remove('hidden');
  }

  // 桌面端右侧栏
  updateRightPanelForNav(nav);

  // 移动端底部按钮选中样式
  const activeCls = 'text-primary';
  const inactiveCls = 'text-on-surface-variant';

  [elements.navFilesMobile, elements.navTransferMobile, elements.navChatMobile].forEach((btn) => {
    if (!btn) return;
    btn.classList.remove(...activeCls.split(' '));
    btn.classList.add(...inactiveCls.split(' '));
  });

  const setBtnActive = (btn) => {
    if (!btn) return;
    btn.classList.remove(...inactiveCls.split(' '));
    btn.classList.add(...activeCls.split(' '));
  };

  if (nav === 'files') setBtnActive(elements.navFilesMobile);
  if (nav === 'transfer') setBtnActive(elements.navTransferMobile);
  if (nav === 'chat') setBtnActive(elements.navChatMobile);

  // 进入 files 时如果还没加载过，确保刷新
  if (nav === 'files' && state.files.length === 0 && !state.isLoading) {
    loadFiles(state.currentPath || '');
  }

  // 进入 chat 才连接
  if (nav === 'chat') {
    state.isChatOpen = true;
    if (!state.chatWebSocket || state.chatWebSocket.readyState === WebSocket.CLOSED) {
      connectChat();
    }
    state.unreadMessages = 0;
    updateChatBadge();
  }

  if (nav !== 'chat') {
    state.isChatOpen = false;
  }
}

export function updateRightPanelForNav(nav) {
  if (window.innerWidth < 1024) return;

  const showChat = (tabNav) => {
    elements.chatPanel?.classList.remove('hidden');
    elements.transferPanel?.classList.add('hidden');
    setRightTabActive('chat');
    if (tabNav === 'chat') state.isChatOpen = true;
  };

  const showTransfer = () => {
    elements.transferPanel?.classList.remove('hidden');
    elements.chatPanel?.classList.add('hidden');
    setRightTabActive('transfer');
    state.isChatOpen = false;
  };

  if (nav === 'files' || nav === 'chat') showChat(nav);
  if (nav === 'transfer') showTransfer();
}

function setRightTabActive(tab) {
  const activeBtnCls = ['bg-surface', 'text-on-surface'];
  const inactiveBtnCls = ['text-on-surface-variant'];

  const apply = (btn, active) => {
    if (!btn) return;
    btn.classList.remove(...activeBtnCls, ...inactiveBtnCls);
    btn.classList.add(...(active ? activeBtnCls : inactiveBtnCls));
  };

  apply(elements.rightTabChat, tab === 'chat');
  apply(elements.rightTabTransfer, tab === 'transfer');
}

