/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
