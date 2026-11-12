/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
        return wheelDao.observeWheelsWithOptions().map { list ->
            list.map { it.wheel.toDomain(it.options.byPosition()) }
        }
    }

    /**
     * 获取最近使用的转盘
     */
    fun getRecentWheels(limit: Int = 5): Flow<List<DecisionWheel>> {
        return wheelDao.observeRecentWheelsWithOptions(limit).map { list ->
            list.map { it.wheel.toDomain(it.options.byPosition()) }
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
     *
     * [wheelTitle] 冗余落库，因为转盘被删除后历史仍需有归属可读。
     */
    suspend fun saveHistory(wheelId: String, wheelTitle: String, selectedOption: WheelOption) {
        val history = WheelHistoryEntity(
            wheelId = wheelId,
            wheelTitle = wheelTitle,
            selectedOptionId = selectedOption.id,
            selectedOptionName = selectedOption.name,
            timestamp = System.currentTimeMillis()
        )
        wheelDao.insertHistory(history)
    }

    /**
     * 抽后移除：翻转单个选项的启用标记。
     */
    suspend fun setOptionEnabled(optionId: String, enabled: Boolean) {
        wheelDao.updateOptionEnabled(optionId, enabled)
    }

    /**
     * 恢复转盘上所有被移除的选项。
     */
    suspend fun resetOptionsEnabled(wheelId: String) {
        wheelDao.resetOptionsEnabled(wheelId)
    }

    /** 关掉"抽后移除"时用：所有转盘一起把选项放回去 */
    suspend fun resetAllOptionsEnabled() {
        wheelDao.resetAllOptionsEnabled()
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
     * 获取所有历史记录（不截断）
     */
    fun observeAllHistory(): Flow<List<WheelHistoryEntity>> {
        return wheelDao.observeAllHistory()
    }

    /**
     * 删除单条历史记录
     */
    suspend fun deleteHistory(historyId: Long) {
        wheelDao.deleteHistoryById(historyId)
    }

    /**
     * 清除历史记录
     */
    suspend fun clearHistory() {
        wheelDao.clearAllHistory()
    }
}

/**
 * @Relation 回传的选项顺序由 Room 自己决定（通常是主键/rowid 序），
 * 而扇区顺序必须跟 position 一致，否则每次刷新扇区都会重排。
 */
private fun List<WheelOptionEntity>.byPosition(): List<WheelOptionEntity> =
    sortedBy { it.position }

// 扩展函数：实体转领域模型
private fun WheelEntity.toDomain(options: List<WheelOptionEntity>): DecisionWheel {
    return DecisionWheel(
        id = id,
        title = title,
        options = options.map { it.toDomain() },
        createdAt = createdAt,
        lastUsedAt = lastUsedAt
    )
}

private fun WheelOptionEntity.toDomain(): WheelOption {
    return WheelOption(
        id = id,
        name = name,
        color = Color(android.graphics.Color.parseColor(colorHex)),
        weight = weight,
        enabled = enabled
    )
}

// 扩展函数：领域模型转实体
private fun DecisionWheel.toEntity(): WheelEntity {
    return WheelEntity(
        id = id,
        title = title,
        createdAt = createdAt,
        // 保留原值：编辑转盘不应把它顶到"最近使用"最前，只有真的转过才更新
        lastUsedAt = lastUsedAt
    )
}

private fun WheelOption.toEntity(wheelId: String, position: Int): WheelOptionEntity {
    return WheelOptionEntity(
        id = id,
        wheelId = wheelId,
        name = name,
        colorHex = String.format("#%08X", color.toArgb()),
        position = position,
        weight = weight,
        enabled = enabled
    )
}

