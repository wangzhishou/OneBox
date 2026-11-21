/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

package com.wanbaohe.core.ui.widget.color_picker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSimpleSettingsInteractor
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.modifier.fadingEdges
import com.wanbaohe.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.core.ui.widget.text.TitleItem
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory

/**
 * 选色面板的「最近使用」：按取色/确认顺序倒序排，点一下回填当前色。
 *
 * 和调色板共用同一套色块形状与选中态：不加描边、不换形状，选中只在色块上压一个对勾。
 * 最近使用是流动数据，不提供重排/编辑，只保留一键清空。
 */
@Composable
fun RecentColorsRow(
    currentColor: Color,
    onColorClick: (Color) -> Unit
) {
    val recentColors = LocalSettingsState.current.recentColors
    val interactor = LocalSimpleSettingsInteractor.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    AnimatedVisibility(visible = recentColors.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TitleItem(
                text = stringResource(R.string.recently_used),
                icon = Icons.Outlined.LineHistory,
                modifier = Modifier,
                endContent = {
                    EnhancedIconButton(
                        onClick = {
                            scope.launch {
                                interactor.clearRecentColors()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = stringResource(R.string.clear_recent_colors),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            LazyRow(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .fadingEdges(listState),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(
                    items = recentColors,
                    key = { it.toArgb() }
                ) { color ->
                    val shape = MaterialTheme.shapes.medium
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(shape)
                            .then(
                                if (color.alpha < 1f) Modifier.transparencyChecker()
                                else Modifier
                            )
                            .background(color)
                            .colorSwatchOutline(color = color, shape = shape)
                            .hapticsClickable { onColorClick(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (color == currentColor) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = if (color.alpha < 1f) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    color.contentColor()
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
