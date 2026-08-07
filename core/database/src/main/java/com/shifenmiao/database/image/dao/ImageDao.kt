package com.shifenmiao.database.image.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.image.entity.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ImageEntity>): List<Long>

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Delete
    suspend fun deleteImage(image: ImageEntity)

    @Query("SELECT * FROM images WHERE id = :id")
    suspend fun getImageById(id: Long): ImageEntity?

    @Query("SELECT * FROM images WHERE uri = :uri")
    suspend fun getImageByUri(uri: String): ImageEntity?

    @Query("SELECT * FROM images ORDER BY created_at DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE format = :format ORDER BY created_at DESC")
    fun getImagesByFormat(format: String): Flow<List<ImageEntity>>

    @Query("SELECT * FROM images WHERE message_id = :messageId")
    suspend fun getImagesByMessageId(messageId: Int): List<ImageEntity>

    @Query("SELECT * FROM images WHERE conversation_id = :conversationId ORDER BY created_at DESC")
    suspend fun getImagesByConversationId(conversationId: String): List<ImageEntity>

    @Query("SELECT * FROM images WHERE conversation_id = :conversationId AND uri = :uri LIMIT 1")
    suspend fun getImageByConversationAndUri(conversationId: String, uri: String): ImageEntity?

    @Query("DELETE FROM images WHERE conversation_id = :conversationId")
    suspend fun deleteByConversationId(conversationId: String)

    @Query("""
        SELECT uri, thumbnail_base64, local_path, format, created_at
        FROM images
        WHERE conversation_id = :conversationId
        GROUP BY uri
        ORDER BY MAX(created_at) DESC
    """)
    suspend fun getDistinctImagesByConversation(conversationId: String): List<ImageSummaryProjection>

    @Query("DELETE FROM images")
    suspend fun clearAll()
}

data class ImageSummaryProjection(
    val uri: String,
    val thumbnail_base64: String?,
    val local_path: String?,
    val format: String,
    val created_at: Long
)
