package com.shifenmiao.network.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.imageprocess.ImageProcessOp
import com.shifenmiao.model.imageprocess.ImageProcessRect
import com.shifenmiao.model.imageprocess.ImageProcessResponse
import com.shifenmiao.model.imageprocess.ImageSegmentRequest
import com.shifenmiao.network.api.BaiduImageProcessApiService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.logger.makeLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * 百度 AI 图像处理仓库:负责把位图按接口约束(最长边 ≤3000px、
 * 最短边 ≥128px、base64 ≤10M)预处理后调用网关代理接口,并解码结果图。
 *
 * 自 markup-layers 的 ImageAiProcessor 下沉,供各 feature(如 AI Agent 工具)直接注入复用。
 *
 * 坐标约定:[rect](归一化,相对传入位图)在实际发出的图片像素坐标系内换算,
 * 与预处理缩放解耦。
 */
@Singleton
class BaiduImageProcessRepository @Inject constructor(
    private val apiService: BaiduImageProcessApiService,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    /**
     * 执行一次 AI 图像处理。
     *
     * @param op 能力项
     * @param bitmap 全分辨率源图(软件位图)
     * @param rect 修复区域(仅 [ImageProcessOp.Inpainting] 需要,归一化坐标,null 视为全图)
     */
    suspend fun process(
        op: ImageProcessOp,
        bitmap: Bitmap,
        rect: ImageProcessRect? = null,
    ): Result<Bitmap> = try {
        val encoded = withContext(encodingDispatcher) { encodeWithinLimit(prepare(bitmap)) }
        val response = call(op, encoded, rect)
        if (!response.isSuccessful) {
            error("请求失败: ${response.code()} ${response.message()}")
        }
        val body = response.body() ?: error("响应体为空")
        val resultBase64 = body.image ?: body.foreground
        if (resultBase64.isNullOrBlank()) {
            val errorCode = body.errorCode
            if (errorCode != null) {
                // 带上百度错误码,方便定位(如 6=控制台未开通该能力)
                throw Exception("${body.errorMsg ?: "处理失败"} (error_code=$errorCode)")
            }
            throw Exception(body.errorMsg?.takeIf { it.isNotBlank() } ?: "处理结果为空")
        }
        val bytes = Base64.decode(resultBase64, Base64.DEFAULT)
        val decoded = withContext(decodingDispatcher) {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } ?: error("结果图解码失败")
        "process success: op=$op path=${op.path} ${decoded.width}x${decoded.height} logId=${body.logId}"
            .makeLog(LOG_TAG)
        Result.success(decoded)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        throwable.makeLog(LOG_TAG)
        Result.failure(throwable)
    }

    private suspend fun call(
        op: ImageProcessOp,
        encoded: EncodedImage,
        rect: ImageProcessRect?,
    ): Response<ImageProcessResponse> {
        val token = UrlConstants.ACCESS_TOKEN
        val image = encoded.base64
        return when (op) {
            ImageProcessOp.Dehaze -> apiService.dehaze(token, image)
            ImageProcessOp.ContrastEnhance -> apiService.contrastEnhance(token, image)
            ImageProcessOp.QualityEnhance -> apiService.imageQualityEnhance(token, image)
            ImageProcessOp.StretchRestore -> apiService.stretchRestore(token, image)
            ImageProcessOp.Inpainting -> apiService.inpainting(
                accessToken = token,
                image = image,
                rectangle = rectangleJson(encoded.bitmap, rect)
            )

            ImageProcessOp.DefinitionEnhance -> apiService.imageDefinitionEnhance(token, image)
            ImageProcessOp.ColorEnhance -> apiService.colorEnhance(token, image)
            ImageProcessOp.RemoveMoire -> apiService.removeMoire(token, image)
            ImageProcessOp.DocRepair -> apiService.docRepair(token, image)
            ImageProcessOp.Segment -> apiService.segment(token, ImageSegmentRequest(image = image))
        }
    }

    /**
     * 尺寸约束预处理:最长边 >3000 等比缩小到 3000;最短边 <128 等比放大到 128
     * (智能抠图要求最短边 ≥128,其余接口放大小图无害)。恒等时返回原位图。
     */
    private fun prepare(source: Bitmap): Bitmap {
        var width = source.width
        var height = source.height
        val longSide = max(width, height)
        if (longSide > MAX_SIDE) {
            val scale = MAX_SIDE.toFloat() / longSide
            width = (width * scale).roundToInt().coerceAtLeast(1)
            height = (height * scale).roundToInt().coerceAtLeast(1)
        }
        val shortSide = min(width, height)
        if (shortSide < MIN_SIDE) {
            val scale = MIN_SIDE.toFloat() / shortSide
            width = (width * scale).roundToInt().coerceAtLeast(1)
            height = (height * scale).roundToInt().coerceAtLeast(1)
        }
        if (width == source.width && height == source.height) return source
        return Bitmap.createScaledBitmap(source, width, height, true)
    }

    /**
     * 编码为 base64,超 10M 时先降 JPEG 质量、再按 0.8 步进缩尺寸重试。
     * 透明图固定 PNG(保住 alpha,如抠图结果二次处理);否则 JPEG。
     */
    private fun encodeWithinLimit(source: Bitmap): EncodedImage {
        var bitmap = source
        var quality = INITIAL_JPEG_QUALITY
        repeat(MAX_ENCODE_ATTEMPTS) {
            val base64 = encode(bitmap, quality)
            if (base64.length <= MAX_BASE64_LENGTH) {
                return EncodedImage(bitmap = bitmap, base64 = base64)
            }
            if (!bitmap.hasAlpha() && quality > MIN_JPEG_QUALITY) {
                quality -= JPEG_QUALITY_STEP
            } else {
                bitmap = bitmap.scaled(ENCODE_SHRINK_FACTOR)
                quality = INITIAL_JPEG_QUALITY
            }
        }
        // 保底:以当前最小配置再编一次(极端大图可能仍超限,交由服务端报错)
        return EncodedImage(bitmap = bitmap, base64 = encode(bitmap, MIN_JPEG_QUALITY))
    }

    private fun encode(bitmap: Bitmap, quality: Int): String {
        val output = ByteArrayOutputStream()
        val format = if (bitmap.hasAlpha()) {
            Bitmap.CompressFormat.PNG
        } else {
            Bitmap.CompressFormat.JPEG
        }
        bitmap.compress(format, quality, output)
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    private fun Bitmap.scaled(factor: Float): Bitmap {
        val width = (width * factor).roundToInt().coerceAtLeast(1)
        val height = (height * factor).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, width, height, true)
    }

    /**
     * 修复区域 JSON:[{"width":w,"height":h,"top":y,"left":x}],
     * 归一化矩形换算为所发图片的像素坐标(钳制在图内,w/h ≥1)
     */
    private fun rectangleJson(bitmap: Bitmap, rect: ImageProcessRect?): String {
        val region = rect ?: ImageProcessRect.Full
        val left = (region.left * bitmap.width).roundToInt().coerceIn(0, bitmap.width - 1)
        val top = (region.top * bitmap.height).roundToInt().coerceIn(0, bitmap.height - 1)
        val right = (region.right * bitmap.width).roundToInt().coerceIn(left + 1, bitmap.width)
        val bottom = (region.bottom * bitmap.height).roundToInt().coerceIn(top + 1, bitmap.height)
        return JSONArray().put(
            JSONObject()
                .put("width", right - left)
                .put("height", bottom - top)
                .put("top", top)
                .put("left", left)
        ).toString()
    }

    /** 实际发出的图片及其 base64;坐标换算(rectangle)以 [bitmap] 尺寸为准 */
    private class EncodedImage(
        val bitmap: Bitmap,
        val base64: String,
    )

}

private const val LOG_TAG = "BaiduImageProcessRepository"

/** 百度图像处理图片约束:最长边 ≤3000px */
private const val MAX_SIDE = 3000

/** 智能抠图约束:最短边 ≥128px(统一对所有能力生效) */
private const val MIN_SIDE = 128

/** base64 字符串长度上限 10M(base64 膨胀 4/3,按编码后长度直接判定) */
private const val MAX_BASE64_LENGTH = 10 * 1024 * 1024

private const val INITIAL_JPEG_QUALITY = 90
private const val MIN_JPEG_QUALITY = 50
private const val JPEG_QUALITY_STEP = 20
private const val ENCODE_SHRINK_FACTOR = 0.8f
private const val MAX_ENCODE_ATTEMPTS = 6
