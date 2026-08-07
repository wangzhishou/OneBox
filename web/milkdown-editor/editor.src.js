/**
 * Milkdown Markdown 编辑器
 *
 * 功能：
 * - 所见即所得 Markdown 编辑
 * - Material 3 主题适配
 * - 与 Android 原生代码双向通信
 * - 图片插入（从相册选择或输入URL）
 * - 底部工具栏（复制/粘贴/全选/格式等）
 */

import { Editor, rootCtx, defaultValueCtx, editorViewCtx } from '@milkdown/core';
import { commonmark } from '@milkdown/preset-commonmark';
import { gfm } from '@milkdown/preset-gfm';
import { history, redoCommand, undoCommand } from '@milkdown/plugin-history';
import { listener, listenerCtx } from '@milkdown/plugin-listener';
import { clipboard } from '@milkdown/plugin-clipboard';
import { indent } from '@milkdown/plugin-indent';
import { trailing } from '@milkdown/plugin-trailing';
import { prism } from '@milkdown/plugin-prism';
import { math } from '@milkdown/plugin-math';

// KaTeX CSS 按需加载标记
let katexCSSLoaded = false;

// 动态加载 KaTeX CSS（从本地 assets 按需加载）
function loadKatexCSS() {
    if (katexCSSLoaded) return Promise.resolve();

    return new Promise((resolve) => {
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        // 使用相对路径，baseURL 已经是 appassets 路径
        link.href = 'katex.bundle.css';
        link.onload = () => {
            katexCSSLoaded = true;
            resolve();
        };
        link.onerror = () => {
            console.error('Failed to load KaTeX CSS');
            resolve(); // 即使失败也继续
        };
        document.head.appendChild(link);
    });
}

import {
    toggleStrongCommand,
    toggleEmphasisCommand,
    wrapInBlockquoteCommand,
    wrapInBulletListCommand,
    wrapInOrderedListCommand,
    insertHrCommand,
    createCodeBlockCommand,
    turnIntoTextCommand,
    wrapInHeadingCommand
} from '@milkdown/preset-commonmark';
import {
    toggleStrikethroughCommand
} from '@milkdown/preset-gfm';
import { callCommand, replaceAll, getMarkdown, insert } from '@milkdown/utils';

// 全局变量
let editor = null;
let isReady = false;
let pendingContent = null;
let currentPlaceholder = '输入内容...';
// 从模板注入的全局变量获取 storage key，避免时序问题
let STORAGE_KEY = window.MILKDOWN_STORAGE_KEY || 'milkdown_draft_default';
let autoSaveTimer = null;
let isSourceView = false; // 是否在源码视图
let isInitializing = true; // 是否正在初始化，用于忽略初始化时的内容变化
let lastNotifiedMarkdown = null; // 最近一次已通知 Android 的内容
let notifyAndroidTimer = null;
let mermaidRenderTimer = null;
let mermaidRenderEpoch = 0;
let mermaidThemeMode = null;


// 获取当前 Markdown 内容
function getCurrentMarkdown() {
    if (isSourceView) {
        const sourceTextarea = document.getElementById('source-textarea');
        return sourceTextarea ? sourceTextarea.value : '';
    }

    if (!isReady || !editor) {
        return loadDraft();
    }

    try {
        return editor.action(getMarkdown());
    } catch (e) {
        console.error('getCurrentMarkdown error:', e);
        return '';
    }
}

// 自动保存到 localStorage
function saveDraft(content = getCurrentMarkdown()) {
    try {
        if (content && content.trim()) {
            localStorage.setItem(STORAGE_KEY, content);
        } else {
            localStorage.removeItem(STORAGE_KEY);
        }
    } catch (e) {
        console.error('saveDraft error:', e);
    }
}

// 从 localStorage 加载草稿
function loadDraft() {
    try {
        return localStorage.getItem(STORAGE_KEY) || '';
    } catch (e) {
        console.error('loadDraft error:', e);
        return '';
    }
}

// 清除草稿
function clearDraft() {
    try {
        localStorage.removeItem(STORAGE_KEY);
    } catch (e) {
        console.error('clearDraft error:', e);
    }
}

// 防抖自动保存
function debounceSaveDraft(content) {
    if (autoSaveTimer) {
        clearTimeout(autoSaveTimer);
    }
    autoSaveTimer = setTimeout(() => saveDraft(content), 500);
}

function syncEditorBaseline(markdown = getCurrentMarkdown()) {
    lastNotifiedMarkdown = markdown;
    updatePlaceholderState(markdown);
    scheduleMermaidRender(markdown);
}

function getProseMirrorElement() {
    return document.querySelector('.milkdown .ProseMirror');
}

function isEditorEffectivelyEmpty() {
    if (isSourceView) {
        const sourceTextarea = document.getElementById('source-textarea');
        return !sourceTextarea || sourceTextarea.value.trim() === '';
    }

    if (!editor) return true;

    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        const { doc } = view.state;

        if (doc.childCount === 0) return true;
        if (doc.childCount > 1) return false;

        const firstChild = doc.firstChild;
        if (!firstChild) return true;
        if (firstChild.type.name !== 'paragraph') return false;

        return firstChild.content.size === 0 && doc.textContent.trim() === '';
    } catch (e) {
        console.error('isEditorEffectivelyEmpty error:', e);
        return getCurrentMarkdown().trim() === '';
    }
}

function hidePlaceholderImmediately() {
    const editorEl = getProseMirrorElement();
    if (editorEl) {
        editorEl.classList.remove('is-empty');
    }
}

function setPlaceholderFocused(isFocused) {
    const editorEl = getProseMirrorElement();
    if (!editorEl) return;

    editorEl.classList.toggle('is-focused', Boolean(isFocused));
}

