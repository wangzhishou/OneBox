package com.wanbaohe.setting.ai.component

import android.content.ClipboardManager
import android.content.Context
import com.arkivanov.decompose.ComponentContext
import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AddEngineResult
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.core.ui.utils.BaseComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AIAddEngineComponent @AssistedInject internal constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted("initialProtocol") initialProtocol: String,
    @Assisted("importConfig") importConfig: String,
    @Assisted val onGoBack: () -> Unit,
    @ApplicationContext private val context: Context,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
    private val aiEngineManager: AIEngineManager,
    dispatchersHolder: DispatchersHolder,
) : BaseComponent(dispatchersHolder, componentContext) {

    /**
     * 协议是否锁定(F4): 只看入口协议 —— initialProtocol 非空表示从 Jev/Pikafish
     * 专属入口进来, 协议不可切; 普通入口不锁, 选择器始终展示全量列表、可自由来回切换。
     */
    val protocolLocked: Boolean = initialProtocol.isNotBlank()

    // 脏检查基线必须是「预填完成后」的快照(R1): 进页即按当前协议补默认 Path(F3),
    // 基线若留在补之前的空 Path, 用户什么都不碰也会被弹「放弃修改」。
    // 只在 Path 为空时补, 深链/导入带入的 Path 不会被冲掉;
    // Jev/Pikafish 专属入口已由 withInitialProtocol 预填, protocolLocked 下不补。
    private val initialDraft = aiEngineCatalogManager.createLocalEngineDraft()
        .withInitialProtocol(initialProtocol)
        .let { draft ->
            if (draft.requestPath.isBlank() && !protocolLocked) {
                draft.switchedRequestProtocol(draft.requestProtocol)
            } else {
                draft
            }
        }

    private val _draft = MutableStateFlow(initialDraft)
    val draft: StateFlow<AiEngine> = _draft.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    /** 深链 onebox://action/import_engine 带进来的配置, 由页面消费一次后清空 */
    private var pendingImportConfig: String = importConfig

    fun updateDraft(transform: (AiEngine) -> AiEngine) {
        _draft.value = transform(_draft.value)
    }

    fun save(onComplete: (AddEngineResult) -> Unit) {
        val draft = _draft.value
        _isSaving.value = true
        // 走新增入口: 撞名会被拒绝、写入后会回读校验, 不会再出现"提示成功但列表里没有"
        aiEngineCatalogManager.addLocalEngine(draft) { result ->
            _isSaving.value = false
            onComplete(result)
        }
    }

    fun hasDraftChanged(): Boolean = _draft.value != initialDraft

    /**
     * 切换请求协议, 预填规则与详情页共用 [switchedRequestProtocol](F4 收敛):
     * authType 跟随新协议默认(用户手改过的除外);
     * Path 仍是上一个协议的默认值时一并跟随, 用户手改过的自定义路径保持不动;
     * 切到 Jev 预填官网地址/端点, Pikafish 清空直连字段。
     * 仅专属入口(protocolLocked)拒绝切换, 普通入口可自由来回切。
     */
    fun updateRequestProtocol(protocol: AiRequestProtocol) {
        if (protocolLocked) return
        updateDraft { it.switchedRequestProtocol(protocol) }
    }

    /**
     * 粘 key 识别协议(纯启发式, 只提示不自动切)。
     *
     * 只保留能明确映射到某个协议的前缀: sk-ant- → Anthropic 兼容。
     * sk-or-(OpenRouter)/AIza(Gemini)/xai-(Grok)/通用 sk- 对应的服务商都走
     * OpenAI 兼容 —— 即默认协议, 没有切换目标, 不出提示。
     */
    fun detectTokenProtocol(token: String): AiRequestProtocol? {
        val trimmed = token.trim()
        if (trimmed.length < MIN_TOKEN_LENGTH) return null
        return if (trimmed.startsWith(ANTHROPIC_KEY_PREFIX)) {
            AiRequestProtocol.ANTHROPIC_COMPATIBLE
        } else {
            null
        }
    }

    /** 保存成功后把新引擎设为当前对话引擎; 失败返回 false(不阻塞退出) */
    fun setDraftAsCurrentEngine(): Boolean {
        // setDefaultEngine 返回 Unit、失败路径静默 return, 必须回读确认真正生效(F9)
        return runCatching {
            val draft = _draft.value
            aiEngineManager.setDefaultEngine(draft)
            val current = aiEngineManager.getCurrentAiEngine()
            current.identityKey() == draft.identityKey() ||
                current.name.equals(draft.name, ignoreCase = true)
        }.getOrDefault(false)
    }

    fun takePendingImportConfig(): String {
        return pendingImportConfig.also { pendingImportConfig = "" }
    }

    /** 从系统剪贴板读取配置文本并预填表单 */
    fun importConfigFromClipboard(onComplete: (EngineImportResult) -> Unit) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val text = clipboard?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)
            ?.toString()
        if (text.isNullOrBlank()) {
            onComplete(EngineImportResult.EmptyClipboard)
            return
        }
        applyImportedConfig(text, onComplete)
    }

    /**
     * 解析配置文本并预填表单(剪贴板与深链共用)。
     * 名称撞已有引擎时自动加 "-2"/"-3"… 后缀。
     */
    fun applyImportedConfig(raw: String, onComplete: (EngineImportResult) -> Unit) {
        val decoded = AiEngineConfigCodec.decode(raw)
        if (decoded == null) {
            onComplete(EngineImportResult.Invalid)
            return
        }
        componentScope.launch {
            val resolvedName = resolveAvailableName(decoded.name)
            _draft.value = decoded.copy(name = resolvedName)
            // 导入配置缺 Path 时按协议补默认值(只在为空时补, 不覆盖导入自带的 Path)
            if (_draft.value.requestPath.isBlank()) {
                updateRequestProtocol(_draft.value.requestProtocol)
            }
            onComplete(
                EngineImportResult.Success(
                    displayName = decoded.title.ifBlank { resolvedName },
                    renamed = !resolvedName.equals(decoded.name, ignoreCase = true),
                )
            )
        }
    }

    private suspend fun resolveAvailableName(rawName: String): String {
        val base = rawName.trim().ifBlank { DEFAULT_IMPORTED_NAME }
        if (aiEngineCatalogManager.getEngineByName(base) == null) return base
        var suffix = 2
        while (aiEngineCatalogManager.getEngineByName("$base-$suffix") != null) {
            suffix++
        }
        return "$base-$suffix"
    }

    private fun AiEngine.withInitialProtocol(initialProtocol: String): AiEngine {
        return when (AiRequestProtocol.fromValue(initialProtocol.takeIf { it.isNotBlank() })) {
            AiRequestProtocol.JEV -> copy(
                requestProtocol = AiRequestProtocol.JEV,
                authType = AuthType.BEARER,
                requestUrl = requestUrl.ifBlank { UrlConstants.TYPESAFE_AI_BASE_URL },
                requestPath = requestPath.ifBlank { UrlConstants.JEV_SYSTEMONE_ENDPOINT },
                proxyUrl = proxyUrl.ifBlank { UrlConstants.RELEASE_URL },
                proxyPath = proxyPath.ifBlank { UrlConstants.JEV_PROXY_PATH },
            )
            AiRequestProtocol.PIKAFISH -> copy(
                requestProtocol = AiRequestProtocol.PIKAFISH,
                authType = AuthType.NONE,
                requestUrl = "",
                requestPath = "",
                proxyUrl = proxyUrl.ifBlank { UrlConstants.RELEASE_URL },
                proxyPath = proxyPath.ifBlank { UrlConstants.XIANGQI_ENGINE_PROXY_PATH },
            )
            else -> this
        }
    }

    private companion object {
        const val DEFAULT_IMPORTED_NAME = "imported-engine"
        const val MIN_TOKEN_LENGTH = 8
        const val ANTHROPIC_KEY_PREFIX = "sk-ant-"
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            @Assisted("initialProtocol") initialProtocol: String,
            @Assisted("importConfig") importConfig: String,
            onGoBack: () -> Unit,
        ): AIAddEngineComponent
    }
}

/** 配置导入结果: 空剪贴板与解析失败分开提示; Invalid 由页面提示"不是有效的引擎配置" */
sealed interface EngineImportResult {
    data object EmptyClipboard : EngineImportResult
    data object Invalid : EngineImportResult
    data class Success(val displayName: String, val renamed: Boolean) : EngineImportResult
}
