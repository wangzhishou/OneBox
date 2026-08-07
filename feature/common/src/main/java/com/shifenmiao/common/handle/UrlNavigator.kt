package com.shifenmiao.common.handle

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.runtime.compositionLocalOf
import androidx.core.net.toUri
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.ItemDeeplinkResolver
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.image.ImageViewerInfo
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.webview.WebViewType
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.ui.utils.content.ContentRouter
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen

// 创建一个CompositionLocal来提供UrlNavigator
val LocalUrlNavigator = compositionLocalOf<UrlNavigator> { error("No UrlNavigator provided") }

/**
 * URL导航工具类，处理各种类型URL的导航
 */
class UrlNavigator(
    private val context: Context,
    val onNavigate: (Screen) -> Unit,
    private val contentRouter: ContentRouter? = null,
) {

    /**
     * 处理URL导航
     * @param url 需要导航的URL
     * @return 是否成功处理
     */
    fun navigate(url: String?): Boolean {
        if (url.isNullOrBlank()) return false

        // 特殊处理：onebox://action/open?uri=... 直接走 ContentRouter 做智能路由
        if (contentRouter != null && isOpenActionDeeplink(url)) {
            return handleOpenDeeplink(url, contentRouter)
        }

        val handler = UrlHandlerFactory.getHandler(url, contentRouter)
        return handler.handleUrl(url, context, onNavigate)
    }

    /**
     * 判断是否为 "智能打开" DeepLink：onebox://action/open?uri=...
     *
     * 使用 Uri 精确匹配，避免 startsWith 误匹配 open_anything 等前缀。
     */
    private fun isOpenActionDeeplink(url: String): Boolean {
        return runCatching {
            val uri = url.toUri()
            val scheme = uri.scheme?.lowercase().orEmpty()
            val host = uri.host?.lowercase().orEmpty()
            val firstPath = uri.pathSegments.firstOrNull().orEmpty()
            (scheme == "onebox" || scheme == "app") &&
                host == UrlConstants.DEEP_LINK_HOST_ACTION &&
                firstPath == "open"
        }.getOrDefault(false)
    }

    /**
     * 处理 open action deeplink：提取 uri 参数并委托给 ContentRouter
     */
    private fun handleOpenDeeplink(url: String, contentRouter: ContentRouter): Boolean {
        val uri = runCatching { url.toUri() }.getOrNull() ?: return false
        val uriParam = uri.getQueryParameter("uri")
            ?.let { runCatching { Uri.parse(it) }.getOrNull() }
            ?: return false
        return contentRouter.route(
            uri = uriParam,
            context = context,
            onNavigate = onNavigate,
            allSiblings = emptyList(),
            fallbackToExternal = true
        )
    }

    /**
     * 直接使用Screen对象进行导航
     * @param screen 导航目标Screen
     */
    fun navigate(screen: Screen) {
        onNavigate(screen)
    }
}

/**
 * URL处理接口
 */
interface UrlHandler {
    /**
     * 判断是否能处理该URL
     */
    fun canHandle(url: String): Boolean

    /**
     * 处理URL导航
     */
    fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean
}

/**
 * 处理内部导航URL (app://...)
 */
class AppUrlHandler : UrlHandler {

    override fun canHandle(url: String): Boolean {
        return url.startsWith(UrlConstants.APP_URL_PREFIX)
    }

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        return handleInternalNavigation(url = url, context = context, onNavigate = onNavigate)
    }
}

/**
 * 处理内部导航URL (onebox://...)
 */
class OneBoxUrlHandler : UrlHandler {

    override fun canHandle(url: String): Boolean {
        return url.startsWith(UrlConstants.DEEP_LINKS_PREFIX)
    }

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        return handleInternalNavigation(url = url, context = context, onNavigate = onNavigate)
    }
}

/**
 * 处理外部链接URL (http://, https://)
 */
class WebUrlHandler : UrlHandler {
    override fun canHandle(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        onNavigate(
            Screen.WebView(
                WebViewParams(
                    url = url,
                    type = WebViewType.COLUMN
                )
            )
        )
        return true
    }
}

/**
 * 处理电话拨打URL (tel:)
 */
