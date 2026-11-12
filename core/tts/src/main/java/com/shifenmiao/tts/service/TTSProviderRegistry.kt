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

import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSProviderType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSProviderRegistry @Inject constructor(
    providers: Set<@JvmSuppressWildcards TTSProvider>,
) {
    private val providersByType = providers.associateBy { it.descriptor.type }.also { providerMap ->
        require(providerMap.size == providers.size) { "Duplicate TTS provider type" }
        require(providerMap.isNotEmpty()) { "No TTS provider registered" }
    }

    fun contains(type: TTSProviderType): Boolean = providersByType.containsKey(type)

    fun get(type: TTSProviderType): TTSProvider =
        providersByType[type] ?: error("Unsupported TTS provider: $type")

    fun getDescriptors(): List<TTSProviderDescriptor> = providersByType.values
        .map(TTSProvider::descriptor)
        .sortedBy { it.type.ordinal }

    fun createDefaultConfig(type: TTSProviderType): TTSConfig {
        val descriptor = get(type).descriptor
        return TTSConfig(
            providerType = type,
            model = descriptor.defaultModel,
            defaultVoice = descriptor.defaultVoice,
        )
    }

    fun fallbackType(): TTSProviderType =
        if (contains(TTSProviderType.MIMO)) {
            TTSProviderType.MIMO
        } else {
            getDescriptors().first().type
        }
}
