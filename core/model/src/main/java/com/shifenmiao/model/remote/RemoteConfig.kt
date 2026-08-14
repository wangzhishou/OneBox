package com.shifenmiao.model.remote

import android.os.Parcelable
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.Constants
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.BuildConfig
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.common.Meta
import com.shifenmiao.model.webview.WebResourceRuleDto
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.util.Objects

@Parcelize
@Serializable
data class RemoteConfig(
    /**
     * 当前版本号
     */
    val version: Int? = 0,
    /**
     * 是否网络缓存
     */
    val networkCache: Boolean? = true,
    /**
     * 是否显示Map
     * key 是 screen 的 id
     * value 是 布尔值
     */
    /**
     * 在页面打开的时候兼听则明, 如果用户没有绑定手机号,提示绑定手机号
     */
    val forceBindPhone: Boolean? = false,

    /**
     * 微信登录以后是否绑定手机号
     */
    val needWechatBindPhone: Boolean? = false,

    /**
     * 登录的时候绑定手机号
     */
    val loginBindPhone: Boolean? = ShowBindPhoneConfig.getConfigByFlavor().isShow,

    /**
     * 管理员权限对应的最低 VIP 等级。
     *
     * 该字段故意默认 `null`：
     * - 服务端未下发时，由消费端回退到本地默认值（当前为 10）；
     * - 避免因为 data class 默认值参与反序列化，导致“未下发”被误判为“显式下发默认值”。
     */
    val adminVipLevel: Int? = null,

    /**
     * AI 图像处理（百度图像处理，如 markup-layers「AI 处理」）单次消耗积分。
     *
     * 与 [adminVipLevel] 同款意图：默认 `null` 表示服务端未下发，
     * 由消费端回退到本地默认值（当前为 200），避免“未下发”被误判为“显式下发默认值”。
     */
    val aiImageProcessPoints: Int? = null,

    /**
     * 网络请求访问的token。
     * 按渠道隔离：国内渠道与 google 渠道使用不同的默认 token，
     * 见 core/r 中 src/domestic / src/google 的 UrlConstantsFlavor。
     */
    val accessToken: String? = UrlConstants.REMOTE_CONFIG_ACCESS_TOKEN,

    /**
     * 请求网络的URL
     */
    val requestUrl: String? = null,

    /**
     * AI Agent 更新间隔
     */
    val aiAgentUpdateInterval: Int? = Constants.AI_AGENT_UPDATE_INTERVAL,

    /**
     * AI内容提示
     */
    val aiNotice: String? = AppContext.getString(R.string.ai_content_notice),
    val defaultEngines: List<String>? = AiEngineConfig.getFlavorFallbackEngines(FlavorType.fromName()),
    val aiCanSetting: Boolean? = AiEngineConfig.getCanAISetting(FlavorType.fromName()),

    /**
     * AI 聊天快速开始文案，支持远程下发。
     * 可以配置多条，前端每次随机展示其中 4 条。
     */
    val chatQuickStartPrompts: List<String>? = defaultChatQuickStartPrompts(),

    /**
     *  网络超时时间, 单位分钟
     */
    val timeOut: Long? = 3L,

    /**
     * 提示词催眠,有些AI胡咧咧,政治不正确, 用于紧急狗头保命
     */
    val aiPromptSuffix: String? = AppContext.getString(R.string.ai_prompt_suffix),

    /**
     * 缓存时间
     * 单位秒
     */
    val cacheTimeout: Int? = 24 * 60 * 60,

    /**
     * 手动下拉刷新冷却时间。
     * 单位秒，默认 30 秒；服务端可动态调整。
     */
    val manualRefreshCooldown: Int? = 30,

    /**
     * App 启动全量同步间隔。
     * 单位秒，默认 3 天；服务端可动态调整。
     * 配合 lastFullSyncAt 水位线控制启动时的全量同步频率。
     */
    val appLaunchSyncIntervalSeconds: Int? = 3 * 24 * 60 * 60,

    /**
     * 进入列表页增量同步的冷却时间。
     * 单位秒，默认 1 天，按 listType 维度持久化（杀进程不重置）；
     * 内容更新频率低时可在服务端调大，无需发版。
     */
    val pageEnterSyncIntervalSeconds: Int? = 24 * 60 * 60,

    /**
     * 博客列表
     */
    val helpBlogIds: Map<String, Int>? = mapOf(
        "AISetting" to 7
    ),

    /**
     * 默认工作引擎名称（DEFAULT 槽位）
     */
    val defaultWorkingEngine: String? = AiEngineConfig.getDefaultWorkingEngine(),

    /**
     * 默认工作模型名称（DEFAULT 槽位），null 表示使用该引擎第一个可用模型
     */
    val defaultWorkingModel: String? = AiEngineConfig.getDefaultWorkingModel(),

    /**
     * 默认快速任务引擎名称（FAST 槽位）
     */
    val defaultFastEngine: String? = AiEngineConfig.getDefaultFastEngine(),

    /**
     * 默认快速任务模型名称（FAST 槽位），null 表示使用该引擎第一个可用模型
     */
    val defaultFastModel: String? = AiEngineConfig.getDefaultFastModel(),

    /**
     * 默认 AI 互聊引擎 A 名称（DUEL_A 槽位）
     */
    val defaultDuelAEngine: String? = AiEngineConfig.getDefaultDuelAEngine(),

    /**
     * 默认 AI 互聊模型 A 名称（DUEL_A 槽位），null 表示使用该引擎第一个可用模型
     */
    val defaultDuelAModel: String? = AiEngineConfig.getDefaultDuelAModel(),

    /**
     * 默认 AI 互聊引擎 B 名称（DUEL_B 槽位）
     */
    val defaultDuelBEngine: String? = AiEngineConfig.getDefaultDuelBEngine(),

    /**
     * 默认 AI 互聊模型 B 名称（DUEL_B 槽位），null 表示使用该引擎第一个可用模型
     */
    val defaultDuelBModel: String? = AiEngineConfig.getDefaultDuelBModel(),

    /**
     * WebView 资源拦截规则。
     *
     * 由 feature:webview 的 [com.shifenmiao.webview.resource.WebResourceEngine] 消费；
     * 与代码内硬编码的内置规则（tailwindcss 等）一起构成完整的资源加载策略。
     * `null` 或空列表表示服务端未下发任何规则，仅使用内置规则。
     *
     * 完整字段说明 + JSON 示例见 [WebResourceRuleDto]。
     *
     * 服务端最小下发示例（仅一条 host → asset 规则）：
     * ```json
     * {
     *   "webViewResourceRules": [
     *     {
     *       "matchKind": "host",
     *       "matchValue": "cdn.tailwindcss.com",
     *       "ruleKind": "asset",
     *       "assetPath": "js/tailwindcss.js"
     *     }
     *   ]
     * }
     * ```
     */
    val webViewResourceRules: List<WebResourceRuleDto>? = null,

    /**
     * 保存 Prompt（新增/编辑）前是否进行敏感词校验
     */
    val enablePromptSensitiveCheck: Boolean? = PromptSensitiveCheckConfig.getConfigByFlavor().isEnabled,

    /**
     * 保存 Agent（新增/编辑）前是否进行敏感词校验
     */
    val enableAgentSensitiveCheck: Boolean? = AgentSensitiveCheckConfig.getConfigByFlavor().isEnabled,

    /**
     * 微信群二维码图片URL，动态下发
     */
    val wechatGroupQrcodeUrl: String? = null,
    /**
     * 设置ai token的权限,动态下发
     */
    val canSetAiToken: Boolean? = false,

    /**
     * AIGC 隐式标识中使用的主体统一社会信用代码。
     *
     * 远端可动态下发，用于在不发版的情况下更换服务提供者主体信息。
     * 应为 18 位统一社会信用代码；不合规时消费端回退到本地默认值。
     */
    val aigcSubjectUscc: String? = "91110114MACY9KQ17M",

    /**
     * 躲避 30 秒小游戏通关奖励积分。
     *
     * 远端可动态调整，用于运营活动或 AB 实验；未下发时默认 300 分。
     */
    val survive30sWinPoints: Int? = 300,

    /**
     * 祈福墙各 tab 文案，远程下发。
     *
     * 出于合规（宗教类文案）考虑，APK 内置仅为中性兜底文案；
     * `null`、空列表或缺少对应 type 的条目时，消费端回退到本地兜底文案。
     * 字段说明与 JSON 示例见 [BlessingWallTabText]。
     */
    val blessingWallTabTexts: List<BlessingWallTabText>? = null
) : Parcelable {

    /**
     * Merge remote config coming from network as a *patch* on top of current config.
     *
     * Contract — for each field, override current value **iff** the network side
     * has a configured value AND that value differs from the current one:
     * - "Configured" = non-null (and non-blank for `String`).
     * - "Differs"    = `netValue != currentValue` via `equals`.
     *
     * Otherwise the current value is preserved. This avoids two failure modes:
     *  1. Server omits a key (serialized as `null`): without this, naively
     *     overwriting the whole local config would drop locally-cached values
     *     for those fields (e.g. `requestUrl` reset to `null`, AI engine
     *     defaults wiped, etc.).
     *  2. Server echoes the same value back: without the equality check, every
     *     poll would trigger a redundant MMKV write for nothing.
     *
     * Known limitation:
     * For fields with a **non-null data class default** (e.g. `version: Int? = 0`,
     * `networkCache: Boolean? = true`), kotlinx-serialization applies the
     * default when the key is absent from the server payload. That makes
     * "server omitted the key" indistinguishable from "server explicitly sent
     * the default value", so an absent field can still overwrite a divergent
     * local value with the data class default. To eliminate this, change all
     * field defaults to `null` and provide fallbacks at consumer sites.
     */
    fun mergeWithNetwork(net: RemoteConfig): RemoteConfig = copy(
        version = mergeField(net.version, version),
        networkCache = mergeField(net.networkCache, networkCache),
        forceBindPhone = mergeField(net.forceBindPhone, forceBindPhone),
        needWechatBindPhone = mergeField(net.needWechatBindPhone, needWechatBindPhone),
        loginBindPhone = mergeField(net.loginBindPhone, loginBindPhone),
        adminVipLevel = mergeField(net.adminVipLevel, adminVipLevel),
        aiImageProcessPoints = mergeField(net.aiImageProcessPoints, aiImageProcessPoints),
        accessToken = mergeField(net.accessToken, accessToken) { !it.isNullOrBlank() },
        requestUrl = mergeField(net.requestUrl, requestUrl) { !it.isNullOrBlank() },
        aiAgentUpdateInterval = mergeField(net.aiAgentUpdateInterval, aiAgentUpdateInterval),
        aiNotice = mergeField(net.aiNotice, aiNotice),
        defaultEngines = mergeField(net.defaultEngines, defaultEngines),
        aiCanSetting = mergeField(net.aiCanSetting, aiCanSetting),
        chatQuickStartPrompts = mergeField(net.chatQuickStartPrompts, chatQuickStartPrompts),
        timeOut = mergeField(net.timeOut, timeOut),
        aiPromptSuffix = mergeField(net.aiPromptSuffix, aiPromptSuffix),
        cacheTimeout = mergeField(net.cacheTimeout, cacheTimeout),
        manualRefreshCooldown = mergeField(net.manualRefreshCooldown, manualRefreshCooldown),
        appLaunchSyncIntervalSeconds = mergeField(net.appLaunchSyncIntervalSeconds, appLaunchSyncIntervalSeconds),
        pageEnterSyncIntervalSeconds = mergeField(net.pageEnterSyncIntervalSeconds, pageEnterSyncIntervalSeconds),
        helpBlogIds = mergeField(net.helpBlogIds, helpBlogIds),
        defaultWorkingEngine = mergeField(net.defaultWorkingEngine, defaultWorkingEngine) { !it.isNullOrBlank() },
        defaultWorkingModel = mergeField(net.defaultWorkingModel, defaultWorkingModel) { !it.isNullOrBlank() },
        defaultFastEngine = mergeField(net.defaultFastEngine, defaultFastEngine) { !it.isNullOrBlank() },
        defaultFastModel = mergeField(net.defaultFastModel, defaultFastModel) { !it.isNullOrBlank() },
        defaultDuelAEngine = mergeField(net.defaultDuelAEngine, defaultDuelAEngine) { !it.isNullOrBlank() },
        defaultDuelAModel = mergeField(net.defaultDuelAModel, defaultDuelAModel) { !it.isNullOrBlank() },
        defaultDuelBEngine = mergeField(net.defaultDuelBEngine, defaultDuelBEngine) { !it.isNullOrBlank() },
        defaultDuelBModel = mergeField(net.defaultDuelBModel, defaultDuelBModel) { !it.isNullOrBlank() },
        webViewResourceRules = mergeField(net.webViewResourceRules, webViewResourceRules),
        enablePromptSensitiveCheck = mergeField(net.enablePromptSensitiveCheck, enablePromptSensitiveCheck),
        enableAgentSensitiveCheck = mergeField(net.enableAgentSensitiveCheck, enableAgentSensitiveCheck),
        wechatGroupQrcodeUrl = mergeField(net.wechatGroupQrcodeUrl, wechatGroupQrcodeUrl) { !it.isNullOrBlank() },
        canSetAiToken = mergeField(net.canSetAiToken, canSetAiToken),
        aigcSubjectUscc = mergeField(net.aigcSubjectUscc, aigcSubjectUscc) { !it.isNullOrBlank() },
        survive30sWinPoints = mergeField(net.survive30sWinPoints, survive30sWinPoints),
        blessingWallTabTexts = mergeField(net.blessingWallTabTexts, blessingWallTabTexts)
    )

    /**
     * Patch a single field: return [netValue] if it is [isConfigured] AND differs
     * from [currentValue], otherwise keep [currentValue]. The `isConfigured`
     * predicate defaults to "non-null"; pass [String::isNotBlank] for strings
     * where empty / whitespace should be treated as "not set".
     */
    private fun <T> mergeField(
        netValue: T?,
        currentValue: T?,
        isConfigured: (T?) -> Boolean = { it != null }
    ): T? = if (isConfigured(netValue) && netValue != currentValue) netValue else currentValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RemoteConfig) return false

        return version == other.version &&
                accessToken == other.accessToken &&
                requestUrl == other.requestUrl &&
                aiAgentUpdateInterval == other.aiAgentUpdateInterval &&
                aiNotice == other.aiNotice &&
                defaultEngines == other.defaultEngines &&
                aiCanSetting == other.aiCanSetting &&
                chatQuickStartPrompts == other.chatQuickStartPrompts &&
                timeOut == other.timeOut &&
                aiPromptSuffix == other.aiPromptSuffix &&
                cacheTimeout == other.cacheTimeout &&
                manualRefreshCooldown == other.manualRefreshCooldown &&
                appLaunchSyncIntervalSeconds == other.appLaunchSyncIntervalSeconds &&
                pageEnterSyncIntervalSeconds == other.pageEnterSyncIntervalSeconds &&
                networkCache == other.networkCache &&
                forceBindPhone == other.forceBindPhone &&
                needWechatBindPhone == other.needWechatBindPhone &&
                loginBindPhone == other.loginBindPhone &&
                adminVipLevel == other.adminVipLevel &&
                aiImageProcessPoints == other.aiImageProcessPoints &&
                helpBlogIds == other.helpBlogIds &&
                webViewResourceRules == other.webViewResourceRules &&
                defaultWorkingEngine == other.defaultWorkingEngine &&
                defaultWorkingModel == other.defaultWorkingModel &&
                defaultFastEngine == other.defaultFastEngine &&
                defaultFastModel == other.defaultFastModel &&
                defaultDuelAEngine == other.defaultDuelAEngine &&
                defaultDuelAModel == other.defaultDuelAModel &&
                defaultDuelBEngine == other.defaultDuelBEngine &&
                defaultDuelBModel == other.defaultDuelBModel &&
                enablePromptSensitiveCheck == other.enablePromptSensitiveCheck &&
                enableAgentSensitiveCheck == other.enableAgentSensitiveCheck &&
                wechatGroupQrcodeUrl == other.wechatGroupQrcodeUrl &&
                canSetAiToken == other.canSetAiToken &&
                aigcSubjectUscc == other.aigcSubjectUscc &&
                survive30sWinPoints == other.survive30sWinPoints &&
                blessingWallTabTexts == other.blessingWallTabTexts
    }

    override fun hashCode(): Int {
        return Objects.hash(
            version,
            accessToken,
            requestUrl,
            aiAgentUpdateInterval,
            aiNotice,
            defaultEngines,
            aiCanSetting,
            chatQuickStartPrompts,
            timeOut,
            aiPromptSuffix,
            cacheTimeout,
            manualRefreshCooldown,
            appLaunchSyncIntervalSeconds,
            pageEnterSyncIntervalSeconds,
            networkCache,
            forceBindPhone,
            needWechatBindPhone,
            loginBindPhone,
            adminVipLevel,
            aiImageProcessPoints,
            helpBlogIds,
            webViewResourceRules,
            defaultWorkingEngine,
            defaultWorkingModel,
            defaultFastEngine,
            defaultFastModel,
            defaultDuelAEngine,
            defaultDuelAModel,
            defaultDuelBEngine,
            defaultDuelBModel,
            enablePromptSensitiveCheck,
            enableAgentSensitiveCheck,
            wechatGroupQrcodeUrl,
            canSetAiToken,
            aigcSubjectUscc,
            survive30sWinPoints,
            blessingWallTabTexts
        )
    }
}

