package com.wanbaohe.markuplayers.presentation.editor

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
import com.t8rin.imagetoolbox.core.ui.widget.color_picker.ColorSelectionRow
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.transparencyChecker
import com.wanbaohe.markuplayers.R

/**
 * 画布背景设置面板:「棋盘格(默认)」/「纯色」两个选项卡,选择即时生效。
 * 纯色选项卡内嵌滚动色板 [ColorSelectionRow];纯色记忆在面板内,
 * 从棋盘格切回纯色时沿用上次颜色(默认白)。
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
        onDismiss = { onDismiss() },
        sheetContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.markup_canvas_background),
                    style = MaterialTheme.typography.titleMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnhancedChip(
                        selected = background is CanvasBackground.Checkerboard,
                        onClick = { onBackgroundChange(CanvasBackground.Checkerboard) },
                        selectedColor = MaterialTheme.colorScheme.secondary,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = stringResource(R.string.markup_canvas_bg_checkerboard),
                            maxLines = 1
                        )
                    }
                    EnhancedChip(
                        selected = background is CanvasBackground.Solid,
                        onClick = { onBackgroundChange(CanvasBackground.Solid(solidColor)) },
                        selectedColor = MaterialTheme.colorScheme.secondary,
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
                    CanvasBackground.Checkerboard -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(ShapeDefaults.default)
                            .transparencyChecker()
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
