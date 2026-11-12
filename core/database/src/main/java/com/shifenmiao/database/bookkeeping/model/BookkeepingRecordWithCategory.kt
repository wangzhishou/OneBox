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

package com.shifenmiao.database.bookkeeping.model

import androidx.room.ColumnInfo

data class BookkeepingRecordWithCategory(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    @ColumnInfo(name = "note") val note: String?,
    @ColumnInfo(name = "happened_at") val happenedAt: Long,
    @ColumnInfo(name = "exclude_from_stats") val excludeFromStats: Boolean,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon_key") val categoryIconKey: String?
)

data class BookkeepingCategorySum(
    @ColumnInfo(name = "category_id") val categoryId: String?,
    @ColumnInfo(name = "category_name") val categoryName: String?,
    @ColumnInfo(name = "category_icon_key") val categoryIconKey: String?,
    @ColumnInfo(name = "total_cents") val totalCents: Long
)

data class BookkeepingTimeTotal(
    @ColumnInfo(name = "time_key") val timeKey: String,
    @ColumnInfo(name = "total_cents") val totalCents: Long
)