function parseFencedCodeBlocks(markdown) {
    const lines = String(markdown || '').split(/\r?\n/);
    const blocks = [];
    let index = 0;

    while (index < lines.length) {
        const line = lines[index];
        const openMatch = line.match(/^[ \t]{0,3}([`~]{3,})[ \t]*([^\s`~]+)?.*$/);
        if (!openMatch) {
            index += 1;
            continue;
        }

        const fence = openMatch[1];
        const fenceChar = fence[0];
        const fenceSize = fence.length;
        const language = (openMatch[2] || '').trim().toLowerCase();
        const codeLines = [];
        index += 1;

        while (index < lines.length) {
            const currentLine = lines[index];
            const closePattern = new RegExp(`^[ \\t]{0,3}${fenceChar}{${fenceSize},}[ \\t]*$`);
            if (closePattern.test(currentLine)) {
                break;
            }
            codeLines.push(currentLine);
            index += 1;
        }

        blocks.push({
            language,
            code: codeLines.join('\n')
        });

        if (index < lines.length) {
            index += 1;
        }
    }

    return blocks;
}

function getMermaidApi() {
    return window.mermaid || null;
}

function ensureMermaidInitialized() {
    const mermaid = getMermaidApi();
    if (!mermaid) return false;

    const theme = document.documentElement.classList.contains('dark') ? 'dark' : 'default';
    if (mermaidThemeMode === theme) {
        return true;
    }

    mermaid.initialize({
        startOnLoad: false,
        theme,
        securityLevel: 'loose',
        fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
        sequence: {
            actorFontSize: 13,
            messageFontSize: 13,
            noteFontSize: 12,
            useMaxWidth: true,
            htmlLabels: false
        },
        flowchart: {
            useMaxWidth: true,
            htmlLabels: false
        },
        class: {
            htmlLabels: false
        },
        state: {
            htmlLabels: false
        },
        er: {
            htmlLabels: false
        }
    });

    mermaidThemeMode = theme;
    return true;
}

function getMermaidPreviewContainer() {
    return document.getElementById('mermaid-preview-container');
}

function getMermaidPreviewContent() {
    return document.getElementById('mermaid-preview-content');
}

function hideMermaidPreviewPanel() {
    const container = getMermaidPreviewContainer();
    const content = getMermaidPreviewContent();
    if (container) {
        container.style.display = 'none';
        container.style.top = '';
        container.style.left = '';
        container.style.width = '';
    }
    if (content) {
        content.innerHTML = '';
    }

    document.querySelectorAll('.milkdown .ProseMirror pre.mermaid-source-hidden').forEach(pre => {
        pre.classList.remove('mermaid-source-hidden');
    });
}

function ensureMermaidPreviewPanelVisible() {
    const container = getMermaidPreviewContainer();
    if (container) {
        container.style.display = 'block';
    }
}

function positionMermaidPreview(anchorElement) {
    const container = getMermaidPreviewContainer();
    const editorContainer = document.getElementById('editor-container');
    if (!container || !editorContainer || !anchorElement) {
        return;
    }

    const containerRect = editorContainer.getBoundingClientRect();
    const anchorRect = anchorElement.getBoundingClientRect();
    const top = anchorRect.top - containerRect.top + editorContainer.scrollTop;
    const left = anchorRect.left - containerRect.left + editorContainer.scrollLeft;
    const width = anchorRect.width;
    const minHeight = Math.max(anchorRect.height, 120);

    container.style.top = `${top}px`;
    container.style.left = `${left}px`;
    container.style.width = `${width}px`;
    container.style.minHeight = `${minHeight}px`;
}

async function renderMermaidPreview(code, previewKey, anchorElement) {
    if (!ensureMermaidInitialized()) {
        hideMermaidPreviewPanel();
        return;
    }

    const preview = getMermaidPreviewContent();
    if (!preview) {
        return;
    }

    ensureMermaidPreviewPanelVisible();
    positionMermaidPreview(anchorElement);
    preview.dataset.previewKey = previewKey;
    preview.classList.remove('error');

    if (!code.trim()) {
        preview.innerHTML = '<div class="mermaid-preview-empty">Mermaid 代码块为空</div>';
        return;
    }

    const renderToken = String(++mermaidRenderEpoch);
    preview.dataset.renderToken = renderToken;

    try {
        const mermaid = getMermaidApi();
        const renderResult = await mermaid.render(
            `mermaid-preview-${Date.now()}-${previewKey.replace(/[^a-z0-9_-]/gi, '-')}`,
            code
        );

        if (preview.dataset.renderToken !== renderToken) {
            return;
        }

        const svg = typeof renderResult === 'string' ? renderResult : renderResult.svg;
        preview.innerHTML = svg;

        if (renderResult && typeof renderResult.bindFunctions === 'function') {
            renderResult.bindFunctions(preview);
        }
    } catch (error) {
        if (preview.dataset.renderToken !== renderToken) {
            return;
        }

        preview.classList.add('error');
        const escapedCode = code
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        preview.innerHTML = `<div class="mermaid-preview-error">Mermaid 渲染失败: ${error.message || error}\n\n${escapedCode}</div>`;
    }
}

function updateMermaidPreviews(markdown = getCurrentMarkdown()) {
    if (isSourceView || !editor) {
        hideMermaidPreviewPanel();
        return;
    }

    const fencedBlocks = parseFencedCodeBlocks(markdown);
    const firstMermaidIndex = fencedBlocks.findIndex(block => block.language === 'mermaid');
    if (firstMermaidIndex < 0) {
        hideMermaidPreviewPanel();
        return;
    }

    const preElements = Array.from(document.querySelectorAll('.milkdown .ProseMirror pre'));
    const anchorElement = preElements[firstMermaidIndex] || null;
    const firstMermaidBlock = fencedBlocks[firstMermaidIndex];
    preElements.forEach(pre => pre.classList.remove('mermaid-source-hidden'));
    if (anchorElement) {
        anchorElement.classList.add('mermaid-source-hidden');
    }
    renderMermaidPreview(firstMermaidBlock.code, 'first-mermaid-block', anchorElement);
}

