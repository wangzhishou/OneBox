package com.shifenmiao.ai.request

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 端侧模型目录注册表:resolve 时扫描应用私有目录 `files/local_models` 下的
 * `*.litertlm` 模型文件,文件名(去扩展名)即模型 id / 展示名。
 *
 * Phase 1 没有模型管理页,模型文件经 adb push / 文件导入手工放进该目录即可被发现;
 * Phase 2 由模型管理页(下载 / 校验 / 删除)正式接管该目录,届时本实现可改为
 * Room 查询,目录约定保持不变。
 *
 * "local-stub" 为保留的内置 demo spec,供 debug 联调主链路使用。
 */
@Singleton
class LocalModelDirectoryRegistry @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocalLlmModelRegistry {

    override fun resolve(modelName: String): LocalLlmModelSpec? {
        if (modelName == STUB_SPEC.id) return STUB_SPEC
        // modelName 只接受文件名,拒绝路径分隔符,防止目录穿越
        if (modelName.contains('/') || modelName.contains('\\')) return null
        val file = File(File(context.filesDir, MODELS_DIR_NAME), "$modelName.$MODEL_EXTENSION")
        if (!file.isFile) return null
        return LocalLlmModelSpec(
            id = modelName,
            displayName = modelName,
            modelPath = file.absolutePath,
            contextWindowTokens = 4096,
            maxOutputTokens = 1024,
            quantization = "unknown",
            estimatedMemoryMb = (file.length() / (1024 * 1024)).toInt(),
        )
    }

    private companion object {
        const val MODELS_DIR_NAME = "local_models"
        const val MODEL_EXTENSION = "litertlm"

        val STUB_SPEC = LocalLlmModelSpec(
            id = "local-stub",
            displayName = "Local Stub (demo)",
            modelPath = "/dev/null",
            contextWindowTokens = 4096,
            maxOutputTokens = 512,
            quantization = "Q4_K_M",
            estimatedMemoryMb = 0,
        )
    }
}
