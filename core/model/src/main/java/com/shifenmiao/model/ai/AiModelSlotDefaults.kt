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

package com.shifenmiao.model.ai

enum class AiWorkingModelSlot {
    DEFAULT,
    FAST,
    DUEL_A,
    DUEL_B,
}

data class AiModelSlotDefault(
    val engineName: String,
    val modelName: String? = null,
)

object AiModelSlotDefaults {
    private val defaultsBySlot: Map<AiWorkingModelSlot, AiModelSlotDefault> = linkedMapOf(
        AiWorkingModelSlot.DEFAULT to build(AiProvider.Mimo),
        AiWorkingModelSlot.FAST to build(AiProvider.DeepSeek),
        AiWorkingModelSlot.DUEL_A to build(AiProvider.Mimo),
        AiWorkingModelSlot.DUEL_B to build(AiProvider.DeepSeek),
    )

    val DEFAULT: AiModelSlotDefault
        get() = get(AiWorkingModelSlot.DEFAULT)

    val FAST: AiModelSlotDefault
        get() = get(AiWorkingModelSlot.FAST)

    val DUEL_A: AiModelSlotDefault
        get() = get(AiWorkingModelSlot.DUEL_A)

    val DUEL_B: AiModelSlotDefault
        get() = get(AiWorkingModelSlot.DUEL_B)


    fun get(slot: AiWorkingModelSlot): AiModelSlotDefault {
        return requireNotNull(defaultsBySlot[slot]) {
            "Missing AI model slot default for slot=$slot"
        }
    }

    private fun build(provider: AiProvider): AiModelSlotDefault {
        return AiModelSlotDefault(
            engineName = provider.value,
            modelName = AiModel.getDefaultModelForProvider(provider).name,
        )
    }
}

