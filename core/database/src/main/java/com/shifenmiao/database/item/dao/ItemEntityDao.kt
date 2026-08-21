package com.shifenmiao.database.item.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shifenmiao.database.agent.entity.ItemAgentEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.database.item.entity.ItemAgentLink
import com.shifenmiao.database.item.entity.ItemCategoryCrossRef
import com.shifenmiao.database.item.entity.ItemClickStatEntity
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemEntity
import com.shifenmiao.database.item.entity.ItemPromptLink
import com.shifenmiao.database.item.entity.ItemUserState
import com.shifenmiao.database.item.entity.ItemWithCategories
import com.shifenmiao.database.item.entity.ItemWithCategoriesAndStats
import com.shifenmiao.database.item.entity.ItemWithRelation
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemEntityDao {

    // ── Item CRUD ────────────────────────────────────────────────────────

    @Transaction
    suspend fun upsertItem(item: ItemEntity): Int {
        val existing = if (item.id > 0) getItemEntityById(item.id) else null
        return if (existing != null) {
            updateItem(
                item.copy(
                    id = existing.id,
                    remoteId = existing.remoteId,
                    source = existing.source,
                )
            )
            existing.id
        } else {
            insertItem(item.copy(id = 0)).toInt()
        }
    }

    @Update
    suspend fun updateItem(item: ItemEntity): Int

    @Query("SELECT * FROM item WHERE id = :id")
    suspend fun getItemEntityById(id: Int): ItemEntity?

    @Insert
    suspend fun insertItem(item: ItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItemIgnore(item: ItemEntity): Long

    @Query("SELECT * FROM item WHERE source = :source AND remote_id = :remoteId LIMIT 1")
    suspend fun getItemByRemoteId(remoteId: Int, source: Source = Source.REMOTE): ItemEntity?

    @Query("SELECT * FROM item WHERE source = :source AND document_id = :documentId LIMIT 1")
    suspend fun getItemByDocumentId(documentId: String, source: Source = Source.REMOTE): ItemEntity?

    @Query("SELECT * FROM item WHERE title = :title LIMIT 1")
    suspend fun getItemByTitle(title: String): ItemEntity?

    @Query("SELECT MAX(updated_at) FROM item")
    fun lastUpdated(): Long

    @Query("SELECT * FROM item WHERE list_type = :listType")
    suspend fun getItemsByListType(listType: Int): List<ItemEntity>

    // ── 同步专用：只刷新服务端字段，保留 local PK / remote_id / source ──

    @Query(
        """
        UPDATE item SET
            title = :title,
            description = :description,
            created_at = :createdAt,
            updated_at = :updatedAt,
            published_at = :publishedAt,
            url = :url,
            mini_program_id = :miniProgramId,
            source = :source,
            placeholder = :placeholder,
            list_type = :listType,
            icon_path = :iconPath,
            icon_name = :iconName,
            recommend = :recommend,
            remote_id = :remoteId,
            vip_level = :vipLevel,
            is_highlighted = :isHighlighted,
            is_online = :isOnline,
            is_ai = :isAi,
            document_id = :documentId,
            comment_count = :commentCount
        WHERE id = :id
        """
    )
    suspend fun updateItemFromSync(
        id: Int,
        title: String,
        description: String,
        createdAt: Long,
        updatedAt: Long,
        publishedAt: Long?,
        url: String,
        miniProgramId: String,
        source: Source,
        placeholder: String,
        listType: Int,
        iconPath: String?,
        iconName: String?,
        recommend: Boolean,
        remoteId: Int?,
        vipLevel: Int,
        isHighlighted: Boolean,
        isOnline: Boolean,
        isAi: Boolean,
        documentId: String?,
        commentCount: Int?,
    ): Int

    @Transaction
    suspend fun upsertItemFromSync(item: ItemEntity): Int {
        // 同步主键是 documentId（Strapi v5 稳定标识）；老数据没有 documentId 时降级 remoteId 匹配。
        val documentId = item.documentId?.takeIf { it.isNotBlank() }
        val remoteId = item.remoteId
        val existing = when {
            documentId != null -> getItemByDocumentId(documentId = documentId, source = item.source)
            remoteId != null -> getItemByRemoteId(remoteId = remoteId, source = item.source)
            else -> null
        }
        return if (existing == null) {
            insertItem(item.copy(id = 0)).toInt()
        } else {
            updateItemFromSync(
                id = existing.id,
                title = item.title,
                description = item.description,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
                publishedAt = item.publishedAt,
                url = item.url,
                miniProgramId = item.miniProgramId,
                source = item.source,
                placeholder = item.placeholder,
                listType = item.listType,
                iconPath = item.iconPath,
                iconName = item.iconName,
                recommend = item.recommend,
                remoteId = item.remoteId,
                vipLevel = item.vipLevel,
                isHighlighted = item.isHighlighted,
                isOnline = item.isOnline,
                isAi = item.isAi,
                documentId = item.documentId,
                commentCount = item.commentCount,
            )
            existing.id
        }
    }

    // ── UserState ────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserState(state: ItemUserState)

    @Query("SELECT * FROM item_user_state WHERE item_id = :itemId LIMIT 1")
    suspend fun getUserStateByItemId(itemId: Int): ItemUserState?

    /**
     * 原子切换收藏：保证 user_state 行存在再 UPDATE，避免首次收藏因无 user_state 失效。
     */
    @Transaction
    suspend fun toggleFavorited(itemId: Int, now: Long): Boolean {
        val current = getUserStateByItemId(itemId)
        val next = !(current?.isFavorited == true)
        val state = (current ?: ItemUserState(itemId = itemId)).copy(
            isFavorited = next,
            updatedAt = now,
        )
        upsertUserState(state)
        return next
    }

    /**
     * 原子切换置顶：同时刷新 pinned_at。
     */
    @Transaction
    suspend fun togglePinned(itemId: Int, now: Long): Boolean {
        val current = getUserStateByItemId(itemId)
        val next = !(current?.isPinned == true)
        val state = (current ?: ItemUserState(itemId = itemId)).copy(
            isPinned = next,
            pinnedAt = if (next) now else null,
            updatedAt = now,
        )
        upsertUserState(state)
        return next
    }

    @Query("UPDATE item_user_state SET is_favorited = :favorited, updated_at = :now WHERE item_id = :itemId")
    suspend fun setFavorited(itemId: Int, favorited: Boolean, now: Long)

    @Query("UPDATE item_user_state SET is_pinned = :pinned, pinned_at = :pinnedAt, updated_at = :now WHERE item_id = :itemId")
    suspend fun setPinned(itemId: Int, pinned: Boolean, pinnedAt: Long?, now: Long)

    @Query("UPDATE item_user_state SET pinned_at = :pinnedAt, updated_at = :pinnedAt WHERE item_id = :itemId")
    suspend fun bumpPinnedAt(itemId: Int, pinnedAt: Long)

    @Query("UPDATE item_user_state SET can_edit = :canEdit, updated_at = :now WHERE item_id = :itemId")
    suspend fun setCanEdit(itemId: Int, canEdit: Boolean, now: Long)

    /**
     * 原子切换 "是否需要授权码才能打开":保证 user_state 行存在再 UPDATE。
     */
    @Transaction
    suspend fun toggleRequiresAuth(itemId: Int, now: Long): Boolean {
        val current = getUserStateByItemId(itemId)
        val next = !(current?.requiresAuth == true)
        val state = (current ?: ItemUserState(itemId = itemId)).copy(
            requiresAuth = next,
            updatedAt = now,
        )
        upsertUserState(state)
        return next
    }

    @Query("UPDATE item_user_state SET requires_auth = :requiresAuth, updated_at = :now WHERE item_id = :itemId")
    suspend fun setRequiresAuth(itemId: Int, requiresAuth: Boolean, now: Long)

    /**
     * 批量关闭所有应用的密码保护(关闭密码总开关时使用)。
     */
    @Query("UPDATE item_user_state SET requires_auth = 0, updated_at = :now WHERE requires_auth = 1")
    suspend fun clearAllRequiresAuth(now: Long)

    /**
     * 观察所有已开启密码保护的应用。
     * 通过 INNER JOIN 过滤 `requires_auth = 1` 的行,并按更新时间倒序。
     * 关联字段 [ItemWithCategories.userState] 由 Room 自动填充。
     */
    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_user_state ON item.id = item_user_state.item_id
        WHERE item_user_state.requires_auth = 1
        ORDER BY item_user_state.updated_at DESC, item.id ASC
    """
    )
    fun observeItemsRequiringAuth(): Flow<List<ItemWithCategories>>

    // ── ClickStat ────────────────────────────────────────────────────────

    @Query(
        """
        INSERT OR IGNORE INTO item_click_stat(item_id, click_count, click_time)
        VALUES (:id, 0, NULL)
        """
    )
    suspend fun ensureClickStat(id: Int)

    @Query("UPDATE item_click_stat SET click_count = click_count + 1, click_time = :now WHERE item_id = :id")
    suspend fun updateClickStat(id: Int, now: Long)

    @Transaction
    suspend fun recordClick(id: Int, now: Long) {
        ensureClickStat(id)
        updateClickStat(id, now)
    }

    // ── Category CRUD ────────────────────────────────────────────────────

    @Query("SELECT * FROM category WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?

    @Transaction
    suspend fun upsertCategory(category: Category): Int {
        val existing = getCategoryByName(category.name)
        return if (existing != null) {
            updateCategoryFromSync(
                id = existing.id,
                name = category.name,
                canEdit = category.canEdit,
                updatedAt = category.updatedAt,
                documentId = category.documentId,
            )
            existing.id
        } else {
            insertCategory(category.copy(id = 0)).toInt()
        }
    }

    @Query(
        """
        UPDATE category SET
            name = :name,
            can_edit = :canEdit,
            updated_at = :updatedAt,
            document_id = COALESCE(:documentId, document_id)
        WHERE id = :id
        """
    )
    suspend fun updateCategoryFromSync(
        id: Int,
        name: String,
        canEdit: Boolean,
        updatedAt: Long,
        documentId: String?,
    ): Int

    @Update
    suspend fun updateCategory(category: Category): Int

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemCategoryCrossRef(crossRef: ItemCategoryCrossRef)

    // ── 资源行直接写（agent / prompt 走各自 DAO，但 insertItemWithCategoriesFromSync 仍在这里编排）──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: ItemAgentEntity): Long

    @Query("SELECT * FROM item_agent WHERE source = :source AND remote_id = :remoteId LIMIT 1")
    suspend fun getAgentByRemoteId(remoteId: Int, source: Source = Source.REMOTE): ItemAgentEntity?

    @Query("SELECT * FROM item_agent WHERE source = :source AND document_id = :documentId LIMIT 1")
    suspend fun getAgentByDocumentId(documentId: String, source: Source = Source.REMOTE): ItemAgentEntity?

    @Query("SELECT * FROM item_agent WHERE id = :id LIMIT 1")
    suspend fun getAgentById(id: Int): ItemAgentEntity?

    /** 同步更新已有 agent 行：只用 UPDATE，避免 REPLACE 触发 link 级联删除。 */
    @Query(
        """
        UPDATE item_agent SET
            remote_id = :remoteId,
            document_id = :documentId,
            source = :source,
            title = :title,
            description = :description,
            header = :header,
            body = :body,
            prompt = :prompt,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateAgentFromSync(
        id: Int,
        remoteId: Int?,
        documentId: String?,
        source: Source,
        title: String,
        description: String?,
        header: String?,
        body: String?,
        prompt: String?,
        updatedAt: Long,
    ): Int

    /** 转发：item_agent_link 表的 link 查询。 */
    @Query("SELECT agent_id FROM item_agent_link WHERE item_id = :itemId LIMIT 1")
    suspend fun getAgentLinkByItemId(itemId: Int): Int?

    /** 转发：item_prompt_link 表的 link 查询。 */
    @Query("SELECT prompt_id FROM item_prompt_link WHERE item_id = :itemId LIMIT 1")
    suspend fun getPromptLinkByItemId(itemId: Int): Int?

    /** 转发：item_data_link 表的 link 查询。 */
    @Query("SELECT data_id FROM item_data_link WHERE item_id = :itemId LIMIT 1")
    suspend fun getDataLinkByItemId(itemId: Int): Int?

    @Query("DELETE FROM item_data WHERE id = :id")
    suspend fun deleteItemDataById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptEntity): Long

    @Query("SELECT * FROM item_prompt WHERE source = :source AND remote_id = :remoteId LIMIT 1")
    suspend fun getPromptByRemoteId(remoteId: Int, source: Source = Source.REMOTE): PromptEntity?

    @Query("SELECT * FROM item_prompt WHERE source = :source AND document_id = :documentId LIMIT 1")
    suspend fun getPromptByDocumentId(documentId: String, source: Source = Source.REMOTE): PromptEntity?

    @Query("SELECT * FROM item_prompt WHERE id = :id LIMIT 1")
    suspend fun getPromptById(id: Int): PromptEntity?

    /** 同步更新已有 prompt 行：只用 UPDATE，避免 REPLACE 触发 link 级联删除。 */
    @Query(
        """
        UPDATE item_prompt SET
            remote_id = :remoteId,
            document_id = :documentId,
            source = :source,
            title = :title,
            description = :description,
            prompt = :prompt,
            placeholder = :placeholder,
            templates = :templates,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updatePromptFromSync(
        id: Int,
        remoteId: Int?,
        documentId: String?,
        source: Source,
        title: String,
        description: String?,
        prompt: String?,
        placeholder: String?,
        templates: String?,
        updatedAt: Long,
    ): Int

    // ── Link 管理（拆 3 张表） ───────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgentLink(link: ItemAgentLink)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromptLink(link: ItemPromptLink)

    @Query("DELETE FROM item_agent_link WHERE item_id = :itemId")
    suspend fun deleteAgentLinkByItemId(itemId: Int): Int

    @Query("DELETE FROM item_prompt_link WHERE item_id = :itemId")
    suspend fun deletePromptLinkByItemId(itemId: Int): Int

    // ── 同步编排：item + 分类 + 资源（data 由调用方传 itemDataDao）──

    /**
     * 写入一条同步数据：item + 分类关联 + agent/prompt 资源与 link。
     * data 资源由调用方通过 [itemDataDao] 写入（cross-DAO 不在此聚合）。
     *
     * 同步语义：
     * 1. item_category 总是先清空再重写 —— 服务端改了 item 的分类后，本地旧分类必须清掉。
     * 2. agent / prompt / data 采用 upsert 语义：
     *    - 新资源非空时，直接 REPLACE 写入并重建 link；资源 id 变化时清理旧孤儿行。
     *    - 新资源为空时保留旧 link，避免上游列表接口未 populate 嵌套资源时误删。
     *    - 真正的下架/删除由调用方根据 publishedAt 为空走 [deleteItemByDocumentId] 处理。
     * 3. 写新分类 / 写新资源 / 写新 link。
     */
    @Transaction
    suspend fun insertItemWithCategoriesFromSync(
        itemWithCategories: ItemWithRelation,
        itemDataDao: ItemDataDao,
    ): Int {
        val localItemId = upsertItemFromSync(itemWithCategories.item)

        // 1) 清空旧 item_category 关联
        deleteItemCategoryCrossRefByItemId(localItemId)
        itemWithCategories.categories.forEach { category ->
            val categoryId = upsertCategory(category)
            insertItemCategoryCrossRef(
                ItemCategoryCrossRef(itemId = localItemId, categoryId = categoryId)
            )
        }

        // 2) 处理 agent 资源
        //    采用 upsert 语义：仅在新资源非空时覆盖；新资源为空时保留旧 link，
        //    避免上游列表接口未 populate agent 时把已存在的 link 误删。
        //    命中已有行时必须用 UPDATE，不能用 REPLACE，否则 SQLite 会先删旧行再插新行，
        //    触发 item_agent_link.agent_id 的 FK CASCADE 把 link 一起删掉。
        val oldAgentId = getAgentLinkByItemId(localItemId)
        itemWithCategories.agent?.let { agent ->
            // 同步主键 documentId 优先，空时降级 remoteId（防御旧数据）
            val existing = agent.documentId?.takeIf { it.isNotBlank() }
                ?.let { getAgentByDocumentId(it, agent.source) }
                ?: agent.remoteId?.let { getAgentByRemoteId(it, agent.source) }
            val localAgentId = if (existing != null) {
                updateAgentFromSync(
                    id = existing.id,
                    remoteId = agent.remoteId,
                    documentId = agent.documentId,
                    source = agent.source,
                    title = agent.title,
                    description = agent.description,
                    header = agent.header,
                    body = agent.body,
                    prompt = agent.prompt,
                    updatedAt = agent.updatedAt,
                )
                existing.id
            } else {
                insertAgent(agent.copy(id = 0)).toInt()
            }
            insertAgentLink(ItemAgentLink(itemId = localItemId, agentId = localAgentId))
            // 只有资源 id 发生变化时，才清理旧的孤儿 agent 行
            if (oldAgentId != null && oldAgentId != localAgentId) {
                deleteAgentByAgentId(oldAgentId)
            }
        }

        // 3) 处理 prompt 资源
        val oldPromptId = getPromptLinkByItemId(localItemId)
        itemWithCategories.prompt?.let { prompt ->
            // 同步主键 documentId 优先，空时降级 remoteId（防御旧数据）
            val existing = prompt.documentId?.takeIf { it.isNotBlank() }
                ?.let { getPromptByDocumentId(it, prompt.source) }
                ?: prompt.remoteId?.let { getPromptByRemoteId(it, prompt.source) }
            val localPromptId = if (existing != null) {
                updatePromptFromSync(
                    id = existing.id,
                    remoteId = prompt.remoteId,
                    documentId = prompt.documentId,
                    source = prompt.source,
                    title = prompt.title,
                    description = prompt.description,
                    prompt = prompt.prompt,
                    placeholder = prompt.placeholder,
                    templates = prompt.templates,
                    updatedAt = prompt.updatedAt,
                )
                existing.id
            } else {
                insertPrompt(prompt.copy(id = 0)).toInt()
            }
            insertPromptLink(ItemPromptLink(itemId = localItemId, promptId = localPromptId))
            if (oldPromptId != null && oldPromptId != localPromptId) {
                deletePromptByPromptId(oldPromptId)
            }
        }

        // 4) 处理 data 资源
        val oldDataId = itemDataDao.getDataLinkByItemId(localItemId)
        itemWithCategories.data?.let { dataEntity ->
            val dataId = itemDataDao.upsertFromSync(dataEntity)
            itemDataDao.insertLink(
                com.shifenmiao.database.item.entity.ItemDataLink(
                    itemId = localItemId,
                    dataId = dataId,
                )
            )
            if (oldDataId != null && oldDataId != dataId) {
                itemDataDao.deleteById(oldDataId)
            }
        }

        return localItemId
    }

    @Transaction
    suspend fun insertItemsWithCategoriesFromSync(
        items: List<ItemWithRelation>,
        itemDataDao: ItemDataDao,
    ) {
        items.forEach { insertItemWithCategoriesFromSync(it, itemDataDao) }
    }

    // ── Paging / 列表查询 ────────────────────────────────────────────────

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        LEFT JOIN item_user_state s ON s.item_id = item.id
        WHERE item_category.category_id = :categoryId
        ORDER BY COALESCE(s.is_pinned, 0) DESC, COALESCE(s.pinned_at, 0) DESC,
                 item.recommend DESC, item.updated_at DESC, item.id ASC
    """
    )
    fun getItemsByCategoryId(categoryId: Int): PagingSource<Int, ItemWithCategories>

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        LEFT JOIN item_user_state s ON s.item_id = item.id
        ORDER BY COALESCE(s.is_pinned, 0) DESC, COALESCE(s.pinned_at, 0) DESC,
                 item.recommend DESC, item.updated_at DESC, item.id ASC
    """
    )
    fun pagingSourceWithCategories(): PagingSource<Int, ItemWithCategories>

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        LEFT JOIN item_user_state s ON s.item_id = item.id
        ORDER BY COALESCE(s.is_pinned, 0) DESC, COALESCE(s.pinned_at, 0) DESC,
                 item.recommend DESC, item.updated_at DESC, item.id ASC
        LIMIT 20
    """
    )
    fun getItemsByCategoryIdFlow(): Flow<List<ItemWithCategoriesAndStats>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        LEFT JOIN item_user_state s ON s.item_id = item.id
        WHERE item.list_type = :listType
          AND (:categoryId IS NULL OR item_category.category_id = :categoryId)
        ORDER BY COALESCE(s.is_pinned, 0) DESC, COALESCE(s.pinned_at, 0) DESC,
                 item.recommend DESC, item.updated_at DESC, item.id ASC
    """
    )
    fun pagingSourceByListTypeAndOptionalCategory(
        listType: Int,
        categoryId: Int?,
    ): PagingSource<Int, ItemWithCategoriesAndStats>

    /**
     * chip 分类列表：在指定 listType 下，被 item 引用过的分类。
     * 替代旧版 category.listType 直接过滤；走 JOIN 推导。
     */
    @Transaction
    @Query(
        """
        SELECT DISTINCT c.* FROM category c
        INNER JOIN item_category ic ON ic.category_id = c.id
        INNER JOIN item i ON i.id = ic.item_id
        WHERE i.list_type = :listType
        ORDER BY c.updated_at DESC, c.id ASC
    """
    )
    fun observeCategoriesUsedByListType(listType: Int): Flow<List<Category>>

    /**
     * 兜底：listType 下没有任何 item 时也要有分类可显示，
     * 用所有分类按更新时间倒序展示。
     */
    @Query("SELECT * FROM category ORDER BY updated_at DESC, id ASC")
    fun observeAllCategoriesForChips(): Flow<List<Category>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        WHERE item.title LIKE :searchString OR item.description LIKE :searchString
    """
    )
    fun searchByTitleOrDescription(searchString: String): Flow<List<ItemWithCategories>>

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        WHERE item.title LIKE :searchString OR item.description LIKE :searchString
    """
    )
    fun searchByTitleOrDescriptionWithStats(searchString: String): Flow<List<ItemWithCategoriesAndStats>>

    // ── 详情页查询 ────────────────────────────────────────────────────────

    @Transaction
    @Query("SELECT * FROM item WHERE id = :id")
    fun getItemById(id: Int): Flow<ItemWithRelation>

    @Transaction
    @Query("SELECT * FROM item WHERE id = :id")
    fun getItemWithCategoriesById(id: Int): Flow<ItemWithCategoriesAndStats>

    // ── 删除 ──────────────────────────────────────────────────────────────

    @Transaction
    suspend fun deleteAllData() {
        deleteAllAgents()
        deleteAllPrompts()
        deleteAllItemCategoryCrossRefs()
        deleteAllCategories()
        deleteAllItems()
    }

    @Query("DELETE FROM item_agent")
    suspend fun deleteAllAgents()

    @Query("DELETE FROM item_prompt")
    suspend fun deleteAllPrompts()

    @Query("DELETE FROM item")
    suspend fun deleteAllItems()

    @Query("SELECT name FROM category WHERE source = :source")
    suspend fun getCategoryNamesBySource(source: Source = Source.REMOTE): List<String>

    @Transaction
    suspend fun deleteCategoryByNameAndSource(name: String, source: Source = Source.REMOTE) {
        deleteCategoryCrossRefsByNameAndSource(name, source)
        deleteCategoryByNameAndSourceRaw(name, source)
    }

    @Query("DELETE FROM item_category WHERE category_id IN (SELECT id FROM category WHERE name = :name AND source = :source)")
    suspend fun deleteCategoryCrossRefsByNameAndSource(name: String, source: Source = Source.REMOTE)

    @Query("DELETE FROM category WHERE name = :name AND source = :source")
    suspend fun deleteCategoryByNameAndSourceRaw(name: String, source: Source = Source.REMOTE)

    @Query("DELETE FROM category")
    suspend fun deleteAllCategories()

    @Query("DELETE FROM item_category")
    suspend fun deleteAllItemCategoryCrossRefs()

    @Transaction
    suspend fun deleteItemById(itemId: Int) {
        // 1. 删除 1:1 关联资源（agent / prompt / data 不跨 item 共享）
        getAgentLinkByItemId(itemId)?.let { deleteAgentByAgentId(it) }
        getPromptLinkByItemId(itemId)?.let { deletePromptByPromptId(it) }
        getDataLinkByItemId(itemId)?.let { deleteItemDataById(it) }
        // 2. 删除分类关联与 item 主行（FK CASCADE 会顺带清理 link 表）
        deleteItemCategoryCrossRefByItemId(itemId)
        deleteItemByItemId(itemId)
    }

    @Query("DELETE FROM item WHERE id = :itemId")
    suspend fun deleteItemByItemId(itemId: Int): Int

    /**
     * 按同步主键删除条目：documentId（Strapi v5 稳定标识）优先，
     * documentId 为空时降级 remoteId（防御老客户端同步包 / 旧数据）。
     */
    @Transaction
    suspend fun deleteItemByDocumentId(documentId: String?, remoteId: Int? = null, source: Source = Source.REMOTE) {
        val validDocumentId = documentId?.takeIf { it.isNotBlank() }
        val item = when {
            validDocumentId != null -> getItemByDocumentId(validDocumentId, source)
            remoteId != null -> getItemByRemoteId(remoteId, source)
            else -> null
        } ?: return
        deleteItemById(item.id)
    }

    @Query("DELETE FROM item_category WHERE item_id = :itemId")
    suspend fun deleteItemCategoryCrossRefByItemId(itemId: Int)

    @Query("DELETE FROM item_prompt WHERE id = :promptId")
    suspend fun deletePromptByPromptId(promptId: Int): Int

    @Query("DELETE FROM item_agent WHERE id = :agentId")
    suspend fun deleteAgentByAgentId(agentId: Int): Int

    @Query("DELETE FROM item_category WHERE category_id = :categoryId")
    suspend fun deleteItemCategoryCrossRefsByCategoryId(categoryId: Int)

    @Query("SELECT COUNT(*) FROM item WHERE list_type = :listType")
    suspend fun getItemCountByListType(listType: Int): Int

    // ── 旧版分页查询（按页码返回 List，保留供 PagingSource 旧实现） ──

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        LEFT JOIN item_user_state s ON s.item_id = item.id
        WHERE item_category.category_id = :categoryId
        ORDER BY COALESCE(s.is_pinned, 0) DESC, COALESCE(s.pinned_at, 0) DESC,
                 item.recommend DESC, item.updated_at DESC, item.id ASC
        LIMIT :pageSize OFFSET (:page - 1) * :pageSize
    """
    )
    fun getItemsListByCategoryId(
        categoryId: Int,
        page: Int,
        pageSize: Int,
    ): List<ItemWithCategories>

    @Transaction
    @Query(
        """
        SELECT DISTINCT item.* FROM item
        INNER JOIN item_category ON item.id = item_category.item_id
        LEFT JOIN item_user_state s ON s.item_id = item.id
        ORDER BY COALESCE(s.is_pinned, 0) DESC, COALESCE(s.pinned_at, 0) DESC,
                 item.recommend DESC, item.updated_at DESC, item.id ASC
        LIMIT :pageSize OFFSET (:page - 1) * :pageSize
    """
    )
    fun getAllItemsList(
        page: Int,
        pageSize: Int,
    ): List<ItemWithCategories>

    // ── 导出（用于备份/迁移） ─────────────────────────────────────────────

    @Transaction
    @Query(
        """
        SELECT item.* FROM item
        INNER JOIN item_user_state s ON s.item_id = item.id
        WHERE s.can_edit = 1
    """
    )
    suspend fun getExportableItems(): List<ItemWithRelation>

    // ── Favorite / Local / Recent ────────────────────────────────────────

    /** 精选：recommend=1 或 is_highlighted=1，结合 is_pinned 排前面 */
    @Transaction
    @Query(
        """
        SELECT item.* FROM item
        LEFT JOIN item_user_state s ON s.item_id = item.id
        WHERE (item.recommend = 1 OR item.is_highlighted = 1)
        ORDER BY COALESCE(s.is_pinned, 0) DESC, item.updated_at DESC, item.id ASC
    """
    )
    fun getRecommendedItems(): Flow<List<ItemWithCategoriesAndStats>>

    /** 收藏：join item_user_state 取 is_favorited=1 */
    @Transaction
    @Query(
        """
        SELECT * FROM item
        INNER JOIN item_user_state s ON s.item_id = item.id
        WHERE s.is_favorited = 1
        ORDER BY s.updated_at DESC, item.id ASC
    """
    )
    fun getFavoritedItems(): Flow<List<ItemWithCategoriesAndStats>>

    /** 我的（用户创建）：join item_user_state 取 can_edit=1 */
    @Transaction
    @Query(
        """
        SELECT * FROM item
        INNER JOIN item_user_state s ON s.item_id = item.id
        WHERE s.can_edit = 1
        ORDER BY s.updated_at DESC, item.id ASC
    """
    )
    fun getEditableItems(): Flow<List<ItemWithCategoriesAndStats>>

    /** 本地工具：is_online = 0 */
    @Transaction
    @Query(
        """
        SELECT item.* FROM item
        LEFT JOIN item_user_state s ON s.item_id = item.id
        WHERE item.is_online = 0
        ORDER BY COALESCE(s.is_pinned, 0) DESC, item.recommend DESC, item.updated_at DESC, item.id ASC
    """
    )
    fun getLocalOfflineItems(): Flow<List<ItemWithCategories>>

    /** 最近访问：click_time > 0，按时间倒序限 20 */
    @Transaction
    @Query(
        """
        SELECT item.* FROM item
        INNER JOIN item_click_stat ON item.id = item_click_stat.item_id
        WHERE item_click_stat.click_time > 0
        ORDER BY item_click_stat.click_time DESC
        LIMIT 20
    """
    )
    fun getRecentClickedItems(): Flow<List<ItemWithCategoriesAndStats>>
}
