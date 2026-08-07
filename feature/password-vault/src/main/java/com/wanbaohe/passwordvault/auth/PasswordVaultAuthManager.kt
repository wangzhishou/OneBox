package com.wanbaohe.passwordvault.auth

import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 密码保险箱访问授权码的薄代理。
 *
 * 实际内存中的授权码存放在全局
 * [com.shifenmiao.base.auth.AuthorizationCodeStateHolder] 中,
 * 本类仅为保留 vault 模块内的兼容入口,转发到全局 holder。
 *
 * 调用方建议直接注入 [AuthorizationCodeStateHolder] 而非本类。
 */
@Singleton
class PasswordVaultAuthManager @Inject constructor(
    private val stateHolder: AuthorizationCodeStateHolder,
) {

    val authCode: StateFlow<String?> = stateHolder.authCode

    val isAuthorized: Boolean
        get() = stateHolder.isAuthorized
}
