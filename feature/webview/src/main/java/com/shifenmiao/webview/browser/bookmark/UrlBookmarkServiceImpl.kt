package com.shifenmiao.webview.browser.bookmark

import androidx.room.withTransaction
import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.shifenmiao.interfaces.bookmark.UrlBookmark
import com.shifenmiao.interfaces.bookmark.UrlBookmarkCategory
import com.shifenmiao.interfaces.bookmark.UrlBookmarkService
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.webview.browser.BrowserRepository
import com.t8rin.logger.makeLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [UrlBookmarkService] 的 Room 实现：浏览器书签 = item 表中
 * `list_type = HTML`、`source = LOCAL`、url 非空的条目，分类走全局 category 池。
 *
 * 写库序列对齐 CreateHtmlComponent.saveItem（item → userState → 分类关联），
 * 但浏览器书签默认不抓取 HTML 快照（不写 item_data,hasSnapshot = false）。
 *
 * 首次访问时把 MMKV(web_browser)里的旧书签/文件夹一次性迁入 Room:
 * 文件夹按名建分类（重名复用），书签按 url 去重，完成后清空旧 key 并置迁移标记；
 * 迁移失败不阻断，下次访问自动重试。
 */
@Singleton
class UrlBookmarkServiceImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val browserRepository: BrowserRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) : UrlBookmarkService {

    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()
    private val categoryDao = appDatabase.categoryDao()

    private val migrationMutex = Mutex()

    @Volatile
    private var isMigrated = false

    // ── 收藏查询 ────────────────────────────────────────────────────────

    override fun observeBookmarks(): Flow<List<UrlBookmark>> =
        itemDao.observeItemsByListTypeAndSource(ListItemType.HTML.id, Source.LOCAL)
            .onStart { ensureMigrated() }
            .map { rows -> rows.toUrlBookmarks() }

    override suspend fun listBookmarks(): List<UrlBookmark> {
        ensureMigrated()
        return itemDao.getItemsByListTypeAndSource(ListItemType.HTML.id, Source.LOCAL)
            .toUrlBookmarks()
    }

    override suspend fun findByUrl(url: String): UrlBookmark? {
        ensureMigrated()
        val item = itemDao.getItemByUrlAndSource(ListItemType.HTML.id, Source.LOCAL, url.trim())
            ?: return null
        val categories = itemDao.getItemWithCategoriesById(item.id).firstOrNull()?.categories
            ?: emptyList()
        return UrlBookmark(
            itemId = item.id.toLong(),
            url = item.url,
            title = item.title,
            createdAt = item.createdAt,
            categoryIds = categories.map { it.id.toLong() },
            hasSnapshot = itemDataDao.getDataLinkByItemId(item.id) != null,
        )
    }

    /** 书签必须有 url 才可点击打开，无 url 的本地 HTML 条目不进入书签视图。 */
    private suspend fun List<ItemWithCategories>.toUrlBookmarks(): List<UrlBookmark> {
        val visible = filter { it.item.url.isNotBlank() }
        if (visible.isEmpty()) return emptyList()
        val snapshotItemIds = itemDataDao.getLinkedItemIds(visible.map { it.item.id }).toSet()
        return visible.map { row ->
            UrlBookmark(
                itemId = row.item.id.toLong(),
                url = row.item.url,
                title = row.item.title,
                createdAt = row.item.createdAt,
                categoryIds = row.categories.map { it.id.toLong() },
                hasSnapshot = row.item.id in snapshotItemIds,
            )
        }
    }

    // ── 收藏写入 ────────────────────────────────────────────────────────

    override suspend fun saveBookmark(url: String, title: String, categoryId: Long?): Long {
        ensureMigrated()
        return saveBookmarkInternal(
            url = url,
            title = title,
            categoryId = categoryId,
            createdAt = null,
        )
    }

