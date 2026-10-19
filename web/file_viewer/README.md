# 文件预览页（web/file_viewer）

文档预览的 HTML/JS/CSS 源码。产物写进
`feature/file-viewer/src/main/assets/viewer/`，**安装包里只有压缩后的文件**。

## 目录

```
web/file_viewer/
├── src/                  # 源码（来自 gander，只有这里可以手改）
│   ├── *.html            # 页面骨架
│   ├── *.js              # 页面逻辑（经典脚本，靠顶层全局函数互相调用）
│   ├── *.css
│   └── licences.md       # 随包的第三方许可全文
└── build.mjs             # 压缩输出
```

`feature/file-viewer/src/main/assets/viewer/lib/` 是第三方 dist（docx-preview、
SheetJS、PPTXjs、jQuery…），**不参与构建**，直接原样打包；它们本来就已经压缩过。

## 构建

```bash
cd web
npm install                 # 首次
npm run build:viewer        # 压缩到 assets
npm run build:viewer:check  # 只校验产物是否最新，CI 可用
```

## 为什么逐文件压缩，而不是打成一个 bundle

这些页面是 gander 的经典脚本，彼此通过**顶层全局函数**通信：`app.js` 定义
`vwStatus`、`vwParams`，`docx.js` 直接调用；`find.js` 再把收到的 port 命令转给
`window.vwPageCommand`。一旦 bundle 成 IIFE/模块，这些名字就进了闭包，页面会
静默失效。所以 build.mjs：

- 逐文件 `esbuild.transform`，不开 bundle；
- `minifyIdentifiers: false`（顶层名字必须原样保留）；
- HTML 用 html-minifier-terser，但 `minifyJS: false`（页面里没有内联脚本）；
- 输出文件名保持不变，HTML 里的 `<script src>` 无需改写。

## 改页面时注意

1. 只改 `src/`，不要直接改 `assets/viewer/`——下次构建会被覆盖。
2. 页面与宿主的契约只有三样：`/assets/viewer/<page>.html`、`/doc/file.<ext>`、
   查询参数 `name/ext/length/night/at/webview/needs/locked`。
3. 宿主通过 message port 发 `q/n/p/c`（查找）、`g<页>`（跳页）、`i1|i0`（夜间）、
   `t<dp>`（顶部让位）；页面回 `page <n> <总>`、`<at> <总> <done>`、`at <章> <偏移>`。
4. 页面里的 CSP `<meta>` 必须和 `ViewerWebHost.VIEWER_CSP` 逐字一致。
5. 改完跑 `npm run build:viewer`，再在模拟器上过一遍 docx/xlsx/pptx/rtf/epub。
