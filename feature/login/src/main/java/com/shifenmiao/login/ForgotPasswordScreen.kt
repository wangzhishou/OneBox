package com.shifenmiao.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.ui.AppCenterAlignedTopAppBar
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.state.ForgotPasswordUiEvent
import com.shifenmiao.login.ui.ForgotPasswordInputs
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme

@Composable
fun ForgotPasswordScreen(
    loginComponent: LoginComponent,
    onGoBack: () -> Unit = {}
) {
    val forgotPasswordState by remember {
        loginComponent.forgotPasswordState
    }

    if (forgotPasswordState.isResetSuccessful) {
        LaunchedEffect(key1 = true) {
            ActionUtils.showToast(R.string.forgot_password_success)
            onGoBack.invoke()
        }
    }

    BackHandler {
        onGoBack.invoke()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        AppCenterAlignedTopAppBar(
            titleText = stringResource(id = R.string.forgot_password_title),
            onBackClick = onGoBack,
            scrollBehavior = scrollBehavior
        )
        Column(
            modifier = Modifier
                .padding(
                    start = AppTheme.dimens.paddingLarge,
                    end = AppTheme.dimens.paddingLarge,
                    bottom = AppTheme.dimens.paddingLarge
                )
        ) {
            ForgotPasswordInputs(
                forgotPasswordState = forgotPasswordState,
                onEmailChange = { inputString ->
                    loginComponent.onForgotPasswordUiEvent(
                        ForgotPasswordUiEvent.EmailChanged(inputValue = inputString)
                    )
                },
                onCodeChange = { inputString ->
                    loginComponent.onForgotPasswordUiEvent(
                        ForgotPasswordUiEvent.CodeChanged(inputValue = inputString)
                    )
                },
                onNewPasswordChange = { inputString ->
                    loginComponent.onForgotPasswordUiEvent(
                        ForgotPasswordUiEvent.NewPasswordChanged(inputValue = inputString)
                    )
                },
                onConfirmPasswordChange = { inputString ->
                    loginComponent.onForgotPasswordUiEvent(
                        ForgotPasswordUiEvent.ConfirmPasswordChanged(inputValue = inputString)
                    )
                },
                onSendCode = {
                    loginComponent.onForgotPasswordUiEvent(ForgotPasswordUiEvent.SendCode)
                },
                onSubmit = {
                    loginComponent.onForgotPasswordUiEvent(ForgotPasswordUiEvent.Submit)
                }
            )
        }
    }
}
