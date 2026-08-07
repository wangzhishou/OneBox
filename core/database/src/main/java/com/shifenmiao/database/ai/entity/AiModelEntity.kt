package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiConfigSource
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.config.ModelConfig
import java.util.Date

@Entity(
    tableName = "ai_models",
    indices = [
        Index(value = ["name", "engine_name"], unique = true)
    ]
)
data class AiModelEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "can_upload_file") val canUploadFile: Boolean = false,
    @ColumnInfo(name = "can_network") val canNetwork: Boolean = false,
    @ColumnInfo(name = "temperature") val temperature: Double = 0.95,
    @ColumnInfo(name = "top_p") val topP: Double = 0.8,
    @ColumnInfo(name = "free") val free: Boolean = false,
    @ColumnInfo(name = "provider") val provider: String,
    @ColumnInfo(name = "base_points") val basePoints: Float = 1f,
    @ColumnInfo(name = "max_tokens") val maxTokens: Int = 2048,
    @ColumnInfo(name = "context_window_tokens") val contextWindowTokens: Int = 264000,
    @ColumnInfo(name = "can_reasoning") val canReasoning: Boolean = false,
    @ColumnInfo(name = "can_edit") val canEdit: Boolean = false,
    @ColumnInfo(name = "update_time") val updateTime: Date = Date(),
    @ColumnInfo(name = "can_video") val canVideo: Boolean = false,
    @ColumnInfo(name = "can_image") val canImage: Boolean = false,
    @ColumnInfo(name = "can_use_temp_api") val canUseTempApi: Boolean = false,
    @ColumnInfo(name = "is_fast") val isFast: Boolean = false,
    @ColumnInfo(name = "is_code") val isCode: Boolean = false,
    @ColumnInfo(name = "support_tool_calls") val supportToolCalls: Boolean = true,
    @ColumnInfo(name = "override_can_upload_file") val overrideCanUploadFile: Boolean? = null,
    @ColumnInfo(name = "override_can_network") val overrideCanNetwork: Boolean? = null,
    @ColumnInfo(name = "override_temperature") val overrideTemperature: Double? = null,
    @ColumnInfo(name = "override_top_p") val overrideTopP: Double? = null,
    @ColumnInfo(name = "override_max_tokens") val overrideMaxTokens: Int? = null,
    @ColumnInfo(name = "override_can_reasoning") val overrideCanReasoning: Boolean? = null,
    @ColumnInfo(name = "override_can_video") val overrideCanVideo: Boolean? = null,
    @ColumnInfo(name = "override_can_image") val overrideCanImage: Boolean? = null,
    @ColumnInfo(name = "override_can_use_temp_api") val overrideCanUseTempApi: Boolean? = null,
    @ColumnInfo(name = "override_is_fast") val overrideIsFast: Boolean? = null,
    @ColumnInfo(name = "override_is_code") val overrideIsCode: Boolean? = null,
    @ColumnInfo(name = "override_support_tool_calls") val overrideSupportToolCalls: Boolean? = null,
    // 关联的引擎名称（与 AiEngineEntity.name 对应，软关联不加外键）
    @ColumnInfo(name = "engine_name") val engineName: String = "",
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "enabled") val enabled: Boolean = true,
    @ColumnInfo(name = "source") val source: String = AiConfigSource.REMOTE.name,
) {
    fun sourceType(): AiConfigSource = AiConfigSource.fromValue(source)

    fun isLocalOwned(): Boolean {
        return sourceType() == AiConfigSource.LOCAL
    }

    fun supportsFullEditing(): Boolean = sourceType() == AiConfigSource.LOCAL

    fun hasLocalOverrides(): Boolean {
        return overrideCanUploadFile != null ||
            overrideCanNetwork != null ||
            overrideTemperature != null ||
            overrideTopP != null ||
            overrideMaxTokens != null ||
            overrideCanReasoning != null ||
            overrideCanVideo != null ||
            overrideCanImage != null ||
            overrideCanUseTempApi != null ||
            overrideIsFast != null ||
            overrideIsCode != null ||
            overrideSupportToolCalls != null
    }

    private fun <T> resolvedValue(base: T, override: T?): T = override ?: base

    fun applyPartialOverrides(
        model: AiModel,
        forceStoreValues: Boolean = false,
    ): AiModelEntity {
        fun <T> overrideValue(base: T, current: T): T? {
            return if (forceStoreValues || current != base) current else null
        }

        return copy(
            overrideCanUploadFile = overrideValue(canUploadFile, model.canUploadFile),
            overrideCanNetwork = overrideValue(canNetwork, model.canNetwork),
            overrideTemperature = overrideValue(temperature, model.temperature),
            overrideTopP = overrideValue(topP, model.topP),
            overrideMaxTokens = overrideValue(maxTokens, model.maxTokens),
            overrideCanReasoning = overrideValue(canReasoning, model.canReasoning),
            overrideCanVideo = overrideValue(canVideo, model.canVideo),
            overrideCanImage = overrideValue(canImage, model.canImage),
            overrideCanUseTempApi = overrideValue(canUseTempApi, model.canUseTempApi),
            overrideIsFast = overrideValue(isFast, model.isFast),
            overrideIsCode = overrideValue(isCode, model.isCode),
            overrideSupportToolCalls = overrideValue(supportToolCalls, model.supportToolCalls),
            updateTime = Date(),
        )
    }

    fun clearPartialOverrides(): AiModelEntity {
        return copy(
            overrideCanUploadFile = null,
            overrideCanNetwork = null,
            overrideTemperature = null,
            overrideTopP = null,
            overrideMaxTokens = null,
            overrideCanReasoning = null,
            overrideCanVideo = null,
            overrideCanImage = null,
            overrideCanUseTempApi = null,
            overrideIsFast = null,
            overrideIsCode = null,
            overrideSupportToolCalls = null,
            updateTime = Date(),
        )
    }

    fun toAiModel(): AiModel {
        val providerObj = AiProvider.fromValue(provider)

        return AiModel(
            id = id,
            name = name,
            title = title,
            description = description,
            canUploadFile = resolvedValue(canUploadFile, overrideCanUploadFile),
            canNetwork = resolvedValue(canNetwork, overrideCanNetwork),
            temperature = resolvedValue(temperature, overrideTemperature),
            topP = resolvedValue(topP, overrideTopP),
            free = free,
            provider = providerObj,
            basePoints = basePoints,
            maxTokens = resolvedValue(maxTokens, overrideMaxTokens),
            contextWindowTokens = contextWindowTokens,
            canReasoning = resolvedValue(canReasoning, overrideCanReasoning),
            canEdit = supportsFullEditing(),
            updateTime = updateTime.time,
            canVideo = resolvedValue(canVideo, overrideCanVideo),
            canImage = resolvedValue(canImage, overrideCanImage),
            canUseTempApi = resolvedValue(canUseTempApi, overrideCanUseTempApi),
            isFast = resolvedValue(isFast, overrideIsFast),
            isCode = resolvedValue(isCode, overrideIsCode),
            supportToolCalls = resolvedValue(supportToolCalls, overrideSupportToolCalls),
            hasLocalOverrides = hasLocalOverrides(),
            engineName = engineName.ifBlank { provider },
        )
    }

    companion object {
        fun buildIdentityKey(name: String, engineName: String): String {
            return "${name.trim().lowercase()}#${engineName.trim().lowercase()}"
        }

        fun fromAiModel(
            model: AiModel,
            existingEntity: AiModelEntity? = null,
            source: AiConfigSource = existingEntity?.sourceType() ?: AiConfigSource.LOCAL,
        ): AiModelEntity {
            val resolvedEngineName = existingEntity?.engineName ?: model.engineName.ifBlank { model.provider.value }
            return AiModelEntity(
                id = existingEntity?.id ?: model.id.takeIf { source != AiConfigSource.REMOTE && it > 0 } ?: 0,
                name = model.name,
                title = model.title,
                description = model.description,
                canUploadFile = model.canUploadFile,
                canNetwork = model.canNetwork,
                temperature = model.temperature,
                topP = model.topP,
                free = model.free,
                provider = model.provider.value,
                basePoints = model.basePoints,
                maxTokens = model.maxTokens,
                contextWindowTokens = model.contextWindowTokens,
                canReasoning = model.canReasoning,
                canEdit = model.canEdit,
                updateTime = existingEntity?.updateTime ?: Date(),
                canImage = model.canImage,
                canVideo = model.canVideo,
                canUseTempApi = model.canUseTempApi,
                isFast = model.isFast,
                isCode = model.isCode,
                supportToolCalls = model.supportToolCalls,
                overrideCanUploadFile = null,
                overrideCanNetwork = null,
                overrideTemperature = null,
                overrideTopP = null,
                overrideMaxTokens = null,
                overrideCanReasoning = null,
                overrideCanVideo = null,
                overrideCanImage = null,
                overrideCanUseTempApi = null,
                overrideIsFast = null,
                overrideIsCode = null,
                overrideSupportToolCalls = null,
                engineName = resolvedEngineName,
                sortOrder = existingEntity?.sortOrder ?: 0,
                enabled = existingEntity?.enabled ?: true,
                source = source.name,
            )
        }


        /**
         * 从远程配置转换为 AiModelEntity
         * @param config 远程模型配置
         * @param existingEntity 现有的本地实体（用于保留用户数据）
         *
         * 更新策略：
         * - 本地模型（source=LOCAL）：完全保留本地数据，不被远程覆盖
         * - 远程模型（source=REMOTE）：基础字段跟随远程更新，仅保留 override_* 中的本地偏好
         */
        fun fromModelConfig(
            config: ModelConfig,
            existingEntity: AiModelEntity? = null
        ): AiModelEntity {
            if (existingEntity?.isLocalOwned() == true) {
                return existingEntity
            }

            return AiModelEntity(
                id = existingEntity?.id ?: 0,
                name = config.name.orEmpty(),
                title = config.title.orEmpty(),
                description = config.description ?: "",
                canUploadFile = config.canUploadFile ?: false,
                canNetwork = config.canNetwork ?: false,
                free = config.free ?: false,
                basePoints = config.basePoints ?: 1f,
                provider = config.provider.orEmpty(),
                canReasoning = config.canReasoning ?: false,
                canEdit = false,
                canImage = config.canImage ?: false,
                canVideo = config.canVideo ?: false,
                canUseTempApi = config.canUseTempApi ?: false,
                isFast = config.isFast ?: false,
                isCode = config.isCode ?: false,
                supportToolCalls = config.supportToolCalls ?: true,
                overrideCanUploadFile = existingEntity?.overrideCanUploadFile,
                overrideCanNetwork = existingEntity?.overrideCanNetwork,
                overrideTemperature = existingEntity?.overrideTemperature,
                overrideTopP = existingEntity?.overrideTopP,
                overrideMaxTokens = existingEntity?.overrideMaxTokens,
                overrideCanReasoning = existingEntity?.overrideCanReasoning,
                overrideCanVideo = existingEntity?.overrideCanVideo,
                overrideCanImage = existingEntity?.overrideCanImage,
                overrideCanUseTempApi = existingEntity?.overrideCanUseTempApi,
                overrideIsFast = existingEntity?.overrideIsFast,
                overrideIsCode = existingEntity?.overrideIsCode,
                overrideSupportToolCalls = existingEntity?.overrideSupportToolCalls,
                engineName = config.provider.orEmpty(),
                sortOrder = config.sortOrder ?: 0,
                enabled = config.enabled ?: true,
                temperature = config.temperature ?: 0.95,
                topP = config.topP ?: 0.8,
                maxTokens = config.maxTokens ?: 2048,
                contextWindowTokens = config.contextWindowTokens ?: 264000,
                updateTime = existingEntity?.updateTime ?: Date(),
                source = AiConfigSource.REMOTE.name,
            )
        }
    }
}