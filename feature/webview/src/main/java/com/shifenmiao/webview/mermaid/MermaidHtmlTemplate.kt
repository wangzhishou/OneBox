package com.shifenmiao.webview.mermaid

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

/**
 * Mermaid 图表 HTML 模版生成器
 *
 * 生成自包含的 HTML 文档，通过 WebView 加载 mermaid.min.js 渲染流程图、
 * 时序图等各类 Mermaid 图表。
 *
 * mermaid.min.js 通过 WebViewAssetLoader 从本地 assets 目录加载：
 * `https://appassets.androidplatform.net/assets/js/mermaid.min.js`
 *
 * 渲染完成后通过 `window.Android.onRendered(height)` 回调通知原生层实际高度，
 * 以便 Compose 动态调整 WebView 尺寸。
 */
object MermaidHtmlTemplate {

    /**
     * 生成 Mermaid 渲染 HTML
     *
     * @param code    Mermaid 源码（如 `sequenceDiagram\n  participant A ...`）
     * @param isDark  是否暗色主题
     * @param colors  Material3 ColorScheme，用于注入 CSS 变量使图表融入当前主题
     * @return 完整 HTML 字符串，可直接通过 `loadDataWithBaseURL` 加载
     */
    fun generate(
        code: String,
        isDark: Boolean,
        colors: ColorScheme
    ): String {
        fun Int.toHex() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)
        fun androidx.compose.ui.graphics.Color.toHex() = toArgb().toHex()

        val mermaidTheme = if (isDark) "dark" else "default"
        val escapedCode = escapeHtml(code.trim())

