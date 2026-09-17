package com.wanbaohe.setting.local.component

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.StringRes
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AiWorkingModelSlot
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.remote.AiEngineConfig
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.ui.utils.BaseComponent
import com.wanbaohe.setting.local.download.LocalModelDownloader
import com.wanbaohe.settings.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume

/** 已下载到本地模型目录的模型文件 */
data class LocalModelFile(
    val fileName: String,
    val sizeBytes: Long,
) {
    /** 模型名 = 文件名去扩展名,与 LocalModelDirectoryRegistry 的解析约定一致 */
    val modelName: String get() = fileName.substringBeforeLast('.')
}

/** 推荐模型的下载状态(按文件名索引) */
data class ModelDownloadUiState(
    val isDownloading: Boolean = false,
    /** 0f..1f;服务端未返回总大小时为 null,界面显示不定进度 */
    val progress: Float? = null,
    val isFailed: Boolean = false,
)

/** 推荐下载清单条目 */
data class RecommendedLocalModel(
    @StringRes val displayNameRes: Int,
    @StringRes val descriptionRes: Int,
    val fileName: String,
    val url: String,
    val sizeLabel: String,
)

/**
 * 本地模型管理页组件:只管 `files/local_models` 目录下的模型文件与引擎/模型目录记录。
 * 页面无需感知端侧运行时——文件落盘后由 LocalModelDirectoryRegistry 扫描解析。
 */
class LocalModelManagementComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val aiEngineManager: AIEngineManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    // 模型文件托管在 Cloudflare R2(onebox-images bucket)的 models/ 路径,
    // 上新模型需先上传 R2 再在此登记;URL 失效时下载失败并在页面上提示重试。
    // 推荐顺序:Gemma 在前——它是 LiteRT-LM GPU 后端的官方优化模型;
    // Qwen3-0.6B 在 Adreno(WebGPU)上会撞 128MB storage buffer 上限回退 CPU,推理明显更慢
    val recommendedModels: List<RecommendedLocalModel> = listOf(
        RecommendedLocalModel(
            displayNameRes = R.string.local_model_gemma3_name,
            descriptionRes = R.string.local_model_gemma3_desc,
            fileName = "gemma3-1b-it-int4.litertlm",
            url = "https://images.oneboxable.com/models/gemma3-1b-it-int4.litertlm",
            sizeLabel = "~585MB",
        ),
        RecommendedLocalModel(
            displayNameRes = R.string.local_model_qwen3_name,
            descriptionRes = R.string.local_model_qwen3_desc,
            fileName = "Qwen3-0.6B.litertlm",
            url = "https://images.oneboxable.com/models/Qwen3-0.6B.litertlm",
            sizeLabel = "~615MB",
        ),
    )

    private val modelsDir: File get() = File(context.filesDir, MODELS_DIR_NAME)

    private val downloader = LocalModelDownloader()

    private val _downloadedModels = MutableStateFlow<List<LocalModelFile>>(emptyList())
    val downloadedModels: StateFlow<List<LocalModelFile>> = _downloadedModels.asStateFlow()

    private val _downloadStates = MutableStateFlow<Map<String, ModelDownloadUiState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, ModelDownloadUiState>> = _downloadStates.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    /** 当前聊天引擎(用于高亮"当前聊天模型"及删除后的回退判断) */
    val currentAIEngine = aiEngineManager.currentAIEngine

    sealed interface ImportResult {
        data object Success : ImportResult
        data class AlreadyExists(val fileName: String) : ImportResult
        data object Failed : ImportResult
    }

    init {
        refreshDownloadedModels()
    }

    fun refreshDownloadedModels() {
        _downloadedModels.value = modelsDir.listFiles()
            ?.filter { it.isFile && it.extension == MODEL_EXTENSION }
            ?.sortedBy { it.name.lowercase() }
            ?.map { LocalModelFile(fileName = it.name, sizeBytes = it.length()) }
            .orEmpty()
    }

    fun startDownload(model: RecommendedLocalModel) {
        if (_downloadStates.value[model.fileName]?.isDownloading == true) return
        _downloadStates.update { it + (model.fileName to ModelDownloadUiState(isDownloading = true)) }
        componentScope.launch(ioDispatcher) {
            when (
                val result = downloader.download(
                    url = model.url,
                    destination = File(modelsDir, model.fileName),
                    onProgress = { downloadedBytes, totalBytes ->
                        _downloadStates.update {
                            it + (model.fileName to ModelDownloadUiState(
                                isDownloading = true,
                                progress = if (totalBytes > 0) {
                                    (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
                                } else null,
                            ))
                        }
                    },
                )
            ) {
                is LocalModelDownloader.Result.Success -> {
                    _downloadStates.update { it - model.fileName }
                    refreshDownloadedModels()
                }

                LocalModelDownloader.Result.Cancelled -> {
                    _downloadStates.update { it - model.fileName }
                }

                is LocalModelDownloader.Result.Failed -> {
                    _downloadStates.update { it + (model.fileName to ModelDownloadUiState(isFailed = true)) }
                }
            }
        }
    }

    fun cancelDownload(fileName: String) {
        downloader.cancel(fileName)
        _downloadStates.update { it - fileName }
    }

    /** 从系统文件选择器导入:拷贝流到 local_models/<原文件名>。已存在且未确认覆盖时先回 AlreadyExists。 */
    fun importModel(uri: Uri, overwrite: Boolean, onComplete: (ImportResult) -> Unit) {
        if (_isImporting.value) return
        componentScope.launch(ioDispatcher) {
            _isImporting.value = true
            val result = try {
                val fileName = resolveDisplayName(uri)
                when {
                    fileName == null -> ImportResult.Failed
                    File(modelsDir, fileName).exists() && !overwrite -> ImportResult.AlreadyExists(fileName)
                    else -> {
                        modelsDir.mkdirs()
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw java.io.IOException("Cannot open selected file")
                        input.use { source ->
                            File(modelsDir, fileName).outputStream().use { target ->
                                source.copyTo(target)
                            }
                        }
                        refreshDownloadedModels()
                        ImportResult.Success
                    }
                }
            } catch (_: Exception) {
                ImportResult.Failed
            } finally {
                _isImporting.value = false
            }
            withContext(uiDispatcher) { onComplete(result) }
        }
    }

    /**
     * 删除模型文件;同时移除引擎目录中的模型记录,避免模型选择器出现悬空条目。
     * 若删除的是当前聊天模型,回退到 DEFAULT 槽位解析出的云端引擎。
     * onComplete(success, revertedChatModel)。
     */
    fun deleteModel(model: LocalModelFile, onComplete: (Boolean, Boolean) -> Unit) {
        componentScope.launch(ioDispatcher) {
            if (!File(modelsDir, model.fileName).delete()) {
                withContext(uiDispatcher) { onComplete(false, false) }
                return@launch
            }
            clearInferenceCaches(model.fileName)
            removeLocalModelRecord(model)
            val current = aiEngineManager.getCurrentAiEngine()
            val reverted = current.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE &&
                current.model.name.equals(model.modelName, ignoreCase = true)
            if (reverted) {
                val fallbackName = AiEngineConfig.getDefaultSlotConfig(AiWorkingModelSlot.DEFAULT).engineName
                val fallback = aiEngineCatalogManager.getEngineByName(fallbackName) ?: AiEngine.defaultEngine()
                aiEngineManager.switchEngine(fallback)
            }
            refreshDownloadedModels()
            withContext(uiDispatcher) { onComplete(true, reverted) }
        }
    }

    /**
     * 设为当前聊天模型:
     * 1. 确保存在 LOCAL_ON_DEVICE 引擎(ai_engines 有 name+request_protocol 唯一索引,先查再建);
     * 2. upsert 该文件的模型记录;
     * 3. 调工作模型 setter 切换 DEFAULT 槽位。
     */
    fun setAsChatModel(model: LocalModelFile, onComplete: (Boolean) -> Unit) {
        componentScope.launch {
            val engine = ensureLocalEngine()
            val modelRecord = if (engine != null) upsertModelRecord(model) else null
            if (engine == null || modelRecord == null) {
                onComplete(false)
                return@launch
            }
            aiEngineManager.switchModel(engine, modelRecord)
            onComplete(true)
        }
    }

    private suspend fun ensureLocalEngine(): AiEngine? {
        aiEngineCatalogManager.getEngineByNameAndProtocol(
            name = LOCAL_ENGINE_NAME,
            requestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE.name,
        )?.let { return it }

        val saved = suspendCancellableCoroutine<Boolean> { continuation ->
            aiEngineCatalogManager.saveEngineConfigOnly(
                aiEngineCatalogManager.createLocalEngineDraft().copy(
                    name = LOCAL_ENGINE_NAME,
                    title = context.getString(R.string.local_models_engine_title),
                    requestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE,
                    authType = AuthType.NONE,
                    stream = true,
                ),
                onComplete = { success -> continuation.resume(success) },
            )
        }
        if (!saved) return null
        return aiEngineCatalogManager.getEngineByNameAndProtocol(
            name = LOCAL_ENGINE_NAME,
            requestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE.name,
        )
    }

    private suspend fun upsertModelRecord(model: LocalModelFile): AiModel? =
        suspendCancellableCoroutine { continuation ->
            aiEngineCatalogManager.upsertLocalModel(
                aiEngineCatalogManager.createLocalModelDraft(LOCAL_ENGINE_NAME).copy(
                    name = model.modelName,
                    title = model.fileName,
                    provider = AiProvider.Local,
                    supportToolCalls = false,
                    free = true,
                    contextWindowTokens = 4096,
                ),
                onComplete = { success, saved ->
                    continuation.resume(saved.takeIf { success })
                },
            )
        }

    private suspend fun removeLocalModelRecord(model: LocalModelFile) {
        suspendCancellableCoroutine { continuation ->
            aiEngineCatalogManager.deleteLocalModel(
                model = aiEngineCatalogManager.createLocalModelDraft(LOCAL_ENGINE_NAME).copy(
                    name = model.modelName,
                    title = model.fileName,
                ),
                engineRequestProtocol = AiRequestProtocol.LOCAL_ON_DEVICE,
                onComplete = { _, _ -> continuation.resume(Unit) },
            )
        }
    }

    /**
     * 清理模型对应的 LiteRT 推理缓存(GPU mldrift + XNNPack,单模型可达 ~1.2GB)。
     * 缓存文件名以 `<模型文件名>_` 为前缀;新位置在 files/litertlm_cache,
     * 旧位置(cacheDir 根,早期版本遗留,会被系统当缓存清掉)一并扫掉。
     */
    private fun clearInferenceCaches(fileName: String) {
        val dirs = listOf(File(context.filesDir, INFERENCE_CACHE_DIR_NAME), context.cacheDir)
        dirs.forEach { dir ->
            dir.listFiles { file -> file.name.startsWith("${fileName}_") }
                ?.forEach { file -> runCatching { file.delete() } }
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        val fromProvider = runCatching {
            context.contentResolver.query(
                uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null,
            )?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            }
        }.getOrNull()
        return (fromProvider ?: uri.lastPathSegment)
            ?.substringAfterLast('/')
            ?.substringAfterLast('\\')
            ?.takeIf { it.isNotBlank() }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
        ): LocalModelManagementComponent
    }

    private companion object {
        const val LOCAL_ENGINE_NAME = "on-device"
        // 目录约定与 feature/ai 的 LocalModelDirectoryRegistry 保持一致
        const val MODELS_DIR_NAME = "local_models"
        const val MODEL_EXTENSION = "litertlm"
        // 与 feature/ai LiteRtLmRuntime 的推理缓存目录约定一致
        const val INFERENCE_CACHE_DIR_NAME = "litertlm_cache"
    }
}
