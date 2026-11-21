/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.t8rin.imagetoolbox.core.filters.presentation.widget.filterStrip

import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.error
import coil3.request.transformations
import coil3.transform.Transformation
import com.t8rin.imagetoolbox.core.domain.transformation.Transformation as DomainTransformation
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.Dots
import com.wanbaohe.core.ui.utils.helper.LocalFilterPreviewModelProvider
import com.wanbaohe.core.ui.utils.helper.toCoil
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.glassBackground
import com.wanbaohe.core.ui.widget.image.Picture
import com.wanbaohe.core.ui.widget.modifier.ShapeDefaults

/**
 * 公共滤镜横滚条：浮在画布底部、操作栏上方的不透明玻璃卡片。
 *
 * 图片创作与图片水印共用这一层：首项「原图」清除滤镜，中间是传入的快捷滤镜分组，
 * 末尾「更多」由调用方打开各自的完整滤镜目录。缩略图用静态预览图 + coil
 * transformation，稳定 cacheKey 保证同一滤镜只渲染一次；选中态只看「单个滤镜」，
 * 叠加了多个滤镜时不误高亮任何一张缩略图。
 */
@Composable
fun FilterStrip(
    filters: List<UiFilter<*>>,
    selectedFilter: UiFilter<*>?,
    activeFilterCount: Int,
    transformation: (UiFilter<*>) -> DomainTransformation<Bitmap>,
    onFilterSelected: (UiFilter<*>) -> Unit,
    onClearFilters: () -> Unit,
    onOpenFullCatalog: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    action: @Composable (() -> Unit)? = null,
) {
    val previewData = LocalFilterPreviewModelProvider.current.preview.data
    val sourceHash = previewData.hashCode()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassBackground(
                style = GlassStyle.None,
                shape = ShapeDefaults.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            )
            // 吞掉卡片空白处的点按，避免穿透成画布的「点空白取消选择」。
            .pointerInput(Unit) { detectTapGestures { } }
            // 外层贴齐屏幕两侧，缩进放到内容层。
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (title != null || action != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                action?.invoke()
            }
            Spacer(Modifier.height(6.dp))
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item(key = "original") {
                FilterThumbnail(
                    source = previewData,
                    transformation = null,
                    cacheKey = "original@$sourceHash",
                    label = stringResource(R.string.original),
                    selected = activeFilterCount == 0,
                    onClick = onClearFilters
                )
            }
            items(
                items = filters,
                key = { it::class.simpleName ?: it.title.toString() }
            ) { filter ->
                FilterThumbnail(
                    source = previewData,
                    transformation = remember(filter) { transformation(filter).toCoil() },
                    cacheKey = "${filter::class.simpleName}@$sourceHash",
                    label = stringResource(filter.title),
                    selected = activeFilterCount == 1 &&
                        selectedFilter?.let { it::class == filter::class } == true,
                    onClick = { onFilterSelected(filter) }
                )
            }
            item(key = "more") {
                MoreFiltersItem(onClick = onOpenFullCatalog)
            }
        }
    }
}

@Composable
private fun FilterThumbnail(
    source: Any?,
    transformation: Transformation?,
    cacheKey: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val request = remember(source, transformation) {
        ImageRequest.Builder(context)
            .data(source)
            .error(R.drawable.filter_preview_source)
            .apply { transformation?.let { transformations(it) } }
            .diskCacheKey(cacheKey)
            .memoryCacheKey(cacheKey)
            .size(THUMBNAIL_PX, THUMBNAIL_PX)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request)
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(THUMBNAIL_ITEM_WIDTH)
            .clip(ShapeDefaults.default)
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Picture(
            model = painter,
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(THUMBNAIL_SIZE)
                .clip(ShapeDefaults.small)
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else MaterialTheme.colorScheme.outlineVariant,
                    shape = ShapeDefaults.small
                )
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MoreFiltersItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(THUMBNAIL_ITEM_WIDTH)
            .clip(ShapeDefaults.default)
            .clickable(onClick = onClick)
            .padding(2.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(THUMBNAIL_SIZE)
                .clip(ShapeDefaults.small)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = ShapeDefaults.small
                )
        ) {
            Icon(
                imageVector = Icons.Rounded.Dots,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxSize(0.4f)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.filter_more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val THUMBNAIL_SIZE = 56.dp
private val THUMBNAIL_ITEM_WIDTH = 64.dp
private const val THUMBNAIL_PX = 240
