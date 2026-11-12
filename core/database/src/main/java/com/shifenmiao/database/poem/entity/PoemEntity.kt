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

package com.shifenmiao.database.poem.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "poem",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["isFavorite"])
    ]
)
data class PoemEntity(
    /** 服务端诗词 id */
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "title") val title: String,
    /** 诗句 JSON 数组字符串 */
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "author") val author: String,
    @ColumnInfo(name = "dynasty") val dynasty: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "aiInsight") val aiInsight: String? = null,
    /** AI 生成的逐字拼音:每句一行,行内空格分隔 */
    @ColumnInfo(name = "pinyin") val pinyin: String? = null,
    /** AI 生成的现代汉语翻译 */
    @ColumnInfo(name = "translation") val translation: String? = null,
    @ColumnInfo(name = "isFavorite") val isFavorite: Boolean = false,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
)
