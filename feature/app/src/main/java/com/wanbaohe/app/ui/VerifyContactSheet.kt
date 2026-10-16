package com.wanbaohe.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ErrorTextInputField
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.model.login.LoginChannelConfig
import com.shifenmiao.model.user.event.BindPhoneEvent
import com.shifenmiao.storage.TokenStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.delay

/**
 * 联系方式验证引导弹层: 未验证用户使用 AI 功能前触发, 单弹层完成验证。
 * 已绑真实邮箱 → 直接向该邮箱发码验证; 未绑邮箱 → 输入新邮箱发码绑定(验证码通过即视为已验证);
 * 国内渠道额外提供手机号验证入口(跳转全局 BindPhoneSheet 流程)。
 */
@Composable
fun VerifyContactSheet(
    loginComponent: LoginComponent,
    onVerified: () -> Unit,
    onDismiss: () -> Unit,
) {
    // 已绑真实邮箱: 验证现有邮箱(confirm-email); 否则: 绑定新邮箱(bind-email, 服务端同时置 confirmed)
    val boundEmail = remember { boundRealEmail() }
    val confirmMode = boundEmail != null

    val email = remember { mutableStateOf(boundEmail.orEmpty()) }
    val codeNumber = remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var isCodeError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = stringResource(R.string.identity_verification),
        enableBackHandler = true,
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = AppTheme.dimens.paddingNormal)
        ) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            Text(
                text = stringResource(id = R.string.verify_contact_ai_desc),
                style = MaterialTheme.typography.labelMedium.copy(
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            if (confirmMode) {
                Text(
                    text = stringResource(R.string.profile_code_send_to, email.value),
                    style = MaterialTheme.typography.labelMedium.copy(
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            } else {
                GlassOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email.value,
                    onValueChange = { newValue: String ->
                        email.value = newValue
                        isEmailError = false
                    },
                    label = {
                        Text(text = stringResource(id = R.string.profile_user_info_email))
                    },
                    isError = isEmailError,
                    supportingText = {
                        if (isEmailError) {
                            ErrorTextInputField(text = stringResource(id = R.string.forgot_password_error_invalid_email))
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlassOutlinedTextField(
                    modifier = Modifier.width(160.dp),
                    value = codeNumber.value,
                    onValueChange = { newValue: String ->
                        codeNumber.value = newValue
                        isCodeError = false
                    },
                    placeholder = {
                        Text(text = stringResource(R.string.code_number))
                    },
                    label = {
                        Text(text = stringResource(id = R.string.input_code_number))
                    },
                    isError = isCodeError,
                    supportingText = {
                        if (isCodeError) {
                            ErrorTextInputField(text = stringResource(id = R.string.code_number_error))
                        }
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start),
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
                Spacer(modifier = Modifier.width(AppTheme.dimens.paddingExtraSmall))
                SendVerifyContactCodeButton(
                    loginComponent = loginComponent,
                    confirmMode = confirmMode,
                    email = { email.value.trim() },
                    onInvalidEmail = { isEmailError = true }
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            Button(
                onClick = {
                    val code = codeNumber.value.trim()
                    isCodeError = !StringUtils.isValidCode(code)
                    if (confirmMode) {
                        if (!isCodeError) {
                            loginComponent.confirmEmail(
                                code = code,
                                onSuccess = {
                                    ActionUtils.showToast(R.string.profile_email_verified_success)
                                    onVerified()
                                },
                                onFail = { ActionUtils.showError(it) }
                            )
                        }
                    } else {
                        val trimmedEmail = email.value.trim()
                        isEmailError = !StringUtils.isValidEmail(trimmedEmail)
                        if (!isEmailError && !isCodeError) {
                            loginComponent.bindEmail(
                                email = trimmedEmail,
                                code = code,
                                onSuccess = {
                                    ActionUtils.showToast(R.string.profile_email_verified_success)
                                    onVerified()
                                },
                                onFail = { ActionUtils.showError(it) }
                            )
                        }
                    }
                },
                colors = AppTheme.colors.getPrimaryButtonColors()
            ) {
                Text(
                    text = stringResource(id = R.string.button_confirm),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            // 海外渠道不提供绑定手机功能
            if (LoginChannelConfig.getConfigByFlavor().bindPhoneSupported) {
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingExtraSmall))
                GlassTonalButton(
                    onClick = {
                        AppEventBus.emit(
                            BindPhoneEvent(
                                source = "VerifyContactSheet",
                                onSuccess = onVerified,
                            )
                        )
                        onDismiss()
                    },
                    colors = AppTheme.colors.getSecondaryContainerButtonColors()
                ) {
                    Text(text = stringResource(id = R.string.verify_contact_by_phone))
                }
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        }
    }
}

@Composable
private fun SendVerifyContactCodeButton(
    loginComponent: LoginComponent,
    confirmMode: Boolean,
    email: () -> String,
    onInvalidEmail: () -> Unit,
) {
    val countdown = remember { mutableIntStateOf(0) }
    var isSending by remember { mutableStateOf(false) }

    if (countdown.intValue > 0) {
        LaunchedEffect(countdown.intValue) {
            delay(1000L)
            countdown.intValue -= 1
        }
    }

    GlassTonalButton(
        onClick = {
            if (isSending) return@GlassTonalButton
            val onError: (String) -> Unit = { error: String ->
                ActionUtils.showError(error)
                isSending = false
            }
            val onSuccess: () -> Unit = {
                ActionUtils.showToast(R.string.forgot_password_code_sent)
                countdown.intValue = 60
                isSending = false
            }
            if (confirmMode) {
                isSending = true
                loginComponent.sendConfirmEmailCode(onError = onError, onSuccess = onSuccess)
            } else {
                val trimmedEmail = email()
                if (StringUtils.isValidEmail(trimmedEmail)) {
                    isSending = true
                    loginComponent.sendBindEmailCode(
                        email = trimmedEmail,
                        onError = onError,
                        onSuccess = onSuccess
                    )
                } else {
                    onInvalidEmail()
                }
            }
        },
        enabled = countdown.intValue == 0 && !isSending,
        colors = AppTheme.colors.getSecondaryContainerButtonColors()
    ) {
        if (isSending) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = if (countdown.intValue > 0) {
                    stringResource(R.string.get_code_ed) + " (${countdown.intValue}s)"
                } else {
                    stringResource(R.string.get_code)
                }
            )
        }
    }
}

/** 当前账号绑定的真实邮箱(排除短信/ Google 一键登录的占位邮箱), 未绑定返回 null */
private fun boundRealEmail(): String? {
    val email = TokenStorage.getLoginInfo()?.user?.email.orEmpty()
    if (!StringUtils.isValidEmail(email)) return null
    if (email.endsWith("@example.com") || email.endsWith("@google.user")) return null
    return email
}
