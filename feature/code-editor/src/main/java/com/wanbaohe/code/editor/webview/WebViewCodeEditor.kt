package com.wanbaohe.code.editor.webview

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.shifenmiao.model.colors.EditorColors
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.common.WebViewSettings
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults
import org.json.JSONObject

/**
 * WebView + CodeMirror 6 代码编辑器
 *
 * @param initialValue 初始内容
 * @param state 编辑器状态
 * @param modifier 修饰符
 * @param placeholder 占位符
 * @param language 初始语言（plaintext/javascript/json/...）
 * @param readOnly 是否只读
 * @param storageKey localStorage 草稿 key
 * @param textStyle 文本样式，默认 monospaced
 * @param onContentChanged 内容变化回调
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebViewCodeEditor(
    initialValue: String = "",
    state: WebViewCodeEditorState = rememberWebViewCodeEditorState(),
    modifier: Modifier = Modifier,
    placeholder: String = "在此输入代码…",
    language: String = "plaintext",
    readOnly: Boolean = false,
    storageKey: String = "default",
    textStyle: androidx.compose.ui.text.TextStyle? = null,
    onContentChanged: (() -> Unit)? = null
) {
    val isDarkTheme = isSystemInDarkTheme()
    val resolvedTextStyle = textStyle ?: EditorUiDefaults.contentTextStyle(monospaced = true)

    var isLoading by remember { mutableStateOf(true) }
    var isEditorReady by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    // 跟踪已应用过的 initialValue,确保 recompose 传新值时能正确同步
    var lastSyncedInitialValue by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(webView) {
        state.webView = webView
    }

    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale

    val baseFontSizeSp = resolvedTextStyle.fontSize.value
    val fontSizePx = baseFontSizeSp * fontScale
    val lineHeightSp = if (resolvedTextStyle.lineHeight.isSp) resolvedTextStyle.lineHeight.value else (baseFontSizeSp * 1.5f)
    val letterSpacingSp = if (resolvedTextStyle.letterSpacing.isSp) resolvedTextStyle.letterSpacing.value else 0f
    val fontWeight = resolvedTextStyle.fontWeight?.weight ?: 400

    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSecondaryColor = MaterialTheme.colorScheme.onSecondary
    val secondaryContainerColor = MaterialTheme.colorScheme.secondaryContainer
    val onSecondaryContainerColor = MaterialTheme.colorScheme.onSecondaryContainer
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground
    val errorColor = MaterialTheme.colorScheme.error
    val onErrorColor = MaterialTheme.colorScheme.onError
    val bgColorArgb = backgroundColor.toArgb()

    val editorColors = remember(
        primaryColor, onPrimaryColor, primaryContainerColor, onPrimaryContainerColor,
        secondaryColor, onSecondaryColor, secondaryContainerColor, onSecondaryContainerColor,
        surfaceColor, onSurfaceColor, surfaceVariantColor, onSurfaceVariantColor,
        outlineColor, outlineVariantColor, backgroundColor, onBackgroundColor,
        errorColor, onErrorColor
    ) {
        EditorColors.fromArgb(
            primary = primaryColor.toArgb(),
            onPrimary = onPrimaryColor.toArgb(),
            primaryContainer = primaryContainerColor.toArgb(),
            onPrimaryContainer = onPrimaryContainerColor.toArgb(),
            secondary = secondaryColor.toArgb(),
            onSecondary = onSecondaryColor.toArgb(),
            secondaryContainer = secondaryContainerColor.toArgb(),
            onSecondaryContainer = onSecondaryContainerColor.toArgb(),
            surface = surfaceColor.toArgb(),
            onSurface = onSurfaceColor.toArgb(),
            surfaceVariant = surfaceVariantColor.toArgb(),
            onSurfaceVariant = onSurfaceVariantColor.toArgb(),
            outline = outlineColor.toArgb(),
            outlineVariant = outlineVariantColor.toArgb(),
            background = backgroundColor.toArgb(),
            onBackground = onBackgroundColor.toArgb(),
            error = errorColor.toArgb(),
            onError = onErrorColor.toArgb()
        )
    }

    val bridge = remember(onContentChanged) {
        CodeEditorBridge(
            onContentChanged = { _ ->
                onContentChanged?.invoke()
            },
            onEditorReady = {
                isEditorReady = true
                isLoading = false
            },
            onCursorChange = { _, _ -> },
            onSelectionChange = { _, _ -> },
            onEditorScroll = { _, _ -> },
            onPopupStateChanged = { isOpen ->
                state.isPopupOpen = isOpen
            }
        )
    }

    fun generateColorsJson(): String {
        val colors = JSONObject().apply {
            put("primary", colorToHex(primaryColor.toArgb()))
            put("on-primary", colorToHex(onPrimaryColor.toArgb()))
            put("primary-container", colorToHex(primaryContainerColor.toArgb()))
            put("on-primary-container", colorToHex(onPrimaryContainerColor.toArgb()))
            put("secondary", colorToHex(secondaryColor.toArgb()))
            put("on-secondary", colorToHex(onSecondaryColor.toArgb()))
            put("secondary-container", colorToHex(secondaryContainerColor.toArgb()))
            put("on-secondary-container", colorToHex(onSecondaryContainerColor.toArgb()))
            put("surface", colorToHex(surfaceColor.toArgb()))
            put("on-surface", colorToHex(onSurfaceColor.toArgb()))
            put("surface-variant", colorToHex(surfaceVariantColor.toArgb()))
            put("on-surface-variant", colorToHex(onSurfaceVariantColor.toArgb()))
            put("outline", colorToHex(outlineColor.toArgb()))
            put("outline-variant", colorToHex(outlineVariantColor.toArgb()))
            put("background", colorToHex(backgroundColor.toArgb()))
            put("on-background", colorToHex(onBackgroundColor.toArgb()))
            put("error", colorToHex(errorColor.toArgb()))
            put("on-error", colorToHex(onErrorColor.toArgb()))
        }
        return colors.toString()
    }

    LaunchedEffect(isEditorReady) {
        if (isEditorReady && webView != null && lastSyncedInitialValue != initialValue) {
            lastSyncedInitialValue = initialValue
            val escapedValue = escapeJsString(initialValue)
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setContent) { window.CodeEditorBridge.setContent(\"$escapedValue\"); }",
                null
            )
            val escapedLang = escapeJsString(language)
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setLanguage) { window.CodeEditorBridge.setLanguage(\"$escapedLang\"); }",
                null
            )
        }
    }

    LaunchedEffect(isEditorReady, isDarkTheme) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setTheme) { window.CodeEditorBridge.setTheme($isDarkTheme); }",
                null
            )
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setColors) { window.CodeEditorBridge.setColors('${generateColorsJson()}'); }",
                null
            )
        }
    }

    LaunchedEffect(isEditorReady, placeholder) {
        if (isEditorReady && webView != null) {
            val escaped = escapeJsString(placeholder)
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setPlaceholder) { window.CodeEditorBridge.setPlaceholder(\"$escaped\"); }",
                null
            )
        }
    }

    LaunchedEffect(isEditorReady, readOnly) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setReadOnly) { window.CodeEditorBridge.setReadOnly($readOnly); }",
                null
            )
        }
    }

    LaunchedEffect(isEditorReady, fontSizePx, lineHeightSp, letterSpacingSp, fontWeight) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript(
                "if (window.CodeEditorBridge && window.CodeEditorBridge.setTextStyle) { window.CodeEditorBridge.setTextStyle($fontSizePx, $lineHeightSp, $letterSpacingSp, $fontWeight); }",
                null
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .background(backgroundColor)
    ) {
        AndroidView(
            factory = { ctx ->
                val assetLoader = WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                    .build()

                val wv = WebViewPool.create(ctx, bgColorArgb).apply {
                    WebViewSettings.applyCommonSettings(this, ctx)
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            return assetLoader.shouldInterceptRequest(request.url)
                        }
                    }
                }

                wv.addJavascriptInterface(bridge, CodeEditorBridge.BRIDGE_NAME)

                val currentConfig = CodeEditorPreloadConfig(
                    isDarkTheme = isDarkTheme,
                    colors = editorColors,
                    storageKey = storageKey,
                    fontSizePx = fontSizePx,
                    lineHeightPx = lineHeightSp,
                    letterSpacingPx = letterSpacingSp,
                    fontWeight = fontWeight
                )

                val html = CodeEditorWebViewPoolHelper.getPreloadedHtml(currentConfig)
                    ?: CodeEditorHtmlTemplate.generate(
                        isDarkTheme = isDarkTheme,
                        colors = editorColors,
                        storageKey = storageKey,
                        fontSizePx = fontSizePx,
                        lineHeightPx = lineHeightSp,
                        letterSpacingPx = letterSpacingSp,
                        fontWeight = fontWeight
                    )

                wv.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/assets/code-editor/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )

                webView = wv
                wv
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.setBackgroundColor(bgColorArgb)
            }
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webView?.let { wv ->
                WebViewPool.destroyWebView(wv, CodeEditorBridge.BRIDGE_NAME)
            }
            webView = null
            // 直接清理 state.webView,避免依赖 LaunchedEffect(webView) 间接置空
            state.webView = null
        }
    }
}

private fun colorToHex(color: Int): String = String.format("#%06X", 0xFFFFFF and color)
