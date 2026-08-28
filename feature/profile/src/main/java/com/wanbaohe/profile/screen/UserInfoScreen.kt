package com.wanbaohe.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.ConfirmContentDialog
import com.shifenmiao.base.ui.ConfirmDialog
import com.shifenmiao.base.ui.ErrorTextInputField
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants.COUNT_DOWN_TIME
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.model.login.LoginChannelConfig
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.profile.components.InvitationCodeAction
import com.wanbaohe.profile.ui.AboutCardDivider
import com.wanbaohe.profile.ui.AboutGlassCard
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePhone
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDoorBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExitToApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDeleteForever

@Composable
fun UserInfoScreen(
    loginComponent: LoginComponent,
    onGoBack: () -> Unit = {},
    onNavigateToVipLevel: () -> Unit = {},
) {
    val loginState = LocalLoginState.current
    val showExitDialog = remember { mutableStateOf(false) }
    val showLogoutDialog = remember { mutableStateOf(false) }
    val showNicknameDialog = remember { mutableStateOf(false) }
    val showChangePasswordSheet = remember { mutableStateOf(false) }
    val countdownTime = remember { mutableIntStateOf(COUNT_DOWN_TIME) }

    if (!loginState.isLogin) {
        LaunchedEffect(key1 = true) {
            onGoBack.invoke()
        }
    } else {
        BaseScreen(
            title = stringResource(id = R.string.profile_user_info_title),
            onGoBack = onGoBack,
            supportGlassEffect = true,
            actions = {
                InvitationCodeAction(loginComponent = loginComponent)
            },
            foreground = {
                // Countdown overlay for long-press logout
                if (countdownTime.intValue in 1 until COUNT_DOWN_TIME) {
                    Text(
                        text = countdownTime.intValue.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(0.5f),
                            fontSize = 600.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 30.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // ── Profile info ──
                item {
                    AboutGlassCard {
                        GlassListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.profile_user_info_avatar),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                Avatar(
                                    username = loginState.username,
                                    avatar = loginState.avatar,
                                )
                            },
                        )
                        AboutCardDivider()
                        GlassListItemText(
                            headline = stringResource(R.string.profile_user_info_username),
                            value = loginState.username,
                        )
                        AboutCardDivider()
                        GlassListItem(
                            modifier = Modifier.clickable { showNicknameDialog.value = true },
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.profile_user_info_nickname),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = loginState.nickname,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                        )
                        AboutCardDivider()
                        GlassListItemText(
                            headline = stringResource(R.string.profile_user_info_email),
                            value = loginState.emailOrMobile,
                        )
                        // 海外渠道不提供绑定手机功能, 不展示手机号行
                        if (LoginChannelConfig.getConfigByFlavor().bindPhoneSupported) {
                            AboutCardDivider()
                            GlassListItem(
                                headlineContent = {
                                    Text(
                                        text = stringResource(R.string.profile_user_info_phone),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                trailingContent = {
                                    if (loginState.phone.isEmpty()) {
                                        Button(
                                            onClick = { loginComponent.showBindPhone() },
                                            colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                                        ) {
                                            Icon(
                                                modifier = Modifier.size(14.dp),
                                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePhone,
                                                contentDescription = "phone",
                                            )
                                            Spacer(modifier = Modifier.size(8.dp))
                                            Text(
                                                text = stringResource(R.string.button_bind),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = loginState.phone,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                            )
                        }
                        // 已绑手机走短信验证码,未绑手机但有真实邮箱(非占位邮箱)走邮箱验证码
                        val hasRealEmail = loginState.emailOrMobile.contains("@") &&
                            !loginState.emailOrMobile.endsWith("@example.com") &&
                            !loginState.emailOrMobile.endsWith("@google.user")
                        if (loginState.phone.isNotEmpty() || hasRealEmail) {
                            AboutCardDivider()
                            GlassListItem(
                                modifier = Modifier.clickable { showChangePasswordSheet.value = true },
                                headlineContent = {
                                    Text(
                                        text = stringResource(R.string.profile_change_password),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                        }
                    }
                }

                // ── Account info ──
                item {
                    AboutGlassCard {
                        GlassListItemText(
                            headline = stringResource(R.string.profile_user_info_points),
                            value = StringUtils.formatNumber(loginState.points),
                        )
                        AboutCardDivider()
                        GlassListItem(
                            modifier = Modifier.clickable { onNavigateToVipLevel() },
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.vip_level_current),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    val levelInfo = vipLevelList.getOrNull(loginState.vipLevel) ?: vipLevelList[0]
                                    Text(
                                        text = "VIP ${loginState.vipLevel} · ${stringResource(levelInfo.nameRes)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                        )
                        AboutCardDivider()
                        GlassListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.profile_user_info_invitation_code),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                GlassTonalButton(
                                    onClick = {
                                        Clipboard.copy(loginState.invitationCode)
                                    },
                                    colors = AppTheme.colors.getSecondaryContainerButtonColors(),
                                ) {
                                    Icon(
                                        modifier = Modifier.size(14.dp),
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                                        contentDescription = "invitationCode",
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text(
                                        text = loginState.invitationCode,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            },
                        )
                    }
                }

                // ── Danger zone ──
                item {
                    AboutGlassCard {
                        GlassLongPressItem(
                            countdownTime = countdownTime,
                            onLongPressTrigger = { showLogoutDialog.value = true },
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.profile_user_info_Logout),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            trailingContent = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = stringResource(R.string.profile_Logout_desc),
                                        color = MaterialTheme.colorScheme.secondary,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Icon(
                                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDeleteForever,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.secondary,
                                    )
                                }
                            },
                        )
                    }
                }

                // ── Exit button ──
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    GlassButton(
                        onClick = { showExitDialog.value = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(AppTheme.dimens.normalButtonHeight),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExitToApp,
                            contentDescription = null,
                        )
                        Text(
                            text = stringResource(R.string.profile_user_info_exit),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // ── Dialogs ──
    ConfirmDialog(
        showDialog = showExitDialog,
        title = stringResource(R.string.login_closing),
        onConfirm = {
            loginComponent.loginExit()
            showExitDialog.value = false
        },
        confirmButtonText = stringResource(R.string.button_confirm),
        dismissButtonText = stringResource(R.string.button_cancel),
        onDismiss = { showExitDialog.value = false },
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDoorBack,
                contentDescription = null,
            )
        },
        message = stringResource(R.string.login_closing_sub),
    )

    ConfirmContentDialog(
        showDialog = showLogoutDialog,
        title = stringResource(R.string.login_logout),
        onConfirm = {
            loginComponent.loginOut()
            showLogoutDialog.value = false
        },
        confirmButtonText = stringResource(R.string.button_confirm),
        dismissButtonText = stringResource(R.string.button_cancel),
        onDismiss = { showLogoutDialog.value = false },
        icon = {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDoorBack,
                contentDescription = null,
            )
        },
    ) {
        Text(
            text = stringResource(R.string.login_out_sub),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
        )
        Spacer(modifier = Modifier.size(8.dp))
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(R.string.profile_user_info_points),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            trailingContent = {
                Text(
                    text = loginState.points.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }

    if (showNicknameDialog.value) {
        NicknameEditDialog(
            currentNickname = loginState.nickname,
            onDismiss = { showNicknameDialog.value = false },
            onConfirm = { nickname ->
                loginComponent.updateNickname(
                    nickname = nickname,
                    onSuccess = {
                        ActionUtils.showToast(R.string.profile_nickname_updated)
                        showNicknameDialog.value = false
                    },
                    onFail = {
                        ActionUtils.showError(it)
                    }
                )
            },
        )
    }

    if (showChangePasswordSheet.value) {
        ChangePasswordSheet(
            loginComponent = loginComponent,
            onDismiss = { showChangePasswordSheet.value = false },
        )
    }
}

// ──────────────────────────────────────────────────────────────
//  Glass-style list item helpers (private to this file)
// ──────────────────────────────────────────────────────────────

@Composable
private fun GlassListItem(
    headlineContent: @Composable () -> Unit,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = headlineContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
private fun GlassListItemText(
    headline: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        trailingContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GlassLongPressItem(
    headlineContent: @Composable () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onLongPressTrigger: () -> Unit = {},
    countdownTime: MutableIntState = remember { mutableIntStateOf(COUNT_DOWN_TIME) },
) {
    val coroutineScope = rememberCoroutineScope()
    var countdownJob: Job? by remember { mutableStateOf(null) }
    var isPressed by remember { mutableStateOf(false) }

    ListItem(
        modifier = modifier
            .pointerInteropFilter { event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> {
                        isPressed = true
                        countdownTime.intValue = COUNT_DOWN_TIME
                        countdownJob?.cancel()
                        countdownJob = coroutineScope.launch {
                            repeat(COUNT_DOWN_TIME) {
                                delay(1000.milliseconds)
                                countdownTime.intValue--
                            }
                            onLongPressTrigger()
                        }
                        true
                    }

                    android.view.MotionEvent.ACTION_UP,
                    android.view.MotionEvent.ACTION_CANCEL -> {
                        isPressed = false
                        countdownJob?.cancel()
                        countdownTime.intValue = COUNT_DOWN_TIME
                        true
                    }

                    else -> false
                }
            }
            .background(
                if (isPressed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                else Color.Transparent
            ),
        headlineContent = headlineContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}
@Composable
private fun NicknameEditDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(currentNickname) }
    var showError by remember { mutableStateOf(false) }
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.profile_change_nickname))
        },
        text = {
            GlassOutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    showError = false
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError,
                supportingText = {
                    if (showError) {
                        ErrorTextInputField(text = stringResource(R.string.profile_nickname_rule))
                    }
                },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = text.trim()
                    if (isValidNickname(trimmed)) {
                        onConfirm(trimmed)
                    } else {
                        showError = true
                    }
                },
            ) {
                Text(text = stringResource(R.string.button_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.button_cancel))
            }
        },
    )
}

/**
 * 昵称规则:2~20 个字符,仅支持中英文、数字、空格以及 _ - ·
 */
private fun isValidNickname(nickname: String): Boolean {
    if (nickname.length !in 2..20) return false
    return nickname.all {
        it.isLetterOrDigit() || it == ' ' || it == '_' || it == '-' || it == '·'
    }
}
