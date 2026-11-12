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

package com.shifenmiao.model.blog

import android.os.Parcelable
import com.shifenmiao.model.StrapiImage
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize

@Parcelize
@Serializable
data class BlogItem(
    val id: Int = 0,
    val title: String = "",
    val content: String? = null,
    val summary: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val publishedAt: Long? = null,
    val fixed: Boolean = false,
    val type: Int = 1,
    val picture: List<StrapiImage>? = null,
    val author: Author? = null,
    val tags: List<Tag> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class Author(
    val avatar: String? = null,
    val id: Int = 0,
    val nickname: String? = null,
    val username: String? = null
) : Parcelable

@Parcelize
@Serializable
data class Tag(
    val desc: String = "",
    val id: Int = 0,
    val name: String = ""
) : Parcelable


@Serializable
data class FeedbackRequest(
    val title: String,
    val content: String,
    val pictureIds: List<Int>,
    val tagIds: List<Int>,
    val blogType: Int = 1
)