package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiConfigSource
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.FileUploadStrategy
import com.shifenmiao.model.ai.config.EngineConfig

private fun normalizeAuthorizationCode(value: String?): String {
    return value
        ?.trim()
        ?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
        .orEmpty()
}

@Entity(
    tableName = "ai_engines",
    indices = [Index(value = ["name", "request_protocol"], unique = true)]
)
data class AiEngineEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "icon_name") val iconName: String = "",
    @ColumnInfo(name = "description") val description: String = "",
    @ColumnInfo(name = "request_url") val requestUrl: String = "",
    @ColumnInfo(name = "request_path") val requestPath: String = "",
    @ColumnInfo(name = "proxy_url") val proxyUrl: String = "",
    @ColumnInfo(name = "proxy_path") val proxyPath: String = "",
    @ColumnInfo(name = "request_protocol") val requestProtocol: String = AiRequestProtocol.OPENAI_COMPATIBLE.name,
    @ColumnInfo(name = "auth_type", defaultValue = "BEARER")
    val authType: String = AuthType.BEARER.name,
    @ColumnInfo(name = "authorization_code") val authorizationCode: String = "",
    @ColumnInfo(name = "stream") val stream: Boolean = true,
    @ColumnInfo(name = "api_can_set") val apiCanSet: Boolean = true,
    @ColumnInfo(name = "is_url_error") val isUrlError: Boolean = false,
    @ColumnInfo(name = "is_detest_passed") val isDetestPassed: Boolean = false,
    /** 该引擎当前选用的模型名称（每个引擎独立记忆） */
    @ColumnInfo(name = "selected_model_name") val selectedModelName: String = "",
    @ColumnInfo(name = "vip_level") val vipLevel: Int = 0,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "enabled") val enabled: Boolean = true,
    @ColumnInfo(name = "can_edit") val canEdit: Boolean = false,
    @ColumnInfo(name = "source") val source: String = AiConfigSource.REMOTE.name,
    @ColumnInfo(name = "file_upload_strategy", defaultValue = "BASE64")
    val fileUploadStrategy: String = FileUploadStrategy.BASE64.name,
    @ColumnInfo(name = "cloud_storage_connection_id") val cloudStorageConnectionId: String? = null,
    @ColumnInfo(name = "cloud_storage_bucket") val cloudStorageBucket: String? = null,
    @ColumnInfo(name = "cloud_storage_prefix") val cloudStoragePrefix: String = "ai-uploads/"
) {

    fun sourceType(): AiConfigSource = AiConfigSource.fromValue(source)

    fun isLocalOwned(): Boolean {
        return sourceType() == AiConfigSource.LOCAL || canEdit
    }

    /**
     * 转换为 AiEngine 对象（每次返回新实例，不修改任何共享状态）
     * @param model 关联的 AiModel，如果为 null 则使用默认模型
     */
    fun toAiEngine(model: AiModel? = null): AiEngine {
        val provider = getProviderByName(name)
        val defaultEngine = AiEngine.builtInEngine(provider)
        val normalizedProtocol = AiRequestProtocol.fromValue(requestProtocol)
        val resolvedModel = model
            ?.takeIf { it.engineName.equals(name, ignoreCase = true) }
            ?.takeIf { selectedModelName.isBlank() || it.name == selectedModelName }
            ?: AiModel.getDefaultModelForProvider(provider).copy(engineName = name)

        return AiEngine(
            name = name,
            title = title.ifEmpty { defaultEngine.title },
            description = description.ifEmpty { defaultEngine.description },
            requestUrl = requestUrl.ifEmpty { defaultEngine.requestUrl },
            requestPath = requestPath.ifEmpty { defaultEngine.requestPath },
            proxyUrl = proxyUrl,
            proxyPath = proxyPath,
            authorizationCode = normalizeAuthorizationCode(authorizationCode)
                .ifEmpty { normalizeAuthorizationCode(defaultEngine.authorizationCode) },
            model = resolvedModel,
            requestProtocol = normalizedProtocol,
            authType = AuthType.resolve(authType, normalizedProtocol),
            stream = stream,
            isUrlError = isUrlError,
            isDetestPassed = isDetestPassed,
            apiCanSet = apiCanSet,
            iconName = iconName,
            fileUploadStrategy = FileUploadStrategy.valueOf(fileUploadStrategy),
            cloudStorageConnectionId = cloudStorageConnectionId,
            cloudStorageBucket = cloudStorageBucket,
            cloudStoragePrefix = cloudStoragePrefix
        )
    }

    companion object {

        fun getProviderByName(name: String): AiProvider {
            return AiProvider.fromValue(name)
        }

        /**
         * 从 AiEngine 转换为 AiEngineEntity
         */
        fun fromAiEngine(
            engine: AiEngine,
            existingEntity: AiEngineEntity? = null,
            source: AiConfigSource = AiConfigSource.REMOTE,
            canEdit: Boolean = false,
        ): AiEngineEntity {
            val defaultVipLevel = if (engine.name == AiProvider.OpenAi.value) 2 else 0
            return AiEngineEntity(
                id = existingEntity?.id ?: 0,
                name = engine.name,
                title = engine.title,
                description = engine.description,
                requestUrl = engine.requestUrl,
                requestPath = engine.requestPath,
                proxyUrl = engine.proxyUrl,
                proxyPath = engine.proxyPath,
                requestProtocol = engine.requestProtocol.name,
                authType = engine.authType.name,
                authorizationCode = normalizeAuthorizationCode(engine.authorizationCode),
                stream = engine.stream,
                isUrlError = engine.isUrlError,
                isDetestPassed = engine.isDetestPassed,
                selectedModelName = engine.model.name,
                vipLevel = existingEntity?.vipLevel ?: defaultVipLevel,
                sortOrder = existingEntity?.sortOrder ?: 0,
                enabled = existingEntity?.enabled ?: true,
                canEdit = canEdit,
                source = source.name,
                apiCanSet = engine.apiCanSet,
                iconName = engine.iconName,
                fileUploadStrategy = engine.fileUploadStrategy.name,
                cloudStorageConnectionId = engine.cloudStorageConnectionId,
                cloudStorageBucket = engine.cloudStorageBucket,
                cloudStoragePrefix = engine.cloudStoragePrefix
            )
        }

        fun buildIdentityKey(name: String, requestProtocol: String): String {
            return "${name.trim().lowercase()}#${AiRequestProtocol.fromValue(requestProtocol).name}"
        }

        /**
         * 从远程配置转换为 AiEngineEntity
         * @param config 远程引擎配置
         * @param existingEntity 现有的本地实体（用于保留用户数据）
         */
        fun fromEngineConfig(
            config: EngineConfig,
            existingEntity: AiEngineEntity? = null
        ): AiEngineEntity {
            val normalizedProtocol = AiRequestProtocol.fromValue(config.requestProtocol)
            val normalizedAuthType = AuthType.resolve(config.authType, normalizedProtocol)
            return AiEngineEntity(
                id = existingEntity?.id ?: 0,
                name = config.name.orEmpty(),
                title = config.title.orEmpty(),
                description = config.description ?: "",
                requestUrl = config.requestUrl.orEmpty(),
                requestPath = config.requestPath.orEmpty(),
                proxyUrl = config.proxyUrl.orEmpty(),
                proxyPath = config.proxyPath.orEmpty(),
                requestProtocol = normalizedProtocol.name,
                authType = normalizedAuthType.name,
                // 保留用户自定义的 API Key
                authorizationCode = normalizeAuthorizationCode(existingEntity?.authorizationCode),
                stream = config.stream ?: true,
                isUrlError = existingEntity?.isUrlError ?: false,
                isDetestPassed = existingEntity?.isDetestPassed ?: false,
                selectedModelName = existingEntity?.selectedModelName.orEmpty(),
                vipLevel = config.vipLevel ?: 0,
                sortOrder = config.sortOrder ?: 0,
                enabled = config.enabled ?: true,
                canEdit = existingEntity?.canEdit ?: false,
                source = existingEntity?.source ?: AiConfigSource.REMOTE.name,
                apiCanSet = existingEntity?.apiCanSet ?: true,
                iconName = existingEntity?.iconName ?: "",
                fileUploadStrategy = existingEntity?.fileUploadStrategy ?: FileUploadStrategy.BASE64.name,
                cloudStorageConnectionId = existingEntity?.cloudStorageConnectionId,
                cloudStorageBucket = existingEntity?.cloudStorageBucket,
                cloudStoragePrefix = existingEntity?.cloudStoragePrefix ?: "ai-uploads/"
            )
        }

    }
}
