package com.wanbaohe.setting.skill.component

import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.ai.SkillImportValidator
import com.shifenmiao.database.ai.SkillLocalStore
import com.shifenmiao.database.ai.SkillUsagePolicy
import com.shifenmiao.database.ai.entity.SkillEntity
import com.shifenmiao.storage.AIChatStorage
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import com.wanbaohe.core.ui.utils.navigation.Screen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * AI 技能管理页组件。
 *
 * 数据接入照 SystemPromptManagementComponent 模式：直接注入 AppDatabase 拿 DAO，
 * feature/settings 不依赖 feature/ai；导入/保存校验与使用频次分档统一走
 * core/database 的 [SkillImportValidator] / [SkillUsagePolicy] /
 * [SkillFrontMatterParser]（与 AppDatabase 预置、feature/ai 同一规则）。
 */
class SkillManagementComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val onGoBack: () -> Unit,
    @Assisted val onNavigate: (Screen) -> Unit,
    private val appDatabase: AppDatabase,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    private val skillDao = appDatabase.skillDao()

    /** 全局技能总开关 */
    val globalSkillsEnabled: StateFlow<Boolean> = AIChatStorage.isEnableSkills

    /** 全部技能（含停用），按 name 排序 */
    val skills: StateFlow<List<SkillEntity>> = skillDao.observeAll()
        .stateIn(componentScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun usageFrequency(useCount: Double): SkillUsagePolicy.UsageFrequency =
        SkillUsagePolicy.frequencyOf(useCount)

    fun setGlobalSkillsEnabled(enabled: Boolean) {
        AIChatStorage.saveIsEnableSkills(enabled)
    }

    fun setSkillEnabled(skill: SkillEntity, enabled: Boolean) {
        componentScope.launch {
            skillDao.update(
                skill.copy(enabled = enabled, updatedAt = System.currentTimeMillis())
            )
        }
    }

    /** 仅 LOCAL 技能可删除。 */
    fun deleteSkill(skill: SkillEntity) {
        if (skill.source != SkillEntity.SOURCE_LOCAL) return
        componentScope.launch {
            skillDao.deleteById(skill.id)
        }
    }

    /**
     * 更新技能元数据（name/description，不动正文），仅 LOCAL 可改；
     * slug 变更在事务内做主键迁移（[SkillLocalStore.updateMetadata]）。
     */
    fun updateMetadata(
        skill: SkillEntity,
        newName: String,
        newDescription: String,
        onResult: (SkillLocalStore.MetadataResult) -> Unit = {}
    ) {
        componentScope.launch {
            onResult(
                SkillLocalStore.updateMetadata(
                    appDatabase = appDatabase,
                    skillDao = skillDao,
                    skill = skill,
                    newName = newName,
                    newDescription = newDescription,
                )
            )
        }
    }

    /**
     * 从 SKILL.md 文本导入 LOCAL 技能（剪贴板 / 文件导入共用管线），
     * 语义与新建编辑页一致（[SkillLocalStore.import]）。
     *
     * @param onResult null = 成功；否则为拒绝原因，由页面映射文案
     */
    fun importFromContent(
        content: String,
        onResult: (SkillImportValidator.Rejection?) -> Unit = {}
    ) {
        componentScope.launch {
            SkillLocalStore.import(skillDao, content).fold(
                onSuccess = { onResult(null) },
                onFailure = { error ->
                    onResult(
                        (error as? SkillImportValidator.SkillImportException)?.rejection
                            ?: SkillImportValidator.Rejection.INVALID_FRONTMATTER
                    )
                }
            )
        }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onGoBack: () -> Unit,
            onNavigate: (Screen) -> Unit,
        ): SkillManagementComponent
    }
}
