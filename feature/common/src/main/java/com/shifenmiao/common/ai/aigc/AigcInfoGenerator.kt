package com.shifenmiao.common.ai.aigc

import com.google.gson.Gson
import com.shifenmiao.common.ai.aigc.AigcServiceProviderCode.generatorCode
import com.shifenmiao.common.ai.aigc.AigcServiceProviderCode.propagatorCode
import com.shifenmiao.common.ai.aigc.AigcServiceProviderCode.resolveModelCode
import com.shifenmiao.common.ai.aigc.AigcServiceProviderCode.resolveSubjectUscc
import com.shifenmiao.model.ai.AIGCInfo
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.storage.TokenStorage
import com.t8rin.imagetoolbox.core.ui.utils.helper.DeviceInfo
import java.security.MessageDigest
import java.util.UUID

/**
 * AIGC 隐式标识信息生成器。
 *
 * 依据 GB/T 44284-2024《网络安全技术 人工智能生成合成内容标识方法》的
 * 七要素格式生成 AIGCInfo，用于向图片/PDF/HTML 等导出介质注入元数据。
 *
 * 服务提供者编码额外遵循 TC260-PG-20252A《人工智能生成合成内容标识服务提供者编码规则》：
 * - ContentProducer 使用 27 位生成合成服务提供者编码（编码 A）；
 * - ContentPropagator 使用 27 位内容传播服务提供者编码（编码 B）；
 * - 编码中的 4 位模型/应用码保证可反向追溯到具体引擎与模型。
 *
 * 生成规则核心原则：
 * - Label 固定为 "1"：导出内容属于人工智能生成合成内容；
 * - ContentProducer / ProduceID 描述「生成合成服务提供者」（即大模型引擎侧）；
 * - ContentPropagator / PropagateID 描述「内容传播服务提供者」（即本 App）；
 * - ReservedCode* 用于安全追溯，包含引擎、模型、用户指纹等敏感但不可逆的信息。
 */
object AigcInfoGenerator {

    private val gson = Gson()

    /**
     * 生成符合 GB 45438-2025 / GB/T 44284-2024 标准的 AIGC 隐式标识 JSON 字符串。
     *
     * 输出为 7 要素 JSON 对象,作为"字段名带 AIGC 的 JSON 对象"中 **AIGC 字段的值** 使用:
     * ```
     * {"Label":"...","ContentProducer":"...","ProduceID":"...","ReservedCode1":"...","ContentPropagator":"...","PropagateID":"...","ReservedCode2":"..."}
     * ```
     *
     * 调用方需要把这个字符串写入文件元数据时,使用 key = `AIGC`(PNG / JPEG XMP / iTXt、PDF /Info.AIGC、HTML `<meta name="AIGC">` 等)。
     * **不要**在 JSON 外面再包一层 `{"AIGC": ...}`。
     *
     * @param engine 实际执行生成合成的大模型引擎（对应 ContentProducer）
     * @param model 实际生成该条内容的模型（用于安全校验预留字段）
     * @param completionId 大模型服务提供者返回的内容唯一编号（如 OpenAI/DouBao 的 completion id）
     * @param conversationId 会话唯一编号，用于 ProduceID 增强溯源；为空时降级为时间戳
     * @param contentId App 侧可读的内容唯一编号（如消息 ID），用于 ProduceID
     * @param entryTypeName 导出内容的业务场景类型，用于传播侧追溯
     * @param entryRefId 导出内容的业务场景 ID，用于传播侧追溯
     */
    fun generateJson(
        engine: AiEngine,
        model: AiModel,
        completionId: String,
        conversationId: String?,
        contentId: String,
        entryTypeName: String,
        entryRefId: String?,
    ): String {
        return gson.toJson(
            generate(
                engine = engine,
                model = model,
                completionId = completionId,
                conversationId = conversationId,
                contentId = contentId,
                entryTypeName = entryTypeName,
                entryRefId = entryRefId,
            )
        )
    }

