package com.shifenmiao.model.remote

import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiModelSlotDefaults
import com.shifenmiao.model.ai.AiWorkingModelSlot
import com.shifenmiao.model.channel.FlavorType

object AiEngineConfig {

    /**
     * flavor 预制引擎版本号: 每次向 [getFlavorFallbackEngines] 新增/变更预制引擎时 +1,
     * 客户端据此对老安装做增量补插(见 AIEngineRepository.ensureFlavorPresetEngines)。
     *
     * v2: Google 渠道清理老安装预制引擎上误带的 Go 网关代理(服务端种子即为空代理)。
     * v3: Google 渠道预制 MiMo 并保留 Go 网关代理(走自家网关按积分计费, 见 AIEngineRepository)。
     * v4: Google 渠道 DeepSeek 也改走 Go 网关代理; 老安装的 REMOTE 行恢复代理路由,
     *     仅清空仍为内置注入值的 token(用户自配 token 的行不动)。
     */
    const val FLAVOR_PRESET_VERSION = 4

    /** 引入"清理 Google 渠道预制引擎代理"迁移的版本号 */
    const val PRESET_VERSION_CLEAR_GOOGLE_PROXY = 2

    /** 引入"Google 渠道 MiMo/DeepSeek 恢复 Go 网关代理"迁移的版本号 */
    const val PRESET_VERSION_GOOGLE_PROXY_ENGINES = 4

    /**
     * Google 渠道保留 Go 网关代理(按积分计费、需登录)的引擎名;
     * 不在此列表的 google 预制引擎一律清空代理, 用户自带 token 直连。
     */
    val googleProxyEngines: List<String>
        get() = listOf(
            AiProvider.Mimo.value,
            AiProvider.DeepSeek.value,
        )

    val defaultEnabledEngines: List<String>
        get() = listOf(
            AiProvider.DouBao.value,
            AiProvider.DeepSeek.value,
            AiProvider.QWen.value,
            AiProvider.Tencent.value,
            AiProvider.Kimi.value,
            AiProvider.Baidu.value,
            AiProvider.Mimo.value,
        )

    val oneboxEnabledEngines: List<String>
        get() = defaultEnabledEngines + AiProvider.OpenAi.value

    /**
     * Google Play 渠道: 预制主流海外 OpenAI 兼容引擎, 用户自带 token 直连.
     * 国内大模型 (豆包/Kimi/腾讯) 在海外无法访问, 不展示.
     * MiMo 为默认引擎, 走 Go 网关代理(按积分计费), 故放首位且保留代理路由。
     */
    val googleEnabledEngines: List<String>
        get() = listOf(
            AiProvider.Mimo.value,
            AiProvider.OpenAi.value,
            AiProvider.Gemini.value,
            AiProvider.Grok.value,
            AiProvider.DeepSeek.value,
            AiProvider.Claude.value,
            AiProvider.QWen.value,
            AiProvider.OpenRouter.value,
        )

    fun getFlavorFallbackEngines(flavorType: FlavorType): List<String> {
        return when (flavorType) {

            FlavorType.ONEBOX -> oneboxEnabledEngines

            FlavorType.XIAOMI,
            FlavorType.YYB,
            FlavorType.OPPO,
            FlavorType.VIVO,
            FlavorType.HUAWEI -> defaultEnabledEngines

            // GOOGLE / FOSS 及未显式映射的新渠道: 回退到海外可用的最小引擎集,
            // 避免新增海外渠道漏配时静默拿到海外不可访问的国内引擎
            else -> googleEnabledEngines
        }
    }

    private fun resolveConfiguredEngineNames(
        configuredEngines: List<String>?,
        flavorType: FlavorType = FlavorType.fromName(),
    ): List<String> {
        val normalizedConfigured = configuredEngines.orEmpty()
            .mapNotNull { it?.trim()?.takeIf(String::isNotBlank) }
            .distinct()
        return normalizedConfigured.ifEmpty { getFlavorFallbackEngines(flavorType) }
    }

    fun getDefaultEngines(
        configuredEngines: List<String>? = null,
        flavorType: FlavorType = FlavorType.fromName(),
    ): List<String> {
        return resolveConfiguredEngineNames(
            configuredEngines = configuredEngines,
            flavorType = flavorType,
        )
    }

