package com.shifenmiao.interfaces.bookmark

import kotlinx.coroutines.flow.Flow

/**
 * 网址收藏（浏览器书签）统一服务。
 *
 * 浏览器书签与「网址收藏」(listType=HTML 的本地条目）共用 Room 里的
 * item / item_category / category 表，此接口是唯一读写入口：
 * - 收藏条目 = `list_type = HTML` 且 `source = LOCAL` 且 url 非空的 item 行
 * - 分类直接使用全局 category 池（与笔记等共用，不做隔离）
 * - 历史记录不在此体系内（仍由浏览器 MMKV 存储）
 *
 * 设计原则：
 * - UI / AgentTool 只依赖此接口，不直接依赖 DAO / Room
 * - 实现方负责 MMKV 旧书签的一次性迁移，调用方无感知
 */
interface UrlBookmarkService {

    /** 观察全部网址收藏（含分类 id 与快照标记），按创建时间倒序。 */
    fun observeBookmarks(): Flow<List<UrlBookmark>>

    /** 一次性读取全部网址收藏。 */
    suspend fun listBookmarks(): List<UrlBookmark>

    /** 按 url 精确查找（入参会先 trim 规范化）。 */
    suspend fun findByUrl(url: String): UrlBookmark?

    /**
     * 新增或更新收藏：url 已存在时更新标题并归到指定分类。
     *
     * @param categoryId 为空时挂到默认分类（全局池中的「未分类」），
     *                   保证条目在 INNER JOIN item_category 的列表里可见
     * @return 条目 id（item 表主键）
     */
    suspend fun saveBookmark(url: String, title: String, categoryId: Long? = null): Long

    /** 按条目 id 删除收藏（级联清理 data / 分类关联 / 用户态）。 */
    suspend fun removeBookmark(itemId: Long)

    /** 按 url 删除收藏，不存在时静默成功。 */
    suspend fun removeByUrl(url: String)

    /** 观察全局分类池（含远端同步分类），按更新时间倒序。 */
    fun observeCategories(): Flow<List<UrlBookmarkCategory>>

    /** 一次性读取全局分类池。 */
    suspend fun listCategories(): List<UrlBookmarkCategory>

    /** 创建本地分类，同名（任意来源）时直接复用已有分类，返回分类 id。 */
    suspend fun createCategory(name: String): Long

    /** 重命名本地分类；远端/系统分类或名称冲突返回 false。 */
    suspend fun renameCategory(categoryId: Long, newName: String): Boolean

    /**
     * 删除本地分类：先清该分类的所有 item_category 关联，再删分类行；
     * 失去分类的收藏条目自动回落到默认分类，避免在列表中不可见。
     * 远端/系统分类返回 false。
     */
    suspend fun deleteCategory(categoryId: Long): Boolean

    /** 重新指定收藏条目的分类（单分类语义），categoryId 为空时挂到默认分类。 */
    suspend fun assignCategory(itemId: Long, categoryId: Long?)
}

/** 一条网址收藏（item 表投影 + 关联信息）。 */
data class UrlBookmark(
    val itemId: Long,
    val url: String,
    val title: String,
    val createdAt: Long,
    val categoryIds: List<Long>,
    /** 是否有离线 HTML 快照（item_data）；无快照时打开应回退在线加载 url。 */
    val hasSnapshot: Boolean,
)

/** 全局分类池里的一个分类（UI 只需要这些字段，isLocal 标记是否可编辑/删除）。 */
data class UrlBookmarkCategory(
    val id: Long,
    val name: String,
    val canEdit: Boolean,
    val isLocal: Boolean,
)
