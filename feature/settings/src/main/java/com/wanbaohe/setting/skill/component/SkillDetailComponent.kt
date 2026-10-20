package com.wanbaohe.setting.skill.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.SkillImportValidator
import com.shifenmiao.database.ai.SkillLocalStore
import com.shifenmiao.database.ai.entity.SkillEntity
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 技能全屏正文编辑页组件（编辑 LOCAL / 只读查看 BUNDLED·REMOTE 两态）。
 *
 * 本页只管正文：name/description 的编辑在列表页的元数据弹窗里完成。
 * 保存时用**库里最新**的 name/description 重新拼装 SKILL.md，
 * 防止弹窗改名后详情页用旧值。
 */
class SkillDetailComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val skillId: String,
    @Assisted val onGoBack: () -> Unit,
    private val appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    /** 保存结果：SLUG_IMMUTABLE 是兜底（name 已由元数据弹窗管理，正常不可达） */
    enum class SaveResult { SUCCESS, INVALID, SLUG_IMMUTABLE }

    private val skillDao = appDatabase.skillDao()

    private val _skill = MutableStateFlow<SkillEntity?>(null)
    val skill: StateFlow<SkillEntity?> = _skill.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    init {
        componentScope.launch(ioDispatcher) {
            _skill.value = skillId.takeIf { it.isNotBlank() }?.let { skillDao.getById(it) }
            _loaded.value = true
        }
    }

    /** 当前是否可编辑（LOCAL）；BUNDLED / REMOTE 只读 */
    fun isEditable(): Boolean {
        return _skill.value?.source == SkillEntity.SOURCE_LOCAL
    }

    /**
     * 保存正文：重读库里该行，用最新 name/description 拼装完整 SKILL.md，
     * 重解析校验后 upsert（slug 身份兜底校验保留）。
     */
    fun saveEdit(bodyContent: String, onResult: (SaveResult) -> Unit) {
        componentScope.launch(ioDispatcher) {
            val current = skillDao.getById(skillId)
            if (current == null || current.source != SkillEntity.SOURCE_LOCAL) {
                onResult(SaveResult.INVALID)
                return@launch
            }
            val markdown = buildString {
                appendLine("---")
                appendLine("name: ${current.name}")
                appendLine("description: ${current.description}")
                appendLine("---")
                appendLine()
                append(bodyContent.trim())
            }
            val validated = SkillImportValidator.validate(markdown).getOrNull()
            if (validated == null) {
                onResult(SaveResult.INVALID)
                return@launch
            }
            if (validated.slug != current.id) {
                onResult(SaveResult.SLUG_IMMUTABLE)
                return@launch
            }
            val updated = current.copy(
                name = validated.slug,
                description = validated.description,
                body = validated.body,
                updatedAt = System.currentTimeMillis()
            )
            skillDao.upsert(updated)
            _skill.value = updated
            onResult(SaveResult.SUCCESS)
        }
    }

    /** BUNDLED / REMOTE 只读 → 另存副本；成功后本页切换为副本的编辑态 */
    fun saveAsCopy(onResult: (SkillEntity?) -> Unit) {
        val current = _skill.value ?: return
        componentScope.launch(ioDispatcher) {
            val copy = SkillLocalStore.saveAsCopy(skillDao, current)
            if (copy != null) _skill.value = copy
            onResult(copy)
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            skillId: String,
            onGoBack: () -> Unit,
        ): SkillDetailComponent
    }
}