function scheduleMermaidRender(markdown = getCurrentMarkdown()) {
    if (mermaidRenderTimer) {
        clearTimeout(mermaidRenderTimer);
    }

    mermaidRenderTimer = setTimeout(() => {
        updateMermaidPreviews(markdown);
    }, 120);
}

function notifyContentChange(force = false) {
    const markdown = getCurrentMarkdown();

    updatePlaceholderState(markdown);
    scheduleMermaidRender(markdown);

    if (!force && markdown === lastNotifiedMarkdown) {
        return;
    }

    lastNotifiedMarkdown = markdown;
    debounceSaveDraft(markdown);

    if (notifyAndroidTimer) {
        clearTimeout(notifyAndroidTimer);
    }

    notifyAndroidTimer = setTimeout(() => {
        if (window.Android && window.Android.onContentChanged) {
            window.Android.onContentChanged('');
        }
    }, 150);
}

// Android Bridge 接口
window.AndroidBridge = {
    // 设置 Markdown 内容
    // markdown 为 null/undefined 时从 localStorage 加载草稿
    // markdown 为空字符串时清空编辑器
    setContent: function(markdown) {
        if (!isReady) {
            pendingContent = markdown;
            return;
        }
        try {
            // 标记为正在设置内容，忽略内容变化回调
            isInitializing = true;

            // 只有当 markdown 为 null 或 undefined 时才加载草稿
            // 空字符串表示要清空编辑器
            const content = (markdown === null || markdown === undefined) ? loadDraft() : markdown;

            // 检测内容是否包含数学公式，如果有则加载 KaTeX CSS
            if (content && (content.includes('$') || content.includes('\\('))) {
                loadKatexCSS();
            }

            // 使用 replaceAll 设置内容（包括空字符串）
            editor.action(replaceAll(content || ''));

            // 等编辑器事务和 DOM 完成后再同步基线，避免首次内容被误判为脏数据
            setTimeout(() => {
                try {
                    syncEditorBaseline(editor.action(getMarkdown()));
                } catch (e) {
                    syncEditorBaseline(content || '');
                }
                isInitializing = false;
            }, 80);
        } catch (e) {
            console.error('setContent error:', e);
            isInitializing = false;
        }
    },

    // 获取 Markdown 内容
    getContent: function() {
        return getCurrentMarkdown();
    },

    // 清除草稿（保存成功后调用）
    clearDraft: function() {
        clearDraft();
    },

    // 设置占位符
    setPlaceholder: function(placeholder) {
        currentPlaceholder = placeholder || '输入内容...';
        const editorEl = getProseMirrorElement();
        if (editorEl) {
            editorEl.setAttribute('data-placeholder', currentPlaceholder);
        }
    },

    // 设置主题（浅色/深色）
    setTheme: function(isDark) {
        if (isDark) {
            document.documentElement.classList.add('dark');
        } else {
            document.documentElement.classList.remove('dark');
        }
        mermaidThemeMode = null;
        scheduleMermaidRender();
    },

    // 设置 Material 3 主题颜色
    setColors: function(colorsJson) {
        try {
            const colors = JSON.parse(colorsJson);
            const root = document.documentElement;
            Object.keys(colors).forEach(key => {
                root.style.setProperty(`--md-${key}`, colors[key]);
            });
        } catch (e) {
            console.error('setColors error:', e);
        }
    },

    // 设置字体样式（响应系统字体缩放及外部 TextStyle 更改）
    setTextStyle: function(fontSizePx, lineHeightPx, letterSpacingPx, fontWeight) {
        try {
            const root = document.documentElement;
            root.style.setProperty('--md-font-size', fontSizePx + 'px');
            root.style.setProperty('--md-line-height', lineHeightPx + 'px');
            root.style.setProperty('--md-letter-spacing', letterSpacingPx + 'px');
            root.style.setProperty('--md-font-weight', fontWeight);
        } catch (e) {
            console.error('setTextStyle error:', e);
        }
    },

    // 插入图片（从 Android 回调）
    insertImage: function(url, alt) {
        if (!isReady || !editor) return;
        try {
            const imageMarkdown = `![${alt || ''}](${url})`;
            editor.action(insert(imageMarkdown));
            notifyContentChange();
        } catch (e) {
            console.error('insertImage error:', e);
        }
    },

    // 插入链接（从 Android 回调）
    insertLink: function(text, url) {
        if (!isReady || !editor) return;
        try {
            const linkMarkdown = `[${text || '链接'}](${url})`;
            editor.action(insert(linkMarkdown));
            notifyContentChange();
        } catch (e) {
            console.error('insertLink error:', e);
        }
    },

    // 设置只读模式
    setReadOnly: function(readOnly) {
        if (!editor) return;
        try {
            const view = editor.action(ctx => ctx.get(editorViewCtx));
            if (view) {
                view.setProps({ editable: () => !readOnly });
            }
            // 隐藏/显示工具栏
            const toolbar = document.getElementById('toolbar');
            if (toolbar) {
                toolbar.style.display = readOnly ? 'none' : 'flex';
            }
        } catch (e) {
            console.error('setReadOnly error:', e);
        }
    },

    // 聚焦编辑器
    focus: function() {
        if (!editor) return;
        try {
            const view = editor.action(ctx => ctx.get(editorViewCtx));
            if (view) {
                view.focus();
            }
        } catch (e) {
            console.error('focus error:', e);
        }
    },

    // 检查编辑器是否就绪，如果就绪则通知 Android
    checkReady: function() {
        if (isReady) {
            notifyReady();
        }
    }
};

