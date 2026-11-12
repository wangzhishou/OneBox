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
