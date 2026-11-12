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

package com.shifenmiao.model.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 聊天消息模型(file_transfer WS 协议,对端是 web/ 浏览器 JS,
 * 字段名与 type 枚举值 text/file/system 一个字符都不能变)。
 */
@Serializable
data class ChatMessage(
    @SerialName("id")
    val id: String = "",

    @SerialName("type")
    val type: MessageType = MessageType.TEXT,

    @SerialName("content")
    val content: String = "",

    @SerialName("sender")
    val sender: String = "", // "mobile" or "browser"

    @SerialName("timestamp")
    val timestamp: Long = 0L,

    @SerialName("fileName")
    val fileName: String? = null,

    @SerialName("fileSize")
    val fileSize: Long? = null,

    @SerialName("filePath")
    val filePath: String? = null
)

@Serializable
enum class MessageType {
    @SerialName("text")
    TEXT,

    @SerialName("file")
    FILE,

    @SerialName("system")
    SYSTEM
}