// 收起当前选区(将光标折叠到选区末尾),撤销/重做后使用
function collapseCurrentSelection() {
    if (!editor) return;

    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        if (!view) return;

        const { state } = view;
        const collapsePos = state.selection.to;
        const tr = state.tr.setSelection(
            state.selection.constructor.near(state.doc.resolve(collapsePos))
        );
        view.dispatch(tr);
        view.focus();
    } catch (e) {
        console.error('collapseCurrentSelection error:', e);
    }

    try {
        const domSelection = window.getSelection();
        domSelection?.removeAllRanges();
    } catch (e) {
        console.error('collapseCurrentSelection dom error:', e);
    }
}

// 更新 placeholder 显示状态
function updatePlaceholderState() {
    const editorEl = getProseMirrorElement();
    if (!editorEl) return;

    try {
        const isEmpty = isEditorEffectivelyEmpty();

        // 根据内容是否为空添加/移除 class
        if (isEmpty) {
            editorEl.classList.add('is-empty');
            editorEl.setAttribute('data-placeholder', currentPlaceholder);
        } else {
            editorEl.classList.remove('is-empty');
        }
    } catch (e) {
        console.error('updatePlaceholderState error:', e);
    }
}

// 通知 Android 编辑器已就绪
function notifyReady() {
    if (window.Android && window.Android.onEditorReady) {
        window.Android.onEditorReady();
    } else {
        // 如果 window.Android 尚未注入，每隔一段时间重试
        setTimeout(notifyReady, 200);
    }
}

// 请求 Android 选择图片
function requestPickImage() {
    if (window.Android && window.Android.pickImage) {
        window.Android.pickImage();
    }
}

// 初始化编辑器
async function initEditor() {
    const editorContainer = document.getElementById('editor');
    const loadingEl = document.getElementById('loading');
    const toolbarEl = document.getElementById('toolbar');
    const editorContainerEl = document.getElementById('editor-container');

    const prismInstance = window.Prism;
    if (prismInstance && prismInstance.languages && !prismInstance.languages.mermaid) {
        prismInstance.languages.mermaid = prismInstance.languages.markup || prismInstance.languages.plain || {};
    }

    try {
        editor = await Editor.make()
            .config(ctx => {
                ctx.set(rootCtx, editorContainer);
                ctx.set(defaultValueCtx, pendingContent ?? '');


                // 监听内容变化
                ctx.get(listenerCtx).markdownUpdated((ctx, markdown, prevMarkdown) => {
                    // 忽略初始化时的内容变化
                    if (isInitializing) return;
                    if (markdown !== prevMarkdown) {
                        notifyContentChange();
                    }
                });
            })
            .use(commonmark)
            .use(gfm)
            .use(history)
            .use(listener)
            .use(clipboard)
            .use(indent)
            .use(trailing)
            .use(prism)
            .use(math)
            .create();

        // 设置占位符
        setTimeout(() => {
            const editorEl = getProseMirrorElement();
            if (editorEl) {
                editorEl.setAttribute('data-placeholder', currentPlaceholder);
            }
            updatePlaceholderState();
        }, 80);

        // 显示编辑器
        loadingEl.style.display = 'none';
        toolbarEl.style.display = 'flex';
        editorContainerEl.style.display = 'block';

        isReady = true;

        // 如果有待处理的内容，设置它
        // pendingContent 为 null/undefined 表示还没调用过 setContent
        // pendingContent 为空字符串表示要清空编辑器
        if (pendingContent !== null && pendingContent !== undefined) {
            editor.action(replaceAll(pendingContent));
            pendingContent = null;
        }

        // 通知 Android 就绪
        notifyReady();

        // 绑定工具栏事件
        bindToolbarEvents();
        bindDialogEvents();
        bindImageResize();
        setupImageWidthObserver();
        bindSourceTextareaEvents();
        bindPlaceholderEvents();
        bindEditorScrollEvents();
        setupKeyboardScrollHandler();

        // 延迟结束初始化状态，等首屏内容稳定后同步基线
        setTimeout(() => {
            try {
                syncEditorBaseline(editor.action(getMarkdown()));
            } catch (e) {
                syncEditorBaseline('');
            }
            isInitializing = false;
        }, 180);

    } catch (error) {
        console.error('Editor initialization failed:', error);
        loadingEl.innerHTML = `
            <div style="color: var(--md-error); text-align: center;">
                <div style="margin-bottom: 8px;">编辑器加载失败</div>
                <div style="font-size: 12px; opacity: 0.7;">${error.message}</div>
            </div>
        `;
        // 通知 Android 隐藏加载动画，显示错误信息
        if (window.Android && window.Android.onEditorReady) {
            window.Android.onEditorReady();
        }
    }
}

function bindSourceTextareaEvents() {
    const sourceTextarea = document.getElementById('source-textarea');
    if (!sourceTextarea) return;

    sourceTextarea.addEventListener('input', () => {
        notifyContentChange();
    });
}

function bindPlaceholderEvents() {
    const editorRoot = document.getElementById('editor');
    if (!editorRoot) return;

    const handleImmediateHide = () => {
        if (!isSourceView) {
            hidePlaceholderImmediately();
        }
    };

    editorRoot.addEventListener('beforeinput', handleImmediateHide);
    editorRoot.addEventListener('compositionstart', handleImmediateHide);
    editorRoot.addEventListener('paste', handleImmediateHide);
    editorRoot.addEventListener('focusin', () => {
        if (!isSourceView) {
            setPlaceholderFocused(true);
        }
    });
    editorRoot.addEventListener('focusout', () => {
        if (!isSourceView) {
            setPlaceholderFocused(false);
            updatePlaceholderState();
        }
    });
    editorRoot.addEventListener('keydown', (event) => {
        if (!event.ctrlKey && !event.metaKey && !event.altKey && event.key.length === 1) {
            handleImmediateHide();
        }
    });
}

