package com.shifenmiao.ai.request

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 国内渠道降级实现:端侧模型仅海外渠道(google/foss)可用,
 * 模型文件从 HuggingFace 分发,国内用户拿不到,且 LiteRT-LM native 库不进国内包。
 *
 * prepare 直接失败,由 Adapter 转成错误事件;generate 不会被执行到,兜底发 Failed。
 */
@Singleton
class UnsupportedLocalLlmRuntime @Inject constructor() : LocalLlmRuntime {

    override suspend fun prepare(model: LocalLlmModelSpec): LocalLlmPrepareResult =
        LocalLlmPrepareResult.Failure(
            LocalLlmError.Unknown("Local on-device models are not supported in this channel")
        )

    override fun generate(request: LocalLlmGenerateRequest): Flow<LocalLlmGenerateEvent> = flow {
        emit(LocalLlmGenerateEvent.Failed("Local on-device models are not supported in this channel"))
    }

    override suspend fun cancel(sessionId: String) = Unit

    override suspend fun releaseAll() = Unit
}
