package com.shifenmiao.database.item.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shifenmiao.database.item.entity.ItemDataEntity
import com.shifenmiao.database.item.entity.ItemDataLink
import com.shifenmiao.model.Source
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDataDao {

    // ── 通过 link 表查 ──

    @Query("""
        SELECT d.* FROM item_data d
        INNER JOIN item_data_link l ON l.data_id = d.id
        WHERE l.item_id = :itemId LIMIT 1
    """)
    suspend fun getByItemId(itemId: Int): ItemDataEntity?

    @Query("""
        SELECT d.* FROM item_data d
        INNER JOIN item_data_link l ON l.data_id = d.id
        WHERE l.item_id = :itemId LIMIT 1
    """)
    fun observeByItemId(itemId: Int): Flow<ItemDataEntity?>

    // ── 同步用：按 remote_id 查 ──

    @Query("SELECT * FROM item_data WHERE source = :source AND remote_id = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: Int, source: Source = Source.REMOTE): ItemDataEntity?

    @Query("SELECT * FROM item_data WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ItemDataEntity?

    // ── 写入 ──

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: ItemDataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: ItemDataEntity): Long

    @Query(
        """
        UPDATE item_data SET
            title = :title,
            kind = :kind,
            data = :data,
            url = :url,
            extra = :extra,
            size_bytes = :sizeBytes,
            updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun updateContent(
        id: Int,
        title: String,
        kind: com.shifenmiao.database.item.entity.ItemDataKind,
        data: String?,
        url: String?,
        extra: String?,
        sizeBytes: Long,
        updatedAt: Long,
    ): Int

    @Query(
        """
        UPDATE item_data SET
            title = :title,
            kind = :kind,
            data = :data,
            url = :url,
            extra = :extra,
            size_bytes = :sizeBytes,
            created_at = :createdAt,
            updated_at = :updatedAt,
            source = :source,
            remote_id = :remoteId
        WHERE id = :id
        """
    )
    suspend fun updateFromSync(
        id: Int,
        remoteId: Int?,
        title: String,
        kind: com.shifenmiao.database.item.entity.ItemDataKind,
        data: String?,
        url: String?,
        extra: String?,
        sizeBytes: Long,
        createdAt: Long,
        updatedAt: Long,
        source: Source,
    ): Int

    /**
     * 本地写入：插入或更新已有（按 remote_id 命中）。
     * 返回资源 id。
     */
    @Transaction
    suspend fun upsert(entity: ItemDataEntity): Int {
        val inserted = insertIgnore(entity)
        if (inserted != -1L) return inserted.toInt()
        val existing = entity.remoteId?.let { getByRemoteId(it, entity.source) }
            ?: return insertOrReplace(entity).toInt()
        updateContent(
            id = existing.id,
            title = entity.title,
            kind = entity.kind,
            data = entity.data,
            url = entity.url,
            extra = entity.extra,
            sizeBytes = entity.sizeBytes,
            updatedAt = entity.updatedAt,
        )
        return existing.id
    }

    @Transaction
    suspend fun upsertFromSync(entity: ItemDataEntity): Int {
        val existing = entity.remoteId?.let { remoteId ->
            getByRemoteId(remoteId = remoteId, source = entity.source)
        }
        if (existing == null) {
            return insertOrReplace(entity).toInt()
        }
        updateFromSync(
            id = existing.id,
            remoteId = entity.remoteId,
            title = entity.title,
            kind = entity.kind,
            data = entity.data,
            url = entity.url,
            extra = entity.extra,
            sizeBytes = entity.sizeBytes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            source = entity.source,
        )
        return existing.id
    }

    // ── Link 管理 ──

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: ItemDataLink)

    @Query("SELECT data_id FROM item_data_link WHERE item_id = :itemId LIMIT 1")
    suspend fun getDataLinkByItemId(itemId: Int): Int?

    @Query("DELETE FROM item_data_link WHERE item_id = :itemId")
    suspend fun deleteLinkByItemId(itemId: Int): Int

    @Query("SELECT item_id FROM item_data_link WHERE data_id = :dataId")
    suspend fun getItemIdsByDataId(dataId: Int): List<Int>

    // ── 删除 ──

    @Query("DELETE FROM item_data WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM item_data WHERE source = :source")
    suspend fun getBySource(source: Source): List<ItemDataEntity>
}
