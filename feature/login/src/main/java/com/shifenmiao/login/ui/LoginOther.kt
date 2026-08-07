@file:Suppress("DEPRECATION")

package com.shifenmiao.login.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.entrypoint.ChannelConfigEntryPoint
import com.shifenmiao.base.ui.BoxHorizontalDivider
import com.shifenmiao.base.ui.button.PrimaryButton
import com.shifenmiao.base.ui.button.SecondaryButton
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.core.R
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineProfile
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSecurity

@Composable
fun LoginOther(
    loginState: LoginState,
    loginComponent: LoginComponent,
    isUserAgreementChecked: MutableState<Boolean>,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: (((() -> Unit)) -> Unit)
) {
    val context = LocalContext.current
    val channelConfig: ChannelConfig = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChannelConfigEntryPoint::class.java
        ).getChannelConfig()
    }
    val enableWechat = channelConfig.enableWechat

    val wechatConfirmFunction = {
        loginComponent.loginByWechat()
    }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("335920196432-dmphr6uuv6rjhvf0gh8cg3qjt23ivccu.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                loginComponent.loginByGoogle(idToken)
            } else {
                ActionUtils.showToast("Google login failed: no id token")
            }
        } catch (e: ApiException) {
            if (e.statusCode != 12501) { // 12501 = USER_CANCELED
                ActionUtils.showToast("Google login failed: ${e.statusCode}")
            }
        }
    }

    val googleConfirmFunction = {
        googleSignInLauncher.launch(googleSignInClient.signInIntent)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = AppTheme.dimens.paddingLarge)
    ) {

        BoxHorizontalDivider {
            UserAgreement(
                isUserAgreementChecked = isUserAgreementChecked,
            )
        }

        if (enableWechat) {
            TextButton(
                enabled = !loginState.isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(AppTheme.dimens.normalButtonHeight),
                onClick = {
                    if (isUserAgreementChecked.value.not()) {
                        setConfirmAction(wechatConfirmFunction)
                        showAgreementDialog.value = true
                        return@TextButton
                    }
                    wechatConfirmFunction.invoke()
                },
                colors = ButtonColors(
                    containerColor = Color(0xFF07c160),
                    contentColor = Color(0xFFFFFFFF),
                    disabledContainerColor = Color(0xFF07c160),
                    disabledContentColor = Color(0xFFF5F5F5)
                )
            ) {
                if (loginState.isLoggingIn) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(24.dp)
                            .width(24.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.wechat),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(24.dp)
                            .width(24.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.wechat_login),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if (channelConfig.enableGms || CoreUtils.isOneBoxDebug()) {
            TextButton(
                enabled = !loginState.isLoggingIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(AppTheme.dimens.normalButtonHeight),
                onClick = {
                    if (isUserAgreementChecked.value.not()) {
                        setConfirmAction(googleConfirmFunction)
                        showAgreementDialog.value = true
                        return@TextButton
                    }
                    googleConfirmFunction.invoke()
                },
                colors = ButtonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color(0xFF3C4043),
                    disabledContainerColor = Color(0xFFFFFFFF),
                    disabledContentColor = Color(0xFF9AA0A6)
                )
            ) {
                if (loginState.isLoggingIn) {
                    CircularProgressIndicator(
                        color = Color(0xFF3C4043),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(24.dp)
                            .width(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineProfile,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(24.dp)
                            .width(24.dp)
                    )
                }
                Text(
                    text = stringResource(id = R.string.google_login),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun LoginAgreementDialog(
    showAgreementDialog: MutableState<Boolean>,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val isUserAgreementChecked = remember { mutableStateOf(!CoreUtils.isHuawei()) }

    val coroutineScope = rememberCoroutineScope()
    if (showAgreementDialog.value) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = {
                showAgreementDialog.value = false
                onDismiss()
            },
            title = { Text(stringResource(R.string.login_agreement_title)) },
            text = {
                Row(modifier = Modifier.padding(vertical = AppTheme.dimens.paddingNormal)) {
                    UserAgreement(isUserAgreementChecked = isUserAgreementChecked)
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = stringResource(R.string.button_confirm),
                    onClick = {
                        if (isUserAgreementChecked.value) {
                            onConfirm()
                            showAgreementDialog.value = false
                        } else {
                            coroutineScope.launch {
                                AppToastHost.showToast(
                                    com.t8rin.imagetoolbox.core.utils.appContext.getString(
                                        R.string.please_agree_to_the_user_agreement
                                    )
                                )
                            }
                        }
                    }
                )
            },
            dismissButton = {
                SecondaryButton(
                    text = stringResource(R.string.button_cancel),
                    onClick = {
                        showAgreementDialog.value = false
                        onDismiss()
                    })
            },
            icon = {
                Icon(imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSecurity, contentDescription = null)
            }
        )
    }
}