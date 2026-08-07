package com.shifenmiao.webview

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.ImageUtils
import com.shifenmiao.common.file.AigcFileMetadataWriter
import com.shifenmiao.common.file.AigcMetadata
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.wechat.Wechat
import com.shifenmiao.model.wechat.common.ImageObject
import com.shifenmiao.model.wechat.common.MediaMessage
import com.shifenmiao.model.wechat.common.WXScene
import com.shifenmiao.webview.client.CustomWebChromeClient
import com.shifenmiao.webview.client.CustomWebViewClient
import com.shifenmiao.webview.common.WebViewPool
import com.shifenmiao.webview.resource.WebResourceEngine
import com.shifenmiao.webview.utils.WebViewExportUtils
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.domain.image.ImageShareProvider
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.ImageInfo
import com.t8rin.imagetoolbox.core.domain.image.model.Preset
import com.t8rin.imagetoolbox.core.domain.resource.ResourceManager
import com.t8rin.imagetoolbox.core.domain.saving.FileController
import com.t8rin.imagetoolbox.core.domain.saving.model.SaveResult
import com.t8rin.imagetoolbox.core.domain.utils.runSuspendCatching
import com.t8rin.imagetoolbox.core.domain.utils.smartJob
import com.t8rin.imagetoolbox.core.domain.utils.timestamp
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.t8rin.imagetoolbox.core.ui.utils.state.update
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.PdfManager
import com.t8rin.imagetoolbox.feature.pdf_tools.domain.model.PdfCreationParams
import com.t8rin.logger.makeLog
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class WebViewComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val currentWebViewParams: WebViewParams?,
    @Assisted val onGoBack: () -> Unit,
    resourceManager: ResourceManager,
    dispatchersHolder: DispatchersHolder,
    private val pdfManager: PdfManager,
    private val fileController: FileController,
    private val imageGetter: ImageGetter<Bitmap>,
    private val shareProvider: ImageShareProvider<Bitmap>,
    @ApplicationContext val applicationContext: Context,
    private val webResourceEngine: WebResourceEngine,
) : BaseComponent(dispatchersHolder, componentContext), ResourceManager by resourceManager {

    // 当前持有的 WebView 弱引用（资源缓存由 [WebResourceEngine] 管理）
    private var cachedWebView: WeakReference<WebView>? = null

    // 修改状态初始化，支持状态恢复
    private val _webViewState = MutableStateFlow(WebViewState())
    val webViewState: StateFlow<WebViewState> = _webViewState.asStateFlow()

    // 当前WebView参数
    private val _webViewParams = MutableStateFlow<WebViewParams?>(currentWebViewParams)
    val webViewParams: StateFlow<WebViewParams?> = _webViewParams.asStateFlow()

    private val _isSaving: MutableState<Boolean> = mutableStateOf(false)
    val isSaving: Boolean by _isSaving

    private val _outputPdfUri = mutableStateOf<String?>(null)

    private var savingJob: Job? by smartJob {
        _isSaving.update { false }
    }

    init {
        componentContext.lifecycle.doOnCreate {

        }

        componentContext.lifecycle.doOnDestroy {
            clearWebView()
        }
    }

    fun setWebViewParams(params: WebViewParams?) {
        _webViewParams.value = params
    }

    // 设置当前WebView引用
    fun setWebView(webView: WebView?) {
        webView?.let {
            cachedWebView = WeakReference(webView)
        }
        load()
    }

    // 更新标题
    fun updateTitle(newTitle: String) {
        if (newTitle.isNotEmpty()) {
            _webViewState.update { it.copy(currentTitle = newTitle) }
        }
    }

    // 更新导航状态
    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _webViewState.update {
            it.copy(canGoBack = canGoBack, canGoForward = canGoForward)
        }
    }

    // 更新加载错误状态
    fun updateErrorState(isError: Boolean, errorMessage: String = "", errorDetails: String = "") {
        _webViewState.update {
            it.copy(isError = isError, errorMessage = errorMessage, errorDetails = errorDetails)
        }
    }

    fun load() {
        cachedWebView?.get()?.let { view ->
            val currentUrl = view.url
            val params = webViewParams.value
            val targetUrl = params?.url
            if (((currentUrl == null) || (currentUrl == UrlConstants.ABOUT_BLANK)) && (targetUrl?.isNotEmpty() == true)) {
                loadUrl(targetUrl)
            } else if ((params?.htmlData != null) && ((currentUrl == null) || (currentUrl == UrlConstants.ABOUT_BLANK))) {
                loadHtml(
                    params.baseUrl,
                    params.htmlData!!,
                    params.mimeType,
                    "utf-8"
                )
            }
        }
    }

    // 在WebView中加载URL
    fun loadUrl(url: String) {
        componentScope.launch {
            getCurrentWebView()?.loadUrl(url)
            // 重置错误状态
            updateErrorState(false)
        }
    }

    // 在WebView中加载HTML内容
    fun loadHtml(
        baseUrl: String? = null,
        html: String,
        mimeType: String? = "text/html",
        encoding: String? = "utf-8"
    ) {
        componentScope.launch {
            getCurrentWebView()?.loadDataWithBaseURL(
                baseUrl,
                html,
                mimeType,
                encoding,
                null
            )
            // 重置错误状态
            updateErrorState(false)
        }
    }

    fun getCurrentWebView(): WebView? {
        return cachedWebView?.get()
    }

    // 导航后退
    fun goBack() {
        getCurrentWebView()?.let { webView ->
            if (webView.canGoBack()) {
                webView.goBack()
                updateNavigationState(webView.canGoBack(), webView.canGoForward())
                // 重置错误状态
                updateErrorState(false)
            } else {
                clearWebView()
                onGoBack()
            }
        }
    }

    // 导航前进
    fun goForward() {
        getCurrentWebView()?.let { webView ->
            if (webView.canGoForward()) {
                webView.goForward()
                updateNavigationState(webView.canGoBack(), webView.canGoForward())
                // 重置错误状态
                updateErrorState(false)
            }
        }
    }

    // 重新加载当前页面
    fun reload() {
        getCurrentWebView()?.reload()
        // 重置错误状态
        updateErrorState(false)
    }

    // 在系统浏览器中打开当前页面
    fun openInBrowser() {
        val url = getCurrentWebView()?.url ?: webViewParams.value?.url
        if (!url.isNullOrEmpty()) {
            ActionUtils.openWebBrowser(applicationContext, url)
        }
    }

    // 停止加载
    fun stopLoading() {
        getCurrentWebView()?.stopLoading()
    }

    // 获取自定义WebViewClient
    fun getWebViewClient(
        onShouldOverrideUrlLoading: ((String) -> Boolean)? = null
    ): WebViewClient {
        val ignoreSslError = webViewParams.value?.ignoreSslError == true
        return CustomWebViewClient(
            context = applicationContext,
            engine = webResourceEngine,
            ignoreSslError = ignoreSslError,
            onShouldOverrideUrlLoading = onShouldOverrideUrlLoading,
            onFinished = { view, _ ->
                _webViewState.update {
                    it.copy(
                        isLoading = false,
                        canGoBack = view?.canGoBack() ?: false,
                        canGoForward = view?.canGoForward() ?: false
                    )
                }
                view?.title?.let { title -> updateTitle(title) }
            },
            onStarted = { _, _, _ ->
                _webViewState.update { it.copy(isLoading = true) }
                // 在开始加载时重置错误状态
                updateErrorState(false)
            },
            onError = { _, _, errorMessage, errorDetails ->
                updateErrorState(true, errorMessage, errorDetails)
            }
        )
    }

    fun getWebChromeClient(): WebChromeClient {
        return CustomWebChromeClient(
            onProgress = { _, newProgress ->
                _webViewState.update { state ->
                    state.copy(
                        isLoading = newProgress < 100,
                        loadingProgress = newProgress / 100f
                    )
                }
            }
        )
    }

    // 显示PDF导出对话框
    fun showPdfExportDialog(localActivity: ComponentActivity) {
        getCurrentWebView()?.let { webView ->
            WebViewExportUtils.exportToPdf(
                webView,
                localActivity,
                getExportFileName()
            ) { success, currentUri ->

            }
        }
    }

    fun savePngFile(
        fileUri: Uri,
        pngBytes: ByteArray? = null,
        onResult: (SaveResult) -> Unit
    ) {
        _isSaving.value = true
        savingJob = componentScope.launch {
            val bytes = pngBytes ?: run {
                val localBitmap = captureBitmapOrFail(onResult) ?: return@launch
                ImageUtils.bitmapToByteArray(localBitmap)
            }

            val writeResult = fileController.writeBytes(
                uri = fileUri.toString(),
                block = {
                    it.writeBytes(bytes)
                }
            )
            val aIgcInfo = webViewParams.value?.aIgcInfo
            try {
                AigcFileMetadataWriter.writeImageDescriptionIfPresent(
                    imageUri = fileUri.toString(),
                    aigc = aIgcInfo?.takeIf { it.isNotBlank() }?.let(::AigcMetadata),
                    imageGetter = imageGetter,
                    fileController = fileController
                )
            } catch (e: Exception) {
                e.makeLog("WebViewComponent")
            }
            _isSaving.value = false
            onResult(writeResult)
            writeResult.onSuccess(::registerSave)
        }
    }

    /**
     * 先把 WebView 当前内容截图并编码为 PNG 字节数组，再回调给调用方。
     *
     * 用于“先截图、后弹文件选择器”的场景：选择器弹出期间 WebView 即使被回收，
     * 也不会影响已捕获到的图片数据。
     */
    fun capturePngBytes(onResult: (ByteArray?) -> Unit) {
        val webView = getCurrentWebView()
        if (webView == null) {
            Toast.makeText(
                applicationContext,
                getString(R.string.webview_share_unavailable),
                Toast.LENGTH_SHORT
            ).show()
            onResult(null)
            return
        }
        _isSaving.value = true
        savingJob = componentScope.launch {
            val bytes = runSuspendCatching {
                val bitmap = withContext(uiDispatcher) {
                    WebViewExportUtils.captureWebViewFullPageBitmap(webView)
                }
                ImageUtils.bitmapToByteArray(bitmap).also { bitmap.recycle() }
            }.onFailure { it.makeLog("WebViewComponent") }.getOrNull()

            _isSaving.value = false
            if (bytes == null) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.webview_share_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onResult(bytes)
        }
    }

    fun savePdfFile(fileUri: Uri, onResult: (SaveResult) -> Unit) {
        _isSaving.value = true
        savingJob = componentScope.launch {
            val bitmap = captureBitmapOrFail(onResult) ?: return@launch

            val pdfGenerationResult = runSuspendCatching {
                pdfManager.createPdf(
                    imageUris = listOf(
                        WebViewExportUtils.saveBitmapToTempFile(
                            applicationContext,
                            bitmap
                        )
                    ),
                    params = PdfCreationParams(
                        scaleSmallImagesToLarge = false,
                        preset = Preset.Percentage(100)
                    )
                )
            }.onFailure { it.makeLog("PdfToolsComponent") }.getOrNull()

            _outputPdfUri.value = pdfGenerationResult

            pdfGenerationResult?.let { pdfUri ->
                val aIgcInfo = webViewParams.value?.aIgcInfo
                val saveResult = runSuspendCatching {
                    if (!aIgcInfo.isNullOrBlank()) {
                        AigcFileMetadataWriter.transferPdfWithAigcInfo(
                            fromPdfUri = pdfUri,
                            toPdfUri = fileUri.toString(),
                            aigc = AigcMetadata(aIgcInfo),
                            fileController = fileController
                        )
                    } else {
                        fileController.transferBytes(
                            fromUri = pdfUri,
                            toUri = fileUri.toString()
                        )
                    }
                }.getOrElse {
                    it.makeLog("WebViewComponent")
                    componentScope.launch(Dispatchers.Main) {
                        Toast.makeText(applicationContext, "AIGC info write failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                    fileController.transferBytes(
                        fromUri = pdfUri,
                        toUri = fileUri.toString()
                    )
                }
                saveResult.also(onResult).onSuccess(::registerSave)
            } ?: run {
                onResult(SaveResult.Error.Exception(Exception("PDF generation failed")))
            }

            _isSaving.value = false
        }
    }

    /**
     * 先把 WebView 当前内容截图、生成带 AIGC 元数据的 PNG 临时文件。
     *
     * 用于“先准备文件、后弹文件选择器”的场景：即使选择器弹出让 App 进入后台、
     * WebView 被回收，回来时也能继续把已准备好的临时文件复制到目标位置。
     */
    fun capturePngToTempFile(onResult: (String?) -> Unit) {
        capturePngBytes { bytes ->
            if (bytes == null) {
                onResult(null)
                return@capturePngBytes
            }
            componentScope.launch {
                val tempUri = runCatching {
                    val tempFile = File(
                        applicationContext.cacheDir,
                        "webview_export/png_${UUID.randomUUID()}.png"
                    ).apply { parentFile?.mkdirs() }
                    val tempFileUri = Uri.fromFile(tempFile).toString()

                    val writeResult = fileController.writeBytes(tempFileUri, "") {
                        it.writeBytes(bytes)
                    }
                    if (writeResult !is SaveResult.Success) {
                        throw IllegalStateException("PNG temp file write failed")
                    }

                    val aIgcInfo = webViewParams.value?.aIgcInfo
                    if (!aIgcInfo.isNullOrBlank()) {
                        runCatching {
                            AigcFileMetadataWriter.writeImageDescriptionIfPresent(
                                imageUri = tempFileUri,
                                aigc = AigcMetadata(aIgcInfo),
                                imageGetter = imageGetter,
                                fileController = fileController
                            )
                        }.onFailure { it.makeLog("WebViewComponent") }
                    }
                    tempFileUri
                }.getOrElse {
                    it.makeLog("WebViewComponent")
                    showSaveFailedToast()
                    null
                }
                onResult(tempUri)
            }
        }
    }

    /**
     * 先把 WebView 当前内容生成带 AIGC 元数据的 PDF 临时文件。
     */
    fun preparePdfToTempFile(onResult: (String?) -> Unit) {
        _isSaving.value = true
        savingJob = componentScope.launch {
            // WebView 截图必须在主线程执行
            val bitmap = withContext(uiDispatcher) {
                captureBitmapOrFail { result ->
                    _isSaving.value = false
                    if (result is SaveResult.Error) {
                        showSaveFailedToast()
                        onResult(null)
                    }
                }
            } ?: return@launch

            val tempUri = runCatching {
                val sourcePdfUri = runSuspendCatching {
                    pdfManager.createPdf(
                        imageUris = listOf(
                            WebViewExportUtils.saveBitmapToTempFile(applicationContext, bitmap)
                        ),
                        params = PdfCreationParams(
                            scaleSmallImagesToLarge = false,
                            preset = Preset.Percentage(100)
                        )
                    )
                }.onFailure { it.makeLog("WebViewComponent") }.getOrNull()
                    ?: throw IllegalStateException("PDF generation failed")

                val tempFile = File(
                    applicationContext.cacheDir,
                    "webview_export/pdf_${UUID.randomUUID()}.pdf"
                ).apply { parentFile?.mkdirs() }
                val uri = Uri.fromFile(tempFile).toString()

                val aIgcInfo = webViewParams.value?.aIgcInfo
                val writeResult = runCatching {
                    if (!aIgcInfo.isNullOrBlank()) {
                        AigcFileMetadataWriter.transferPdfWithAigcInfo(
                            fromPdfUri = sourcePdfUri,
                            toPdfUri = uri,
                            aigc = AigcMetadata(aIgcInfo),
                            fileController = fileController
                        )
                    } else {
                        fileController.transferBytes(sourcePdfUri, uri)
                    }
                }.getOrElse {
                    it.makeLog("WebViewComponent")
                    fileController.transferBytes(sourcePdfUri, uri)
                }

                if (writeResult !is SaveResult.Success) {
                    throw IllegalStateException("PDF temp file write failed")
                }
                uri
            }.getOrElse {
                it.makeLog("WebViewComponent")
                showSaveFailedToast()
                null
            }

            _isSaving.value = false
            onResult(tempUri)
        }
    }

    /**
     * 将临时文件复制到用户选择的目标 URI。
     */
    private fun showSaveFailedToast() {
        Toast.makeText(
            applicationContext,
            getString(R.string.webview_share_failed),
            Toast.LENGTH_SHORT
        ).show()
    }

    fun copyTempFileToUri(
        tempUri: String,
        targetUri: Uri,
        onResult: (SaveResult) -> Unit
    ) {
        _isSaving.value = true
        savingJob = componentScope.launch {
            val result = runSuspendCatching {
                fileController.transferBytes(tempUri, targetUri.toString())
            }.getOrElse {
                it.makeLog("WebViewComponent")
                SaveResult.Error.Exception(it)
            }
            _isSaving.value = false
            result.also(onResult).onSuccess(::registerSave)
        }
    }

    private suspend fun captureBitmapOrFail(onResult: (SaveResult) -> Unit): Bitmap? {
        val webView = getCurrentWebView()
        if (webView == null) {
            _isSaving.value = false
            onResult(SaveResult.Error.Exception(IllegalStateException("WebView 已被销毁")))
            return null
        }
        val captureResult = runSuspendCatching {
            WebViewExportUtils.captureWebViewFullPageBitmap(webView)
        }
        val bitmap = captureResult.getOrNull()
        if (bitmap == null) {
            captureResult.exceptionOrNull()?.makeLog("WebViewComponent")
            _isSaving.value = false
            onResult(
                SaveResult.Error.Exception(
                    captureResult.exceptionOrNull() ?: RuntimeException("WebView 截图失败")
                )
            )
        }
        return bitmap
    }

    fun generatePdfFilename(): String {
        val timeStamp = "${timestamp()}_${Random(Random.nextInt()).hashCode().toString().take(4)}"
        return "PDF_$timeStamp.pdf"
    }

    fun cancelSaving() {
        savingJob?.cancel()
        savingJob = null
        _isSaving.value = false
    }

    // 获取导出文件名（带时间戳和正确的扩展名）
    fun getExportFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val rawTitle = webViewParams.value?.title?.takeIf { it.isNotBlank() }
            ?: getCurrentWebView()?.title?.takeIf { it.isNotBlank() }
        val sanitizedTitle = rawTitle
            ?.take(20)
            ?.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            ?.replace("^(about|file|data):.*$".toRegex(RegexOption.IGNORE_CASE), "")
            ?.replace("^about_?blank$".toRegex(RegexOption.IGNORE_CASE), "")
            ?.trim('_', ' ')
            ?.takeIf { it.isNotEmpty() }
        return if (sanitizedTitle != null) {
            "${sanitizedTitle}_$timestamp"
        } else {
            "Web_$timestamp"
        }
    }

    // 复制网页内容到剪贴板
    fun copyWebContent(context: Context, copyToClipboard: (String) -> Unit) {
        getCurrentWebView()?.let { webView ->
            webView.url?.let {
                if (it.isNotEmpty()) {
                    copyToClipboard(it)
                } else {
                    Toast.makeText(context, "没有可复制的内容", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(context, "没有可复制的内容", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // WebView状态数据类
    @Serializable
    data class WebViewState(
        val currentTitle: String = "",
        val isLoading: Boolean = false,
        val loadingProgress: Float = 0f,
        val canGoBack: Boolean = false,
        val canGoForward: Boolean = false,
        val isError: Boolean = false,
        val errorMessage: String = "",
        val errorDetails: String = ""
    )

    /**
     * 分享当前 WebView 的 HTML 内容作为文件。
     *
     * 若 [webViewParams] 中包含 htmlData，则直接将其写入临时 .html 文件并通过系统分享组件发送；
     * 否则回退到截图分享（[share]）。
     */
    fun shareHtml(onComplete: () -> Unit = {}) {
        val htmlData = webViewParams.value?.htmlData
        if (htmlData.isNullOrBlank()) {
            share(onComplete)
            return
        }
        _isSaving.value = true
        savingJob = componentScope.launch {
            // 分享 HTML 文件时，在文件头部也注入 AIGC 信息（优先紧跟在 DOCTYPE 之后）。
            val htmlWithAigcHeader = buildHtmlWithAigcHeader(
                htmlData = htmlData,
                aIgcInfo = webViewParams.value?.aIgcInfo
            )
            val shareResult = runSuspendCatching {
                shareProvider.shareData(
                    writeData = { it.writeBytes(htmlWithAigcHeader.toByteArray(Charsets.UTF_8)) },
                    filename = getExportFileName() + ".html",
                    onComplete = {}
                )
            }.onFailure { it.makeLog("WebViewComponent") }
            _isSaving.value = false
            if (shareResult.isFailure) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.webview_share_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onComplete()
        }
    }

    /**
     * 在 HTML 头部注入 AIGC 隐式标识。
     *
     * 按 GB 45438-2025 标准,**字段名是 AIGC**,值是 7 要素 JSON。
     * 实现为 HTML `<head>` 中的 `<meta name="AIGC" content="...">` 标签。
     * 如果原始 HTML 不包含 `<head>`,则尽量在 `<!DOCTYPE html>` 之后插入 `<head>` 块。
     */
    private fun buildHtmlWithAigcHeader(htmlData: String, aIgcInfo: String?): String {
        if (aIgcInfo.isNullOrBlank()) return htmlData
        // JSON 内的引号需要转义成 &quot;,避免破坏 HTML 属性
        val escaped = aIgcInfo
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
        val metaTag = "<meta name=\"AIGC\" content=\"$escaped\">"

        val headOpenRegex = "(?i)(<head\\b[^>]*>)".toRegex()
        val headMatch = headOpenRegex.find(htmlData)
        if (headMatch != null) {
            val insertAt = headMatch.range.endInclusive + 1
            return htmlData.substring(0, insertAt) + "\n" + metaTag + htmlData.substring(insertAt)
        }
        val doctypeRegex = "(?i)(<!DOCTYPE\\s+html\\s*>)".toRegex()
        val doctypeMatch = doctypeRegex.find(htmlData)
        if (doctypeMatch != null) {
            val insertAt = doctypeMatch.range.endInclusive + 1
            val syntheticHead = "\n<head>\n$metaTag\n</head>\n"
            return htmlData.substring(0, insertAt) + syntheticHead + htmlData.substring(insertAt)
        }
        return "<head>\n$metaTag\n</head>\n" + htmlData
    }

    fun share(onComplete: () -> Unit = {}) {
        val webView = getCurrentWebView()
        if (webView == null) {
            Toast.makeText(
                applicationContext,
                getString(R.string.webview_share_unavailable),
                Toast.LENGTH_SHORT
            ).show()
            onComplete()
            return
        }
        _isSaving.value = true
        savingJob = componentScope.launch {
            val shareResult = runSuspendCatching {
                val bitmap = withContext(uiDispatcher) {
                    WebViewExportUtils.captureWebViewFullPageBitmap(webView)
                }
                shareImage(bitmap)
            }.onFailure { it.makeLog("WebViewComponent") }
            _isSaving.value = false
            if (shareResult.getOrDefault(false).not()) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.webview_share_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            onComplete()
        }
    }

    private suspend fun shareImage(bitmap: Bitmap): Boolean {
        val imageInfo = ImageInfo(
            width = bitmap.width,
            height = bitmap.height,
            imageFormat = ImageFormat.Png.Lossless
        )
        return try {
            val cachedUri = shareProvider.cacheImage(
                image = bitmap,
                imageInfo = imageInfo,
                filename = getExportFileName() + ".png"
            ) ?: return false

            // 分享图片同样要注入 AIGC 隐式标识，保证通过系统分享出去的图片也携带元数据。
            val aIgcInfo = webViewParams.value?.aIgcInfo
            if (!aIgcInfo.isNullOrBlank()) {
                runCatching {
                    AigcFileMetadataWriter.writeImageDescriptionIfPresent(
                        imageUri = cachedUri,
                        aigc = AigcMetadata(aIgcInfo),
                        imageGetter = imageGetter,
                        fileController = fileController
                    )
                }.onFailure { it.makeLog("WebViewComponent") }
            }

            shareProvider.shareUri(
                uri = cachedUri,
                type = imageInfo.imageFormat.mimeType,
                onComplete = {}
            )
            true
        } finally {
            bitmap.recycle()
        }
    }

    fun clearWebView() {
        cachedWebView?.get()?.let { webView ->
            // 统一销毁 WebView：停止加载、移除视图、清除历史、销毁实例
            WebViewPool.destroyWebView(webView)
        }
        cachedWebView = null
        _webViewState.update {
            it.copy(
                currentTitle = "",
                isLoading = false,
                loadingProgress = 0f,
                canGoBack = false,
                canGoForward = false,
                isError = false,
                errorMessage = "",
                errorDetails = ""
            )
        }
    }

    fun shareToWechat(
        title: String = applicationContext.getString(R.string.share_title),
        scene: WXScene = WXScene.Session
    ) {
        if (!Wechat.isEnabled) return
        getCurrentWebView()?.let { webView ->
            componentScope.launch {
                runCatching {
                    WebViewExportUtils.captureWebViewFullPageBitmap(webView)
                }.onSuccess { bitmap ->
                    Wechat.share(
                        mediaMessage = MediaMessage(
                            title = title,
                            description = "",
                            thumbData = null,
                            mediaObject = ImageObject(
                                imageData = ImageUtils.bitmapToByteArray(bitmap)
                            ),
                        ),
                        scene = scene
                    )
                }.onFailure { it.makeLog("WebViewComponent") }
            }
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            webViewParams: WebViewParams?
        ): WebViewComponent
    }
}
