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

package com.shifenmiao.login.ui

import androidx.compose.runtime.MutableState
import com.shifenmiao.model.channel.NeedPrivacyPolicyDialog

/**
 * 登录动作统一入口: 处理《用户协议》与《隐私政策》的确认。
 *
 * 国内渠道(应用市场合规要求)与 foss 渠道(首启隐私弹窗同一条合规线)在未勾选协议时不执行 [action],
 * 改为弹出"服务协议及隐私保护"确认弹窗, 用户确认后由 [LoginAgreementDialog] 的 onConfirm 回调重新触发 [action];
 * google 渠道无此要求, 不弹窗, 直接执行 [action]。
 */
fun loginWithAgreementCheck(
    isUserAgreementChecked: Boolean,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: (((() -> Unit)) -> Unit),
    action: () -> Unit,
) {
    if (NeedPrivacyPolicyDialog.getConfigByFlavor().need && !isUserAgreementChecked) {
        setConfirmAction(action)
        showAgreementDialog.value = true
    } else {
        action()
    }
}
