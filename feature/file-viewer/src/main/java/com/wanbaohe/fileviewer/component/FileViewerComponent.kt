package com.wanbaohe.fileviewer.component

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.webkit.WebView
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.core.R as CoreR
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.settings.domain.SettingsProvider
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.fileviewer.data.ViewerPageListener
import com.wanbaohe.fileviewer.data.ViewerPreferences
import com.wanbaohe.fileviewer.data.ViewerRequest
import com.wanbaohe.fileviewer.data.ViewerWebHost
import com.wanbaohe.fileviewer.domain.DocumentSource
import com.wanbaohe.fileviewer.domain.ViewerFormat
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 文件预览页的 Decompose 组件。
 *
 * 它负责：把 URI 解析成"用哪个页面 + 名字 + 大小"，决定要不要拒开，
 * 以及把页面回报的页码/查找计数翻译成 UI 状态。WebView 本身归 Compose
 * 的 `AndroidView` 持有——组件被 retain 而 View 已 detach 的话必然泄漏。
 *
 * 阅读能力（页码/跳页/夜间/查找）都建立在页面侧的 message port 上：
 * 端口由 [ViewerWebHost] 在 `onPageFinished` 打开，命令发不出去时静默丢弃，
 * 所以老 WebView 或页面不支持时界面只是少几个控件，不会报错。
 */
class FileViewerComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val uri: Uri?,
    @Assisted private val mimeType: String?,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    private val source: DocumentSource,
    private val preferences: ViewerPreferences,
    settingsProvider: SettingsProvider,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext), ViewerPageListener {

    private val _uiState = MutableStateFlow(
        FileViewerUiState(
            isNight = settingsProvider.settingsState.value.nightMode.resolveNight(),
        )
    )
    val uiState = _uiState.asStateFlow()

    private val webHost = ViewerWebHost(source, this)
    private var request: ViewerRequest? = null
    private var documentKey: String? = null

    /** 上次读到的页；第一次收到页面回报时用掉一次，之后按用户实际滚动保存。 */
    private var pendingPageRestore: Int? = null
    private var restoreConsumed = false

    init {
        // 全局日夜是唯一的夜间来源：设置一变，月亮图标和打开着的文档一起变
        settingsProvider.settingsState
            .onEach { settings ->
                val night = settings.nightMode.resolveNight()
                if (_uiState.value.isNight != night) {
                    _uiState.value = _uiState.value.copy(isNight = night)
                    applyNightMode(night)
                }
            }
            .launchIn(componentScope)

        componentScope.launch {
            val resolved = resolve()
            if (resolved == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = FileViewerError.CannotOpen,
                )
                return@launch
            }

            val target = resolved.request
            val format = ViewerFormat.fromExtension(target.extension)
            val tooLarge = ViewerFormat.loadsWholeFile(format) && target.length > MAX_INLINE_BYTES

            // 读不到的也把名字和大小填上：顶栏与卡片都要说清楚是哪个文件打不开
            _uiState.value = _uiState.value.copy(
                displayName = target.displayName,
                extension = target.extension,
                format = format,
                sizeBytes = target.length,
                isLoading = false,
                error = when {
                    !resolved.readable -> FileViewerError.CannotOpen
                    tooLarge -> FileViewerError.TooLarge(target.length)
                    else -> null
                },
            )
            if (!resolved.readable) return@launch

            val key = ViewerPreferences.keyFor(target.uri, target.length)
            documentKey = key
            val saved = preferences.position(key)
            pendingPageRestore = saved?.toIntOrNull()

            request = target.copy(
                // 夜间跟全局主题走：只有支持独立夜间模式的页面（Word）才传 night
                nightMode = _uiState.value.isNight && format.supportsNight,
                // 电子书的进度是页面自己认的字符串，加载时直接给它
                bookPosition = saved?.takeIf { format == ViewerFormat.EPUB },
                // 预览页的外壳文案一律交给页面翻译（顶栏仍用 App 自己的）
                uiLabels = uiLabelsFor(format),
            )
        }
    }

    /**
     * 给 Compose 的 `AndroidView` 用；解析失败或文件过大时返回 null。
     *
     * [nightMode] 是 App 当前的全局日夜状态，创建页面时定下初始底色；
     * 之后用户切换主题由 [applyNightMode] 通过 message port 追上去。
     */
    fun createWebView(webViewContext: Context, nightMode: Boolean): WebView? {
        val state = _uiState.value
        if (!state.canRender) return null
        val target = request ?: return null
        val supportsNight = state.format?.supportsNight == true
        // App 的"文字大小"是 Activity 的 fontScale；可重排的阅读页跟，固定版式的文档不跟
        val textZoom = if (state.format?.followsTextSize == true) {
            (webViewContext.resources.configuration.fontScale * 100f).roundToInt()
        } else 100
        return webHost.create(
            webViewContext,
            target.copy(
                // Word 有自己的夜间矩阵；其余页面交给 WebView 算法暗化
                nightMode = nightMode && supportsNight,
                forceDark = nightMode && !supportsNight,
                textZoomPercent = textZoom,
            ),
        )
    }

    /** Compose 的 `onRelease` 里调；之后不再向页面发命令。 */
    fun release() = webHost.release()

    // ── 阅读位置 ─────────────────────────────────────────────────────────

    override fun onPage(page: Int, total: Int) {
        val state = _uiState.value
        if (!state.canRender) return

        if (!restoreConsumed) {
            restoreConsumed = true
            val target = pendingPageRestore
            pendingPageRestore = null
            if (target != null && target in 2..total) {
                // 先按记忆的页码亮着，页面跳过去之后再回报真实页
                _uiState.value = state.copy(page = target, pageCount = total)
                documentKey?.let { preferences.savePosition(it, target.toString()) }
                webHost.goToPage(target)
                return
            }
        }

        _uiState.value = state.copy(page = page, pageCount = total)
        if (page in 1..total) {
            documentKey?.let { preferences.savePosition(it, page.toString()) }
        }
    }

    override fun onBookPosition(value: String) {
        documentKey?.let { preferences.savePosition(it, value) }
    }

    // ── 查找 ─────────────────────────────────────────────────────────────

    override fun onSearchCount(at: Int, total: Int, done: Boolean) {
        _uiState.value = _uiState.value.copy(
            search = _uiState.value.search.copy(at = at, total = total, done = done),
        )
    }

    fun openSearch() {
        _uiState.value = _uiState.value.copy(
            search = _uiState.value.search.copy(isOpen = true),
        )
        // 查找条是浮层，页面要让出这块高度，否则第一行字被压在条下面
        webHost.setTopInset(SEARCH_BAR_HEIGHT_DP)
    }

    fun closeSearch() {
        webHost.clearFind()
        webHost.setTopInset(0)
        _uiState.value = _uiState.value.copy(
            search = SearchUiState(isOpen = false),
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            search = _uiState.value.search.copy(query = query, at = 0, total = 0),
        )
        webHost.find(query)
    }

    fun findNext() = webHost.findNext()

    fun findPrevious() = webHost.findPrevious()

    // ── 夜间 / 跳页 ──────────────────────────────────────────────────────

    /**
     * 全局日夜模式变化后，把还开着的文档页一起跟过去。
     *
     * 支持独立夜间的页面（Word）走 `i1/i0`；其余页面没有自己的深色样式，
     * 交给 WebView 的算法暗化（见 [ViewerWebHost.applyForceDark]）。
     */
    fun applyNightMode(isNight: Boolean) {
        val format = _uiState.value.format ?: return
        if (format.supportsNight) {
            webHost.setNightMode(isNight)
        } else {
            webHost.applyForceDark(isNight)
        }
    }

    fun goToPage(page: Int) {
        val total = _uiState.value.pageCount
        val target = if (total > 0) page.coerceIn(1, total) else page.coerceAtLeast(1)
        if (target <= 0) return
        _uiState.value = _uiState.value.copy(page = target)
        documentKey?.let { preferences.savePosition(it, target.toString()) }
        webHost.goToPage(target)
    }

    /** 内部打不开时的出口：交给系统 chooser，和文件浏览器里点"打开方式"一致。 */
    fun openWithOtherApp() {
        val target = uri ?: return
        runCatching {
            val type = mimeType ?: source.mimeType(target, _uiState.value.extension)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(target, type)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    /** 全局设置 + 系统配置 → 当前是不是深色。 */
    private fun NightMode.resolveNight(): Boolean = when (this) {
        NightMode.Dark -> true
        NightMode.Light -> false
        NightMode.System ->
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * [request] 是页面要的东西，[readable] 为 false 说明这份文件现在读不到
     * （被移走、被撤销授权，或者共享存储不给权限），不要交给 WebView。
     */
    private data class Resolved(val request: ViewerRequest, val readable: Boolean)

    private suspend fun resolve(): Resolved? {
        val target = uri ?: return null
        val name = source.displayName(target) ?: target.lastPathSegment ?: return null
        val extension = name.substringAfterLast('.', "")
            .lowercase()
            .ifBlank { extensionFromMime(mimeType) }
        val format = ViewerFormat.fromExtension(extension)

        /*
         * 读不到的文件不进 WebView。
         *
         * 页面自己能报的只有一句英文 "Could not open this spreadsheet"，说不出是权限、
         * 是被删了，还是该换个应用；而且空页面比错误卡片更像卡死。这里先探一次：
         * 打不开就交给 App 自己的错误卡片，那里有文件名、有说明、有出口。
         * 探针也把 SAF 那条后路算进去（见 ContentResolverDocumentSource）。
         */
        val readable = kotlinx.coroutines.withContext(ioDispatcher) {
            source.open(target)?.use { true } ?: false
        }
        val length = kotlinx.coroutines.withContext(ioDispatcher) { source.length(target) }

        return Resolved(
            request = ViewerRequest(
                uri = target,
                page = format.page,
                webViewFloor = format.webViewFloor,
                extension = extension,
                displayName = name,
                length = length,
            ),
            readable = readable,
        )
    }

    /**
     * 电子书阅读器(assets/viewer/epub.html + epub.js)外壳的英文原文 → 当前语言。
     *
     * key 必须和页面里写死的英文逐字一致（epub.html 的按钮/弹窗、epub.js 运行时
     * 塞进去的 "Copied" 和错误标题）；页面对不上时原样显示英文，不会出乱码。
     * 文案改了要同步改页面，否则只是退回英文。
     */
    private fun epubUiLabels(): Map<String, String> = mapOf(
        "Opening book…" to context.getString(CoreR.string.viewer_epub_opening),
        "Contents" to context.getString(CoreR.string.viewer_epub_contents),
        "Close" to context.getString(CoreR.string.viewer_epub_close),
        "Previous" to context.getString(CoreR.string.viewer_epub_previous),
        "Next" to context.getString(CoreR.string.viewer_epub_next),
        "This link leaves the book" to context.getString(CoreR.string.viewer_epub_link_leaves),
        "Web addresses can't be opened here. Copy it to open it in a browser." to
                context.getString(CoreR.string.viewer_epub_link_note),
        "Copy address" to context.getString(CoreR.string.viewer_epub_copy_address),
        "Copied" to context.getString(CoreR.string.viewer_epub_copied),
        "Could not open this book" to context.getString(CoreR.string.viewer_epub_open_failed),
        "This book is locked" to context.getString(CoreR.string.viewer_epub_locked),
        "This part of the book could not be read." to
                context.getString(CoreR.string.viewer_epub_part_unreadable),
    )

    /**
     * 页面外壳文案：加载中、各类"打不开"、不认识格式的卡片。
     *
     * key 是页面里写死的英文原文，页面侧 i18n.js 逐字匹配（匹配不上就原样显示英文，
     * 页面升级后失配也不会坏）。演示文稿本身的文字不在其中，那些属于文档内容。
     */
    private fun viewerUiLabels(): Map<String, String> = mapOf(
        "Rendering document…" to context.getString(CoreR.string.viewer_status_rendering_document),
        "Rendering…" to context.getString(CoreR.string.viewer_status_rendering),
        "Rendering slides…" to context.getString(CoreR.string.viewer_status_rendering_slides),
        "Loading…" to context.getString(CoreR.string.viewer_status_loading),
        "Loading spreadsheet…" to context.getString(CoreR.string.viewer_status_loading_spreadsheet),
        "Loading presentation…" to context.getString(CoreR.string.viewer_presentation_loading),
        "Could not render this Word document" to context.getString(CoreR.string.viewer_error_word),
        "Could not render this Markdown file" to context.getString(CoreR.string.viewer_error_markdown),
        "Could not display this image" to context.getString(CoreR.string.viewer_error_image),
        "Could not open this document" to context.getString(CoreR.string.viewer_error_document),
        "Could not read this file" to context.getString(CoreR.string.viewer_error_file),
        "Could not read the rest of this file" to context.getString(CoreR.string.viewer_error_file_rest),
        "Could not open this spreadsheet" to context.getString(CoreR.string.viewer_error_spreadsheet),
        "Could not render this presentation" to context.getString(CoreR.string.viewer_error_slides),
        "Could not open this presentation" to context.getString(CoreR.string.viewer_presentation_error),
        "Something went wrong while rendering" to context.getString(CoreR.string.viewer_error_generic),
        "This .pptx may use features the built-in renderer does not understand yet." to
                context.getString(CoreR.string.viewer_error_slides_detail),
        "Could not read the file" to context.getString(CoreR.string.viewer_error_read),
        "This file can't be previewed here" to context.getString(CoreR.string.viewer_unsupported_title),
        "View as text" to context.getString(CoreR.string.viewer_unsupported_as_text),
        "If it isn't text, you'll see random characters." to
                context.getString(CoreR.string.viewer_unsupported_caveat),
    )

    /** 按格式拼对照表：公共外壳 + 该页面自己的文案。 */
    private fun uiLabelsFor(format: ViewerFormat): Map<String, String> = buildMap {
        putAll(viewerUiLabels())
        when (format) {
            ViewerFormat.EPUB -> putAll(epubUiLabels())
            ViewerFormat.PPT -> putAll(pptUiLabels())
            else -> Unit
        }
    }

    /**
     * 旧版 PPT 大纲页自己的文案：说明卡片与页码标签。
     *
     * key 必须和 ppt.js 里写死的英文逐字一致（那里用 window.vwT 取译文），对不上就
     * 原样显示英文，不会出乱码。
     */
    private fun pptUiLabels(): Map<String, String> = mapOf(
        "Legacy PowerPoint file (.ppt)" to context.getString(CoreR.string.viewer_ppt_legacy_title),
        "This format keeps its slides as binary drawing records, which this viewer does not draw. " +
                "The text of the deck is shown below, slide by slide. " +
                "Re-save it as .pptx to see the slides themselves." to
                context.getString(CoreR.string.viewer_ppt_legacy_note),
        "SLIDE" to context.getString(CoreR.string.viewer_ppt_slide_label),
    )

    private fun extensionFromMime(mime: String?): String = when (mime?.lowercase()) {
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "xlsx"
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "pptx"
        "application/msword" -> "doc"
        "application/vnd.ms-excel" -> "xls"
        "application/vnd.ms-powerpoint" -> "ppt"
        "application/vnd.oasis.opendocument.presentation" -> "odp"
        "application/vnd.oasis.opendocument.text" -> "odt"
        "application/vnd.oasis.opendocument.spreadsheet" -> "ods"
        "application/rtf" -> "rtf"
        "application/epub+zip" -> "epub"
        else -> ""
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            uri: Uri?,
            mimeType: String?,
            onGoBack: () -> Unit,
        ): FileViewerComponent
    }

    private companion object {
        /**
         * docx / xlsx / pptx / epub 都是"整包读进 JS 内存"的，只有 PDF 走 range。
         * 64MB 是给低端机留的余量；再大就让用户换应用，好过 OOM 闪退。
         */
        const val MAX_INLINE_BYTES = 64L * 1024 * 1024

        /** 查找条（TextField + 内边距）大约占的高度，跟 FileViewerScreen 里的一致 */
        const val SEARCH_BAR_HEIGHT_DP = 64
    }
}