    private suspend fun saveBookmarkInternal(
        url: String,
        title: String,
        categoryId: Long?,
        createdAt: Long?,
    ): Long {
        val normalizedUrl = url.trim()
        require(normalizedUrl.isNotEmpty()) { "url 不能为空" }
        val now = System.currentTimeMillis()
        val resolvedTitle = title.trim().ifEmpty { normalizedUrl }
        val targetCategoryId = (categoryId ?: ensureDefaultCategory()).toInt()

        return appDatabase.withTransaction {
            val existing = itemDao.getItemByUrlAndSource(ListItemType.HTML.id, Source.LOCAL, normalizedUrl)
            val itemId = itemDao.upsertItem(
                ItemEntity(
                    id = existing?.id ?: 0,
                    remoteId = null,
                    source = Source.LOCAL,
                    listType = ListItemType.HTML.id,
                    title = resolvedTitle,
                    description = existing?.description ?: "",
                    url = normalizedUrl,
                    iconName = resolvedTitle.firstOrNull()?.toString(),
                    createdAt = createdAt ?: existing?.createdAt ?: now,
                    updatedAt = now,
                    publishedAt = existing?.publishedAt ?: now,
                    documentId = existing?.documentId,
                )
            )

            // 本地可编辑 + 置顶；已存在的用户态（收藏/密码保护）保留不覆盖
            val currentState = itemDao.getUserStateByItemId(itemId)
            itemDao.upsertUserState(
                (currentState ?: ItemUserState(itemId = itemId)).copy(
                    isPinned = true,
                    pinnedAt = currentState?.pinnedAt ?: now,
                    canEdit = true,
                    updatedAt = now,
                )
            )

            categoryDao.deleteCategoriesByItemId(itemId)
            itemDao.insertItemCategoryCrossRef(
                ItemCategoryCrossRef(itemId = itemId, categoryId = targetCategoryId)
            )
            itemId.toLong()
        }
    }

    override suspend fun removeBookmark(itemId: Long) {
        ensureMigrated()
        appDatabase.withTransaction {
            val item = itemDao.getItemEntityById(itemId.toInt()) ?: return@withTransaction
            activityLogRecorder.deleteRelatedLogsForItem(item)
            itemDao.deleteItemById(itemId.toInt())
        }
    }

    override suspend fun removeByUrl(url: String) {
        ensureMigrated()
        val item = itemDao.getItemByUrlAndSource(ListItemType.HTML.id, Source.LOCAL, url.trim())
            ?: return
        removeBookmark(item.id.toLong())
    }

    override suspend fun assignCategory(itemId: Long, categoryId: Long?) {
        ensureMigrated()
        val targetCategoryId = (categoryId ?: ensureDefaultCategory()).toInt()
        appDatabase.withTransaction {
            categoryDao.deleteCategoriesByItemId(itemId.toInt())
            itemDao.insertItemCategoryCrossRef(
                ItemCategoryCrossRef(itemId = itemId.toInt(), categoryId = targetCategoryId)
            )
        }
    }

    // ── 分类（全局池） ──────────────────────────────────────────────────

    override fun observeCategories(): Flow<List<UrlBookmarkCategory>> =
        categoryDao.getAllCategories()
            .onStart { ensureMigrated() }
            .map { categories -> categories.map { it.toBookmarkCategory() } }

    override suspend fun listCategories(): List<UrlBookmarkCategory> {
        ensureMigrated()
        return categoryDao.getAllCategoriesList().map { it.toBookmarkCategory() }
    }

    override suspend fun createCategory(name: String): Long {
        ensureMigrated()
        return createCategoryInternal(name)
    }

