package com.t8rin.imagetoolbox.feature.pdf_tools.service

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.shifenmiao.network.service.FileDownloadService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfToolsService @Inject constructor(
    private val fileDownloadService: FileDownloadService,
    @ApplicationContext private val context: Context,
) {

    suspend fun resolveLocalPdfUri(input: String): Result<Uri> = withContext(Dispatchers.IO) {
        runCatching {
            val trimmed = input.trim()
            when {
                trimmed.startsWith("http://", ignoreCase = true) ||
                    trimmed.startsWith("https://", ignoreCase = true) -> {
                    val cacheName = ensureFilenameIsPdf(deriveFileNameFromUrl(trimmed))
                    val result = fileDownloadService.downloadSync(
                        url = trimmed,
                        fileName = cacheName,
                        savePath = PDF_CACHE_DIR,
                        overwrite = false
                    ).getOrThrow()
                    Uri.fromFile(result.file)
                }

                trimmed.startsWith("content://", ignoreCase = true) ||
                    trimmed.startsWith("file://", ignoreCase = true) -> {
                    com.t8rin.imagetoolbox.core.data.utils.SafUriUtils.toFileUri(context, trimmed)?.toUri()
                        ?: trimmed.toUri()
                }

                else -> error("Unsupported uri scheme: $trimmed")
            }
        }
    }

    fun ensureFilenameIsPdf(name: String?): String {
        val safe = name?.takeIf { it.isNotBlank() } ?: "pdf_${UUID.randomUUID().toString().take(8)}"
        return if (safe.endsWith(".pdf", ignoreCase = true)) safe else "$safe.pdf"
    }

    private fun deriveFileNameFromUrl(url: String): String {
        return runCatching {
            val path = java.net.URL(url).path
            val rawName = path.substringAfterLast('/').substringBefore('?')
            rawName.takeIf { it.isNotBlank() }
        }.getOrNull() ?: "pdf_${UUID.randomUUID().toString().take(8)}.pdf"
    }

    companion object {
        private const val PDF_CACHE_DIR = "pdf-cache"
    }
}
