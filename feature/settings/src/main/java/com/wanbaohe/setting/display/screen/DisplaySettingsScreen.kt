package com.wanbaohe.setting.display.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.utils.Navigation
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.theme.AppTheme
import com.smarttoolfactory.colordetector.util.ColorUtil.roundToTwoDigits
import com.t8rin.imagetoolbox.core.domain.utils.trimTrailingZero
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiFontFamily
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSwitch
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxDesignSystem
import com.t8rin.imagetoolbox.core.ui.widget.system.OneBoxSectionCard
import com.t8rin.imagetoolbox.core.ui.widget.theme.ThemePresetSelector
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.PickFontFamilySheet
import com.t8rin.imagetoolbox.feature.settings.presentation.screenLogic.SettingsComponent
import com.wanbaohe.setting.display.component.DisplaySettingsComponent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.R as CoreR
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRobot
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.OpenInNew
import com.t8rin.imagetoolbox.core.resources.icons.line.LineApp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDarkMode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettingsSuggest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFont
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLightMode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineQuickTiles
import com.t8rin.imagetoolbox.core.resources.icons.line.LineScience
import com.t8rin.imagetoolbox.core.resources.icons.line.LineViewList
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCelebration
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDoorFront
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVibration

@Composable
fun DisplaySettingsScreen(component: DisplaySettingsComponent) {
    val onNavigate = LocalOnNavigate.current
    val settingsComponent = component.settingsComponent

    BaseScreen(
        title = {
            Text(
                text = stringResource(R.string.profile_item_dispaly),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        onGoBack = component.onGoBack,
        isShowDefaultActions = true,
        showNavigationBarsPadding = false,
        supportGlassEffect = true,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = OneBoxDesignSystem.screenPadding)
        ) {
            item { Spacer(modifier = Modifier.height(OneBoxDesignSystem.screenTopSpacing)) }

            item {
                OneBoxSectionCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(OneBoxDesignSystem.cardPadding)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(OneBoxDesignSystem.blockSpacing)) {
                        StartEntrySettingItem()
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        ThemePresetSelector(
                            onNavigateToThemeSettings = {
                                onNavigate(Screen.ThemeSettings)
                            }
                        )
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        ThemeSettingItem()
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        LayoutSettingItem()
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        PointsSettingItem()
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        QuickSettingItem(component.appComponentProxy)
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        MiniProgramSettingItem()
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        FontFamilySettingSection(settingsComponent)
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        FontSizeSettingSection(settingsComponent)
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                        ConfettiSettingItem(component)
                        Spacer(modifier = Modifier.height(OneBoxDesignSystem.compactSpacing))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(OneBoxDesignSystem.blockSpacing)) }
        }
    }
}

