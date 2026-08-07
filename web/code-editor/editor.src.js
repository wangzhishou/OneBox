/**
 * Code Editor — Phase 5
 *
 * - CodeMirror 6 完整版（行号、当前行高亮、括号匹配、closeBrackets、autocomplete、foldGutter、highlightActiveLineGutter）
 * - 行号区透明无边框,与编辑区同色 (但当前行号高亮)
 * - 12 种语言（js/ts/jsx/tsx/json/html/css/python/java/kotlin/md/sql/yaml/shell）
 * - 选区浮层（长按/选中文本 → 复制/粘贴/全选）
 * - 软键盘弹出自动滚动到光标
 * - window.CodeEditorBridge 完整 API
 * - 与 Android 通过 window.Android.* 双向通信
 *
 * Phase 6 接入业务能力（文件 I/O、历史、草稿、查找面板 UI）。
 */

import {
    EditorState, Compartment, Transaction, Prec
} from '@codemirror/state';
import {
    EditorView, keymap, lineNumbers, highlightActiveLine,
    highlightActiveLineGutter, drawSelection, dropCursor,
    placeholder as cmPlaceholder, ViewPlugin, Decoration
} from '@codemirror/view';
import {
    defaultKeymap, history, historyKeymap, indentWithTab
} from '@codemirror/commands';
import {
    searchKeymap, highlightSelectionMatches, openSearchPanel
} from '@codemirror/search';
import {
    bracketMatching, foldGutter, indentOnInput,
    syntaxHighlighting, defaultHighlightStyle, bracketMatchingHandle
} from '@codemirror/language';
import { closeBrackets, closeBracketsKeymap, autocompletion } from '@codemirror/autocomplete';
import { javascript } from '@codemirror/lang-javascript';
import { json } from '@codemirror/lang-json';
import { html } from '@codemirror/lang-html';
import { css } from '@codemirror/lang-css';
import { python } from '@codemirror/lang-python';
import { markdown } from '@codemirror/lang-markdown';
import { java } from '@codemirror/lang-java';
import { sql } from '@codemirror/lang-sql';
import { yaml } from '@codemirror/lang-yaml';
import { StreamLanguage } from '@codemirror/language';
import { shell } from '@codemirror/legacy-modes/mode/shell';

// ── 全局状态 ─────────────────────────────────────────────
let editorView = null;
let isReady = false;
let pendingContent = null;
let pendingLanguage = null;
let currentPlaceholder = '在此输入代码…';
let STORAGE_KEY = window.CODE_EDITOR_STORAGE_KEY || 'code_editor_draft_default';
let autoSaveTimer = null;
let isInitializing = true;
let lastNotifiedContent = null;
let notifyAndroidTimer = null;
let wordWrapCompartment = new Compartment();
let placeholderCompartment = new Compartment();
let languageCompartment = new Compartment();
let readonlyCompartment = new Compartment();
let themeCompartment = new Compartment();

