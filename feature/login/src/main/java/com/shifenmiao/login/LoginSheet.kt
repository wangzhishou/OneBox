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


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.shifenmiao.core.R
import com.wanbaohe.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.model.login.LoginStyle
import com.shifenmiao.login.viewModel.LoginComponent
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet

@Composable
fun ModalBottomSheetLogin(
    loginComponent: LoginComponent,
    onNavigateToAddEngine: (() -> Unit)? = null
) {
    val loginState = LocalLoginState.current
    if (loginState.showLogin) {
        EnhancedModalBottomSheet(
            visible = true,
            onDismiss = {
                loginComponent.hideLoginModal()
            },
            title = stringResource(R.string.login_heading_text),
            enableBackHandler = true,
        ) {
            when (loginState.loginStyle) {
                LoginStyle.LOGIN -> {
                    LoginBottomSheet(
                        onGoBack = {
                            loginComponent.hideLoginModal()
                        },
                        loginComponent = loginComponent,
                        onNavigateToAddEngine = onNavigateToAddEngine
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