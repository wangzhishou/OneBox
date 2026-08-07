package com.shifenmiao.ai.agent.auth

import com.shifenmiao.model.ai.tool.ToolCatalogItem
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthorizationResult {
    object Allowed : AuthorizationResult()
    data class NeedConfirmation(val reason: String) : AuthorizationResult()
    data class Denied(val reason: String) : AuthorizationResult()
}

@Singleton
class ToolAuthorizationGuard @Inject constructor() {

    /**
     * 工具执行前校验。
     *
     * 注：历史上曾做 sessionAllowed 权限围墙校验，当前架构下工具可见性由工具目录统一管理，
     * 此处不再拦截。
     */
    fun evaluate(
        toolName: String,
        metadata: ToolCatalogItem?
    ): AuthorizationResult {
        return AuthorizationResult.Allowed
    }
}
