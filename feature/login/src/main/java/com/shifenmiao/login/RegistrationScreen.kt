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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.AppCenterAlignedTopAppBar
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AppTheme.dimens.paddingLarge),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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