class TelUrlHandler : UrlHandler {
    override fun canHandle(url: String): Boolean {
        return url.startsWith("tel:")
    }

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        val intent = Intent(Intent.ACTION_DIAL, url.toUri())
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent, null)
            return true
        }
        return false
    }
}

/**
 * URL处理器工厂
 */
object UrlHandlerFactory {
    private val baseHandlers = listOf(
        AppUrlHandler(),
        WebUrlHandler(),
        TelUrlHandler(),
        OneBoxUrlHandler()
    )

    /**
     * 获取能处理该URL的处理器
     */
    fun getHandler(url: String, contentRouter: ContentRouter? = null): UrlHandler {
        return when {
            url.startsWith("dir://") -> DirContentUrlHandler()
            url.startsWith("content://") -> ContentUrlHandler(contentRouter)
            else -> baseHandlers.find { it.canHandle(url) } ?: DefaultUrlHandler()
        }
    }
}

/**
 * 处理Content URI (content://)
 *
 * 优先委托给 [ContentRouter] 做统一路由决策；
 * 当 ContentRouter 不可用时，回退到简单的 ImageViewer / FileBrowser 二分逻辑。
 */
class ContentUrlHandler(
    private val contentRouter: ContentRouter? = null
) : UrlHandler {
    override fun canHandle(url: String): Boolean {
        return url.startsWith("content://")
    }

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        val uri = url.toUri()

        // 优先使用统一内容路由器
        contentRouter?.let { router ->
            return router.route(uri, context, onNavigate, emptyList(), fallbackToExternal = false)
        }

        // Fallback：原有简单逻辑（ContentRouter 不可用时）
        return handleUriLegacy(uri, context, onNavigate)
    }

    private fun handleUriLegacy(uri: Uri, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        return try {
            val accessibleUri = SafUriUtils.documentUriToFileUri(uri) ?: uri
            val mimeType = context.contentResolver.getType(accessibleUri)
                ?: MimeTypeMap.getSingleton()
                    .getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(accessibleUri.toString())
                            ?: ""
                    )

            when {
                mimeType?.startsWith("image/") == true -> {
                    onNavigate(
                        Screen.ImageViewer(
                            imageViewerInfo = ImageViewerInfo(
                                images = listOf(accessibleUri.toString()),
                                initialIndex = 0
                            )
                        )
                    )
                }
                else -> {
                    onNavigate(Screen.FileBrowser(accessibleUri))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * 处理目录链接 (dir://content://...) — 用于活动日志中"保存到 xxx 文件夹"的可点击路径。
 * 导航到 FileBrowser 打开对应目录。
 */
class DirContentUrlHandler : UrlHandler {

    override fun canHandle(url: String): Boolean {
        return url.startsWith("dir://")
    }

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        runCatching {
            val contentUri = url.removePrefix("dir://").toUri()
            val accessibleUri = SafUriUtils.treeUriToFileUri(contentUri) ?: contentUri
            onNavigate(Screen.FileBrowser(accessibleUri))
        }.getOrElse {
            it.printStackTrace()
            return false
        }
        return true
    }
}

/**
 * 默认URL处理器，用于处理未知类型的URL
 */
class DefaultUrlHandler : UrlHandler {
    override fun canHandle(url: String): Boolean = true

    override fun handleUrl(url: String, context: Context, onNavigate: (Screen) -> Unit): Boolean {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent, null)
            return true
        } catch (_: Exception) {
            return false
        }
    }
}

private fun handleInternalNavigation(
    url: String,
    context: Context,
    onNavigate: (Screen) -> Unit
): Boolean {
    ItemDeeplinkResolver.resolve(url = url, context = context)?.let { resolvedScreen ->
        onNavigate(resolvedScreen)
        return true
    }

    AppNavigationRegistry.resolveDeeplink(url)?.let { resolved ->
        onNavigate(resolved.buildScreen())
        return true
    }

    val legacyScreen = Screen.valueOf(url)
    val normalizedRoute = Uri.decode(url)
        .substringAfter("://", "")
        .substringBefore('?')
        .substringAfter('/')
        .trim()

    val shouldTreatAsUnknown = legacyScreen is Screen.NewApp &&
        normalizedRoute.isNotBlank() &&
        normalizedRoute.lowercase() !in setOf("newapp", "new_app", "main")

    if (shouldTreatAsUnknown) return false

    onNavigate(legacyScreen)
    return true
}
