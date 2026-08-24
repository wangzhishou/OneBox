package com.wanbaohe.textcard.presentation.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.textcard.R
import com.wanbaohe.textcard.presentation.screenLogic.TextCardComponent

/**
 * 装饰选择 Sheet:emoji 贴纸网格(core/resources Emoji.allIcons,导出经 assets SVG 解码)
 * + 固定角落位置(范围裁剪:不做画布内手势变换)+ 「无」。
 */
@Composable
fun DecorationPickerSheet(
    visible: Boolean,
    component: TextCardComponent,
    onDismiss: () -> Unit,
) {
    if (!visible) return
    val emojis = Emoji.allIcons()
    val decoration = component.decoration

    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = { onDismiss() },
        title = {
            Text(text = stringResource(R.string.textcard_decoration_title))
        },
        sheetContent = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // 位置由画布内自由拖动决定,Sheet 只管选贴纸
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    item(key = "none") {
                        DecorationCell(
                            selected = decoration.emojiIndex == null,
                            onClick = {
                                component.updateDecoration(decoration.copy(emojiIndex = null))
                            }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(
                                    text = stringResource(R.string.textcard_decoration_none),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    itemsIndexed(emojis) { index, uri ->
                        DecorationCell(
                            selected = decoration.emojiIndex == index,
                            onClick = {
                                component.updateDecoration(
                                    decoration.copy(emojiIndex = index)
                                )
                            }
                        ) {
                            Picture(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                showTransparencyChecker = false,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun DecorationCell(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(ShapeDefaults.small)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else MaterialTheme.colorScheme.outlineVariant,
                shape = ShapeDefaults.small
            )
            .clickable(onClick = onClick)
    ) {
        content()
    }
}
