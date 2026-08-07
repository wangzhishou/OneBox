package com.wanbaohe.markdown.edit.webview

import com.shifenmiao.model.colors.EditorColors

/**
 * Milkdown 编辑器 HTML 模板生成器
 *
 * 用于生成内联样式的 HTML，避免加载时闪白
 */
object MarkdownEditorHtmlTemplate {

    /**
     * 生成完整的编辑器 HTML
     *
     * @param isDarkTheme 是否暗色主题
     * @param colors Material 3 颜色配置
     * @param storageKey 用于区分不同页面的 localStorage 缓存 key
     * @param fontSizeSp 正文字体大小（单位 sp）
     * @param lineHeightSp 正文行高（单位 sp）
     * @param letterSpacingSp 字符间距（单位 sp）
     * @param fontWeight 字重（例如 400, 700）
     */
    fun generate(
        isDarkTheme: Boolean,
        colors: EditorColors,
        storageKey: String = "default",
        fontSizeSp: Float = 16f,
        lineHeightSp: Float = 24f,
        letterSpacingSp: Float = 0f,
        fontWeight: Int = 400
    ): String {
        val darkClass = if (isDarkTheme) "dark" else ""
        val fullStorageKey = "milkdown_draft_$storageKey"
        return """
<!DOCTYPE html>
<html lang="zh-CN" class="h-full $darkClass">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Markdown Editor</title>
    <script>
        // 在最早时机设置 storage key，避免时序问题
        window.MILKDOWN_STORAGE_KEY = "$fullStorageKey";
    </script>
    <style>
        :root {
            --md-primary: ${colors.primary};
            --md-on-primary: ${colors.onPrimary};
            --md-primary-container: ${colors.primaryContainer};
            --md-on-primary-container: ${colors.onPrimaryContainer};
            --md-secondary: ${colors.secondary};
            --md-on-secondary: ${colors.onSecondary};
            --md-secondary-container: ${colors.secondaryContainer};
            --md-on-secondary-container: ${colors.onSecondaryContainer};
            --md-surface: ${colors.surface};
            --md-on-surface: ${colors.onSurface};
            --md-surface-variant: ${colors.surfaceVariant};
            --md-on-surface-variant: ${colors.onSurfaceVariant};
            --md-outline: ${colors.outline};
            --md-outline-variant: ${colors.outlineVariant};
            --md-background: ${colors.background};
            --md-on-background: ${colors.onBackground};
            --md-error: ${colors.error};
            --md-on-error: ${colors.onError};
            --md-font-size: ${fontSizeSp}px;
            --md-line-height: ${lineHeightSp}px;
            --md-letter-spacing: ${letterSpacingSp}px;
            --md-font-weight: $fontWeight;
        }
        
        * { margin: 0; padding: 0; box-sizing: border-box; }
        
        html, body {
            height: 100%;
            overflow: hidden;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background-color: var(--md-background);
            color: var(--md-on-background);
        }
        
        #app {
            display: flex;
            flex-direction: column;
            height: 100%;
        }
        
        /* 工具栏样式 - 底部固定横向滚动 */
        #toolbar {
            display: flex;
            flex-wrap: nowrap;
            gap: 4px;
            padding: 8px 12px;
            padding-bottom: calc(8px + env(safe-area-inset-bottom, 0px));
            background-color: var(--md-surface);
            overflow-x: auto;
            overflow-y: hidden;
            -webkit-overflow-scrolling: touch;
            flex-shrink: 0;
            order: 2;
            -ms-overflow-style: none;
            scrollbar-width: none;
        }
        
        #toolbar::-webkit-scrollbar { display: none; }
        
        .toolbar-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 36px;
            height: 36px;
            min-width: 36px;
            border: none;
            border-radius: 8px;
            background-color: transparent;
            color: var(--md-on-surface-variant);
            cursor: pointer;
            transition: all 0.2s ease;
            -webkit-tap-highlight-color: transparent;
            flex-shrink: 0;
        }
        
        .toolbar-btn:hover { background-color: var(--md-surface-variant); }
        .toolbar-btn:active { background-color: var(--md-primary-container); }
        .toolbar-btn.active { background-color: var(--md-primary-container); color: var(--md-on-primary-container); }
        .toolbar-btn svg { width: 20px; height: 20px; }
        
        /* 主要操作按钮 - 高亮显示 */
        .toolbar-btn-primary {
            color: var(--md-primary);
            background-color: var(--md-primary-container);
        }
        .toolbar-btn-primary:hover { 
            background-color: var(--md-primary-container); 
            filter: brightness(0.95);
        }
        .toolbar-btn-primary:active { 
            background-color: var(--md-primary); 
            color: var(--md-on-primary);
        }
        .toolbar-btn-primary.active {
            background-color: var(--md-primary);
            color: var(--md-on-primary);
        }
        
        .toolbar-divider {
            width: 1px;
            height: 24px;
            background-color: var(--md-outline-variant);
            margin: 6px 4px;
            flex-shrink: 0;
        }
        
        /* 编辑器容器 */
        #editor-container {
            flex: 1;
            position: relative;
            overflow-y: auto;
            padding: 0 16px 16px 16px;
            background-color: var(--md-background);
            order: 1;
        }
        
        #editor-container::-webkit-scrollbar { width: 6px; }
        #editor-container::-webkit-scrollbar-track { background: transparent; }
        #editor-container::-webkit-scrollbar-thumb { background-color: var(--md-outline-variant); border-radius: 3px; }
        
        /* Milkdown 编辑器样式 */
        .milkdown {
            min-height: 100%;
            outline: none;
            color: var(--md-on-surface);
            font-size: var(--md-font-size);
            line-height: var(--md-line-height);
            letter-spacing: var(--md-letter-spacing);
            font-weight: var(--md-font-weight);
        }
        .milkdown .editor, .milkdown .ProseMirror {
            outline: none;
            min-height: 200px;
            position: relative;
            white-space: pre-wrap;
            word-wrap: break-word;
            overflow-wrap: break-word;
            -webkit-font-variant-ligatures: none;
            font-variant-ligatures: none;
            color: var(--md-on-surface);
            font-size: var(--md-font-size);
            line-height: var(--md-line-height);
            letter-spacing: var(--md-letter-spacing);
            font-weight: var(--md-font-weight);
        }
        
        /* Placeholder 样式 - 优化消失速度及匹配字体大小 */
        .milkdown .ProseMirror.is-empty::before {
            content: attr(data-placeholder);
            color: var(--md-on-surface-variant);
            opacity: 0.9;
            pointer-events: none;
            float: left;
            height: 0;
            font-size: var(--md-font-size);
            line-height: var(--md-line-height);
            letter-spacing: var(--md-letter-spacing);
            font-weight: var(--md-font-weight);
            transition: opacity 0.04s linear;
        }
        .milkdown .ProseMirror.is-empty.is-focused::before {
            opacity: 0;
        }
        .milkdown .ProseMirror:not(.is-empty)::before {
            content: none;
            display: none;
        }
        
        /* 标题样式 */
        .milkdown h1 { font-size: 2em; font-weight: 700; margin: 0.67em 0; color: var(--md-on-surface); }
        .milkdown h2 { font-size: 1.5em; font-weight: 600; margin: 0.83em 0; color: var(--md-on-surface); }
        .milkdown h3 { font-size: 1.17em; font-weight: 600; margin: 1em 0; color: var(--md-on-surface); }
        
        /* 段落和文本样式 */
        .milkdown p { 
            margin: 0; 
            color: var(--md-on-surface); 
            font-size: var(--md-font-size); 
            line-height: var(--md-line-height);
            letter-spacing: var(--md-letter-spacing);
            font-weight: var(--md-font-weight);
        }
        .milkdown p + p { margin-top: 1em; }
        .milkdown strong { font-weight: 700; }
        .milkdown em { font-style: italic; }
        .milkdown del { text-decoration: line-through; color: var(--md-on-surface-variant); }
        
        /* 列表内容也使用正文字体大小 */
        .milkdown li { 
            margin: 0.5em 0; 
            font-size: var(--md-font-size); 
            line-height: var(--md-line-height);
            letter-spacing: var(--md-letter-spacing);
            font-weight: var(--md-font-weight);
        }
        
        /* 链接样式 - 带角标图标 */
        .milkdown a {
            color: var(--md-primary);
            text-decoration: none;
            border-bottom: 1px dashed var(--md-primary);
            padding-bottom: 1px;
            position: relative;
        }
        .milkdown a::after {
            content: '↗';
            font-size: 0.7em;
            margin-left: 1px;
            vertical-align: super;
            opacity: 0.7;
        }
        .milkdown a:hover {
            border-bottom-style: solid;
        }
        .milkdown a:hover::after {
            opacity: 1;
        }
        
        /* 代码样式 */
        .milkdown code {
            font-family: 'Fira Code', 'Consolas', monospace;
            background-color: var(--md-surface-variant);
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 0.9em;
        }
        .milkdown pre {
            background-color: var(--md-surface-variant);
            padding: 16px;
            border-radius: 8px;
            overflow-x: auto;
            margin: 1em 0;
        }
        .milkdown pre code { background: none; padding: 0; font-size: 0.85em; line-height: 1.5; }

        /* Mermaid 预览样式 */
        #mermaid-preview-container {
            display: none;
            position: absolute;
            z-index: 20;
            pointer-events: none;
        }
        .mermaid-preview {
            padding: 12px;
            border-radius: 12px;
            border: 1px solid var(--md-outline-variant);
            background-color: rgba(255, 255, 255, 0.92);
            backdrop-filter: blur(4px);
            overflow-x: auto;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        }
        .dark .mermaid-preview { background-color: rgba(28, 27, 31, 0.92); }
        .mermaid-preview svg {
            display: block;
            max-width: 100%;
            height: auto !important;
            margin: 0 auto;
        }
        .mermaid-preview-empty,
        .mermaid-preview-error {
            font-size: 13px;
            line-height: 1.5;
            color: var(--md-on-surface-variant);
            white-space: pre-wrap;
        }
        .mermaid-preview.error {
            border-color: var(--md-error);
            background-color: var(--md-surface-variant);
        }
        .mermaid-preview.error .mermaid-preview-error {
            color: var(--md-error);
        }
        .milkdown .ProseMirror pre.mermaid-source-hidden {
            opacity: 0;
            pointer-events: none;
            height: 0;
            margin: 0;
            padding-top: 0;
            padding-bottom: 0;
            border-width: 0;
            overflow: hidden;
        }
        
        /* Prism 代码高亮样式 */
        .milkdown .token.comment,
        .milkdown .token.prolog,
        .milkdown .token.doctype,
        .milkdown .token.cdata { color: #6a9955; font-style: italic; }
        
        .milkdown .token.punctuation { color: var(--md-on-surface-variant); }
        
        .milkdown .token.property,
        .milkdown .token.tag,
        .milkdown .token.boolean,
        .milkdown .token.number,
        .milkdown .token.constant,
        .milkdown .token.symbol,
        .milkdown .token.deleted { color: #b5cea8; }
        
        .milkdown .token.selector,
        .milkdown .token.attr-name,
        .milkdown .token.string,
        .milkdown .token.char,
        .milkdown .token.builtin,
        .milkdown .token.inserted { color: #ce9178; }
        
        .milkdown .token.operator,
        .milkdown .token.entity,
        .milkdown .token.url,
        .milkdown .language-css .token.string,
        .milkdown .style .token.string { color: var(--md-on-surface); }
        
        .milkdown .token.atrule,
        .milkdown .token.attr-value,
        .milkdown .token.keyword { color: #569cd6; }
        
        .milkdown .token.function,
        .milkdown .token.class-name { color: #dcdcaa; }
        
        .milkdown .token.regex,
        .milkdown .token.important,
        .milkdown .token.variable { color: #d16969; }
        
        /* 暗色主题代码高亮调整 */
        .dark .milkdown .token.comment,
        .dark .milkdown .token.prolog,
        .dark .milkdown .token.doctype,
        .dark .milkdown .token.cdata { color: #6a9955; }
        
        .dark .milkdown .token.property,
        .dark .milkdown .token.tag,
        .dark .milkdown .token.boolean,
        .dark .milkdown .token.number,
        .dark .milkdown .token.constant,
        .dark .milkdown .token.symbol,
        .dark .milkdown .token.deleted { color: #b5cea8; }
        
        .dark .milkdown .token.selector,
        .dark .milkdown .token.attr-name,
        .dark .milkdown .token.string,
        .dark .milkdown .token.char,
        .dark .milkdown .token.builtin,
        .dark .milkdown .token.inserted { color: #ce9178; }
        
        .dark .milkdown .token.atrule,
        .dark .milkdown .token.attr-value,
        .dark .milkdown .token.keyword { color: #569cd6; }
        
        .dark .milkdown .token.function,
        .dark .milkdown .token.class-name { color: #dcdcaa; }
        
        /* 浅色主题代码高亮 */
        :not(.dark) .milkdown .token.comment,
        :not(.dark) .milkdown .token.prolog,
        :not(.dark) .milkdown .token.doctype,
        :not(.dark) .milkdown .token.cdata { color: #008000; }
        
        :not(.dark) .milkdown .token.property,
        :not(.dark) .milkdown .token.tag,
        :not(.dark) .milkdown .token.boolean,
        :not(.dark) .milkdown .token.number,
        :not(.dark) .milkdown .token.constant,
        :not(.dark) .milkdown .token.symbol,
        :not(.dark) .milkdown .token.deleted { color: #098658; }
        
        :not(.dark) .milkdown .token.selector,
        :not(.dark) .milkdown .token.attr-name,
        :not(.dark) .milkdown .token.string,
        :not(.dark) .milkdown .token.char,
        :not(.dark) .milkdown .token.builtin,
        :not(.dark) .milkdown .token.inserted { color: #a31515; }
        
        :not(.dark) .milkdown .token.atrule,
        :not(.dark) .milkdown .token.attr-value,
        :not(.dark) .milkdown .token.keyword { color: #0000ff; }
        
        :not(.dark) .milkdown .token.function,
        :not(.dark) .milkdown .token.class-name { color: #795e26; }
        
        :not(.dark) .milkdown .token.regex,
        :not(.dark) .milkdown .token.important,
        :not(.dark) .milkdown .token.variable { color: #811f3f; }
        
        /* KaTeX 数学公式样式 */
        .milkdown .math-inline,
        .milkdown .math-block {
            font-family: 'KaTeX_Main', 'Times New Roman', serif;
        }
        
        .milkdown .math-inline {
            display: inline-block;
            padding: 0 2px;
        }
        
        .milkdown .math-block {
            display: block;
            text-align: center;
            margin: 1em 0;
            padding: 1em;
            background-color: var(--md-surface-variant);
            border-radius: 8px;
            overflow-x: auto;
        }
        
        /* KaTeX 通用样式覆盖 */
        .milkdown .katex {
            font-size: 1.1em;
            color: var(--md-on-surface);
        }
        
        .milkdown .katex-display {
            margin: 0;
            overflow-x: auto;
            overflow-y: hidden;
        }
        
        .milkdown .katex-display > .katex {
            max-width: 100%;
        }
        
        /* 数学公式编辑状态样式 */
        .milkdown [data-type="math_inline"],
        .milkdown [data-type="math_block"] {
            cursor: text;
        }
        
        .milkdown [data-type="math_inline"]:focus,
        .milkdown [data-type="math_block"]:focus {
            outline: 2px solid var(--md-primary);
            outline-offset: 2px;
            border-radius: 4px;
        }
        
        /* 引用样式 */
        .milkdown blockquote {
            border-left: 4px solid var(--md-primary);
            margin: 1em 0;
            padding: 0.5em 1em;
            background-color: var(--md-surface-variant);
            border-radius: 0 8px 8px 0;
        }
        
        /* 列表样式 */
        .milkdown ul, .milkdown ol { margin: 1em 0; padding-left: 2em; }
        .milkdown li[data-task-list-item] { list-style: none; margin-left: -1.5em; }
        .milkdown li[data-task-list-item] input[type="checkbox"] { margin-right: 0.5em; accent-color: var(--md-primary); }
        
        /* 分割线和图片 */
        .milkdown hr { border: none; border-top: 1px solid var(--md-outline-variant); margin: 2em 0; }
        .milkdown img { 
            max-width: 100%; 
            height: auto; 
            border-radius: 8px; 
            margin: 1em 0;
            cursor: pointer;
        }
        .milkdown img:active {
            opacity: 0.8;
        }
        
        /* 表格样式 */
        .milkdown table { width: 100%; border-collapse: collapse; margin: 1em 0; }
        .milkdown th, .milkdown td { border: 1px solid var(--md-outline-variant); padding: 8px 12px; text-align: left; }
        .milkdown th { background-color: var(--md-surface-variant); font-weight: 600; }
        
        /* Loading 动画 */
        #loading {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
            flex-direction: column;
            gap: 16px;
        }
        .spinner {
            width: 40px;
            height: 40px;
            border: 3px solid var(--md-surface-variant);
            border-top-color: var(--md-primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }
        @keyframes spin { to { transform: rotate(360deg); } }
        #loading-text { color: var(--md-on-surface-variant); font-size: 14px; }
        
        /* 源码视图样式 */
        #source-container {
            flex: 1;
            overflow-y: auto;
            background-color: var(--md-background);
            order: 1;
            display: none;
        }
        #source-textarea {
            width: 100%;
            height: 100%;
            min-height: 300px;
            padding: 16px;
            border: none;
            background-color: transparent;
            color: var(--md-on-surface);
            font-family: 'Fira Code', 'Consolas', 'Monaco', monospace;
            font-size: 14px;
            line-height: 1.6;
            resize: none;
            outline: none;
        }
        #source-textarea:focus {
            border-color: var(--md-primary);
        }
        
        /* 选中操作栏样式 - 已移除,改用底部工具栏 */

        /* 打印样式 - 隐藏工具栏和操作按钮，适配A4页面 */
        @page {
            size: A4;
            margin: 20mm;
        }

        @media print {
            #toolbar,
            #source-container {
                display: none !important;
            }

            html, body {
                height: auto;
                overflow: visible;
            }

            #app {
                height: auto;
                width: 100%;
            }

            #editor-container {
                display: block !important;
                overflow: visible;
                padding: 0;
            }

            .milkdown {
                color: #000000;
            }

            /* 图片处理 - 避免空白 */
            .milkdown img {
                max-width: 100% !important;
                width: auto !important;
                height: auto !important;
                page-break-inside: avoid;
            }

            /* 代码块处理 */
            .milkdown pre {
                max-width: 100% !important;
                overflow-x: auto;
                page-break-inside: avoid;
            }

            /* 表格处理 */
            .milkdown table {
                width: 100% !important;
                max-width: 100% !important;
                word-wrap: break-word;
            }

            /* 避免元素在页面边缘被截断 */
            .milkdown h1,
            .milkdown h2,
            .milkdown h3,
            .milkdown h4,
            .milkdown h5,
            .milkdown h6 {
                page-break-after: avoid;
                page-break-inside: avoid;
            }

            .milkdown p {
                page-break-inside: avoid;
                orphans: 3;
                widows: 3;
            }

            /* 列表处理 */
            .milkdown ul,
            .milkdown ol {
                page-break-inside: avoid;
            }

            .milkdown li {
                page-break-inside: avoid;
            }

            /* 引用块处理 */
            .milkdown blockquote {
                page-break-inside: avoid;
            }
        }
    </style>
</head>
<body>
    <div id="app">
        <div id="loading">
            <div class="spinner"></div>
            <div id="loading-text">正在加载编辑器...</div>
        </div>
        <div id="editor-container" style="display: none;">
            <div id="mermaid-preview-container">
                <div id="mermaid-preview-content" class="mermaid-preview"></div>
            </div>
            <div id="editor"></div>
        </div>
        
        <div id="source-container">
            <textarea id="source-textarea" placeholder="Markdown 源码..."></textarea>
        </div>
        <div id="toolbar" style="display: none;">
            ${TOOLBAR_HTML}
        </div>
    </div>
    
    <script>
        // Compatibility for older Android WebView missing some modern built-ins.
        if (typeof Object.hasOwn !== 'function') {
            Object.hasOwn = function (obj, prop) {
                return Object.prototype.hasOwnProperty.call(Object(obj), prop);
            };
        }

        if (typeof Array.prototype.at !== 'function') {
            Object.defineProperty(Array.prototype, 'at', {
                value: function(index) {
                    var len = this.length >>> 0;
                    var relative = Number(index) || 0;
                    var k = relative < 0 ? len + relative : relative;
                    return (k < 0 || k >= len) ? undefined : this[k];
                },
                writable: true,
                configurable: true
            });
        }
    </script>
    <script src="https://appassets.androidplatform.net/assets/js/mermaid.min.js"></script>
    <script src="editor.bundle.js"></script>
</body>
</html>
        """.trimIndent()
    }

