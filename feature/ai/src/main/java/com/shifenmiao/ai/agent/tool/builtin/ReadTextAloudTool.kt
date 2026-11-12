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

package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.R
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.agent.tool.RetryPolicy
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.tts.service.TTSService
import org.json.JSONObject
import javax.inject.Inject

/**
 * 内置工具:`read_text_aloud` — 把文本合成为语音并立即朗读出来。
 *
 * 复用 core/tts 的 TTSService(带本地缓存,相同文本二次朗读直接命中缓存)合成音频,
 * 再由 core/base 的 NetworkAudioPlayer 播放;音色/语速缺省时使用 TTS 设置里的默认值。
 * 朗读中的音频可用 `stop_reading` 工具打断。
 */
class ReadTextAloudTool @Inject constructor(
    private val ttsService: TTSService,
    private val audioPlayer: NetworkAudioPlayer,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "read_text_aloud"

    override val description: String =
        textProvider.string(R.string.agent_tool_read_text_aloud_description)

    override val title: String =
        textProvider.string(R.string.agent_tool_read_text_aloud_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_read_text_aloud_summary)

    override val category: ToolCategory = ToolCategory.MEDIA

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    // TTS 合成走网络接口,失败后允许一次快速重试
    override val retryPolicy: RetryPolicy = RetryPolicy.API_DEFAULT

    // 朗读会占用全局音频播放器,多次朗读/停止操作互斥,不可并行
    override val parallelizable: Boolean = false

    override val executionTimeoutMs: Long = 60_000L

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_read_text_aloud_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_read_text_aloud_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        properties = mapOf(
            "text" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_read_text_aloud_param_text),
            ),
            "voice" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_read_text_aloud_param_voice),
            ),
            "speed" to ToolParameterProperty(
                type = "number",
                description = textProvider.string(R.string.agent_tool_read_text_aloud_param_speed),
            ),
        ),
        required = listOf("text"),
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        val text = parseText(arguments)
            ?: return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_read_text_aloud_text_required),
                isError = true,
            )
        if (text.length > MAX_TEXT_LENGTH) {
            return AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_read_text_aloud_text_too_long,
                    MAX_TEXT_LENGTH,
                ),
                isError = true,
            )
        }
        if (!ttsService.getConfig().isValid()) {
            return AgentToolResult(
                content = textProvider.string(R.string.agent_tool_read_text_aloud_not_configured),
                isError = true,
            )
        }

        val json = JSONObject(arguments)
        val voice = json.optString("voice").takeIf { it.isNotBlank() }
        val speed = json.optDouble("speed", Double.NaN)
            .takeIf { !it.isNaN() }
            ?.coerceIn(MIN_SPEED, MAX_SPEED)

        return ttsService.synthesize(
            text = text,
            voice = voice,
            speed = speed,
            tag = AUDIO_TAG,
        ).fold(
            onSuccess = { file ->
                audioPlayer.playLocalFile(file)
                AgentToolResult(content = buildResultJson(text, voice, speed).toString())
            },
            onFailure = { error ->
                AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_read_text_aloud_failed,
                        error.message ?: textProvider.string(R.string.agent_tool_unknown_error),
                    ),
                    isError = true,
                )
            },
        )
    }

    private fun parseText(arguments: String): String? {
        if (arguments.isBlank()) return null
        return runCatching { JSONObject(arguments).optString("text").trim() }
            .getOrNull()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun buildResultJson(text: String, voice: String?, speed: Double?): JSONObject {
        val config = ttsService.getConfig()
        return JSONObject().apply {
            put("success", true)
            put("playing", true)
            put("textLength", text.length)
            put("voice", voice ?: config.defaultVoice)
            put("speed", speed ?: config.defaultSpeed)
        }
    }

    private companion object {
        /** 单次朗读文本长度上限,防止超长文本滥用合成接口 */
        const val MAX_TEXT_LENGTH = 2000
        const val MIN_SPEED = 0.5
        const val MAX_SPEED = 2.0

        /** 朗读音频的缓存分类标签,便于统一清理 */
        const val AUDIO_TAG = "agent-read-aloud"
    }
}
