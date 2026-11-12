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

package com.wanbaohe.idphoto.data

import com.shifenmiao.database.idphoto.dao.IdPhotoSizeDao
import com.shifenmiao.database.idphoto.entity.IdPhotoSizeEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.storage.AppSharedStorage
import com.wanbaohe.idphoto.domain.IdPhotoPresetCatalog
import com.wanbaohe.idphoto.domain.IdPhotoSize
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 证件照尺寸仓库
 *
 * 预置尺寸按语言环境播种：FeatureDatabase 按语言分库（dbNameForLocale），
 * 切语言会冷重启并新建该语言的库，播种时即取该语言的本地化名称/描述，
 * 因此库里存的就是该语言的展示文本（历史版本播的是固定的中文规格，
 * 由 [IdPhotoPresetCatalog] 的目录 id 与 AppSharedStorage 的标记比对后整表重播）。
 */
@Singleton
class IdPhotoSizeRepository @Inject constructor(
    private val dao: IdPhotoSizeDao
) {
    /**
     * 获取最近使用的尺寸
     */
    fun getRecentSizes(limit: Int = 20): Flow<List<IdPhotoSize>> {
        return dao.getRecentSizes(limit).map { entities ->
            entities.map { it.toIdPhotoSize() }
        }
    }

    /**
     * 当前语言的默认尺寸（预置目录第一条）：组件构造时即选中，不必等库加载。
     */
    fun defaultSize(): IdPhotoSize =
        IdPhotoPresetCatalog.current().first().toIdPhotoSize(AppContext.getContext())

    /**
     * 播种当前语言的预置尺寸。
     *
     * 三种情况会整表重播：
     * - 该语言库还没播过（新装/首次进页面/库被重建）；
     * - 库里的预置是旧版本（如固定的中文规格）或别的语言目录；
     * - 预置被删光（此时列表不可用，重播兜底）。
     *
     * 只是重播预置行，用户自建尺寸不受影响。
     */
    suspend fun initPresetsIfNeeded() {
        val catalogId = IdPhotoPresetCatalog.currentCatalogId()
        val seeded = AppSharedStorage.loadIdPhotoPresetCatalogId() == catalogId
        if (seeded && dao.getPresetCount() > 0) return
        seedPresets()
    }

    /**
     * 恢复默认预置尺寸（删除所有预置，按当前语言重新插入）
     */
    suspend fun resetPresetsOnly() {
        seedPresets()
    }

    private suspend fun seedPresets() {
        val catalog = IdPhotoPresetCatalog.current()
        dao.deletePresetSizes()
        val context = AppContext.getContext()
        val now = System.currentTimeMillis()
        // DAO 按 updatedAt DESC 取，递减时间戳保证列表顺序 = 目录顺序（第一条为默认尺寸）
        catalog.forEachIndexed { index, preset ->
            dao.insertSize(
                preset.toIdPhotoSize(context).toEntity(updatedAt = now - index)
            )
        }
        AppSharedStorage.saveIdPhotoPresetCatalogId(IdPhotoPresetCatalog.currentCatalogId())
    }

    /**
     * 保存或更新尺寸
     */
    suspend fun saveOrUpdate(size: IdPhotoSize): Long {
        return if (size.id > 0) {
            dao.updateSize(size.toEntity())
            size.id
        } else {
            dao.insertSize(size.toEntity())
        }
    }

    /**
     * 删除尺寸
     */
    suspend fun deleteSize(id: Long) {
        dao.deleteSizeById(id)
    }

    /**
     * 批量删除尺寸
     */
    suspend fun deleteSizes(ids: List<Long>) {
        ids.forEach { dao.deleteSizeById(it) }
    }

    /**
     * 记录尺寸使用（更新时间）
     */
    suspend fun recordSizeUsage(id: Long) {
        dao.updateSizeTime(id)
    }

    /**
     * 根据 ID 获取尺寸
     */
    suspend fun getSizeById(id: Long): IdPhotoSize? {
        return dao.getSizeById(id)?.toIdPhotoSize()
    }
}

/**
 * Entity 转换为 Domain 模型
 */
private fun IdPhotoSizeEntity.toIdPhotoSize(): IdPhotoSize {
    return IdPhotoSize(
        id = id,
        name = name,
        widthMm = widthMm,
        heightMm = heightMm,
        widthPx = widthPx,
        heightPx = heightPx,
        description = description,
        isPreset = isPreset,
        createdAt = createdAt,
    )
}

/**
 * Domain 模型转换为 Entity
 */
private fun IdPhotoSize.toEntity(updatedAt: Long = System.currentTimeMillis()): IdPhotoSizeEntity {
    return IdPhotoSizeEntity(
        id = id,
        name = name,
        widthMm = widthMm,
        heightMm = heightMm,
        widthPx = widthPx,
        heightPx = heightPx,
        description = description,
        isPreset = isPreset,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
