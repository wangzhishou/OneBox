package com.shifenmiao.tts.service

import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSSpeechRequest

/**
 * 新增引擎只需实现本接口并通过 Hilt @IntoSet 注册，无需修改中央调度逻辑。
 */
interface TTSProvider {
    val descriptor: TTSProviderDescriptor

    suspend fun synthesize(
        config: TTSConfig,
        request: TTSSpeechRequest,
    ): Result<ByteArray>

    /** 必须与 synthesize 的实际路由选择保持一致。 */
    fun isProxyRoute(config: TTSConfig): Boolean
}
