/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlexBox
import androidx.compose.foundation.layout.FlexWrap
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.domain.image.model.BlendingMode
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.line.LineKeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMarkupLayers
import com.wanbaohe.core.ui.widget.enhanced.EnhancedChip
import com.wanbaohe.core.ui.widget.icon_shape.IconShapeContainer
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.core.ui.widget.modifier.flatGlassContainer

/** 常用叠加模式，顺序按「基础 → 变亮/变暗 → 对比 → 色彩」排列。 */
private val commonOverlayModes = listOf(
    BlendingMode.SrcOver,
    BlendingMode.Multiply,
    BlendingMode.Screen,
    BlendingMode.Overlay,
    BlendingMode.Softlight,
    BlendingMode.Hardlight,
    BlendingMode.Darken,
    BlendingMode.Lighten,
    BlendingMode.ColorDodge,
    BlendingMode.ColorBurn,
    BlendingMode.Difference,
    BlendingMode.Exclusion,
    BlendingMode.Plus,
    BlendingMode.Hue,
    BlendingMode.Saturation,
    BlendingMode.Color,
    BlendingMode.Luminosity,
)

@StringRes
private fun BlendingMode.labelRes(): Int? = when (this) {
    BlendingMode.SrcOver -> R.string.blend_mode_normal
    BlendingMode.Multiply -> R.string.blend_mode_multiply
    BlendingMode.Screen -> R.string.blend_mode_screen
    BlendingMode.Overlay -> R.string.blend_mode_overlay
    BlendingMode.Softlight -> R.string.blend_mode_soft_light
    BlendingMode.Hardlight -> R.string.blend_mode_hard_light
    BlendingMode.ColorDodge -> R.string.blend_mode_color_dodge
    BlendingMode.ColorBurn -> R.string.blend_mode_color_burn
    BlendingMode.Darken -> R.string.blend_mode_darken
    BlendingMode.Lighten -> R.string.blend_mode_lighten
    BlendingMode.Difference -> R.string.blend_mode_difference
    BlendingMode.Exclusion -> R.string.blend_mode_exclusion
    BlendingMode.Plus -> R.string.blend_mode_add
    BlendingMode.Hue -> R.string.blend_mode_hue
    BlendingMode.Saturation -> R.string.blend_mode_saturation
    BlendingMode.Color -> R.string.blend_mode_color
    BlendingMode.Luminosity -> R.string.blend_mode_luminosity
    else -> null
}

/**
 * 叠加模式选择器。
 *
 * 原来的 DataSelector 把 29 个 PorterDuff 名字平铺成一行会横向溢出、还全是英文，
 * 这里改成「标题行显示当前模式 + 展开后按常用模式换行排列」：
 * - 折叠时不挤占横向空间，当前值一眼可见；
 * - 展开用 FlexBox 自动换行，永远不会被容器裁掉；
 * - 文案全部本地化，老预设里存在的非常用模式仍会追加在末尾。
 */
@Composable
fun OverlayModeSelector(
    value: BlendingMode,
    onValueChange: (BlendingMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "overlayModeChevronRotation",
    )
    val modes = remember(value) {
        if (value in commonOverlayModes) commonOverlayModes else commonOverlayModes + value
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(ShapeDefaults.large)
            .flatGlassContainer(
                shape = ShapeDefaults.large,
                resultPadding = 0.dp,
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconShapeContainer(enabled = true) {
                Icon(
                    imageVector = Icons.Outlined.LineMarkupLayers,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.overlay_mode),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = blendModeLabel(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Outlined.LineKeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotation),
            )
        }
        AnimatedVisibility(visible = expanded) {
            FlexBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                config = {
                    wrap(FlexWrap.Wrap)
                    columnGap(8.dp)
                    rowGap(8.dp)
                },
            ) {
                modes.forEach { mode ->
                    val selected = mode == value
                    EnhancedChip(
                        selected = selected,
                        onClick = { onValueChange(mode) },
                        selectedColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(14.dp),
                                )
                            }
                            Text(
                                text = blendModeLabel(mode),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun blendModeLabel(mode: BlendingMode): String {
    val res = mode.labelRes()
    return if (res != null) stringResource(res) else mode.toString()
}
