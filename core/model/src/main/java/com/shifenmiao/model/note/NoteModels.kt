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

package com.shifenmiao.model.note

import com.shifenmiao.model.Category

/**
 * 笔记创建结果
 */
sealed class NoteResult {
    data class Success(val itemId: Int, val title: String) : NoteResult()
    data class Error(val message: String) : NoteResult()
}

/**
 * 笔记详情
 */
data class NoteDetail(
    val itemId: Int,
    val title: String,
    val description: String,
    val data: String,
    val categories: List<Category>
)

/**
 * 笔记摘要（用于列表）
 */
data class NoteSummary(
    val itemId: Int,
    val title: String,
    val description: String
)

/**
 * 笔记保存参数（创建或更新）
 *
 * @param existingItemId 非 null 时为更新已有笔记，null 时为创建新笔记
 */
data class NoteSaveParams(
    val existingItemId: Int? = null,
    val title: String,
    val description: String,
    val data: String,
    val categoryIds: List<Long>
)
