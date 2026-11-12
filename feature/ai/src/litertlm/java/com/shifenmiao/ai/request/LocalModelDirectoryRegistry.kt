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

package com.shifenmiao.ai.request

import com.shifenmiao.common.localmodel.LocalModelRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelDirectoryRegistry @Inject constructor(
    private val localModelRepository: LocalModelRepository,
) : LocalLlmModelRegistry {

    override fun resolve(modelName: String): LocalLlmModelSpec? {
        if (modelName == STUB_SPEC.id) return STUB_SPEC
        val model = localModelRepository.findInstalledModel(modelName) ?: return null
        return LocalLlmModelSpec(
            id = modelName,
            displayName = modelName,
            modelPath = model.path,
            contextWindowTokens = 4096,
            maxOutputTokens = 1024,
            quantization = "unknown",
            estimatedMemoryMb = (model.sizeBytes / (1024 * 1024)).toInt(),
        )
    }

    private companion object {
        val STUB_SPEC = LocalLlmModelSpec(
            id = "local-stub",
            displayName = "Local Stub (demo)",
            modelPath = "/dev/null",
            contextWindowTokens = 4096,
            maxOutputTokens = 512,
            quantization = "Q4_K_M",
            estimatedMemoryMb = 0,
        )
    }
}
