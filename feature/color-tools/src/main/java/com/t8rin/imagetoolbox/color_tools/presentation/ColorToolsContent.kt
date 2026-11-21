/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.color_tools.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.dynamic.theme.LocalDynamicThemeState
import com.t8rin.imagetoolbox.color_tools.presentation.components.ColorHarmonies
import com.t8rin.imagetoolbox.color_tools.presentation.components.ColorHistogram
import com.t8rin.imagetoolbox.color_tools.presentation.components.ColorInfo
import com.t8rin.imagetoolbox.color_tools.presentation.components.ColorMixing
import com.t8rin.imagetoolbox.color_tools.presentation.components.ColorShading
import com.t8rin.imagetoolbox.color_tools.presentation.components.PaletteEditor
import com.t8rin.imagetoolbox.color_tools.presentation.screenLogic.ColorToolsComponent
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.domain.model.ColorPalette
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Swatch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAreaChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlender
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePaletteTools
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.t8rin.imagetoolbox.core.settings.presentation.provider.rememberAppColorTuple
import com.wanbaohe.core.ui.widget.controls.selection.ColorRowSelector
import com.wanbaohe.core.ui.widget.navigation.BottomNavItem
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBar
import com.wanbaohe.core.ui.widget.navigation.BottomNavigationBarStyle
import kotlinx.coroutines.launch

private enum class ColorToolTab { PALETTE, INFO, MIXING, SHADING, HISTOGRAM }

@Composable
fun ColorToolsContent(
    component: ColorToolsComponent
) {
    val themeState = LocalDynamicThemeState.current
    val settingsState = LocalSettingsState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val appColorTuple = rememberAppColorTuple()

    val selectedColor = component.selectedColor.takeOrElse { appColorTuple.primary }

    // 色板行的颜色来自「当前调色板」：默认调色板就是原来那套预置色，
    // 用户在调色板 tab 切到自己的分组后，这行立刻变成那组颜色的快捷选择。
    // 选中的组写进设置：下次进色彩工具/选色面板仍是这一组，不回到第一组。
    val interactor = LocalSimpleSettingsInteractor.current
    val scope = rememberCoroutineScope()
    val palettes = settingsState.customPalettes
    // 切组时先在本地记住 id，等 DataStore 回流追平后再交还设置值，
    // 否则下拉选中/顶部色板会先闪一下旧组。
    var pendingPaletteId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(settingsState.selectedPaletteId, pendingPaletteId) {
        if (pendingPaletteId == settingsState.selectedPaletteId) {
            pendingPaletteId = null
        }
    }
    val activePalette = remember(palettes, settingsState.selectedPaletteId, pendingPaletteId) {
        palettes.firstOrNull {
            it.id == (pendingPaletteId ?: settingsState.selectedPaletteId)
        } ?: palettes.firstOrNull()
    }
    val quickColors = remember(activePalette) {
        activePalette?.colors
            ?.takeIf { it.isNotEmpty() }
            ?.map { Color(it) }
            ?: ColorPalette.DEFAULT_COLORS.map { Color(it) }
    }

    LaunchedEffect(selectedColor) {
        if (allowChangeColor) {
            themeState.updateColor(selectedColor)
        }
    }

    var selectedTab by rememberSaveable {
        mutableStateOf(
            ColorToolTab.entries.getOrNull(component.initialTab) ?: ColorToolTab.PALETTE
        )
    }

    BaseScreen(
        title = stringResource(R.string.color_tools),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false
    ) {
        ColorRowSelector(
            value = selectedColor,
            onValueChange = component::updateSelectedColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            icon = null,
            title = stringResource(R.string.selected_color),
            defaultColors = quickColors,
            // 和调色板色块、添加颜色格子同一圆角；选中态只显示对勾，不做形状/旋转动画
            swatchShape = MaterialTheme.shapes.large
        )
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "color_tool_tab"
        ) { tab ->
            // 调色板页自己带 LazyColumn：色块多/分组多时只组合可见行，
            // 不再跟着外层整页 verticalScroll 一次性铺开。
            if (tab == ColorToolTab.PALETTE) {
                PaletteEditor(
                    selectedColor = selectedColor,
                    onColorChange = component::updateSelectedColor,
                    selectedPaletteId = activePalette?.id,
                    onSelectPalette = { id ->
                        pendingPaletteId = id
                        scope.launch { interactor.setSelectedPaletteId(id) }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (tab) {
                        ColorToolTab.INFO -> {
                            ColorInfo(
                                selectedColor = selectedColor,
                                onColorChange = component::updateSelectedColor
                            )
                            Text(
                                text = stringResource(R.string.color_harmonies),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            ColorHarmonies(
                                selectedColor = selectedColor
                            )
                        }

                        ColorToolTab.MIXING -> ColorMixing(
                            selectedColor = selectedColor,
                            appColorTuple = appColorTuple
                        )

                        ColorToolTab.SHADING -> ColorShading(
                            selectedColor = selectedColor
                        )

                        ColorToolTab.HISTOGRAM -> ColorHistogram()

                        ColorToolTab.PALETTE -> Unit
                    }
                }
            }
        }
        BottomNavigationBar(
            items = listOf(
                BottomNavItem(
                    id = ColorToolTab.PALETTE.name,
                    label = stringResource(R.string.palette_tab),
                    icon = Icons.Outlined.LinePaletteTools
                ),
                BottomNavItem(
                    id = ColorToolTab.INFO.name,
                    label = stringResource(R.string.color_info_tab),
                    icon = Icons.Outlined.LineInfo
                ),
                BottomNavItem(
                    id = ColorToolTab.MIXING.name,
                    label = stringResource(R.string.color_mixing),
                    icon = Icons.Outlined.LineBlender
                ),
                BottomNavItem(
                    id = ColorToolTab.SHADING.name,
                    label = stringResource(R.string.color_shading_tab),
                    icon = Icons.Rounded.Swatch
                ),
                BottomNavItem(
                    id = ColorToolTab.HISTOGRAM.name,
                    label = stringResource(R.string.histogram),
                    icon = Icons.Outlined.LineAreaChart
                )
            ),
            selectedItemId = selectedTab.name,
            onItemClick = { item ->
                selectedTab = ColorToolTab.valueOf(item.id)
            },
            modifier = Modifier.fillMaxWidth(),
            tabTextStyle = MaterialTheme.typography.labelMedium,
            style = BottomNavigationBarStyle(
                tabHorizontalPadding = 8.dp
            )
        )
    }
}
