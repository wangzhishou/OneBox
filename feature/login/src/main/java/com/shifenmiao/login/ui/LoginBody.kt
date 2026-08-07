package com.shifenmiao.login.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.EmailTextField
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.state.LoginShowType
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.model.login.LoginType
import com.shifenmiao.login.state.LoginUiEvent
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.logger.Logger.makeLog
import com.t8rin.imagetoolbox.core.resources.icons.CheckCircle

@Composable
fun LoginBody(
    loginState: LoginState,
    loginComponent: LoginComponent,
    loginShowType: LoginShowType = LoginShowType.SCREEN,
    isUserAgreementChecked: MutableState<Boolean>,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: ((()->Unit)) -> Unit
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .padding(horizontal = AppTheme.dimens.paddingLarge)
    ) {
        val options = loginComponent.getLoginList()
        var selectedLoginType by remember { mutableStateOf(loginComponent.getLoginType()) }
        GlassSegmentedButtonRow(
            options = options,
            selectedOption = selectedLoginType,
            onOptionSelected = { selectedLoginType = it },
            modifier = Modifier.fillMaxWidth(),
            buttonHeight = AppTheme.dimens.normalButtonHeight,
            selectedIcon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            label = { loginType ->
                Text(
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    text = loginType.title,
                    style = MaterialTheme.typography.titleMedium,
                )
            },
        )

        Spacer(modifier = Modifier.padding(AppTheme.dimens.paddingNormal))
        when (selectedLoginType) {
            LoginType.EMAIL_LOGIN -> {
                EmailLoginBox(
                    loginShowType = loginShowType,
                    loginState = loginState,
                    loginComponent = loginComponent,
                    isUserAgreementChecked = isUserAgreementChecked,
                    showAgreementDialog = showAgreementDialog,
                    setConfirmAction = setConfirmAction
                )
            }
            LoginType.CODE_LOGIN -> {
                CodeLoginBox(
                    loginState = loginState,
                    loginComponent = loginComponent,
                    isUserAgreementChecked = isUserAgreementChecked,
                    showAgreementDialog = showAgreementDialog,
                    setConfirmAction = setConfirmAction
                )
            }
            LoginType.PHONE_LOGIN -> {
                PhoneLoginBox(
                    loginState = loginState,
                    loginComponent = loginComponent,
                    isUserAgreementChecked = isUserAgreementChecked,
                    showAgreementDialog = showAgreementDialog,
                    setConfirmAction = setConfirmAction
                )
            }
        }
    }
}

@Composable
fun PhoneLoginBox(
    loginState: LoginState,
    loginComponent: LoginComponent,
    isUserAgreementChecked: MutableState<Boolean>,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: ((()->Unit)) -> Unit
) {
    val phoneNumber = remember { mutableStateOf("") }
    val codeNumber = remember { mutableStateOf("") }
    val loginFun = {
        if (StringUtils.isValidChinesePhoneNumber(phoneNumber.value)
            && StringUtils.isValidCode(codeNumber.value)
        ) {
            loginComponent.loginByPhone(
                phone = phoneNumber.value,
                code = codeNumber.value,
                onSuccess = {
                },
                onFail = {
                    ActionUtils.showError(it)
                }
            )
        } else {
            ActionUtils.showError(R.string.please_enter_correct_phone_number_and_verification_code)
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        PhoneBody(
            phoneNumber = phoneNumber,
            codeNumber = codeNumber,
            loginComponent = loginComponent,
            sendType = 1
        )
        Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        Row(
            modifier = Modifier.padding(0.dp, AppTheme.dimens.paddingNormal),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Login Submit Button
            PrimaryButton(
                enable = !loginState.isLoggingIn,
                text = stringResource(id = R.string.login_button_text),
                onClick = {
                    if (isUserAgreementChecked.value.not()) {
                        showAgreementDialog.value = true
                        makeLog{
                            "showAgreementDialog ${showAgreementDialog.value}"
                        }
                        setConfirmAction(loginFun)
                        return@PrimaryButton
                    }
                    loginFun.invoke()
                }
            )
        }
    }
}

@Composable
fun CodeLoginBox(
    loginState: LoginState,
    loginComponent: LoginComponent,
    isUserAgreementChecked: MutableState<Boolean>,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: ((()->Unit)) -> Unit
) {
    val confirmFunction = {
        loginComponent.onUiEvent(loginUiEvent = LoginUiEvent.SubmitCode)
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        WeChatQRCodeBox()

        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.wechat_login_tips),
            textAlign = TextAlign.Start
        )

        val verificationCodeInputText = remember { mutableStateOf("") }

        EmailTextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = verificationCodeInputText.value,
            onValueChange = {
                verificationCodeInputText.value = it
                loginComponent.onUiEvent(
                    loginUiEvent = LoginUiEvent.VerificationCodeChanged(
                        it
                    )
                )
            },
            label = stringResource(id = R.string.login_verification_code_label),
            isError = loginState.errorState.verificationCodeErrorState.hasError,
            errorText = stringResource(id = loginState.errorState.verificationCodeErrorState.errorMessageStringResource)
        )

        Row(
            modifier = Modifier.padding(0.dp, AppTheme.dimens.paddingNormal),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Login Submit Button
            PrimaryButton(
                enable = !loginState.isLoggingIn,
                text = stringResource(id = R.string.login_button_text),
                onClick = {
                    if (isUserAgreementChecked.value.not()) {
                        setConfirmAction(confirmFunction)
                        showAgreementDialog.value = true
                        return@PrimaryButton
                    }
                    confirmFunction.invoke()
                }
            )
        }
    }
}

@Composable
fun EmailLoginBox(
    loginState: LoginState,
    loginComponent: LoginComponent,
    isUserAgreementChecked: MutableState<Boolean>,
    loginShowType: LoginShowType,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: ((()->Unit)) -> Unit
) {
    val onNavigate = LocalOnNavigate.current
    val confirmFunction = {
        loginComponent.onUiEvent(loginUiEvent = LoginUiEvent.Submit)
    }
    // Login Inputs Composable
    LoginInputs(
        loginState = loginState,
        onEmailOrMobileChange = { inputString ->
            loginComponent.onUiEvent(
                loginUiEvent = LoginUiEvent.EmailOrMobileChanged(
                    inputString
                )
            )
        },
        onPasswordChange = { inputString ->
            loginComponent.onUiEvent(
                loginUiEvent = LoginUiEvent.PasswordChanged(
                    inputString
                )
            )
        },
        onSubmit = {
            if (isUserAgreementChecked.value.not()) {
                setConfirmAction(confirmFunction)
                showAgreementDialog.value = true
                return@LoginInputs
            }
            confirmFunction.invoke()
        },

        onForgotPasswordClick = {
            loginComponent.showForgotPassword()
        },

        onNavigateToRegistration = {
            if (loginShowType == LoginShowType.SCREEN) {
                onNavigate(
                    Screen.Registration(
                        onLoginSuccess = {}
                    ))
            } else {
                loginComponent.showRegistration()
            }
        }
    )
}
