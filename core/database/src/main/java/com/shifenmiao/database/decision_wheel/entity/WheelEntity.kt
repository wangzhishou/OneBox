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

package com.shifenmiao.database.decision_wheel.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 转盘配置实体
 */
@Entity(tableName = "decision_wheels")
data class WheelEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val lastUsedAt: Long = 0L,
    val useCount: Int = 0
)

/**
 * 转盘选项实体
 */
@Entity(
    tableName = "wheel_options",
    indices = [Index(value = ["wheelId"])]
)
data class WheelOptionEntity(
    @PrimaryKey
    val id: String,
    val wheelId: String,
    val name: String,
    val colorHex: String, // 颜色的十六进制字符串
    val position: Int, // 选项在转盘中的位置
    val weight: Float = 1f, // 抽中权重，1 为等概率
    val enabled: Boolean = true // 抽后移除用；false 的扇区不参与抽取
)

/**
 * 转盘使用历史记录
 */
@Entity(
    tableName = "wheel_history",
    indices = [Index(value = ["wheelId"])]
)
data class WheelHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wheelId: String,
    val wheelTitle: String = "", // 冗余存标题：转盘被删除后历史仍有归属
    val selectedOptionId: String,
    val selectedOptionName: String,
    val timestamp: Long
)

