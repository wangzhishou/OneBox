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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.button.SmallClickableWithIconAndText
import com.wanbaohe.base.ui.TitleText
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.core.R
import com.shifenmiao.login.state.RegistrationUiEvent
import com.shifenmiao.login.ui.RegistrationInputs
import com.shifenmiao.login.viewModel.LoginComponent
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.wanbaohe.core.ui.widget.glass.GlassCard

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
            GlassCard(
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
