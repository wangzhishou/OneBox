package com.shifenmiao.tts.cache

import android.content.Context
import com.shifenmiao.tts.service.TTSCacheManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSCacheManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TTSCacheManager {

    private val cacheDir: File by lazy {
        File(context.filesDir, CACHE_DIR_NAME).also { it.mkdirs() }
    }

    override fun getCacheFile(hash: String): File? {
        val file = File(cacheDir, "$hash.mp3")
        return if (file.exists() && file.length() > 0) file else null
    }

    override fun saveToCache(hash: String, bytes: ByteArray): File {
        cacheDir.mkdirs()
        val file = File(cacheDir, "$hash.mp3")
        file.writeBytes(bytes)
        return file
    }

    override fun deleteCache(hash: String): Boolean {
        return File(cacheDir, "$hash.mp3").delete()
    }

    override fun clearAllCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }

    override fun getCacheSize(): Long {
        return cacheDir.listFiles()?.sumOf { it.length() } ?: 0L
    }

    companion object {
        private const val CACHE_DIR_NAME = "tts"
    }
}
