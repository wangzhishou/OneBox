package com.shifenmiao.database.ai.dao

import androidx.room.*
import com.shifenmiao.database.ai.entity.AiEngineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiEngineDao {

    @Query("SELECT * FROM ai_engines ORDER BY sort_order ASC")
    fun getAllEngines(): Flow<List<AiEngineEntity>>

    @Query("SELECT * FROM ai_engines ORDER BY sort_order ASC")
    suspend fun getAllEnginesList(): List<AiEngineEntity>

    /**
     * 根据用户等级获取可用的引擎列表
     */
    @Query("SELECT * FROM ai_engines WHERE vip_level <= :userLevel ORDER BY sort_order ASC")
    fun getEnginesByUserLevel(userLevel: Int): Flow<List<AiEngineEntity>>

    @Query("SELECT * FROM ai_engines WHERE vip_level <= :userLevel ORDER BY sort_order ASC")
    suspend fun getEnginesListByUserLevel(userLevel: Int): List<AiEngineEntity>

    @Query("SELECT * FROM ai_engines WHERE name = :name ORDER BY sort_order ASC, id ASC LIMIT 1")
    suspend fun getEngineByName(name: String): AiEngineEntity?

    @Query("SELECT * FROM ai_engines WHERE name = :name ORDER BY sort_order ASC, id ASC")
    suspend fun getEnginesByName(name: String): List<AiEngineEntity>

    @Query("SELECT * FROM ai_engines WHERE id = :id LIMIT 1")
    suspend fun getEngineById(id: Long): AiEngineEntity?

    @Query("SELECT * FROM ai_engines WHERE name = :name AND request_protocol = :requestProtocol LIMIT 1")
    suspend fun getEngineByNameAndProtocol(name: String, requestProtocol: String): AiEngineEntity?

    /**
     * 更新引擎关联的模型名称（每个引擎独立记忆选中的模型）
     */
    @Query("UPDATE ai_engines SET selected_model_name = :selectedModelName WHERE id = :id")
    suspend fun updateEngineSelectedModelName(id: Long, selectedModelName: String)

    @Query("UPDATE ai_engines SET selected_model_name = :newModelName WHERE name = :name AND selected_model_name = :currentModelName")
    suspend fun updateEnginesSelectedModelNameByNameAndCurrentModelName(name: String, currentModelName: String, newModelName: String)

    // ==================== CRUD ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEngine(engine: AiEngineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEngines(engines: List<AiEngineEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEnginesIgnore(engines: List<AiEngineEntity>)

    @Update
    suspend fun updateEngine(engine: AiEngineEntity)

    @Delete
    suspend fun deleteEngine(engine: AiEngineEntity)

    @Query("DELETE FROM ai_engines WHERE name = :name AND request_protocol = :requestProtocol")
    suspend fun deleteEngineByNameAndProtocol(name: String, requestProtocol: String)

    @Query("DELETE FROM ai_engines")
    suspend fun deleteAllEngines()

    @Query("SELECT COUNT(*) FROM ai_engines")
    suspend fun getEngineCount(): Int

    /**
     * 清空指定引擎的代理路由（仅限非用户自建的预制行，用户自建/可编辑行不动）
     */
    @Query("UPDATE ai_engines SET proxy_url = '', proxy_path = '' WHERE name IN (:names) AND can_edit = 0 AND source != 'LOCAL'")
    suspend fun clearProxyRoutesByNames(names: List<String>)
}
