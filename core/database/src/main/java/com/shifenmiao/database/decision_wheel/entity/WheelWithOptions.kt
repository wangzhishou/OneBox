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

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 转盘 + 选项的关联查询结果。
 *
 * 存在的唯一理由：**让 Room 同时监听 `decision_wheels` 和 `wheel_options` 两张表。**
 *
 * 之前 `getAllWheels()` 只查 `decision_wheels`，再在 map 里用挂起函数单独取选项。
 * Room 的失效追踪只认 SQL 里出现的表，所以 `UPDATE wheel_options SET enabled = ...`
 * （抽后移除 / 全部恢复）不会触发重新发射 —— 表现就是：抽完确实移除了（因为紧随其后的
 * `updateWheelUsage` 改了主表，顺带刷了一次），但"全部恢复"点了完全没反应。
 * 带 `@Relation` 的 `@Transaction` 查询会被 Room 展开成对父子表的联合监听，这里才能收得到。
 */
data class WheelWithOptions(
    @Embedded
    val wheel: WheelEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "wheelId"
    )
    val options: List<WheelOptionEntity>
)