function bindEditorScrollEvents() {
    const editorContainer = document.getElementById('editor-container');
    if (!editorContainer) return;

    let lastScrollTop = editorContainer.scrollTop;
    editorContainer.addEventListener('scroll', () => {
        const currentScrollTop = editorContainer.scrollTop;
        const deltaY = currentScrollTop - lastScrollTop;
        lastScrollTop = currentScrollTop;

        if (window.Android && window.Android.onEditorScroll) {
            window.Android.onEditorScroll(currentScrollTop, deltaY);
        }
    }, { passive: true });
}

// 滚动到光标位置 - 键盘弹出时使用
function scrollToCursor() {
    if (!editor || isSourceView) return;

    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        const { state } = view;
        const { selection } = state;

        // 获取光标位置的坐标
        const coords = view.coordsAtPos(selection.from);
        if (!coords) return;

        const editorContainer = document.getElementById('editor-container');
        if (!editorContainer) return;

        // 获取可视区域高度（考虑键盘）
        const viewportHeight = window.visualViewport ? window.visualViewport.height : window.innerHeight;
        const containerRect = editorContainer.getBoundingClientRect();

        // 底部工具栏高度
        const toolbarHeight = 56;

        // 计算可视区域的中间位置
        const visibleTop = containerRect.top;
        const visibleBottom = viewportHeight - toolbarHeight;
        const visibleHeight = visibleBottom - visibleTop;
        const visibleCenter = visibleTop + visibleHeight / 2;

        // 光标中心位置
        const cursorCenter = (coords.top + coords.bottom) / 2;

        // 计算需要滚动的距离，使光标位于可视区域中间
        const scrollAmount = cursorCenter - visibleCenter;

        // 只有当滚动距离超过一定阈值时才滚动（避免小幅度抖动）
        if (Math.abs(scrollAmount) > 30) {
            editorContainer.scrollBy({
                top: scrollAmount,
                behavior: 'smooth'
            });
        }
    } catch (e) {
        console.error('scrollToCursor error:', e);
    }
}

// 设置键盘弹出时滚动到光标位置
function setupKeyboardScrollHandler() {
    // 使用 visualViewport API 监听键盘弹出
    if (window.visualViewport) {
        let lastHeight = window.visualViewport.height;

        window.visualViewport.addEventListener('resize', () => {
            const currentHeight = window.visualViewport.height;
            const heightDiff = lastHeight - currentHeight;

            // 如果高度减少超过 100px，认为是键盘弹出
            if (heightDiff > 100) {
                // 延迟滚动，等待布局稳定
                setTimeout(() => {
                    scrollToCursor();
                }, 100);
            }

            lastHeight = currentHeight;
        });
    }

    // 备用方案：监听 focus 事件
    const editorContainer = document.getElementById('editor-container');
    if (editorContainer) {
        editorContainer.addEventListener('focus', () => {
            setTimeout(() => {
                scrollToCursor();
            }, 300);
        }, true);
    }
}

// 绑定工具栏事件
function bindToolbarEvents() {
    const toolbar = document.getElementById('toolbar');

    // 阻止工具栏的 mousedown/touchstart 导致编辑器失焦
    toolbar.addEventListener('mousedown', (e) => {
        e.preventDefault();
    });

    toolbar.addEventListener('click', (e) => {
        const btn = e.target.closest('.toolbar-btn');
        if (!btn) return;

        e.preventDefault();
        e.stopPropagation();

        const action = btn.dataset.action;
        executeAction(action);

        // 执行完操作后，重新聚焦编辑器
        if (!isSourceView) {
            const editorEl = document.querySelector('.milkdown .ProseMirror, .milkdown .editor');
            if (editorEl) {
                editorEl.focus();
            }
        }
    });
}

// 执行工具栏操作
function executeAction(action) {
    // 源码模式下只允许特定操作
    if (isSourceView) {
        switch (action) {
            case 'toggleSource':
                toggleSourceView();
                return;
            case 'copy':
                document.execCommand('copy');
                return;
            case 'paste':
                document.execCommand('paste');
                return;
            case 'selectAll':
                const textarea = document.getElementById('source-textarea');
                if (textarea) {
                    // 切换:已全选 → 取消并把光标移到末尾
                    if (textarea.selectionStart === 0 && textarea.selectionEnd === textarea.value.length) {
                        textarea.setSelectionRange(textarea.value.length, textarea.value.length);
                    } else {
                        textarea.select();
                    }
                }
                return;
            case 'undo':
                document.execCommand('undo');
                return;
            case 'redo':
                document.execCommand('redo');
                return;
            default:
                // 其它操作在源码模式下不可用
                return;
        }
    }

    if (!editor) return;

    try {
        switch (action) {
            case 'undo':
                editor.action(callCommand(undoCommand.key));
                collapseCurrentSelection();
                break;
            case 'redo':
                editor.action(callCommand(redoCommand.key));
                collapseCurrentSelection();
                break;
            case 'heading1':
                insertHeading(1);
                break;
            case 'heading2':
                insertHeading(2);
                break;
            case 'heading3':
                insertHeading(3);
                break;
            case 'bold':
                editor.action(callCommand(toggleStrongCommand.key));
                break;
            case 'italic':
                editor.action(callCommand(toggleEmphasisCommand.key));
                break;
            case 'strikethrough':
                editor.action(callCommand(toggleStrikethroughCommand.key));
                break;
            case 'code':
                insertInlineCode();
                break;
            case 'bulletList':
                insertList('bullet');
                break;
            case 'orderedList':
                insertList('ordered');
                break;
            case 'taskList':
                insertList('task');
                break;
            case 'blockquote':
                insertBlockquote();
                break;
            case 'codeBlock':
                insertCodeBlock();
                break;
            case 'link':
                showLinkDialog();
                break;
            case 'image':
                showImageDialog();
                break;
            case 'hr':
                editor.action(callCommand(insertHrCommand.key));
                break;
            case 'toggleSource':
                toggleSourceView();
                return;
            case 'copy':
                copySelection();
                return;
            case 'paste':
                pasteContent();
                return;
            case 'selectAll':
                selectAllContent();
                return;
        }
        notifyContentChange();
    } catch (e) {
        console.error('executeAction error:', e);
    }
}

