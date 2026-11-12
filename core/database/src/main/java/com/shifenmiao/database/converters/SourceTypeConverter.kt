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

package com.shifenmiao.database.converters

import androidx.room.TypeConverter
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AIConversationEntryType

class SourceTypeConverter {
    @TypeConverter
    fun fromSource(source: Source): Int = source.value

    @TypeConverter
    fun toSource(value: Int): Source = Source.fromValue(value)

    @TypeConverter
    fun fromEntryType(type: AIConversationEntryType): String = type.name

    @TypeConverter
    fun toEntryType(string: String): AIConversationEntryType =
        enumValueOf<AIConversationEntryType>(string)
}
