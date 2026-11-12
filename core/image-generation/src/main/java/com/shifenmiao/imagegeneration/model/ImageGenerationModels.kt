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

package com.shifenmiao.imagegeneration.model

import kotlinx.serialization.Serializable

/** 独立于聊天模型的图片 Provider 配置。 */
@Serializable
data class ImageProviderConfig(
    // 默认值兜底 Gson 时代的 EncryptedSharedPreferences 旧数据(缺字段时 Gson 给 null)
    val id: String = "",
    val providerId: String = "",
    val displayName: String = "",
    val baseUrl: String = "",
    val apiToken: String = "",
    val proxyUrl: String = "",
    val proxyPath: String = "",
    val model: String = "",
    val enabled: Boolean = true,
) {
    val hasDirectConfig: Boolean
        get() = apiToken.isNotBlank()
}

data class ImageProviderDescriptor(
    val providerId: String,
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultProxyUrl: String,
    val defaultProxyPath: String,
    val defaultModel: String,
    val availableModels: List<String>,
    val supportsGeneration: Boolean,
    val supportsEditing: Boolean,
    val maxInputImages: Int,
)

data class ImageGenerationRequest(
    val prompt: String,
    val inputImages: List<String> = emptyList(),
    val model: String? = null,
    val negativePrompt: String? = null,
    val outputSize: String? = null,
    val outputCount: Int = 1,
    val seed: Int? = null,
    val watermark: Boolean = false,
    val promptExtend: Boolean = true,
    val promptExtendMode: String = "direct",
    val enableThinking: Boolean = true,
)

data class GeneratedImage(
    val url: String,
)

data class ImageGenerationResult(
    val images: List<GeneratedImage>,
    val providerId: String,
    val model: String,
    val requestId: String? = null,
    val width: Int? = null,
    val height: Int? = null,
)