// 插入列表 - 智能处理选中和未选中情况
function insertList(type) {
    const view = editor.action(ctx => ctx.get(editorViewCtx));
    const { state } = view;
    const { from, to, empty } = state.selection;

    if (empty) {
        // 没有选中，插入新列表项
        switch (type) {
            case 'bullet':
                editor.action(insert('- '));
                break;
            case 'ordered':
                editor.action(insert('1. '));
                break;
            case 'task':
                editor.action(insert('- [ ] '));
                break;
        }
    } else {
        // 有选中文本，将每行转换为列表项
        try {
            const selectedText = state.doc.textBetween(from, to, '\n');
            const lines = selectedText.split('\n');
            let converted;

            if (type === 'bullet') {
                converted = lines.map(line => '- ' + line.trim()).join('\n');
            } else if (type === 'ordered') {
                converted = lines.map((line, index) => (index + 1) + '. ' + line.trim()).join('\n');
            } else if (type === 'task') {
                converted = lines.map(line => '- [ ] ' + line.trim()).join('\n');
            }

            // 使用 replaceAll 来替换选中内容
            const tr = state.tr.insertText(converted, from, to);
            view.dispatch(tr);
        } catch (e) {
            console.error('insertList error:', e);
            // 降级为插入前缀
            const prefix = type === 'bullet' ? '- ' : type === 'ordered' ? '1. ' : '- [ ] ';
            editor.action(insert(prefix));
        }
    }
}

// 插入引用 - 智能处理选中和未选中情况
function insertBlockquote() {
    const view = editor.action(ctx => ctx.get(editorViewCtx));
    const { state } = view;
    const { from, to, empty } = state.selection;

    if (empty) {
        // 没有选中，插入引用前缀
        editor.action(insert('> '));
    } else {
        // 有选中文本，尝试用命令包裹
        try {
            editor.action(callCommand(wrapInBlockquoteCommand.key));
        } catch (e) {
            // 命令失败，手动转换每行
            try {
                const selectedText = state.doc.textBetween(from, to, '\n');
                const lines = selectedText.split('\n');
                const converted = lines.map(line => '> ' + line).join('\n');

                // 删除选中
                const tr = state.tr.deleteSelection();
                view.dispatch(tr);

                // 插入引用
                editor.action(insert(converted));
            } catch (e2) {
                console.error('insertBlockquote error:', e2);
                editor.action(insert('> '));
            }
        }
    }
}

// 插入代码块 - 智能处理选中和未选中情况
function insertCodeBlock() {
    const view = editor.action(ctx => ctx.get(editorViewCtx));
    const { state } = view;
    const { from, to, empty } = state.selection;

    if (empty) {
        // 没有选中，尝试创建代码块
        try {
            editor.action(callCommand(createCodeBlockCommand.key));
        } catch (e) {
            editor.action(insert('```\n\n```'));
        }
    } else {
        // 有选中文本，先删除选中内容，然后用 replaceAll 重新渲染
        try {
            const selectedText = state.doc.textBetween(from, to, '\n');
            const fullMarkdown = editor.action(getMarkdown());

            // 计算选中文本在 markdown 中的位置（简化处理：替换第一次出现）
            // 使用代码块包裹
            const codeBlock = '```\n' + selectedText + '\n```';

            // 删除选中，插入代码块 markdown
            const tr = state.tr.deleteSelection();
            view.dispatch(tr);

            // 插入代码块文本
            editor.action(insert(codeBlock));
        } catch (e) {
            console.error('insertCodeBlock error:', e);
            editor.action(insert('```\n\n```'));
        }
    }
}

// 插入标题 - 使用 wrapInHeadingCommand
function insertHeading(level) {
    try {
        editor.action(callCommand(wrapInHeadingCommand.key, level));
    } catch (e) {
        // 降级：直接插入 markdown 前缀
        const prefix = '#'.repeat(level) + ' ';
        editor.action(insert(prefix));
    }
}

// 插入行内代码 - 包裹选中文本
function insertInlineCode() {
    const view = editor.action(ctx => ctx.get(editorViewCtx));
    const { state } = view;
    const { from, to, empty } = state.selection;

    if (empty) {
        // 没有选中，插入占位符
        editor.action(insert('`code`'));
    } else {
        // 有选中文本，先删除再插入
        try {
            const selectedText = state.doc.textBetween(from, to);
            const inlineCode = '`' + selectedText + '`';

            // 删除选中
            const tr = state.tr.deleteSelection();
            view.dispatch(tr);

            // 插入行内代码
            editor.action(insert(inlineCode));
        } catch (e) {
            console.error('insertInlineCode error:', e);
            editor.action(insert('`code`'));
        }
    }
}


