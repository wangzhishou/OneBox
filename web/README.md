# 文件传输网页构建指南

本目录包含文件传输功能的网页源代码及构建工具。

## 目录结构

```
web/
├── file_transfer/          # 源代码目录
│   ├── index.html          # HTML 入口
│   ├── js/
│   │   ├── bootstrap.js    # JS 入口 (ES Module)
│   │   ├── core/           # 核心模块
│   │   ├── features/       # 功能模块
│   │   └── ui/             # UI 组件
│   └── css/                # CSS 样式 (如有)
├── package.json            # npm 配置
├── build.js                # 构建脚本
└── README.md               # 本文档
```

## 快速开始

### 1. 安装依赖

```bash
cd web
npm install
```

### 2. 构建打包

```bash
npm run build
```

构建后的文件将输出到:  
`feature/file-transfer/src/main/assets/file_transfer/`

### 3. 监听模式 (开发时)

```bash
npm run watch
```

文件变化时自动重新构建。

## 构建输出

| 源文件 | 输出文件 | URL 路由 | 说明 |
|--------|----------|----------|------|
| `js/bootstrap.js` | `js/app.min.js` | `/static/js/app.min.js` | 所有 JS 打包成单文件 |
| `css/*.css` | `css/*.min.css` | `/static/css/*.min.css` | CSS 压缩 |
| `index.html` | `index.html` | `/` | HTML 压缩 |

### 服务端路由映射

服务端 `FileTransferServer.kt` 的路由规则:

```
/                       -> assets/file_transfer/index.html
/static/xxx             -> assets/file_transfer/xxx
/js/xxx                 -> assets/js/xxx
```

所以:
- `/static/js/app.min.js` -> `assets/file_transfer/js/app.min.js` ✓
- `/js/tailwindcss.js` -> `assets/js/tailwindcss.js` ✓

## 添加新的 CSS 文件

1. 在 `file_transfer/css/` 目录下创建 CSS 文件:

```bash
mkdir -p file_transfer/css
touch file_transfer/css/custom.css
```

2. 在 `index.html` 中引用:

```html
<link rel="stylesheet" href="/css/custom.css">
```

3. 运行 `npm run build`，构建脚本会自动:
   - 压缩 CSS 文件为 `custom.min.css`
   - 更新 HTML 中的引用为 `/css/custom.min.css`

## 添加新的 JS 模块

1. 在对应目录下创建 JS 文件:

```bash
# 例如添加新功能模块
touch file_transfer/js/features/newFeature.js
```

2. 在 `bootstrap.js` 中导入:

```javascript
import { newFunction } from './features/newFeature.js';
```

3. 运行 `npm run build`，esbuild 会自动将所有模块打包到 `app.min.js`

## 构建配置

可以在 `build.js` 中修改配置:

```javascript
const config = {
  srcDir: path.join(__dirname, 'file_transfer'),  // 源码目录
  outDir: path.join(__dirname, '../feature/file-transfer/src/main/assets/file_transfer'), // 输出目录
  watch: process.argv.includes('--watch')
};
```

### esbuild 选项

```javascript
await esbuild.build({
  entryPoints: [entryPoint],
  bundle: true,          // 打包所有模块
  minify: true,          // 压缩代码
  format: 'esm',         // ES Module 格式
  target: ['es2020'],    // 目标 ES 版本
  treeShaking: true,     // 移除未使用代码
  drop: ['console', 'debugger'] // 移除调试代码
});
```

### HTML 压缩选项

```javascript
const htmlMinifyOptions = {
  collapseWhitespace: true,    // 合并空白
  removeComments: true,         // 移除注释
  minifyCSS: true,              // 压缩内联 CSS
  minifyJS: true                // 压缩内联 JS
};
```

## 常见问题

### Q: 如何保留 console.log?

修改 `build.js` 中的 esbuild 配置，移除 `drop` 选项:

```javascript
// drop: ['console', 'debugger'] // 注释掉这行
```

### Q: 如何生成 source map?

修改 esbuild 配置:

```javascript
sourcemap: true,  // 改为 true
```

### Q: 如何排除某些文件不被压缩?

静态资源目录 (images, fonts, icons) 会直接复制，不会被处理。

## 依赖说明

- **esbuild**: 超快的 JavaScript/CSS 打包器
- **html-minifier-terser**: HTML 压缩工具

## 注意事项

1. `tailwindcss.js` 位于 `assets/js/` 目录，对应 `/js/` 路由，构建时不会被影响
2. 打包后的 `app.min.js` 位于 `assets/file_transfer/js/`，对应 `/static/js/` 路由
3. HTML 中的内联 `onclick` 事件处理器需要在 `bootstrap.js` 中通过 `window.xxx` 暴露
4. 源码中 JS 引用是 `/static/js/bootstrap.js`，构建后会自动替换为 `/static/js/app.min.js`

## 输出路径

构建后的文件输出到:

```
feature/file-transfer/src/main/assets/
├── file_transfer/
│   ├── index.html          # 压缩后的 HTML (路由: /)
│   └── js/
│       └── app.min.js      # 打包压缩后的 JS (路由: /static/js/app.min.js)
└── js/
    └── tailwindcss.js      # Tailwind CSS (路由: /js/tailwindcss.js，不会被覆盖)
```

## 版本历史

- **1.0.0** - 初始版本，支持 JS/CSS/HTML 打包压缩

