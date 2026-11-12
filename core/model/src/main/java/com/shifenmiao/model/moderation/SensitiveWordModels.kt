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

package com.shifenmiao.model.moderation

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class SensitiveWordCheckRequest(
    @SerialName("scene")
    val scene: String,
    @SerialName("fields")
    val fields: List<SensitiveWordCheckField>
)

@Keep
@Serializable
data class SensitiveWordCheckField(
    @SerialName("key")
    val key: String = "",
    @SerialName("text")
    val text: String
)

@Keep
@Serializable
data class SensitiveWordCheckResponse(
    @SerialName("hit")
    val hit: Boolean = false,
    @SerialName("hits")
    val hits: List<SensitiveWordHit> = emptyList(),
    @SerialName("message")
    val message: String? = null
)

@Keep
@Serializable
data class SensitiveWordHit(
    @SerialName("key")
    val key: String = "",
    @SerialName("words")
    val words: List<String> = emptyList(),
    @SerialName("reason")
    val reason: String? = null
)
