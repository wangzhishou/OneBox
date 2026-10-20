package com.t8rin.imagetoolbox.core.ui.utils.content

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.shifenmiao.model.image.ImageViewerInfo
import com.shifenmiao.model.item.ItemEntityParams
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.webview.WebViewType
import com.t8rin.imagetoolbox.core.domain.content.ContentType
import com.t8rin.imagetoolbox.core.domain.content.ContentTypeResolver
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.utils.fileProviderAuthority
import java.io.File

/**
 * 内容路由器 — 给定一个 URI，统一决策最合适的打开方式。
 *
 * 这是整个 App 中**唯一的** "URI → 打开方式" 决策点。
 * 所有内部打开文件/文件夹/URI 的操作都应该通过此接口，
 * 禁止在业务层直接构造 Screen 来打开文件。
 *
 * 典型使用场景：
 * - 文件浏览器内文件点击
 * - 外部 Intent (ACTION_VIEW / SEND) 单文件处理
 * - DeepLink 中的 `onebox://action/open?uri=...`
 * - AI Agent 工具打开文件
 * - 任何需要"智能打开一个 URI"的地方
 */
interface ContentRouter {

    /**
     * 将 URI 路由到最合适的打开方式。
     *
     * @param uri 目标 URI（文件、文件夹、content://、file:// 等）
     * @param context Context（用于外部打开时创建 Intent）
     * @param onNavigate 内部导航回调，由调用方提供（通常是 push Screen 到导航栈）
     * @param allSiblings 同目录下的所有 URI（用于图片浏览器的上下文，支持左右滑动浏览）
     * @param fallbackToExternal 当内部无法处理时，是否回退到系统 chooser。
     * 外部打开入口（Intent/DeepLink）应传 false：打不开的类型改为回退文件浏览器，
     * 避免本 App 作为 chooser 候选被再次点回造成死循环；
     * 文件浏览器内点击传 true，保持"抛给其它应用打开"的行为。
     * @param mimeTypeHint 调用方已知的 MIME 类型（分享/打开 Intent 的 type），
     * 用于外部 content URI 无法反查文件名时的类型兜底。
     * @return 是否成功处理（true = 已路由，false = 未处理）
     */
    fun route(
        uri: Uri,
        context: Context,
        onNavigate: (Screen) -> Unit,
        allSiblings: List<Uri> = emptyList(),
        fallbackToExternal: Boolean = true,
        mimeTypeHint: String? = null
    ): Boolean
}

/**
 * 默认实现 — 根据 [ContentType] 分发到对应的 Screen 或外部应用。
 */
