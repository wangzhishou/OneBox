package com.wanbaohe.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.shifenmiao.base.ui.PasswordTextField
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.delay

/**
 * 修改密码底部弹层:已绑手机走短信验证码,未绑手机但有真实邮箱走邮箱验证码
 */
@Composable
fun ChangePasswordSheet(
    loginComponent: LoginComponent,
    onDismiss: () -> Unit,
) {
    val loginState = LocalLoginState.current
    val usePhone = loginState.phone.isNotEmpty()
    val account = if (usePhone) maskPhone(loginState.phone) else loginState.emailOrMobile
    val codeNumber = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val confirmPassword = remember { mutableStateOf("") }
    var isCodeError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        dragHandle = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_change_password),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                },
                actions = {
                }
            )
        },
        enableBackHandler = true,
        enableBottomContentWeight = false
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .fillMaxSize()
                .padding(horizontal = AppTheme.dimens.paddingNormal)
        ) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            Text(
                text = stringResource(R.string.profile_code_send_to, account),
                style = MaterialTheme.typography.labelMedium.copy(
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
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
                SendChangePasswordCodeButton(
                    usePhone = usePhone,
                    phone = loginState.phone,
                    loginComponent = loginComponent
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                value = newPassword.value,
                onValueChange = {
                    newPassword.value = it
                    isPasswordError = false
                },
                label = stringResource(R.string.profile_new_password),
                isError = isPasswordError,
                errorText = stringResource(R.string.profile_password_min_length),
                imeAction = ImeAction.Next
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            PasswordTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPassword.value,
                onValueChange = {
                    confirmPassword.value = it
                    isConfirmError = false
                },
                label = stringResource(R.string.profile_confirm_new_password),
                isError = isConfirmError,
                errorText = stringResource(R.string.profile_password_mismatch),
            )
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            Button(
                onClick = {
                    val code = codeNumber.value.trim()
                    val password = newPassword.value
                    isCodeError = !StringUtils.isValidCode(code)
                    isPasswordError = password.length < 6
                    isConfirmError = !isPasswordError && confirmPassword.value != password
                    if (!isCodeError && !isPasswordError && !isConfirmError) {
                        loginComponent.changePassword(
                            code = code,
                            newPassword = password,
                            onSuccess = {
                                ActionUtils.showToast(R.string.profile_password_changed)
                                onDismiss()
                            },
                            onFail = {
                                ActionUtils.showError(it)
                            }
                        )
                    }
                },
                colors = AppTheme.colors.getPrimaryButtonColors()
            ) {
                Text(
                    text = stringResource(id = R.string.button_confirm),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SendChangePasswordCodeButton(
    usePhone: Boolean,
    phone: String,
    loginComponent: LoginComponent,
) {
    val countdown = remember { mutableIntStateOf(0) }

    if (countdown.intValue > 0) {
        LaunchedEffect(countdown.intValue) {
            delay(1000L)
            countdown.intValue -= 1
        }
    }

    GlassTonalButton(
        onClick = {
            val onSendSuccess = {
                if (!usePhone) {
                    ActionUtils.showToast(R.string.forgot_password_code_sent)
                }
                countdown.intValue = 60
            }
            if (usePhone) {
                loginComponent.sendCode(
                    value = phone,
                    sendType = 0,
                    onError = {
                        ActionUtils.showError(it)
                    },
                    onSuccess = onSendSuccess
                )
            } else {
                loginComponent.sendChangePasswordEmailCode(
                    onError = {
                        ActionUtils.showError(it)
                    },
                    onSuccess = onSendSuccess
                )
            }
        },
        enabled = countdown.intValue == 0,
        colors = AppTheme.colors.getSecondaryContainerButtonColors()
    ) {
        Text(
            text = if (countdown.intValue > 0) {
                stringResource(R.string.get_code_ed) + " (${countdown.intValue}s)"
            } else {
                stringResource(R.string.get_code)
            }
        )
    }
}

private fun maskPhone(phone: String): String {
    return if (phone.length >= 7) {
        phone.take(3) + "****" + phone.takeLast(4)
    } else {
        phone
    }
}
