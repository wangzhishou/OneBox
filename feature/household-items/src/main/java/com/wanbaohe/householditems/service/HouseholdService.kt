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

package com.wanbaohe.householditems.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.net.toUri
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.household.entity.HouseholdItemEntity
import com.shifenmiao.database.household.entity.HouseholdLocationEntity
import com.shifenmiao.database.household.repo.HouseholdRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.householditems.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.json.JSONObject

/**
 * 家庭物品业务门面 — UI 层与 Agent 层共用的唯一写入入口。
 *
 * 职责:
 *  - 入参校验(名称、位置路径、保质期)
 *  - 调用 [HouseholdRepository] 写库
 *  - 图片压缩落盘(filesDir/household_item_photos)与旧图清理
 *  - 调用 [ActivityLogRecorder] 写审计日志
 *
 * 不做的事:
 *  - 不持有 UI 状态
 *  - 不暴露 Flow(订阅仍走 Repository)
 */
@Singleton
class HouseholdService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: HouseholdRepository,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    data class ItemInput(
        val name: String,
        val category: String? = null,
        /** 与 [locationPath] 二选一;path 非空时按路径逐级查找/创建位置 */
        val locationId: String? = null,
        val locationPath: List<String>? = null,
        /** 到期时间戳(ms),null = 无保质期 */
        val expireAt: Long? = null,
        val note: String? = null,
        /** 待导入的图片 content uri 字符串,null = 不变更图片 */
        val photoUri: String? = null,
        /** true = 移除现有图片(photoUri 为空时生效) */
        val removePhoto: Boolean = false,
    )

    data class ItemView(
        val id: String,
        val name: String,
        val category: String?,
        val locationPath: String,
        val expireAt: Long?,
        val note: String?,
    )

    // ── 写入:物品 ────────────────────────────────────

    suspend fun addItem(
        input: ItemInput,
        actor: String,
        source: String,
    ): Result<String> = runCatching {
        val name = input.name.trim()
        require(name.isNotEmpty()) { "item_name_blank" }
        val locationId = resolveLocationId(input)
        val photoPath = input.photoUri?.let { importPhoto(it.toUri()) }
        val id = UUID.randomUUID().toString()
        repository.upsertItem(
            HouseholdItemEntity(
                id = id,
                name = name,
                category = input.category?.trim()?.takeIf { it.isNotEmpty() },
                locationId = locationId,
                expireAt = input.expireAt,
                photoPath = photoPath,
                note = input.note?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
        logItemChange(
            entityId = id,
            actor = actor,
            action = "CREATE",
            source = source,
            title = AppContext.getString(R.string.household_log_item_created, name),
            locationId = locationId,
        )
        id
    }

    suspend fun updateItem(
        itemId: String,
        input: ItemInput,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val name = input.name.trim()
        require(name.isNotEmpty()) { "item_name_blank" }
        val previous = repository.getItemById(itemId) ?: error("item_not_found")
        val locationId = resolveLocationId(input)
        val photoPath = when {
            input.photoUri != null -> importPhoto(input.photoUri.toUri())
            input.removePhoto -> null
            else -> previous.photoPath
        }
        if (photoPath != previous.photoPath) {
            previous.photoPath?.let(::deletePhoto)
        }
        repository.upsertItem(
            previous.copy(
                name = name,
                category = input.category?.trim()?.takeIf { it.isNotEmpty() },
                locationId = locationId,
                expireAt = input.expireAt,
                photoPath = photoPath,
                note = input.note?.trim()?.takeIf { it.isNotEmpty() },
                updatedAt = System.currentTimeMillis(),
            )
        )
        logItemChange(
            entityId = itemId,
            actor = actor,
            action = "UPDATE",
            source = source,
            title = AppContext.getString(R.string.household_log_item_updated, name),
            locationId = locationId,
        )
    }

    suspend fun deleteItem(
        itemId: String,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val previous = repository.getItemById(itemId)
        repository.deleteItem(itemId)
        previous?.photoPath?.let(::deletePhoto)
        activityLogRecorder.recordHousehold(
            entityId = itemId,
            entityType = "HouseholdItem",
            actorType = actor,
            actionType = "DELETE",
            source = source,
            title = AppContext.getString(
                R.string.household_log_item_deleted,
                previous?.name ?: itemId,
            ),
            description = previous?.let {
                repository.buildLocationPath(it.locationId)
            } ?: "",
            appTitle = AppContext.getString(R.string.household_title),
        )
    }

    /** 移动物品到新位置路径(路径不存在自动逐级创建),供 Agent 工具使用。 */
    suspend fun moveItem(
        itemId: String,
        locationPath: List<String>,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        require(locationPath.isNotEmpty()) { "location_path_empty" }
        val previous = repository.getItemById(itemId) ?: error("item_not_found")
        val location = repository.findOrCreateLocationByPath(locationPath.map { it.trim() })
        repository.upsertItem(
            previous.copy(
                locationId = location.id,
                updatedAt = System.currentTimeMillis(),
            )
        )
        activityLogRecorder.recordHousehold(
            entityId = itemId,
            entityType = "HouseholdItem",
            actorType = actor,
            actionType = "MOVE",
            source = source,
            title = AppContext.getString(R.string.household_log_item_moved, previous.name),
            description = repository.buildLocationPath(location.id),
            appTitle = AppContext.getString(R.string.household_title),
        )
    }

    // ── 写入:位置 ────────────────────────────────────

    suspend fun addLocation(
        name: String,
        parentId: String?,
        iconKey: String? = null,
        actor: String,
        source: String,
    ): Result<String> = runCatching {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "location_name_blank" }
        val id = UUID.randomUUID().toString()
        repository.upsertLocation(
            HouseholdLocationEntity(
                id = id,
                name = trimmed,
                parentId = parentId,
                iconKey = iconKey?.takeIf { it.isNotBlank() },
            )
        )
        activityLogRecorder.recordHousehold(
            entityId = id,
            entityType = "HouseholdLocation",
            actorType = actor,
            actionType = "CREATE",
            source = source,
            title = AppContext.getString(R.string.household_log_location_created, trimmed),
            description = repository.buildLocationPath(id),
            appTitle = AppContext.getString(R.string.household_title),
        )
        id
    }

    suspend fun renameLocation(
        locationId: String,
        newName: String,
        iconKey: String? = null,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "location_name_blank" }
        val target = repository.getLocationById(locationId) ?: error("location_not_found")
        // iconKey 传 null 表示保持不变;要清空图标传 ""
        repository.upsertLocation(
            target.copy(
                name = trimmed,
                iconKey = when {
                    iconKey == null -> target.iconKey
                    iconKey.isBlank() -> null
                    else -> iconKey
                },
            )
        )
        activityLogRecorder.recordHousehold(
            entityId = locationId,
            entityType = "HouseholdLocation",
            actorType = actor,
            actionType = "UPDATE",
            source = source,
            title = AppContext.getString(R.string.household_log_location_renamed, trimmed),
            description = repository.buildLocationPath(locationId),
            appTitle = AppContext.getString(R.string.household_title),
        )
    }

    /** 删除位置;有子级或物品时失败(由 Repository 抛 IllegalStateException)。 */
    suspend fun deleteLocation(
        locationId: String,
        actor: String,
        source: String,
    ): Result<Unit> = runCatching {
        val target = repository.getLocationById(locationId)
        repository.deleteLocation(locationId)
        activityLogRecorder.recordHousehold(
            entityId = locationId,
            entityType = "HouseholdLocation",
            actorType = actor,
            actionType = "DELETE",
            source = source,
            title = AppContext.getString(
                R.string.household_log_location_deleted,
                target?.name ?: locationId,
            ),
            description = "",
            appTitle = AppContext.getString(R.string.household_title),
        )
    }

    // ── 只读查询(供 AgentTool 使用) ─────────────────

    suspend fun searchItems(query: String, limit: Int = 20): List<ItemView> {
        val items = if (query.isBlank()) {
            repository.observeItems().first()
        } else {
            repository.search(query.trim()).first()
        }
        return items.take(limit.coerceIn(1, 100)).map { it.toView() }
    }

    /** 按名称定位物品:先精确匹配,再模糊取第一条。 */
    suspend fun findItemByName(name: String): HouseholdItemEntity? {
        val target = name.trim()
        if (target.isEmpty()) return null
        val all = repository.observeItems().first()
        return all.firstOrNull { it.name.equals(target, ignoreCase = true) }
            ?: repository.search(target).first().firstOrNull()
    }

    suspend fun getItemById(id: String): HouseholdItemEntity? = repository.getItemById(id)

    suspend fun buildLocationPath(locationId: String?): String =
        repository.buildLocationPath(locationId)

    // ── 内部 ─────────────────────────────────────────

    private suspend fun resolveLocationId(input: ItemInput): String? {
        val path = input.locationPath?.map { it.trim() }?.filter { it.isNotEmpty() }
        return when {
            !path.isNullOrEmpty() -> repository.findOrCreateLocationByPath(path).id
            else -> input.locationId
        }
    }

    /** 把用户选的图片压缩存到 filesDir/household_item_photos(JPEG 80),失败返回 null。 */
    private fun importPhoto(uri: Uri): String? = runCatching {
        val bitmap = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null
        val scale = PHOTO_MAX_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height)
        val scaled = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt().coerceAtLeast(1),
                (bitmap.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            bitmap
        }
        val dir = File(context.filesDir, PHOTO_DIR_NAME).apply { mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        file.outputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, PHOTO_JPEG_QUALITY, out)
        }
        file.absolutePath
    }.getOrNull()

    private fun deletePhoto(path: String) {
        if (path.isBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    private suspend fun logItemChange(
        entityId: String,
        actor: String,
        action: String,
        source: String,
        title: String,
        locationId: String?,
    ) {
        activityLogRecorder.recordHousehold(
            entityId = entityId,
            entityType = "HouseholdItem",
            actorType = actor,
            actionType = action,
            source = source,
            title = title,
            description = repository.buildLocationPath(locationId),
            appTitle = AppContext.getString(R.string.household_title),
        )
    }

    private suspend fun HouseholdItemEntity.toView(): ItemView = ItemView(
        id = id,
        name = name,
        category = category,
        locationPath = repository.buildLocationPath(locationId),
        expireAt = expireAt,
        note = note,
    )

    @Suppress("unused")
    private fun HouseholdItemEntity.toSnapshotJson(): String = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("category", category)
        put("locationId", locationId)
        put("expireAt", expireAt)
        put("note", note)
    }.toString()

    companion object {
        const val ACTOR_USER = "USER"
        const val ACTOR_AGENT = "AGENT"

        const val SOURCE_UI = "ui:household_items"
        const val SOURCE_AGENT_ADD = "agent_tool:add_household_item"
        const val SOURCE_AGENT_MOVE = "agent_tool:move_household_item"

        private const val PHOTO_DIR_NAME = "household_item_photos"
        private const val PHOTO_MAX_SIZE = 1280
        private const val PHOTO_JPEG_QUALITY = 80
    }
}
