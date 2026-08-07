package com.wanbaohe.decisionwheel.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.shifenmiao.database.decision_wheel.dao.WheelDao
import com.shifenmiao.database.decision_wheel.entity.WheelEntity
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.database.decision_wheel.entity.WheelOptionEntity
import com.wanbaohe.decisionwheel.component.DecisionWheel
import com.wanbaohe.decisionwheel.component.WheelOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * 转盘数据仓库
 */
class WheelRepository @Inject constructor(
    private val wheelDao: WheelDao
) {

    /**
     * 获取所有转盘配置
     */
    fun getAllWheels(): Flow<List<DecisionWheel>> {
        return wheelDao.getAllWheels().map { entities ->
            entities.map { entity ->
                val options = wheelDao.getOptionsByWheelId(entity.id)
                entity.toDomain(options)
            }
        }
    }

    /**
     * 获取最近使用的转盘
     */
    fun getRecentWheels(limit: Int = 5): Flow<List<DecisionWheel>> {
        return wheelDao.getRecentWheels(limit).map { entities ->
            entities.map { entity ->
                val options = wheelDao.getOptionsByWheelId(entity.id)
                entity.toDomain(options)
            }
        }
    }

    /**
     * 根据ID获取转盘
     */
    suspend fun getWheelById(wheelId: String): DecisionWheel? {
        val entity = wheelDao.getWheelById(wheelId) ?: return null
        val options = wheelDao.getOptionsByWheelId(wheelId)
        return entity.toDomain(options)
    }

    /**
     * 保存转盘配置
     */
    suspend fun saveWheel(wheel: DecisionWheel) {
        val entity = wheel.toEntity()
        val optionEntities = wheel.options.mapIndexed { index, option ->
            option.toEntity(wheel.id, index)
        }
        wheelDao.insertWheelWithOptions(entity, optionEntities)
    }

    /**
     * 更新转盘配置
     */
    suspend fun updateWheel(wheel: DecisionWheel) {
        val entity = wheel.toEntity()
        wheelDao.updateWheel(entity)

        // 删除旧选项，插入新选项
        wheelDao.deleteOptionsByWheelId(wheel.id)
        val optionEntities = wheel.options.mapIndexed { index, option ->
            option.toEntity(wheel.id, index)
        }
        wheelDao.insertOptions(optionEntities)
    }

    /**
     * 删除转盘
     */
    suspend fun deleteWheel(wheelId: String) {
        wheelDao.deleteWheelWithOptions(wheelId)
    }

    /**
     * 更新转盘使用记录
     */
    suspend fun updateWheelUsage(wheelId: String) {
        wheelDao.updateWheelUsage(wheelId, System.currentTimeMillis())
    }

    /**
     * 保存转盘结果历史
     */
    suspend fun saveHistory(wheelId: String, selectedOption: WheelOption) {
        val history = WheelHistoryEntity(
            wheelId = wheelId,
            selectedOptionId = selectedOption.id,
            selectedOptionName = selectedOption.name,
            timestamp = System.currentTimeMillis()
        )
        wheelDao.insertHistory(history)
    }

    /**
     * 获取转盘历史记录
     */
    fun getWheelHistory(wheelId: String, limit: Int = 20): Flow<List<WheelHistoryEntity>> {
        return wheelDao.getWheelHistory(wheelId, limit)
    }

    /**
     * 获取所有历史记录
     */
    fun getAllHistory(limit: Int = 50): Flow<List<WheelHistoryEntity>> {
        return wheelDao.getAllHistory(limit)
    }

    /**
     * 清除历史记录
     */
    suspend fun clearHistory() {
        wheelDao.clearAllHistory()
    }
}

// 扩展函数：实体转领域模型
private fun WheelEntity.toDomain(options: List<WheelOptionEntity>): DecisionWheel {
    return DecisionWheel(
        id = id,
        title = title,
        options = options.map { it.toDomain() },
        createdAt = createdAt
    )
}

private fun WheelOptionEntity.toDomain(): WheelOption {
    return WheelOption(
        id = id,
        name = name,
        color = Color(android.graphics.Color.parseColor(colorHex))
    )
}

// 扩展函数：领域模型转实体
private fun DecisionWheel.toEntity(): WheelEntity {
    return WheelEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        lastUsedAt = System.currentTimeMillis()
    )
}

private fun WheelOption.toEntity(wheelId: String, position: Int): WheelOptionEntity {
    return WheelOptionEntity(
        id = id,
        wheelId = wheelId,
        name = name,
        colorHex = String.format("#%08X", color.toArgb()),
        position = position
    )
}

