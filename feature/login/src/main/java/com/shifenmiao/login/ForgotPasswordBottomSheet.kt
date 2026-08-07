package com.shifenmiao.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.ui.button.SmallClickableWithIconAndText
import com.shifenmiao.base.ui.TitleText
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.state.ForgotPasswordUiEvent
import com.shifenmiao.login.ui.ForgotPasswordInputs
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun ForgotPasswordBottomSheet(
    loginComponent: LoginComponent,
    onNavigateBack: () -> Unit
) {
    val forgotPasswordState by remember {
        loginComponent.forgotPasswordState
    }

    if (forgotPasswordState.isResetSuccessful) {
        LaunchedEffect(key1 = true) {
            ActionUtils.showToast(R.string.forgot_password_success)
            onNavigateBack.invoke()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        SmallClickableWithIconAndText(
            modifier = Modifier
                .padding(horizontal = AppTheme.dimens.paddingLarge)
                .padding(top = AppTheme.dimens.paddingLarge),
            iconContentDescription = stringResource(id = R.string.navigate_back),
            iconVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
            text = stringResource(id = R.string.back_to_login),
            onClick = onNavigateBack
        )

        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingLarge)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.paddingLarge)
                    .padding(bottom = AppTheme.dimens.paddingExtraLarge)
            ) {
                TitleText(
                    modifier = Modifier.padding(top = AppTheme.dimens.paddingLarge),
                    text = stringResource(id = R.string.forgot_password_title)
                )

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
}
