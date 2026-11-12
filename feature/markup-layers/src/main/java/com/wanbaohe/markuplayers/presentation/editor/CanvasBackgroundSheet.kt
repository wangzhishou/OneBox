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

package com.wanbaohe.markuplayers.presentation.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.color_picker.ColorSelectionRow
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.components.SheetHeader

/**
 * 画布背景设置面板:「默认」/「纯色」两个选项卡,选择即时生效。
 * 默认 = 跟随主题 surface 背景色;纯色选项卡内嵌滚动色板 [ColorSelectionRow],
 * 纯色记忆在面板内,从默认切回纯色时沿用上次颜色(默认白)。
 */
@Composable
fun CanvasBackgroundSheet(
    visible: Boolean,
    background: CanvasBackground,
    onBackgroundChange: (CanvasBackground) -> Unit,
    onDismiss: () -> Unit,
) {
    var solidColor by rememberSaveable { mutableIntStateOf(Color.White.toArgb()) }
    LaunchedEffect(background) {
        if (background is CanvasBackground.Solid) solidColor = background.color
    }

    EnhancedModalBottomSheet(
        visible = visible,
        dragHandle = {},
        onDismiss = { onDismiss() },
        sheetContent = {
            SheetHeader(
                title = stringResource(R.string.studio_workspace_background),
                onClose = onDismiss
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnhancedChip(
                        selected = background is CanvasBackground.Default,
                        onClick = { onBackgroundChange(CanvasBackground.Default) },
                        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.markup_canvas_bg_default),
                            maxLines = 1
                        )
                    }
                    EnhancedChip(
                        selected = background is CanvasBackground.Solid,
                        onClick = { onBackgroundChange(CanvasBackground.Solid(solidColor)) },
                        selectedColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.markup_canvas_bg_solid),
                            maxLines = 1
                        )
                    }
                }

                when (background) {
                    CanvasBackground.Default -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(ShapeDefaults.default)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = ShapeDefaults.default
                            )
                            .background(MaterialTheme.colorScheme.surface)
                    )

                    is CanvasBackground.Solid -> ColorSelectionRow(
                        value = Color(background.color),
                        onValueChange = {
                            solidColor = it.toArgb()
                            onBackgroundChange(CanvasBackground.Solid(it.toArgb()))
                        },
                        allowAlpha = false
                    )
                }
            }
        }
    )
}
