/*
 * ImageToolbox is an image copyor for android
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

package com.t8rin.imagetoolbox.color_tools.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.t8rin.imagetoolbox.color_tools.presentation.screenLogic.ColorToolsComponent
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Swatch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAreaChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBarChart
import com.t8rin.imagetoolbox.core.resources.icons.line.LineBlender
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTheme
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.rememberAppColorTuple
import com.t8rin.imagetoolbox.core.ui.widget.controls.selection.ColorRowSelector
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.flatGlassContainer
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavItem
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBar
import com.t8rin.imagetoolbox.core.ui.widget.navigation.BottomNavigationBarStyle

private enum class ColorToolTab { INFO, MIXING, HARMONIES, SHADING, HISTOGRAM }

@Composable
fun ColorToolsContent(
    component: ColorToolsComponent
) {
    val themeState = LocalDynamicThemeState.current
    val settingsState = LocalSettingsState.current
    val allowChangeColor = settingsState.allowChangeColorByImage

    val appColorTuple = rememberAppColorTuple()

    val selectedColor = component.selectedColor.takeOrElse { appColorTuple.primary }

    LaunchedEffect(selectedColor) {
        if (allowChangeColor) {
            themeState.updateColor(selectedColor)
        }
    }

    var selectedTab by rememberSaveable {
        mutableStateOf(ColorToolTab.INFO)
    }

    BaseScreen(
        title = stringResource(R.string.color_tools),
        onGoBack = component.onGoBack,
        showNavigationBarsPadding = false
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        ColorRowSelector(
            value = selectedColor,
            onValueChange = component::updateSelectedColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .flatGlassContainer(
                    shape = ShapeDefaults.large
                ),
            icon = Icons.Outlined.LineTheme,
            title = stringResource(R.string.selected_color)
        )
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier.weight(1f),
            label = "color_tool_tab"
        ) { tab ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (tab) {
                    ColorToolTab.INFO -> ColorInfo(
                        selectedColor = selectedColor,
                        onColorChange = component::updateSelectedColor
                    )

                    ColorToolTab.MIXING -> ColorMixing(
                        selectedColor = selectedColor,
                        appColorTuple = appColorTuple
                    )

                    ColorToolTab.HARMONIES -> ColorHarmonies(
                        selectedColor = selectedColor
                    )

                    ColorToolTab.SHADING -> ColorShading(
                        selectedColor = selectedColor
                    )

                    ColorToolTab.HISTOGRAM -> ColorHistogram()
                }
            }
        }
        BottomNavigationBar(
            items = listOf(
                BottomNavItem(
                    id = ColorToolTab.INFO.name,
                    label = stringResource(R.string.color_info),
                    icon = Icons.Outlined.LineInfo
                ),
                BottomNavItem(
                    id = ColorToolTab.MIXING.name,
                    label = stringResource(R.string.color_mixing),
                    icon = Icons.Outlined.LineBlender
                ),
                BottomNavItem(
                    id = ColorToolTab.HARMONIES.name,
                    label = stringResource(R.string.color_harmonies),
                    icon = Icons.Outlined.LineBarChart
                ),
                BottomNavItem(
                    id = ColorToolTab.SHADING.name,
                    label = stringResource(R.string.color_shading),
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
