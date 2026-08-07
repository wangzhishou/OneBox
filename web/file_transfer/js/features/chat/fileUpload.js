/**
 * 聊天发送文件：走 HTTP 上传，然后用 WS 广播一个 file message。
 */

import { state } from '../../core/state.js';
import { i18n } from '../../core/i18n.js';
import { generateId } from '../../core/utils.js';
import { showToast } from '../../ui/toast.js';
import { waitForChatWebSocketOpen } from './ws.js';
import { handleChatMessage, updateChatMessage } from './messages.js';
import * as api from '../../core/api.js';

export async function sendChatFiles(files) {
  if (!files || files.length === 0) return;

  const ok = await waitForChatWebSocketOpen(2500);
  if (!ok) {
    showToast(i18n.t('toast.chat.cannotConnect', '无法建立聊天连接'), 'error');
    return;
  }

  for (const file of files) {
    const messageId = generateId();

    const fileMessage = {
      id: messageId,
      type: 'file',
      fileName: file.name,
      fileSize: file.size,
      status: 'queued',
      progress: 0,
      sender: 'browser',
      timestamp: Date.now(),
    };
    handleChatMessage(fileMessage);

    state.chatUploadTasks[messageId] = { file, xhr: null, state: 'queued' };
    state.chatUploadQueue.push({ messageId, file });
  }

  pumpChatUploadQueue();
}

export function pumpChatUploadQueue() {
  const limit = state.chatUploadConcurrency || 2;

  while (state.chatUploadActiveCount < limit && state.chatUploadQueue.length > 0) {
    const job = state.chatUploadQueue.shift();
    if (!job) break;

    const existing = state.chatUploadTasks?.[job.messageId];
    if (!existing || existing.state === 'canceled') continue;

    state.chatUploadActiveCount++;
    updateChatMessage(job.messageId, { status: 'uploading', progress: 0, errorMessage: null });
    existing.state = 'uploading';

    uploadChatFileViaXhr({ messageId: job.messageId, file: job.file })
      .catch(() => {})
      .finally(() => {
        state.chatUploadActiveCount = Math.max(0, (state.chatUploadActiveCount || 1) - 1);
        pumpChatUploadQueue();
      });
  }
}

function broadcastChatFileMessageToWebSocket({ messageId, fileName, fileSize, filePath }) {
  try {
    const ok = state.chatWebSocket && state.chatWebSocket.readyState === WebSocket.OPEN;
    if (!ok) return;

    const wsMsg = {
      id: messageId,
      type: 'file',
      content: '',
      sender: 'browser',
      timestamp: Date.now(),
      fileName,
      fileSize,
      filePath,
    };

    state.chatWebSocket.send(JSON.stringify(wsMsg));
  } catch (e) {
    console.warn('broadcastChatFileMessageToWebSocket failed', e);
  }
}

function uploadChatFileViaXhr({ messageId, file }) {
  return new Promise((resolve, reject) => {
    const url = `/api/upload?path=${encodeURIComponent('')}`;

    const formData = new FormData();
    formData.append('file', file);

    const xhr = new XMLHttpRequest();
    state.chatUploadTasks[messageId] = { xhr, file, state: 'uploading' };

    xhr.open('POST', url);
    xhr.withCredentials = true;

    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        const pct = Math.round((e.loaded / e.total) * 100);
        updateChatMessage(messageId, { progress: pct });
      }
    };

    xhr.onload = () => {
      const task = state.chatUploadTasks?.[messageId];
      if (task) task.xhr = null;

      let payload = null;
      try {
        payload = JSON.parse(xhr.responseText || '{}');
      } catch (_) {}

      if (xhr.status === 200 && payload && payload.success) {
        const finalFileName = payload.fileName || file.name;
        const finalFilePath = payload.filePath;

        updateChatMessage(messageId, {
          status: 'completed',
          progress: 100,
          fileName: finalFileName,
          filePath: finalFilePath,
          fileSize: file.size,
          errorMessage: null,
        });

        broadcastChatFileMessageToWebSocket({
          messageId,
          fileName: finalFileName,
          fileSize: file.size,
          filePath: finalFilePath,
        });

        resolve(payload);
        return;
      }

      const serverMsg = payload?.message || i18n.t('chat.upload.failedHttp', '上传失败（{status}）').replace('{status}', xhr.status || 'unknown');
      updateChatMessage(messageId, { status: 'error', errorMessage: serverMsg });
      if (state.chatUploadTasks?.[messageId]) state.chatUploadTasks[messageId].state = 'error';
      reject(new Error(serverMsg));
    };

    xhr.onerror = () => {
      if (state.chatUploadTasks?.[messageId]) {
        state.chatUploadTasks[messageId].xhr = null;
        state.chatUploadTasks[messageId].state = 'error';
      }
      updateChatMessage(messageId, { status: 'error', errorMessage: i18n.t('chat.upload.networkError', '网络错误：无法连接到手机端服务') });
      reject(new Error('network error'));
    };

    xhr.onabort = () => {
      if (state.chatUploadTasks?.[messageId]) {
        state.chatUploadTasks[messageId].xhr = null;
        state.chatUploadTasks[messageId].state = 'canceled';
      }
      reject(new Error('aborted'));
    };

    xhr.send(formData);
  });
}

export function cancelChatUpload(messageId) {
  const task = state.chatUploadTasks?.[messageId];

  if (task && task.state === 'queued') {
    state.chatUploadQueue = (state.chatUploadQueue || []).filter((j) => j.messageId !== messageId);
  }

  if (task && task.xhr) {
    try {
      task.xhr.abort();
    } catch (_) {}
  }

  if (state.chatUploadTasks?.[messageId]) {
    state.chatUploadTasks[messageId].state = 'canceled';
  }

  updateChatMessage(messageId, { status: 'canceled', errorMessage: i18n.t('chat.upload.canceled', '已取消') });
  pumpChatUploadQueue();
}

export function retryChatUpload(messageId) {
  const task = state.chatUploadTasks?.[messageId];
  if (!task || !task.file) return;

  if (task.state === 'uploading') return;

  updateChatMessage(messageId, { status: 'queued', progress: 0, errorMessage: null });
  state.chatUploadQueue.push({ messageId, file: task.file });
  task.state = 'queued';

  pumpChatUploadQueue();
}

