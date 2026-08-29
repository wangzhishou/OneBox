/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

package com.t8rin.imagetoolbox.core.ui.widget.other

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.shifenmiao.base.ui.shapes.BubbleShape
import com.shifenmiao.core.R
import com.shifenmiao.theme.AppTheme
import com.smarttoolfactory.colordetector.util.ColorUtil.roundToTwoDigits
import com.t8rin.imagetoolbox.core.domain.utils.trimTrailingZero
import com.t8rin.imagetoolbox.core.settings.domain.SettingsManager
import com.t8rin.imagetoolbox.core.settings.domain.model.NightMode
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsManager
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.provider.LocalComponentActivity
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSegmentedButtonRow
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCustomSlider
import com.t8rin.imagetoolbox.core.ui.widget.theme.ThemePresetSelector
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.t8rin.imagetoolbox.core.resources.icons.line.LineDarkMode
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettingsSuggest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFont
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLightMode


@Composable
fun DisableContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.alpha(
            animateFloatAsState(
                if (enabled) 1f else 0.5f
            ).value
        )
    ) {
        content()
        if (!enabled) {
            Surface(
                color = Color.Transparent,
                modifier = Modifier.matchParentSize()
            ) {}
        }
    }
}

@Composable
fun TopAppBarEmoji() {
    ThemeQuickSettingsButton()
}

/**
 * 主题快捷设置按钮 - 点击展开浮动菜单
 *
 * 重构后的布局：
 * 1. 横向滚动主题预设卡片列表（ 千色千面  / 品牌色 / 预制主题 / 用户主题 / +新建）
 * 2. 深色/浅色/跟随系统模式切换
 * 3. 字体大小调整
 */
@Composable
fun ThemeQuickSettingsButton(
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val settingsManager = LocalSettingsManager.current
    val onNavigate = LocalOnNavigate.current

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTheme,
                contentDescription = "Theme Settings",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (expanded) {
            val density = LocalDensity.current
            val bubbleShape = BubbleShape(
                arrowSize = 10.dp,
                arrowDirection = BubbleShape.ArrowDirection.Top,
                arrowAlignment = BubbleShape.ArrowAlignment.End,
                arrowOffset = 24.dp,
                cornerRadius = 16.dp
            )
            Popup(
                alignment = Alignment.TopEnd,
                offset = IntOffset(
                    x = with(density) { 16.dp.roundToPx() },
                    y = with(density) { 40.dp.roundToPx() }
                ),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Surface(
                    shape = bubbleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.width(320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 16.dp, end = 16.dp,
                                top = 18.dp, bottom = 16.dp
                            )
                    ) {
                        // ── 横向滚动主题预设选择器（复用组件）──
                        ThemePresetSelector(
                            onNavigateToThemeSettings = {
                                expanded = false
                                onNavigate(Screen.ThemeSettings)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── 深色/浅色/跟随系统 ──
                        ThemeMenuThemeModeItem(settingsManager = settingsManager)

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── 字体大小 ──
                        ThemeMenuFontSizeItem(settingsManager = settingsManager)
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
//  通用子组件
// ══════════════════════════════════════════════════════════════


// ── 深色/浅色/跟随系统（保留原逻辑）──
@Composable
private fun ThemeMenuThemeModeItem(settingsManager: SettingsManager) {
    val scope = rememberCoroutineScope()
    val settingsState by settingsManager.settingsState.collectAsState()
    val nightMode = settingsState.nightMode

    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.profile_item_day_night_mode),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
        }

        val options = listOf(
            Triple(stringResource(com.t8rin.imagetoolbox.core.resources.R.string.dark), com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineDarkMode, NightMode.Dark),
            Triple(stringResource(com.t8rin.imagetoolbox.core.resources.R.string.light), com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLightMode, NightMode.Light),
            Triple(stringResource(com.t8rin.imagetoolbox.core.resources.R.string.system), com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest, NightMode.System),
        )

        GlassSegmentedButtonRow(
            options = options,
            selectedOption = options.firstOrNull { it.third == nightMode } ?: options.last(),
            onOptionSelected = { triple ->
                if (triple.third != nightMode) {
                    scope.launch { settingsManager.setNightMode(triple.third) }
                }
            },
            modifier = Modifier.fillMaxWidth().height(40.dp),
            rowShape = RoundedCornerShape(50),
            buttonShape = RoundedCornerShape(50),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
            label = { triple ->
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(triple.second, null, Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = triple.first,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        )
    }
}

// ── 字体大小（保留原逻辑）──
@Composable
private fun ThemeMenuFontSizeItem(settingsManager: SettingsManager) {
    val localSettingsState = LocalSettingsState.current
    val derivedValue by remember(localSettingsState.fontScale) {
        derivedStateOf { localSettingsState.fontScale ?: 0f }
    }
    var sliderValue by remember(derivedValue) { mutableFloatStateOf(derivedValue) }
    val localActivity = LocalComponentActivity.current
    val scope = rememberCoroutineScope()
    val onValueChange = { it: Float ->
        scope.launch { settingsManager.setFontScale(it); localActivity.recreate() }
    }

    Column(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
            Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineFont, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.profile_item_font_size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f))
            AnimatedContent(sliderValue, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "") { value ->
                Text(
                    text = value.takeIf { it > 0 }?.toString()?.trimTrailingZero()
                        ?: stringResource(com.t8rin.imagetoolbox.core.resources.R.string.defaultt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        GlassCustomSlider(
            value = sliderValue,
            onValueChange = { sliderValue = if (it == 0.45f) 0f else it.roundToTwoDigits() },
            valueRange = 0.45f..1.5f,
            onValueChangeFinished = { onValueChange(sliderValue) }
        )
    }
}

// ── 保留供外部引用的子组件 ──

@Composable
fun ThemeMenuGuessThemeItem(settingsManager: SettingsManager) {
    val localSettingsState = LocalSettingsState.current
    val scope = rememberCoroutineScope()
    val isDynamicColors = remember { mutableStateOf(localSettingsState.isDynamicColors) }

    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp), Alignment.CenterVertically) {
        Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
        Text(stringResource(R.string.profile_item_dynamic_colors),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f))
        Switch(
            checked = isDynamicColors.value,
            onCheckedChange = { isDynamicColors.value = it; scope.launch { settingsManager.toggleDynamicColors() } },
            thumbContent = {
                if (isDynamicColors.value) Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check, null, Modifier.size(SwitchDefaults.IconSize))
            },
            colors = AppTheme.colors.switchColors()
        )
    }
}