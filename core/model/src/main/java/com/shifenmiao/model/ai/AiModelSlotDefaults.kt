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

