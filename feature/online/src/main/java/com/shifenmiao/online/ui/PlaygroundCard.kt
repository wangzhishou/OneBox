package com.shifenmiao.online.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.wanbaohe.com.string.TimeFormatter
import java.util.Date
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.core.R
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.model.blog.Tag
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassBadge
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar

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
        containerAlpha = GlassStyle.Regular.backgroundAlpha,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (coverImage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(128.dp),
                    contentAlignment = Alignment.Center
                ) {
                    var isLoading by remember(coverImage.url) { mutableStateOf(true) }

                    AsyncImage(
                        // 网关 /api/blogs 的 formats 只保 thumbnail 小图,全宽封面直接用原图,避免放大模糊
                        model = coverImage.url,
                        contentDescription = blog.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                        onState = { state ->
                            isLoading = state is AsyncImagePainter.State.Loading
                        }
                    )

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }

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
                                tint = MaterialTheme.colorScheme.primary,
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
                    maxLines = 2,
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

                if (blog.tags.isNotEmpty()) {
                    TagRow(tags = blog.tags)
                }

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
private fun TagRow(tags: List<Tag>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            GlassBadge(
                style = GlassStyle.Thin,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            ) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
                )
            }
        }
    }
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
