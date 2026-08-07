package com.t8rin.imagetoolbox.feature.scan_qr_code.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.common.handle.navigation.ItemDeeplinkResolver
import com.shifenmiao.model.image.ImageViewerInfo
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeContentType
import com.t8rin.imagetoolbox.feature.scan_qr_code.domain.ScanCodeResultResolver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultScanCodeResultResolver @Inject constructor(
    @ApplicationContext private val context: Context
) : ScanCodeResultResolver {

    override fun classify(raw: String): ScanCodeContentType {
        val normalized = raw.trim()
        if (normalized.isBlank()) return ScanCodeContentType.TEXT
        if (looksLikeInternalDeeplink(normalized)) return ScanCodeContentType.INTERNAL_DEEPLINK
        if (isImageUri(normalized)) return ScanCodeContentType.IMAGE
        if (isWebUrl(normalized)) return ScanCodeContentType.WEB
        return ScanCodeContentType.TEXT
    }

    override fun resolveScreen(raw: String): Screen? {
        val normalized = raw.trim()
        if (normalized.isBlank()) return null

        resolveInternalScreen(normalized)?.let { return it }

        if (isImageUri(normalized)) {
            return Screen.ImageViewer(
                imageViewerInfo = ImageViewerInfo(
                    images = listOf(normalized),
                    initialIndex = 0
                )
            )
        }

        if (isWebUrl(normalized)) {
            return Screen.WebBrowser(url = normalized)
        }

        return null
    }

    private fun resolveInternalScreen(url: String): Screen? {
        ItemDeeplinkResolver.resolve(url = url, context = context)?.let { return it }
        AppNavigationRegistry.resolveDeeplink(url)?.let { return it.buildScreen() }

        val legacyScreen = Screen.valueOf(url)
        val normalizedRoute = Uri.decode(url)
            .substringAfter("://", "")
            .substringBefore('?')
            .substringAfter('/')
            .trim()

        val shouldTreatAsUnknown = legacyScreen is Screen.NewApp &&
            normalizedRoute.isNotBlank() &&
            normalizedRoute.lowercase(Locale.ROOT) !in setOf("newapp", "new_app", "main")

        return legacyScreen.takeUnless { shouldTreatAsUnknown }
    }

    private fun looksLikeInternalDeeplink(raw: String): Boolean {
        val scheme = raw.toUri().scheme?.lowercase(Locale.ROOT)
        return scheme == "onebox" || scheme == "app"
    }

    private fun isWebUrl(raw: String): Boolean {
        val scheme = raw.toUri().scheme?.lowercase(Locale.ROOT)
        return scheme == "http" || scheme == "https"
    }

    private fun isImageUri(raw: String): Boolean {
        val uri = runCatching { raw.toUri() }.getOrNull() ?: return false
        val accessibleUri = when {
            uri.scheme == "content" && DocumentsContract.isDocumentUri(context, uri) -> {
                SafUriUtils.documentUriToFileUri(uri) ?: uri
            }

            else -> uri
        }

        return when {
            accessibleUri.scheme == "content" -> {
                val mimeType = context.contentResolver.getType(accessibleUri)
                mimeType?.startsWith("image/") == true || pathLooksLikeImage(accessibleUri)
            }

            accessibleUri.scheme == "file" -> pathLooksLikeImage(accessibleUri)
            isWebUrl(raw) -> pathLooksLikeImage(accessibleUri)
            else -> false
        }
    }

    private fun pathLooksLikeImage(uri: Uri): Boolean {
        val path = uri.toString().substringBefore('?')
        val extension = MimeTypeMap.getFileExtensionFromUrl(path).lowercase(Locale.ROOT)
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        return mimeType?.startsWith("image/") == true || extension in IMAGE_EXTENSIONS
    }

    private companion object {
        val IMAGE_EXTENSIONS = setOf(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif",
            "tif", "tiff", "svg", "avif", "jfif"
        )
    }
}