// 复制选中内容 - 优先使用原生剪贴板(WebView 下 navigator.clipboard 不可用)
function copySelection() {
    try {
        const selection = window.getSelection();
        if (selection && !selection.isCollapsed) {
            const text = selection.toString();
            // 优先走原生桥接 (Android ClipboardManager)
            if (window.AndroidBridge && typeof window.AndroidBridge.copyToClipboard === 'function') {
                const ok = window.AndroidBridge.copyToClipboard(text);
                if (ok) return;
            }
            // 降级: 用 execCommand 操作当前 DOM 选区
            try {
                document.execCommand('copy');
            } catch (e2) {
                console.error('copySelection fallback failed:', e2);
            }
        }
    } catch (e) {
        console.error('copySelection error:', e);
    }
}

// 粘贴内容 - 优先使用原生剪贴板(WebView 下 navigator.clipboard 不可用)
function pasteContent() {
    try {
        // 优先走原生桥接 (Android ClipboardManager)
        if (window.AndroidBridge && typeof window.AndroidBridge.pasteFromClipboard === 'function') {
            const text = window.AndroidBridge.pasteFromClipboard();
            if (text && editor) {
                editor.action(insert(text));
                notifyContentChange();
            }
            return;
        }
        // 降级: 用 execCommand (在很多 WebView 上无效)
        try {
            document.execCommand('paste');
        } catch (e2) {
            console.error('pasteContent fallback failed:', e2);
        }
    } catch (e) {
        console.error('pasteContent error:', e);
    }
}

// 全选/取消全选 —— 已全选时再点会取消,等价于把光标折叠到文档末尾
function selectAllContent() {
    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        const { state } = view;
        const { doc } = state;
        const { from, to, empty } = state.selection;
        const docSize = doc.content.size;
        const isAllSelected = !empty && from === 0 && to === docSize;

        let tr;
        if (isAllSelected) {
            // 已全选 → 取消,光标移到文档末尾
            tr = state.tr.setSelection(
                state.selection.constructor.near(state.doc.resolve(docSize))
            );
        } else {
            // 全选
            tr = state.tr.setSelection(
                state.selection.constructor.create(doc, 0, docSize)
            );
        }
        view.dispatch(tr);
        view.focus();
    } catch (e) {
        // 降级使用 DOM selection
        const editorEl = document.querySelector('.milkdown .editor, .milkdown .ProseMirror');
        if (editorEl) {
            const selection = window.getSelection();
            const fullText = editorEl.textContent || '';
            // 判断是否已全选
            if (selection && !selection.isCollapsed && selection.toString() === fullText) {
                // 取消
                selection.removeAllRanges();
            } else {
                const range = document.createRange();
                range.selectNodeContents(editorEl);
                selection.removeAllRanges();
                selection.addRange(range);
            }
        }
    }
}

// 图片对话框 - 调用 Native 对话框
function showImageDialog() {
    if (window.Android && window.Android.showImageDialog) {
        window.Android.showImageDialog();
    }
}

function hideImageDialog() {
    // 保留空函数以保持兼容性
}

// 链接对话框 - 调用 Native 对话框
function showLinkDialog() {
    // 获取选中的文字作为链接文字
    let selectedText = '';
    try {
        const selection = window.getSelection();
        if (selection && !selection.isCollapsed) {
            selectedText = selection.toString().trim();
        }
    } catch (e) {
        console.error('getSelection error:', e);
    }

    if (window.Android && window.Android.showLinkDialog) {
        window.Android.showLinkDialog(selectedText);
    }
}

function hideLinkDialog() {
    // 保留空函数以保持兼容性
}

// 绑定对话框事件（已迁移到 Native，保留空函数以保持兼容性）
function bindDialogEvents() {
    // 对话框已迁移到 Native 实现，此函数保留为空
}

// 切换源码视图
function toggleSourceView() {
    const editorContainer = document.getElementById('editor-container');
    const sourceContainer = document.getElementById('source-container');
    const sourceTextarea = document.getElementById('source-textarea');
    const toggleBtn = document.querySelector('[data-action="toggleSource"]');

    if (!editorContainer || !sourceContainer || !sourceTextarea) return;

    isSourceView = !isSourceView;

    if (isSourceView) {
        // 切换到源码视图
        const markdown = editor.action(getMarkdown());
        sourceTextarea.value = markdown;
        hideMermaidPreviewPanel();
        editorContainer.style.display = 'none';
        sourceContainer.style.display = 'block';
        toggleBtn?.classList.add('active');
        toggleBtn?.classList.add('toolbar-btn-primary');
        sourceTextarea.focus();
        // 禁用不支持的工具栏按钮
        updateToolbarState(true);
    } else {
        // 切换回编辑器视图
        const markdown = sourceTextarea.value;

        // 标记为正在初始化，忽略内容变化回调（与 setContent 一致）
        isInitializing = true;
        try {
            // 使用 flush=true 强制创建新的 EditorState，确保内容完整替换
            editor.action(replaceAll(markdown, true));
        } catch (e) {
            console.error('toggleSourceView replaceAll error:', e);
            // 降级：使用默认 flush=false
            try {
                editor.action(replaceAll(markdown));
            } catch (e2) {
                console.error('toggleSourceView fallback error:', e2);
            }
        }

        sourceContainer.style.display = 'none';
        editorContainer.style.display = 'block';
        toggleBtn?.classList.remove('active');
        toggleBtn?.classList.remove('toolbar-btn-primary');

        // 等编辑器状态稳定后同步基线并通知
        setTimeout(() => {
            try {
                syncEditorBaseline(editor.action(getMarkdown()));
            } catch (e) {
                syncEditorBaseline(markdown);
            }
            isInitializing = false;
            notifyContentChange(true);
        }, 50);

        // 启用所有工具栏按钮
        updateToolbarState(false);
    }
}

