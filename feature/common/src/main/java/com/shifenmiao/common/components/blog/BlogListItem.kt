package com.shifenmiao.common.components.blog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.base.ui.MarkdownContent
import com.shifenmiao.base.utils.DateUtils
import com.shifenmiao.common.components.Avatar
import com.shifenmiao.common.components.SelectableContentWrapper
import com.shifenmiao.common.components.common.ImageThumbnailRow
import com.shifenmiao.core.R
import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.model.blog.Tag
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStar

@Composable
fun BlogListItem(
    blog: BlogItem,
    onClick: (() -> Unit)? = null
) {
    BlogCard(
        onClick = onClick,
        content = {
            BlogListItemContent(blog = blog)
        }
    )
}

@Composable
fun BlogCard(
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.spaceNormal)
        ) {
            content()
        }
    }
}

@Composable
private fun BlogListItemContent(blog: BlogItem) {
    BlogHeader(title = blog.title, isFixed = blog.fixed)
    Spacer(modifier = Modifier.height(8.dp))

    blog.tags.takeIf { it.isNotEmpty() }?.let { tags ->
        TagRow(tags = tags)
        Spacer(modifier = Modifier.height(8.dp))
    }

    blog.picture?.takeIf { it.isNotEmpty() }?.let { images ->
        Spacer(modifier = Modifier.height(8.dp))
        ImageThumbnailRow(images = images)
        Spacer(modifier = Modifier.height(8.dp))
    }
    BlogSummary(summary = blog.summary)
    Spacer(modifier = Modifier.height(12.dp))
    AuthorInfo(
        authorName = blog.author?.nickname.orEmpty(),
        authorAvatar = blog.author?.avatar.orEmpty(),
        publishDate = blog.publishedAt
    )
}

@Composable
fun BlogHeader(
    modifier: Modifier = Modifier,
    title: String,
    isFixed: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        if (isFixed) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStar,
                contentDescription = "Fixed",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TagRow(tags: List<Tag>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            TagChip(tagName = tag.name)
        }
    }
}

@Composable
private fun TagChip(tagName: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = tagName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BlogBody(content: String? = null) {
    if (content?.isNotBlank() == true) {
        SelectableContentWrapper(
            textContent = content,
        ) {
            MarkdownContent(
                message = content,
                paddingValues = PaddingValues(
                    top = 0.dp,
                    start = 0.dp,
                    end = 0.dp,
                    bottom = 12.dp
                )
            )
        }
    }
}

@Composable
fun BlogSummary(summary: String? = null) {
    if (summary?.isNotBlank() == true) {
        MarkdownContent(
            message = summary,
            paddingValues = PaddingValues(
                top = 12.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = 12.dp
            )
        )
    }
}

@Composable
fun AuthorInfo(
    authorName: String,
    authorAvatar: String?,
    publishDate: Long?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            username = authorName,
            avatar = authorAvatar,
            size = 24.dp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = authorName.ifBlank { stringResource(R.string.default_user_nickname) },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = DateUtils.formatDate(publishDate),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}