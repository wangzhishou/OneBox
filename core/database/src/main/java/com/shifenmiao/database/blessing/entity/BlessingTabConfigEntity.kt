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

package com.shifenmiao.database.blessing.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 祈福 tab 文案自定义快照。
 * 每次编辑写入编辑发生当天（[date]）的一条记录；
 * 某日期生效的文案 = date 不晚于该日期的最近一条快照。
 */
@Entity(
    tableName = "blessing_tab_config",
    primaryKeys = ["date", "type"],
)
data class BlessingTabConfigEntity(
    @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "subtitle") val subtitle: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
