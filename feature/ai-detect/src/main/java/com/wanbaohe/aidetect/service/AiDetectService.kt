/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.aidetect.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.shifenmiao.model.ModelProvider.AppJson
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import com.shifenmiao.database.aidetect.entity.AiDetectRecordEntity
import com.shifenmiao.database.aidetect.repo.AiDetectRecordRepository
import com.shifenmiao.model.aidetect.AiDetectImageRequest
import com.shifenmiao.model.aidetect.AiDetectSegmentLabel
import com.shifenmiao.model.aidetect.AiDetectTextRequest
import com.shifenmiao.model.aidetect.AiDetectTextResponse
import com.shifenmiao.network.api.ApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** 文本检测结果 */
data class TextDetectResult(
    /** 疑似 AI 占比(0-1) */
    val probability: Float,
    val softmaxConfidence: Float,
    val labelsRatio: Map<String, Float>,
    val segments: List<AiDetectSegmentLabel>,
)

/** 图片检测结果 */
data class ImageDetectResult(
    /** AI 生成置信度(0-1) */
    val confidence: Float,
)

/** 检测失败原因,UI 据此映射到字符串资源 */
enum class AiDetectError {
    IMAGE_TOO_LARGE,
    IMAGE_TOO_SMALL,
    IMAGE_UNREADABLE,
    NETWORK,
}

/** 检测失败异常,非本类型异常一律按 [AiDetectError.NETWORK] 处理 */
class AiDetectException(
    val error: AiDetectError,
    message: String? = null,
) : Exception(message)

/**
 * AI 检测服务:调用 go-proxy 的朱雀 AIGC 检测代理,并把结果写入本地历史。
 *
 * 图片检测会把选中图压缩一份缩略图存到应用私有目录供历史页展示,
 * 历史记录裁剪/删除/清空时同步清理对应文件。
 */
@Singleton
class AiDetectService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: ApiService,
    private val repository: AiDetectRecordRepository,
) {

    // ── 检测 ─────────────────────────────────────────────────────────────

    suspend fun detectText(text: String): Result<TextDetectResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.detectAiText(AiDetectTextRequest(text = text))
            val body = response.body()
            if (!response.isSuccessful || body == null || body.status != "success") {
                throw AiDetectException(AiDetectError.NETWORK, body?.msg)
            }
            val result = TextDetectResult(
                probability = body.ratioConfidence,
                softmaxConfidence = body.softmaxConfidence,
                labelsRatio = body.labelsRatio,
                segments = body.segmentLabels,
            )
            repository.insert(
                AiDetectRecordEntity(
                    type = TYPE_TEXT,
                    inputSummary = text.take(SUMMARY_MAX_LENGTH),
                    probability = result.probability,
                    verdict = verdictKey(result.probability),
                    detailJson = AppJson.encodeToString(body),
                )
            )
            result
        }
    }

    suspend fun detectImage(uri: Uri): Result<ImageDetectResult> = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw AiDetectException(AiDetectError.IMAGE_UNREADABLE)
            if (bytes.size > MAX_IMAGE_BYTES) {
                throw AiDetectException(AiDetectError.IMAGE_TOO_LARGE)
            }
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            if (bounds.outWidth < MIN_IMAGE_DIMENSION || bounds.outHeight < MIN_IMAGE_DIMENSION) {
                throw AiDetectException(AiDetectError.IMAGE_TOO_SMALL)
            }

            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val response = apiService.detectAiImage(AiDetectImageRequest(imageBase64 = base64))
            val body = response.body()
            val confidence = body?.data?.confidence
            if (!response.isSuccessful || body == null || body.status != "success" || confidence == null) {
                throw AiDetectException(AiDetectError.NETWORK, body?.message)
            }

            // 先存缩略图再入库,记录里保存缩略图路径供历史页展示
            val thumbPath = saveThumbnail(bytes)
            val result = ImageDetectResult(confidence = confidence)
            val (_, pruned) = repository.insert(
                AiDetectRecordEntity(
                    type = TYPE_IMAGE,
                    inputSummary = thumbPath.orEmpty(),
                    probability = result.confidence,
                    verdict = verdictKey(result.confidence),
                    detailJson = AppJson.encodeToString(body),
                )
            )
            pruned.filter { it.type == TYPE_IMAGE }.forEach { deleteThumbnail(it.inputSummary) }
            result
        }
    }

    // ── 历史 ─────────────────────────────────────────────────────────────

    fun observeHistory(): Flow<List<AiDetectRecordEntity>> = repository.observeAll()

    suspend fun deleteRecord(id: Long) {
        val removed = repository.delete(id) ?: return
        if (removed.type == TYPE_IMAGE) deleteThumbnail(removed.inputSummary)
    }

    suspend fun clearHistory() {
        repository.clearAll()
            .filter { it.type == TYPE_IMAGE }
            .forEach { deleteThumbnail(it.inputSummary) }
    }

    /** 从历史记录的 detailJson 还原文本检测结果(详情页用) */
    fun parseTextDetail(detailJson: String): AiDetectTextResponse? = runCatching {
        AppJson.decodeFromString<AiDetectTextResponse>(detailJson)
    }.getOrNull()

    // ── 内部 ─────────────────────────────────────────────────────────────

    private fun saveThumbnail(bytes: ByteArray): String? = runCatching {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        val scale = THUMBNAIL_MAX_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            bitmap
        }
        val dir = File(context.filesDir, THUMBNAIL_DIR_NAME).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_JPEG_QUALITY, out)
        }
        file.absolutePath
    }.getOrNull()

    private fun deleteThumbnail(path: String) {
        if (path.isBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    companion object {
        const val TYPE_TEXT = "text"
        const val TYPE_IMAGE = "image"

        const val VERDICT_LIKELY_AI = "likely_ai"
        const val VERDICT_MAYBE_AI = "maybe_ai"
        const val VERDICT_LIKELY_HUMAN = "likely_human"

        /** 判定阈值:>=0.8 疑似 AI,>=0.5 可能 AI,否则疑似人类 */
        fun verdictKey(probability: Float): String = when {
            probability >= 0.8f -> VERDICT_LIKELY_AI
            probability >= 0.5f -> VERDICT_MAYBE_AI
            else -> VERDICT_LIKELY_HUMAN
        }

        private const val SUMMARY_MAX_LENGTH = 200
        private const val MAX_IMAGE_BYTES = 10 * 1024 * 1024
        private const val MIN_IMAGE_DIMENSION = 300
        private const val THUMBNAIL_DIR_NAME = "ai_detect_thumbs"
        private const val THUMBNAIL_MAX_SIZE = 480
        private const val THUMBNAIL_JPEG_QUALITY = 80
    }
}
