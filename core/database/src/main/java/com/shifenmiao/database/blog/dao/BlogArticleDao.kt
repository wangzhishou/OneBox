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

package com.shifenmiao.database.blog.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.shifenmiao.database.blog.entity.BlogArticleEntity

@Dao
interface BlogArticleDao {

    @Query(
        """
        SELECT * FROM blog_article
        WHERE blog_type = :blogType
        ORDER BY fixed DESC, published_at DESC, remote_id DESC
        """
    )
    fun pagingSourceByType(blogType: Int): PagingSource<Int, BlogArticleEntity>

    @Upsert
    suspend fun upsert(entities: List<BlogArticleEntity>)

    @Query("DELETE FROM blog_article WHERE blog_type = :blogType")
    suspend fun deleteByType(blogType: Int)

    @Query("SELECT * FROM blog_article WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int): BlogArticleEntity?

    @Query("SELECT COUNT(*) FROM blog_article WHERE blog_type = :blogType")
    suspend fun countByType(blogType: Int): Int
}
