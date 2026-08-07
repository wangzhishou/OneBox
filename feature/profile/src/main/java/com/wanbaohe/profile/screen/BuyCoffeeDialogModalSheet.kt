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
import com.shifenmiao.base.ui.button.SmallSecondaryButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.pay.PayState
import com.shifenmiao.model.state.UIState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
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
        dragHandle = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.need_coffee),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                },
                actions = {
                    SmallSecondaryButton(
                        text = stringResource(id = R.string.free_coffee),
                        onClick = {
                            ActionUtils.showLogin(
                                source = "BuyCoffeeDialogModalSheet",
                            ) {
                                showDialog.value = true
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(AppTheme.dimens.paddingNormal))
                }
            )
        },
        enableBackHandler = true,
        enableBottomContentWeight = false
    ) {
        BuyCoffeeBody(
            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
            loginComponent = loginComponent,
            payComponent = payComponent
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