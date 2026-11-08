@file:Suppress("DEPRECATION")

package com.shifenmiao.login.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.entrypoint.ChannelConfigEntryPoint
import com.wanbaohe.base.ui.button.PrimaryButton
import com.wanbaohe.base.ui.button.SecondaryButton
import com.shifenmiao.base.utils.CoreUtils
import com.shifenmiao.base.utils.LoginUtils
import com.shifenmiao.core.R
import com.shifenmiao.model.login.LoginMethod
import com.shifenmiao.model.login.LoginState
import com.shifenmiao.model.remote.AiEngineConfig
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKey
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.system.OneBoxLeadingIconBadge
import com.wanbaohe.core.ui.widget.system.OneBoxListItem
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSecurity

@Composable
fun LoginOther(
    loginState: LoginState,
    loginComponent: LoginComponent,
    isUserAgreementChecked: MutableState<Boolean>,
    showAgreementDialog: MutableState<Boolean>,
    setConfirmAction: (((() -> Unit)) -> Unit),
    onNavigateToAddEngine: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val channelConfig: ChannelConfig = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChannelConfigEntryPoint::class.java
        ).getChannelConfig()
    }
    val enableWechat = channelConfig.enableWechat
    // 没装 / 不可用的 Play 服务上 Google 登录点了必失败, 按钮不画。
    // 检测放在 flavor 源集里, main 不引用 GMS 类。
    // 「或」只出现在 Google 按钮上面。国内渠道没有这条分隔线, 微信登录照常留着。
    val enableGoogle = channelConfig.enableGms && rememberGoogleSignInAvailable()

    // 从微信返回时兜底复位 loading：微信侧取消/拒绝可能不回任何回调，
    // 不处理的话登录页会一直停在「转圈 + 全灰」，看着像卡死。
    LifecycleResumeEffect(Unit) {
        loginComponent.onLoginScreenResumed()
        onPauseOrDispose { }
    }

    val wechatConfirmFunction = {
        loginComponent.loginByWechat()
    }

    // GMS 相关代码按 flavor 隔离在 src/google (真实) 与 src/nogms (stub), main 不 import GMS 类
    val googleConfirmFunction = rememberGoogleSignInAction(
        onIdToken = { idToken -> loginComponent.loginByGoogle(idToken) }
    )

    Column(
        modifier = Modifier
            .padding(horizontal = AppTheme.dimens.paddingLarge)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppTheme.dimens.paddingNormal),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAgreement(
                isUserAgreementChecked = isUserAgreementChecked,
            )
        }

        if (enableGoogle) {
            LoginOrDivider(
                modifier = Modifier.padding(
                    top = AppTheme.dimens.paddingLarge,
                    bottom = AppTheme.dimens.paddingNormal,
                )
            )
        } else if (enableWechat) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingLarge))
        }

        if (enableWechat) {
            // 与「登录」按钮共用 PrimaryButton：同样的胶囊圆角 + 玻璃质感，只换品牌色
            PrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(AppTheme.dimens.normalButtonHeight),
                text = stringResource(id = R.string.wechat_login),
                enable = !loginState.isLoggingIn,
                containerColor = Color(0xFF07C160),
                contentColor = Color.White,
                textStyle = MaterialTheme.typography.titleMedium,
                // 品牌绿要实底：关掉玻璃时走平涂会被洗淡，白字对比度不够
                glassStyle = GlassStyle.None,
                icon = {
                    if (loginState.loggingInMethod == LoginMethod.WECHAT) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.wechat),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                onClick = {
                    loginWithAgreementCheck(
                        isUserAgreementChecked = isUserAgreementChecked.value,
                        showAgreementDialog = showAgreementDialog,
                        setConfirmAction = setConfirmAction,
                    ) {
                        wechatConfirmFunction.invoke()
                    }
                }
            )
        }

        if (enableWechat && enableGoogle) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        }
        if (enableGoogle) {
            PrimaryButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(AppTheme.dimens.normalButtonHeight),
                text = stringResource(id = R.string.google_login),
                enable = !loginState.isLoggingIn,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                textStyle = MaterialTheme.typography.titleMedium,
                icon = {
                    if (loginState.loggingInMethod == LoginMethod.GOOGLE) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        // 用 Google 官方四色 G,别让 Icon 的默认 tint 把品牌色刷掉
                        Icon(
                            painter = painterResource(id = R.drawable.google),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                onClick = {
                    loginWithAgreementCheck(
                        isUserAgreementChecked = isUserAgreementChecked.value,
                        showAgreementDialog = showAgreementDialog,
                        setConfirmAction = setConfirmAction,
                    ) {
                        googleConfirmFunction.invoke()
                    }
                }
            )
        }

        // 「自带 API Key」逃生口: BYOK 直连不扣积分、免登录, 仅渠道能力放开或管理员可见
        if (onNavigateToAddEngine != null &&
            (AiEngineConfig.getCapabilities().canAddEngine || LoginUtils.isAdmin())
        ) {
            Spacer(modifier = Modifier.height(AppTheme.dimens.paddingLarge))
            OneBoxListItem(
                headlineContent = {
                    Text(
                        text = stringResource(id = R.string.byok_login_hint_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                subtitle = {
                    Text(
                        text = stringResource(id = R.string.byok_login_hint_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                leadingContent = {
                    OneBoxLeadingIconBadge(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineKey
                    )
                },
                trailingContent = {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp),
                    )
                },
                contained = true,
                onClick = onNavigateToAddEngine,
            )
        }
    }
}

/**
 * 「—— 或 ——」：把表单登录和第三方登录在视觉上分成两组
 */
@Composable
private fun LoginOrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Text(
            modifier = Modifier.padding(horizontal = AppTheme.dimens.paddingNormal),
            text = stringResource(id = R.string.login_or),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
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