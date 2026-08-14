package com.wanbaohe.markuplayers.presentation.tools.filter

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.t8rin.imagetoolbox.core.filters.presentation.model.UiFilter
import com.t8rin.imagetoolbox.core.resources.Icons
import com.t8rin.imagetoolbox.core.resources.icons.Dots
import com.t8rin.imagetoolbox.core.ui.utils.helper.LocalFilterPreviewModelProvider
import com.t8rin.imagetoolbox.core.ui.utils.helper.toCoil
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassDense
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.wanbaohe.markuplayers.R
import com.wanbaohe.markuplayers.presentation.screenLogic.MarkupLayersComponent
import com.t8rin.imagetoolbox.core.resources.R as CoreR

/**
 * 滤镜横滚面板(半浮动层):玻璃卡片浮在画布底部、底部 Tab 栏上方,不遮挡画布主体。
 * 首项「原图」= 清除滤镜;中间为「简单效果」分组([UiFilter.sortedGroupedEntries] 首组)
 * 的滤镜缩略图(与 AddFiltersSheet 目录同源的默认静态预览图 + coil transformation,
 * 面板打开一次渲染、不随画布变化,选中项高亮描边);末尾「更多」
 * 打开完整 AddFiltersSheet 目录(选中滤镜经 onFilterPicked 回填后关闭)。
 */
@Composable
fun FilterPanel(
    component: MarkupLayersComponent,
    onOpenFullCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 默认展示「简单效果」分组,避免全量 400+ 滤镜都渲染缩略图
    val filters = remember { UiFilter.sortedGroupedEntries.firstOrNull().orEmpty() }
    // 缩略图源图:滤镜体系的默认静态预览图(「预览图片」机制,默认 filter_preview_source),
    // 与「更多」目录里的缩略图观感一致;静态图源 + 稳定 cacheKey,coil 只渲染一次
    val previewData = LocalFilterPreviewModelProvider.current.preview.data
    val sourceHash = previewData.hashCode()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .glassDense(shape = ShapeDefaults.extraLarge)
            // 吞掉落在卡片空白处的点按,避免穿透成画布的「点空白取消选择」
            .pointerInput(Unit) { detectTapGestures { } }
            .padding(vertical = 10.dp)
    ) {
        item(key = "original") {
            FilterThumbnail(
                source = previewData,
                transformation = null,
                cacheKey = "original@$sourceHash",
                label = stringResource(R.string.markup_filter_original),
                selected = component.selectedFilter == null,
                onClick = { component.selectFilter(null) }
            )
        }
        items(
            items = filters,
            key = { it::class.simpleName ?: it.title.toString() }
        ) { filter ->
            FilterThumbnail(
                source = previewData,
                transformation = remember(filter) {
                    component.filterTransformation(filter).toCoil()
                },
                cacheKey = "${filter::class.simpleName}@$sourceHash",
                label = stringResource(filter.title),
                selected = component.selectedFilter?.let { it::class == filter::class } == true,
                onClick = { component.selectFilter(filter) }
            )
        }
        item(key = "more") {
            MoreFiltersItem(onClick = onOpenFullCatalog)
        }
    }
}

/** 单个滤镜缩略图:圆角小图 + 名称,选中项描边高亮;[transformation] 为空时即原图 */
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
            .error(CoreR.drawable.filter_preview_source)
            .apply { transformation?.let { transformations(it) } }
            .diskCacheKey(cacheKey)
            .memoryCacheKey(cacheKey)
            .size(THUMBNAIL_PX, THUMBNAIL_PX)
            .build()
    }
    val painter = rememberAsyncImagePainter(model = request)
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
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
                        MaterialTheme.colorScheme.primary
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

/** 末尾「更多」入口:打开完整滤镜目录 Sheet */
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
            text = stringResource(R.string.markup_filter_more),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 滤镜缩略图边长 */
private val THUMBNAIL_SIZE = 56.dp

/** 缩略图项宽度(略宽于图,容纳名称) */
private val THUMBNAIL_ITEM_WIDTH = 64.dp

/** coil 缩略图请求像素尺寸 */
private const val THUMBNAIL_PX = 240
