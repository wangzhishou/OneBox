# 内置文档渲染页的来源与许可

`src/main/assets/viewer/` 下的 HTML / JS 是**构建产物**：源码在
`web/file_viewer/src/`，由 `npm run build:viewer` 压缩后写到这里。改页面请改源码，
不要直接改 assets（`viewer/lib/` 例外，那是第三方 dist，原样打包）。

原始 HTML / JS / CSS 来自开源项目 **Gander**，做离线文档预览用：

- 上游：https://github.com/mokshablr/gander
- 版本：v2.5，commit `8f60e20`
- 许可：MIT，Copyright (c) 2026 Arjun Maniyani
- 完整许可文本：随包放在 `src/main/assets/viewer/licences.md`（含下表每个库的原文）

## 取了什么

| 我们的文件 | 上游文件 | 说明 |
|---|---|---|
| `app.js` `app.css` `find.js` | 同名 | 页面公共底座：状态条、错误卡、查找、下载/链接处理 |
| `docx.html` `docx.js` `docx-pages.js` `docx-lines.js` `docx-night.js` | 同名 | Word 2007+ 预览 |
| `prose.html` `prose.js` `prose.css` `prose-doc.js` `prose-odt.js` `prose-rtf.js` `prose-draw.js` `symbol-fonts.js` | 同名 | Word 97-2003 / ODT / RTF（Gander 自研 reader） |
| `xlsx.html` `xlsx.js` | 同名 | Excel / ODS / CSV |
| `pptx.html` `pptx.js` | 同名 | PowerPoint 2007+ |
| `epub.html` `epub.js` `epub.css` | 同名 | EPUB（Gander 自研 reader） |
| `text.html` `text.js` `md.html` `md.js` `imgweb.html` `imgweb.js` `unsupported.html` `unsupported.js` | 同名 | 文本、Markdown、动图/SVG、兜底页 |
| `lib/docx-preview.min.js` | 同名 | docx-preview 0.4.0，Apache-2.0 |
| `lib/jszip3.min.js` | 同名 | JSZip 3.10.1，MIT or GPL-3.0（选 MIT） |
| `lib/xlsx.full.min.js` | 同名 | SheetJS CE 0.20.3，Apache-2.0 |
| `lib/marked.min.js` | 同名 | marked 15.0.12，MIT |
| `lib/purify.min.js` | 同名 | DOMPurify 3.4.12，Apache-2.0 or MPL-2.0（选 Apache） |
| `lib/pptx/*` | 同名 | PPTXjs 1.21.1（MIT）、divs2slides、FileReader.js、jQuery 1.11.3（MIT）、JSZip 2.x、D3 3.5.10（BSD-3）、NVD3 1.8.1（Apache-2.0） |

## 特意没取

- `pdf.html` / `pdf.mjs` / `pdf.min.mjs` / `pdf.worker.min.mjs` / `cmaps/` / `wasm/`：万宝盒的 PDF 走 `androidx.pdf`，不重复引入（这部分约 1.7MB 压缩体积）。
- `model*.js`（STL）、zip 浏览相关 Kotlin：当前范围外。

## 维护注意

1. **`lib/pptx/pptxjs.js` 是打过补丁的**：上游为"单个形状画不出就留空盒继续画"加了 `try/catch` 与 `missingPlace()`。**不要**用上游原版覆盖，也不要直接跑 gander 的 `scripts/fetch-viewer-libs.sh` 更新这个文件。
2. 宿主与页面的契约只有三样，改宿主时保持：
   - 页面地址：`https://appassets.androidplatform.net/assets/viewer/<page>.html`
   - 文档地址：同域下 `/doc/file.<ext>`（PPTX 页把这个地址写死成 `/doc/file.pptx`，所以 `/doc/` 必须通配）
   - 查询参数：`name`、`ext`、`length`、`webview`、`needs`、`locked`（后者用于"WebView 太旧"提示卡）
3. 页面里的 CSP `<meta>` 与 `ViewerWebHost.VIEWER_CSP` 必须逐字一致；改一处要同步另一处。
4. 升级上游时只 diff `assets/viewer/`，并重新跑一遍 docx/xlsx/pptx/rtf/epub 的冒烟样例。
