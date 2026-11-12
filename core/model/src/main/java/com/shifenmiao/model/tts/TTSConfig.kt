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

package com.shifenmiao.model.tts

data class TTSConfig(
    val providerType: TTSProviderType = TTSProviderType.MIMO,
    val baseUrl: String = "",
    val apiToken: String = "",
    val proxyUrl: String = "",
    val proxyPath: String = "",
    val proxyToken: String = "",
    val model: String = "mimo-v2.5-tts",
    val defaultVoice: String = "mimo_default",
    val defaultSpeed: Double = 1.0,
) {
    val hasDirectConfig: Boolean
        get() = baseUrl.isNotBlank() && apiToken.isNotBlank()

    fun isValid(): Boolean = hasDirectConfig || proxyUrl.isNotBlank()

    fun useProxy(): Boolean = !hasDirectConfig && proxyUrl.isNotBlank()

    fun resolveBaseUrl(): String =
        if (baseUrl.isNotBlank()) baseUrl else proxyUrl

    fun resolveApiToken(): String =
        if (apiToken.isNotBlank()) apiToken else proxyToken
}
