package com.shifenmiao.database.data_draft

import com.shifenmiao.database.data_draft.dao.DataDraftDao
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 通用草稿操作助手，所有模块共用。
 * 解耦草稿的创建/更新逻辑，调用方只需关心业务数据。
 */
@Singleton
class DataDraftHelper @Inject constructor(
    private val dataDraftDao: DataDraftDao
) {

    /**
     * 创建一条新草稿，返回自增ID。
     */
    suspend fun createDraft(
        draftType: Int,
        title: String = "",
        description: String = "",
        url: String = "",
        data: String = "",
        selectedCategoryIds: Set<Int> = emptySet(),
        status: Int = DataDraftEntity.STATUS_DRAFT,
        itemId: Int? = null,
        relatedEntityId: Int? = null,
    ): Long {
        val draft = DataDraftEntity(
            title = title,
            description = description,
            url = url,
            data = data,
            selectedCategoryIds = encodeCategoryIds(selectedCategoryIds),
            draftType = draftType,
            status = status,
            itemId = itemId,
            relatedEntityId = relatedEntityId,
            updateTime = System.currentTimeMillis(),
        )
        return dataDraftDao.insert(draft)
    }

    /**
     * 更新已有草稿的内容字段（保持id/draftType/itemId不变）。
     */
    suspend fun updateDraft(
        draftId: Long,
        title: String? = null,
        description: String? = null,
        url: String? = null,
        data: String? = null,
        selectedCategoryIds: Set<Int>? = null,
        status: Int? = null,
        itemId: Int? = null,
        relatedEntityId: Int? = null,
    ) {
        val existing = dataDraftDao.getById(draftId) ?: return
        val updated = existing.copy(
            title = title ?: existing.title,
            description = description ?: existing.description,
            url = url ?: existing.url,
            data = data ?: existing.data,
            selectedCategoryIds = selectedCategoryIds?.let { encodeCategoryIds(it) }
                ?: existing.selectedCategoryIds,
            status = status ?: existing.status,
            itemId = itemId ?: existing.itemId,
            relatedEntityId = relatedEntityId ?: existing.relatedEntityId,
            updateTime = System.currentTimeMillis(),
        )
        dataDraftDao.update(updated)
    }

    /**
     * 创建或更新草稿，如果draftId>0则更新，否则创建并返回新ID。
     */
    suspend fun upsertDraft(
        draftId: Long = 0,
        draftType: Int,
        title: String = "",
        description: String = "",
        url: String = "",
        data: String = "",
        selectedCategoryIds: Set<Int> = emptySet(),
        status: Int = DataDraftEntity.STATUS_DRAFT,
        itemId: Int? = null,
        relatedEntityId: Int? = null,
    ): Long {
        return if (draftId > 0 && dataDraftDao.getById(draftId) != null) {
            updateDraft(
                draftId = draftId,
                title = title,
                description = description,
                url = url,
                data = data,
                selectedCategoryIds = selectedCategoryIds,
                status = status,
                itemId = itemId,
                relatedEntityId = relatedEntityId,
            )
            draftId
        } else {
            createDraft(
                draftType = draftType,
                title = title,
                description = description,
                url = url,
                data = data,
                selectedCategoryIds = selectedCategoryIds,
                status = status,
                itemId = itemId,
                relatedEntityId = relatedEntityId,
            )
        }
    }

    suspend fun getById(draftId: Long): DataDraftEntity? = dataDraftDao.getById(draftId)

    suspend fun getLatestByTypeAndRelatedEntityId(
        draftType: Int,
        relatedEntityId: Int,
    ): DataDraftEntity? = dataDraftDao.getLatestByTypeAndRelatedEntityId(
        draftType = draftType,
        relatedEntityId = relatedEntityId,
    )

    suspend fun deleteById(draftId: Long) = dataDraftDao.deleteById(draftId)

    companion object {
        fun encodeCategoryIds(ids: Set<Int>): String =
            Json.encodeToString(ids.toList())

        fun decodeCategoryIds(json: String): Set<Int> = try {
            Json.decodeFromString<List<Int>>(json).toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }
}

