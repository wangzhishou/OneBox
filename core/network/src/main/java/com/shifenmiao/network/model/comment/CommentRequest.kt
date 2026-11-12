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

/** 发表评论 / 回复请求体. */
@Serializable
data class CreateCommentRequest(
    val content: String,
    val images: List<Int> = emptyList(),
)

/** 管理员更新评论请求体 (屏蔽 / 取消屏蔽 / 设置屏蔽原因). */
@Serializable
data class UpdateCommentRequest(
    val blocked: Boolean? = null,
    val blockReason: String? = null,
)
