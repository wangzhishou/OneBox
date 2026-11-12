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

package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * item 点击统计（1:1 汇总行）。
 * 行数 = item 数，与点击次数无关；不会随用户行为线性增长。
 *
 * 索引：
 * - item_id 是 PK，不需要单独索引
 * - click_time 单索引保留，给"最近访问"用 ORDER BY DESC LIMIT 20
 * - 不再给 click_count 建索引（永远不会按它排序）
 */
@Entity(
    tableName = "item_click_stat",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["click_time"])
    ]
)
data class ItemClickStatEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "click_count", defaultValue = "0") val clickCount: Int = 0,
    /** null = 从未点击；非 null = 上次点击时间（epoch millis） */
    @ColumnInfo(name = "click_time") val clickTime: Long? = null,
) {
    /** DAO 还在用 java.util.Date 入参；提供一个兼容取景。 */
    val clickTimeAsDate: Date? get() = clickTime?.let(::Date)
}
