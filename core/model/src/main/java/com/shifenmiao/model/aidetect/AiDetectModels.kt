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

package com.shifenmiao.model.aidetect

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 朱雀 AIGC 文本检测请求(go-proxy 代理 /ai-detect/text) */
@Keep
@Serializable
data class AiDetectTextRequest(
    @SerialName("text")
    val text: String,
    @SerialName("is_merge")
    val isMerge: Boolean = true,
)

@Keep
@Serializable
data class AiDetectTextResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("softmax_confidence")
    val softmaxConfidence: Float = 0f,
    /** 疑似 AI 占比(0-1,越高越可能是 AI) */
    @SerialName("ratio_confidence")
    val ratioConfidence: Float = 0f,
    /** key "0"=人类、"1"=AI、"2"=疑似 AI */
    @SerialName("labels_ratio")
    val labelsRatio: Map<String, Float> = emptyMap(),
    @SerialName("segment_labels")
    val segmentLabels: List<AiDetectSegmentLabel> = emptyList(),
    @SerialName("msg")
    val msg: String? = null,
)

@Keep
@Serializable
data class AiDetectSegmentLabel(
    @SerialName("text")
    val text: String = "",
    /** 0=人类、1=AI、2=疑似 AI */
    @SerialName("label")
    val label: Int = 0,
    @SerialName("conf")
    val conf: Float = 0f,
    @SerialName("order")
    val order: Int = 0,
    @SerialName("position")
    val position: List<Int> = emptyList(),
)

/** 朱雀 AIGC 图片检测请求(go-proxy 代理 /ai-detect/image) */
@Keep
@Serializable
data class AiDetectImageRequest(
    @SerialName("imageBase64")
    val imageBase64: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
)

@Keep
@Serializable
data class AiDetectImageResponse(
    @SerialName("status")
    val status: String? = null,
    @SerialName("data")
    val data: AiDetectImageData? = null,
    @SerialName("message")
    val message: String? = null,
)

@Keep
@Serializable
data class AiDetectImageData(
    /** AI 生成置信度(0-1,越高越可能是 AI 生成) */
    @SerialName("confidence")
    val confidence: Float = 0f,
)
