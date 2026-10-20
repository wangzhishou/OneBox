package com.wanbaohe.recordcenter.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.recordcenter.repo.HealthRecordRepository
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.recordcenter.model.RecordFieldsCodec
import com.wanbaohe.recordcenter.registry.RecordTypeCatalog
import com.wanbaohe.recordcenter.registry.RecordTypeDefinition
import com.wanbaohe.recordcenter.service.RecordCenterService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** 表单字段校验错误,由界面层本地化为文案 */
sealed interface RecordFieldError {
    data object Required : RecordFieldError
    data object InvalidNumber : RecordFieldError
    data class OutOfRange(val min: Float?, val max: Float?) : RecordFieldError
}

/**
 * 记录录入/编辑页 Component — 按 [RecordTypeDefinition.fields] 驱动动态表单。
 *
 * editingRecordId 非空时为编辑模式:启动时加载实体预填,保存走 updateRecord。
 */
class RecordEditorComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("recordType") val recordTypeKey: String,
    @Assisted("editingRecordId") val editingRecordId: String?,
    @Assisted val onGoBack: () -> Unit,
    dispatchersHolder: DispatchersHolder,
    catalog: RecordTypeCatalog,
    private val repository: HealthRecordRepository,
    private val service: RecordCenterService,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** 未知类型时为 null,界面层展示空态 */
    val definition: RecordTypeDefinition? = catalog.byKey(recordTypeKey)

    val isEditing: Boolean = editingRecordId != null

    /** 字段 key → 输入文本 */
    private val _inputs = MutableStateFlow<Map<String, String>>(emptyMap())
    val inputs: StateFlow<Map<String, String>> = _inputs

    /** 字段 key → 校验错误 */
    private val _fieldErrors = MutableStateFlow<Map<String, RecordFieldError>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, RecordFieldError>> = _fieldErrors

    private val _happenedAt = MutableStateFlow(System.currentTimeMillis())
    val happenedAt: StateFlow<Long> = _happenedAt

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    /** service 层失败的可读错误信息(已本地化),界面层 toast 后调用 [consumeSaveError] */
    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving

    init {
        if (editingRecordId != null) {
            componentScope.launch {
                repository.getById(editingRecordId)?.let { entity ->
                    val values = RecordFieldsCodec.decode(entity.fieldsJson)
                    _inputs.value = values.mapValues { (_, v) -> RecordFieldsCodec.formatValue(v) }
                    _happenedAt.value = entity.happenedAt
                    _note.value = entity.note.orEmpty()
                }
            }
        }
    }

    fun onFieldInputChange(fieldKey: String, value: String) {
        _inputs.value = _inputs.value + (fieldKey to value)
        // 输入即清除该字段的旧错误
        if (_fieldErrors.value.containsKey(fieldKey)) {
            _fieldErrors.value = _fieldErrors.value - fieldKey
        }
    }

    fun onNoteChange(value: String) {
        _note.value = value
    }

    /** 日期变化:保留当前时分,只替换日期部分 */
    fun onDateChange(dateMillis: Long) {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
        val time = Instant.ofEpochMilli(_happenedAt.value).atZone(zone).toLocalTime()
        _happenedAt.value = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
    }

    /** 时间变化:保留当前日期,只替换时分 */
    fun onTimeChange(hour: Int, minute: Int) {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(_happenedAt.value).atZone(zone).toLocalDate()
        _happenedAt.value = date.atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli()
    }

    /**
     * 校验并保存。同步校验失败只更新 [fieldErrors];service 失败更新 [saveError];
     * 成功直接 onGoBack()。
     */
    fun submit() {
        val def = definition ?: return
        if (_saving.value) return

        val values = mutableMapOf<String, Float>()
        val errors = mutableMapOf<String, RecordFieldError>()
        def.fields.forEach { field ->
            val raw = _inputs.value[field.key]?.trim().orEmpty()
            if (raw.isEmpty()) {
                if (field.required) errors[field.key] = RecordFieldError.Required
                return@forEach
            }
            val parsed = raw.toFloatOrNull()
            if (parsed == null) {
                errors[field.key] = RecordFieldError.InvalidNumber
                return@forEach
            }
            val outOfRange = (field.min != null && parsed < field.min!!) ||
                (field.max != null && parsed > field.max!!)
            if (outOfRange) {
                errors[field.key] = RecordFieldError.OutOfRange(field.min, field.max)
            } else {
                values[field.key] = parsed
            }
        }
        _fieldErrors.value = errors
        if (errors.isNotEmpty()) return

        _saving.value = true
        componentScope.launch {
            val result = if (editingRecordId != null) {
                service.updateRecord(
                    id = editingRecordId,
                    values = values,
                    happenedAt = _happenedAt.value,
                    note = _note.value,
                    actor = RecordCenterService.ACTOR_USER,
                )
            } else {
                service.addRecord(
                    typeKey = recordTypeKey,
                    values = values,
                    happenedAt = _happenedAt.value,
                    note = _note.value,
                    actor = RecordCenterService.ACTOR_USER,
                )
            }
            _saving.value = false
            result
                .onSuccess { onGoBack() }
                .onFailure { _saveError.value = it.message }
        }
    }

    fun consumeSaveError() {
        _saveError.value = null
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("recordType") recordType: String,
            @Assisted("editingRecordId") editingRecordId: String?,
            onGoBack: () -> Unit,
        ): RecordEditorComponent
    }
}
