package com.shifenmiao.webview

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.theme.AppTheme
import java.util.Locale

object MarkdownRenderHtmlTemplate {
    fun wrap(
        title: String,
        bodyHtml: String,
        colors: ColorScheme = AppTheme.colorScheme,
        fontSizeSp: Float = 16f
    ): String {
        fun Int.toHex() = String.format(Locale.US, "#%06X", this and 0xFFFFFF)
        fun androidx.compose.ui.graphics.Color.toHex() = toArgb().toHex()

        return """
        <!DOCTYPE html>
        <html lang="zh-CN">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>${escapeHtml(title)}</title>
            <script src="/js/tailwindcss.js"></script>
            <style>
                :root {
                    color-scheme: light dark;

                    --md-primary: ${colors.primary.toHex()};
                    --md-on-primary: ${colors.onPrimary.toHex()};
                    --md-primary-container: ${colors.primaryContainer.toHex()};
                    --md-on-primary-container: ${colors.onPrimaryContainer.toHex()};
                    --md-secondary: ${colors.secondary.toHex()};
                    --md-on-secondary: ${colors.onSecondary.toHex()};
                    --md-secondary-container: ${colors.secondaryContainer.toHex()};
                    --md-on-secondary-container: ${colors.onSecondaryContainer.toHex()};
                    --md-surface: ${colors.surface.toHex()};
                    --md-on-surface: ${colors.onSurface.toHex()};
                    --md-surface-variant: ${colors.surfaceVariant.toHex()};
                    --md-on-surface-variant: ${colors.onSurfaceVariant.toHex()};
                    --md-outline: ${colors.outline.toHex()};
                    --md-outline-variant: ${colors.outlineVariant.toHex()};
                    --md-background: ${colors.background.toHex()};
                    --md-on-background: ${colors.onBackground.toHex()};
                    --md-error: ${colors.error.toHex()};
                    --md-on-error: ${colors.onError.toHex()};
                    --md-font-size: ${fontSizeSp}px;
                }

                body {
                    margin: 0;
                    padding: 16px;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    font-size: var(--md-font-size);
                    line-height: 1.6;
                    word-wrap: break-word;
                    overflow-wrap: break-word;
                    background: var(--md-background);
                    color: var(--md-on-background);
                }

                h1, h2, h3, h4, h5, h6 {
                    margin: 1.2em 0 0.6em;
                    line-height: 1.25;
                }

                p { margin: 0.9em 0; }

                a { color: inherit; text-decoration: underline; }

                ul, ol { padding-left: 1.5em; margin: 0.9em 0; }
                li { margin: 0.3em 0; }

                blockquote {
                    margin: 1em 0;
                    padding: 0.5em 1em;
                    border-left: 4px solid var(--md-outline-variant);
                    background: var(--md-surface-variant);
                    color: var(--md-on-surface-variant);
                }

                pre {
                    padding: 12px 14px;
                    border-radius: 8px;
                    background: var(--md-surface-variant);
                    overflow-x: auto;
                }

                code {
                    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', monospace;
                    font-size: 0.95em;
                }

                p code, li code {
                    padding: 0.15em 0.35em;
                    border-radius: 4px;
                    background: var(--md-surface-variant);
                }

                img {
                    max-width: 100%;
                    height: auto;
                }

                table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 1em 0;
                    display: block;
                    overflow-x: auto;
                    -webkit-overflow-scrolling: touch;
                }

                th, td {
                    border: 1px solid var(--md-outline-variant);
                    padding: 8px 10px;
                    text-align: left;
                    vertical-align: top;
                }

                th {
                    background: var(--md-surface-variant);
                    font-weight: 600;
                }
            </style>
        </head>
        <body>
            $bodyHtml
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
