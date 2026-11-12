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
 * Token 使用总览聚合结果
 *
 * 统计口径：
 * - 排除 expired = true 的记录
 * - 排除 totalTokens = 0 的记录
 * - 仅统计 role = 'assistant' 侧，避免同一 completionId 下 question+answer 双写导致重复计数
 */
data class TokenUsageSummary(
    @ColumnInfo(name = "total_tokens")
    val totalTokens: Long = 0,

    @ColumnInfo(name = "prompt_tokens")
    val promptTokens: Long = 0,

    @ColumnInfo(name = "completion_tokens")
    val completionTokens: Long = 0,

    @ColumnInfo(name = "request_count")
    val requestCount: Long = 0,
)
