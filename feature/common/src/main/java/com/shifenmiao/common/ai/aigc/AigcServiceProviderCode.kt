package com.shifenmiao.common.ai.aigc

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider

/**
 * AIGC 服务提供者编码构造器。
 *
 * 依据《网络安全标准实践指南——人工智能生成合成内容标识服务提供者编码规则》
 * （TC260-PG-20252A）生成 27 位服务提供者编码，用于填充 AIGC 隐式标识的
 * [ContentProducer]（生成合成服务提供者）和 [ContentPropagator]（内容传播服务提供者）。
 *
 * 编码结构（共 27 位）：
 * ```
 * 00 + 11 + <18 位统一社会信用代码> + <1 位服务类型码> + <4 位模型/应用码>
 * ```
 * - 第 1~2 位：标识格式定义码，固定为 "00"；
 * - 第 3~4 位：主体类型码（组织为 "1"）+ 绑定方式码（统一社会信用代码绑定为 "1"），即 "11"；
 * - 第 5~22 位：主体编码，采用统一社会信用代码；
 * - 第 23 位：服务类型码，"1" 表示生成合成服务，"2" 表示内容传播服务；
 * - 第 24~27 位：模型/应用码，由服务提供者自行编写。
 *
 * 模型码设计（4 位数字或大写英文字母）：
 * - 第 1 位为厂商家族码，用于快速识别模型所属引擎；
 * - 第 2~4 位取模型 [AiModel.modeId] 的末三位十进制数字（如 modeId=12345 则取 "345"），
 *   这样模型在远端新增/更新时无需改动客户端代码即可稳定溯源；
 * - 当 [AiModel.modeId] 小于等于 0（未下发或无效）时，退化为截取模型名称后 3 位，
 *   并过滤为合规字符（0-9、A-Z）。
 *
 * 主体统一社会信用代码由调用方从 [com.shifenmiao.storage.RemoteConfigStorage] 传入，
 * 支持远端动态更新；传入空/不合规时回退到本地默认值。
 */
object AigcServiceProviderCode {

    /** 主体统一社会信用代码本地默认值。 */
    const val DEFAULT_SUBJECT_USCC = "91110114MACY9KQ17M"

    /** 统一社会信用代码标准长度。 */
    private const val USCC_LENGTH = 18

    /** 标识格式定义码，采用本指南时固定为 "00"。 */
    private const val FORMAT_CODE = "00"

    /** 组织主体类型码（"1"）+ 统一社会信用代码绑定方式码（"1"）。 */
    private const val ORG_TYPE_AND_BINDING = "11"

    /** 服务类型：生成合成服务。 */
    private const val SERVICE_TYPE_GENERATION = "1"

    /** 服务类型：内容传播服务。 */
    private const val SERVICE_TYPE_PROPAGATION = "2"

    /** 内容传播服务提供者的应用码，自行编写，本 App 固定为 "APP1"。 */
    private const val APP_CODE = "APP1"

    /** 模型码长度。 */
    const val MODEL_CODE_LENGTH = 4

    /** 模型码中后三位的长度。 */
    private const val MODEL_SUFFIX_LENGTH = 3

    /**
     * 生成合成服务提供者编码（编码 A）。
     *
     * @param modelCode 4 位模型码。
     * @param subjectUscc 主体统一社会信用代码，默认 [DEFAULT_SUBJECT_USCC]。
     * @return 27 位生成合成服务提供者编码。
     */
    fun generatorCode(
        modelCode: String,
        subjectUscc: String = DEFAULT_SUBJECT_USCC,
    ): String {
        require(modelCode.length == MODEL_CODE_LENGTH) {
            "模型码必须为 $MODEL_CODE_LENGTH 位，实际为 ${modelCode.length} 位：$modelCode"
        }
        return buildCode(
            serviceType = SERVICE_TYPE_GENERATION,
            extension = modelCode,
            subjectUscc = subjectUscc,
        )
    }

