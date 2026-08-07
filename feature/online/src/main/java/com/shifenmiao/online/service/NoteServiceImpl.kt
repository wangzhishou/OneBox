package com.shifenmiao.online.service

import com.shifenmiao.core.R
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataKind
import com.shifenmiao.database.item.entity.ItemDataLink
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.note.NoteDetail
import com.shifenmiao.model.note.NoteResult
import com.shifenmiao.model.note.NoteSaveParams
import com.shifenmiao.model.note.NoteService
import com.shifenmiao.model.note.NoteSummary
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.logger.makeLog
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteServiceImpl @Inject constructor(
    private val appDatabase: AppDatabase,
    private val activityLogRecorder: ActivityLogRecorder
) : NoteService {
    private val itemDao = appDatabase.itemEntityDao()
    private val itemDataDao = appDatabase.itemDataDao()
    private val categoryDao = appDatabase.categoryDao()

    override suspend fun saveNote(params: NoteSaveParams): NoteResult {
        if (params.title.isBlank()) return NoteResult.Error("标题不能为空")
        if (params.data.isBlank()) return NoteResult.Error("内容不能为空")

        return try {
            // 未选分类时兜底挂到默认分类，避免笔记因列表 INNER JOIN item_category 而不可见
            val categoryIds = params.categoryIds.ifEmpty { listOf(ensureDefaultCategory()) }
            val now = System.currentTimeMillis()
            val item = ItemEntity(
                id = params.existingItemId ?: 0,
                remoteId = null,
                source = Source.LOCAL,
                listType = ListItemType.NOTE.id,
                title = params.title,
                description = params.description,
                url = "",
                createdAt = now,
                updatedAt = now,
                publishedAt = now,
            )
            val itemId = itemDao.upsertItem(item)

            // 本地可编辑 + 置顶状态
            itemDao.upsertUserState(
                ItemUserState(
                    itemId = itemId,
                    isPinned = true,
                    pinnedAt = now,
                    canEdit = true,
                    updatedAt = now,
                )
            )

            // 资源行：data 走独立表 + link 关联
            // 编辑已有笔记时重用现有 data_id，避免 id=0 在 (source, remote_id) 唯一索引下
            // 因 remote_id=NULL 而重复插入孤儿行
            val existingDataId = params.existingItemId?.let { itemDataDao.getDataLinkByItemId(it) }
            val dataId = itemDataDao.upsert(
                ItemDataEntity(
                    id = existingDataId ?: 0,
                    title = params.title,
                    kind = ItemDataKind.MARKDOWN,
                    data = params.data,
                    source = Source.LOCAL,
                )
            )
            itemDataDao.insertLink(ItemDataLink(itemId = itemId, dataId = dataId))

            categoryDao.deleteCategoriesByItemId(itemId)
            categoryIds.forEach { categoryId ->
                itemDao.insertItemCategoryCrossRef(
                    ItemCategoryCrossRef(itemId = itemId, categoryId = categoryId.toInt())
                )
            }

            activityLogRecorder.recordNote(
                itemId = itemId,
                title = params.title,
                appTitle = AppContext.getString(Screen.CreateNote().title),
                description = AppContext.getString(
                    R.string.operation_history_description_note_saved,
                    params.title
                ),
                screenRoute = Screen.CreateNote().id.toString()
            )
            NoteResult.Success(itemId = itemId, title = params.title)
        } catch (e: Exception) {
            NoteResult.Error("保存笔记失败: ${e.message}")
        }
    }

    /**
     * 查找或创建笔记的默认分类（未手动选择分类时兜底使用）。
     */
    private suspend fun ensureDefaultCategory(): Long {
        val name = AppContext.getString(R.string.note_default_category)
        categoryDao.getCategoryByName(name)?.let { return it.id.toLong() }
        return categoryDao.insert(
            Category(
                name = name,
                canEdit = true,
                source = Source.LOCAL,
            )
        )
    }

    override suspend fun getNoteById(itemId: Int): NoteDetail? {
        return try {
            val itemWithRelation = itemDao.getItemById(itemId).firstOrNull() ?: return null
            val itemData = itemDataDao.getByItemId(itemWithRelation.item.id)?.data ?: ""

            NoteDetail(
                itemId = itemWithRelation.item.id,
                title = itemWithRelation.item.title,
                description = itemWithRelation.item.description,
                data = itemData,
                categories = itemWithRelation.categories.map { it.toModel() }
            )
        } catch (e: Exception) {
            "NoteServiceImpl.getNoteById: failed, ${e.message}".makeLog(TAG)
            null
        }
    }

    override suspend fun searchNotes(query: String): List<NoteSummary> {
        return try {
            val items = itemDao.searchByTitleOrDescription("%$query%").firstOrNull() ?: emptyList()
            items
                .filter { it.item.listType == ListItemType.NOTE.id }
                .map { item ->
                    NoteSummary(
                        itemId = item.item.id,
                        title = item.item.title,
                        description = item.item.description
                    )
                }
        } catch (e: Exception) {
            "NoteServiceImpl.searchNotes: failed, ${e.message}".makeLog(TAG)
            emptyList()
        }
    }


    companion object {
        private const val TAG = "NoteServiceImpl"
    }
}
