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

package com.shifenmiao.model.auth

import androidx.compose.runtime.Immutable

/**
 * 全局授权码锁屏状态。
 *
 * 由 [com.shifenmiao.base.auth.AuthorizationCodeStateHolder] 持有,
 * 在 [com.shifenmiao.model.event.AppEventBus] 接收到
 * [RequestAuthorizationCodeEvent] 时切换为 [showAuthCode] = true,
 * 完成或取消后回到默认状态。
 *
 * [error] 字段使用 [AuthCodeError] 枚举表示,UI 层根据当前语言翻译为字符串
 * (core/r/values/strings.xml + 各 locale 翻译);这样 holder 不直接依赖
 * Android 资源,符合 "核心逻辑不依赖 IO / 系统资源" 的设计约定。
 */
@Immutable
data class AuthorizationCodeState(
    val showAuthCode: Boolean = false,
    val mode: AuthCodeMode = AuthCodeMode.Unlock,
    val setupStep: AuthCodeSetupStep = AuthCodeSetupStep.Enter,
    val error: AuthCodeError? = null,
)

/**
 * 首次设置授权码时的两步状态机。
 */
enum class AuthCodeSetupStep {
    Enter,
    Confirm,
}

/**
 * 锁屏上的错误类型,UI 层根据此枚举映射到字符串资源。
 */
enum class AuthCodeError {
    WrongCode,
    Mismatch,
}
