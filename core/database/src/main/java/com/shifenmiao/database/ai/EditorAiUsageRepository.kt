package com.shifenmiao.database.ai

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.dao.ModelUsageStat
import com.shifenmiao.database.ai.dao.TokenUsageSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Entity(tableName = "editor_ai_usage")
data class EditorAiUsage(
    @PrimaryKey val id: String,
    val source: String,
    val action: String,
    val engine: String,
    val model: String,
    val createdAt: Long,
    val status: String,
    val totalTokens: Int?,
    val inputTokens: Int?,
    val outputTokens: Int?,
)

@Dao
interface EditorAiUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entry: EditorAiUsage)

    @Query("SELECT * FROM editor_ai_usage ORDER BY createdAt DESC LIMIT 100")
    fun observeRecent(): Flow<List<EditorAiUsage>>

    @Query("""
        SELECT COALESCE(SUM(totalTokens), 0) AS total_tokens,
        COALESCE(SUM(inputTokens), 0) AS prompt_tokens,
        COALESCE(SUM(outputTokens), 0) AS completion_tokens, COUNT(*) AS request_count
        FROM editor_ai_usage WHERE totalTokens > 0
    """)
    suspend fun summarize(): TokenUsageSummary

    @Query("""
        SELECT model, engine, SUM(totalTokens) AS total_tokens, COUNT(*) AS request_count
        FROM editor_ai_usage WHERE totalTokens > 0 GROUP BY model, engine
    """)
    suspend fun byModel(): List<ModelUsageStat>
}

class EditorAiUsageRepository @Inject constructor(database: AppDatabase) {
    private val dao = database.editorAiUsageDao()
    fun observeRecent() = dao.observeRecent()
    suspend fun summarize() = dao.summarize()
    suspend fun byModel() = dao.byModel()

    suspend fun record(entry: EditorAiUsage) {
        try {
            dao.put(entry)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // Usage history must not replace the editor's actual result.
        }
    }
}
