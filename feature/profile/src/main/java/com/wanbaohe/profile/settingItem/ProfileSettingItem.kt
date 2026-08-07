package com.wanbaohe.profile.settingItem

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.shifenmiao.base.manager.DataBaseManager
import com.shifenmiao.base.manager.StorageManager
import com.shifenmiao.base.ui.ConfirmDialog
import com.shifenmiao.base.ui.getDefaultShareText
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.WeChatConfirmDialog
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.di.WebViewEntryPoint
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.helper.ContextUtils.findActivity
import com.wanbaohe.core.ui.review.InAppReviewPrompt
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.other.ToastDuration
import com.t8rin.imagetoolbox.core.utils.appContext
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import com.wanbaohe.profile.model.ProfileSetting
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFolderOff
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExposurePlus1

@Composable
fun ProfileSettingItem(
    modifier: Modifier,
    settingsComponent: SettingsComponent,
    setting: ProfileSetting,
    appComponent: AppComponent,
    themeIndex: Int = 0
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val onNavigate = LocalOnNavigate.current

    val settingsUIState = LocalSettingsState.current
    val currentFolderUri = settingsUIState.saveFolderUri
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                settingsComponent.setSaveFolderUri(it)
            }
        }
    )

    val navigateFeedback = {
        onNavigate(
            Screen.CreateFeedback(
            )
        )

    }

    when (setting) {

        ProfileSetting.AIFeatureSettings -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.AIFeatureSettings)
                }
            )
        }

        ProfileSetting.AIModelSettings -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.AISettings())
                }
            )
        }

        ProfileSetting.AIModelWorkSettings -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.AISettings(Screen.AISettings.Type.WorkingModel))
                },
                appComponent = appComponent
            )
        }

        ProfileSetting.AITokenUsage -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.TokenUsage)
                }
            )
        }

        ProfileSetting.TTSSettings -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.TTSSettings)
                },
                appComponent = appComponent
            )
        }

        ProfileSetting.SystemPromptManagement -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.SystemPromptManagement)
                }
            )
        }

        ProfileSetting.Help -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    navigateFeedback()
                }
            )
        }

        ProfileSetting.QQGroup -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    ActionUtils.joinQQGroup(context, Constants.QQ_GROUP_KEY)
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.Wechat -> {
            val showWechatTipsDialog = remember { mutableStateOf(false) }
            WeChatConfirmDialog(showWechatTipsDialog)
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    showWechatTipsDialog.value = true
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.Community -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.Community)
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.ClearCache -> {
            val showDialog = remember { mutableStateOf(false) }
            if (showDialog.value) {
                ConfirmDialog(
                    title = stringResource(id = R.string.profile_item_clear_cache_title),
                    message = stringResource(id = R.string.profile_item_clear_cache_message),
                    confirmButtonText = stringResource(id = R.string.button_confirm),
                    dismissButtonText = stringResource(id = R.string.button_cancel),
                    onConfirm = {
                        DataBaseManager.instance.clearAll()
                        StorageManager.instance.clearAll()
                        EntryPointAccessors.fromApplication(
                            context = context.applicationContext,
                            entryPoint = WebViewEntryPoint::class.java,
                        ).webResourceEngine().cache.clear()
                        settingsComponent.clearCache(
                            onComplete = {
                                scope.launch {
                                    AppToastHost.showToast(
                                        message = appContext.getString(
                                            R.string.profile_item_found_cache,
                                            it
                                        ),
                                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExposurePlus1,
                                        duration = ToastDuration.Long
                                    )
                                }
                            }
                        )
                        showDialog.value = false
                    },
                    onDismiss = { /* Handle dismiss action here */ },
                    showDialog = showDialog
                )
            }
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    showDialog.value = true
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.FolderSetting -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    runCatching {
                        launcher.launch(currentFolderUri)
                    }.onFailure {
                        scope.launch {
                            AppToastHost.showToast(
                                message = appContext.getString(com.t8rin.imagetoolbox.core.resources.R.string.activate_files),
                                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFolderOff,
                                duration = ToastDuration.Long
                            )
                        }
                    }
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.Donate -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(
                        Screen.BuyCoffee()
                    )
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.FontFamilySetting -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.DisplaySettings)
                }
            )
        }

        ProfileSetting.FontSizeSetting -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.DisplaySettings)
                }
            )
        }

        ProfileSetting.DisplaySetting -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.DisplaySettings)
                }
            )
        }

        ProfileSetting.LanguageSetting -> {
            val showLanguageSheet = remember { mutableStateOf(false) }
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APP_LOCALE_SETTINGS,
                                    "package:${context.packageName}".toUri()
                                )
                            )
                        }.onFailure {
                            showLanguageSheet.value = true
                        }
                    } else {
                        showLanguageSheet.value = true
                    }
                }
            )
            ChangeLanguageSheet(
                visible = showLanguageSheet.value,
                onDismiss = { showLanguageSheet.value = false }
            )
        }

        ProfileSetting.AuthCode -> {
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(Screen.AuthCodeSettings)
                }
            )
        }

        ProfileSetting.AboutUsSetting -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    onNavigate(
                        Screen.AboutUs()
                    )
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.ShareAppSetting -> {
            val shareText = getDefaultShareText()
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    appComponent.shareText(
                        shareText
                    )
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.RateAppSetting -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    val openStore = {
                        val packageName = context.packageName
                        val flavor = com.wanbaohe.profile.BuildConfig.FLAVOR
                        val uris = buildList {
                            when (flavor) {
                                "huawei" -> add("appmarket://details?id=$packageName")
                                "xiaomi" -> add("mimarket://details?id=$packageName")
                                "oppo" -> add("oppomarket://details?packagename=$packageName")
                                "vivo" -> add("vivomarket://details?id=$packageName")
                                "yyb" -> add("tmast://appdetails?pname=$packageName")
                            }
                            add("market://details?id=$packageName")
                            add("https://play.google.com/store/apps/details?id=$packageName")
                        }
                        var opened = false
                        for (uri in uris) {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, uri.toUri())
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                                opened = true
                                break
                            } catch (_: Exception) {
                            }
                        }
                        if (!opened) {
                            scope.launch {
                                AppToastHost.showToast(
                                    message = "无法打开应用商店",
                                    duration = ToastDuration.Short
                                )
                            }
                        }
                    }
                    // google 渠道优先弹应用内评分半屏层,不可用时回退跳商店;
                    // 国内渠道 InAppReviewPrompt 为空实现,直接走回退
                    val activity = context.findActivity()
                    if (activity != null) {
                        InAppReviewPrompt.launch(activity, onUnavailable = openStore)
                    } else {
                        openStore()
                    }
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.UpdateSetting -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {
                    appComponent.jumpToAppStoreDetailUpdate()
                },
                settingsComponent = settingsComponent
            )
        }

        ProfileSetting.AnalyticsSetting -> {
            // 统计与崩溃上报共用一个开关, 两个 key 保持同步;
            // ComposeActivity 观察 settingsState 变化后应用到 Firebase
            val toggleAnalytics = {
                settingsComponent.toggleAllowCollectCrashlytics()
                settingsComponent.toggleAllowCollectAnalytics()
            }
            BaseSettingItem(
                modifier,
                settingsComponent = settingsComponent,
                setting = setting,
                themeIndex = themeIndex,
                onclick = toggleAnalytics,
                trailingContent = {
                    // 与 DisplaySettingsScreen 的开关同款: GlassSwitch + 选中时拇指带对勾
                    GlassSwitch(
                        checked = settingsUIState.allowCollectCrashlytics,
                        onCheckedChange = { toggleAnalytics() },
                        thumbContent = {
                            if (settingsUIState.allowCollectCrashlytics) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                )
                            }
                        },
                        colors = AppTheme.colors.switchColors()
                    )
                }
            )
        }

        else -> {
            BaseSettingItem(
                modifier,
                setting = setting,
                themeIndex = themeIndex,
                onclick = {

                },
                settingsComponent = settingsComponent
            )
        }

    }
}
