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

package com.shifenmiao.network.model.comment

import kotlinx.serialization.Serializable

/**
 * 评论作者 — 与 go-proxy CommentAuthor 字段对齐.
 *
 * 字段全部可空 / 带默认值: 后端 author_user_lnk 可能没记录 (老评论、孤儿数据等),
 * 客户端展示 fallback 为 "匿名".
 *
 * 不实现 Parcelable: 仅 API 响应层使用, 无需进 Bundle.
 */
@Serializable
data class CommentAuthor(
    val id: Int = 0,
    val nickname: String = "",
    val avatar: String = "",
)
