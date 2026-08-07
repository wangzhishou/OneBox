/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.feature.webp_tools.service

import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.model.ImageFormat
import com.t8rin.imagetoolbox.core.domain.image.model.Quality
import com.t8rin.imagetoolbox.feature.webp_tools.domain.WebpConverter
import com.t8rin.imagetoolbox.feature.webp_tools.domain.WebpParams
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WebP 业务服务层。
 *
 * 封装 WebpConverter 的底层调用，提供参数校验、统一错误处理和结果缓存。
 * AI AgentTool 和 UI 共用此 Service，避免双份业务逻辑。
 */
@Singleton
class WebpService @Inject constructor(
    private val converter: WebpConverter,
    dispatchersHolder: DispatchersHolder
) : DispatchersHolder by dispatchersHolder {

    data class ExtractResult(
        val frameUris: List<String>,
        val frameCount: Int
    )

    data class CreateResult(
        val data: ByteArray,
        val suggestedFilename: String
    )

    private var cachedExtractResult: Pair<String, ExtractResult>? = null

    /**
     * 从 WebP 文件中提取所有帧。
     *
     * 结果会被缓存，相同 URI 的后续调用直接返回缓存。
     */
    suspend fun extractFrames(
        webpUri: String,
        imageFormat: ImageFormat,
        quality: Quality
    ): Result<ExtractResult> = withContext(ioDispatcher) {
        cachedExtractResult?.takeIf { it.first == webpUri }?.let {
            return@withContext Result.success(it.second)
        }

        runCatching {
            val uris = converter.extractFramesFromWebp(webpUri, imageFormat, quality).toList()
            ExtractResult(uris, uris.size).also { result ->
                cachedExtractResult = webpUri to result
            }
        }
    }

    /**
     * 获取已缓存的提取结果。
     */
    fun getCachedExtractResult(webpUri: String): ExtractResult? =
        cachedExtractResult?.takeIf { it.first == webpUri }?.second

    /**
     * 手动缓存提取结果（供 UI 层在渐进加载完成后写入）。
     */
    fun cacheExtractResult(webpUri: String, frameUris: List<String>) {
        cachedExtractResult = webpUri to ExtractResult(frameUris, frameUris.size)
    }

    /**
     * 清空提取缓存。
     */
    fun clearCache() {
        cachedExtractResult = null
    }

    /**
     * 将多张图片合成为 WebP。
     */
    suspend fun createWebp(
        imageUris: List<String>,
        params: WebpParams,
        onProgress: () -> Unit
    ): Result<CreateResult> = withContext(defaultDispatcher) {
        if (imageUris.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("至少需要一张图片")
            )
        }

        runCatching {
            converter.createWebpFromImageUris(
                imageUris = imageUris,
                params = params,
                onFailure = { throw it },
                onProgress = onProgress
            )?.let { data ->
                CreateResult(
                    data = data,
                    suggestedFilename = "WEBP_${System.currentTimeMillis()}.webp"
                )
            } ?: throw IllegalStateException("WebP 合成失败")
        }
    }
}
