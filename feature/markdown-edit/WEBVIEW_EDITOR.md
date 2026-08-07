# WebView Markdown 编辑器 - 实现说明

## 概述

基于 Milkdown + WebView 实现的所见即所得 Markdown 编辑器，完全离线运行，支持 Material 3 主题适配。

## 架构

```
feature/markdown-edit/
├── src/main/
│   ├── assets/
│   │   ├── js/
│   │   │   └── tailwindcss.js          # TailwindCSS 运行时
│   │   └── milkdown-editor/
│   │       ├── index.html              # 编辑器页面
│   │       ├── style.css               # 自定义样式
│   │       ├── editor.src.js           # 编辑器源码
│   │       └── editor.bundle.js        # 打包后的 JS
│   └── java/com/wanbaohe/markdown/edit/
│       └── webview/
│           ├── MarkdownEditorBridge.kt # JS Bridge 接口
│           ├── WebViewMarkdownEditor.kt # Compose 组件
│           └── MarkdownWebViewPool.kt  # WebView 预加载池

web/milkdown-editor/
├── package.json                        # npm 依赖配置
├── build.js                            # esbuild 构建脚本
└── node_modules/                       # Milkdown 依赖
```

## 功能特性

### 编辑功能
- ✅ 粗体、斜体、删除线
- ✅ 一级/二级/三级标题
- ✅ 有序列表、无序列表、任务列表
- ✅ 引用块、代码块、行内代码
- ✅ 链接插入
- ✅ 图片插入（URL 或从相册选择）
- ✅ 分割线
- ✅ 撤销/重做

### 主题适配
- ✅ Material 3 颜色变量注入
- ✅ 深色/浅色主题自动切换
- ✅ 与 Android 系统主题同步

### 性能优化
- ✅ WebView 预加载池
- ✅ 离线资源打包（约 450KB）
- ✅ Loading 动画

## 使用方式

```kotlin
@Composable
fun MyScreen() {
    var markdown by remember { mutableStateOf("") }
    
    WebViewMarkdownEditor(
        value = markdown,
        onValueChange = { markdown = it },
        placeholder = "输入内容...",
        modifier = Modifier.fillMaxSize()
    )
}
```

## 构建前端资源

```bash
cd web/milkdown-editor
npm install
npm run build
```

输出文件：`feature/markdown-edit/src/main/assets/milkdown-editor/editor.bundle.js`

## WebView 预加载（可选）

在 Application 中初始化：

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MarkdownWebViewPool.init(this)
        
        // 在空闲时预加载
        Handler(Looper.getMainLooper()).postDelayed({
            MarkdownWebViewPool.preload()
        }, 3000)
    }
}
```

## JS Bridge API

### Android → JS

```javascript
// 设置内容
window.AndroidBridge.setContent(markdown)

// 获取内容
window.AndroidBridge.getContent()

// 设置主题
window.AndroidBridge.setTheme(isDark)

// 设置颜色
window.AndroidBridge.setColors(colorsJson)

// 设置占位符
window.AndroidBridge.setPlaceholder(text)

// 设置只读
window.AndroidBridge.setReadOnly(readOnly)

// 插入图片
window.AndroidBridge.insertImage(url, alt)
```

### JS → Android

```kotlin
@JavascriptInterface
fun onContentChanged(markdown: String)

@JavascriptInterface
fun onEditorReady()

@JavascriptInterface
fun pickImage()
```

## 依赖版本

- Milkdown: 7.5.0
- esbuild: 0.19.0
- TailwindCSS: 内置 CDN 版本

## 后续优化

1. **表格编辑**：Milkdown 已支持表格，可扩展工具栏
2. **数学公式**：可集成 KaTeX 插件
3. **代码高亮**：可集成 Prism.js
4. **协同编辑**：Milkdown 支持 Y.js 协同

