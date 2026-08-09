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