@Composable
fun StartEntrySettingItem() {
    val options = Navigation.rememberStartEntryOptions()
    var selectedIndex by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(options) {
        val preferredScreenId = AppSharedStorage.loadStartEntryScreenId()
        val legacyIndex = AppSharedStorage.loadStartEntryIndex()
        selectedIndex = options.indexOfFirst { it.id == preferredScreenId }
            .takeIf { it >= 0 }
            ?: legacyIndex.coerceIn(options.indices)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineApp,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.profile_start_entry),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        GlassSegmentedButtonRow(
            options = options,
            selectedOption = options[selectedIndex],
            onOptionSelected = { screen ->
                val index = options.indexOf(screen)
                if (index != selectedIndex) {
                    selectedIndex = index
                    AppSharedStorage.saveStartEntry(index = index, screenId = screen.id)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { screen ->
                Text(
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    text = stringResource(screen.title),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 2.dp)
                )
            },
            selectedIcon = {
                Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDoorFront,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            rowStyle = GlassStyle.None,
            rowColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
fun ThemeSettingItem() {
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val settingsState by settingsManager.settingsState.collectAsState()
    val nightMode = settingsState.nightMode
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.profile_item_day_night_mode),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        val options = listOf(
            Triple(
                stringResource(CoreR.string.dark),
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode,
                NightMode.Dark
            ),
            Triple(
                stringResource(CoreR.string.light),
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLightMode,
                NightMode.Light
            ),
            Triple(
                stringResource(CoreR.string.system),
                com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest,
                NightMode.System
            ),
        )
        GlassSegmentedButtonRow(
            options = options,
            // 选中态直接由 nightMode 派生, 切换主题预设(如暗色主题)改写 NIGHT_MODE 后能即时联动
            selectedOption = options.firstOrNull { it.third == nightMode } ?: options.last(),
            onOptionSelected = { triple ->
                if (triple.third != nightMode) {
                    scope.launch {
                        settingsManager.setNightMode(triple.third)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { (text, icon, _) ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
            },
            rowStyle = GlassStyle.None,
            rowColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
fun LayoutSettingItem() {
    val localSettingsState = LocalSettingsState.current
    val settingsManager = LocalSettingsManager.current
    val scope = rememberCoroutineScope()
    val isGirdMode = localSettingsState.groupOptionsByTypes
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.profile_item_display),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        val options = listOf(
            Pair(stringResource(R.string.profile_item_dislay_gird), com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineQuickTiles),
            Pair(stringResource(R.string.profile_item_dislay_list), com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineViewList)
        )
        // 选中态直接由全局状态派生, 外部(如备份恢复)改写后能即时联动
        val selected = if (isGirdMode) options[0] else options[1]
        GlassSegmentedButtonRow(
            options = options,
            selectedOption = selected,
            onOptionSelected = { pair ->
                if (pair != selected) {
                    scope.launch {
                        settingsManager.toggleGroupOptionsByTypes()
                    }
                }
            },
            hugContent = true,
            label = { (label, icon) ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
            },
            rowStyle = GlassStyle.None,
            rowColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
fun PointsSettingItem() {
    val isShowPointsTip = AppSharedStorage.isShowPointsTips.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_item_show_points_tip),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        GlassSwitch(
            checked = isShowPointsTip.value,
            onCheckedChange = {
                AppSharedStorage.saveIsShowPointsTips(it)
            },
            thumbContent = {
                if (isShowPointsTip.value) {
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
}

@Composable
fun QuickSettingItem(appComponent: AppComponent, isMe: Boolean = false) {
    val isShowQuickSetting by AppSharedStorage.isEnableSensor.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVibration,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_item_quick_setting),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        GlassSwitch(
            checked = isShowQuickSetting,
            onCheckedChange = {
                AppSharedStorage.saveIsEnableSensor(it)
                if (it) {
                    appComponent.initSensor()
                    appComponent.registerShakeListener()
                } else {
                    appComponent.unregisterShakeListener()
                }
            },
            thumbContent = {
                if (isShowQuickSetting) {
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
}

@Composable
fun ConfettiSettingItem(component: DisplaySettingsComponent) {
    val settingsState = LocalSettingsState.current
    val scope = rememberCoroutineScope()
    val isConfettiEnabled = settingsState.isConfettiEnabled
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCelebration,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_item_confetti),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        GlassSwitch(
            checked = isConfettiEnabled,
            onCheckedChange = { enabled ->
                scope.launch {
                    component.settingsComponent.toggleConfettiEnabled()
                }
                if (enabled) {
                    com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost.showConfetti()
                }
            },
            thumbContent = {
                if (isConfettiEnabled) {
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
}

@Composable
fun DebugEnvironmentSettingItem(appComponent: AppComponent) {
    val isDebug = remember { mutableStateOf(appComponent.isDebugEnvironment()) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineScience,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_item_debug_environment),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        GlassSwitch(
            checked = isDebug.value,
            onCheckedChange = {
                isDebug.value = it
                if (it) {
                    appComponent.setDebug()
                } else {
                    appComponent.setRelease()
                }
            },
            thumbContent = {
                if (isDebug.value) {
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
}

@Composable
fun DisableRobotSettingItem(appComponent: AppComponent) {
    val isDisableRobot by AppSharedStorage.isDisableRobot.collectAsState()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRobot,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.profile_item_disable_robot),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        val options = listOf(
            stringResource(R.string.dispaly),
            stringResource(R.string.hide)
        )
        // isDisableRobot 是 StateFlow 驱动, 选中态直接派生, 外部改写后能即时联动
        val selected = if (isDisableRobot) options[1] else options[0]
        GlassSegmentedButtonRow(
            options = options,
            selectedOption = selected,
            onOptionSelected = { label ->
                if (label != selected) {
                    appComponent.setDisableRobot(label == options[1])
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f),
            label = { label ->
                Text(
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(end = 2.dp)
                )
            },
            selectedIcon = {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            },
            rowStyle = GlassStyle.None,
            rowColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
fun MiniProgramSettingItem() {
    val isMiniProgramRememberChoice = AppSharedStorage.isMiniProgramRememberChoice.collectAsState()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.mini_program_remember_choice),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        GlassSwitch(
            checked = isMiniProgramRememberChoice.value,
            onCheckedChange = {
                AppSharedStorage.saveMiniProgramRememberChoice(it)
            },
            thumbContent = {
                if (isMiniProgramRememberChoice.value) {
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
}

@Composable
private fun FontFamilySettingSection(settingsComponent: SettingsComponent) {
    val showFontSheet = rememberSaveable { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val onFontSelected: (font: UiFontFamily) -> Unit = { font: UiFontFamily ->
        settingsComponent.setFont(font.asDomain())
        (context as? Activity)?.recreate()
    }

    val exportFontsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = {
            it?.let { uri ->
                settingsComponent.exportFonts(
                    uri = uri,
                    onResult = settingsComponent::parseFileSaveResult
                )
            }
        }
    )

    val showConfetti: () -> Unit = com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost::showConfetti

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFont,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_item_font_family),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = stringResource(R.string.edit),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { showFontSheet.value = true }
        )
    }

    PickFontFamilySheet(
        visible = showFontSheet.value,
        onFontSelected = onFontSelected,
        onDismiss = {
            showFontSheet.value = false
        },
        onAddFont = {
            settingsComponent.importCustomFont(
                uri = it,
                onSuccess = showConfetti,
                onFailure = {
                    com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost.showToast(
                        message = com.t8rin.imagetoolbox.core.utils.appContext.getString(com.t8rin.imagetoolbox.core.resources.R.string.wrong_font),
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText
                    )
                }
            )
        },
        onRemoveFont = settingsComponent::removeCustomFont,
        onExportFonts = {
            runCatching {
                val timeStamp = SimpleDateFormat(
                    "yyyy-MM-dd_HH-mm-ss",
                    Locale.getDefault()
                ).format(Date())

                exportFontsLauncher.launch("FONTS_EXPORT_$timeStamp.zip")
            }.onFailure {
                com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost.showActivateFilesToast()
            }
        }
    )
}

@Composable
private fun FontSizeSettingSection(settingsComponent: SettingsComponent) {
    val settingsState = LocalSettingsState.current
    // 默认(null=跟随系统)时滑杆停在 1.0 的中间位置, 可大可小
    val derivedValue by remember(settingsState) {
        derivedStateOf {
            settingsState.fontScale ?: 1f
        }
    }
    var sliderValue by remember(derivedValue) {
        mutableFloatStateOf(derivedValue)
    }

    val localActivity = LocalComponentActivity.current
    val onValueChange = { it: Float ->
        // 回到 1.0 即恢复"默认(跟随系统)"
        settingsComponent.setFontScale(if (it == 1f) 0f else it)
        localActivity.recreate()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFont,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.profile_item_font_size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            AnimatedContent(
                targetState = sliderValue,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                }, label = ""
            ) { value ->
                Text(
                    // 1.0 即"默认(跟随系统)"
                    text = value.takeIf { it != 1f }?.toString()?.trimTrailingZero()
                        ?: stringResource(CoreR.string.defaultt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        GlassCustomSlider(
            modifier = Modifier.padding(
                vertical = 0.dp,
                horizontal = AppTheme.dimens.paddingNormal
            ),
            value = sliderValue,
            onValueChange = {
                sliderValue = it.roundToTwoDigits()
            },
            valueRange = 0.45f..1.5f,
            onValueChangeFinished = {
                onValueChange(sliderValue)
            },
            steps = 20,
        )
    }
}

@Composable
fun FontSizeQuickSettingItem() {
    val localSettingsState = LocalSettingsState.current
    val settingsManager = LocalSettingsManager.current
    // 默认(null=跟随系统)时滑杆停在 1.0 的中间位置, 可大可小
    val derivedValue by remember(localSettingsState.fontScale) {
        derivedStateOf {
            localSettingsState.fontScale ?: 1f
        }
    }
    var sliderValue by remember(derivedValue) {
        mutableFloatStateOf(derivedValue)
    }
    val localActivity = LocalComponentActivity.current
    val scope = rememberCoroutineScope()
    val onValueChange = { it: Float ->
        scope.launch {
            // 回到 1.0 即恢复"默认(跟随系统)"
            settingsManager.setFontScale(if (it == 1f) 0f else it)
            localActivity.recreate()
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFont,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.profile_item_font_size),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        AnimatedContent(
            targetState = sliderValue,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            }, label = ""
        ) { value ->
            Text(
                // 1.0 即"默认(跟随系统)"
                text = value.takeIf { it != 1f }?.toString()?.trimTrailingZero()
                    ?: stringResource(CoreR.string.defaultt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }

    GlassCustomSlider(
        modifier = Modifier.padding(vertical = 0.dp),
        value = sliderValue,
        onValueChange = {
            sliderValue = it.roundToTwoDigits()
        },
        valueRange = 0.45f..1.5f,
        onValueChangeFinished = {
            onValueChange(sliderValue)
        },
        steps = 20,
    )
}
