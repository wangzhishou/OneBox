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
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.core.R
import com.shifenmiao.login.state.RegistrationUiEvent
import com.shifenmiao.login.ui.RegistrationInputs
import com.shifenmiao.login.viewModel.LoginComponent
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack

@Composable
fun RegistrationBottomSheet(
    loginComponent: LoginComponent,
    onNavigateBack: () -> Unit,
    onNavigateToAuthenticatedRoute: () -> Unit
) {

    val registrationState by remember {
        loginComponent.registrationState
    }

    if (registrationState.isRegistrationSuccessful) {
        LaunchedEffect(key1 = true) {
            onNavigateToAuthenticatedRoute.invoke()
        }
    } else {
        // Full Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {

            // Back Button Icon
            SmallClickableWithIconAndText(
                modifier = Modifier
                    .padding(horizontal = AppTheme.dimens.paddingLarge)
                    .padding(top = AppTheme.dimens.paddingLarge),
                iconContentDescription = stringResource(id = R.string.navigate_back),
                iconVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                text = stringResource(id = R.string.back_to_login),
                onClick = onNavigateBack
            )


            // Main card Content for Registration
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

                    // Heading Registration
                    TitleText(
                        modifier = Modifier.padding(top = AppTheme.dimens.paddingLarge),
                        text = stringResource(id = R.string.registration_heading_text)
                    )

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
                            loginComponent.onRegistrationUiEvent(registrationUiEvent = RegistrationUiEvent.Submit)
                        }
                    )
                }
            }
        }
    }

}
