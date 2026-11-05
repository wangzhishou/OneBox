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
