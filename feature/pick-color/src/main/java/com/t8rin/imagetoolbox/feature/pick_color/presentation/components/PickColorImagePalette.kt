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

package com.t8rin.imagetoolbox.feature.pick_color.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.colordetector.rememberImageColorPaletteState
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.utils.helper.Clipboard
import com.wanbaohe.core.ui.utils.helper.toHex
import com.wanbaohe.core.ui.widget.color_picker.AddToPaletteSheet
import com.wanbaohe.core.ui.widget.color_picker.LargeColorBlock
import com.wanbaohe.core.ui.widget.enhanced.hapticsClickable
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle

/**
 * 图片配色条：选完图片直接把这幅图的主色摊开，不用一个个去点。
 *
 * - 点色块：把它设为当前取到的颜色（等价于点图片上那个位置）；
 * - 长按色块：复制色值；
 * - 「整组存为调色板」：一次把这组颜色写进某组调色板。
 *
 * 量化用组件里预缩到 ~180px 的小图，主线程只做 25k 像素，不会卡。
 */
@Composable
internal fun PickColorImagePalette(
    paletteSource: ImageBitmap,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val paletteState = rememberImageColorPaletteState(
        imageBitmap = paletteSource,
        maximumColorCount = 6
    )
    val colors = remember(paletteState.paletteData) {
        paletteState.paletteData
            .map { it.colorData.color.toArgb() }
            .distinct()
            .take(6)
    }

    if (colors.isEmpty()) return

    var showAddSheet by remember { mutableStateOf(false) }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        style = GlassStyle.Medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.image_palette),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.image_palette_save_all),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .hapticsClickable { showAddSheet = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                colors.forEach { argb ->
                    val color = Color(argb)
                    LargeColorBlock(
                        color = color,
                        onClick = { onColorSelected(color) },
                        onLongClick = {
                            Clipboard.copy(
                                text = color.toHex(),
                                message = R.string.color_copied
                            )
                        },
                        showCopyIcon = false,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = 4.dp,
                        textStyle = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    )
                }
            }
        }
    }

    AddToPaletteSheet(
        colors = colors,
        visible = showAddSheet,
        onDismiss = { showAddSheet = false }
    )
}
