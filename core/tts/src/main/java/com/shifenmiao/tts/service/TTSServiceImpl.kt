package com.shifenmiao.tts.service

import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSAudioEntry
import com.shifenmiao.model.tts.TTSProviderType
import com.shifenmiao.model.tts.TTSSpeechRequest
import com.shifenmiao.tts.repository.TTSAudioRepository
import com.shifenmiao.tts.repository.TTSConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSServiceImpl @Inject constructor(
    private val configRepository: TTSConfigRepository,
    private val audioRepository: TTSAudioRepository,
    private val cacheManager: TTSCacheManager,
    private val openAIProvider: OpenAITTSProvider,
    private val mimoProvider: MimoTTSProvider,
) : TTSService {

    override suspend fun synthesize(
        text: String,
        voice: String?,
        speed: Double?,
        tag: String,
    ): Result<File> = synthesizeInternal(
        config = configRepository.getConfig(),
        text = text,
        voice = voice,
        speed = speed,
        tag = tag,
        forceRefresh = false,
    )

    override suspend fun synthesizeWithConfig(
        config: TTSConfig,
        text: String,
        voice: String?,
        speed: Double?,
        tag: String,
        forceRefresh: Boolean,
    ): Result<File> = synthesizeInternal(
        config = config,
        text = text,
        voice = voice,
        speed = speed,
        tag = tag,
        forceRefresh = forceRefresh,
    )

    override suspend fun regenerate(
        text: String,
        voice: String?,
        speed: Double?,
        tag: String,
    ): Result<File> = synthesizeInternal(
        config = configRepository.getConfig(),
        text = text,
        voice = voice,
        speed = speed,
        tag = tag,
        forceRefresh = true,
    )

    private suspend fun synthesizeInternal(
        config: TTSConfig,
        text: String,
        voice: String?,
        speed: Double?,
        tag: String,
        forceRefresh: Boolean,
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val resolvedVoice = voice ?: config.defaultVoice
            val resolvedSpeed = speed ?: config.defaultSpeed
            val hash = computeHash(
                text = text,
                voice = resolvedVoice,
                speed = resolvedSpeed,
                config = config,
            )

            if (forceRefresh) {
                cacheManager.deleteCache(hash)
                audioRepository.deleteAudio(hash)
            }

            // 1. 检查缓存
            if (!forceRefresh) {
                cacheManager.getCacheFile(hash)?.let { cachedFile ->
                    audioRepository.getAudioByHash(hash)?.let { return@runCatching cachedFile }
                }
            }

            // 2. 调用 Provider 合成
            val request = TTSSpeechRequest(
                model = config.model,
                input = text,
                voice = resolvedVoice,
                speed = resolvedSpeed,
            )
            val provider = resolveProvider(config.providerType)
            val bytes = provider.synthesize(config, request).getOrThrow()

            // 3. 保存到缓存
            val file = cacheManager.saveToCache(hash, bytes)

            // 4. 保存数据库记录
            val entry = TTSAudioEntry(
                id = hash,
                text = text,
                voice = resolvedVoice,
                speed = resolvedSpeed,
                filePath = file.absolutePath,
                createdAt = System.currentTimeMillis(),
                tag = tag,
                providerType = config.providerType,
            )
            audioRepository.saveAudio(entry)

            file
        }
    }

    override fun getAudiosByTag(tag: String): Flow<List<TTSAudioEntry>> {
        return audioRepository.getAudiosByTag(tag)
    }

    override suspend fun getAudioByTextAndTag(text: String, tag: String): TTSAudioEntry? {
        return audioRepository.getAudioByTextAndTag(text, tag)
    }

    override suspend fun deleteAudio(id: String): Boolean {
        cacheManager.deleteCache(id)
        return audioRepository.deleteAudio(id)
    }

    override suspend fun deleteAudiosByTag(tag: String): Int {
        val audios = audioRepository.getAudiosByTagList(tag)
        audios.forEach { cacheManager.deleteCache(it.id) }
        return audioRepository.deleteAudiosByTag(tag)
    }

    override fun getConfig(): TTSConfig = configRepository.getConfig()

    override suspend fun updateConfig(config: TTSConfig) {
        configRepository.updateConfig(config)
    }

    override fun observeConfig(): Flow<TTSConfig> = configRepository.config

    override suspend fun clearAllCache() {
        cacheManager.clearAllCache()
        audioRepository.deleteAllAudios()
    }

    override suspend fun getCacheSize(): Long = cacheManager.getCacheSize()

    private fun resolveProvider(type: TTSProviderType): TTSProvider {
        return when (type) {
            TTSProviderType.OPENAI_COMPATIBLE -> openAIProvider
            TTSProviderType.MIMO -> mimoProvider
        }
    }

    private fun computeHash(
        text: String,
        voice: String,
        speed: Double,
        config: TTSConfig,
    ): String {
        val content = buildString {
            append(text)
            append('|')
            append(voice)
            append('|')
            append(speed)
            append('|')
            append(config.providerType.name)
            append('|')
            append(config.model)
            append('|')
            append(config.resolveBaseUrl().trim())
        }
        return MessageDigest.getInstance("SHA-256")
            .digest(content.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}