        return """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * {
            margin: 0; padding: 0; box-sizing: border-box;
            -webkit-user-select: none;
            user-select: none;
            -webkit-touch-callout: none;
        }
        html, body {
            width: 100%;
            background: ${colors.surface.toHex()};
            overflow: hidden;
        }
        ::-webkit-scrollbar {
            display: none;
        }
        .mermaid-container {
            display: flex;
            justify-content: center;
            align-items: flex-start;
            width: 100%;
            padding: 8px 4px;
        }
        .mermaid {
            width: 100%;
        }
        .mermaid svg {
            max-width: 100%;
            height: auto !important;
        }
        /* 错误提示样式 */
        .error-container {
            padding: 16px;
            color: ${colors.error.toHex()};
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 14px;
            word-break: break-word;
        }
    </style>
</head>
<body>
    <div class="mermaid-container">
        <pre class="mermaid">
$escapedCode
        </pre>
    </div>

    <div class="error-container" id="errorBox" style="display:none;"></div>

    <script>
        // Compatibility for older Android WebView missing some modern built-ins.
        if (typeof Object.hasOwn !== 'function') {
            Object.hasOwn = function(obj, prop) {
                return Object.prototype.hasOwnProperty.call(Object(obj), prop);
            };
        }

        if (typeof Array.prototype.at !== 'function') {
            Object.defineProperty(Array.prototype, 'at', {
                value: function(index) {
                    var len = this.length >>> 0;
                    var relative = Number(index) || 0;
                    var k = relative < 0 ? len + relative : relative;
                    return (k < 0 || k >= len) ? undefined : this[k];
                },
                writable: true,
                configurable: true
            });
        }
    </script>
    <script src="https://appassets.androidplatform.net/assets/js/mermaid.min.js"></script>
    <script>
        // 禁止长按弹出菜单
        document.addEventListener('contextmenu', function(e) { e.preventDefault(); return false; });
        document.addEventListener('long-press', function(e) { e.preventDefault(); return false; });

        function reportRenderError(err) {
            var message = (err && (err.message || err.toString())) || 'Unknown error';
            var errorBox = document.getElementById('errorBox');
            if (errorBox) {
                errorBox.style.display = 'block';
                errorBox.textContent = 'Mermaid 渲染失败: ' + message;
            }
            var container = document.querySelector('.mermaid-container');
            if (container) container.style.display = 'none';

            if (window.Android && window.Android.onError) {
                window.Android.onError(message);
            }
        }

        /**
         * 为独立 SVG 渲染（AndroidSVG / Coil SvgDecoder）做准备。
         *
         * 核心问题：Mermaid SVG 中 <g> 父元素常设 fill:none（避免形状默认黑色填充），
         * 子 <text> 继承此值后文字不可见。浏览器内的 CSS <style> 能覆盖，但 AndroidSVG
         * 对复合/后代选择器（.node .nodeLabel 等）支持有限，导致 override 失效。
         *
         * 策略：
         * 1. 利用浏览器完整 CSS 引擎（getComputedStyle），将 fill、font-* 等
         *    关键属性写入每个 <text>/<tspan> 的 SVG presentation attribute，
         *    作为 AndroidSVG 解析失败时的可靠 fallback。
         * 2. 将 <foreignObject>（AndroidSVG 不支持）转换为 <text> 元素。
         * 3. 保留原始 <style> 和 class，不做破坏性删除。
         * 4. 确保 xmlns 命名空间声明。
         */
        function prepareSvgForNativeRendering(svgElement) {

            // ── 1. 文字元素：内联 getComputedStyle 计算的关键属性 ──
            var textEls = svgElement.querySelectorAll('text, tspan');
            for (var i = 0; i < textEls.length; i++) {
                var el = textEls[i];
                try {
                    var cs = window.getComputedStyle(el);

                    // fill — 文字可见性的关键
                    if (!el.getAttribute('fill')) {
                        var fill = cs.getPropertyValue('fill');
                        if (!fill || fill === 'none' || fill === '') {
                            // 部分浏览器对 SVG text 的 fill 返回空，尝试 color
                            fill = cs.getPropertyValue('color');
                        }
                        if (fill && fill !== 'none' && fill !== '') {
                            el.setAttribute('fill', fill);
                        }
                    }

                    // font-size
                    if (!el.getAttribute('font-size')) {
                        var fs = cs.getPropertyValue('font-size');
                        if (fs) el.setAttribute('font-size', fs);
                    }

                    // font-family
                    if (!el.getAttribute('font-family')) {
                        var ff = cs.getPropertyValue('font-family');
                        if (ff) el.setAttribute('font-family', ff);
                    }

                    // font-weight
                    if (!el.getAttribute('font-weight')) {
                        var fw = cs.getPropertyValue('font-weight');
                        if (fw && fw !== '400' && fw !== 'normal') {
                            el.setAttribute('font-weight', fw);
                        }
                    }

                    // text-anchor
                    if (!el.getAttribute('text-anchor')) {
                        var ta = cs.getPropertyValue('text-anchor');
                        if (ta && ta !== 'start') {
                            el.setAttribute('text-anchor', ta);
                        }
                    }

                    // dominant-baseline
                    if (!el.getAttribute('dominant-baseline')) {
                        var db = cs.getPropertyValue('dominant-baseline');
                        if (db && db !== 'auto' && db !== 'baseline') {
                            el.setAttribute('dominant-baseline', db);
                        }
                    }
                } catch(e) { /* getComputedStyle 偶尔失败，跳过 */ }
            }

            // ── 2. foreignObject → text（AndroidSVG 不支持 foreignObject）──
            var foreignObjects = svgElement.querySelectorAll('foreignObject');
            for (var f = 0; f < foreignObjects.length; f++) {
                var fo = foreignObjects[f];
                var textContent = (fo.textContent || '').trim();
                if (!textContent) continue;

                // 读取 foreignObject 尺寸，文字居中放置
                var foW = parseFloat(fo.getAttribute('width')) || 100;
                var foH = parseFloat(fo.getAttribute('height')) || 20;

                // 尝试从 HTML 内容读取颜色
                var foColor = '';
                try {
                    var span = fo.querySelector('span, div, p');
                    if (span) {
                        var spanCs = window.getComputedStyle(span);
                        foColor = spanCs.getPropertyValue('color') || '';
                    }
                } catch(e) {}

                var newText = document.createElementNS('http://www.w3.org/2000/svg', 'text');
                newText.setAttribute('x', String(foW / 2));
                newText.setAttribute('y', String(foH / 2));
                newText.setAttribute('text-anchor', 'middle');
                newText.setAttribute('dominant-baseline', 'central');
                newText.setAttribute('font-size', '14px');
                newText.setAttribute('font-family', '-apple-system, BlinkMacSystemFont, sans-serif');
                if (foColor) newText.setAttribute('fill', foColor);
                newText.textContent = textContent;

                fo.parentNode.replaceChild(newText, fo);
            }

            // ── 3. 确保 xmlns 命名空间 ──
            if (!svgElement.getAttribute('xmlns')) {
                svgElement.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
            }
        }

        try {
            if (typeof mermaid === 'undefined' || !mermaid) {
                throw new Error('mermaid library not loaded');
            }

            mermaid.initialize({
                startOnLoad: true,
                theme: '$mermaidTheme',
                securityLevel: 'loose',
                fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
                sequence: {
                    actorFontSize: 13,
                    messageFontSize: 13,
                    noteFontSize: 12,
                    useMaxWidth: true,
                    htmlLabels: false
                },
                flowchart: {
                    useMaxWidth: true,
                    htmlLabels: false
                },
                class: {
                    htmlLabels: false
                },
                state: {
                    htmlLabels: false
                },
                er: {
                    htmlLabels: false
                },
                themeVariables: {
                    primaryColor: '${colors.primaryContainer.toHex()}',
                    primaryTextColor: '${colors.onPrimaryContainer.toHex()}',
                    primaryBorderColor: '${colors.outline.toHex()}',
                    lineColor: '${colors.onSurfaceVariant.toHex()}',
                    secondaryColor: '${colors.secondaryContainer.toHex()}',
                    tertiaryColor: '${colors.tertiaryContainer.toHex()}',
                    background: '${colors.surface.toHex()}',
                    mainBkg: '${colors.surfaceContainerHigh.toHex()}',
                    nodeBorder: '${colors.outline.toHex()}',
                    clusterBkg: '${colors.surfaceContainer.toHex()}',
                    titleColor: '${colors.onSurface.toHex()}',
                    edgeLabelBackground: '${colors.surfaceContainerHighest.toHex()}',
                    actorTextColor: '${colors.onSurface.toHex()}',
                    actorBkg: '${colors.surfaceContainerHigh.toHex()}',
                    actorBorder: '${colors.outlineVariant.toHex()}',
                    signalColor: '${colors.onSurface.toHex()}',
                    noteBkgColor: '${colors.tertiaryContainer.toHex()}',
                    noteTextColor: '${colors.onTertiaryContainer.toHex()}',
                    noteBorderColor: '${colors.outline.toHex()}'
                }
            });

            mermaid.run().then(function() {
                // 渲染完成，提取 SVG 字符串并报告给原生层
                setTimeout(function() {
                    try {
                        var svg = document.querySelector('.mermaid svg');
                        if (!svg) return;
                        // 为原生 SVG 渲染准备：内联文字属性 + 转换 foreignObject
                        prepareSvgForNativeRendering(svg);
                        var rect = svg.getBoundingClientRect();
                        var height = Math.ceil(rect.height) + 16;
                        var svgString = svg.outerHTML;
                        if (window.Android && window.Android.onSvgReady) {
                            window.Android.onSvgReady(svgString, height);
                        }
                    } catch(e) {
                        console.error('Mermaid SVG extraction failed:', e);
                        // 回退：仅报告高度
                        try {
                            var svg = document.querySelector('.mermaid svg');
                            if (svg && window.Android && window.Android.onRendered) {
                                var rect = svg.getBoundingClientRect();
                                window.Android.onRendered(Math.ceil(rect.height) + 16);
                            }
                        } catch(e2) {}
                    }
                }, 150);
            }).catch(function(err) {
                reportRenderError(err);
            });
        } catch (err) {
            reportRenderError(err);
        }
    </script>
</body>
</html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return buildString(text.length) {
            for (ch in text) {
                when (ch) {
                    '&' -> append("&amp;")
                    '<' -> append("&lt;")
                    '>' -> append("&gt;")
                    '"' -> append("&quot;")
                    '\'' -> append("&#39;")
                    else -> append(ch)
                }
            }
        }
    }
}

