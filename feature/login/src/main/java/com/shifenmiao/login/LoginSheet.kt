package com.shifenmiao.login


import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.model.login.LoginStyle
import com.shifenmiao.login.viewModel.LoginComponent
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet

@Composable
fun ModalBottomSheetLogin(
    loginComponent: LoginComponent
) {
    val loginState = LocalLoginState.current
    if (loginState.showLogin) {
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = {
                loginComponent.hideLoginModal()
            },
            dragHandle = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.login_heading_text),
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
            when (loginState.loginStyle) {
                LoginStyle.LOGIN -> {
                    LoginBottomSheet(
                        onGoBack = {
                            loginComponent.hideLoginModal()
                        },
                        loginComponent = loginComponent
                    )
                }

                LoginStyle.REGISTRATION -> {
                    RegistrationBottomSheet(
                        loginComponent,
                        onNavigateBack = {
                            loginComponent.hideRegistration()
                        }, onNavigateToAuthenticatedRoute = {
                            loginComponent.hideLoginModal()
                        })
                }

                LoginStyle.FORGOT -> {
                    ForgotPasswordBottomSheet(
                        loginComponent = loginComponent,
                        onNavigateBack = {
                            loginComponent.hideForgotPassword()
                        }
                    )
                }
            }
        }
    }
}