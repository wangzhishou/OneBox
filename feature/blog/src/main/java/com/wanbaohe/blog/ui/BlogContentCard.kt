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