// 更新工具栏按钮状态（源码模式下禁用部分按钮）
function updateToolbarState(disabled) {
    const toolbar = document.getElementById('toolbar');
    if (!toolbar) return;

    // 源码模式下可用的操作
    const allowedInSource = ['toggleSource', 'undo', 'redo', 'copy', 'paste', 'selectAll'];

    toolbar.querySelectorAll('.toolbar-btn').forEach(btn => {
        const action = btn.dataset.action;
        if (disabled && !allowedInSource.includes(action)) {
            btn.classList.add('disabled');
            btn.style.opacity = '0.4';
            btn.style.pointerEvents = 'none';
        } else {
            btn.classList.remove('disabled');
            btn.style.opacity = '';
            btn.style.pointerEvents = '';
        }
    });
}

// 图片缩放相关变量
let selectedImageIndex = -1;

// 绑定图片缩放功能
function bindImageResize() {
    const editorContainer = document.getElementById('editor-container');
    if (!editorContainer) return;

    // 图片点击处理 - 调用 Native 弹窗
    editorContainer.addEventListener('click', (e) => {
        const img = e.target.closest('img');
        if (!img) return;

        // 获取图片信息
        const images = editorContainer.querySelectorAll('img');
        let index = -1;
        images.forEach((image, i) => {
            if (image === img) {
                index = i;
            }
        });

        if (index >= 0) {
            selectedImageIndex = index;
            const currentWidth = img.offsetWidth;
            const maxWidth = editorContainer.offsetWidth - 32; // 减去 padding
            const naturalWidth = img.naturalWidth || currentWidth;

            // 调用 Native 显示图片调整弹窗
            if (window.Android && window.Android.showImageResizeDialog) {
                window.Android.showImageResizeDialog(index, currentWidth, maxWidth, naturalWidth);
            }
        }
    });
}

// Native 回调：设置图片宽度
window.AndroidBridge.setImageWidth = function(index, width) {
    if (!editor) return;

    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        const { state } = view;
        let imageCount = 0;
        let targetPos = -1;
        let targetNode = null;

        // 遍历文档找到第 index 个图片
        state.doc.descendants((node, pos) => {
            if (node.type.name === 'image') {
                if (imageCount === index) {
                    targetPos = pos;
                    targetNode = node;
                }
                imageCount++;
            }
        });

        if (targetPos >= 0 && targetNode) {
            const src = targetNode.attrs.src || '';
            const alt = targetNode.attrs.alt || '';
            const title = targetNode.attrs.title || '';

            // 创建新的属性，通过 title 存储宽度信息
            const newAttrs = { ...targetNode.attrs };

            if (width > 0) {
                // 在 title 中存储宽度信息，格式：width=XXX 或 原title|width=XXX
                const widthStr = `width=${width}`;
                // 移除旧的宽度信息
                let cleanTitle = (title || '').replace(/\|?width=\d+/g, '').replace(/^\|/, '');
                newAttrs.title = cleanTitle ? `${cleanTitle}|${widthStr}` : widthStr;
            } else {
                // 恢复原始大小，移除宽度信息
                newAttrs.title = (title || '').replace(/\|?width=\d+/g, '').replace(/^\|/, '');
                if (!newAttrs.title) delete newAttrs.title;
            }

            // 创建新的图片节点并替换
            const newNode = targetNode.type.create(newAttrs);
            const tr = state.tr.replaceWith(targetPos, targetPos + targetNode.nodeSize, newNode);
            view.dispatch(tr);

            // 同时更新 DOM 显示
            setTimeout(() => {
                const editorContainer = document.getElementById('editor-container');
                if (editorContainer) {
                    const images = editorContainer.querySelectorAll('img');
                    if (index >= 0 && index < images.length) {
                        const img = images[index];
                        if (width > 0) {
                            img.style.width = width + 'px';
                            img.style.height = 'auto';
                        } else {
                            img.style.width = '';
                            img.style.height = '';
                        }
                    }
                }
            }, 50);

            notifyContentChange();
        }
    } catch (e) {
        console.error('setImageWidth error:', e);
    }
};

// Native 回调：实时预览图片宽度（只更新 DOM，不保存到文档模型）
window.AndroidBridge.previewImageWidth = function(index, width) {
    const editorContainer = document.getElementById('editor-container');
    if (!editorContainer) return;

    const images = editorContainer.querySelectorAll('img');
    if (index >= 0 && index < images.length) {
        const img = images[index];
        if (width > 0) {
            img.style.width = width + 'px';
            img.style.height = 'auto';
        } else {
            // width <= 0 表示恢复原始大小
            img.style.width = '';
            img.style.height = '';
        }
    }
};

// 应用图片宽度样式（从 title 属性解析宽度）
function applyImageWidthStyles() {
    const editorContainer = document.getElementById('editor-container');
    if (!editorContainer) return;

    const images = editorContainer.querySelectorAll('img');
    images.forEach(img => {
        const title = img.getAttribute('title') || '';
        const widthMatch = title.match(/width=(\d+)/);
        if (widthMatch) {
            img.style.width = widthMatch[1] + 'px';
            img.style.height = 'auto';
        }
        
        // 优化：为图片添加懒加载属性，提升长图文文档的滑动流畅度
        if (!img.hasAttribute('loading')) {
            img.setAttribute('loading', 'lazy');
        }
    });
}

// 监听编辑器内容变化，应用图片宽度
function setupImageWidthObserver() {
    const editorContainer = document.getElementById('editor-container');
    if (!editorContainer) return;

    const observer = new MutationObserver(() => {
        applyImageWidthStyles();
    });

    observer.observe(editorContainer, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ['src']
    });

    // 初始应用
    setTimeout(applyImageWidthStyles, 500);
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', initEditor);
