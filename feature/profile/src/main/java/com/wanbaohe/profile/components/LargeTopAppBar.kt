package com.wanbaohe.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.ConfirmContentDialog
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.PersonAdd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentPaste

@Composable
fun InvitationCodeAction(
    loginComponent: LoginComponent
) {
    val loginState = LocalLoginState.current

    val showDialog = remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .clickable {
                if (loginState.isLogin) {
                    showDialog.value = true
                } else {
                    AppToastHost.showToast(AppContext.getString(R.string.login_first))
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.profile_user_info_invitation_code),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PersonAdd,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    if (showDialog.value) {
        ShowInvitationCodeDialog(
            isVisible = showDialog,
            onConfirmHandle = { invitationCode ->
                if (invitationCode.trim() == loginState.invitationCode) {
                    AppToastHost.showToast(AppContext.getString(R.string.profile_invitation_code_same))
                } else if (StringUtils.validateInvitationCode(invitationCode)) {
                    loginComponent.applyInvitationCode(
                        invitationCode = invitationCode,
                        onSuccess = {
                            showDialog.value = false
                        },
                        onFail = { tips ->
                            AppToastHost.showToast(tips)
                        }
                    )
                } else {
                    AppToastHost.showToast(AppContext.getString(R.string.profile_invitation_code_invalid))

                }
            },
            onDismissHandle = {
                // Optional: Handle dialog dismissal if needed
            }
        )
    }
}

@Composable
fun ShowInvitationCodeDialog(
    isVisible: MutableState<Boolean>,
    onConfirmHandle: (String) -> Unit = {},
    onDismissHandle: (() -> Unit)? = null
) {
    var invitationCode by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    val loginState = LocalLoginState.current

    if (isVisible.value) {
        ConfirmContentDialog(
            showDialog = isVisible,
            onDismiss = {
                onDismissHandle?.invoke()
                isVisible.value = false
            },
            title = stringResource(R.string.profile_input_invitation_code),
            confirmButtonText = stringResource(id = R.string.button_confirm),
            dismissButtonText = stringResource(id = R.string.button_cancel),
            onConfirm = {
                onConfirmHandle(invitationCode)
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.copy_invitation_code_hint),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            Clipboard.copy(loginState.invitationCode)
                        },
                        colors = AppTheme.colors.getSecondaryContainerButtonColors()
                    ) {
                        Icon(
                            modifier = Modifier.size(14.dp),
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = "invitationCode",
                        )
                        Spacer(modifier = Modifier.size(3.dp))
                        SelectionContainer {
                            Text(
                                text = loginState.invitationCode,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(AppTheme.dimens.paddingNormal))
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = invitationCode,
                    onValueChange = {
                        if (it.length <= 8) {
                            invitationCode = it
                            showError = false
                        } else {
                            showError = true
                        }
                    },
                    supportingText = {
                        if (showError) {
                            Text(
                                text = stringResource(R.string.profile_invitation_code_hint),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.input_invitation_code),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = AppTheme.shapes.getTextFieldShape(),
                    colors = AppTheme.colors.getOutlinedTextFieldColors(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                Clipboard.getText{ text ->
                                    invitationCode = text
                                }
                            }
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineContentPaste,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                )
            }
        }
    }
}