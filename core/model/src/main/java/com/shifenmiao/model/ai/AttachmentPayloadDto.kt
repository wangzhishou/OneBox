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

import kotlinx.serialization.Serializable

/**
 * 附件序列化 DTO — 用于 DB 持久化和 UI 展示。
 *
 * 设计说明：
 * - 此类同时服务于 DB 存储（通过 [from] 排除 localContent）和 UI 展示（完整字段）。
 * - 位于 :core:model 模块，供 feature:ai、feature:common 等多模块复用。
 * - DB 序列化时由 [from] 生成（排除 localContent），UI 展示时保留完整字段。
 */
@Serializable
data class AttachmentPayloadDto(
    val uri: String,
    val name: String,
    val mimeType: String,
    val size: Long = 0L,
    val url: String? = null,
    /** base64 内容（图片或文件）。DB 存储时为 null，UI 展示时可能有值。 */
    val localContent: String? = null,
    /** 缓存文件路径，用于从磁盘恢复 localContent。 */
    val localPath: String? = null,
    val parseError: String? = null,
) {
    /** 是否为图片附件 */
    val isImage: Boolean get() = mimeType.startsWith("image/")

    companion object {
        /**
         * 序列化为 DB 存储格式。
         *
         * 故意排除 localContent（base64 字符串约 100-700KB/张图片），
         * 仅持久化 localPath 引用，避免：
         * - DB 文件膨胀（10 张图可增加 1-7MB）
         * - Room Flow 发射大字段导致内存压力
         * - Gson 序列化/反序列化大 JSON 的性能开销
         *
         * 历史消息加载时由 AttachmentContentResolver 按需从 localPath 恢复。
         */
        fun from(media: AttachedMedia): AttachmentPayloadDto {
            return from(media, stripLocalContent = true)
        }

        /**
         * 序列化为 DTO，可通过 [stripLocalContent] 控制是否保留 localContent。
         *
         * @param stripLocalContent true 时排除 localContent（用于 DB 持久化），
         *                          false 时保留 localContent（用于 API 请求构建）。
         */
        fun from(media: AttachedMedia, stripLocalContent: Boolean): AttachmentPayloadDto {
            return AttachmentPayloadDto(
                uri = media.uri.toString(),
                name = media.name,
                mimeType = media.mimeType,
                size = media.size,
                url = media.url?.takeIf { it.isNotBlank() },
                // DB 持久化时排除 localContent，API 请求时保留以便直接使用
                localContent = if (stripLocalContent) null else media.localContent,
                localPath = media.localPath?.takeIf { it.isNotBlank() },
                parseError = media.parseError?.takeIf { it.isNotBlank() }
            )
        }
    }
}