    fun getDefaultModels(
        configuredEngines: List<String>? = null,
        flavorType: FlavorType = FlavorType.fromName(),
    ): List<AiModel> {
        return getDefaultEngines(
            configuredEngines = configuredEngines,
            flavorType = flavorType,
        ).mapNotNull { engineName ->
            AiProvider.fromValue(engineName)
                .takeUnless { it == AiProvider.Default }
                ?.let { provider ->
                    AiModel.getDefaultModelForProvider(provider).copy(
                        engineName = engineName,
                    )
                }
        }
    }

    fun getDefaultWorkingEngine(flavorType: FlavorType = FlavorType.fromName()): String {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.DEFAULT,
            flavorType = flavorType,
        ).engineName
    }

    fun getDefaultWorkingModel(flavorType: FlavorType = FlavorType.fromName()): String? {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.DEFAULT,
            flavorType = flavorType,
        ).modelName
    }

    fun getDefaultFastEngine(flavorType: FlavorType = FlavorType.fromName()): String {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.FAST,
            flavorType = flavorType,
        ).engineName
    }

    fun getDefaultFastModel(flavorType: FlavorType = FlavorType.fromName()): String? {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.FAST,
            flavorType = flavorType,
        ).modelName
    }

    fun getDefaultDuelAEngine(flavorType: FlavorType = FlavorType.fromName()): String {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.DUEL_A,
            flavorType = flavorType,
        ).engineName
    }

    fun getDefaultDuelAModel(flavorType: FlavorType = FlavorType.fromName()): String? {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.DUEL_A,
            flavorType = flavorType,
        ).modelName
    }

    fun getDefaultDuelBEngine(flavorType: FlavorType = FlavorType.fromName()): String {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.DUEL_B,
            flavorType = flavorType,
        ).engineName
    }

    fun getDefaultDuelBModel(flavorType: FlavorType = FlavorType.fromName()): String? {
        return getDefaultSlotConfig(
            slot = AiWorkingModelSlot.DUEL_B,
            flavorType = flavorType,
        ).modelName
    }

    fun getDefaultSlotConfig(
        slot: AiWorkingModelSlot,
        configuredEngines: List<String>? = null,
        flavorType: FlavorType = FlavorType.fromName(),
    ): com.shifenmiao.model.ai.AiModelSlotDefault {
        val configured = AiModelSlotDefaults.get(slot)
        val enabledEngines = getDefaultEngines(
            configuredEngines = configuredEngines,
            flavorType = flavorType,
        )
        if (enabledEngines.isEmpty() || enabledEngines.contains(configured.engineName)) {
            return configured
        }

        val fallbackEngineName = enabledEngines.first()
        val fallbackProvider = AiProvider.fromValue(fallbackEngineName)
        return com.shifenmiao.model.ai.AiModelSlotDefault(
            engineName = fallbackEngineName,
            modelName = AiModel.getDefaultModelForProvider(fallbackProvider).name,
        )
    }

    fun getCanAISetting(flavorType: FlavorType): Boolean {
        return when (flavorType) {
            FlavorType.ONEBOX -> true
            FlavorType.HUAWEI -> true
            FlavorType.OPPO -> true
            FlavorType.XIAOMI -> true
            FlavorType.VIVO -> true
            FlavorType.YYB -> true
            FlavorType.GOOGLE -> true
            FlavorType.FOSS -> true
            else -> false
        }
    }

    /**
     * 分渠道 AI 引擎能力配置: 声明式控制各渠道普通用户的可操作范围.
     *
     * UI 判定规则统一为 `capabilities.xxx || 国内原有判定(管理员/高等级/远程开关)`,
     * 因此 false 不会影响国内渠道现状; 新增海外渠道时只需在此加一行映射.
     */
    fun getCapabilities(flavorType: FlavorType = FlavorType.fromName()): AiChannelCapabilities {
        return when (flavorType) {
            // 海外渠道(google / foss): 无国内账号体系限制, 用户自带 token, 全部放开
            FlavorType.GOOGLE, FlavorType.FOSS -> AiChannelCapabilities(
                canAddEngine = true,
                canEditToken = true,
                canEditUrl = true,
                canLoadRemoteModels = true,
            )

            else -> AiChannelCapabilities()
        }
    }
}

/**
 * 渠道级 AI 引擎能力开关, 全部为 false 时退化为国内渠道现有的 管理员/vip 判定.
 */
data class AiChannelCapabilities(
    /** 列表页"新增引擎"入口 */
    val canAddEngine: Boolean = false,
    /** 详情页服务器/Token 卡片可见 */
    val canEditToken: Boolean = false,
    /** 详情页 URL/Path 可编辑 */
    val canEditUrl: Boolean = false,
    /** 从服务商拉取远程模型列表 */
    val canLoadRemoteModels: Boolean = false,
)