package com.shifenmiao.common.ai

import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import java.util.Locale

/**
 * 后台单轮 AI 任务的「用户语言」指令。
 *
 * 会话聊天会经 `AiUtils.buildLlmMessages` 把用户语言拼进系统提示词，但走
 * [AIPromptExecutor] 的后台任务（会话标题摘要、内容生成等）不带会话上下文：
 * 一旦提示词正文写死中文，模型就会永远输出中文，外语界面下表现为
 * 「对话是土耳其语、AI 历史列表的标题却是中文」。
 *
 * 所以任何会产出用户可见文本的后台提示词，都应拼上这里的方法：
 * - 提示词正文尽量用英文书写，中文指令本身也会把模型往中文上带；
 * - [outputInCurrentLanguage] 追加在业务提示词末尾，[systemDirective] 与会话聊天
 *   现有的措辞保持一致，便于统一维护。
 */
object AiLanguagePrompt {

    /** 当前应用语言的英文名（如 Turkish / Chinese）；ICU 数据缺失时回退 BCP-47 tag。 */
    fun currentLanguageName(): String {
        val tag = LocaleUtils.getCurrentLocaleTag()
        return runCatching { Locale.forLanguageTag(tag).getDisplayLanguage(Locale.ENGLISH) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: tag
    }

    /** 当前应用语言 tag（如 tr / zh-CN），与请求 locale 参数同源。 */
    fun currentLanguageTag(): String = LocaleUtils.getCurrentLocaleTag()

    /**
     * 会话级系统提示词的语言指令，直接拼接在系统提示词尾部（自带两个换行分隔）。
     * 发送时拼装、不落库，对历史会话同样生效。
     */
    fun systemDirective(): String = "\n\n[User context] The user's system language is " +
        "${currentLanguageName()} (${currentLanguageTag()}). " +
        "Respond in that language unless the user explicitly asks for another language."

    /**
     * 单轮后台任务的语言指令，追加在业务提示词末尾。
     *
     * @param subject 需要跟随用户语言的对象，如 "the title"、"all user-visible text"
     */
    fun outputInCurrentLanguage(subject: String): String =
        "[Language] Write $subject in ${currentLanguageName()} (${currentLanguageTag()}), " +
            "regardless of the language used in the conversation above."
}
