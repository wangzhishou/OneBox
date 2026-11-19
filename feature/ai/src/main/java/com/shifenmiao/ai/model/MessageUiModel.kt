/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.ai.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.error.AiChatError
import com.shifenmiao.ai.error.AiChatErrorFactory
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AttachmentPayloadDto
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.SearchResult
import com.wanbaohe.markdown.splitMarkdownForLazyRendering
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

sealed class MessageUiModel(open val id: String) {
    data class UserBlock(
        override val id: String,
        val text: String,
        val showAvatar: Boolean,
        val attachments: List<AttachmentPayloadDto>?,
    ) : MessageUiModel(id)

    data class UserAttachments(
        override val id: String,
        val attachments: List<AttachmentPayloadDto>,
    ) : MessageUiModel(id)

    data class UserContainerHeader(
        override val id: String,
        val showAvatar: Boolean,
        val modelName: String,
        val modelSubtitle: String = "",
    ) : MessageUiModel(id)

    data class UserContainerFooter(
        override val id: String,
        val messageEntity: MessageEntity,
    ) : MessageUiModel(id)

    data class UserContent(
        override val id: String,
        val source: String,
    ) : MessageUiModel(id)

    data class UserTextContent(
        override val id: String,
        val text: String,
        val isHighlighted: Boolean,
        val isFirst: Boolean,
        val isLast: Boolean,
    ) : MessageUiModel(id)

    data class UserReasoningContent(
        override val id: String,
        val source: String,
        val showCollapseFooter: Boolean = false,
    ) : MessageUiModel(id)

    data class UserReasoningHeader(
        override val id: String,
        val time: Long = 0L,
        val preview: String = "",
        val isStreaming: Boolean = false,
    ) : MessageUiModel(id)

    data class UserSearchResults(
        override val id: String,
        val searchResult: SearchResult,
        val isExpanded: Boolean = false,
    ) : MessageUiModel(id)

    data class UserLoading(override val id: String) : MessageUiModel(id)

    data class UserError(
        override val id: String,
        val errorMessage: String = AppContext.getString(R.string.ai_error),
    ) : MessageUiModel(id)

    data class UserVerticalSpace(
        override val id: String,
        val height: Dp = 4.dp,
    ) : MessageUiModel(id)

    /**
     * 机器人回答正文（Markdown 源码）。
     *
     * 超长回答会被拆成多个 [RobotContent]（见 [addRobotAnswerSection]），这样外层聊天列表
     * （LazyColumn）可以按 Markdown 顶层块虚拟化：进入会话只渲染可见的 1~2 块，而不是把
     * 整篇几千字、十几个代码块和公式一次性排版。普通长度的回答不会被拆，行为与拆分前一致。
     *
     * @property partIndex 分块序号，0 表示文档开头；未拆分的整段回答恒为 0
     * @property partCount 分块总数；1 表示整段回答（未拆分）
     */
    data class RobotContent(
        override val id: String,
        val isStreaming: Boolean,
        val showCursor: Boolean = false,
        val answerSource: String,
        val partIndex: Int = 0,
        val partCount: Int = 1,
    ) : MessageUiModel(id)

    data class RobotReasoningContent(
        override val id: String,
        val isStreaming: Boolean,
        val reasoningSource: String,
        val showCollapseFooter: Boolean = false,
    ) : MessageUiModel(id)

    data class RobotReasoningHeader(
        override val id: String,
        val time: Long = 0L,
        val preview: String = "",
        val isStreaming: Boolean = false,
        val isFirst: Boolean = true,
        val isLast: Boolean = false,
    ) : MessageUiModel(id)

    data class RobotLoading(override val id: String) : MessageUiModel(id)

    data class RobotVerticalSpace(
        override val id: String,
        val height: Dp = 4.dp,
    ) : MessageUiModel(id)

    data class RobotError(
        override val id: String,
        val error: AiChatError,
    ) : MessageUiModel(id)

    data class RobotContainerFooter(
        override val id: String,
        val showTokens: Boolean,
        val messageEntity: MessageEntity,
        val isReportable: Boolean = false,
    ) : MessageUiModel(id)

    data class RobotContainerHeader(
        override val id: String,
        val showAvatar: Boolean,
        val modelName: String,
        val modelSubtitle: String = "",
    ) : MessageUiModel(id)

    data class RobotSearchResults(
        override val id: String,
        val searchResult: SearchResult,
        val isExpanded: Boolean = false,
    ) : MessageUiModel(id)

