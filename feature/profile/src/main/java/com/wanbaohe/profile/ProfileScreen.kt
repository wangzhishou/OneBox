package com.wanbaohe.profile

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.entrypoint.ChannelConfigEntryPoint
import com.shifenmiao.base.ui.ConfirmDialog
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.components.DatabaseBackupRestoreSection
import com.shifenmiao.common.logic.AppComponent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolder
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMedal
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.R
import com.shifenmiao.login.viewModel.LoginComponent
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.data.utils.SafUriUtils
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalLoginState
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxGroupDivider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import com.wanbaohe.profile.components.InvitationCodeAction
import com.wanbaohe.profile.model.ProfileSetting
import com.wanbaohe.profile.screen.vipLevelList
import com.wanbaohe.profile.settingItem.ProfileGroup
import com.wanbaohe.profile.settingItem.ProfileSettingItem
import dagger.hilt.android.EntryPointAccessors
import java.io.File
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLogout

@Composable
fun ProfileScreen(
    settingsComponent: SettingsComponent,
    appComponent: AppComponent,
    loginComponent: LoginComponent
) {
    val loginState = LocalLoginState.current
    val onNavigate = LocalOnNavigate.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    BaseScreen(
        title = {
            ProfileUserSection(
                loginComponent = loginComponent,
                onEditProfile = {
                    if (loginState.isLogin) {
                        onNavigate(Screen.UserInfo())
                    } else {
                        onNavigate(Screen.Login())
                    }
                },
                onNavigateToVipLevel = {
                    onNavigate(Screen.VipLevel())
                }
            )
        },
        type = EnhancedTopAppBarType.Medium,
        scrollBehavior = scrollBehavior,
        onGoBack = { appComponent.onGoBack() },
        isShowDefaultActions = false,
        supportGlassEffect = true,
        navigationIcon = {}
    ) {
        ProfileContent(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            settingsComponent = settingsComponent,
            appComponent = appComponent,
            loginComponent = loginComponent
        )
    }
}


