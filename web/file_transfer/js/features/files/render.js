/**
 * 文件列表渲染（网格/列表）
 *
 * 注意：这里沿用原来 HTML 字符串模板（包含 onclick="..."），
 * 因为页面现有结构依赖 inline handler。
 *
 * 后续如果想进一步提升可维护性，可以改成事件委托 + dataset。
 */

import { state } from '../../core/state.js';
import { elements } from '../../core/dom.js';
import { i18n } from '../../core/i18n.js';
import { escapeHtml, formatSize, formatTime } from '../../core/utils.js';

export function renderFiles() {
  const gridContainer = elements.fileGrid;
  const listContainer = elements.fileList;

  if (!gridContainer || !listContainer) {
    console.error('renderFiles - containers not found');
    return;
  }

  // 搜索过滤后的列表
  const filesToRender = state.searchQuery ? state.filteredFiles : state.files;

  if (!filesToRender || filesToRender.length === 0) {
    gridContainer.innerHTML = '';
    listContainer.innerHTML = '';
    elements.emptyState?.classList.remove('hidden');
    return;
  }

  elements.emptyState?.classList.add('hidden');

  // 排序：文件夹在前，文件在后
  const sortedFiles = [...filesToRender].sort((a, b) => {
    if (a.isDirectory !== b.isDirectory) return a.isDirectory ? -1 : 1;

    let comparison = 0;
    if (state.sortBy === 'name') comparison = a.name.localeCompare(b.name);
    else if (state.sortBy === 'size') comparison = (a.size || 0) - (b.size || 0);
    else if (state.sortBy === 'date') comparison = new Date(a.lastModified) - new Date(b.lastModified);

    return state.sortOrder === 'asc' ? comparison : -comparison;
  });

  gridContainer.innerHTML = sortedFiles.map((file) => renderGridItem(file)).join('');
  listContainer.innerHTML = sortedFiles.map((file) => renderListItem(file)).join('');
}

function renderGridItem(file) {
  const isSelected = state.selectedFiles.has(file.path);
  const icon = getFileIcon(file);
  const meta = file.isDirectory ? '' : formatSize(file.size);

  return `
    <div class="file-item group relative bg-surface rounded-xl p-3 cursor-pointer transition-all hover:shadow-lg hover:-translate-y-0.5 border-2 ${isSelected ? 'border-primary bg-primary-container' : 'border-transparent'}"
         data-path="${escapeHtml(file.path)}"
         data-is-dir="${file.isDirectory}"
         onclick="handleFileClick(event, ${JSON.stringify(file).replace(/"/g, '&quot;')})"
         oncontextmenu="handleContextMenu(event, ${JSON.stringify(file).replace(/"/g, '&quot;')})">

        <!-- 选择框 -->
        <div class="absolute top-2 left-2 ${state.selectedFiles.size > 0 || 'opacity-0 group-hover:opacity-100'} transition-opacity">
            <div class="w-5 h-5 rounded border-2 ${isSelected ? 'bg-primary border-primary' : 'border-outline bg-surface-container'} flex items-center justify-center"
                 onclick="event.stopPropagation(); toggleSelect('${escapeHtml(file.path)}')">
                ${isSelected ? '<svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>' : ''}
            </div>
        </div>

        <!-- 下载按钮 -->
        ${!file.isDirectory ? `
        <div class="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <button class="p-1.5 bg-surface-container rounded-lg shadow hover:bg-primary hover:text-white transition-colors"
                    onclick="event.stopPropagation(); downloadFile('${escapeHtml(file.path)}', '${escapeHtml(file.name)}')">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
                </svg>
            </button>
        </div>
        ` : ''}

        <!-- 图标 -->
        <div class="w-16 h-16 mx-auto mb-3 flex items-center justify-center">
            ${icon}
        </div>

        <!-- 文件名 -->
        <p class="text-sm text-center text-on-surface truncate" title="${escapeHtml(file.name)}">
            ${escapeHtml(file.name)}
        </p>

        <!-- 文件大小/时间 -->
        <p class="text-xs text-center text-on-surface-variant mt-1">
            ${meta}${file.lastModified ? (meta ? ' · ' : '') + formatTime(file.lastModified) : ''}
        </p>
    </div>
  `;
}

function renderListItem(file) {
  const isSelected = state.selectedFiles.has(file.path);
  const icon = getFileIconSmall(file);

  return `
    <div class="file-item flex items-center gap-3 px-4 py-3 bg-surface rounded-xl cursor-pointer transition-colors hover:bg-surface-container-high border-2 ${isSelected ? 'border-primary bg-primary-container' : 'border-transparent'}"
         data-path="${escapeHtml(file.path)}"
         data-is-dir="${file.isDirectory}"
         onclick="handleFileClick(event, ${JSON.stringify(file).replace(/"/g, '&quot;')})"
         oncontextmenu="handleContextMenu(event, ${JSON.stringify(file).replace(/"/g, '&quot;')})">

        <!-- 选择框 -->
        <div class="w-5 h-5 rounded border-2 ${isSelected ? 'bg-primary border-primary' : 'border-outline'} flex items-center justify-center flex-shrink-0"
             onclick="event.stopPropagation(); toggleSelect('${escapeHtml(file.path)}')">
            ${isSelected ? '<svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"/></svg>' : ''}
        </div>

        <!-- 图标 -->
        <div class="w-10 h-10 flex items-center justify-center flex-shrink-0">
            ${icon}
        </div>

        <!-- 文件信息 -->
        <div class="flex-1 min-w-0">
            <p class="text-sm text-on-surface truncate">${escapeHtml(file.name)}</p>
            <p class="text-xs text-on-surface-variant">
                ${file.isDirectory ? i18n.t('fileType.folder', '文件夹') : formatSize(file.size)}
                ${file.lastModified ? ' · ' + formatTime(file.lastModified) : ''}
            </p>
        </div>

        <!-- 操作按钮 -->
        ${!file.isDirectory ? `
        <button class="p-2 text-on-surface-variant hover:text-primary hover:bg-primary-container rounded-lg transition-colors touch-target"
                onclick="event.stopPropagation(); downloadFile('${escapeHtml(file.path)}', '${escapeHtml(file.name)}')">
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4"/>
            </svg>
        </button>
        ` : ''}
    </div>
  `;
}

