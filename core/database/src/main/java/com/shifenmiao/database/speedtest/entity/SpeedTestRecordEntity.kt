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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 测速记录数据库实体 */
@Entity(tableName = "speed_test_record")
data class SpeedTestRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 网络类型，如 "5G移动网络"、"WiFi" */
    val networkType: String,
    /** 下载速度 (Mbps) */
    val downloadMbps: Float,
    /** 上传速度 (Mbps)，-1 表示未测量 */
    @ColumnInfo(defaultValue = "-1")
    val uploadMbps: Float = -1f,
    /** 网络延迟 (ms)，-1 表示未能测量 */
    val latencyMs: Int,
    @ColumnInfo(defaultValue = "''")
    val clientIp: String = "",
    @ColumnInfo(defaultValue = "''")
    val clientLocation: String = "",
    @ColumnInfo(defaultValue = "''")
    val carrier: String = "",
    @ColumnInfo(defaultValue = "''")
    val serverHost: String = "",
    @ColumnInfo(defaultValue = "''")
    val latencyNode: String = "",
    @ColumnInfo(defaultValue = "''")
    val downloadNode: String = "",
    @ColumnInfo(defaultValue = "''")
    val uploadNode: String = "",
    /** 记录时间戳 (ms) */
    val recordedAt: Long = System.currentTimeMillis()
)

