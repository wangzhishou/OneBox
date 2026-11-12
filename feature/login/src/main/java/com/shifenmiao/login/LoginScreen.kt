/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
import com.wanbaohe.core.ui.utils.provider.LocalLoginState
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
                    setConfirmAction = { action -> currentConfirmAction.value = action },
                    onNavigateToAddEngine = {
                        // 先退出登录页再进服务与模型, 返回时直达原页面
                        onGoBack()
                        appComponent.onNavigate(
                            com.wanbaohe.core.ui.utils.navigation.Screen.AISettings()
                        )
                    }
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