    /**
     * 生成 [AIGCInfo]。
     *
     * @param engine 实际执行生成合成的大模型引擎
     * @param model 实际生成该条内容的模型
     * @param completionId 服务提供者侧内容唯一编号
     * @param conversationId 会话唯一编号，用于 ProduceID 增强溯源；为空时降级为时间戳
     * @param contentId App 侧可读的内容唯一编号
     * @param entryTypeName 业务场景类型
     * @param entryRefId 业务场景 ID
     */
    fun generate(
        engine: AiEngine,
        model: AiModel,
        completionId: String,
        conversationId: String?,
        contentId: String,
        entryTypeName: String,
        entryRefId: String?,
    ): AIGCInfo {
        // 本次导出/传播批次在本 App 内的唯一编号。
        // 作为传播服务提供者，需要为每一份导出的介质分配独立编号。
        val exportTimestamp = System.currentTimeMillis()
        val exportRandom = UUID.randomUUID().toString().take(8)

        // 用户指纹：仅用于安全追溯，不直接暴露用户名/手机号等 PII。
        // 已登录用户使用用户 ID，未登录用户使用设备信息哈希。
        val userFingerprint = resolveUserFingerprint()
        // 明文用户 ID，用于 App 侧（Demo/管理后台）快速定位具体用户。
        // 注意：该值会写入导出的文件元数据，仅保留数值 ID，不暴露用户名/手机号。
        val userId = resolveUserId()

        // 主体统一社会信用代码从远端配置读取，支持后台动态更新；不合规时回退本地默认值。
        val subjectUscc = resolveSubjectUscc(
            RemoteConfigStorage.getRemoteConfig().aigcSubjectUscc
        )

        return AIGCInfo(
            // Label：
            // 1 = 属于人工智能生成合成内容；
            // 2 = 可能为人工智能生成合成内容；
            // 3 = 疑似为人工智能生成合成内容。
            // 导出的是大模型直接生成的回复，因此固定为 "1"。
            label = "1",

            // ContentProducer：生成合成服务提供者。
            // 使用符合 TC260-PG-20252A 的 27 位编码 A，其中包含 4 位可溯源模型码。
            // 具体引擎与模型名称保留在 ReservedCode1 中，便于深度追溯。
            contentProducer = generatorCode(
                modelCode = resolveModelCode(engine, model),
                subjectUscc = subjectUscc,
            ),

            // ProduceID：生成合成服务提供者对该内容的唯一编号。
            // 优先组合「会话 ID + 内容 ID」，既可定位到具体会话，也能定位到会话内的某条消息；
            // 缺少会话 ID 时退化为内容 ID，再缺失时降级为时间戳编号。
            produceID = buildProduceID(
                conversationId = conversationId,
                contentId = contentId,
                userId = userId,
                timestamp = exportTimestamp,
                random = exportRandom,
            ),

            // ReservedCode1：生成合成服务提供者用于安全防护、保护内容/标识完整性的信息。
            // 这里用结构化字符串记录引擎、模型、服务提供者 completionId 以及用户指纹哈希，
            // Demo/管理后台可直接解析查看，同时保留不可逆的用户指纹用于安全校验。
            reservedCode1 = buildContentIntegrityInfo(
                engineName = engine.name,
                modelName = model.name,
                completionId = completionId,
                userFingerprint = userFingerprint,
            ),

            // ContentPropagator：内容传播服务提供者。
            // 使用符合 TC260-PG-20252A 的 27 位编码 B，应用包名等细节保留在 ReservedCode2 中。
            contentPropagator = propagatorCode(subjectUscc),

            // PropagateID：内容传播服务提供者对该内容的唯一编号。
            // 使用「u{用户ID}-t{时间戳}-r{随机码}」可读格式，直接包含用户 ID，便于传播侧溯源。
            propagateID = "u${userId}-t${exportTimestamp}-r${exportRandom}",

            // ReservedCode2：内容传播服务提供者用于安全防护、保护内容/标识完整性的信息。
            // 记录业务场景（entryType）、业务 ID（entryRefId）以及明文用户 ID，
            // 用于 App 侧或管理后台在接收到投诉/举报时快速定位具体用户和原始会话。
            reservedCode2 = buildPropagationTrace(entryTypeName, entryRefId, userId),
        )
    }

    /**
     * 解析当前用户指纹。
     *
     * - 已登录：使用用户 ID 的 SHA-256 哈希；
     * - 未登录：使用设备信息的 SHA-256 哈希。
     *
     * 返回十六进制字符串，仅包含 [0-9A-F]，满足 AIGC 隐式标识字符集要求。
     */
    private fun resolveUserFingerprint(): String {
        val user = TokenStorage.getLoginInfo()?.user
        val raw = if (user != null && user.id != 0) {
            "user:${user.id}"
        } else {
            "device:${DeviceInfo.getAsString()}"
        }
        return sha256(raw).toHex()
    }

    /**
     * 解析当前用户 ID。
     *
     * - 已登录：返回用户数字 ID；
     * - 未登录：返回 "0"。
     */
    private fun resolveUserId(): String {
        val user = TokenStorage.getLoginInfo()?.user
        return if (user != null && user.id != 0) user.id.toString() else "0"
    }

    /**
     * 生成内容完整性信息串。
     *
     * 用结构化字符串记录生成合成服务提供者关心的关键参数（引擎、模型、completionId、用户指纹哈希），
     * Demo/管理后台可直接解析查看；其中用户指纹保持 SHA-256 哈希，不暴露原始 PII。
     */
    /**
     * 构造生成合成服务提供者对该内容的唯一编号（ProduceID）。
     *
     * 组合规则：
     * - 有会话 ID + 内容 ID：`{conversationId}-{contentId}`
     * - 仅有会话 ID：`{conversationId}-t{timestamp}`
     * - 无会话 ID 有内容 ID：`{contentId}`
     * - 都没有：`u{userId}-t{timestamp}-r{random}`
     */
    private fun buildProduceID(
        conversationId: String?,
        contentId: String,
        userId: String,
        timestamp: Long,
        random: String,
    ): String {
        val effectiveContentId = contentId.takeIf { it.isNotBlank() && it != "0" }
        return when {
            !conversationId.isNullOrBlank() && effectiveContentId != null ->
                "${conversationId}-${effectiveContentId}"
            !conversationId.isNullOrBlank() ->
                "${conversationId}-t${timestamp}"
            effectiveContentId != null ->
                effectiveContentId
            else ->
                "u${userId}-t${timestamp}-r${random}"
        }
    }

    private fun buildContentIntegrityInfo(
        engineName: String,
        modelName: String,
        completionId: String,
        userFingerprint: String,
    ): String {
        return "engine=${engineName}|model=${modelName}|completion=${completionId}|userHash=${userFingerprint}"
    }

    /**
     * 生成传播侧追溯串。
     *
     * 包含业务场景类型、业务 ID 以及明文用户 ID，
     * 便于 App 在接收到投诉/举报时定位原始会话和具体用户。
     */
    private fun buildPropagationTrace(
        entryTypeName: String,
        entryRefId: String?,
        userId: String,
    ): String {
        return "entry=${entryTypeName}|ref=${entryRefId.orEmpty()}|uid=${userId}"
    }

    private fun sha256(input: String): ByteArray {
        return MessageDigest.getInstance("SHA-256").run {
            digest(input.toByteArray(Charsets.UTF_8))
        }
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