@Parcelize
@Serializable
data class DataConfig(
    val id: Int,
    val version: Int = 0,
    val versionCode: Int = BuildConfig.VersionCode.toInt(),
    val config: RemoteConfig = RemoteConfig()
) : Parcelable

@Serializable
@Parcelize
data class RemoteConfigListResponse(
    val data: List<DataConfig>,
    val meta: Meta
) : Parcelable

private fun defaultChatQuickStartPrompts(): List<String> = listOf(
    AppContext.getString(R.string.ai_chat_quick_start_1),
    AppContext.getString(R.string.ai_chat_quick_start_2),
    AppContext.getString(R.string.ai_chat_quick_start_3),
    AppContext.getString(R.string.ai_chat_quick_start_4),
    AppContext.getString(R.string.ai_chat_quick_start_5),
    AppContext.getString(R.string.ai_chat_quick_start_6),
    AppContext.getString(R.string.ai_chat_quick_start_7),
    AppContext.getString(R.string.ai_chat_quick_start_8),
    AppContext.getString(R.string.ai_chat_quick_start_9),
    AppContext.getString(R.string.ai_chat_quick_start_10),
    AppContext.getString(R.string.ai_chat_quick_start_11),
    AppContext.getString(R.string.ai_chat_quick_start_12),
)

