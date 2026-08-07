package com.shifenmiao.ai.agent.tool

import com.shifenmiao.common.handle.ItemScreenAction
import com.shifenmiao.common.handle.ItemScreenResolution
import com.shifenmiao.common.handle.ItemScreenResolver
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.model.ListItemType
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemScreenCatalogRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val dataDraftHelper: DataDraftHelper
) {

    private val itemDao by lazy { appDatabase.itemEntityDao() }

    suspend fun searchItems(
        query: String,
        listItemType: ListItemType? = null,
        limit: Int = 8
    ): List<ItemWithCategories> {
        val items = if (query.isBlank()) {
            itemDao.getItemsByCategoryIdFlow().firstOrNull().orEmpty()
        } else {
            itemDao.searchByTitleOrDescriptionWithStats("%${query.trim()}%").firstOrNull().orEmpty()
        }
        return items.asSequence()
            .filter { listItemType == null || it.item.listType == listItemType.id }
            .sortedWith(compareByDescending<ItemWithCategoriesAndStats> { it.item.recommend }
                .thenByDescending { it.clickCount }
                .thenBy { it.item.title })
            .take(limit.coerceIn(1, 20))
            .map { it.toItemWithCategories() }
            .toList()
    }

    suspend fun getItemById(itemId: Int): ItemWithCategories? {
        return appDatabase.itemEntityDao()
            .getItemWithCategoriesById(itemId)
            .firstOrNull()
            ?.toItemWithCategories()
    }

    suspend fun findItemByExactTitle(title: String): ItemWithCategories? {
        val candidates = itemDao.searchByTitleOrDescription("%${title.trim()}%").firstOrNull().orEmpty()
        return candidates.firstOrNull { it.item.title.equals(title.trim(), ignoreCase = true) }
    }

    suspend fun findItemByStableKey(stableKey: String): ItemWithCategories? {
        val normalized = stableKey.trim()
        if (normalized.isBlank()) return null
        val candidates = itemDao.getItemsByCategoryIdFlow().firstOrNull().orEmpty()
        return candidates.firstOrNull { candidate ->
            val resource = com.shifenmiao.common.handle.ItemResourceResolver.resolve(
                appDatabase = appDatabase,
                itemId = candidate.item.id,
                listType = candidate.item.listType,
            )
            val open = ItemScreenResolver.resolveForOpen(
                candidate,
                agent = resource.agent,
                prompt = resource.prompt,
            )
            val edit = runCatching { ItemScreenResolver.resolveForEdit(candidate.item, dataDraftHelper) }.getOrNull()
            normalized.equals(open.routeKey, ignoreCase = true) ||
                normalized.equals(open.canonicalName, ignoreCase = true) ||
                normalized.equals(open.slug, ignoreCase = true) ||
                normalized.equals(edit?.routeKey, ignoreCase = true) ||
                normalized.equals(edit?.canonicalName, ignoreCase = true) ||
                normalized.equals(edit?.slug, ignoreCase = true)
        }?.toItemWithCategories()
    }

    suspend fun resolve(item: ItemWithCategories, action: ItemScreenAction): ItemScreenResolution {
        // PROMPT / AGENT 必须预查资源，否则 resolveForOpen 会因资源缺失返回错误。
        val resource = com.shifenmiao.common.handle.ItemResourceResolver.resolve(
            appDatabase = appDatabase,
            itemId = item.item.id,
            listType = item.item.listType,
        )
        return when (action) {
            ItemScreenAction.OPEN -> ItemScreenResolver.resolveForOpen(
                item,
                agent = resource.agent,
                prompt = resource.prompt,
            )
            ItemScreenAction.EDIT -> ItemScreenResolver.resolveForEdit(item.item, dataDraftHelper)
        }
    }

    fun supportsResultCallback(item: ItemEntity, action: ItemScreenAction): Boolean {
        val type = ListItemType.fromId(item.listType)
        return when (action) {
            ItemScreenAction.OPEN -> type == ListItemType.NOTE || type == ListItemType.HTML
            ItemScreenAction.EDIT -> type == ListItemType.NOTE ||
                type == ListItemType.HTML ||
                type == ListItemType.PROMPT
        }
    }
}