    private const val TOOLBAR_HTML = """
        <button class="toolbar-btn" data-action="toggleSource" title="查看 Markdown 源码">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><polyline points="9.5 13.5 7.5 15.5 9.5 17.5"/><polyline points="14.5 13.5 16.5 15.5 14.5 17.5"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="undo" title="撤销">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 10h10a5 5 0 0 1 5 5v0a5 5 0 0 1-5 5H7m-4-10l4-4m-4 4l4 4"/></svg>
        </button>
        <button class="toolbar-btn" data-action="redo" title="重做">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10H11a5 5 0 0 0-5 5v0a5 5 0 0 0 5 5h6m4-10l-4-4m4 4l-4 4"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="heading1" title="一级标题">
            <svg viewBox="0 0 24 24" fill="currentColor"><text x="4" y="18" font-size="14" font-weight="bold">H1</text></svg>
        </button>
        <button class="toolbar-btn" data-action="heading2" title="二级标题">
            <svg viewBox="0 0 24 24" fill="currentColor"><text x="4" y="18" font-size="14" font-weight="bold">H2</text></svg>
        </button>
        <button class="toolbar-btn" data-action="heading3" title="三级标题">
            <svg viewBox="0 0 24 24" fill="currentColor"><text x="4" y="18" font-size="14" font-weight="bold">H3</text></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="bold" title="粗体">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M6 4h8a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/><path d="M6 12h9a4 4 0 0 1 4 4 4 4 0 0 1-4 4H6z"/></svg>
        </button>
        <button class="toolbar-btn" data-action="italic" title="斜体">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="19" y1="4" x2="10" y2="4"/><line x1="14" y1="20" x2="5" y2="20"/><line x1="15" y1="4" x2="9" y2="20"/></svg>
        </button>
        <button class="toolbar-btn" data-action="strikethrough" title="删除线">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 4H9a3 3 0 0 0-3 3v0a3 3 0 0 0 3 3h6"/><path d="M8 20h7a3 3 0 0 0 3-3v0a3 3 0 0 0-3-3H6"/><line x1="4" y1="12" x2="20" y2="12"/></svg>
        </button>
        <button class="toolbar-btn" data-action="code" title="行内代码">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="bulletList" title="无序列表">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="9" y1="6" x2="20" y2="6"/><line x1="9" y1="12" x2="20" y2="12"/><line x1="9" y1="18" x2="20" y2="18"/><circle cx="4" cy="6" r="1.5" fill="currentColor"/><circle cx="4" cy="12" r="1.5" fill="currentColor"/><circle cx="4" cy="18" r="1.5" fill="currentColor"/></svg>
        </button>
        <button class="toolbar-btn" data-action="orderedList" title="有序列表">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="10" y1="6" x2="21" y2="6"/><line x1="10" y1="12" x2="21" y2="12"/><line x1="10" y1="18" x2="21" y2="18"/><text x="2" y="8" font-size="8" fill="currentColor">1</text><text x="2" y="14" font-size="8" fill="currentColor">2</text><text x="2" y="20" font-size="8" fill="currentColor">3</text></svg>
        </button>
        <button class="toolbar-btn" data-action="taskList" title="任务列表">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="5" width="6" height="6" rx="1"/><path d="M5 8l1 1 2-2"/><line x1="12" y1="8" x2="21" y2="8"/><rect x="3" y="13" width="6" height="6" rx="1"/><line x1="12" y1="16" x2="21" y2="16"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="blockquote" title="引用">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M6 17h3l2-4V7H5v6h3zm8 0h3l2-4V7h-6v6h3z"/></svg>
        </button>
        <button class="toolbar-btn" data-action="codeBlock" title="代码块">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><polyline points="8 10 6 12 8 14"/><polyline points="16 10 18 12 16 14"/><line x1="12" y1="8" x2="12" y2="16"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="link" title="链接">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
        </button>
        <button class="toolbar-btn" data-action="image" title="图片">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="hr" title="分割线">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="3" y1="12" x2="21" y2="12"/></svg>
        </button>
        <div class="toolbar-divider"></div>
        <button class="toolbar-btn" data-action="copy" title="复制">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg>
        </button>
        <button class="toolbar-btn" data-action="paste" title="粘贴">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><rect x="8" y="2" width="8" height="4" rx="1"/></svg>
        </button>
        <button class="toolbar-btn" data-action="selectAll" title="全选">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 8V5a2 2 0 0 1 2-2h3"/><path d="M21 8V5a2 2 0 0 0-2-2h-3"/><path d="M3 16v3a2 2 0 0 0 2 2h3"/><path d="M21 16v3a2 2 0 0 1-2 2h-3"/><rect x="8" y="8" width="8" height="8" rx="1"/></svg>
        </button>
    """

}
