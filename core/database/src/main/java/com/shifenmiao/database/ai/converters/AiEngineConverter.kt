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

package com.shifenmiao.database.ai.converters

import androidx.room.TypeConverter
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import kotlinx.serialization.json.Json

class AiEngineConverter {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromAiEngine(engine: AiEngine): String {
        return json.encodeToString(engine)
    }

    @TypeConverter
    fun toAiEngine(engineString: String): AiEngine {
        val trimmedEngineString = engineString.trim()
        if (trimmedEngineString.isBlank()) {
            return AiEngine.defaultEngine()
        }

        return runCatching {
            json.decodeFromString<AiEngine>(trimmedEngineString)
        }.getOrElse {
            AiProvider.fromValue(trimmedEngineString)
                .takeUnless { provider -> provider == AiProvider.Default }
                ?.let(AiEngine::builtInEngine)
                ?: AiEngine.defaultEngine()
        }
    }
}