    /**
     * 内容传播服务提供者编码（编码 B）。
     *
     * @param subjectUscc 主体统一社会信用代码，默认 [DEFAULT_SUBJECT_USCC]。
     * @return 27 位内容传播服务提供者编码。
     */
    fun propagatorCode(subjectUscc: String = DEFAULT_SUBJECT_USCC): String {
        return buildCode(
            serviceType = SERVICE_TYPE_PROPAGATION,
            extension = APP_CODE,
            subjectUscc = subjectUscc,
        )
    }

    /**
     * 根据引擎和模型解析 4 位模型码。
     *
     * 结构：[厂商家族码 1 位] + [模型标识 3 位]。
     * 模型标识优先使用 [AiModel.modeId] 末三位；modeId 无效时退化为模型名后 3 位。
     */
    fun resolveModelCode(engine: AiEngine, model: AiModel): String {
        val family = providerFamilyCode(engine.name, model.provider)
        val suffix = resolveModelSuffix(model)
        return "$family$suffix"
    }

    /**
     * 校验并清洗主体统一社会信用代码。
     *
     * - 去除首尾空白；
     * - 长度必须为 18 位；
     * - 不合规时回退到 [DEFAULT_SUBJECT_USCC]。
     */
    fun resolveSubjectUscc(value: String?): String {
        val cleaned = value?.trim().orEmpty()
        return if (cleaned.length == USCC_LENGTH) cleaned else DEFAULT_SUBJECT_USCC
    }

    private fun buildCode(
        serviceType: String,
        extension: String,
        subjectUscc: String,
    ): String {
        val validUscc = resolveSubjectUscc(subjectUscc)
        return "$FORMAT_CODE$ORG_TYPE_AND_BINDING$validUscc$serviceType$extension"
    }

    private fun resolveModelSuffix(model: AiModel): String {
        return if (model.modeId > 0) {
            // 取 modeId 末三位，前补零固定为 3 位。
            (model.modeId % 1000).toString().padStart(MODEL_SUFFIX_LENGTH, '0')
        } else {
            // modeId 无效时，截取模型名后 3 位并过滤为合规字符。
            model.name.takeLast(MODEL_SUFFIX_LENGTH)
                .uppercase()
                .filter { it.isDigitOrUpperLetter() }
                .padStart(MODEL_SUFFIX_LENGTH, '0')
        }
    }

    private fun Char.isDigitOrUpperLetter(): Boolean {
        return this in '0'..'9' || this in 'A'..'Z'
    }

    private fun providerFamilyCode(engineName: String, provider: AiProvider): Char {
        val name = engineName.trim().lowercase()
        return when {
            name == AiProvider.OpenAi.value -> 'O'
            name == AiProvider.Kimi.value -> 'K'
            name == AiProvider.QWen.value -> 'Q'
            name == AiProvider.DouBao.value -> 'D'
            name == AiProvider.Tencent.value -> 'T'
            name == AiProvider.DeepSeek.value -> 'S'
            name == AiProvider.Mimo.value -> 'M'
            name == AiProvider.Local.value -> 'L'
            name == AiProvider.Baidu.value -> 'B'
            name.equals(AiProvider.MinMax.value, ignoreCase = true) -> 'X'
            name == AiProvider.ZhiPu.value -> 'Z'
            name == AiProvider.OpenRouter.value -> 'R'
            provider == AiProvider.OpenAi -> 'O'
            provider == AiProvider.Kimi -> 'K'
            provider == AiProvider.QWen -> 'Q'
            provider == AiProvider.DouBao -> 'D'
            provider == AiProvider.Tencent -> 'T'
            provider == AiProvider.DeepSeek -> 'S'
            provider == AiProvider.Mimo -> 'M'
            provider == AiProvider.Local -> 'L'
            provider == AiProvider.Baidu -> 'B'
            provider == AiProvider.MinMax -> 'X'
            provider == AiProvider.ZhiPu -> 'Z'
            provider == AiProvider.OpenRouter -> 'R'
            else -> 'U'
        }
    }
}
