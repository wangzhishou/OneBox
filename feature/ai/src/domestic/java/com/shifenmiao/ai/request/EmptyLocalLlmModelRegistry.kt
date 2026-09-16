package com.shifenmiao.ai.request

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 国内渠道不注册任何本地模型:resolve 一律 miss,
 * Adapter 走 "Local model not registered" 分支,国内用户不会看到可用入口。
 */
@Singleton
class EmptyLocalLlmModelRegistry @Inject constructor() : LocalLlmModelRegistry {
    override fun resolve(modelName: String): LocalLlmModelSpec? = null
}