    private suspend fun createCategoryInternal(name: String): Long {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "分类名不能为空" }
        categoryDao.getCategoryByName(trimmed)?.let { return it.id.toLong() }
        return categoryDao.insert(
            Category(name = trimmed, canEdit = true, source = Source.LOCAL)
        )
    }

    override suspend fun renameCategory(categoryId: Long, newName: String): Boolean {
        ensureMigrated()
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return false
        val category = categoryDao.getCategoryById(categoryId.toInt()) ?: return false
        if (!category.canModify) return false
        // category 表有 unique(name, source)，改名撞上已有本地分类时拒绝
        val conflict = categoryDao.getCategoryByName(trimmed)
        if (conflict != null && conflict.id != category.id && conflict.source == Source.LOCAL) {
            return false
        }
        categoryDao.updateCategoryName(category.id, trimmed)
        categoryDao.updateCategoryUpdatedAt(category.id, System.currentTimeMillis())
        return true
    }

    override suspend fun deleteCategory(categoryId: Long): Boolean {
        ensureMigrated()
        val category = categoryDao.getCategoryById(categoryId.toInt()) ?: return false
        if (!category.canModify) return false
        appDatabase.withTransaction {
            itemDao.deleteItemCategoryCrossRefsByCategoryId(category.id)
            categoryDao.deleteCategory(category.id)
            reassignOrphanBookmarks()
        }
        return true
    }

    /**
     * 分类删除后，把失去所有分类的收藏条目回落到默认分类，
     * 避免条目因列表 INNER JOIN item_category 而不可见（与笔记默认分类同理）。
     */
    private suspend fun reassignOrphanBookmarks() {
        val orphans = itemDao.getUncategorizedItems(ListItemType.HTML.id, Source.LOCAL)
        if (orphans.isEmpty()) return
        val defaultCategoryId = ensureDefaultCategory().toInt()
        orphans.forEach { item ->
            itemDao.insertItemCategoryCrossRef(
                ItemCategoryCrossRef(itemId = item.id, categoryId = defaultCategoryId)
            )
        }
    }

    /** 默认分类：复用全局「未分类」（笔记同款兜底），找不到才创建。 */
    private suspend fun ensureDefaultCategory(): Long {
        val name = AppContext.getString(R.string.note_default_category)
        categoryDao.getCategoryByName(name)?.let { return it.id.toLong() }
        return categoryDao.insert(
            Category(name = name, canEdit = true, source = Source.LOCAL)
        )
    }

    private fun Category.toBookmarkCategory() = UrlBookmarkCategory(
        id = id.toLong(),
        name = name,
        canEdit = canEdit,
        isLocal = source == Source.LOCAL,
    )

    private val Category.canModify: Boolean
        get() = canEdit && source == Source.LOCAL

    // ── MMKV → Room 一次性迁移 ──────────────────────────────────────────

    private suspend fun ensureMigrated() {
        if (isMigrated) return
        migrationMutex.withLock {
            if (isMigrated) return
            runCatching { migrateLegacyBookmarksIfNeeded() }
                .onSuccess { isMigrated = true }
                .onFailure { "UrlBookmarkServiceImpl migrate failed: ${it.message}".makeLog(TAG) }
        }
    }

    private suspend fun migrateLegacyBookmarksIfNeeded() {
        if (browserRepository.isBookmarkMigrationDone()) return
        val legacyFolders = browserRepository.loadBookmarkFolders()
        val legacyBookmarks = browserRepository.loadBookmarks()
        if (legacyFolders.isEmpty() && legacyBookmarks.isEmpty()) {
            browserRepository.markBookmarkMigrationDone()
            return
        }

        val categoryIdsByFolderId = legacyFolders
            .filter { it.name.isNotBlank() }
            .associate { it.id to createCategoryInternal(it.name) }

        legacyBookmarks.forEach { bookmark ->
            runCatching {
                saveBookmarkInternal(
                    url = bookmark.url,
                    title = bookmark.title,
                    categoryId = bookmark.folderId
                        .takeIf { it.isNotBlank() }
                        ?.let { categoryIdsByFolderId[it] },
                    createdAt = bookmark.timestamp,
                )
            }.onFailure {
                "UrlBookmarkServiceImpl skip legacy bookmark ${bookmark.url}: ${it.message}".makeLog(TAG)
            }
        }

        browserRepository.saveBookmarks(emptyList())
        browserRepository.saveBookmarkFolders(emptyList())
        browserRepository.markBookmarkMigrationDone()
    }

    companion object {
        private const val TAG = "UrlBookmarkServiceImpl"
    }
}
