package com.wanbaohe.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.GrayHorizontalDivider
import com.shifenmiao.common.components.DatabaseBackupRestoreSection
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.BuildConfig
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants.DRAWER_WIDTH
import com.shifenmiao.login.viewModel.LoginComponent
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.helper.ProvidesValue
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorTuplePicker
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.theme.ThemePresetSelector
import com.wanbaohe.profile.components.InvitationCodeAction
import com.wanbaohe.profile.components.ProfileLogin
import com.wanbaohe.setting.display.screen.DebugEnvironmentSettingItem
import com.wanbaohe.setting.display.screen.DisableRobotSettingItem
import com.wanbaohe.setting.display.screen.FontSizeQuickSettingItem
import com.wanbaohe.setting.display.screen.LayoutSettingItem
import com.wanbaohe.setting.display.screen.PointsSettingItem
import com.wanbaohe.setting.display.screen.QuickSettingItem
import com.wanbaohe.setting.display.screen.StartEntrySettingItem
import com.wanbaohe.setting.display.screen.ThemeSettingItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun DrawerContentWrapper(
    appComponent: AppComponent,
    onCloseDrawer: () -> Job,
    loginComponent: LoginComponent
) {
    LocalLayoutDirection.ProvidesValue(LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 0.dp,
                        topEnd = 0.dp
                    )
                )
                .navigationBarsPadding()
                .statusBarsPadding()
                .fillMaxHeight()
                .width(DRAWER_WIDTH)
        ) {
            DrawerMenu(
                modifier = Modifier.weight(1f),
                onCloseDrawer = onCloseDrawer,
                appComponent = appComponent,
                loginComponent = loginComponent
            )
        }
    }
}

@Composable
fun DrawerMenu(
    modifier: Modifier = Modifier,
    appComponent: AppComponent,
    onCloseDrawer: () -> Job = { Job() },
    loginComponent: LoginComponent,
) {
    val onNavigate = LocalOnNavigate.current
    val listState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()

    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(OneBoxDesignSystem.screenPadding),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)
    ) {
        item {
            ProfileLogin(
                onClick = {
                    onCloseDrawer()
                }
            ) {
                InvitationCodeAction(
                    loginComponent = loginComponent
                )
            }
        }
        item {
            GrayHorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
        }
        item {
            Text(
                text = stringResource(id = R.string.quick_setting),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            // 横向滚动主题预设选择器（复用组件）
            ThemePresetSelector(
                onNavigateToThemeSettings = {
                    onCloseDrawer()
                    onNavigate(Screen.ThemeSettings)
                }
            )
        }
        item {
            if (BuildConfig.DEBUG) {
                DebugEnvironmentSettingItem(appComponent)
            }
        }
        item {
            StartEntrySettingItem()
        }
        item {
            QuickSettingItem(appComponent, isMe = true)
        }
        item {
            DisableRobotSettingItem(appComponent)
        }
        item {
            PointsSettingItem()
        }
        item {
            ThemeSettingItem()
        }
        item {
            LayoutSettingItem()
        }
        item {
            Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
            FontSizeQuickSettingItem()
        }
        item {
            GrayHorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
        }
        item {
            Text(
                text = stringResource(id = R.string.data_database),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            DatabaseBackupRestoreSection(
                commonComponent = appComponent,
                onAction = { onCloseDrawer() }
            )
        }
        item {
            GrayHorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
        }
    }

    if (showColorPicker) {
        val settingsUIState = LocalSettingsState.current
        ColorTuplePicker(
            visible = showColorPicker,
            colorTuple = settingsUIState.appColorTuple,
            onDismiss = {
                showColorPicker = false
            },
            onColorChange = {
                scope.launch {
                    val tupleString = it.run {
                        "${primary.toArgb()}*${secondary?.toArgb()}*${tertiary?.toArgb()}*${surface?.toArgb()}"
                    }
                    val tuplesString =
                        (settingsUIState.colorTupleList + it).joinToString(separator = "*") { tuple ->
                            "${tuple.primary.toArgb()}/${tuple.secondary?.toArgb()}/${tuple.tertiary?.toArgb()}/${tuple.surface?.toArgb()}"
                        }
                    settingsManager.setColorTuple(tupleString)
                    settingsManager.setColorTuples(tuplesString)
                }
                showColorPicker = false
            }
        )
    }
}
