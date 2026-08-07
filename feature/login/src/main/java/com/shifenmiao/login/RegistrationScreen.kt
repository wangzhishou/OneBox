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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.base.ui.AppCenterAlignedTopAppBar
import com.shifenmiao.base.ui.BoxHorizontalDivider
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.core.R
import com.shifenmiao.login.state.RegistrationUiEvent
import com.shifenmiao.login.ui.RegistrationInputs
import com.shifenmiao.login.ui.UserAgreement
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme

@Composable
fun RegistrationScreen(
    loginComponent: LoginComponent,
    onGoBack: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val registrationState by remember {
        loginComponent.registrationState
    }

    val isUserAgreementChecked = remember { mutableStateOf(false) }

    if (registrationState.isRegistrationSuccessful) {
        LaunchedEffect(key1 = true) {
            onLoginSuccess.invoke()
            onGoBack.invoke()
        }
    } else {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        // Full Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            AppCenterAlignedTopAppBar(
                titleText = stringResource(id = R.string.registration_heading_text),
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
                /**
                 * Registration Inputs Composable
                 */
                RegistrationInputs(
                    registrationState = registrationState,
                    onEmailIdChange = { inputString ->
                        loginComponent.onRegistrationUiEvent(
                            registrationUiEvent = RegistrationUiEvent.EmailChanged(
                                inputValue = inputString
                            )
                        )
                    },
                    onPasswordChange = { inputString ->
                        loginComponent.onRegistrationUiEvent(
                            registrationUiEvent = RegistrationUiEvent.PasswordChanged(
                                inputValue = inputString
                            )
                        )
                    },
                    onConfirmPasswordChange = { inputString ->
                        loginComponent.onRegistrationUiEvent(
                            registrationUiEvent = RegistrationUiEvent.ConfirmPasswordChanged(
                                inputValue = inputString
                            )
                        )
                    },
                    onSubmit = {
                        if (isUserAgreementChecked.value.not()) {
                            ActionUtils.showToast(R.string.please_agree_to_the_user_agreement)
                            return@RegistrationInputs
                        }
                        loginComponent.onRegistrationUiEvent(registrationUiEvent = RegistrationUiEvent.Submit)
                    }
                )

                BoxHorizontalDivider {
                    UserAgreement(
                        isUserAgreementChecked = isUserAgreementChecked,
                    )
                }
            }
        }
    }

    BackHandler {
        loginComponent.reset()
        onGoBack()
    }
}