@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    settingsComponent: SettingsComponent,
    appComponent: AppComponent,
    loginComponent: LoginComponent
) {
    val initialSettingGroups = remember {
        ProfileSetting.entries.filter {
            !(it.isDebug)
        }
    }
    val context = LocalComponentActivity.current
    val channelConfig = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ChannelConfigEntryPoint::class.java
        ).getChannelConfig()
    }
    // 仅打包了多语言资源的渠道(如 google)展示语言切换入口, 由渠道 buildConfigField 控制
    val showLanguageSetting = channelConfig.showLanguageSetting
    // 支付全关的渠道(google)隐藏"请喝咖啡"购买入口; QQ/微信社区入口仅国内渠道展示
    val showDonateSetting = channelConfig.enablePayment
    val showCommunitySetting = channelConfig.enableWechat
    val itemModifier = Modifier
    val loginState = LocalLoginState.current
    val onNavigate = LocalOnNavigate.current
    val showExitDialog = remember { mutableStateOf(false) }

    // Logout confirm dialog
    if (showExitDialog.value) {
        ConfirmDialog(
            showDialog = showExitDialog,
            title = stringResource(R.string.login_closing),
            onConfirm = {
                loginComponent.loginExit()
                showExitDialog.value = false
            },
            confirmButtonText = stringResource(R.string.button_confirm),
            dismissButtonText = stringResource(R.string.button_cancel),
            onDismiss = {
                showExitDialog.value = false
            },
            icon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLogout,
                    contentDescription = null
                )
            },
            message = stringResource(R.string.login_closing_sub)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = OneBoxDesignSystem.screenPadding
        )
    ) {
        item {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing))
        }

        // 2. Stats cards
        item {
            ProfileStatsRow(onNavigate = onNavigate)
        }

        // 3. Setting groups with section headers
        var globalThemeIndex = 0
        initialSettingGroups.forEach { group ->
            item {
                Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
                // Section header
                val groupTitleRes = group.groupTitle
                if (groupTitleRes != R.string.empty_string) {
                    Text(
                        text = stringResource(groupTitleRes).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = OneBoxDesignSystem.microSpacing,
                            bottom = OneBoxDesignSystem.compactSpacing
                        )
                    )
                }
                // Group card
                ProfileGroup {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        if (group.settingsList != null) {
                            val itemsList = remember {
                                group.settingsList.filter {
                                    (showLanguageSetting || it != ProfileSetting.LanguageSetting) &&
                                        (showDonateSetting || it != ProfileSetting.Donate) &&
                                        (showCommunitySetting || it != ProfileSetting.Community)
                                }
                            }
                            itemsList.forEachIndexed { index, setting ->
                                ProfileSettingItem(
                                    itemModifier,
                                    settingsComponent,
                                    setting,
                                    appComponent,
                                    themeIndex = globalThemeIndex++
                                )
                                if (itemsList.size > 1 && index < itemsList.lastIndex) {
                                    OneBoxGroupDivider()
                                }
                            }
                        } else {
                            ProfileSettingItem(
                                modifier = itemModifier,
                                settingsComponent = settingsComponent,
                                setting = group,
                                appComponent = appComponent,
                                themeIndex = globalThemeIndex++
                            )
                        }
                    }
                }
            }
        }

        // 4. Data backup / restore
        item {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.sectionSpacing))
            Text(
                text = stringResource(R.string.data_database).uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    start = OneBoxDesignSystem.microSpacing,
                    bottom = OneBoxDesignSystem.compactSpacing
                )
            )
            DatabaseBackupRestoreSection(
                commonComponent = appComponent
            )
        }

        if (loginState.isLogin) {
            // 4. Logout button
            item {
                Spacer(modifier = Modifier.height(24.dp))
                OneSecondaryButton(
                    text = stringResource(R.string.profile_user_info_exit),
                    onClick = {
                        showExitDialog.value = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }


        // 5. Version text
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${stringResource(R.string.app_name)} V${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun ProfileUserSection(
    onEditProfile: () -> Unit,
    onNavigateToVipLevel: () -> Unit,
    loginComponent: LoginComponent
) {
    val loginState = LocalLoginState.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditProfile() }
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Avatar(
            username = loginState.username,
            nickname = loginState.nickname,
            avatar = loginState.avatar,
            size = 40.dp,
            isLogin = loginState.isLogin
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = BaseUtils.getDisplayName(loginState.nickname, loginState.username),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (loginState.isLogin && loginState.vipLevel > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                val levelInfo = vipLevelList.getOrNull(loginState.vipLevel) ?: vipLevelList[0]
                Text(
                    text = "VIP ${loginState.vipLevel} · ${stringResource(levelInfo.nameRes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onNavigateToVipLevel() },
                )
            }
        }
        InvitationCodeAction(
            loginComponent = loginComponent
        )
    }
}

@Composable
private fun ProfileStatsRow(
    onNavigate: (Screen) -> Unit
) {
    val loginState = LocalLoginState.current
    val settingsState = LocalSettingsState.current

    // Resolve the folder URI for navigation — 转为 file:// URI 避免 SAF 权限过期
    val folderUri: Uri? = remember(settingsState.saveFolderUri) {
        settingsState.saveFolderUri?.let { SafUriUtils.treeUriToFileUri(it) }
    }

    // Count files in the save folder directory
    val fileCount = remember(folderUri) {
        try {
            if (folderUri != null) {
                val dir = File(folderUri.path ?: return@remember 0)
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.size ?: 0
                } else 0
            } else {
                val defaultDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    "OneBox"
                )
                if (defaultDir.exists() && defaultDir.isDirectory) {
                    defaultDir.listFiles()?.size ?: 0
                } else 0
            }
        } catch (_: Exception) {
            0
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Points card
        GlassCard(
            onClick = {
                ActionUtils.showLogin(source = "points_card_game") {
                    onNavigate(Screen.Survive30s)
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMedal,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column {
                    Text(
                        text = StringUtils.formatNumber(loginState.points),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.profile_stats_points).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Files card
        GlassCard(
            onClick = {
                onNavigate(Screen.FileBrowser(folderUri))
            },
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = AppTheme.dimens.containerAlpha),
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolder,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column {
                    Text(
                        text = fileCount.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.profile_stats_files).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
