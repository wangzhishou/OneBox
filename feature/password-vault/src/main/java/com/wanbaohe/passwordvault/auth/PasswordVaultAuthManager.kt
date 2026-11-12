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
