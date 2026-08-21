package com.shifenmiao.database.chat_prompt.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.Source
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {

    @Query("SELECT * FROM item_prompt WHERE id = :id")
    suspend fun getPromptById(id: Int): PromptEntity?

    @Query("SELECT * FROM item_prompt WHERE source = :source AND remote_id = :remoteId LIMIT 1")
    suspend fun getPromptByRemoteId(remoteId: Int, source: Source = Source.REMOTE): PromptEntity?

    @Query("SELECT * FROM item_prompt WHERE source = :source AND document_id = :documentId LIMIT 1")
    suspend fun getPromptByDocumentId(documentId: String, source: Source = Source.REMOTE): PromptEntity?

    /** 命中老数据（按 remoteId 匹配）后回填 document_id；已有值时不覆盖。 */
    @Query("UPDATE item_prompt SET document_id = :documentId WHERE id = :id AND (document_id IS NULL OR document_id = '')")
    suspend fun backfillDocumentIdIfMissing(id: Int, documentId: String)

    @Query("SELECT * FROM item_prompt")
    suspend fun getAllPrompts(): List<PromptEntity>

    @Query("SELECT * FROM item_prompt ORDER BY updated_at DESC")
    fun getAllPromptsFlow(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM item_prompt WHERE source = :source")
    suspend fun getPromptsBySource(source: Source): List<PromptEntity>

    @Query("SELECT * FROM item_prompt WHERE source = :source ORDER BY updated_at DESC")
    fun getPromptsBySourceFlow(source: Source): Flow<List<PromptEntity>>

    @Query("SELECT * FROM item_prompt WHERE source = :source LIMIT 1")
    suspend fun getPromptBySource(source: Source): PromptEntity?

    // ── Link 查询 ──

    @Query("SELECT prompt_id FROM item_prompt_link WHERE item_id = :itemId LIMIT 1")
    suspend fun getPromptLinkByItemId(itemId: Int): Int?

    /** 反向：根据 prompt_id 找关联的 item_id。草稿加载时用。 */
    @Query("SELECT item_id FROM item_prompt_link WHERE prompt_id = :promptId LIMIT 1")
    suspend fun getItemIdByPromptId(promptId: Int): Int?

    @Query("SELECT item_id FROM item_prompt_link WHERE prompt_id = :promptId")
    suspend fun getItemIdsByPromptId(promptId: Int): List<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromptLink(link: com.shifenmiao.database.item.entity.ItemPromptLink)

    @Query("DELETE FROM item_prompt_link WHERE item_id = :itemId")
    suspend fun deletePromptLinkByItemId(itemId: Int): Int

    @Query("DELETE FROM item_prompt WHERE id = :id")
    suspend fun deletePromptById(id: Int)

    @Query("DELETE FROM item_prompt WHERE source = :source")
    suspend fun deleteBySource(source: Source)

    @Query("DELETE FROM item_prompt")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(promptEntity: PromptEntity): Long

    @Query(
        """
        UPDATE item_prompt SET
            title = :title,
            description = :description,
            prompt = :prompt,
            placeholder = :placeholder,
            templates = :templates,
            updated_at = :updateTime
        WHERE id = :id
        """
    )
    suspend fun updatePromptPreservingRemoteFields(
        id: Int,
        title: String,
        description: String?,
        prompt: String?,
        placeholder: String?,
        templates: String?,
        updateTime: Long,
    ): Int

    @Transaction
    suspend fun upsertLocalPrompt(promptEntity: PromptEntity): Int {
        if (promptEntity.id <= 0) {
            return insertPrompt(promptEntity.copy(id = 0)).toInt()
        }
        val existing = getPromptById(promptEntity.id)
        return if (existing != null) {
            updatePromptPreservingRemoteFields(
                id = existing.id,
                title = promptEntity.title,
                description = promptEntity.description,
                prompt = promptEntity.prompt,
                placeholder = promptEntity.placeholder,
                templates = promptEntity.templates,
                updateTime = promptEntity.updatedAt,
            )
            existing.id
        } else {
            insertPrompt(promptEntity.copy(id = 0)).toInt()
        }
    }

    @Transaction
    suspend fun upsertRemotePrompt(promptEntity: PromptEntity): Int {
        // 同步主键 documentId 优先，空时降级 remoteId（防御旧数据）
        val existing = promptEntity.documentId?.takeIf { it.isNotBlank() }
            ?.let { getPromptByDocumentId(documentId = it, source = promptEntity.source) }
            ?: promptEntity.remoteId?.let { getPromptByRemoteId(remoteId = it, source = promptEntity.source) }
        return if (existing != null) {
            // 必须用 UPDATE，不能用 REPLACE：REPLACE 会先删除旧行再插入，
            // 触发 item_prompt_link.prompt_id 的 FK CASCADE，导致 link 被误删。
            updatePromptPreservingRemoteFields(
                id = existing.id,
                title = promptEntity.title,
                description = promptEntity.description,
                prompt = promptEntity.prompt,
                placeholder = promptEntity.placeholder,
                templates = promptEntity.templates,
                updateTime = promptEntity.updatedAt,
            )
            promptEntity.documentId?.takeIf { it.isNotBlank() }
                ?.let { backfillDocumentIdIfMissing(existing.id, it) }
            existing.id
        } else {
            insertPrompt(promptEntity.copy(id = 0)).toInt()
        }
    }

    @Query(
        """
        SELECT * FROM item_prompt
        WHERE source = :source
          AND placeholder = :systemKey
        LIMIT 1
        """
    )
    suspend fun getSystemPromptByKey(systemKey: String, source: Source = Source.SYSTEM): PromptEntity?

    /**
     * 通过 category 过滤可用 prompt（用于 chip 切换 list）。
     * 替代旧版基于单一 item_resource_link 的查询；现在 JOIN item_prompt_link。
     */
    @Query("""
        SELECT DISTINCT p.* FROM item_prompt p
        INNER JOIN item_prompt_link l ON l.prompt_id = p.id
        INNER JOIN item i ON i.id = l.item_id
        INNER JOIN item_category ic ON ic.item_id = i.id
        WHERE ic.category_id = :categoryId
          AND i.list_type = :listType
        ORDER BY p.updated_at DESC
    """)
    fun getPromptsByCategoryId(categoryId: Int, listType: Int): Flow<List<PromptEntity>>
}
