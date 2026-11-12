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

package com.wanbaohe.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.wanbaohe.base.ui.button.SecondarySmallButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.pay.PayState
import com.shifenmiao.model.state.UIState
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.utils.appContext
import com.wanbaohe.profile.components.NeedCoffeeTextInfo
import com.wanbaohe.profile.components.ShowInvitationCodeDialog
import com.wanbaohe.profile.viewmodel.PayComponent
import kotlinx.coroutines.launch

@Composable
fun BuyCoffeeDialogModalSheet(
    appComponent: AppComponent,
    loginComponent: LoginComponent,
    payComponent: PayComponent,
    uiState: UIState,
) {
    val showDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val payUIState = payComponent.payUIState
    if (payUIState.payState == PayState.SUCCESS) {
        appComponent.hideBuyCoffeeDialogModalSheet()
        payComponent.setPayDefaultState()
    }
    EnhancedModalBottomSheet(
        visible = uiState.showBuyCoffeeDialog,
        onDismiss = {
            appComponent.hideBuyCoffeeDialogModalSheet()
        },
        title = stringResource(id = R.string.need_coffee),
        endContent = {
            SecondarySmallButton(
                text = stringResource(id = R.string.free_coffee),
                onClick = {
                    ActionUtils.showLogin(
                        source = "BuyCoffeeDialogModalSheet",
                    ) {
                        showDialog.value = true
                    }
                }
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.paddingSmall))
        },
        enableBackHandler = true,
    ) {
        BuyCoffeeBody(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            loginComponent = loginComponent,
            payComponent = payComponent,
            onNavigateToAdWatch = {
                appComponent.hideBuyCoffeeDialogModalSheet()
                appComponent.onNavigate(com.wanbaohe.core.ui.utils.navigation.Screen.AdWatch)
            },
            onNavigateToAddEngine = {
                appComponent.hideBuyCoffeeDialogModalSheet()
                appComponent.onNavigate(
                    com.wanbaohe.core.ui.utils.navigation.Screen.AISettings(
                        com.wanbaohe.core.ui.utils.navigation.Screen.AISettings.Type.AddEngine()
                    )
                )
            }
        ) {
            NeedCoffeeTextInfo()
        }
    }

    if (showDialog.value) {
        ShowInvitationCodeDialog(
            isVisible = showDialog,
            onConfirmHandle = { invitationCode ->
                if (StringUtils.validateInvitationCode(invitationCode)) {
                    loginComponent.applyInvitationCode(
                        invitationCode = invitationCode,
                        onSuccess = {
                            showDialog.value = false
                        },
                        onFail = { tips ->
                            coroutineScope.launch {
                                AppToastHost.showToast(tips)
                            }
                        }
                    )
                } else {
                    coroutineScope.launch {
                        AppToastHost.showToast(appContext.getString(R.string.profile_invitation_code_invalid))
                    }
                }
            },
            onDismissHandle = {
                // Optional: Handle dialog dismissal if needed
            }
        )
    }
}