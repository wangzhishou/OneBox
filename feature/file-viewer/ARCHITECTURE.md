# file-viewer 模块架构

## 分层

```
Compose (screen/)                 FileViewerScreen
   │  只读 state、转发点击
   ▼
Decompose (component/)            FileViewerComponent
   │  解析 URI → 页面/名字/大小；页码、查找、夜间、进度的状态机
   ▼
domain/                           DocumentSource / ViewerFormat
   │  只暴露"读流/长度/MIME"与"扩展名 → 页面 + 能力"
   ▼
data/                             ContentResolverDocumentSource
                                  ViewerWebHost（WebView + /doc/ + port）
                                  ViewerPreferences（夜间、阅读位置）
```

UI 层不碰 `ContentResolver`，读文件全部经 `DocumentSource`；页面能力（能不能查找、
有没有页码、支不支持夜间）由 `ViewerFormat` 描述，UI 只是照着显示控件。

## 支持的格式

整个 App 的文件打开共覆盖 124 种扩展名（见根目录 README 的“文件打开”一节）。
下表是本模块能渲染的全部页面；实际从系统或文件浏览器打开时，`ContentRouter` 会优先把
图片、PDF、音视频、ZIP、HTML、Markdown 分给各自模块，只有文档类落到这里，
`md` / `text` 页面用于直接进入预览或兜底。

扩展名 → 页面 的唯一入口是 `ViewerFormat.fromExtension`；页面能力由枚举项声明。

| 页面 | 扩展名 | 渲染器 | 能力 |
|---|---|---|---|
| `docx.html` | docx / docm / dotx | docx-preview | 页码、查找、夜间 |
| `prose.html` | doc / dot / odt / ott / fodt / rtf | 自研 prose reader | 查找 |
| `xlsx.html` | xlsx / xls / xlsm / xlsb / xltx / csv / ods | SheetJS | 查找 |
| `pptx.html` | pptx / ppsx / pptm / potx | PPTXjs | 查找 |
| `ppt.html` | ppt | 二进制文字大纲 | 查找 |
| `odp.html` | odp / otp | 按幻灯片绘制 | 查找 |
| `epub.html` | epub | 自研 EPUB reader | 查找、阅读进度 |
| `md.html` | md / markdown / mkd / mdown | marked + DOMPurify | 查找 |
| `text.html` | txt / log / json / xml / yaml / ini / cfg / conf / properties / toml / srt / vtt / m3u / nfo | text.js | 5MB 分页、查找 |
| `imgweb.html` | gif / svg / avif / ico | WebView 原生解码 | — |
| `unsupported.html` | 其他后缀 | 兜底页 | 按文本查看 |

新增格式时，除了在 `ViewerFormat` 里加页面和后缀，还要同步
`ContentType.DOCUMENT_EXTENSIONS`（`core/domain`）与 `app/src/main/AndroidManifest.xml`
的 VIEW filter，外部“打开方式”才进得来。

## 页面从哪来

```
web/file_viewer/src/*.html|js|css     ← 唯一可手改的源码（来自 gander）
        │  npm run build:viewer（esbuild + html-minifier）
        ▼
feature/file-viewer/src/main/assets/viewer/*   ← 压缩产物，随 APK 打包
feature/file-viewer/src/main/assets/viewer/lib/ ← 第三方 dist，原样打包，不参与构建
```

构建规则、为什么逐文件压缩、页面契约都写在 `web/file_viewer/README.md`。

## 运行时

```
FileViewerComponent ──create──▶ ViewerWebHost ──▶ WebView
                                                   │
                    https://appassets.androidplatform.net/assets/viewer/<page>.html
                    https://appassets.androidplatform.net/doc/file.<ext>   ← 自定义拦截，支持 Range
                                                   │
                                           message port（find.js）
                    宿主 → 页面：q/n/p/c · g<页> · i1|i0 · t<dp>
                    页面 → 宿主：page n 总 · at 总 done · at 章 偏移
```

- 页面永远看不到 `content://`；`allowFileAccess`/`allowContentAccess` 都是 false，
  非本域 http(s) 一律 404，asset 响应带 CSP。
- 端口在 `onPageFinished` 打开；打不开时页面照样渲染，只是少了页码/查找/夜间。
- WebView 由 Compose 的 `AndroidView` 持有，`onRelease` 里先 `release()` 再 `destroy()`。

## 阅读能力

| 能力 | 依赖 | 说明 |
|---|---|---|
| 页码 + 跳页 | `ViewerFormat.supportsPages` | 页面回报 `page n 总`；跳页发 `g<n>` |
| 夜间 | `supportsNight`（Word） | 页面无独立夜间样式的，宿主在加载后挂 `dark.css` 并给内容容器打 `.vw-force-dark`，用 CSS 滤镜翻纸；Word 走自己的 `night=1` / `i1/i0`。整个模块只**跟随**全局日夜，页面上没有独立开关 |
| 查找 | `supportsFind` | 发 `q/n/p/c`，回收 `at 总 done` 显示计数；浮层高度经 `t64` 让位 |
| 阅读进度 | 全部 | Word 存页码，EPUB 存页面回报的 `at 章 偏移`；键 = SHA-256(uri + 长度) |
