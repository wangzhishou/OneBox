/**
 * Milkdown Markdown 编辑器
 *
 * 功能：
 * - 所见即所得 Markdown 编辑
 * - Material 3 主题适配
 * - 与 Android 原生代码双向通信
 * - 图片插入（从相册选择或输入URL）
 * - 底部工具栏（复制/粘贴/全选/格式等）
 * - 代码块基础语法高亮（Milkdown prism + 图表语言语法）
 * - 图表代码块原位内联渲染：Mermaid 走 mermaid.min.js，PlantUML / DOT 走 native 渲染桥
 * - 代码块使用自定义 nodeView 承载预览，避免外部 DOM 改动被 ProseMirror 误判为文档编辑
 */

import { Editor, rootCtx, defaultValueCtx, editorViewCtx, nodeViewCtx } from '@milkdown/core';
import { commonmark } from '@milkdown/preset-commonmark';
import { gfm } from '@milkdown/preset-gfm';
import { history, redoCommand, undoCommand } from '@milkdown/plugin-history';
import { listener, listenerCtx } from '@milkdown/plugin-listener';
import { clipboard } from '@milkdown/plugin-clipboard';
import { indent } from '@milkdown/plugin-indent';
import { trailing } from '@milkdown/plugin-trailing';
import { prism, prismConfig } from '@milkdown/plugin-prism';
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
let diagramRenderTimer = null;
let diagramRenderEpoch = 0;
let diagramIdSeed = 0;
let diagramEditingTimer = null;
let mermaidThemeMode = null;

// 图表渲染结果按「语法 + 主题 + 源码」缓存，编辑区每次内容变化不必重新解析布局。
const DIAGRAM_CACHE_LIMIT = 40;
const diagramCache = new Map();
const DIAGRAM_KINDS = {
    mermaid: 'mermaid', mmd: 'mermaid',
    plantuml: 'plantuml', puml: 'plantuml', uml: 'plantuml',
    dot: 'dot', gv: 'dot', graphviz: 'dot'
};


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
    scheduleDiagramRender(markdown);
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

// ══════════════════════════════════════════════════════════════════════════
//  代码块内联图表渲染
//
//  渲染结果是 <pre> 内部的 contenteditable=false 子节点，直接占住代码块在文档流
//  里的位置，不再使用浮在正文上方的绝对定位面板。源码始终留在 ProseMirror 的
//  <code> 里：点击预览或把光标移进代码块就切回可编辑、带语法高亮的源码视图；
//  渲染失败、语法未支持或 native 桥不可用时，保留原始代码并给出提示。
//
//  注意：ProseMirror 不允许外部代码随意改动它管理的 DOM。DOMObserver 会把
//  <pre> 上的 class/dataset 变更和新增的子节点当成文档编辑，进而把整个
//  code_block 标记成 NODE_DIRTY 并重建，预览会被抹掉。因此这里给 code_block
//  注册一个自定义 nodeView：预览容器在节点创建时就一并生成，之后所有写入都
//  通过 ignoreMutation 明确告知编辑器「这不是文档内容」。
// ══════════════════════════════════════════════════════════════════════════

/** code_block 的自定义 nodeView：与 preset-commonmark 的 toDOM 结构保持一致。 */
class DiagramCodeBlockView {
    constructor(node) {
        this.node = node;

        // commonmark 的 codeBlockAttr 默认是 { pre: {}, code: {} }，
        // toDOM 等价于 <pre data-language="..."><code>...content...</code></pre>。
        this.dom = document.createElement('pre');
        this.dom.dataset.language = node.attrs.language || '';
        this.contentDOM = document.createElement('code');
        this.dom.appendChild(this.contentDOM);

        this.holder = document.createElement('div');
        this.holder.className = 'diagram-inline';
        this.holder.setAttribute('contenteditable', 'false');
        this.holder.setAttribute('data-diagram-preview', 'true');
        this.dom.appendChild(this.holder);
    }

    update(node) {
        if (node.type !== this.node.type) return false;
        this.node = node;
        const language = node.attrs.language || '';
        if (this.dom.dataset.language !== language) {
            this.dom.dataset.language = language;
        }
        return true;
    }

