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
import com.wanbaohe.core.ui.widget.enhanced.EnhancedIconButton
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

@Composable
fun ImageBatchPanel(
    uris: List<Uri>,
    selectedUri: Uri,
    onSelect: (Uri) -> Unit,
    onRemove: (Uri) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
    ) {
        items(uris, key = Uri::toString) { uri ->
            Box {
                GlassCard(
                    onClick = { onSelect(uri) },
                    modifier = if (uri == selectedUri) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.primaryContainer, ShapeDefaults.large)
                    } else Modifier,
                ) {
                    Picture(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier.size(84.dp).padding(3.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
                EnhancedIconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier.align(Alignment.TopEnd).size(32.dp),
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        item {
            GlassCard(onClick = onAdd, modifier = Modifier.size(84.dp)) {
                Box(modifier = Modifier.size(84.dp), contentAlignment = Alignment.Center) {
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
