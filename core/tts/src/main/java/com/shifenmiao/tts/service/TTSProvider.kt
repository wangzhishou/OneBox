package com.shifenmiao.tts.service

import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSSpeechRequest

/**
 * TTS Provider 抽象接口。
 *
 * 每个 Provider 实现负责：
 * 1. 将通用请求转换为厂商特定格式
 * 2. 发起网络请求
 * 3. 返回音频字节数组
 */
interface TTSProvider {

    /**
     * 发起语音合成请求，返回音频文件字节数组。
     */
    suspend fun synthesize(
        config: TTSConfig,
        request: TTSSpeechRequest,
    ): Result<ByteArray>

    /**
     * 该 Provider 支持的音色列表（用于 UI 展示）。
     */
    fun supportedVoices(): List<String>

    /**
     * 该 Provider 默认的音色。
     */
    fun defaultVoice(): String

    /**
     * 该 Provider 默认的模型。
     */
    fun defaultModel(): String
}
