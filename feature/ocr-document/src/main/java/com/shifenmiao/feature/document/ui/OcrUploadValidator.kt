package com.shifenmiao.feature.document.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import java.io.InputStream
import java.io.File

private const val MAX_UPLOAD_SIZE_BYTES: Long = 3L * 1024L * 1024L
private const val MIN_IMAGE_SHORT_SIDE_PX: Int = 15
private const val MAX_IMAGE_LONG_SIDE_PX: Int = 4096

data class ImageDimensions(
    val width: Int,
    val height: Int
)

sealed class UploadValidationResult {
    data class Ok(val fileSizeBytes: Long) : UploadValidationResult()
    data class TooLarge(val maxBytes: Long, val actualBytes: Long) : UploadValidationResult()
    data class InvalidImageDimensions(
        val width: Int,
        val height: Int,
        val minShortSidePx: Int,
        val maxLongSidePx: Int
    ) : UploadValidationResult()
    data object UnableToReadFileSize : UploadValidationResult()
    data object UnableToReadImageDimensions : UploadValidationResult()
}

fun validateOcrUpload(
    context: Context,
    uri: Uri,
    isPdf: Boolean
): UploadValidationResult {
    return validateUpload(
        context = context,
        uri = uri,
        isPdf = isPdf,
        maxUploadBytes = MAX_UPLOAD_SIZE_BYTES,
        validateImageDimensions = !isPdf,
        minImageShortSidePx = MIN_IMAGE_SHORT_SIDE_PX,
        maxImageLongSidePx = MAX_IMAGE_LONG_SIDE_PX
    )
}

fun validateDocConvertUpload(
    context: Context,
    uri: Uri,
    isPdf: Boolean
): UploadValidationResult {
    val maxUploadBytes = if (isPdf) {
        10L * 1024L * 1024L
    } else {
        4L * 1024L * 1024L
    }
    return validateUpload(
        context = context,
        uri = uri,
        isPdf = isPdf,
        maxUploadBytes = maxUploadBytes,
        validateImageDimensions = !isPdf,
        minImageShortSidePx = MIN_IMAGE_SHORT_SIDE_PX,
        maxImageLongSidePx = MAX_IMAGE_LONG_SIDE_PX
    )
}

fun bytesToDisplay(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024.0
    return if (bytes >= mb) {
        String.format("%.2fMB", bytes / mb)
    } else {
        String.format("%.0fKB", bytes / kb)
    }
}

private fun getFileSizeBytes(context: Context, uri: Uri): Long? {
    val resolver = context.contentResolver
    if (uri.scheme == "file") {
        val path = uri.path ?: return null
        return runCatching { File(path).length() }.getOrNull()
    }

    if (uri.scheme == "content") {
        runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                    val size = cursor.getLong(sizeIndex)
                    if (size >= 0) return size
                }
            }
        }
    }

    return runCatching {
        resolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val size = pfd.statSize
            size.takeIf { it >= 0 }
        }
    }.getOrNull()
}

private fun validateUpload(
    context: Context,
    uri: Uri,
    isPdf: Boolean,
    maxUploadBytes: Long,
    validateImageDimensions: Boolean,
    minImageShortSidePx: Int,
    maxImageLongSidePx: Int
): UploadValidationResult {
    val sizeBytes = getFileSizeBytes(context, uri)
        ?: measureStreamSizeUpTo(context, uri, maxUploadBytes + 1)
        ?: return UploadValidationResult.UnableToReadFileSize

    if (sizeBytes > maxUploadBytes) {
        return UploadValidationResult.TooLarge(maxUploadBytes, sizeBytes)
    }

    if (!isPdf && validateImageDimensions) {
        val dims = getImageDimensions(context, uri) ?: return UploadValidationResult.UnableToReadImageDimensions
        val shortSide = minOf(dims.width, dims.height)
        val longSide = maxOf(dims.width, dims.height)

        if (shortSide < minImageShortSidePx || longSide > maxImageLongSidePx) {
            return UploadValidationResult.InvalidImageDimensions(
                width = dims.width,
                height = dims.height,
                minShortSidePx = minImageShortSidePx,
                maxLongSidePx = maxImageLongSidePx
            )
        }
    }

    return UploadValidationResult.Ok(fileSizeBytes = sizeBytes)
}

private fun measureStreamSizeUpTo(context: Context, uri: Uri, maxBytesInclusive: Long): Long? {
    val resolver = context.contentResolver
    return runCatching {
        resolver.openInputStream(uri)?.use { input ->
            input.countBytesUpTo(maxBytesInclusive)
        }
    }.getOrNull()
}

private fun InputStream.countBytesUpTo(maxBytesInclusive: Long): Long {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (total < maxBytesInclusive) {
        val toRead = minOf(buffer.size.toLong(), maxBytesInclusive - total).toInt()
        val read = read(buffer, 0, toRead)
        if (read <= 0) break
        total += read
    }
    return total
}

private fun getImageDimensions(context: Context, uri: Uri): ImageDimensions? {
    val resolver = context.contentResolver
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    runCatching {
        resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
    }

    val w = options.outWidth
    val h = options.outHeight
    if (w > 0 && h > 0) return ImageDimensions(width = w, height = h)

    val fdDims = runCatching {
        resolver.openFileDescriptor(uri, "r")?.use { pfd ->
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, opts)
            val fw = opts.outWidth
            val fh = opts.outHeight
            if (fw > 0 && fh > 0) ImageDimensions(width = fw, height = fh) else null
        }
    }.getOrNull()
    if (fdDims != null) return fdDims

    if (Build.VERSION.SDK_INT >= 28) {
        return runCatching {
            val source = ImageDecoder.createSource(resolver, uri)
            val drawable = ImageDecoder.decodeDrawable(source) { decoder, _, _ ->
                decoder.isMutableRequired = false
            }
            val dw = drawable.intrinsicWidth
            val dh = drawable.intrinsicHeight
            if (dw > 0 && dh > 0) ImageDimensions(width = dw, height = dh) else null
        }.getOrNull()
    }

    return null
}
