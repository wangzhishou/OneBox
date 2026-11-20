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

package com.shifenmiao.online.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar
import com.wanbaohe.com.string.TimeFormatter
import java.util.Date
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.components.blog.BlogTagChips
import com.shifenmiao.core.R
import com.shifenmiao.model.StrapiImage
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.widget.glass.GlassCard
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface

@Composable
fun PlaygroundCard(
    blog: BlogItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.large
    val coverImage = blog.picture?.firstOrNull()

    GlassCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (coverImage != null) {
                // 小红书封面：比例夹在 3:4 和 4:3 之间，超出居中裁掉。
                // 只用接口给的原始宽高。解码后的 bitmap 是按显示框采样的，
                // 拿它再改比例会换尺寸并触发第二次加载。
                val coverRatio = xiaohongshuCoverRatio(coverImage.width, coverImage.height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(coverRatio),
                    contentAlignment = Alignment.Center
                ) {
                    CachedCoverImage(
                        image = coverImage,
                        contentDescription = blog.title,
                        modifier = Modifier.matchParentSize(),
                    )

                    if (blog.fixed) {
                        GlassSurface(
                            style = GlassStyle.Thin,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimens.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = blog.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                blog.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                BlogTagChips(
                    tags = blog.tags,
                    onTagClick = { onClick() },
                )

                PlaygroundAuthorRow(
                    name = blog.author?.nickname.orEmpty(),
                    avatar = blog.author?.avatar,
                    date = blog.publishedAt
                )
            }
        }
    }
}

@Composable
private fun CachedCoverImage(
    image: StrapiImage,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val cacheKey = remember(image.url, image.hash, image.updatedAt) {
        coverCacheKey(image)
    }
    val memoryHit = remember(cacheKey) { memoryCacheContains(context, cacheKey) }
    val diskHit = remember(cacheKey) { diskCacheContains(context, cacheKey) }
    var forceNetwork by remember(cacheKey) { mutableStateOf(false) }
    var showSpinner by remember(cacheKey) { mutableStateOf(!memoryHit && !diskHit) }
    val request = remember(cacheKey, image.url, forceNetwork, context) {
        ImageRequest.Builder(context)
            .data(image.url)
            .diskCacheKey(cacheKey)
            .memoryCacheKey(cacheKey)
            .placeholderMemoryCacheKey(MemoryCache.Key(cacheKey))
            .precision(Precision.INEXACT)
            .crossfade(false)
            .networkCachePolicy(
                if (diskHit && !forceNetwork) CachePolicy.DISABLED else CachePolicy.ENABLED
            )
            .build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            alignment = Alignment.Center,
            modifier = Modifier.matchParentSize(),
            onState = { state ->
                when (state) {
                    is AsyncImagePainter.State.Success -> showSpinner = false
                    is AsyncImagePainter.State.Error -> {
                        showSpinner = false
                        if (diskHit && !forceNetwork) forceNetwork = true
                    }
                    is AsyncImagePainter.State.Loading -> {
                        if (!memoryHit && !diskHit) showSpinner = true
                    }
                    else -> Unit
                }
            }
        )

        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * 封面缓存键：URL 路径 + 文件 hash + 更新时间。
 * 签名 query 会变，不参与键。路径、hash、updatedAt 都没变就命中旧缓存，不再请求。
 */
private fun coverCacheKey(image: StrapiImage): String {
    val stableUrl = image.url.substringBefore('#').substringBefore('?')
    val revision = buildString {
        image.hash?.takeIf { it.isNotBlank() }?.let { append(it) }
        val updatedAt = image.updatedAt
        if (updatedAt != null && updatedAt > 0L) {
            if (isNotEmpty()) append('|')
            append(updatedAt)
        }
    }
    return if (revision.isEmpty()) stableUrl else "$stableUrl|$revision"
}

private fun memoryCacheContains(context: Context, key: String): Boolean {
    return context.imageLoader.memoryCache?.get(MemoryCache.Key(key)) != null
}

private fun diskCacheContains(context: Context, key: String): Boolean {
    val cache = context.imageLoader.diskCache ?: return false
    return runCatching {
        cache.openSnapshot(key)?.use { true } ?: false
    }.getOrDefault(false)
}

@Composable
private fun PlaygroundAuthorRow(
    name: String,
    avatar: String?,
    date: Long?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Avatar(
                username = name,
                avatar = avatar,
                size = 20.dp,
                shape = RoundedCornerShape(50)
            )

            Text(
                text = name.ifBlank { stringResource(R.string.default_user_nickname) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        date?.let {
            Text(
                text = TimeFormatter.formatRelativeTime(Date(it)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
        }
    }
}

/** 小红书封面宽高比：最高 3:4，最宽 4:3，中间保持原比例。返回宽/高。 */
private const val CoverTallestRatio = 3f / 4f
private const val CoverWidestRatio = 4f / 3f

private fun xiaohongshuCoverRatio(width: Int?, height: Int?): Float {
    if (width == null || height == null || width <= 0 || height <= 0) {
        return CoverTallestRatio
    }
    return (width.toFloat() / height.toFloat()).coerceIn(CoverTallestRatio, CoverWidestRatio)
}
