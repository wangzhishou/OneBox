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

package com.shifenmiao.database.speedtest.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 测速配置数据库实体 */
@Entity(tableName = "speed_test_config")
data class SpeedTestConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 配置名称，用于界面展示 */
    val name: String,
    /** 测速下载 URL */
    val testUrl: String,
    /** 预估消耗流量 (MB) */
    val estimatedDataMb: Int,
    /** 最大测速时长 (秒) */
    val durationSeconds: Int,
    /** 是否为当前激活配置（同时只有一个为 true） */
    val isActive: Boolean = false,
    /** 是否为内置预设（预设不允许删除） */
    val isPreset: Boolean = false
)

