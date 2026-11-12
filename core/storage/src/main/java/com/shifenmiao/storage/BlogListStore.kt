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

package com.shifenmiao.storage

import com.shifenmiao.model.blog.BlogItem
import com.shifenmiao.model.common.DataList
import com.tencent.mmkv.MMKV

object BlogListStore {

    // 按语言隔离：博客内容按 locale 下发
    private val mmkv: MMKV get() = localizedMmkv(MMKVName.BLOG_LIST)
    private val CACHE_TIMEOUT = RemoteConfigStorage.getRemoteConfig().cacheTimeout
    private const val KEY_BLOGS_PREFIX = "blogs_page_"
    private const val KEY_BLOG_DETAIL_PREFIX = "blog_detail_"

    fun saveBlogDetail(blogId: Int, blog: BlogItem) {
        val key = "$KEY_BLOG_DETAIL_PREFIX$blogId"
        mmkv.encode(key, blog, CACHE_TIMEOUT ?: (60 * 60))
    }

    fun loadBlogDetail(blogId: Int): BlogItem? {
        val key = "$KEY_BLOG_DETAIL_PREFIX$blogId"
        return mmkv.decodeParcelable(key, BlogItem::class.java)
    }

    fun clearBlogDetailCache(blogId: Int) {
        val key = "$KEY_BLOG_DETAIL_PREFIX$blogId"
        mmkv.remove(key)
    }

    fun clearAllBlogDetailsCache() {
        val allKeys = mmkv.allKeys()
        allKeys?.filter { it.startsWith(KEY_BLOG_DETAIL_PREFIX) }?.forEach {
            mmkv.remove(it)
        }
    }

    fun saveBlogs(page: Int, pageSize: Int, blogs: DataList<BlogItem>) {
        val key = "$KEY_BLOGS_PREFIX${page}_$pageSize"
        mmkv.encode(key, blogs, CACHE_TIMEOUT ?: (60 * 60))
    }

    fun loadBlogs(page: Int, pageSize: Int): DataList<BlogItem>? {
        val key = "$KEY_BLOGS_PREFIX${page}_$pageSize"
        @Suppress("UNCHECKED_CAST")
        return mmkv.decodeParcelable(key, DataList::class.java) as? DataList<BlogItem>
    }

    fun clearBlogsCache() {
        val allKeys = mmkv.allKeys()
        allKeys?.filter { it.startsWith(KEY_BLOGS_PREFIX) }?.forEach {
            mmkv.remove(it)
        }
    }

    fun clearBlogCachePage(page: Int, pageSize: Int) {
        val key = "$KEY_BLOGS_PREFIX${page}_$pageSize"
        mmkv.remove(key)
    }
}