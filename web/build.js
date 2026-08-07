/**
 * 文件传输网页构建脚本
 *
 * 功能:
 * - 打包压缩 JavaScript (ES modules -> 单文件)
 * - 压缩 CSS
 * - 压缩 HTML
 * - 输出到 Android assets 目录
 */

const esbuild = require('esbuild');
const { minify: minifyHtml } = require('html-minifier-terser');
const fs = require('fs');
const path = require('path');

// 配置
const config = {
  // 源文件目录
  srcDir: path.join(__dirname, 'file_transfer'),
  // 输出目录 (Android assets)
  outDir: path.join(__dirname, '../feature/file-transfer/src/main/assets/file_transfer'),
  // 是否监听模式
  watch: process.argv.includes('--watch')
};

// HTML 压缩选项
const htmlMinifyOptions = {
  collapseWhitespace: true,
  removeComments: true,
  removeRedundantAttributes: true,
  removeScriptTypeAttributes: true,
  removeStyleLinkTypeAttributes: true,
  useShortDoctype: true,
  minifyCSS: true,
  minifyJS: true,
  sortAttributes: true,
  sortClassName: true
};

// 确保输出目录存在
function ensureDir(dir) {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
}

// 清理输出目录
function cleanOutDir() {
  if (fs.existsSync(config.outDir)) {
    const files = fs.readdirSync(config.outDir);
    for (const file of files) {
      const filePath = path.join(config.outDir, file);
      fs.rmSync(filePath, { recursive: true, force: true });
    }
  }
}

// 打包 JavaScript
async function buildJS() {
  console.log('📦 打包 JavaScript...');

  const entryPoint = path.join(config.srcDir, 'js/bootstrap.js');
  // 输出到 file_transfer/js/ 目录，对应服务端 /static/js/ 路由
  const outFile = path.join(config.outDir, 'js/app.min.js');

  ensureDir(path.dirname(outFile));

  await esbuild.build({
    entryPoints: [entryPoint],
    bundle: true,
    minify: true,
    format: 'iife', // 改为 IIFE 格式，不需要 type="module"
    target: ['es2020'],
    outfile: outFile,
    sourcemap: false,
    treeShaking: true,
    drop: ['console', 'debugger'] // 移除 console 和 debugger
  });

  const stat = fs.statSync(outFile);
  console.log(`   ✅ app.min.js (${formatSize(stat.size)})`);
}

// 处理 CSS 文件
async function buildCSS() {
  console.log('🎨 打包 CSS...');

  const cssDir = path.join(config.srcDir, 'css');
  if (!fs.existsSync(cssDir)) {
    console.log('   ⏭️  没有 CSS 目录，跳过');
    return;
  }

  const cssFiles = fs.readdirSync(cssDir).filter(f => f.endsWith('.css'));
  if (cssFiles.length === 0) {
    console.log('   ⏭️  没有 CSS 文件，跳过');
    return;
  }

  ensureDir(path.join(config.outDir, 'css'));

  for (const file of cssFiles) {
    const srcPath = path.join(cssDir, file);
    const outPath = path.join(config.outDir, 'css', file.replace('.css', '.min.css'));

    await esbuild.build({
      entryPoints: [srcPath],
      bundle: true,
      minify: true,
      outfile: outPath
    });

    const stat = fs.statSync(outPath);
    console.log(`   ✅ ${file} -> ${path.basename(outPath)} (${formatSize(stat.size)})`);
  }
}

// 处理 HTML 文件
async function buildHTML() {
  console.log('📄 处理 HTML...');

  const srcHtml = path.join(config.srcDir, 'index.html');
  const outHtml = path.join(config.outDir, 'index.html');

  let html = fs.readFileSync(srcHtml, 'utf-8');

  // 替换 JS 引用: /static/js/bootstrap.js -> /static/js/app.min.js
  // 支持多种格式: type="module" src="..." 或 src="..." type="module"
  html = html.replace(
    /<script\s+type="module"\s+src="[^"]*bootstrap\.js"\s*><\/script>/g,
    '<script src="/static/js/app.min.js"></script>'
  );
  html = html.replace(
    /<script\s+src="[^"]*bootstrap\.js"\s+type="module"\s*><\/script>/g,
    '<script src="/static/js/app.min.js"></script>'
  );

  // 替换 CSS 引用: xxx.css -> xxx.min.css (如果有)
  html = html.replace(/href="([^"]+)\.css"/g, 'href="$1.min.css"');

  // 压缩 HTML
  const minified = await minifyHtml(html, htmlMinifyOptions);

  fs.writeFileSync(outHtml, minified);

  const originalSize = Buffer.byteLength(html, 'utf-8');
  const minifiedSize = Buffer.byteLength(minified, 'utf-8');
  const savedPercent = ((1 - minifiedSize / originalSize) * 100).toFixed(1);

  console.log(`   ✅ index.html (${formatSize(originalSize)} -> ${formatSize(minifiedSize)}, 节省 ${savedPercent}%)`);
}

// 复制其他静态资源
function copyAssets() {
  console.log('📁 复制静态资源...');

  // 复制图片、字体等
  const assetDirs = ['images', 'fonts', 'icons'];

  for (const dir of assetDirs) {
    const srcPath = path.join(config.srcDir, dir);
    if (fs.existsSync(srcPath)) {
      const outPath = path.join(config.outDir, dir);
      copyDirSync(srcPath, outPath);
      console.log(`   ✅ ${dir}/`);
    }
  }
}

// 递归复制目录
function copyDirSync(src, dest) {
  ensureDir(dest);
  const entries = fs.readdirSync(src, { withFileTypes: true });

  for (const entry of entries) {
    const srcPath = path.join(src, entry.name);
    const destPath = path.join(dest, entry.name);

    if (entry.isDirectory()) {
      copyDirSync(srcPath, destPath);
    } else {
      fs.copyFileSync(srcPath, destPath);
    }
  }
}

// 格式化文件大小
function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
}

// 主构建流程
async function build() {
  console.log('\n🚀 开始构建...\n');
  const startTime = Date.now();

  try {
    cleanOutDir();
    await buildJS();
    await buildCSS();
    await buildHTML();
    copyAssets();

    const elapsed = ((Date.now() - startTime) / 1000).toFixed(2);
    console.log(`\n✨ 构建完成! 耗时 ${elapsed}s\n`);
    console.log(`📂 输出目录: ${config.outDir}\n`);
  } catch (err) {
    console.error('\n❌ 构建失败:', err);
    process.exit(1);
  }
}

// 监听模式
async function watch() {
  console.log('👀 监听模式已启动...\n');

  // 首次构建
  await build();

  // 监听文件变化
  const chokidar = require('chokidar');
  const watcher = chokidar.watch(config.srcDir, {
    ignored: /node_modules/,
    persistent: true
  });

  let debounceTimer;
  watcher.on('change', (filePath) => {
    console.log(`📝 文件变化: ${path.relative(config.srcDir, filePath)}`);
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(build, 300);
  });
}

// 执行
if (config.watch) {
  watch();
} else {
  build();
}