    data class RobotToolCallHistory(
        override val id: String,
        val toolCallsJson: String,
        val isLive: Boolean = false,
    ) : MessageUiModel(id)

    companion object {
        private const val REASONING_COLLAPSE_FOOTER_MIN_CHARS = 1200

        private fun resolveMessageState(message: MessageEntity): Int {
            if (message.uId != MessageUIState.NORMAL.value) return message.uId
            if (message.role == RoleType.USER.value) return message.uId
            return if (looksLikePersistedError(message)) MessageUIState.ERROR.value else message.uId
        }

        private fun looksLikePersistedError(message: MessageEntity): Boolean {
            if (message.finishReason.startsWith("error:")) return true
            if (!message.expired) return false
            val text = message.answer.trim()
            return (text.startsWith("HTTP ") && text.contains("\nURL:")) ||
                text.startsWith("IO error while") ||
                text.startsWith("Failed to extract error details") ||
                text.startsWith("Connection interrupted before the response completed.") ||
                text.startsWith("连接中断，回答未完成。")
        }

        fun fromMessage(
            message: MessageEntity,
            conversation: Conversation,
            getAIModel: (String) -> String,
            showExpandedReasoning: Boolean = true,
        ): List<MessageUiModel> {
            val effectiveCompletionId = message.completionId.takeIf { it.isNotBlank() }
                ?: "msg_${message.id}"
            return if (message.role == RoleType.USER.value) {
                createUserMessageModels(
                    message = message,
                    conversation = conversation,
                    getAIModel = getAIModel,
                    effectiveCompletionId = effectiveCompletionId,
                    showExpandedReasoning = showExpandedReasoning,
                )
            } else {
                when (resolveMessageState(message)) {
                    MessageUIState.LOADING.value -> {
                        val idPrefix = "${effectiveCompletionId}_thinking"
                        val headerText = buildDuelHeaderText(
                            conversation = conversation,
                            speaker = DuelSpeakerLabel.B,
                            modelName = getAIModel(message.model),
                        )
                        listOf(
                            RobotContainerFooter(
                                id = "${idPrefix}_footer",
                                showTokens = false,
                                messageEntity = message.normalizedFor(conversation.isDuel),
                            ),
                            RobotLoading(id = "${idPrefix}_thinking"),
                            RobotContainerHeader(
                                id = "${idPrefix}_header",
                                showAvatar = conversation.showAvatar,
                                modelName = headerText.title,
                                modelSubtitle = headerText.subtitle,
                            ),
                        )
                    }

                    MessageUIState.ERROR.value -> {
                        val idPrefix = "${effectiveCompletionId}_error"
                        val headerText = buildDuelHeaderText(
                            conversation = conversation,
                            speaker = DuelSpeakerLabel.B,
                            modelName = getAIModel(message.model),
                        )
                        val normalizedMessage = message.normalizedFor(conversation.isDuel)
                        listOf(
                            RobotContainerFooter(
                                id = "${idPrefix}_footer",
                                showTokens = false,
                                messageEntity = normalizedMessage,
                            ),
                            RobotError(
                                id = "${idPrefix}_Error",
                                error = AiChatErrorFactory.fromStoredMessage(
                                    message = normalizedMessage.answer,
                                    meta = normalizedMessage.finishReason,
                                ),
                            ),
                            RobotContainerHeader(
                                id = "${idPrefix}_header",
                                showAvatar = conversation.showAvatar,
                                modelName = headerText.title,
                                modelSubtitle = headerText.subtitle,
                            ),
                        )
                    }

                    else -> createRobotMessageModels(
                        message = message,
                        conversation = conversation,
                        getAIModel = getAIModel,
                        effectiveCompletionId = effectiveCompletionId,
                        showExpandedReasoning = showExpandedReasoning,
                    )
                }
            }
        }

        private fun createUserMessageModels(
            message: MessageEntity,
            conversation: Conversation,
            getAIModel: (String) -> String,
            effectiveCompletionId: String,
            showExpandedReasoning: Boolean,
        ): List<MessageUiModel> {
            val normalizedMessage = message.normalizedFor(conversation.isDuel, user = true)
            val idPrefix = "${effectiveCompletionId}_${RoleType.USER.value}"
            if (!conversation.isDuel && message.question.isNotBlank()) {
                val attachments = if (message.attachmentsJson.isBlank()) {
                    null
                } else {
                    runCatching {
                        AppJson.decodeFromString<List<AttachmentPayloadDto>>(message.attachmentsJson)
                    }.getOrNull()
                }
                return listOf(
                    UserBlock(
                        id = "${idPrefix}_question",
                        showAvatar = conversation.showAvatar,
                        text = message.question,
                        attachments = attachments,
                    )
                )
            }
            return when (message.uId) {
                MessageUIState.LOADING.value -> {
                    val headerText = buildDuelHeaderText(
                        conversation = conversation,
                        speaker = DuelSpeakerLabel.A,
                        modelName = getAIModel(message.model),
                    )
                    listOf(
                        UserContainerFooter(id = "${idPrefix}_footer", messageEntity = normalizedMessage),
                        UserLoading(id = "${idPrefix}_thinking"),
                        UserContainerHeader(
                            id = "${idPrefix}_header",
                            showAvatar = conversation.showAvatar,
                            modelName = headerText.title,
                            modelSubtitle = headerText.subtitle,
                        ),
                    )
                }

                MessageUIState.ERROR.value -> {
                    val headerText = buildDuelHeaderText(
                        conversation = conversation,
                        speaker = DuelSpeakerLabel.A,
                        modelName = getAIModel(message.model),
                    )
                    listOf(
                        UserContainerFooter(id = "${idPrefix}_footer", messageEntity = normalizedMessage),
                        UserError(
                            id = "${idPrefix}_error",
                            errorMessage = normalizedMessage.question.ifBlank {
                                AppContext.getString(R.string.ai_error)
                            },
                        ),
                        UserContainerHeader(
                            id = "${idPrefix}_header",
                            showAvatar = conversation.showAvatar,
                            modelName = headerText.title,
                            modelSubtitle = headerText.subtitle,
                        ),
                    )
                }

                else -> {
                    val result = mutableListOf<MessageUiModel>()
                    val headerText = buildDuelHeaderText(
                        conversation = conversation,
                        speaker = DuelSpeakerLabel.A,
                        modelName = getAIModel(message.model),
                    )
                    result.add(
                        UserContainerFooter(id = "${idPrefix}_footer", messageEntity = normalizedMessage)
                    )
                    addUserAnswerSection(result, normalizedMessage, idPrefix)
                    addUserReasoningSection(result, normalizedMessage, idPrefix, showExpandedReasoning)
                    addUserSearchSection(result, normalizedMessage, idPrefix)
                    if (result.size == 1) {
                        result.add(
                            UserTextContent(
                                id = "${idPrefix}_missing",
                                text = AppContext.getString(R.string.ai_chat_message_content_missing),
                                isHighlighted = false,
                                isFirst = true,
                                isLast = true,
                            )
                        )
                    }
                    result.add(
                        UserContainerHeader(
                            id = "${idPrefix}_header",
                            showAvatar = conversation.showAvatar,
                            modelName = headerText.title,
                            modelSubtitle = headerText.subtitle,
                        )
                    )
                    result
                }
            }
        }

        private fun addUserAnswerSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            if (message.question.isBlank()) return
            result.add(UserContent(id = "${idPrefix}_content", source = message.question))
        }

