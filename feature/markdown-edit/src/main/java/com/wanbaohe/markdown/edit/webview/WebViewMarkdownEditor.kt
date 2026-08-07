package com.wanbaohe.markdown.edit.webview

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import com.shifenmiao.model.colors.EditorColors
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.common.WebViewSettings
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.ui.widget.text.EditorUiDefaults
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.wanbaohe.markdown.edit.R as MarkdownR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * 基于 WebView + Milkdown 的 Markdown 编辑器
 *
 * @param initialValue 初始 Markdown 文本内容
 * @param state 编辑器状态，用于获取内容和清除草稿
 * @param modifier 修饰符
 * @param placeholder 占位符文本
 * @param readOnly 是否只读
 * @param enabled 是否启用
 * @param storageKey 用于区分不同页面的 localStorage 缓存 key
 * @param textStyle 文本样式，默认使用统一编辑器正文样式。将会映射为 WebView 内部的 CSS (支持 fontSize, lineHeight, letterSpacing, fontWeight)
 * @param onContentChanged 内容变化回调（用于更新 isDirty 状态）
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun WebViewMarkdownEditor(
    initialValue: String = "",
    state: WebViewMarkdownEditorState = rememberWebViewMarkdownEditorState(),
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(MarkdownR.string.markdown_placeholder),
    readOnly: Boolean = false,
    @Suppress("UNUSED_PARAMETER") enabled: Boolean = true,
    storageKey: String = "default",
    textStyle: androidx.compose.ui.text.TextStyle? = null,
    onContentChanged: (() -> Unit)? = null,
    onVerticalScrollDelta: ((Float) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()
    val resolvedTextStyle = textStyle ?: EditorUiDefaults.contentTextStyle()
    val context = LocalContext.current

    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var isEditorReady by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var hasInitialized by remember { mutableStateOf(false) }

    // 图片缩放弹窗状态
    var showImageResizeDialog by remember { mutableStateOf(false) }
    var imageResizeIndex by remember { mutableIntStateOf(-1) }
    var imageCurrentWidth by remember { mutableIntStateOf(0) }
    var imageMaxWidth by remember { mutableIntStateOf(0) }
    var imageNaturalWidth by remember { mutableIntStateOf(0) }

    // 插入图片对话框状态
    var showInsertImageDialog by remember { mutableStateOf(false) }

    // 插入链接对话框状态
    var showInsertLinkDialog by remember { mutableStateOf(false) }
    var linkDialogSelectedText by remember { mutableStateOf("") }

    // 获取系统字体缩放比例（使用 LocalConfiguration 以响应配置变化）
    val configuration = LocalConfiguration.current
    val fontScale = configuration.fontScale
    // 字体大小：原始 sp 值 * fontScale = 实际显示大小

    // 提取并应用 TextStyle 参数
    val baseFontSizeSp = resolvedTextStyle.fontSize.value
    val fontSizeSp = baseFontSizeSp * fontScale
    val lineHeightSp = if (resolvedTextStyle.lineHeight.isSp) resolvedTextStyle.lineHeight.value else (baseFontSizeSp * 1.5f)
    val letterSpacingSp = if (resolvedTextStyle.letterSpacing.isSp) resolvedTextStyle.letterSpacing.value else 0f
    val fontWeight = resolvedTextStyle.fontWeight?.weight ?: 400

    // 同步 WebView 到 state
    LaunchedEffect(webView) {
        state.webView = webView
    }

    // Material 3 颜色
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

    // 背景色 ARGB
    val bgColorArgb = backgroundColor.toArgb()

    // 生成颜色配置
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

    // 图片选择器
    val imagePicker = rememberImagePicker { uri: Uri ->
        // 把相册返回的 content/media URI 转成 file:// URL,markdown 文件拷贝/分享时图片路径更稳定
        val finalUri = SafUriUtils.toFileUri(context, uri) ?: uri
        webView?.evaluateJavascript(
            "if (window.AndroidBridge && window.AndroidBridge.insertImage) { window.AndroidBridge.insertImage('${finalUri}', ''); }",
            null
        )
    }

    // 创建 JavaScript Bridge
    val bridge = remember(onContentChanged, onVerticalScrollDelta) {
        MarkdownEditorBridge(
            context = context,
            onContentChanged = { _ ->
                // 通知外部内容已变化
                onContentChanged?.invoke()
            },
            onEditorReady = {
                scope.launch(Dispatchers.Main) {
                    isEditorReady = true
                    isLoading = false
                }
            },
            onPickImage = {
                scope.launch(Dispatchers.Main) {
                    imagePicker.pickImage()
                }
            },
            onShowImageResizeDialog = { index, currentWidth, maxWidth, naturalWidth ->
                scope.launch(Dispatchers.Main) {
                    imageResizeIndex = index
                    imageCurrentWidth = currentWidth
                    imageMaxWidth = maxWidth
                    imageNaturalWidth = naturalWidth
                    showImageResizeDialog = true
                }
            },
            onShowImageDialog = {
                scope.launch(Dispatchers.Main) {
                    showInsertImageDialog = true
                }
            },
            onShowLinkDialog = { selectedText ->
                scope.launch(Dispatchers.Main) {
                    linkDialogSelectedText = selectedText
                    showInsertLinkDialog = true
                }
            },
            onEditorScroll = { _, deltaY ->
                scope.launch(Dispatchers.Main) {
                    onVerticalScrollDelta?.invoke(deltaY.toFloat())
                }
            }
        )
    }

    // 生成颜色 JSON（用于动态更新主题）
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

    // 编辑器就绪后设置初始内容（只执行一次）
    LaunchedEffect(isEditorReady) {
        if (isEditorReady && webView != null && !hasInitialized) {
            hasInitialized = true

            // 设置初始内容，如果为空则会从 localStorage 加载草稿
            // storage key 已在 HTML 模板中通过 window.MILKDOWN_STORAGE_KEY 设置
            val escapedValue = initialValue
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
            webView?.evaluateJavascript(
                "if (window.AndroidBridge && window.AndroidBridge.setContent) { window.AndroidBridge.setContent(\"$escapedValue\"); }",
                null
            )
        }
    }

    // 设置主题（动态切换时）
    LaunchedEffect(isEditorReady, isDarkTheme) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript(
                "if (window.AndroidBridge && window.AndroidBridge.setTheme) { window.AndroidBridge.setTheme($isDarkTheme); }",
                null
            )
            webView?.evaluateJavascript(
                "if (window.AndroidBridge && window.AndroidBridge.setColors) { window.AndroidBridge.setColors('${generateColorsJson()}'); }",
                null
            )
        }
    }

    // 设置占位符
    LaunchedEffect(isEditorReady, placeholder) {
        if (isEditorReady && webView != null) {
            val escapedPlaceholder = placeholder
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
            webView?.evaluateJavascript(
                "if (window.AndroidBridge && window.AndroidBridge.setPlaceholder) { window.AndroidBridge.setPlaceholder(\"$escapedPlaceholder\"); }",
                null
            )
        }
    }

    // 设置只读模式
    LaunchedEffect(isEditorReady, readOnly) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript(
                "if (window.AndroidBridge && window.AndroidBridge.setReadOnly) { window.AndroidBridge.setReadOnly($readOnly); }",
                null
            )
        }
    }

    // 设置字体样式（响应系统字体缩放及外部 TextStyle 更改）
    LaunchedEffect(isEditorReady, fontSizeSp, lineHeightSp, letterSpacingSp, fontWeight) {
        if (isEditorReady && webView != null) {
            webView?.evaluateJavascript(
                "if (window.AndroidBridge && window.AndroidBridge.setTextStyle) { window.AndroidBridge.setTextStyle($fontSizeSp, $lineHeightSp, $letterSpacingSp, $fontWeight); }",
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
                // 创建 WebViewAssetLoader 用于加载本地资源
                val assetLoader = WebViewAssetLoader.Builder()
                    .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(ctx))
                    .build()

                // 每次创建全新 WebView，不复用旧实例
                val wv = WebViewPool.create(ctx, bgColorArgb).apply {
                    // 使用统一的完整配置
                    WebViewSettings.applyCommonSettings(this, context)
                    // 设置 AssetLoader
                    webViewClient = object : WebViewClient() {
                        override fun shouldInterceptRequest(
                            view: WebView,
                            request: WebResourceRequest
                        ): WebResourceResponse? {
                            return assetLoader.shouldInterceptRequest(request.url)
                        }
                    }
                }


                // 添加 Bridge（必须在加载 HTML 之前添加，确保 JS 能调用回调）
                wv.addJavascriptInterface(bridge, MarkdownEditorBridge.BRIDGE_NAME)


                // 构建当前配置（用于检查是否可以使用预拼接的 HTML）
                val currentConfig = MarkdownPreloadConfig(
                    isDarkTheme = isDarkTheme,
                    colors = editorColors,
                    storageKey = storageKey,
                    fontSizeSp = fontSizeSp,
                    lineHeightSp = lineHeightSp,
                    letterSpacingSp = letterSpacingSp,
                    fontWeight = fontWeight
                )

                // 优先使用预拼接的 HTML，否则生成新的
                val html = MarkdownWebViewPoolHelper.getPreloadedHtml(currentConfig)
                    ?: MarkdownEditorHtmlTemplate.generate(
                        isDarkTheme = isDarkTheme,
                        colors = editorColors,
                        storageKey = storageKey,
                        fontSizeSp = fontSizeSp,
                        lineHeightSp = lineHeightSp,
                        letterSpacingSp = letterSpacingSp,
                        fontWeight = fontWeight
                    )

                // 使用 appassets URL 作为 baseURL，支持按需加载本地资源
                wv.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/assets/milkdown-editor/",
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
                // 更新背景色
                view.setBackgroundColor(bgColorArgb)
            }
        )

        // Loading 指示器
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = primaryColor
                )
            }
        }
    }

    // 图片缩放弹窗
    if (showImageResizeDialog) {
        ImageResizeDialog(
            currentWidth = imageCurrentWidth,
            maxWidth = imageMaxWidth,
            naturalWidth = imageNaturalWidth,
            onDismiss = { showImageResizeDialog = false },
            onConfirm = { newWidth ->
                webView?.evaluateJavascript(
                    "if (window.AndroidBridge && window.AndroidBridge.setImageWidth) { window.AndroidBridge.setImageWidth($imageResizeIndex, $newWidth); }",
                    null
                )
                showImageResizeDialog = false
            },
            onReset = {
                webView?.evaluateJavascript(
                    "if (window.AndroidBridge && window.AndroidBridge.setImageWidth) { window.AndroidBridge.setImageWidth($imageResizeIndex, 0); }",
                    null
                )
                showImageResizeDialog = false
            },
            onPreview = { previewWidth ->
                // 实时预览：只更新 DOM 显示，不保存到文档模型
                webView?.evaluateJavascript(
                    "if (window.AndroidBridge && window.AndroidBridge.previewImageWidth) { window.AndroidBridge.previewImageWidth($imageResizeIndex, $previewWidth); }",
                    null
                )
            }
        )
    }

    // 插入图片对话框
    if (showInsertImageDialog) {
        InsertImageDialog(
            onDismiss = { showInsertImageDialog = false },
            onPickFromGallery = {
                showInsertImageDialog = false
                imagePicker.pickImage()
            },
            onInsertUrl = { url, alt ->
                val escapedUrl = url.replace("'", "\\'")
                val escapedAlt = alt.replace("'", "\\'")
                webView?.evaluateJavascript(
                    "if (window.AndroidBridge && window.AndroidBridge.insertImage) { window.AndroidBridge.insertImage('$escapedUrl', '$escapedAlt'); }",
                    null
                )
                showInsertImageDialog = false
            }
        )
    }

    // 插入链接对话框
    if (showInsertLinkDialog) {
        InsertLinkDialog(
            initialText = linkDialogSelectedText,
            onDismiss = { showInsertLinkDialog = false },
            onInsert = { text, url ->
                val escapedText = text.replace("'", "\\'")
                val escapedUrl = url.replace("'", "\\'")
                webView?.evaluateJavascript(
                    "if (window.AndroidBridge && window.AndroidBridge.insertLink) { window.AndroidBridge.insertLink('$escapedText', '$escapedUrl'); }",
                    null
                )
                showInsertLinkDialog = false
            }
        )
    }

    // 清理
    DisposableEffect(Unit) {
        onDispose {
            webView?.let { wv ->
                // 销毁 WebView（停止加载、移除 Bridge、清除历史、销毁实例）
                WebViewPool.destroyWebView(wv, MarkdownEditorBridge.BRIDGE_NAME)
            }
            webView = null
        }
    }
}


/**
 * 将 ARGB 颜色转换为 HEX 字符串
 */
private fun colorToHex(color: Int): String {
    return String.format("#%06X", 0xFFFFFF and color)
}
