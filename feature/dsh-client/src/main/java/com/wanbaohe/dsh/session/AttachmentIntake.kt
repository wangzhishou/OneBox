package com.wanbaohe.dsh.session

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.wanbaohe.dsh.wire.model.ImageLimitsProjection
import com.wanbaohe.dsh.wire.model.PromptContentPart
import java.io.ByteArrayOutputStream

/**
 * 图片附件 intake(DSH-PROTOCOL §7,对齐 Flutter attachments.dart)。
 *
 * 契约:
 * - prompt 图片块:{type:image, mediaType, data(base64), name?},主机把字节提升为持久引用
 * - mediaType 闭合枚举:image/png | image/jpeg | image/webp | image/gif
 * - imageLimits 投影下发限额,intake 前本地预拒(数量/单张字节/聚合字节/像素/媒体类型),
 *   投影缺席时跳过预检(服务端权威)
 * - 尺寸探测为纯头部解析:Android 侧用 BitmapFactory.Options(inJustDecodeBounds),不解码像素
 */

/** prompt 图片媒体类型闭合枚举(协议固定四种) */
val AllowedImageMediaTypes: Set<String> = setOf(
    "image/png", "image/jpeg", "image/webp", "image/gif"
)

/** 单张待发图片的内存表示(尺寸探测结果一并携带) */
class PendingImage(
    val bytes: ByteArray,
    val mediaType: String,
    val width: Int,
    val height: Int,
    val name: String?
) {
    /** base64 上行形态(prompt 图片块的 data 字段) */
    fun base64(): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}

/** 本地预拒原因(不带文案,UI 按 kind 本地化) */
sealed class AttachmentRejection {
    /** 数量超限 */
    data class TooMany(val count: Int, val max: Int) : AttachmentRejection()

    /** 媒体类型不在主机枚举内 */
    data class UnsupportedType(val mediaType: String) : AttachmentRejection()

    /** 单张字节超限 */
    data class SingleTooLarge(val bytes: Long, val max: Long) : AttachmentRejection()

    /** 单张像素超限 */
    data class PixelsTooLarge(val pixels: Long, val max: Long) : AttachmentRejection()

    /** 聚合字节超限 */
    data class AggregateTooLarge(val bytes: Long, val max: Long) : AttachmentRejection()
}

/** 本地预拒上抛(send 路径的防御性复检;主入口在 intake 时已拦) */
class AttachmentRejectException(val rejection: AttachmentRejection) : Exception(
    "AttachmentRejectException($rejection)"
)

/** 本地预拒:返回 null = 通过;否则拒绝原因(不构造 payload、不上行) */
fun validateImages(
    images: List<PendingImage>,
    limits: ImageLimitsProjection
): AttachmentRejection? {
    if (images.size > limits.maxImagesPerMessage) {
        return AttachmentRejection.TooMany(images.size, limits.maxImagesPerMessage)
    }
    var total = 0L
    for (image in images) {
        if (image.mediaType !in limits.mediaTypes) {
            return AttachmentRejection.UnsupportedType(image.mediaType)
        }
        if (image.bytes.size.toLong() > limits.maxImageBytes) {
            return AttachmentRejection.SingleTooLarge(image.bytes.size.toLong(), limits.maxImageBytes)
        }
        val pixels = image.width.toLong() * image.height.toLong()
        if (pixels > limits.maxImagePixels) {
            return AttachmentRejection.PixelsTooLarge(pixels, limits.maxImagePixels)
        }
        total += image.bytes.size
    }
    if (total > limits.maxMessageImageBytes) {
        return AttachmentRejection.AggregateTooLarge(total, limits.maxMessageImageBytes)
    }
    return null
}

/** 构造 prompt content 数组:文本块 + 图片块(base64);文本为空时只发图片块 */
fun buildPromptContent(text: String, images: List<PendingImage>): List<PromptContentPart> =
    buildList {
        if (text.isNotEmpty()) add(PromptContentPart.Text(text))
        for (image in images) {
            add(
                PromptContentPart.Image(
                    mediaType = image.mediaType,
                    data = image.base64(),
                    name = image.name
                )
            )
        }
    }

/**
 * 从相册 Uri 读入一张图片:字节 + 纯头部尺寸探测 + 显示名。
 * 返回 null = 媒体类型不在闭合枚举内(本地拒,UI 提示不支持的类型)。
 */
fun readPendingImage(context: Context, uri: Uri): PendingImage? {
    val resolver = context.contentResolver
    val mediaType = resolver.getType(uri)?.lowercase()?.takeIf { it in AllowedImageMediaTypes }
        ?: return null
    val bytes = resolver.openInputStream(uri)?.use { it.readBytesCapped() } ?: return null
    // 纯头部尺寸探测:inJustDecodeBounds 不解码像素,大图也只看头
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    return PendingImage(
        bytes = bytes,
        mediaType = mediaType,
        width = bounds.outWidth.coerceAtLeast(0),
        height = bounds.outHeight.coerceAtLeast(0),
        name = queryDisplayName(context, uri)
    )
}

/** 读流并设硬上限(防御异常大图撑爆内存;限额校验在 validateImages 另做) */
private fun java.io.InputStream.readBytesCapped(): ByteArray {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0
    while (true) {
        val read = read(buffer)
        if (read < 0) break
        total += read
        if (total > MaxIntakeBytes) {
            throw AttachmentRejectException(
                AttachmentRejection.SingleTooLarge(total.toLong(), MaxIntakeBytes.toLong())
            )
        }
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

/** 取显示名(OpenableColumns.DISPLAY_NAME),失败回退 null(prompt 块 name 可缺席) */
private fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}.getOrNull()

/** intake 读入硬上限:128 MiB(对齐服务端请求体上限 160 MiB 的 base64 膨胀余量) */
private const val MaxIntakeBytes = 128 shl 20