    ignoreMutation(mutation) {
        if (!mutation) return true;
        // 选区变更必须继续交给 ProseMirror，否则光标会失灵。
        if (mutation.type === 'selection') return false;

        const target = mutation.target;
        if (!target) return true;
        // <pre> 包装层上的 class / data-* 全部由内联渲染逻辑维护。
        if (target === this.dom) return true;
        // 预览容器及其 SVG 子树不是文档内容。
        if (target === this.holder || this.holder.contains(target)) return true;
        // <code> 内的真实编辑照常交给 ProseMirror 解析、生成事务。
        if (target === this.contentDOM || this.contentDOM.contains(target)) return false;
        return true;
    }

    destroy() {}
}

function createDiagramCodeBlockView(node) {
    return new DiagramCodeBlockView(node);
}

function getMermaidApi() {
    return window.mermaid || null;
}

function ensureMermaidInitialized() {
    const mermaid = getMermaidApi();
    if (!mermaid) return false;

    const theme = isDiagramDarkTheme() ? 'dark' : 'default';
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

function isDiagramDarkTheme() {
    return document.documentElement.classList.contains('dark');
}

function getDiagramKind(language) {
    return DIAGRAM_KINDS[String(language || '').trim().toLowerCase()] || null;
}

function getDiagramColorsJson() {
    const style = getComputedStyle(document.documentElement);
    const read = (name, fallback) => style.getPropertyValue(name).trim() || fallback;
    return JSON.stringify({
        'background': read('--md-surface', '#FEF7FF'),
        'node-fill': read('--md-primary-container', '#EADDFF'),
        'accent': read('--md-on-primary-container', '#21005D'),
        'label-background': read('--md-surface-variant', '#E7E0EC')
    });
}

function buildDiagramKey(kind, code) {
    return kind + '\u0000' + (isDiagramDarkTheme() ? 'dark' : 'light') + '\u0000' + code;
}

function rememberDiagramResult(key, result) {
    diagramCache.delete(key);
    diagramCache.set(key, result);
    while (diagramCache.size > DIAGRAM_CACHE_LIMIT) {
        diagramCache.delete(diagramCache.keys().next().value);
    }
}

function escapeDiagramText(text) {
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

/** 从 ProseMirror 的代码块 DOM 直接取语言与源码，避免靠 Markdown 围栏下标对齐。 */
function readDiagramBlock(pre) {
    const kind = getDiagramKind(pre.dataset.language);
    if (!kind) return null;

    let codeElement = null;
    for (let i = 0; i < pre.children.length; i += 1) {
        if (pre.children[i].tagName === 'CODE') {
            codeElement = pre.children[i];
            break;
        }
    }
    if (!codeElement) return null;

    return {
        kind,
        code: codeElement.textContent || ''
    };
}

/** 预览容器由 DiagramCodeBlockView 在节点创建时生成，这里只负责查找，绝不再往 <pre> 里插节点。 */
function getDiagramHolder(pre) {
    for (let i = 0; i < pre.children.length; i += 1) {
        const child = pre.children[i];
        if (child.classList && child.classList.contains('diagram-inline')) {
            return child;
        }
    }
    return null;
}

/** 只清空容器内容与状态，不动 DOM 结构，避免 nodeView 失去对 holder 的引用。 */
function removeInlineDiagram(pre) {
    const holder = getDiagramHolder(pre);
    if (holder) holder.innerHTML = '';
    pre.classList.remove(
        'diagram-inline-pending', 'diagram-inline-ready',
        'diagram-inline-failed', 'diagram-inline-editing'
    );
    delete pre.dataset.diagramKey;
    delete pre.dataset.diagramKind;
    delete pre.dataset.diagramStatus;
}

function clearInlineDiagramPreviews() {
    document.querySelectorAll('.milkdown .ProseMirror pre').forEach(pre => removeInlineDiagram(pre));
}

function applyDiagramSvg(pre, key, epoch, svg) {
    if (epoch !== diagramRenderEpoch || !pre.isConnected || pre.dataset.diagramKey !== key) return;

    const holder = getDiagramHolder(pre);
    if (!holder) return;
    holder.innerHTML = svg;
    pre.dataset.diagramStatus = 'ok';
    pre.classList.remove('diagram-inline-pending', 'diagram-inline-failed');
    pre.classList.add('diagram-inline-ready');
    updateDiagramEditingState();
}

function applyDiagramError(pre, key, epoch, message) {
    if (epoch !== diagramRenderEpoch || !pre.isConnected || pre.dataset.diagramKey !== key) return;

    // 不隐藏源码：只保留一行错误说明，代码（含语法高亮）照常可见、可编辑。
    const holder = getDiagramHolder(pre);
    if (!holder) return;
    holder.innerHTML = '<div class="diagram-inline-error">图表渲染失败，已显示原始代码' +
        (message ? '（' + escapeDiagramText(message) + '）' : '') + '</div>';
    pre.dataset.diagramStatus = 'error';
    pre.classList.remove('diagram-inline-pending', 'diagram-inline-ready');
    pre.classList.add('diagram-inline-failed');
    updateDiagramEditingState();
}

async function renderMermaidSvg(code) {
    if (!ensureMermaidInitialized()) {
        throw new Error('mermaid 引擎未就绪');
    }
    const mermaid = getMermaidApi();
    return await mermaid.render('onebox-diagram-' + (++diagramIdSeed) + '-' + Date.now(), code);
}

function renderNativeDiagramSvg(kind, code, colorsJson) {
    const bridge = window.Android;
    if (!bridge || typeof bridge.renderDiagramSvg !== 'function') {
        throw new Error('native 图表渲染器不可用');
    }
    const raw = bridge.renderDiagramSvg(kind, code, isDiagramDarkTheme(), colorsJson);
    if (!raw) {
        throw new Error('native 图表渲染器无响应');
    }
    const result = JSON.parse(raw);
    if (!result.ok || !result.svg) {
        throw new Error(result.error || '图表解析失败');
    }
    return result.svg;
}

function updateDiagramPreviews() {
    if (isSourceView || !editor) {
        clearInlineDiagramPreviews();
        return;
    }

    const preElements = Array.from(document.querySelectorAll('.milkdown .ProseMirror pre'));
    const epoch = ++diagramRenderEpoch;
    const colorsJson = getDiagramColorsJson();

    preElements.forEach(pre => {
        const block = readDiagramBlock(pre);
        if (!block) {
            removeInlineDiagram(pre);
            return;
        }

        // 正在编辑这个代码块时只保留高亮源码，不触发解析，也不改动它内部的 DOM。
        if (pre.classList.contains('diagram-inline-editing')) return;

        const key = buildDiagramKey(block.kind, block.code);
        const holder = getDiagramHolder(pre);
        if (!holder) {
            // 拿不到 nodeView 的预览容器时不写 ProseMirror 管理的 DOM，直接保留原始代码。
            return;
        }
        if (pre.dataset.diagramKey !== key) {
            holder.innerHTML = '<div class="diagram-inline-loading">图表渲染中…</div>';
            pre.dataset.diagramKey = key;
            pre.dataset.diagramKind = block.kind;
            pre.dataset.diagramStatus = 'pending';
            pre.classList.remove('diagram-inline-ready', 'diagram-inline-failed');
            pre.classList.add('diagram-inline-pending');
        }

        if (pre.dataset.diagramStatus === 'ok' || pre.dataset.diagramStatus === 'error') {
            return;
        }

        const cached = diagramCache.get(key);
        if (cached) {
            if (cached.ok) applyDiagramSvg(pre, key, epoch, cached.svg);
            else applyDiagramError(pre, key, epoch, cached.error);
            return;
        }

        if (block.kind === 'mermaid' && getMermaidApi()) {
            renderMermaidSvg(block.code)
                .then(result => {
                    const svg = typeof result === 'string' ? result : result && result.svg;
                    if (!svg) throw new Error('mermaid 未返回 SVG');
                    rememberDiagramResult(key, { ok: true, svg });
                    applyDiagramSvg(pre, key, epoch, svg);
                    if (result && typeof result.bindFunctions === 'function') {
                        const target = getDiagramHolder(pre);
                        if (target) result.bindFunctions(target);
                    }
                })
                .catch(error => {
                    if (epoch !== diagramRenderEpoch) return;
                    const message = String(error && error.message ? error.message : error);
                    // JS 引擎解析失败或尚未注入时，再借 native 渲染器兜一次底。
                    try {
                        const svg = renderNativeDiagramSvg('mermaid', block.code, colorsJson);
                        rememberDiagramResult(key, { ok: true, svg });
                        applyDiagramSvg(pre, key, epoch, svg);
                    } catch (nativeError) {
                        rememberDiagramResult(key, { ok: false, error: message });
                        applyDiagramError(pre, key, epoch, message);
                    }
                });
        } else {
            // PlantUML / DOT 走 native；mermaid.min.js 尚未注入时 mermaid 也先走 native。
            try {
                const svg = renderNativeDiagramSvg(block.kind, block.code, colorsJson);
                rememberDiagramResult(key, { ok: true, svg });
                applyDiagramSvg(pre, key, epoch, svg);
            } catch (error) {
                const message = String(error && error.message ? error.message : error);
                rememberDiagramResult(key, { ok: false, error: message });
                applyDiagramError(pre, key, epoch, message);
            }
        }
    });
}

function scheduleDiagramRender() {
    if (diagramRenderTimer) {
        clearTimeout(diagramRenderTimer);
    }
    diagramRenderTimer = setTimeout(() => {
        updateDiagramPreviews();
    }, 160);
}

/** 光标在图表代码块内时显示源码，移开后恢复内联图表。 */
function updateDiagramEditingState() {
    if (!editor || isSourceView) return;

    let activePre = null;
    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        const $from = view.state.selection.$from;
        if ($from.parent.type.name === 'code_block') {
            const dom = view.nodeDOM($from.before($from.depth));
            if (dom && dom.nodeType === 1 && dom.dataset && dom.dataset.diagramKey) {
                activePre = dom;
            }
        }
    } catch (e) {
        console.error('updateDiagramEditingState error:', e);
    }

    const hadEditing = document.querySelector('.milkdown .ProseMirror pre.diagram-inline-editing') !== null;
    document.querySelectorAll('.milkdown .ProseMirror pre[data-diagram-key]').forEach(pre => {
        pre.classList.toggle('diagram-inline-editing', pre === activePre);
    });

    if (hadEditing && !activePre) {
        scheduleDiagramRender();
    }
}

function enterDiagramEditing(pre) {
    if (!editor || isSourceView) return;
    try {
        const view = editor.action(ctx => ctx.get(editorViewCtx));
        const preElements = Array.from(document.querySelectorAll('.milkdown .ProseMirror pre'));
        const index = preElements.indexOf(pre);
        if (index < 0) return;

        let count = 0;
        let targetPos = -1;
        view.state.doc.descendants((node, pos) => {
            if (node.type.name === 'code_block') {
                if (count === index) targetPos = pos;
                count += 1;
            }
        });
        if (targetPos < 0) return;

        pre.classList.add('diagram-inline-editing');
        const selection = view.state.selection;
        const tr = view.state.tr.setSelection(
            selection.constructor.near(view.state.doc.resolve(targetPos + 1))
        );
        view.dispatch(tr);
        view.focus();
    } catch (e) {
        console.error('enterDiagramEditing error:', e);
    }
}

function bindDiagramEvents() {
    const editorRoot = document.getElementById('editor');
    if (!editorRoot) return;

    const inPreview = event => event.target && event.target.closest &&
        event.target.closest('.diagram-inline');

    editorRoot.addEventListener('mousedown', event => {
        if (inPreview(event)) event.preventDefault();
    }, true);

    editorRoot.addEventListener('click', event => {
        const holder = inPreview(event);
        if (!holder) return;
        event.preventDefault();
        event.stopPropagation();
        const pre = holder.parentElement;
        if (pre && pre.tagName === 'PRE') {
            enterDiagramEditing(pre);
        }
    }, true);

    document.addEventListener('selectionchange', () => {
        if (diagramEditingTimer) clearTimeout(diagramEditingTimer);
        diagramEditingTimer = setTimeout(updateDiagramEditingState, 30);
    });
}

// ── 代码块基础语法高亮 ──────────────────────────────────────────────────
// 复用 @milkdown/plugin-prism 的 refractor：内置语言照常高亮，另外给三种图表
// 语法注册轻量 grammar。高亮由 ProseMirror decoration 渲染，不改文档文本，
// 因此与内联图表预览（同属 <pre> 的非编辑子节点）可以共存。
function createDiagramGrammars() {
    const common = {
        'string': /"(?:[^"\\\r\n]|\\.)*"|'(?:[^'\\\r\n]|\\.)*'/,
        'number': /\b\d+(?:\.\d+)?\b/,
        'punctuation': /[{}()[\];,=]/
    };

    const mermaid = {
        'comment': /%%[^\r\n]*/,
        'string': common.string,
        'keyword': /\b(?:flowchart|graph|graph TD|sequenceDiagram|classDiagram|stateDiagram|erDiagram|journey|gantt|pie|mindmap|timeline|gitGraph|subgraph|end|classDef|linkStyle|style|direction|click|TB|TD|BT|RL|LR)\b/,
        'operator': /-->|-\.->|==>|==|---|--|-\.-|:::|->/,
        'attr-name': /\b(?:shape|label|color|stroke|fill|style)\b/,
        'number': common.number,
        'punctuation': /[{}()[\];,|]/
    };

    const plantuml = {
        'comment': /'[^\r\n]*/,
        'keyword': /\b(?:@startuml|@enduml|start|stop|end|if|else|elseif|endif|then|while|endwhile|repeat|fork|again|actor|participant|class|interface|enum|package|note|title|skinparam|activate|deactivate|autonumber)\b/,
        'string': common.string,
        'function': /:[^\r\n;]+;/,
        'operator': /-->|->|\.\.>|=>|\|\|/,
        'number': common.number,
        'punctuation': /[{}()[\];,|]/
    };

    const dot = {
        'comment': /\/\/[^\r\n]*|\/\*[\s\S]*?\*\//,
        'keyword': /\b(?:strict|digraph|graph|subgraph|node|edge|rankdir|rank|label|shape|color|style|arrowhead|fontname|fontsize)\b/,
        'string': common.string,
        'operator': /->|--|\|/,
        'number': common.number,
        'punctuation': /[{}()[\];,=]/
    };

    return {
        mermaid, mmd: mermaid,
        plantuml, puml: plantuml, uml: plantuml,
        dot, gv: dot, graphviz: dot
    };
}

