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

package com.wanbaohe.recordcenter.service

import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.recordcenter.entity.HealthRecordEntity
import com.shifenmiao.database.recordcenter.repo.HealthRecordRepository
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.activity.ActivityCategory
import com.t8rin.logger.makeLog
import com.wanbaohe.recordcenter.R
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * 记录中心业务门面 — UI 层与 Agent 层共用的唯一写入入口。
 *
 * 职责:
 *  - 入参校验(类型存在、必填字段、字段范围、未知字段)
 *  - 调用 [HealthRecordRepository] 写库
 *  - 调用 [ActivityLogRecorder] 写审计日志
 *
 * 不做的事:
 *  - 不持有 UI 状态
 *  - 不暴露 Flow(订阅仍走 Repository,Component 直接 inject Repository 用作只读)
 *
 * 错误处理:校验失败抛出带可读文案的异常(经 string resources 本地化),
 * 由 Result 包装后交给调用方;Agent 工具会把 message 直接回给 LLM。
 */
@Singleton
class RecordCenterService @Inject constructor(
    private val repository: HealthRecordRepository,
    private val catalog: RecordTypeCatalog,
    private val activityLogRecorder: ActivityLogRecorder,
) {

    // ── 写入 ─────────────────────────────────────────

    suspend fun addRecord(
        typeKey: String,
        values: Map<String, Float>,
        happenedAt: Long,
        note: String?,
        actor: String,
    ): Result<HealthRecordEntity> = runCatching {
        val definition = resolveType(typeKey)
        val normalized = validateValues(definition, values)
        val now = System.currentTimeMillis()
        val entity = HealthRecordEntity(
            id = UUID.randomUUID().toString(),
            type = definition.key,
            happenedAt = happenedAt,
            fieldsJson = RecordFieldsCodec.encode(normalized),
            note = note?.takeIf { it.isNotBlank() }?.trim(),
            createdAt = now,
            updatedAt = now,
        )
        repository.upsert(entity)
        logChangeSafe(entity = entity, actor = actor, action = "CREATE")
        makeLog { "RecordCenterService.addRecord: ${definition.key} by $actor" }
        entity
    }

    suspend fun updateRecord(
        id: String,
        values: Map<String, Float>,
        happenedAt: Long,
        note: String?,
        actor: String,
    ): Result<HealthRecordEntity> = runCatching {
        val previous = repository.getById(id)
            ?: error(AppContext.getString(R.string.record_center_error_not_found))
        val definition = resolveType(previous.type)
        val normalized = validateValues(definition, values)
        val entity = previous.copy(
            happenedAt = happenedAt,
            fieldsJson = RecordFieldsCodec.encode(normalized),
            note = note?.takeIf { it.isNotBlank() }?.trim(),
            updatedAt = System.currentTimeMillis(),
        )
        repository.upsert(entity)
        logChangeSafe(entity = entity, actor = actor, action = "UPDATE", previous = previous)
        makeLog { "RecordCenterService.updateRecord: $id by $actor" }
        entity
    }

    suspend fun deleteRecord(
        id: String,
        actor: String,
    ): Result<Unit> = runCatching {
        val previous = repository.getById(id)
            ?: error(AppContext.getString(R.string.record_center_error_not_found))
        repository.deleteById(id)
        logChangeSafe(entity = previous, actor = actor, action = "DELETE")
        makeLog { "RecordCenterService.deleteRecord: $id by $actor" }
    }

    // ── 只读查询(供 Agent 工具使用) ─────────────────

    suspend fun getRecord(id: String): HealthRecordEntity? = repository.getById(id)

    suspend fun listRange(typeKey: String, from: Long, to: Long): List<HealthRecordEntity> {
        return repository.getRangeOnce(typeKey, from, to)
    }

    suspend fun latestPerType(): List<HealthRecordEntity> {
        return repository.getLatestPerTypeOnce()
    }

    // ── 校验 ─────────────────────────────────────────

    private fun resolveType(typeKey: String): RecordTypeDefinition {
        return catalog.byKey(typeKey)
            ?: error(AppContext.getString(R.string.record_center_error_unknown_type, typeKey))
    }

    /**
     * 校验并按定义顺序归一化字段值:
     *  - 未知字段一律拒绝(防 LLM/调用方乱传 key 污染 fieldsJson)
     *  - 必填字段缺失拒绝
     *  - 超出 min/max 合理范围拒绝
     */
    private fun validateValues(
        definition: RecordTypeDefinition,
        values: Map<String, Float>,
    ): Map<String, Float> {
        val fieldByKey = definition.fields.associateBy { it.key }
        values.keys.firstOrNull { it !in fieldByKey }?.let { unknownKey ->
            error(AppContext.getString(R.string.record_center_error_unknown_field, unknownKey))
        }
        definition.fields.firstOrNull { it.required && values[it.key] == null }?.let { missing ->
            error(
                AppContext.getString(
                    R.string.record_center_error_missing_field,
                    AppContext.getString(missing.labelRes),
                )
            )
        }
        definition.fields.forEach { field ->
            val value = values[field.key] ?: return@forEach
            val outOfRange = (field.min != null && value < field.min!!) ||
                (field.max != null && value > field.max!!)
            if (outOfRange) {
                error(
                    AppContext.getContext().getString(
                        R.string.record_center_error_out_of_range,
                        AppContext.getString(field.labelRes),
                        RecordFieldsCodec.formatValue(field.min ?: Float.MIN_VALUE),
                        RecordFieldsCodec.formatValue(field.max ?: Float.MAX_VALUE),
                    )
                )
            }
        }
        // 按定义顺序输出,保证 fieldsJson 键序稳定
        return definition.fields.mapNotNull { field ->
            values[field.key]?.let { field.key to it }
        }.toMap()
    }

    // ── 审计日志 ─────────────────────────────────────

    /**
     * 日志失败不弄崩主流程。
     *
     * 注意:CancellationException 必须继续上抛,不能吞掉——
     * 吞掉取消会破坏结构化并发(如 Agent 停止/超时后仍在写库);
     * 其余异常记 Error 级日志,便于排查"有记录无日志"的问题。
     */
    private suspend fun logChangeSafe(
        entity: HealthRecordEntity,
        actor: String,
        action: String,
        previous: HealthRecordEntity? = null,
    ) {
        runCatching {
            val titleRes = when (action) {
                "CREATE" -> R.string.record_center_log_created
                "UPDATE" -> R.string.record_center_log_updated
                else -> R.string.record_center_log_deleted
            }
            val typeTitle = catalog.byKey(entity.type)
                ?.let { AppContext.getString(it.titleRes) }
                ?: entity.type
            val payload = JSONObject().apply {
                put("entityId", entity.id)
                put("entityType", "HealthRecord")
                put("recordType", entity.type)
                put("actorType", actor)
                put("actionType", action)
                put("happenedAt", entity.happenedAt)
                put("fieldsJson", entity.fieldsJson)
                if (previous != null) put("previousFieldsJson", previous.fieldsJson)
            }.toString()
            activityLogRecorder.record(
                category = ActivityCategory.RECORD_CENTER,
                title = AppContext.getString(titleRes),
                appTitle = AppContext.getString(R.string.record_center_title),
                description = "$typeTitle · ${entity.fieldsJson}",
                payload = payload,
                dedupKey = "record_center_${System.currentTimeMillis()}_${UUID.randomUUID()}",
            )
        }.onFailure { error ->
            if (error is CancellationException) throw error
            error.makeLog(TAG) { "活动日志写入失败: $action ${entity.type} by $actor" }
        }
    }

    companion object {
        const val ACTOR_USER = "USER"
        const val ACTOR_AGENT = "AGENT"

        const val SOURCE_UI = "ui:record_center"
        const val SOURCE_AGENT_ADD = "agent_tool:add_health_record"
        const val SOURCE_AGENT_QUERY = "agent_tool:query_health_records"

        private const val TAG = "RecordCenterService"
    }
}