        private fun addUserReasoningSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            showExpandedReasoning: Boolean,
        ) {
            val reasoning = message.reasoningContent.normalizeReasoningWhitespace()
            if (reasoning.isBlank()) return
            if (showExpandedReasoning) {
                result.add(UserVerticalSpace(id = "${idPrefix}_reasoning_space", height = 8.dp))
                result.add(
                    UserReasoningContent(
                        id = "${idPrefix}_reasoning",
                        source = reasoning,
                        showCollapseFooter = reasoning.length >= REASONING_COLLAPSE_FOOTER_MIN_CHARS,
                    )
                )
            }
            result.add(
                UserReasoningHeader(
                    id = "${idPrefix}_reasoning_header",
                    time = message.reasoningTime,
                    preview = buildReasoningPreview(reasoning),
                    isStreaming = message.uId == MessageUIState.STREAMING.value,
                )
            )
        }

        private fun addUserSearchSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            if (message.searchResults.isBlank()) return
            SearchResult.fromJson(message.searchResults)?.let { searchResult ->
                if (searchResult.citations.isNotEmpty()) {
                    result.add(
                        UserSearchResults(
                            id = "${idPrefix}_search_results",
                            searchResult = searchResult,
                        )
                    )
                }
            }
        }

        private fun createRobotMessageModels(
            message: MessageEntity,
            conversation: Conversation,
            getAIModel: (String) -> String,
            effectiveCompletionId: String,
            showExpandedReasoning: Boolean,
        ): List<MessageUiModel> {
            val normalizedMessage = message.normalizedFor(conversation.isDuel)
            val idPrefix = "${effectiveCompletionId}_${RoleType.ASSISTANT.value}"
            val result = mutableListOf<MessageUiModel>()
            result.add(
                RobotContainerFooter(
                    id = "${idPrefix}_footer",
                    showTokens = conversation.showTokens,
                    messageEntity = normalizedMessage,
                    isReportable = true,
                )
            )
            addRobotAnswerSection(result, normalizedMessage, idPrefix)
            addRobotReasoningSection(result, normalizedMessage, idPrefix, showExpandedReasoning)
            addRobotToolTraceSection(result, normalizedMessage, idPrefix)
            addRobotSearchSection(result, normalizedMessage, idPrefix)
            val headerText = buildDuelHeaderText(
                conversation = conversation,
                speaker = DuelSpeakerLabel.B,
                modelName = getAIModel(message.model),
            )
            result.add(
                RobotContainerHeader(
                    id = "${idPrefix}_header",
                    showAvatar = conversation.showAvatar,
                    modelName = headerText.title,
                    modelSubtitle = headerText.subtitle,
                )
            )
            return result
        }

        /**
         * 回答正文：普通长度整段一个 item；超长回答按 Markdown 顶层块拆成多个 item。
         *
         * 拆分只发生在**已落库、非流式**的稳定回答上：
         * - 流式期间正文每帧都在增长，分块会让打字光标和增量解析不停重建 item，所以整段渲染；
         * - 流结束、消息转为 NORMAL 后，下一次 [fromMessage] 会自然产出分块版本。
         *
         * 注意顺序：聊天列表是 `reverseLayout = true`，列表索引 0 在屏幕底部，
         * 因此把分块**逆序**加入 result，视觉上从上到下才是 partIndex 0 → N。
         */
        private fun addRobotAnswerSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            if (message.answer.isBlank()) return
            val isStreaming = message.uId == MessageUIState.STREAMING.value
            // 分块器内部会解析整篇 Markdown（CPU 工作；解析失败或含引用定义时自动退回整段）。
            // 主聊天路径在 IO 线程构建消息；AIDuel 少数调用点可能在主线程，但同一条消息只会
            // 构建一次（之后命中 cachedMessageUiModels），9000 字量级的解析是几十毫秒，可接受。
            val chunks = if (isStreaming) {
                emptyList()
            } else {
                splitMarkdownForLazyRendering(message.answer)
            }

            if (chunks.size <= 1) {
                // 未拆分：保持原 id，LazyColumn key 与拆分前完全一致，避免普通消息被重建。
                result.add(
                    RobotContent(
                        id = "${idPrefix}_content",
                        answerSource = message.answer,
                        isStreaming = isStreaming,
                    )
                )
                return
            }

            chunks.asReversed().forEach { chunk ->
                result.add(
                    RobotContent(
                        id = "${idPrefix}_content_${chunk.index}",
                        answerSource = chunk.source,
                        isStreaming = false,
                        // partIndex/partCount 供界面区分"文档开头/续块"，补块间距与后续可视化策略使用。
                        partIndex = chunk.index,
                        partCount = chunks.size,
                    )
                )
            }
        }

        private fun addRobotReasoningSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            showExpandedReasoning: Boolean,
        ) {
            val reasoning = message.reasoningContent.normalizeReasoningWhitespace()
            if (reasoning.isBlank()) return
            if (showExpandedReasoning) {
                result.add(RobotVerticalSpace(id = "${idPrefix}_reasoning_space", height = 8.dp))
                result.add(
                    RobotReasoningContent(
                        id = "${idPrefix}_reasoning",
                        reasoningSource = reasoning,
                        isStreaming = message.uId == MessageUIState.STREAMING.value,
                        showCollapseFooter = reasoning.length >= REASONING_COLLAPSE_FOOTER_MIN_CHARS,
                    )
                )
            }
            result.add(
                RobotReasoningHeader(
                    id = "${idPrefix}_reasoning_header",
                    time = message.reasoningTime,
                    preview = buildReasoningPreview(reasoning),
                    isStreaming = message.uId == MessageUIState.STREAMING.value,
                    isFirst = !showExpandedReasoning,
                )
            )
        }

        private fun addRobotToolTraceSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            if (message.toolCalls.isBlank()) return
            result.add(
                RobotToolCallHistory(
                    id = "${idPrefix}_tool_calls",
                    toolCallsJson = message.toolCalls,
                )
            )
        }

        private fun addRobotSearchSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            if (message.searchResults.isBlank()) return
            SearchResult.fromJson(message.searchResults)?.let { searchResult ->
                if (searchResult.citations.isNotEmpty()) {
                    result.add(
                        RobotSearchResults(
                            id = "${idPrefix}_search_results",
                            searchResult = searchResult,
                        )
                    )
                }
            }
        }

        private val Conversation.isDuel: Boolean
            get() = entryType == AIConversationEntryType.DUEL

        private fun MessageEntity.normalizedFor(isDuel: Boolean, user: Boolean = false): MessageEntity {
            if (!isDuel) return this
            return if (user) {
                copy(question = stripDuelSpeakerPrefix(question))
            } else {
                copy(answer = stripDuelSpeakerPrefix(answer))
            }
        }

        private enum class DuelSpeakerLabel { A, B }

        private data class DuelHeaderText(val title: String, val subtitle: String)

        private fun buildDuelHeaderText(
            conversation: Conversation,
            speaker: DuelSpeakerLabel,
            modelName: String,
        ): DuelHeaderText {
            if (!conversation.isDuel) return DuelHeaderText(title = modelName, subtitle = "")
            val config = AIDuelConfigCodec.decodeOrNull(conversation.prompt)
            val label = if (speaker == DuelSpeakerLabel.A) {
                val fromConfig = config?.roleNameA.orEmpty().ifBlank { config?.promptNameA.orEmpty() }
                val fromJson = parseDuelConfigTextField(conversation.prompt, "roleNameA")
                    .ifBlank { parseDuelConfigTextField(conversation.prompt, "promptNameA") }
                fromConfig.ifBlank { fromJson }.ifBlank {
                    AppContext.getString(R.string.ai_duel_speaker_a)
                }
            } else {
                val fromConfig = config?.roleNameB.orEmpty().ifBlank { config?.promptNameB.orEmpty() }
                val fromJson = parseDuelConfigTextField(conversation.prompt, "roleNameB")
                    .ifBlank { parseDuelConfigTextField(conversation.prompt, "promptNameB") }
                fromConfig.ifBlank { fromJson }.ifBlank {
                    AppContext.getString(R.string.ai_duel_speaker_b)
                }
            }
            val subtitleFromConfig = if (speaker == DuelSpeakerLabel.A) {
                config?.engineA?.model?.title.orEmpty()
            } else {
                config?.engineB?.model?.title.orEmpty()
            }
            val subtitleFromJson = if (speaker == DuelSpeakerLabel.A) {
                parseDuelConfigModelTitle(conversation.prompt, "engineA")
            } else {
                parseDuelConfigModelTitle(conversation.prompt, "engineB")
            }
            return DuelHeaderText(
                title = label,
                subtitle = subtitleFromConfig.ifBlank { subtitleFromJson }.ifBlank { modelName },
            )
        }

        private fun parseDuelConfigTextField(prompt: String, key: String): String {
            if (prompt.isBlank()) return ""
            return runCatching {
                val obj = AppJson.parseToJsonElement(prompt).jsonObject
                (obj[key] as? JsonPrimitive)?.contentOrNull.orEmpty().trim()
            }.getOrNull().orEmpty()
        }

        private fun parseDuelConfigModelTitle(prompt: String, engineKey: String): String {
            if (prompt.isBlank()) return ""
            return runCatching {
                val obj = AppJson.parseToJsonElement(prompt).jsonObject
                val engineObj = obj[engineKey] as? JsonObject ?: return ""
                val modelObj = engineObj["model"] as? JsonObject ?: return ""
                (modelObj["title"] as? JsonPrimitive)?.contentOrNull.orEmpty()
            }.getOrNull().orEmpty()
        }

        private fun stripDuelSpeakerPrefix(text: String): String = when {
            text.startsWith("【A】") -> text.removePrefix("【A】").trimStart()
            text.startsWith("【B】") -> text.removePrefix("【B】").trimStart()
            else -> text
        }

        private fun String.normalizeReasoningWhitespace(): String = trim()
            .replace("\r\n", "\n")
            .replace(Regex("""\n(?:[ \t]*\n)+"""), "\n\n")

        private fun buildReasoningPreview(reasoning: String): String {
            if (reasoning.isBlank()) return ""
            val normalized = reasoning.replace("\r\n", "\n")
                .replace(Regex("""\n(?:\s*\n)+"""), "\n")
                .trimEnd()
            val lastBreak = normalized.lastIndexOf('\n')
            val preview = if (lastBreak >= 0 && lastBreak < normalized.length - 1) {
                normalized.substring(lastBreak + 1)
            } else {
                normalized
            }
            return preview.trim().take(120)
        }
    }
}