function configureDiagramRefractor(refractor) {
    const grammars = createDiagramGrammars();
    if (refractor && refractor.languages) {
        Object.keys(grammars).forEach(name => {
            if (!refractor.languages[name]) {
                refractor.languages[name] = grammars[name];
            }
        });
    }
    return refractor;
}

function notifyContentChange(force = false) {
    const markdown = getCurrentMarkdown();

    updatePlaceholderState(markdown);
    scheduleDiagramRender();

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
        diagramCache.clear();
        scheduleDiagramRender();
    },

    // 设置 Material 3 主题颜色
    setColors: function(colorsJson) {
        try {
            const colors = JSON.parse(colorsJson);
            const root = document.documentElement;
            Object.keys(colors).forEach(key => {
                root.style.setProperty(`--md-${key}`, colors[key]);
            });
            // 主题色变了，native 图表要按新配色重新导出 SVG
            diagramCache.clear();
            scheduleDiagramRender();
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

    try {
        editor = await Editor.make()
            .config(ctx => {
                ctx.set(rootCtx, editorContainer);
                ctx.set(defaultValueCtx, pendingContent ?? '');
                // 给 refractor 注册 mermaid / plantuml / dot 语法，代码块走 decoration 高亮
                ctx.set(prismConfig.key, { configureRefractor: configureDiagramRefractor });
                // 用自定义 nodeView 承载内联预览：ignoreMutation 会把预览相关的 DOM
                // 变更挡在 DOMObserver 之外，避免 code_block 被重建、光标被抢走。
                ctx.update(nodeViewCtx, views => views.concat([
                    ['code_block', createDiagramCodeBlockView]
                ]));


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
        bindDiagramEvents();
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
        clearInlineDiagramPreviews();
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