function getFileIcon(file) {
  if (file.isDirectory) {
    return `<svg class="w-16 h-16 text-yellow-500" viewBox="0 0 24 24" fill="currentColor">
      <path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/>
    </svg>`;
  }

  if (file.mimeType) {
    if (file.mimeType.startsWith('image/')) {
      return `<img src="/api/thumbnail?path=${encodeURIComponent(file.path)}"
                  class="w-16 h-16 object-cover rounded-lg bg-gray-100 dark:bg-gray-700"
                  loading="lazy"
                  onerror="this.outerHTML='${getDefaultFileIconSVG()}'" />`;
    }
    if (file.mimeType.startsWith('video/')) {
      return `<svg class="w-16 h-16 text-purple-500" viewBox="0 0 24 24" fill="currentColor">
        <path d="M18 4l2 4h-3l-2-4h-2l2 4h-3l-2-4H8l2 4H7L5 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V4h-4z"/>
      </svg>`;
    }
    if (file.mimeType.startsWith('audio/')) {
      return `<svg class="w-16 h-16 text-pink-500" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/>
      </svg>`;
    }
    if (file.mimeType === 'application/pdf') {
      return `<svg class="w-16 h-16 text-red-500" viewBox="0 0 24 24" fill="currentColor">
        <path d="M20 2H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-8.5 7.5c0 .83-.67 1.5-1.5 1.5H9v2H7.5V7H10c.83 0 1.5.67 1.5 1.5v1zm5 2c0 .83-.67 1.5-1.5 1.5h-2.5V7H15c.83 0 1.5.67 1.5 1.5v3zm4-3H19v1h1.5V11H19v2h-1.5V7h3v1.5zM9 9.5h1v-1H9v1zM4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm10 5.5h1v-3h-1v3z"/>
      </svg>`;
    }
    if (file.mimeType.includes('zip') || file.mimeType.includes('rar') || file.mimeType.includes('7z')) {
      return `<svg class="w-16 h-16 text-orange-500" viewBox="0 0 24 24" fill="currentColor">
        <path d="M20 6h-8l-2-2H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm-2 6h-2v2h2v2h-2v2h-2v-2h2v-2h-2v-2h2v-2h-2V8h2v2h2v2z"/>
      </svg>`;
    }
  }

  const ext = file.name.split('.').pop()?.toLowerCase();
  const codeExts = ['js', 'ts', 'py', 'java', 'kt', 'swift', 'c', 'cpp', 'h', 'css', 'html', 'xml', 'json'];
  const docExts = ['doc', 'docx', 'txt', 'md', 'rtf'];
  const sheetExts = ['xls', 'xlsx', 'csv'];
  const presentExts = ['ppt', 'pptx'];

  if (codeExts.includes(ext)) {
    return `<svg class="w-16 h-16 text-green-500" viewBox="0 0 24 24" fill="currentColor">
      <path d="M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"/>
    </svg>`;
  }
  if (docExts.includes(ext)) {
    return `<svg class="w-16 h-16 text-blue-500" viewBox="0 0 24 24" fill="currentColor">
      <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
    </svg>`;
  }
  if (sheetExts.includes(ext)) {
    return `<svg class="w-16 h-16 text-emerald-500" viewBox="0 0 24 24" fill="currentColor">
      <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/>
    </svg>`;
  }
  if (presentExts.includes(ext)) {
    return `<svg class="w-16 h-16 text-orange-500" viewBox="0 0 24 24" fill="currentColor">
      <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14zM7 10h2v7H7zm4-3h2v10h-2zm4 6h2v4h-2z"/>
    </svg>`;
  }

  return getDefaultFileIconSVG();
}

function getDefaultFileIconSVG() {
  return `<svg class="w-16 h-16 text-gray-400" viewBox="0 0 24 24" fill="currentColor">
    <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
  </svg>`;
}

function getFileIconSmall(file) {
  if (file.isDirectory) {
    return `<svg class="w-10 h-10 text-yellow-500" viewBox="0 0 24 24" fill="currentColor">
      <path d="M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z"/>
    </svg>`;
  }

  if (file.mimeType?.startsWith('image/')) {
    return `<img src="/api/thumbnail?path=${encodeURIComponent(file.path)}"
                class="w-10 h-10 object-cover rounded bg-gray-100 dark:bg-gray-700"
                loading="lazy" />`;
  }

  return `<svg class="w-10 h-10 text-gray-400" viewBox="0 0 24 24" fill="currentColor">
    <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
  </svg>`;
}

