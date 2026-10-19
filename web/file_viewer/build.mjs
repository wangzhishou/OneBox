/**
 * 文件预览页构建脚本
 *
 * 源码在 web/file_viewer/src/，压缩后的产物写进
 * feature/file-viewer/src/main/assets/viewer/（同一个文件名，不改 HTML 里的引用）。
 *
 * 为什么不打包成一个 bundle：这些页面是 gander 的经典脚本，彼此靠顶层全局函数
 * 通信（app.js 定义 vwStatus，docx.js 调用它）。esbuild 一 bundle 就会把顶层
 * 名字收进闭包，页面立刻坏。所以逐文件压缩，并且关掉标识符重命名。
 *
 * 用法：
 *   node file_viewer/build.mjs            # 构建
 *   node file_viewer/build.mjs --check    # 只比较产物是否最新（不改文件）
 */

import esbuild from 'esbuild'
import { minify as minifyHtml } from 'html-minifier-terser'
import fs from 'node:fs'
import path from 'node:path'
import url from 'node:url'

const here = path.dirname(url.fileURLToPath(import.meta.url))
const srcDir = path.join(here, 'src')
const outDir = path.join(here, '../../feature/file-viewer/src/main/assets/viewer')

const checkOnly = process.argv.includes('--check')

const htmlOptions = {
  collapseWhitespace: true,
  removeComments: true,
  removeRedundantAttributes: true,
  removeScriptTypeAttributes: true,
  removeStyleLinkTypeAttributes: true,
  useShortDoctype: true,
  // 页面里没有内联 <script>；JS 由下面的 esbuild 单独压缩
  minifyJS: false,
  minifyCSS: true,
}

const prettier = (bytes) => `${(bytes / 1024).toFixed(1)} KB`

async function transformFile(name) {
  const source = fs.readFileSync(path.join(srcDir, name), 'utf8')

  if (name.endsWith('.js')) {
    const result = await esbuild.transform(source, {
      loader: 'js',
      // 不重命名标识符：跨文件的全局函数/变量靠名字通信
      minifyIdentifiers: false,
      minifyWhitespace: true,
      minifySyntax: true,
      target: ['es2018'],
      legalComments: 'none',
    })
    return result.code
  }

  if (name.endsWith('.css')) {
    const result = await esbuild.transform(source, { loader: 'css', minify: true })
    return result.code
  }

  if (name.endsWith('.html')) {
    return await minifyHtml(source, htmlOptions)
  }

  return source
}

async function main() {
  if (!fs.existsSync(srcDir)) {
    console.error(`✗ 找不到源码目录：${srcDir}`)
    process.exit(1)
  }
  if (!checkOnly) fs.mkdirSync(outDir, { recursive: true })

  const files = fs.readdirSync(srcDir).filter((name) => !name.startsWith('.')).sort()
  let before = 0
  let after = 0
  let stale = 0

  for (const name of files) {
    const srcPath = path.join(srcDir, name)
    const outPath = path.join(outDir, name)
    const source = fs.readFileSync(srcPath)
    before += source.length

    const output = await transformFile(name)
    after += Buffer.byteLength(output)

    if (checkOnly) {
      const current = fs.existsSync(outPath) ? fs.readFileSync(outPath, 'utf8') : ''
      if (current !== output) {
        stale++
        console.log(`   ✗ ${name} 需要重新构建`)
      }
      continue
    }

    fs.writeFileSync(outPath, output)
    console.log(`   ✓ ${name}  ${prettier(source.length)} → ${prettier(Buffer.byteLength(output))}`)
  }

  if (checkOnly) {
    if (stale > 0) {
      console.error(`\n✗ ${stale} 个产物过期，请运行 npm run build:viewer`)
      process.exit(1)
    }
    console.log('\n✓ 所有产物都是最新的')
    return
  }

  console.log(`\n✓ ${files.length} 个文件，${prettier(before)} → ${prettier(after)}`)
  console.log(`  输出：${path.relative(process.cwd(), outDir)}`)
  console.log('  注意：viewer/lib/ 是第三方 dist，不参与构建，保持原样')
}

main().catch((error) => {
  console.error(error)
  process.exit(1)
})
