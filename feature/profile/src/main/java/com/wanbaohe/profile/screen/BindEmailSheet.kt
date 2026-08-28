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
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import kotlinx.coroutines.delay

/**
 * 绑定邮箱底部弹层:输入新邮箱 → 发验证码 → 输入验证码 → 绑定
 */
@Composable
fun BindEmailSheet(
    loginComponent: LoginComponent,
    onDismiss: () -> Unit,
) {
    val email = remember { mutableStateOf("") }
    val codeNumber = remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var isCodeError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        dragHandle = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile_bind_email),
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
                SendBindEmailCodeButton(
                    email = email.value.trim(),
                    loginComponent = loginComponent,
                    validatedFail = {
                        isEmailError = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
            Button(
                onClick = {
                    val trimmedEmail = email.value.trim()
                    val code = codeNumber.value.trim()
                    isEmailError = !StringUtils.isValidEmail(trimmedEmail)
                    isCodeError = !StringUtils.isValidCode(code)
                    if (!isEmailError && !isCodeError) {
                        loginComponent.bindEmail(
                            email = trimmedEmail,
                            code = code,
                            onSuccess = {
                                ActionUtils.showToast(R.string.profile_email_bound)
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
                    text = stringResource(id = R.string.button_bind),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SendBindEmailCodeButton(
    email: String,
    loginComponent: LoginComponent,
    validatedFail: () -> Unit,
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
            if (StringUtils.isValidEmail(email)) {
                loginComponent.sendBindEmailCode(
                    email = email,
                    onError = {
                        ActionUtils.showError(it)
                    },
                    onSuccess = {
                        ActionUtils.showToast(R.string.forgot_password_code_sent)
                        countdown.intValue = 60
                    }
                )
            } else {
                validatedFail.invoke()
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
