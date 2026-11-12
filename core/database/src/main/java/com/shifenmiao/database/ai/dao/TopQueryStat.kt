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

package com.shifenmiao.database.ai.dao

import androidx.room.ColumnInfo

/**
 * 单条查询的 Token 消耗排行项
 *
 * 取 role = 'user' 侧的问题文本，按 total_tokens 降序排列。
 */
data class TopQueryStat(
    @ColumnInfo(name = "conversation_id")
    val conversationId: String = "",

    @ColumnInfo(name = "question")
    val question: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "entry_type")
    val entryType: String = "",

    @ColumnInfo(name = "entry_ref_id")
    val entryRefId: String? = null,

    @ColumnInfo(name = "model")
    val model: String = "",

    @ColumnInfo(name = "engine")
    val engine: String = "",

    @ColumnInfo(name = "total_tokens")
    val totalTokens: Long = 0,
)