// ── Kotlin StreamLanguage（cm6 没有官方 lang-kotlin，扩展 clike） ──
const kotlinLanguage = StreamLanguage.define({
    name: 'kotlin',
    startState: () => ({
        next: 'start',
        blockComment: false
    }),
    languageData: {
        commentTokens: { line: '//', block: { open: '/*', close: '*/' } },
        indentOnInput: /^\s*[\}\]\)]$/,
        closeBrackets: { brackets: ['(', '[', '{', '\'', '"', '`'] }
    },
    token(stream, state) {
        if (state.blockComment) {
            if (stream.match(/.*?\*\//)) state.blockComment = false;
            else stream.skipToEnd();
            return 'comment';
        }
        if (stream.eatSpace()) return null;
        if (stream.match(/^\/\//)) { stream.skipToEnd(); return 'lineComment'; }
        if (stream.match(/^\/\*/)) {
            state.blockComment = !!stream.match(/^.*?\*\//, false);
            return 'comment';
        }
        if (stream.match(/^("(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*')/)) return 'string';
        if (stream.match(/^\b(fun|val|var|if|else|when|while|for|do|return|in|is|as|class|object|interface|trait|enum|sealed|data|open|final|abstract|override|private|public|protected|internal|companion|init|this|super|throw|try|catch|finally|import|package|by|lazy|lateinit|operator|infix|inline|noinline|crossinline|reified|suspend|typealias|where|out|const|vararg|expect|actual|continue|break)\b/)) return 'keyword';
        if (stream.match(/^(true|false|null|this|super|Nothing|Unit|Any)\b/)) return 'atom';
        if (stream.match(/^\b(Int|Long|Short|Byte|Float|Double|Boolean|Char|String|Array|List|MutableList|Map|MutableMap|Set|MutableSet|Sequence)\b/)) return 'typeName';
        if (stream.match(/^\b(println|print|require|check|error|TODO|run|let|also|apply|with|repeat|listOf|mapOf|setOf|arrayOf|mutableListOf|mutableMapOf|mutableSetOf)\b/)) return 'builtin';
        if (stream.match(/^(@\w+)/)) return 'attributeName';
        if (stream.match(/^\b\d+(\.\d+)?[fFlL]?\b/)) return 'number';
        if (stream.match(/^\b[A-Z][a-zA-Z0-9_]*\b/)) return 'className';
        if (stream.match(/^([a-z_][a-zA-Z0-9_]*)/)) return 'variableName';
        stream.next();
        return null;
    }
});

// ── 语言映射（12+ 种） ────────────────────────────────────
const LANGUAGE_LOADERS = {
    javascript: () => javascript(),
    typescript: () => javascript({ typescript: true }),
    jsx: () => javascript({ jsx: true }),
    tsx: () => javascript({ jsx: true, typescript: true }),
    json: () => json(),
    html: () => html(),
    css: () => css(),
    scss: () => css(),
    python: () => python(),
    java: () => java(),
    kotlin: () => kotlinLanguage,
    markdown: () => markdown(),
    sql: () => sql(),
    yaml: () => yaml(),
    yml: () => yaml(),
    shell: () => StreamLanguage.define(shell),
    bash: () => StreamLanguage.define(shell),
    zsh: () => StreamLanguage.define(shell),
    plaintext: () => [],
    text: () => [],
    '': () => []
};

function getLanguageExtension(lang) {
    const loader = LANGUAGE_LOADERS[(lang || 'plaintext').toLowerCase()];
    return loader ? loader() : [];
}

// ── localStorage 草稿 ──────────────────────────────────────
function saveDraft(content) {
    try {
        if (content && content.trim()) {
            localStorage.setItem(STORAGE_KEY, content);
        } else {
            localStorage.removeItem(STORAGE_KEY);
        }
    } catch (e) { console.error('saveDraft error:', e); }
}

function loadDraft() {
    try { return localStorage.getItem(STORAGE_KEY) || ''; }
    catch (e) { return ''; }
}

function clearDraftStorage() {
    try { localStorage.removeItem(STORAGE_KEY); }
    catch (e) { console.error('clearDraft error:', e); }
}

function debounceSaveDraft(content) {
    if (autoSaveTimer) clearTimeout(autoSaveTimer);
    autoSaveTimer = setTimeout(() => saveDraft(content), 500);
}

function getCurrentContent() {
    if (!editorView) return loadDraft();
    return editorView.state.doc.toString();
}

function notifyContentChange(force = false) {
    if (!isReady || !editorView) return;
    const content = getCurrentContent();
    if (!force && content === lastNotifiedContent) return;
    lastNotifiedContent = content;
    debounceSaveDraft(content);

    if (notifyAndroidTimer) clearTimeout(notifyAndroidTimer);
    notifyAndroidTimer = setTimeout(() => {
        if (window.Android && window.Android.onContentChanged) {
            window.Android.onContentChanged(content);
        }
    }, 150);
}

function notifyReady() {
    if (window.Android && window.Android.onEditorReady) {
        window.Android.onEditorReady();
    } else {
        setTimeout(notifyReady, 200);
    }
}

function notifyPopupState(open) {
    if (window.Android && window.Android.onPopupStateChanged) {
        window.Android.onPopupStateChanged(open);
    }
}

// ── Android 桥接 ──────────────────────────────────────────
window.CodeEditorBridge = {
    setContent(text) {
        if (!isReady) {
            pendingContent = text;
            return;
        }
        try {
            isInitializing = true;
            const content = (text === null || text === undefined) ? loadDraft() : (text || '');
            editorView.dispatch({
                changes: { from: 0, to: editorView.state.doc.length, insert: content }
            });
            // 不调用 notifyContentChange —— setContent 是加载/恢复内容,
            // 不是用户编辑,不应触发 Android 端的 onContentChanged (避免初次打开就被 markAsDirty)
            setTimeout(() => {
                lastNotifiedContent = getCurrentContent();
                isInitializing = false;
            }, 80);
        } catch (e) {
            console.error('setContent error:', e);
            isInitializing = false;
        }
    },

    getContent() {
        return getCurrentContent();
    },

    clearDraft() {
        clearDraftStorage();
    },

    setLanguage(lang) {
        if (!isReady) {
            pendingLanguage = lang;
            return;
        }
        try {
            const ext = getLanguageExtension(lang);
            editorView.dispatch({
                effects: languageCompartment.reconfigure(ext)
            });
        } catch (e) { console.error('setLanguage error:', e); }
    },

    setReadOnly(readOnly) {
        if (!editorView) return;
        try {
            editorView.dispatch({
                effects: readonlyCompartment.reconfigure(EditorState.readOnly.of(!!readOnly))
            });
        } catch (e) { console.error('setReadOnly error:', e); }
    },

    setWordWrap(enabled) {
        if (!editorView) return;
        try {
            editorView.dispatch({
                effects: wordWrapCompartment.reconfigure(
                    enabled ? EditorView.lineWrapping : []
                )
            });
        } catch (e) { console.error('setWordWrap error:', e); }
    },

    setTheme(isDark) {
        if (!editorView) return;
        try {
            const cls = isDark ? 'cm-theme-dark' : 'cm-theme-light';
            const remove = isDark ? 'cm-theme-light' : 'cm-theme-dark';
            document.documentElement.classList.remove(remove);
            document.documentElement.classList.add(cls);
            // theme 已通过 CSS 变量驱动,不需要重配置
        } catch (e) { console.error('setTheme error:', e); }
    },

    setColors(colorsJson) {
        try {
            const colors = JSON.parse(colorsJson);
            const root = document.documentElement;
            Object.keys(colors).forEach(key => {
                root.style.setProperty(`--ce-${key}`, colors[key]);
            });
        } catch (e) { console.error('setColors error:', e); }
    },

    setTextStyle(fontSizePx, lineHeightPx, letterSpacingPx, fontWeight) {
        try {
            const root = document.documentElement;
            root.style.setProperty('--ce-font-size', fontSizePx + 'px');
            root.style.setProperty('--ce-line-height', lineHeightPx + 'px');
            root.style.setProperty('--ce-letter-spacing', letterSpacingPx + 'px');
            root.style.setProperty('--ce-font-weight', fontWeight);
        } catch (e) { console.error('setTextStyle error:', e); }
    },

    setPlaceholder(text) {
        currentPlaceholder = text || '在此输入代码…';
        if (editorView) {
            try {
                editorView.dispatch({
                    effects: placeholderCompartment.reconfigure(cmPlaceholder(currentPlaceholder))
                });
            } catch (e) { console.error('setPlaceholder error:', e); }
        }
    },

    focus() {
        if (editorView) editorView.focus();
    },

    blur() {
        if (editorView) editorView.contentDOM.blur();
    },

    undo() { undoFallback(); },
    redo() { redoFallback(); },

    selectAll() {
        if (!editorView) return;
        try {
            editorView.dispatch({
                selection: { anchor: 0, head: editorView.state.doc.length },
                scrollIntoView: true
            });
            editorView.focus();
        } catch (e) { console.error('selectAll error:', e); }
    },

    insertText(text) {
        if (!editorView || !text) return;
        try {
            const { from, to } = editorView.state.selection.main;
            editorView.dispatch({
                changes: { from, to, insert: text },
                selection: { anchor: from + text.length }
            });
            editorView.focus();
        } catch (e) { console.error('insertText error:', e); }
    },

    setSelection(from, to) {
        if (!editorView) return;
        try {
            editorView.dispatch({
                selection: { anchor: from, head: to },
                scrollIntoView: true
            });
            editorView.focus();
        } catch (e) { console.error('setSelection error:', e); }
    },

    find(query, caseSensitive) {
        if (!editorView || !query) return;
        try {
            const cursor = editorView.state.selection.main;
            const text = editorView.state.doc.toString();
            const haystack = caseSensitive ? text : text.toLowerCase();
            const needle = caseSensitive ? query : query.toLowerCase();
            const idx = haystack.indexOf(needle, cursor.head);
            if (idx >= 0) {
                editorView.dispatch({
                    selection: { anchor: idx, head: idx + query.length },
                    scrollIntoView: true
                });
                editorView.focus();
            }
        } catch (e) { console.error('find error:', e); }
    },

    replaceAll(find, replace) {
        if (!editorView || !find) return;
        try {
            const doc = editorView.state.doc;
            const text = doc.toString();
            const escaped = find.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
            const re = new RegExp(escaped, 'g');
            const newText = text.replace(re, replace);
            if (newText !== text) {
                editorView.dispatch({
                    changes: { from: 0, to: doc.length, insert: newText }
                });
                notifyContentChange(true);
            }
        } catch (e) { console.error('replaceAll error:', e); }
    },

    format() {
        if (!editorView) return;
        try {
            const text = getCurrentContent();
            if (!text.trim()) return;
            const parsed = JSON.parse(text);
            const formatted = JSON.stringify(parsed, null, 2);
            editorView.dispatch({
                changes: { from: 0, to: editorView.state.doc.length, insert: formatted }
            });
            notifyContentChange(true);
        } catch (e) { /* 非 JSON,静默 */ }
    },

    openSearch() {
        if (!editorView) return;
        try { openSearchPanel(editorView); }
        catch (e) { console.error('openSearch error:', e); }
    },

    closePopup() {
        if (selectionToolbar && selectionToolbar.classList.contains('visible')) {
            hideSelectionToolbar();
            return true;
        }
        return false;
    },

    checkReady() {
        if (isReady) notifyReady();
    }
};

// ── Undo/Redo ────────────────────────────────────────────
function undoFallback() {
    if (!editorView) return;
    try {
        editorView.dispatch({
            annotations: [Transaction.userEvent.of('undo')]
        });
    } catch (e) { console.error('undo error:', e); }
}

function redoFallback() {
    if (!editorView) return;
    try {
        editorView.dispatch({
            annotations: [Transaction.userEvent.of('redo')]
        });
    } catch (e) { console.error('redo error:', e); }
}

// ── 选区浮层 ─────────────────────────────────────────────
let selectionToolbar = null;
let longPressTimer = null;
const LONG_PRESS_DURATION = 500;

function showSelectionToolbar(range) {
    if (!selectionToolbar) return;
    const rect = range.getBoundingClientRect();
    const margin = 8;
    selectionToolbar.style.visibility = 'hidden';
    selectionToolbar.style.display = 'flex';
    const toolbarRect = selectionToolbar.getBoundingClientRect();
    const toolbarWidth = toolbarRect.width;
    const toolbarHeight = toolbarRect.height;
    let top = rect.top - toolbarHeight - margin;
    let left = rect.left + rect.width / 2;
    if (top < margin) top = rect.bottom + margin;
    const halfWidth = toolbarWidth / 2;
    if (left - halfWidth < margin) left = halfWidth + margin;
    else if (left + halfWidth > window.innerWidth - margin)
        left = window.innerWidth - halfWidth - margin;
    selectionToolbar.style.top = top + 'px';
    selectionToolbar.style.left = left + 'px';
    selectionToolbar.style.visibility = 'visible';
    selectionToolbar.classList.add('visible');
    notifyPopupState(true);
}

function hideSelectionToolbar() {
    if (selectionToolbar && selectionToolbar.classList.contains('visible')) {
        selectionToolbar.classList.remove('visible');
        selectionToolbar.style.display = 'none';
        notifyPopupState(false);
    }
}

function showSelectionToolbarAtCursor() {
    if (!selectionToolbar || !editorView) return;
    try {
        const view = editorView;
        const head = view.state.selection.main.head;
        const line = view.state.doc.lineAt(head);
        const coords = view.coordsAtPos(head);
        if (!coords) return;
        const margin = 8;
        selectionToolbar.style.visibility = 'hidden';
        selectionToolbar.style.display = 'flex';
        const toolbarRect = selectionToolbar.getBoundingClientRect();
        const toolbarWidth = toolbarRect.width;
        const toolbarHeight = toolbarRect.height;
        let top = coords.top - toolbarHeight - margin;
        let left = coords.left;
        if (top < margin) top = coords.bottom + margin;
        const halfWidth = toolbarWidth / 2;
        if (left - halfWidth < margin) left = halfWidth + margin;
        else if (left + halfWidth > window.innerWidth - margin)
            left = window.innerWidth - halfWidth - margin;
        selectionToolbar.style.top = top + 'px';
        selectionToolbar.style.left = left + 'px';
        selectionToolbar.style.visibility = 'visible';
        selectionToolbar.classList.add('visible');
        notifyPopupState(true);
    } catch (e) { console.error('showSelectionToolbarAtCursor error:', e); }
}

function bindSelectionToolbar() {
    selectionToolbar = document.getElementById('selection-toolbar');
    if (!selectionToolbar) return;

    selectionToolbar.addEventListener('click', (e) => {
        const btn = e.target.closest('.selection-btn');
        if (!btn) return;
        e.preventDefault();
        e.stopPropagation();
        const action = btn.dataset.action;
        executeSelectionAction(action);
        if (action !== 'copy' && action !== 'selectAll' && action !== 'paste') {
            hideSelectionToolbar();
        }
    });

    // 选区变化监听
    document.addEventListener('selectionchange', () => {
        if (!isReady) return;
        const selection = window.getSelection();
        if (!selection || selection.isCollapsed || selection.rangeCount === 0) {
            hideSelectionToolbar();
            return;
        }
        const range = selection.getRangeAt(0);
        const editorContainer = document.getElementById('editor');
        if (!editorContainer || !editorContainer.contains(range.commonAncestorContainer)) {
            hideSelectionToolbar();
            return;
        }
        showSelectionToolbar(range);
    });

    // 长按手势
    const handleLongPressStart = (e) => {
        if (selectionToolbar && selectionToolbar.contains(e.target)) return;
        const editorContainer = document.getElementById('editor');
        if (!editorContainer || !editorContainer.contains(e.target)) return;
        if (longPressTimer) clearTimeout(longPressTimer);
        longPressTimer = setTimeout(() => {
            const selection = window.getSelection();
            if (!selection || selection.rangeCount === 0) return;
            if (!selection.isCollapsed) {
                showSelectionToolbar(selection.getRangeAt(0));
            } else {
                showSelectionToolbarAtCursor();
            }
        }, LONG_PRESS_DURATION);
    };
    const handleLongPressEnd = () => {
        if (longPressTimer) { clearTimeout(longPressTimer); longPressTimer = null; }
    };
    document.addEventListener('touchstart', handleLongPressStart, { passive: true });
    document.addEventListener('mousedown', handleLongPressStart);
    document.addEventListener('touchend', handleLongPressEnd);
    document.addEventListener('touchcancel', handleLongPressEnd);
    document.addEventListener('touchmove', handleLongPressEnd, { passive: true });
    document.addEventListener('mouseup', handleLongPressEnd);
    document.addEventListener('mousemove', handleLongPressEnd);

    // 外部点击关闭
    document.addEventListener('mousedown', (e) => {
        if (selectionToolbar && selectionToolbar.contains(e.target)) return;
        const editorContainer = document.getElementById('editor');
        if (editorContainer && editorContainer.contains(e.target)) return;
        hideSelectionToolbar();
    });
}

function executeSelectionAction(action) {
    switch (action) {
        case 'copy': document.execCommand('copy'); break;
        case 'paste': document.execCommand('paste'); break;
        case 'selectAll': window.CodeEditorBridge.selectAll(); break;
    }
}

// ── 软键盘滚动 ──────────────────────────────────────────
function setupKeyboardScrollHandler() {
    if (!window.visualViewport) return;
    let lastHeight = window.visualViewport.height;
    window.visualViewport.addEventListener('resize', () => {
        const currentHeight = window.visualViewport.height;
        const heightDiff = lastHeight - currentHeight;
        if (heightDiff > 100) {
            setTimeout(scrollToCursor, 100);
        }
        lastHeight = currentHeight;
    });
}

function scrollToCursor() {
    if (!editorView) return;
    try {
        const view = editorView;
        const head = view.state.selection.main.head;
        const coords = view.coordsAtPos(head);
        if (!coords) return;
        const viewportHeight = window.visualViewport
            ? window.visualViewport.height
            : window.innerHeight;
        const editorEl = document.getElementById('editor');
        if (!editorEl) return;
        const containerRect = editorEl.getBoundingClientRect();
        const visibleTop = containerRect.top;
        const visibleBottom = viewportHeight - 50; // toolbar height
        const visibleCenter = (visibleTop + visibleBottom) / 2;
        const cursorCenter = (coords.top + coords.bottom) / 2;
        const scrollAmount = cursorCenter - visibleCenter;
        if (Math.abs(scrollAmount) > 30) {
            view.scrollDOM.scrollBy({ top: scrollAmount, behavior: 'smooth' });
        }
    } catch (e) { console.error('scrollToCursor error:', e); }
}

// ── 工具栏 ──────────────────────────────────────────────
function bindToolbarEvents() {
    const toolbar = document.getElementById('toolbar');
    if (!toolbar) return;

    toolbar.addEventListener('mousedown', (e) => {
        e.preventDefault();
        hideSelectionToolbar();
    });
    toolbar.addEventListener('touchstart', () => hideSelectionToolbar(), { passive: true });
    toolbar.addEventListener('click', (e) => {
        const btn = e.target.closest('.toolbar-btn');
        if (!btn) return;
        e.preventDefault();
        e.stopPropagation();
        const action = btn.dataset.action;
        executeToolbarAction(action);
        if (editorView) editorView.focus();
    });
}

function executeToolbarAction(action) {
    switch (action) {
        case 'undo': undoFallback(); break;
        case 'redo': redoFallback(); break;
        case 'copy': document.execCommand('copy'); break;
        case 'paste': document.execCommand('paste'); break;
        case 'selectAll': window.CodeEditorBridge.selectAll(); break;
        case 'toggleWrap': {
            const state = editorView && editorView.state;
            const wrapped = state && state.facet(EditorView.lineWrapping).length > 0;
            window.CodeEditorBridge.setWordWrap(!wrapped);
            break;
        }
        case 'find': {
            try { openSearchPanel(editorView); }
            catch (e) { console.error('openSearch error:', e); }
            break;
        }
    }
}

// ── 初始化 ──────────────────────────────────────────────
async function initEditor() {
    const editorContainer = document.getElementById('editor');
    const loadingEl = document.getElementById('loading');
    const toolbarEl = document.getElementById('toolbar');

    try {
        const initialContent = pendingContent !== null && pendingContent !== undefined
            ? pendingContent
            : loadDraft();
        const initialLang = pendingLanguage || 'plaintext';
        pendingContent = null;
        pendingLanguage = null;

        // 先显示 editor 容器,再挂载 CodeMirror (确保有正确的尺寸)
        editorContainer.style.display = 'block';

        const state = EditorState.create({
            doc: initialContent,
            extensions: [
                // 基础行号 (gutter 透明无边框,与编辑区同色;当前行号高亮)
                lineNumbers(),
                highlightActiveLineGutter(),
                foldGutter(),
                // 高亮
                highlightActiveLine(),
                highlightSelectionMatches(),
                syntaxHighlighting(defaultHighlightStyle, { fallback: true }),
                // 历史 + 选择
                history(),
                drawSelection(),
                dropCursor(),
                // 括号匹配 + 自动闭合
                bracketMatching(),
                closeBrackets(),
                // 输入缩进
                indentOnInput(),
                // 自动补全
                autocompletion(),
                // Keymaps
                keymap.of([
                    ...closeBracketsKeymap,
                    ...defaultKeymap,
                    ...searchKeymap,
                    ...historyKeymap,
                    indentWithTab
                ]),
                // 占位符
                cmPlaceholder(currentPlaceholder),
                // 动态重配置
                languageCompartment.of(getLanguageExtension(initialLang)),
                wordWrapCompartment.of(EditorView.lineWrapping),
                readonlyCompartment.of(EditorState.readOnly.of(false)),
                themeCompartment.of([]),
                // 监听文档变化 + 选区
                EditorView.updateListener.of((update) => {
                    if (update.docChanged && !isInitializing) {
                        notifyContentChange();
                    }
                    if (update.selectionSet && isReady) {
                        const head = update.state.selection.main.head;
                        const from = update.state.selection.main.from;
                        const to = update.state.selection.main.to;
                        const line = update.state.doc.lineAt(head);
                        if (window.Android && window.Android.onCursorChange) {
                            window.Android.onCursorChange(line.number, head - line.from + 1);
                        }
                        if (window.Android && window.Android.onSelectionChange) {
                            window.Android.onSelectionChange(from, to);
                        }
                    }
                })
            ]
        });

        editorView = new EditorView({
            state,
            parent: editorContainer
        });

        // 隐藏 loading,显示 toolbar
        loadingEl.style.display = 'none';
        toolbarEl.style.display = 'flex';

        isReady = true;
        lastNotifiedContent = getCurrentContent();

        bindToolbarEvents();
        bindSelectionToolbar();
        setupKeyboardScrollHandler();
        notifyReady();

        setTimeout(() => {
            isInitializing = false;
            lastNotifiedContent = getCurrentContent();
        }, 200);

    } catch (error) {
        console.error('Editor initialization failed:', error);
        if (loadingEl) {
            loadingEl.innerHTML = `
                <div style="color: var(--ce-error, #b3261e); text-align: center;">
                    <div style="margin-bottom: 8px;">编辑器加载失败</div>
                    <div style="font-size: 12px; opacity: 0.7;">${error.message}</div>
                </div>
            `;
        }
        notifyReady();
    }
}

initEditor();
