package com.shifenmiao.base.authcode

import com.shifenmiao.model.auth.AuthCodeVerifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局授权码校验器。
 *
 * 通过 [AuthCodeService] 读取 / 校验持久化在数据库中的授权码,
 * 由 [com.shifenmiao.base.auth.AuthorizationCodeStateHolder] 通过 Hilt 注入使用。
 *
 * 切换为基于数据库的实现后,授权码的"是否已设置"与"是否能校验通过"都有了
 * 真实的存储源,不再依赖其它 feature (如密码保险箱) 是否有数据。
 */
@Singleton
class AuthCodeAuthCodeVerifier @Inject constructor(
    private val service: AuthCodeService,
) : AuthCodeVerifier {

    override val id: String = ID

    override suspend fun hasCode(): Boolean = service.hasCode()

    override suspend fun verify(code: String): Boolean = service.verify(code)

    companion object {
        const val ID: String = "auth_code_db"
    }
}
