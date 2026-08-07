package com.shifenmiao.tts.service

import java.io.File

/**
 * TTS 音频文件缓存管理器。
 *
 * 缓存目录位于 app filesDir/tts/，使用 filesDir 而非 cacheDir，
 * 确保系统不会自动清理用户生成的音频。
 */
interface TTSCacheManager {

    /**
     * 根据哈希获取缓存文件。
     * @return 文件存在且大小大于 0 时返回，否则 null
     */
    fun getCacheFile(hash: String): File?

    /**
     * 将字节数据保存到缓存。
     * @return 保存后的文件
     */
    fun saveToCache(hash: String, bytes: ByteArray): File

    /**
     * 删除指定哈希对应的缓存文件。
     */
    fun deleteCache(hash: String): Boolean

    /**
     * 清除所有缓存文件。
     */
    fun clearAllCache()

    /**
     * 获取缓存目录总大小（字节）。
     */
    fun getCacheSize(): Long
}
