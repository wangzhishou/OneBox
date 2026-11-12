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

package com.shifenmiao.model.ai

import android.os.Parcelable
import com.shifenmiao.model.Source
import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.common.Meta
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Agent(
    val id: Int = 0,
    /** 服务端 ID；本地创建未推送时为 null。fallback 路径专用，与 id 本地表主键解耦。 */
    val remoteId: Int? = null,
    /** Strapi v5 文档级 cuid；回查 / 回写服务端定位优先用它（数字 remoteId 重发后会漂移）。 */
    @SerialName("documentId")
    val documentId: String? = null,
    val title: String? = "",
    val description: String? = "",
    val prompt: String? = null,
    val header: AgentHeader? = null,
    @SerialName("body")
    val dynamicBody: String? = null,
    @SerialName("background_image")
    val backgroundImage: StrapiImage? = null,
    val source: Source? = null
) : Parcelable

@Parcelize
@Serializable
data class AgentItem(
    val data: Agent = Agent(),
    val meta: Meta = Meta()
) : Parcelable


@Parcelize
@Serializable
data class AgentHeader(
    val colors: String = "",
) : Parcelable
