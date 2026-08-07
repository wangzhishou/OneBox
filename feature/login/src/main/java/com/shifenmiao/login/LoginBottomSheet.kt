package com.shifenmiao.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.login.state.LoginShowType
import com.shifenmiao.login.ui.LoginAgreementDialog
import com.shifenmiao.login.ui.LoginBody
import com.shifenmiao.login.ui.LoginOther
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme


@Composable
fun LoginBottomSheet(
    onGoBack: () -> Unit,
    loginComponent: LoginComponent
) {
    val loginState = LocalLoginState.current
    val isUserAgreementChecked = remember { mutableStateOf(false) }
    val showAgreementDialog = remember { mutableStateOf(false) }
    val currentConfirmAction = remember { mutableStateOf<(() -> Unit)?>(null) }
    LoginAgreementDialog(
        showAgreementDialog = showAgreementDialog,
        onConfirm = {
            isUserAgreementChecked.value = true
            currentConfirmAction.value?.invoke()
        }
    )
    Surface(
        color = MaterialTheme.colorScheme.surface
    ) {        // Full Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .padding(vertical = AppTheme.dimens.paddingExtraLarge)
            ) {
                LoginBody(
                    loginState = loginState,
                    loginComponent = loginComponent,
                    loginShowType = LoginShowType.BOTTOM_SHEET,
                    isUserAgreementChecked = isUserAgreementChecked,
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

    BackHandler(onBack = onGoBack)

}