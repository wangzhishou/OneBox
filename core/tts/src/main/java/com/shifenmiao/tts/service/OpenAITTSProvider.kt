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

package com.shifenmiao.tts.service

import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSProviderType
import com.shifenmiao.model.tts.TTSSpeechRequest
import com.shifenmiao.network.api.TTSSpeechApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAITTSProvider @Inject constructor(
    private val api: TTSSpeechApi,
) : TTSProvider {

    override val descriptor = TTSProviderDescriptor(
        type = TTSProviderType.OPENAI_COMPATIBLE,
        defaultModel = "tts-1",
        defaultVoice = "alloy",
        supportedVoices = listOf(
            "alloy", "echo", "fable", "onyx", "nova", "shimmer",
            "ash", "ballad", "coral", "sage", "verse",
        ),
        isSelectable = FlavorType.fromName() == FlavorType.GOOGLE,
    )

    override suspend fun synthesize(config: TTSConfig, request: TTSSpeechRequest): Result<ByteArray> {
        return runCatching {
            val useProxy = isProxyRoute(config)
            val baseUrl = if (useProxy) config.proxyUrl else config.baseUrl
            val apiToken = if (useProxy) config.proxyToken else config.apiToken
            require(baseUrl.isNotBlank()) { "OpenAI baseUrl 或 proxyUrl 未配置" }
            require(apiToken.isNotBlank()) { "OpenAI apiToken 或 proxyToken 未配置" }

            val response = api.synthesizeSpeech(
                baseUrl.trim().removeSuffix("/"),
                "Bearer ${apiToken.trim()}",
                request,
            )
            response.bytes()
        }
    }

    override fun isProxyRoute(config: TTSConfig): Boolean =
        !config.hasDirectConfig && config.proxyUrl.isNotBlank()
}
