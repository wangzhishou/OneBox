package com.shifenmiao.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.login.LoginStyle
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.login.ui.LoginAgreementDialog
import com.shifenmiao.login.ui.LoginBody
import com.shifenmiao.login.ui.LoginOther
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme

@Composable
fun LoginScreen(
    loginComponent: LoginComponent,
    onGoBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    appComponent: AppComponent
) {
    val loginState = LocalLoginState.current
    val showAgreementDialog = remember { mutableStateOf(false) }
    val isUserAgreementChecked = remember { mutableStateOf(false) }
    val currentConfirmAction = remember { mutableStateOf<(() -> Unit)?>(null) }
    LoginAgreementDialog(
        showAgreementDialog = showAgreementDialog,
        onConfirm = {
            isUserAgreementChecked.value = true
            currentConfirmAction.value?.invoke()
        }
    )
    if (loginState.isLogin) {
        /**
         * Navigate to Authenticated navigation route
         * once login is successful
         */
        LaunchedEffect(key1 = true) {
            onLoginSuccess.invoke()
            onGoBack.invoke()
        }
    } else if (loginState.loginStyle == LoginStyle.FORGOT) {
        ForgotPasswordScreen(
            loginComponent = loginComponent,
            onGoBack = {
                loginComponent.hideForgotPassword()
            }
        )
    } else {
        // Full Screen Content
        BaseScreen(
            title = stringResource(id = R.string.login_heading_text),
            onGoBack = onGoBack
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.padding(AppTheme.dimens.paddingNormal))
                LoginBody(
                    isUserAgreementChecked = isUserAgreementChecked,
                    loginState = loginState,
                    loginComponent = loginComponent,
                    showAgreementDialog = showAgreementDialog,
                    setConfirmAction = { action -> currentConfirmAction.value = action }
                )

                LoginOther(
                    loginState = loginState,
                    loginComponent = loginComponent,
                    isUserAgreementChecked = isUserAgreementChecked,
                    showAgreementDialog = showAgreementDialog,
                    setConfirmAction = { action -> currentConfirmAction.value = action }
                )
            }
        }
    }

    BackHandler {
        if (loginState.loginStyle == LoginStyle.FORGOT) {
            loginComponent.hideForgotPassword()
        } else {
            loginComponent.reset()
            onGoBack()
        }
    }
}


