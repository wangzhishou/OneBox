package com.wanbaohe.code.editor.webview

import com.shifenmiao.model.colors.EditorColors

/**
 * CodeEditor HTML 模板生成器
 *
 * 用于生成内联样式的 HTML，避免加载时闪白
 *
 * CSS 变量约定（由 web 端 editor.src.js 消费）：
 * - --ce-primary, --ce-on-primary, --ce-primary-container, --ce-on-primary-container
 * - --ce-surface, --ce-on-surface, --ce-surface-variant, --ce-on-surface-variant
 * - --ce-outline, --ce-outline-variant, --ce-background, --ce-on-background
 * - --ce-error
 * - --ce-font-size, --ce-line-height, --ce-letter-spacing, --ce-font-weight
 * - --ce-gutter-bg, --ce-gutter-fg, --ce-selection, --ce-active-line
 */
object CodeEditorHtmlTemplate {

    fun generate(
        isDarkTheme: Boolean,
        colors: EditorColors,
        storageKey: String = "default",
        fontSizePx: Float = 14f,
        lineHeightPx: Float = 20f,
        letterSpacingPx: Float = 0f,
        fontWeight: Int = 400
    ): String {
        val darkClass = if (isDarkTheme) "cm-theme-dark" else "cm-theme-light"
        val fullStorageKey = "code_editor_draft_$storageKey"
        return """
<!DOCTYPE html>
<html lang="zh-CN" class="h-full $darkClass">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <title>Code Editor</title>
    <script>
        // 在最早时机设置 storage key，避免时序问题
        window.CODE_EDITOR_STORAGE_KEY = "$fullStorageKey";
    </script>
    <style>
        :root {
            --ce-primary: ${colors.primary};
            --ce-on-primary: ${colors.onPrimary};
            --ce-primary-container: ${colors.primaryContainer};
            --ce-on-primary-container: ${colors.onPrimaryContainer};
            --ce-surface: ${colors.surface};
            --ce-on-surface: ${colors.onSurface};
            --ce-surface-variant: ${colors.surfaceVariant};
            --ce-on-surface-variant: ${colors.onSurfaceVariant};
            --ce-outline: ${colors.outline};
            --ce-outline-variant: ${colors.outlineVariant};
            --ce-background: ${colors.background};
            --ce-on-background: ${colors.onBackground};
            --ce-error: ${colors.error};
            --ce-font-size: ${fontSizePx}px;
            --ce-line-height: ${lineHeightPx}px;
            --ce-letter-spacing: ${letterSpacingPx}px;
            --ce-font-weight: $fontWeight;
        }

        * { margin: 0; padding: 0; box-sizing: border-box; }

        html, body {
            height: 100%;
            overflow: hidden;
            font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace;
            background-color: var(--ce-background);
            color: var(--ce-on-background);
        }

        #app {
            display: flex;
            flex-direction: column;
            height: 100%;
        }

        #editor-container {
            flex: 1;
            overflow: auto;
            background-color: var(--ce-background);
            order: 1;
        }

        #editor {
            min-height: 100%;
        }

        #editor-container::-webkit-scrollbar { width: 6px; height: 6px; }
        #editor-container::-webkit-scrollbar-track { background: transparent; }
        #editor-container::-webkit-scrollbar-thumb { background-color: var(--ce-outline-variant); border-radius: 3px; }

        /* 选区浮层 */
        #selection-toolbar {
            position: fixed;
            display: none;
            flex-direction: row;
            gap: 2px;
            padding: 6px 8px;
            background-color: var(--ce-surface);
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
            z-index: 1000;
            transform: translateX(-50%);
        }
        #selection-toolbar.visible { display: flex; }
        .selection-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 32px;
            height: 32px;
            border: none;
            border-radius: 6px;
            background-color: transparent;
            color: var(--ce-on-surface);
            cursor: pointer;
            -webkit-tap-highlight-color: transparent;
        }
        .selection-btn:active { background-color: var(--ce-primary-container); }
        .selection-btn svg { width: 18px; height: 18px; }

        /* CodeMirror 6 主题样式（通过 CSS 变量接入 MD3） */
        .cm-editor {
            height: 100%;
            font-size: var(--ce-font-size);
            font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', 'Monaco', monospace;
        }

        .cm-editor.cm-focused {
            outline: none;
        }

        .cm-scroller {
            font-family: inherit;
            line-height: var(--ce-line-height);
            letter-spacing: var(--ce-letter-spacing);
        }

        .cm-content {
            padding: 12px 0;
            caret-color: var(--ce-primary);
        }

        .cm-line {
            padding: 0 16px;
        }

        .cm-cursor, .cm-dropCursor {
            border-left: 2px solid var(--ce-primary);
        }

        .cm-activeLine {
            background-color: var(--ce-surface-variant);
        }

        /* 行号区:透明 + 无边框,与编辑区同色 */
        .cm-gutters,
        .cm-gutter {
            background-color: transparent !important;
            border: none !important;
            border-right: 0 !important;
            box-shadow: none !important;
            color: var(--ce-on-surface-variant);
        }
        /* 当前行号:背景轻微高亮 + 数字本身高亮 (primary 色 + 加粗) */
        .cm-activeLineGutter,
        .cm-activeLineGutter.cm-gutter {
            background-color: var(--ce-surface-variant) !important;
        }
        .cm-activeLineGutter .cm-gutterElement {
            color: var(--ce-primary) !important;
            font-weight: 600 !important;
            opacity: 1 !important;
        }

        .cm-lineNumbers .cm-gutterElement {
            padding: 0 8px 0 12px;
            min-width: 32px;
            width: auto;
            text-align: right;
            opacity: 0.6;
        }

        .cm-selectionBackground, ::selection {
            background-color: var(--ce-primary-container) !important;
            opacity: 0.5;
        }

        .cm-editor ::selection {
            background-color: var(--ce-primary-container);
        }

        .cm-placeholder {
            color: var(--ce-on-surface-variant);
            opacity: 0.6;
            font-style: italic;
        }

        .cm-searchMatch {
            background-color: var(--ce-primary-container);
            outline: 1px solid var(--ce-primary);
        }

        .cm-searchMatch.cm-searchMatch-selected {
            background-color: var(--ce-primary);
            color: var(--ce-on-primary);
        }

        .cm-panels {
            background-color: var(--ce-surface);
            color: var(--ce-on-surface);
            border-bottom: 1px solid var(--ce-outline-variant);
        }

        .cm-panels input[type=text] {
            background-color: var(--ce-surface);
            color: var(--ce-on-surface);
            border: 1px solid var(--ce-outline);
            border-radius: 4px;
            padding: 4px 8px;
        }

        .cm-tooltip {
            background-color: var(--ce-surface);
            color: var(--ce-on-surface);
            border: 1px solid var(--ce-outline-variant);
            border-radius: 6px;
        }

        .cm-tooltip-autocomplete ul li[aria-selected] {
            background-color: var(--ce-primary-container);
            color: var(--ce-on-primary-container);
        }

        /* 工具栏样式 - 底部固定 */
        #toolbar {
            display: none;
            flex-wrap: nowrap;
            gap: 4px;
            padding: 6px 12px;
            padding-bottom: calc(6px + env(safe-area-inset-bottom, 0px));
            background-color: transparent;
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
            color: var(--ce-on-surface-variant);
            cursor: pointer;
            transition: all 0.15s ease;
            -webkit-tap-highlight-color: transparent;
            flex-shrink: 0;
        }

        .toolbar-btn:hover { background-color: var(--ce-surface-variant); }
        .toolbar-btn:active { background-color: var(--ce-primary-container); }
        .toolbar-btn.active { background-color: var(--ce-primary-container); color: var(--ce-on-primary-container); }
        .toolbar-btn svg { width: 18px; height: 18px; }

        .toolbar-divider {
            width: 1px;
            height: 24px;
            background-color: var(--ce-outline-variant);
            margin: 6px 4px;
            flex-shrink: 0;
        }

        /* Loading 状态 */
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
            border: 3px solid var(--ce-outline-variant);
            border-top-color: var(--ce-primary);
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin { to { transform: rotate(360deg); } }

        #loading-text {
            color: var(--ce-on-surface-variant);
            font-size: 14px;
        }

        /* 错误状态 */
        #error-text {
            color: var(--ce-error);
            font-size: 13px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div id="app">
        <div id="editor-container">
            <div id="loading">
                <div class="spinner"></div>
                <div id="loading-text">正在加载编辑器…</div>
            </div>
            <div id="editor" style="display: none;"></div>
        </div>

        <div id="toolbar">
            <button class="toolbar-btn" data-action="undo" title="撤销">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 10h10a5 5 0 0 1 5 5v0a5 5 0 0 1-5 5H7m-4-10l4-4m-4 4l4 4"/>
                </svg>
            </button>
            <button class="toolbar-btn" data-action="redo" title="重做">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M21 10H11a5 5 0 0 0-5 5v0a5 5 0 0 0 5 5h6m4-10l-4-4m4 4l-4 4"/>
                </svg>
            </button>
            <div class="toolbar-divider"></div>
            <button class="toolbar-btn" data-action="find" title="查找">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="7"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                </svg>
            </button>
            <div class="toolbar-divider"></div>
            <button class="toolbar-btn" data-action="copy" title="复制">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="9" y="9" width="13" height="13" rx="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
            </button>
            <button class="toolbar-btn" data-action="paste" title="粘贴">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
                    <rect x="8" y="2" width="8" height="4" rx="1"/>
                </svg>
            </button>
            <button class="toolbar-btn" data-action="selectAll" title="全选">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <path d="M9 3v18"/><path d="M15 3v18"/><path d="M3 9h18"/><path d="M3 15h18"/>
                </svg>
            </button>
            <div class="toolbar-divider"></div>
            <button class="toolbar-btn" data-action="toggleWrap" title="换行">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 6h18M3 12h15a3 3 0 1 1 0 6h-4M16 16l-2 2 2 2M21 18h-3"/>
                </svg>
            </button>
        </div>

        <!-- 选区浮层 -->
        <div id="selection-toolbar">
            <button class="selection-btn" data-action="copy" title="复制">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="9" y="9" width="13" height="13" rx="2"/>
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
                </svg>
            </button>
            <button class="selection-btn" data-action="paste" title="粘贴">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/>
                    <rect x="8" y="2" width="8" height="4" rx="1"/>
                </svg>
            </button>
            <div class="toolbar-divider"></div>
            <button class="selection-btn" data-action="selectAll" title="全选">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2"/>
                    <path d="M9 3v18"/><path d="M15 3v18"/><path d="M3 9h18"/><path d="M3 15h18"/>
                </svg>
            </button>
        </div>
    </div>
    <script src="editor.bundle.js"></script>
</body>
</html>
        """.trimIndent()
    }
}
