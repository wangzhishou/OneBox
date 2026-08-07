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