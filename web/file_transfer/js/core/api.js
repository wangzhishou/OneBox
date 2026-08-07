/**
 * 与手机端 FileTransferServer 的 HTTP API 交互
 *
 * 约定：所有请求默认带上 credentials，保证 cookie/session 正常使用。
 */

const JSON_HEADERS = { 'Content-Type': 'application/json' };

async function readJsonSafe(resp) {
  try {
    return await resp.json();
  } catch (_) {
    return null;
  }
}

export async function listFiles(path) {
  const resp = await fetch(`/api/files?path=${encodeURIComponent(path || '')}`, {
    credentials: 'include',
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

export async function auth(password) {
  const resp = await fetch('/api/auth', {
    method: 'POST',
    headers: JSON_HEADERS,
    credentials: 'include',
    body: JSON.stringify({ password }),
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

export async function deviceInfo() {
  const resp = await fetch('/api/info', {
    credentials: 'include',
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

export async function mkdir(path, name) {
  const resp = await fetch('/api/mkdir', {
    method: 'POST',
    headers: JSON_HEADERS,
    credentials: 'include',
    body: JSON.stringify({ path, name }),
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

export async function rename(path, newName) {
  const resp = await fetch('/api/rename', {
    method: 'POST',
    headers: JSON_HEADERS,
    credentials: 'include',
    body: JSON.stringify({ path, newName }),
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

export async function deletePaths(paths) {
  const resp = await fetch('/api/delete', {
    method: 'POST',
    headers: JSON_HEADERS,
    credentials: 'include',
    body: JSON.stringify({ paths }),
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

export async function downloadProbe(filePath) {
  const url = `/api/download?path=${encodeURIComponent(filePath)}`;
  const resp = await fetch(url, {
    method: 'GET',
    headers: { Range: 'bytes=0-0' },
    credentials: 'include',
  });
  return resp;
}

export function uploadFileViaXhr({ path = '', file, onProgress }) {
  return new Promise((resolve, reject) => {
    const formData = new FormData();
    formData.append('file', file);

    const url = `/api/upload?path=${encodeURIComponent(path || '')}`;

    const xhr = new XMLHttpRequest();
    xhr.open('POST', url);
    xhr.withCredentials = true;

    xhr.upload.onprogress = (e) => {
      if (!onProgress) return;
      if (e.lengthComputable) {
        const pct = Math.round((e.loaded / e.total) * 100);
        onProgress(pct, e);
      }
    };

    xhr.onload = () => {
      let payload = null;
      try {
        payload = JSON.parse(xhr.responseText || '{}');
      } catch (_) {
        // ignore
      }

      if (xhr.status === 200 && payload && payload.success) {
        resolve(payload);
        return;
      }

      reject(new Error(payload?.message || `upload failed (${xhr.status || 'unknown'})`));
    };

    xhr.onerror = () => reject(new Error('network error'));
    xhr.onabort = () => reject(new Error('aborted'));

    xhr.send(formData);

    // 额外返回 xhr（如果调用方需要 cancel）
    resolve.xhr = xhr;
  });
}

export async function chatHistory(channelId) {
  const url = channelId
    ? `/api/chat/history?channelId=${encodeURIComponent(channelId)}`
    : '/api/chat/history';

  const resp = await fetch(url, {
    credentials: 'include',
  });
  const data = await readJsonSafe(resp);
  return { resp, data };
}

