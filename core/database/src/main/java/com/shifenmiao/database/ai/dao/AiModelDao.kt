package com.shifenmiao.database.ai.dao

import androidx.room.*
import com.shifenmiao.database.ai.entity.AiModelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiModelDao {
    @Query("SELECT * FROM ai_models ORDER BY can_edit ASC, id DESC")
    fun getAllModels(): Flow<List<AiModelEntity>>

    @Query("SELECT * FROM ai_models ORDER BY can_edit ASC, id DESC")
    suspend fun getAllModelsList(): List<AiModelEntity>

    @Query("SELECT * FROM ai_models WHERE id = :modelId")
    suspend fun getModelById(modelId: Int): AiModelEntity?

    @Query("SELECT MAX(sort_order) FROM ai_models WHERE engine_name = :engineName")
    suspend fun getMaxSortOrderForEngine(engineName: String): Int?

    @Query("SELECT * FROM ai_models WHERE name = :name AND engine_name = :engineName LIMIT 1")
    suspend fun getModelByNameAndEngineName(name: String, engineName: String): AiModelEntity?

    @Query("SELECT * FROM ai_models WHERE provider = :provider")
    suspend fun getModelsByProvider(provider: String): List<AiModelEntity>

    /**
     * 根据引擎名获取所有关联的模型
     */
    @Query("SELECT * FROM ai_models WHERE engine_name = :engineName ORDER BY sort_order ASC, id DESC")
    suspend fun getModelsByEngineName(engineName: String): List<AiModelEntity>

    /**
     * 根据引擎名获取所有关联的模型 Flow
     */
    @Query("SELECT * FROM ai_models WHERE engine_name = :engineName ORDER BY sort_order ASC, id DESC")
    fun getModelsByEngineNameFlow(engineName: String): Flow<List<AiModelEntity>>

    /**
     * 获取第一个可用的模型（用于设置默认选中）
     */
    @Query("SELECT * FROM ai_models WHERE enabled = 1 ORDER BY sort_order ASC, id ASC LIMIT 1")
    suspend fun getFirstAvailableModel(): AiModelEntity?

    /**
     * 获取指定引擎下第一个可用的模型
     */
    @Query("SELECT * FROM ai_models WHERE engine_name = :engineName AND enabled = 1 ORDER BY sort_order ASC, id ASC LIMIT 1")
    suspend fun getFirstAvailableModelForEngine(engineName: String): AiModelEntity?

    @Query("DELETE FROM ai_models WHERE engine_name = :engineName")
    suspend fun deleteModelsByEngineName(engineName: String)

    /**
     * 更新模型的引擎名称（用于迁移或关联）
     */
    @Query("UPDATE ai_models SET engine_name = :engineName WHERE provider = :provider")
    suspend fun updateModelsEngineName(provider: String, engineName: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModel(model: AiModelEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<AiModelEntity>): List<Long>

    @Transaction
    suspend fun upsertModelByIdentity(model: AiModelEntity): Int {
        val existing = getModelByNameAndEngineName(
            name = model.name,
            engineName = model.engineName,
        )
        return if (existing != null) {
            insertModel(model.copy(id = existing.id))
            existing.id
        } else {
            insertModel(model.copy(id = 0)).toInt()
        }
    }

    @Update
    suspend fun updateModel(model: AiModelEntity)

    @Delete
    suspend fun deleteModel(model: AiModelEntity)

    @Query("DELETE FROM ai_models")
    suspend fun deleteAllModels()
}