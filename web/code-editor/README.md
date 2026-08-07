# Code Editor — Web 端

WebView 通用代码编辑器的 Web 端源码，构建产物输出到 Android assets。

## 概述

基于 **CodeMirror 6** 的代码编辑器（替换 Milkdown 用于 CodeEditor 场景）。提供：

- 12 种语言语法高亮（Phase 5 完整版，Phase 3 脚手架 9 种）
- 行号、当前行高亮、括号匹配、撤销/重做
- 软换行切换、查找/替换
- 与 Android 通过 `window.CodeEditorBridge` 双向通信
- localStorage 草稿自动保存

## 目录结构

```
web/code-editor/
├── package.json          # npm 依赖
├── build.js              # esbuild 打包脚本
└── editor.src.js         # 入口（ES modules）
        ↓ build
feature/code-editor/src/main/assets/code-editor/editor.bundle.js
```

## 构建

```bash
# 安装依赖（首次）
npm install --prefix web/code-editor

# 生产构建（minified）
node web/code-editor/build.js

# 监听模式（开发）
node web/code-editor/build.js --watch
```

## 依赖说明

| 包 | 用途 |
|---|---|
| `codemirror` | `basicSetup`（行号、选区高亮、命令面板等） |
| `@codemirror/state` | 编辑器状态 + Transaction |
| `@codemirror/view` | EditorView + Compartment（动态重配置） |
| `@codemirror/commands` | 默认 keymap + history |
| `@codemirror/language` | StreamLanguage 基础（Shell 等） |
| `@codemirror/search` | 搜索 keymap + 高亮 |
| `@codemirror/legacy-modes` | Shell 等 StreamLanguage 实现 |
| `@codemirror/lang-*` | 各语言高亮（js/ts/json/html/css/python/md/java/sql/yaml） |

## Bridge 接口

见 `editor.src.js` 内的 `window.CodeEditorBridge` 对象，详见 [Phase 4 文档]。

## DOM 期望

由 Kotlin 侧 `CodeEditorHtmlTemplate` 提供：

- `#editor` — CodeMirror 挂载点
- `#loading` — 加载状态（init 后隐藏）
- `#toolbar` — 工具栏，按钮含 `.toolbar-btn[data-action="..."]`

## Phase 进度

- ✅ Phase 3：脚手架 + CodeMirror 6 + Bridge 契约
- 🟡 Phase 4：Kotlin 侧 `WebViewCodeEditor` + `CodeEditorHtmlTemplate` + Bridge
- 🟡 Phase 5：12 种 lang 完整版 + 行号定制 + bracket pair
- 🟡 Phase 6：业务能力（文件 I/O、历史、草稿、选区浮层、查找面板 UI）
