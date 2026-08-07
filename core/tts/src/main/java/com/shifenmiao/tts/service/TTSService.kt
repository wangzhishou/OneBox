package com.shifenmiao.tts.service

import com.shifenmiao.model.tts.TTSAudioEntry
import com.shifenmiao.model.tts.TTSConfig
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * TTS 语音合成服务接口。
 *
 * 设计要点：
 * - 所有合成结果都通过本地文件返回，调用方拿到 File 后自行播放
 * - 缓存 key 为 SHA-256(text + voice + speed + providerType)，保证不同引擎的音频独立缓存
 * - tag 用于分类管理（如 "xiangqi-move", "xiangqi-check"）
 * - 支持 Flow 监听指定 tag 的音频变化
 */
interface TTSService {

    /**
     * 合成语音。命中缓存时直接返回，否则调用 TTS API。
     *
     * @param text 要合成的文本
     * @param voice 音色，null 时使用当前配置的默认音色
     * @param speed 语速，null 时使用当前配置的默认语速
     * @param tag 分类标签
     * @return 本地音频文件
     */
    suspend fun synthesize(
        text: String,
        voice: String? = null,
        speed: Double? = null,
        tag: String = "",
    ): Result<File>

    /**
     * 使用显式配置合成语音，可用于设置页试听等“未保存先预览”的场景。
     *
     * @param forceRefresh 为 true 时跳过缓存并强制重新请求接口
     */
    suspend fun synthesizeWithConfig(
        config: TTSConfig,
        text: String,
        voice: String? = null,
        speed: Double? = null,
        tag: String = "",
        forceRefresh: Boolean = false,
    ): Result<File>

    /**
     * 强制重新生成（跳过缓存，删除旧文件后重新调用 API）。
     */
    suspend fun regenerate(
        text: String,
        voice: String? = null,
        speed: Double? = null,
        tag: String = "",
    ): Result<File>

    /**
     * 根据 tag 获取已生成的音频（按创建时间降序）。
     */
    fun getAudiosByTag(tag: String): Flow<List<TTSAudioEntry>>

    /**
     * 根据文本和 tag 查找已生成的音频（精确匹配文本）。
     */
    suspend fun getAudioByTextAndTag(text: String, tag: String): TTSAudioEntry?

    /**
     * 删除指定音频（删文件 + 删数据库记录）。
     */
    suspend fun deleteAudio(id: String): Boolean

    /**
     * 删除指定 tag 下的所有音频。
     */
    suspend fun deleteAudiosByTag(tag: String): Int

    /**
     * 获取当前 TTS 配置。
     */
    fun getConfig(): TTSConfig

    /**
     * 更新 TTS 配置（持久化到 SharedPreferences）。
     */
    suspend fun updateConfig(config: TTSConfig)

    /**
     * 监听配置变化。
     */
    fun observeConfig(): Flow<TTSConfig>

    /**
     * 清除所有缓存。
     */
    suspend fun clearAllCache()

    /**
     * 获取缓存总大小（字节）。
     */
    suspend fun getCacheSize(): Long
}
