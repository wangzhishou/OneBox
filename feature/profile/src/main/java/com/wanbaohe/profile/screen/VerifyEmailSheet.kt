package com.wanbaohe.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
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
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.delay

/**
 * 验证邮箱底部弹层:向当前账号绑定的邮箱发验证码 → 输入验证码 → 确认验证
 */
@Composable
fun VerifyEmailSheet(
    loginComponent: LoginComponent,
    onDismiss: () -> Unit,
) {
    val loginState = LocalLoginState.current
    val email = loginState.emailOrMobile
    val codeNumber = remember { mutableStateOf("") }
    var isCodeError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        dragHandle = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_verify_email),
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
                text = stringResource(R.string.profile_code_send_to, email),
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
                SendConfirmEmailCodeButton(loginComponent = loginComponent)
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            Button(
                onClick = {
                    val code = codeNumber.value.trim()
                    isCodeError = !StringUtils.isValidCode(code)
                    if (!isCodeError) {
                        loginComponent.confirmEmail(
                            code = code,
                            onSuccess = {
                                ActionUtils.showToast(R.string.profile_email_verified_success)
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
private fun SendConfirmEmailCodeButton(
    loginComponent: LoginComponent,
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
            isSending = true
            loginComponent.sendConfirmEmailCode(
                onError = { error: String ->
                    ActionUtils.showError(error)
                    isSending = false
                },
                onSuccess = {
                    ActionUtils.showToast(R.string.forgot_password_code_sent)
                    countdown.intValue = 60
                    isSending = false
                }
            )
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