class DefaultContentRouter(
    private val contentTypeResolver: ContentTypeResolver
) : ContentRouter {

    override fun route(
        uri: Uri,
        context: Context,
        onNavigate: (Screen) -> Unit,
        allSiblings: List<Uri>,
        fallbackToExternal: Boolean,
        mimeTypeHint: String?
    ): Boolean {
        val contentType = contentTypeResolver.resolve(uri, context, mimeTypeHint)

        return when (contentType) {
            is ContentType.Image -> {
                openImageViewer(uri, allSiblings, onNavigate)
                true
            }

            is ContentType.Pdf -> {
                onNavigate(Screen.PdfTools(Screen.PdfTools.Type.Preview(pdfUri = uri)))
                true
            }

            is ContentType.Text -> {
                onNavigate(Screen.CodeEditor(initialUri = uri))
                true
            }

            is ContentType.Markdown -> {
                onNavigate(Screen.MarkdownEditor(initialUri = uri))
                true
            }

            is ContentType.Html -> {
                // 本地 HTML 统一走内置浏览器渲染，不再单独维护一个预览页
                onNavigate(
                    Screen.WebBrowser(
                        localUri = uri
                    )
                )
                true
            }

            is ContentType.Document -> {
                // Office / OpenDocument / RTF / EPUB：交给 file-viewer 模块的离线渲染页。
                // 具体用哪个页面由模块按后缀决定，这里只传 URI 与 MIME 兜底。
                onNavigate(
                    Screen.FileViewer(
                        uri = uri,
                        mimeType = contentType.mimeType
                    )
                )
                true
            }

            is ContentType.Audio -> {
                // 音频/视频统一走内置播放器：后台播放、通知栏控制都在那里
                onNavigate(
                    Screen.AudioPlayer(
                        uri = uri,
                        mimeType = contentType.mimeType,
                        playlist = allSiblings.map { it.toString() },
                    )
                )
                true
            }

            is ContentType.Video -> {
                onNavigate(
                    Screen.VideoPlayer(
                        uri = uri,
                        mimeType = contentType.mimeType,
                        playlist = allSiblings.map { it.toString() },
                    )
                )
                true
            }

            is ContentType.Directory -> {
                onNavigate(Screen.FileBrowser(initialUri = uri))
                true
            }

            is ContentType.Archive -> {
                // zip 走内置的浏览/解压页；rar/7z 等 App 内暂不支持，维持原来的外抛/回退行为。
                val isZip = contentType.extension == "zip" ||
                    contentType.mimeType == "application/zip" ||
                    contentType.mimeType == "application/x-zip-compressed"
                if (isZip) {
                    onNavigate(Screen.ZipBrowser(uri = uri))
                    true
                } else if (fallbackToExternal) {
                    openWithExternalApp(uri, context)
                } else {
                    onNavigate(Screen.FileBrowser(initialUri = uri))
                    true
                }
            }

            is ContentType.Unknown -> {
                // 打不开的文件：外部入口（fallbackToExternal=false）回退到文件浏览器
                // 定位到所在目录；文件浏览器内点击（=true）才抛给系统 chooser。
                // 不能对外部入口抛 chooser —— 本 App 自己就在 chooser 候选里，
                // 用户点回来会死循环，表现为"停在首页打不开"。
                if (fallbackToExternal) {
                    openWithExternalApp(uri, context)
                } else {
                    onNavigate(Screen.FileBrowser(initialUri = uri))
                    true
                }
            }

            else -> {
                // 兜底：外部入口回退文件浏览器，内部点击才外抛系统 chooser。
                if (fallbackToExternal) {
                    openWithExternalApp(uri, context)
                } else {
                    onNavigate(Screen.FileBrowser(initialUri = uri))
                    true
                }
            }
        }
    }

    private fun openImageViewer(
        uri: Uri,
        allSiblings: List<Uri>,
        onNavigate: (Screen) -> Unit
    ) {
        // allSiblings 已由调用方预过滤为图片 URI（如 FileBrowserComponent），
        // 无需再跑 contentTypeResolver.resolve 做 N 次 I/O。
        val imageUris = allSiblings.ifEmpty { listOf(uri) }

        val uriStrings = imageUris.map { it.toString() }
        val currentIndex = uriStrings.indexOf(uri.toString()).coerceAtLeast(0)

        onNavigate(
            Screen.ImageViewer(
                imageViewerInfo = ImageViewerInfo(
                    images = uriStrings,
                    initialIndex = currentIndex
                )
            )
        )
    }

    private fun openWithExternalApp(uri: Uri, context: Context): Boolean {
        return try {
            val resolvedUri = resolveUriForExternalOpen(uri, context)
            val mimeType = context.contentResolver.getType(resolvedUri) ?: "*/*"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(resolvedUri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(intent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // 排除本 App 自身（release/debug 两个包名都排），
                // 避免用户又点回我们造成"打开 -> 弹选择器 -> 再打开"死循环
                val selfComponents = listOf(
                    ComponentName(context, "com.shifenmiao.app.AppActivity"),
                    ComponentName("com.shifenmiao.app", "com.shifenmiao.app.AppActivity"),
                    ComponentName("com.shifenmiao.app.debug", "com.shifenmiao.app.AppActivity")
                ).distinct()
                putParcelableArrayListExtra(
                    Intent.EXTRA_EXCLUDE_COMPONENTS,
                    ArrayList(selfComponents)
                )
            }
            context.startActivity(chooserIntent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 将 file:// URI 转换为 content:// URI（Android 7.0+ 要求）。
     * content:// URI 直接透传。
     */
    private fun resolveUriForExternalOpen(uri: Uri, context: Context): Uri {
        return when (uri.scheme) {
            "file" -> {
                val file = File(uri.path ?: return uri)
                if (!file.exists()) return uri
                try {
                    FileProvider.getUriForFile(
                        context,
                        context.fileProviderAuthority,
                        file
                    )
                } catch (_: Exception) {
                    uri
                }
            }

            else -> uri
        }
    }
}
