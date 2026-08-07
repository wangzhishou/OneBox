package com.t8rin.imagetoolbox.core.ui.utils.content

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
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
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
     * @param fallbackToExternal 当内部无法处理时，是否回退到系统 chooser
     * @return 是否成功处理（true = 已路由，false = 未处理）
     */
    fun route(
        uri: Uri,
        context: Context,
        onNavigate: (Screen) -> Unit,
        allSiblings: List<Uri> = emptyList(),
        fallbackToExternal: Boolean = true
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
        fallbackToExternal: Boolean
    ): Boolean {
        val contentType = contentTypeResolver.resolve(uri, context)

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
                onNavigate(
                    Screen.PreviewHtml(
                        localUri = uri
                    )
                )
                true
            }

            is ContentType.Directory -> {
                onNavigate(Screen.FileBrowser(initialUri = uri))
                true
            }

            is ContentType.Unknown -> {
                val typeHint = contentType.mimeType
                    ?: uri.lastPathSegment?.substringAfterLast('.', "")?.let { ".$it" }
                    ?: ""
                AppToastHost.showToast(
                    message = context.getString(
                        com.t8rin.imagetoolbox.core.resources.R.string.unsupported_type,
                        typeHint
                    )
                )
                if (fallbackToExternal) {
                    openWithExternalApp(uri, context)
                } else {
                    false
                }
            }

            else -> {
                if (fallbackToExternal) {
                    openWithExternalApp(uri, context)
                } else {
                    false
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

            val chooserIntent = Intent.createChooser(intent, null)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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
                        context.getString(
                            com.t8rin.imagetoolbox.core.resources.R.string.file_provider
                        ),
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
