/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.presentation.panels

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.resources.R
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

private val THUMB_SIZE = 84.dp

@Composable
fun ImageBatchPanel(
    uris: List<Uri>,
    selectedUri: Uri,
    onSelect: (Uri) -> Unit,
    onRemove: (Uri) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val thumbShape = ShapeDefaults.large
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(uris, key = Uri::toString) { uri ->
            Box(modifier = Modifier.size(THUMB_SIZE)) {
                GlassCard(
                    onClick = { onSelect(uri) },
                    modifier = Modifier
                        .size(THUMB_SIZE)
                        .then(
                            if (uri == selectedUri) {
                                Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primaryContainer,
                                    thumbShape,
                                )
                            } else Modifier,
                        ),
                    shape = thumbShape,
                ) {
                    // 图片按卡片同一圆角裁切并填满，不再内缩、不再露出方角，
                    // 也就不会出现「圆角后面还有东西」。照片不需要棋盘格。
                    Picture(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(THUMB_SIZE),
                        contentScale = ContentScale.Crop,
                        shape = thumbShape,
                        showTransparencyChecker = false,
                        shimmerEnabled = false,
                    )
                }
                // 用普通 Box + clickable 而非 EnhancedIconButton：后者会强制
                // 48dp 最小点击区，导致按钮是个大白圆、里面的叉又特别小。
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(24.dp)
                        .glassBackground(
                            GlassStyle.Regular,
                            ShapeDefaults.circle,
                            MaterialTheme.colorScheme.surfaceContainer,
                        )
                        .clickable { onRemove(uri) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        item {
            GlassCard(onClick = onAdd, modifier = Modifier.size(THUMB_SIZE), shape = thumbShape) {
                Box(modifier = Modifier.size(THUMB_SIZE), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.add),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
