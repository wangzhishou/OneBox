# Markdown Editor - 使用说明

## 概述

Markdown Editor 是一个功能完整的 Markdown 编辑器组件，提供以下特性：

- ✅ 类似 `OutlinedTextField` 的 API 设计
- ✅ 支持常用 Markdown 语法快捷输入（粗体、斜体、下划线、标题、列表、链接、图片）
- ✅ 编辑和预览模式切换
- ✅ 响应式设计，适配不同屏幕尺寸和方向
- ✅ 大屏设备分栏布局（编辑器和预览并排显示）
- ✅ 工具栏仅在获取焦点后显示（性能优化）
- ✅ 工具栏位于底部，便于操作

## 核心组件

### 1. MarkdownEditor

基础的 Markdown 编辑器组件，类似于 `OutlinedTextField`。

```kotlin
@Composable
fun MyScreen() {
    var text by remember { mutableStateOf("# Hello Markdown") }
    
    MarkdownEditor(
        value = text,
        onValueChange = { text = it },
        placeholder = "输入 Markdown 内容...",
        label = "Markdown 编辑器",
        modifier = Modifier.fillMaxSize()
    )
}
```

### 2. ResponsiveMarkdownEditor

响应式 Markdown 编辑器，支持横屏分栏布局。

```kotlin
@Composable
fun MyResponsiveScreen() {
    var text by remember { mutableStateOf("") }
    val state = rememberMarkdownEditorState(initialText = text)
    
    ResponsiveMarkdownEditor(
        value = text,
        onValueChange = { text = it },
        state = state,
        placeholder = "开始编写...",
        modifier = Modifier.fillMaxSize(),
        splitViewInLandscape = true  // 横屏时使用分栏布局
    )
}
```

## API 参数说明

### MarkdownEditor 参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `value` | `String` | 当前文本内容 |
| `onValueChange` | `(String) -> Unit` | 文本改变回调 |
| `modifier` | `Modifier` | 修饰符 |
| `state` | `MarkdownEditorState` | 编辑器状态（可选） |
| `placeholder` | `String` | 占位符文本 |
| `label` | `String?` | 标签 |
| `enabled` | `Boolean` | 是否可用 |
| `readOnly` | `Boolean` | 是否只读 |
| `showToolbar` | `Boolean` | 是否显示工具栏（默认在获取焦点后显示） |
| `tools` | `List<MarkdownToolConfig>` | 工具栏工具列表 |

### MarkdownEditorState

编辑器状态管理类：

```kotlin
val state = rememberMarkdownEditorState(
    initialText = "# 初始内容",
    initialIsPreview = false
)

// 切换预览模式
state.togglePreview()

// 设置预览模式
state.setPreview(true)

// 获取当前文本
val currentText = state.text

// 判断是否为预览模式
if (state.isPreview) {
    // 预览模式
}
```

## 支持的 Markdown 语法

工具栏提供以下快捷操作：

| 工具 | 语法 | 说明 |
|------|------|------|
| 粗体 | `**text**` | 加粗文本 |
| 斜体 | `*text*` | 斜体文本 |
| 下划线 | `<u>text</u>` | 下划线文本 |
| 一级标题 | `# ` | H1 标题 |
| 二级标题 | `## ` | H2 标题 |
| 三级标题 | `### ` | H3 标题 |
| 无序列表 | `- ` | 列表项 |
| 有序列表 | `1. ` | 编号列表 |
| 链接 | `[text](url)` | 超链接 |
| 图片 | `![alt](url)` | 图片 |
| 行内代码 | `` `code` `` | 代码 |
| 引用 | `> ` | 引用文本 |
| 删除线 | `~~text~~` | 删除文本 |

## 响应式布局

### 竖屏模式（手机/平板竖屏）
- 编辑器占据全屏
- 点击预览按钮切换到预览模式
- 工具栏位于底部

### 横屏模式（大屏设备/平板横屏）
- 启用预览时，自动切换为分栏布局
- 左侧：编辑器
- 右侧：实时预览
- 工具栏在编辑器底部

## 自定义工具栏

可以自定义工具栏显示的工具：

```kotlin
val customTools = listOf(
    MarkdownTools.BOLD,
    MarkdownTools.ITALIC,
    MarkdownTools.HEADING_1,
    MarkdownTools.LINK,
    MarkdownTools.IMAGE
)

MarkdownEditor(
    value = text,
    onValueChange = { text = it },
    tools = customTools,
    modifier = Modifier.fillMaxSize()
)
```

## 性能优化

1. **工具栏延迟显示**：工具栏仅在编辑器获取焦点时显示，避免不必要的渲染
2. **响应式布局**：根据屏幕方向和尺寸自动调整布局
3. **状态保存**：使用 `rememberSaveable` 保存编辑器状态，防止配置改变时丢失内容

## 示例场景

### 1. 简单的 Markdown 编辑器

```kotlin
@Composable
fun SimpleMarkdownEditor() {
    var markdown by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Markdown Editor") })
        }
    ) { padding ->
        MarkdownEditor(
            value = markdown,
            onValueChange = { markdown = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
```

### 2. 带保存功能的编辑器

```kotlin
@Composable
fun EditableMarkdownDocument(
    initialContent: String,
    onSave: (String) -> Unit
) {
    var markdown by remember { mutableStateOf(initialContent) }
    val state = rememberMarkdownEditorState(initialText = markdown)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("编辑文档") },
                actions = {
                    IconButton(onClick = { onSave(markdown) }) {
                        Icon(Icons.Default.Save, "保存")
                    }
                }
            )
        }
    ) { padding ->
        ResponsiveMarkdownEditor(
            value = markdown,
            onValueChange = { markdown = it },
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
```

### 3. 仅预览模式

```kotlin
@Composable
fun MarkdownViewer(content: String) {
    MarkdownPreview(
        markdown = content,
        modifier = Modifier.fillMaxSize()
    )
}
```

## 包结构

```
com.wanbaohe.markdown.edit/
├── MarkdownEditor.kt                   // 主编辑器组件
├── MarkdownEditorState.kt              // 状态管理
├── MarkdownTool.kt                     // 工具定义
├── MarkdownToolbar.kt                  // 工具栏组件
├── MarkdownPreview.kt                  // 预览组件
└── ResponsiveMarkdownEditor.kt         // 响应式编辑器
```

## 依赖

- `androidx.compose.material3:material3`
- `com.halilibo.richtext:markdown` (项目内部)
- `com.mohamedrejeb:richeditor` (项目内部)
- `core.ui` (isPortraitOrientationAsState)

## 注意事项

1. 确保在使用前已正确配置项目的 Compose 依赖
2. 工具栏图标使用 Material Icons
3. 预览功能依赖项目中的 `BasicMarkdown` 渲染器
4. 支持的 Markdown 语法遵循 CommonMark 规范

