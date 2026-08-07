package com.shifenmiao.database.ai.entity

import com.shifenmiao.model.ai.AiEngine

/**
 * Engine 和 Model 的解析结果。
 * 当前已由 Repository 负责按 engineName + selectedModelName 解析，不再依赖 Room relation 元数据。
 */
data class AiEngineWithModel(
    val engine: AiEngineEntity,
    val model: AiModelEntity?
) {
    fun toAiEngine(): AiEngine {
        return engine.toAiEngine(model?.toAiModel())
    }
}

