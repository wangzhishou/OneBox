package com.wanbaohe.blog.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.components.blog.AuthorInfo
import com.shifenmiao.common.components.blog.BlogBody
import com.shifenmiao.common.components.blog.BlogCard
import com.shifenmiao.common.components.common.ImageThumbnailRow
import com.shifenmiao.model.blog.BlogItem

@Composable
fun BlogContentCard(blog: BlogItem) {
    BlogCard {
        blog.picture?.takeIf { it.isNotEmpty() }?.let { images ->
            Spacer(modifier = Modifier.height(8.dp))
            ImageThumbnailRow(images = images)
            Spacer(modifier = Modifier.height(8.dp))
        }
        BlogBody(content = blog.content)
        blog.author?.takeIf { true }?.let { author ->
            Spacer(modifier = Modifier.height(8.dp))
            AuthorInfo(
                authorName = author.nickname.orEmpty(),
                authorAvatar = author.avatar,
                publishDate = blog.publishedAt
            )
        }
    }
}