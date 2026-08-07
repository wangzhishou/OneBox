package com.shifenmiao.ai.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.halilibo.richtext.markwon.MarkdownAstNodeParser
import com.shifenmiao.core.R
import com.shifenmiao.database.ai.entity.MessageEntity
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.AttachmentPayloadDto
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.MessageUIState
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.SearchResult
import com.shifenmiao.model.node.AstBlockQuote
import com.shifenmiao.model.node.AstDocument
import com.shifenmiao.model.node.AstFencedCodeBlock
import com.shifenmiao.model.node.AstHeading
import com.shifenmiao.model.node.AstIndentedCodeBlock
import com.shifenmiao.model.node.AstJLatexBlockMath
import com.shifenmiao.model.node.AstListItem
import com.shifenmiao.model.node.AstNode
import com.shifenmiao.model.node.AstOrderedList
import com.shifenmiao.model.node.AstParagraph
import com.shifenmiao.model.node.AstTableRoot
import com.shifenmiao.model.node.AstThematicBreak
import com.shifenmiao.model.node.AstUnorderedList

/**
 * 消息UI模型，用于在UI中展示消息
 */
sealed class MessageUiModel(
    open val id: String
) {
    /**
     * 用户消息头部
     */
    data class UserBlock(
        override val id: String,
        val text: String,
        val showAvatar: Boolean,
        val attachments: List<AttachmentPayloadDto>?
    ) : MessageUiModel(id)

    /**
     * 用户消息附件展示（图片缩略图 + 文件 chip）。
     * 持有预解析的 DTO 列表，避免 UI 层重复 Gson 反序列化。
     */
    data class UserAttachments(
        override val id: String,
        val attachments: List<AttachmentPayloadDto>
    ) : MessageUiModel(id)

    data class UserContainerHeader(
        override val id: String,
        val showAvatar: Boolean,
        val modelName: String,
        val modelSubtitle: String = ""
    ) : MessageUiModel(id)

    data class UserContainerFooter(
        override val id: String,
        val messageEntity: MessageEntity
    ) : MessageUiModel(id)

    data class UserContent(
        override val id: String,
        val isHighlighted: Boolean,
        val node: AstNode
    ) : MessageUiModel(id)

    data class UserTextContent(
        override val id: String,
        val text: String,
        val isHighlighted: Boolean,
        val isFirst: Boolean,
        val isLast: Boolean
    ) : MessageUiModel(id)

    data class UserMarkdownBlock(
        override val id: String,
        val isHighlighted: Boolean,
        val node: AstNode,
        val blockType: MarkdownBlock.BlockType,
        val isFirst: Boolean,
        val isLast: Boolean
    ) : MessageUiModel(id)

    data class UserReasoningContent(
        override val id: String,
        val isHighlighted: Boolean,
        val node: AstNode
    ) : MessageUiModel(id)

    data class UserReasoningHeader(
        override val id: String,
        val time: Long = 0L,
        val preview: String = "",
        val isStreaming: Boolean = false,
    ) : MessageUiModel(id)

    data class UserReasoningBlock(
        override val id: String,
        val node: AstNode,
        val isHighlighted: Boolean,
        val blockType: MarkdownBlock.BlockType,
        val isFirst: Boolean,
        val isLast: Boolean
    ) : MessageUiModel(id)

    data class UserSearchResults(
        override val id: String,
        val searchResult: SearchResult,
        val isExpanded: Boolean = false
    ) : MessageUiModel(id)

    data class UserLoading(
        override val id: String
    ) : MessageUiModel(id)

    data class UserError(
        override val id: String,
        val errorMessage: String = AppContext.getString(R.string.ai_error),
    ) : MessageUiModel(id)

    data class UserVerticalSpace(
        override val id: String,
        val height: Dp = 4.dp
    ) : MessageUiModel(id)

    /**
     * Markdown内容
     */
    data class RobotContent(
        override val id: String,
        val isHighlighted: Boolean,
        val isStreaming: Boolean,
        val showCursor: Boolean = false,
        val answerAstNode: AstNode? = null
    ) : MessageUiModel(id)


    data class RobotReasoningContent(
        override val id: String,
        val isHighlighted: Boolean,
        val isStreaming: Boolean,
        val reasoningAstNode: AstNode? = null,
        val isFirst: Boolean = true,
        val isLast: Boolean = true,
    ) : MessageUiModel(id)

    data class RobotReasoningHeader(
        override val id: String,
        val time: Long = 0L,
        val preview: String = "",
        val isStreaming: Boolean = false,
        val isFirst: Boolean = true,
        val isLast: Boolean = false,
    ) : MessageUiModel(id)

    data class RobotLoading(
        override val id: String
    ) : MessageUiModel(id)

    data class RobotVerticalSpace(
        override val id: String,
        val height: Dp = 4.dp
    ) : MessageUiModel(id)

    data class RobotError(
        override val id: String,
        val errorMessage: String = AppContext.getString(R.string.ai_error),
        /** 直连模式下错误以原始 HTTP 信息展示，不走代理错误卡片 */
        val isDirectConnection: Boolean = false,
    ) : MessageUiModel(id)

    data class RobotContainerFooter(
        override val id: String,
        val showTokens: Boolean,
        val messageEntity: MessageEntity
    ) : MessageUiModel(id)

    data class RobotContainerHeader(
        override val id: String,
        val showAvatar: Boolean,
        val modelName: String,
        val modelSubtitle: String = ""
    ) : MessageUiModel(id)

    /**
     * 搜索结果展示块
     */
    data class RobotSearchResults(
        override val id: String,
        val searchResult: SearchResult,
        val isExpanded: Boolean = false
    ) : MessageUiModel(id)

    /**
     * Agent 工具调用历史展示块 —— 展示已持久化到数据库的 tool_calls 记录
     */
    data class RobotToolCallHistory(
        override val id: String,
        val toolCallsJson: String,
        val isLive: Boolean = false
    ) : MessageUiModel(id)

    data class RobotReasoningBlock(
        override val id: String,
        val node: AstNode,
        val isHighlighted: Boolean,
        val isStreaming: Boolean,
        val blockType: MarkdownBlock.BlockType,
        /** 视觉上的顶部 block（reverseLayout 下对应 blocks.lastIndex） */
        val isFirst: Boolean = true,
        /** 视觉上的底部 block（只有最后一个 block 负责收底部圆角） */
        val isLast: Boolean = true,
    ) : MessageUiModel(id)

    data class MarkdownBlock(
        override val id: String,
        val isHighlighted: Boolean,
        val isStreaming: Boolean,
        val showCursor: Boolean = false,
        val node: AstNode,
        val blockType: BlockType
    ) : MessageUiModel(id) {
        enum class BlockType {
            HEADING,
            PARAGRAPH,
            CODE_BLOCK,
            BLOCKQUOTE,
            LIST,
            TABLE,
            THEMATIC_BREAK,
            LATEX_FORMULAS,
            OTHER
        }

        companion object {
            /**
             * 将AstNode树拆分为可单独渲染的块列表
             */
            fun splitIntoBlocks(
                rootNode: AstNode?,
                prefixId: String = "",
                message: MessageEntity
            ): List<MarkdownBlock> {
                rootNode ?: return emptyList()

                val blocks = mutableListOf<MarkdownBlock>()
                var blockId = 0

                fun processNode(node: AstNode?) {
                    node ?: return

                    when (node.type) {
                        is AstHeading,
                        is AstParagraph,
                        is AstFencedCodeBlock,
                        is AstIndentedCodeBlock,
                        is AstBlockQuote,
                        is AstOrderedList,
                        is AstUnorderedList,
                        is AstTableRoot,
                        is AstThematicBreak,
                        is AstJLatexBlockMath -> {
                            val blockType = when (node.type) {
                                is AstHeading -> BlockType.HEADING
                                is AstParagraph -> BlockType.PARAGRAPH
                                is AstFencedCodeBlock, is AstIndentedCodeBlock -> BlockType.CODE_BLOCK
                                is AstBlockQuote -> BlockType.BLOCKQUOTE
                                is AstOrderedList, is AstUnorderedList -> BlockType.LIST
                                is AstTableRoot -> BlockType.TABLE
                                is AstThematicBreak -> BlockType.THEMATIC_BREAK
                                is AstJLatexBlockMath -> BlockType.LATEX_FORMULAS
                                else -> BlockType.OTHER
                            }

                            blocks.add(
                                MarkdownBlock(
                                    id = "${prefixId}_block_${blockId++}",
                                    node = node,
                                    blockType = blockType,
                                    isHighlighted = message.uId > 0,
                                    isStreaming = message.uId == MessageUIState.STREAMING.value,
                                    showCursor = false
                                )
                            )
                            return
                        }

                        is AstDocument -> {
                            var childNode = node.links.firstChild
                            while (childNode != null) {
                                processNode(childNode)
                                childNode = childNode.links.next
                            }
                        }

                        else -> {
                            val nextNode = node.links.next
                            if (nextNode != null) {
                                processNode(nextNode)
                            }
                        }
                    }
                }

                processNode(rootNode)
                return blocks.reversed()
            }
        }
    }

    companion object {
        /**
         * 将消息实体转换为UI模型列表。
         *
         * @param blockReuseCache 流式状态下用于复用上一轮 parse 出的 [AstNode] 引用，
         *                        让下游 `MarkdownRichText.remember(astNode, ...)` 能命中缓存。
         *                        非流式调用方传 null 即可（NORMAL 整体走 `cachedMessageUiModels`）。
         * @param showExpandedReasoning reasoning 折叠时不生成重型 reasoning content/block item，
         *                              只保留 header 预览，避免无意义的长 Markdown 构建。
         */
        fun fromMessage(
            parser: MarkdownAstNodeParser,
            message: MessageEntity,
            conversation: Conversation,
            getAIModel: (String) -> String,
            blockReuseCache: BlockReuseCache? = null,
            showExpandedReasoning: Boolean = true,
        ): List<MessageUiModel> {
            // 使用 completionId 作为 id 前缀；若为空（旧数据或异常流），回退到数据库自增 id，
            // 确保多轮对话或同一对话中多条消息不会因空 completionId 而产生重复 key。
            val effectiveCompletionId = message.completionId.takeIf { it.isNotBlank() }
                ?: "msg_${message.id}"
            return if (message.role == RoleType.USER.value) {
                createUserMessageModels(
                    parser = parser,
                    message = message,
                    conversation = conversation,
                    getAIModel = getAIModel,
                    effectiveCompletionId = effectiveCompletionId,
                    showExpandedReasoning = showExpandedReasoning
                )
            } else {
                // 根据消息状态处理不同的UI模型
                when (message.uId) {
                    MessageUIState.LOADING.value -> {
                        // 使用 effectiveCompletionId + 角色后缀确保唯一性，且在 placeholder → DB 切换时保持稳定
                        val idPrefix = "${effectiveCompletionId}_thinking"
                        val headerText = buildDuelHeaderText(
                            conversation = conversation,
                            speaker = DuelSpeakerLabel.B,
                            modelName = getAIModel(message.model)
                        )
                        // 机器人正在思考
                        return listOf(
                            RobotContainerFooter(
                                id = "${idPrefix}_footer",
                                showTokens = false,
                                messageEntity = if (conversation.entryType == AIConversationEntryType.DUEL) {
                                    message.copy(answer = stripDuelSpeakerPrefix(message.answer))
                                } else {
                                    message
                                }
                            ),
                            RobotLoading(
                                id = "${idPrefix}_thinking",
                            ),
                            RobotContainerHeader(
                                id = "${idPrefix}_header",
                                showAvatar = conversation.showAvatar,
                                modelName = headerText.title,
                                modelSubtitle = headerText.subtitle
                            )
                        )
                    }

                    MessageUIState.ERROR.value -> {
                        // 使用 effectiveCompletionId + 角色后缀确保唯一性
                        val idPrefix = "${effectiveCompletionId}_error"
                        val headerText = buildDuelHeaderText(
                            conversation = conversation,
                            speaker = DuelSpeakerLabel.B,
                            modelName = getAIModel(message.model)
                        )
                        // 机器人输出错误
                        val normalizedMessage = if (conversation.entryType == AIConversationEntryType.DUEL) {
                            message.copy(answer = stripDuelSpeakerPrefix(message.answer))
                        } else {
                            message
                        }
                        val isDirectConnection = conversation.engine.canChatDirectly()
                        return listOf(
                            RobotContainerFooter(
                                id = "${idPrefix}_footer",
                                showTokens = false,
                                messageEntity = normalizedMessage
                            ),
                            RobotError(
                                id = "${idPrefix}_Error",
                                errorMessage = normalizedMessage.answer.ifBlank {
                                    AppContext.getString(R.string.ai_error)
                                },
                                isDirectConnection = isDirectConnection
                            ),
                            RobotContainerHeader(
                                id = "${idPrefix}_header",
                                showAvatar = conversation.showAvatar,
                                modelName = headerText.title,
                                modelSubtitle = headerText.subtitle
                            )
                        )
                    }

                    else -> {
                        createRobotMessageModels(
                            parser,
                            message,
                            conversation,
                            getAIModel,
                            blockReuseCache,
                            effectiveCompletionId,
                            showExpandedReasoning
                        )
                    }
                }
            }
        }

        /**
         * 创建用户消息UI模型
         */
        private fun createUserMessageModels(
            parser: MarkdownAstNodeParser,
            message: MessageEntity,
            conversation: Conversation,
            getAIModel: (String) -> String,
            effectiveCompletionId: String,
            showExpandedReasoning: Boolean = true,
        ): List<MessageUiModel> {
            val result = mutableListOf<MessageUiModel>()
            val normalizedMessage = if (conversation.entryType == AIConversationEntryType.DUEL) {
                message.copy(
                    question = stripDuelSpeakerPrefix(message.question)
                )
            } else {
                message
            }
            // 使用 effectiveCompletionId + 角色后缀确保唯一性，且在 placeholder → DB 切换时保持稳定
            val idPrefix = "${effectiveCompletionId}_${RoleType.USER.value}"
            // 用户消息只显示文本, 如果不是AI互聊的话
            if (conversation.entryType != AIConversationEntryType.DUEL && message.question.isNotBlank()) {
                // 先添加附件展示（如果有）— 在模型创建时反序列化，避免 UI 层重复解析
                var attachments: List<AttachmentPayloadDto>? = null
                if (message.attachmentsJson.isNotBlank()) {
                    attachments = runCatching {
                        Gson().fromJson<List<AttachmentPayloadDto>>(
                            message.attachmentsJson,
                            TypeToken.getParameterized(
                                List::class.java, AttachmentPayloadDto::class.java
                            ).type
                        )
                    }.getOrNull()
                }
                result.add(
                    UserBlock(
                        id = "${idPrefix}_question",
                        showAvatar = conversation.showAvatar,
                        text = message.question,
                        attachments = attachments
                    )
                )
                return result
            }
            return when (message.uId) {
                MessageUIState.LOADING.value -> {
                    val headerText = buildDuelHeaderText(
                        conversation = conversation,
                        speaker = DuelSpeakerLabel.A,
                        modelName = getAIModel(message.model)
                    )
                    listOf(
                        UserContainerFooter(
                            id = "${idPrefix}_footer",
                            messageEntity = normalizedMessage
                        ),
                        UserLoading(id = "${idPrefix}_thinking"),
                        UserContainerHeader(
                            id = "${idPrefix}_header",
                            showAvatar = conversation.showAvatar,
                            modelName = headerText.title,
                            modelSubtitle = headerText.subtitle
                        )
                    )
                }

                MessageUIState.ERROR.value -> {
                    val headerText = buildDuelHeaderText(
                        conversation = conversation,
                        speaker = DuelSpeakerLabel.A,
                        modelName = getAIModel(message.model)
                    )
                    listOf(
                        UserContainerFooter(
                            id = "${idPrefix}_footer",
                            messageEntity = normalizedMessage
                        ),
                        UserError(
                            id = "${idPrefix}_error",
                            errorMessage = normalizedMessage.question.ifBlank {
                                AppContext.getString(R.string.ai_error)
                            }
                        ),
                        UserContainerHeader(
                            id = "${idPrefix}_header",
                            showAvatar = conversation.showAvatar,
                            modelName = headerText.title,
                            modelSubtitle = headerText.subtitle
                        )
                    )
                }

                else -> {
                    val result = mutableListOf<MessageUiModel>()
                    val headerText = buildDuelHeaderText(
                        conversation = conversation,
                        speaker = DuelSpeakerLabel.A,
                        modelName = getAIModel(message.model)
                    )
                    result.add(
                        UserContainerFooter(
                            id = "${idPrefix}_footer",
                            messageEntity = normalizedMessage
                        )
                    )
                    addUserMessageSections(
                        parser = parser,
                        result = result,
                        message = normalizedMessage,
                        idPrefix = idPrefix,
                        showExpandedReasoning = showExpandedReasoning
                    )
                    result.add(
                        UserContainerHeader(
                            id = "${idPrefix}_header",
                            showAvatar = conversation.showAvatar,
                            modelName = headerText.title,
                            modelSubtitle = headerText.subtitle
                        )
                    )
                    result
                }
            }
        }

        private fun addUserMessageSections(
            parser: MarkdownAstNodeParser,
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            showExpandedReasoning: Boolean,
        ) {
            addUserAnswerSection(parser, result, message, idPrefix)
            addUserReasoningSection(parser, result, message, idPrefix, showExpandedReasoning)
            addUserSearchSection(result, message, idPrefix)
        }

        /** 用户侧正文 section：只处理 question，自身保持 block 化策略。 */
        private fun addUserAnswerSection(
            parser: MarkdownAstNodeParser,
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            val question = message.question
            if (question.isBlank()) return

            val parsedNode = try {
                parser.parse(question)
            } catch (_: Exception) {
                null
            }

            if (parsedNode == null) {
                result.add(
                    UserTextContent(
                        id = "${idPrefix}_text",
                        text = question,
                        isHighlighted = message.uId > 0,
                        isFirst = true,
                        isLast = true
                    )
                )
                return
            }

            val shouldSplit = question.length > 500 || containsComplexBlocks(parsedNode)
            if (shouldSplit) {
                val blocks = MarkdownBlock.splitIntoBlocks(
                    rootNode = parsedNode,
                    prefixId = "${idPrefix}_content",
                    message = message
                )
                blocks.forEachIndexed { index, block ->
                    val isBottom = index == 0
                    val isTop = index == blocks.lastIndex
                    result.add(
                        UserMarkdownBlock(
                            id = "${block.id}_user",
                            node = block.node,
                            blockType = block.blockType,
                            isHighlighted = message.uId > 0,
                            isFirst = isTop,
                            isLast = isBottom
                        )
                    )
                }
                return
            }

            result.add(
                UserContent(
                    id = "${idPrefix}_content",
                    node = parsedNode,
                    isHighlighted = message.uId > 0
                )
            )
        }

        /** 用户侧 reasoning section：折叠时只保留 header，展开时再生成 content/block。 */
        private fun addUserReasoningSection(
            parser: MarkdownAstNodeParser,
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            showExpandedReasoning: Boolean,
        ) {
            val parsedReasoningNode = parseReasoningNodeOrFallback(parser, message.reasoningContent)
            if (message.reasoningContent.isBlank() || parsedReasoningNode == null) return

            if (showExpandedReasoning) {
                result.add(
                    UserVerticalSpace(
                        id = "${idPrefix}_reasoning_space",
                        height = 8.dp
                    )
                )
                val shouldSplitReasoning = message.reasoningContent.length > 500 || containsComplexBlocks(parsedReasoningNode)
                if (shouldSplitReasoning) {
                    val blocks = MarkdownBlock.splitIntoBlocks(
                        rootNode = parsedReasoningNode,
                        prefixId = "${idPrefix}_reasoning",
                        message = message
                    )
                    blocks.forEachIndexed { index, block ->
                        val isBottom = index == 0
                        val isTop = index == blocks.lastIndex
                        result.add(
                            UserReasoningBlock(
                                id = "${block.id}_user_reasoning",
                                node = block.node,
                                blockType = block.blockType,
                                isHighlighted = message.uId > 0,
                                isFirst = isTop,
                                isLast = isBottom
                            )
                        )
                    }
                } else {
                    result.add(
                        UserReasoningContent(
                            id = "${idPrefix}_reasoning",
                            node = parsedReasoningNode,
                            isHighlighted = message.uId > 0
                        )
                    )
                }
            }

            result.add(
                UserReasoningHeader(
                    id = "${idPrefix}_reasoning_header",
                    time = message.reasoningTime,
                    preview = buildReasoningPreview(message.reasoningContent),
                    isStreaming = message.uId == MessageUIState.STREAMING.value,
                )
            )
        }

        /** 用户侧搜索结果 section：只在 citations 非空时进入扁平列表。 */
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
                            searchResult = searchResult
                        )
                    )
                }
            }
        }

        /**
         * 创建机器人消息UI模型
         */
        private fun createRobotMessageModels(
            parser: MarkdownAstNodeParser,
            message: MessageEntity,
            conversation: Conversation,
            getAIModel: (String) -> String,
            blockReuseCache: BlockReuseCache? = null,
            effectiveCompletionId: String,
            showExpandedReasoning: Boolean,
        ): List<MessageUiModel> {
            val result = mutableListOf<MessageUiModel>()
            val normalizedMessage = if (conversation.entryType == AIConversationEntryType.DUEL) {
                message.copy(
                    answer = stripDuelSpeakerPrefix(message.answer)
                )
            } else {
                message
            }
            // 使用 effectiveCompletionId + 角色后缀确保唯一性，且在 placeholder → DB 切换时保持稳定
            val idPrefix = "${effectiveCompletionId}_${RoleType.ASSISTANT.value}"

            // 添加消息底部
            result.add(
                RobotContainerFooter(
                    id = "${idPrefix}_footer",
                    showTokens = conversation.showTokens,
                    messageEntity = normalizedMessage
                )
            )

            // 添加消息内容
            addRobotMessageSections(
                parser,
                result,
                normalizedMessage,
                idPrefix,
                blockReuseCache,
                showExpandedReasoning
            )

            // 添加机器人消息头部
            val headerText = buildDuelHeaderText(
                conversation = conversation,
                speaker = DuelSpeakerLabel.B,
                modelName = getAIModel(message.model)
            )
            result.add(
                RobotContainerHeader(
                    id = "${idPrefix}_header",
                    showAvatar = conversation.showAvatar,
                    modelName = headerText.title,
                    modelSubtitle = headerText.subtitle
                )
            )

            return result
        }

        private enum class DuelSpeakerLabel { A, B }

        private data class DuelHeaderText(
            val title: String,
            val subtitle: String
        )

        private fun parseDuelConfigTextField(prompt: String, key: String): String {
            if (prompt.isBlank()) return ""
            return kotlin.runCatching {
                val obj = JsonParser.parseString(prompt).asJsonObject
                obj.get(key)?.takeIf { it.isJsonPrimitive }?.asString.orEmpty().trim()
            }.getOrNull().orEmpty()
        }

        private fun parseDuelConfigModelTitle(prompt: String, engineKey: String): String {
            if (prompt.isBlank()) return ""
            return kotlin.runCatching {
                val obj = JsonParser.parseString(prompt).asJsonObject
                val engineObj = obj.get(engineKey)?.asJsonObject ?: return ""
                val modelObj = engineObj.get("model")?.asJsonObject ?: return ""
                modelObj.get("title")?.takeIf { it.isJsonPrimitive }?.asString.orEmpty().trim()
            }.getOrNull().orEmpty()
        }

        private fun buildDuelHeaderText(
            conversation: Conversation,
            speaker: DuelSpeakerLabel,
            modelName: String
        ): DuelHeaderText {
            if (conversation.entryType != AIConversationEntryType.DUEL) {
                return DuelHeaderText(title = modelName, subtitle = "")
            }
            val config = AIDuelConfigCodec.decodeOrNull(conversation.prompt)
            val label = if (speaker == DuelSpeakerLabel.A) {
                val fromConfig = config?.roleNameA.orEmpty()
                    .ifBlank { config?.promptNameA.orEmpty() }
                val fromJson = parseDuelConfigTextField(conversation.prompt, "roleNameA")
                    .ifBlank { parseDuelConfigTextField(conversation.prompt, "promptNameA") }
                fromConfig.ifBlank { fromJson }
                    .ifBlank { AppContext.getString(R.string.ai_duel_speaker_a) }
            } else {
                val fromConfig = config?.roleNameB.orEmpty()
                    .ifBlank { config?.promptNameB.orEmpty() }
                val fromJson = parseDuelConfigTextField(conversation.prompt, "roleNameB")
                    .ifBlank { parseDuelConfigTextField(conversation.prompt, "promptNameB") }
                fromConfig.ifBlank { fromJson }
                    .ifBlank { AppContext.getString(R.string.ai_duel_speaker_b) }
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
            val subtitle = subtitleFromConfig
                .ifBlank { subtitleFromJson }
                .ifBlank { modelName }
            return DuelHeaderText(
                title = label,
                subtitle = subtitle
            )
        }

        private fun stripDuelSpeakerPrefix(text: String): String {
            return when {
                text.startsWith("【A】") -> text.removePrefix("【A】").trimStart()
                text.startsWith("【B】") -> text.removePrefix("【B】").trimStart()
                else -> text
            }
        }

        /**
         * 添加机器人消息内容
         */
        private fun normalizeLatexDelimiters(markdown: String): String {
            var result = markdown
            result = result.replace(
                Regex("""\\\[(.+?)\\\]""", setOf(RegexOption.DOT_MATCHES_ALL))
            ) { match ->
                val content = match.groupValues.getOrNull(1).orEmpty()
                "${'$'}${'$'}\n$content\n${'$'}${'$'}"
            }
            result = result.replace(
                Regex("""\\\((.+?)\\\)""", setOf(RegexOption.DOT_MATCHES_ALL))
            ) { match ->
                val content = match.groupValues.getOrNull(1).orEmpty()
                "${'$'}$content${'$'}"
            }
            return result
        }

        private fun addRobotMessageSections(
            parser: MarkdownAstNodeParser,
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            blockReuseCache: BlockReuseCache? = null,
            showExpandedReasoning: Boolean,
        ) {
            addRobotAnswerSection(parser, result, message, idPrefix, blockReuseCache)
            addRobotReasoningSection(parser, result, message, idPrefix, blockReuseCache, showExpandedReasoning)
            addRobotToolTraceSection(result, message, idPrefix)
            addRobotSearchSection(result, message, idPrefix)
        }

        /** 机器人正文 section：维持“短内容单块 / 长内容拆 block”的性能模型。 */
        private fun addRobotAnswerSection(
            parser: MarkdownAstNodeParser,
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            blockReuseCache: BlockReuseCache? = null,
        ) {
            val answerText = message.answer
            if (answerText.isBlank()) return

            val parsedAnswerNode = try {
                parser.parse(answerText)
            } catch (_: Exception) {
                null
            } ?: return

            if (answerText.length > 500 || containsComplexBlocks(parsedAnswerNode)) {
                val blocks = MarkdownBlock.splitIntoBlocks(
                    parsedAnswerNode,
                    "${idPrefix}_content",
                    message
                )
                val finalBlocks = if (blockReuseCache != null) {
                    blockReuseCache.trim(message.completionId, blocks.size)
                    blocks.mapIndexed { index, block ->
                        block.copy(
                            node = blockReuseCache.reuseOrPut(
                                messageKey = message.completionId,
                                blockIndex = index,
                                newNode = block.node
                            )
                        )
                    }
                } else blocks
                result.addAll(finalBlocks)
                return
            }

            val finalNode = if (blockReuseCache != null) {
                blockReuseCache.reuseOrPut(
                    messageKey = message.completionId,
                    blockIndex = 0,
                    newNode = parsedAnswerNode
                )
            } else parsedAnswerNode
            result.add(
                RobotContent(
                    id = "${idPrefix}_content",
                    answerAstNode = finalNode,
                    isHighlighted = message.uId > 0,
                    isStreaming = message.uId == MessageUIState.STREAMING.value,
                    showCursor = false
                )
            )
        }

        /**
         * 机器人 reasoning section。
         *
         * 这里不额外引入 footer item，而是把“底部圆角闭合”职责交给最后一个 reasoning block，
         * 这样既能保持 block 级 LazyColumn 性能，也不会让多个 block 都各自带底部圆角。
         */
        private fun addRobotReasoningSection(
            parser: MarkdownAstNodeParser,
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
            blockReuseCache: BlockReuseCache? = null,
            showExpandedReasoning: Boolean,
        ) {
            val parsedReasoningNode = parseReasoningNodeOrFallback(parser, message.reasoningContent)
            if (message.reasoningContent.isBlank() || parsedReasoningNode == null) return

            var hasExpandedContent = false

            if (showExpandedReasoning) {
                result.add(
                    RobotVerticalSpace(
                        id = "${idPrefix}_reasoning_Space",
                        height = 8.dp
                    )
                )
                val shouldSplitReasoning = message.reasoningContent.length > 500 || containsComplexBlocks(parsedReasoningNode)
                if (shouldSplitReasoning) {
                    val blocks = MarkdownBlock.splitIntoBlocks(
                        parsedReasoningNode,
                        "${idPrefix}_reasoning",
                        message
                    )
                    val finalBlocks = if (blockReuseCache != null) {
                        blocks.mapIndexed { index, block ->
                            block.copy(
                                node = blockReuseCache.reuseOrPut(
                                    messageKey = message.completionId,
                                    blockIndex = -1000 - index,
                                    newNode = block.node
                                )
                            )
                        }
                    } else blocks
                    hasExpandedContent = finalBlocks.isNotEmpty()
                    result.addAll(
                        finalBlocks.mapIndexed { index, block ->
                            val isBottom = index == 0
                            val isTop = index == finalBlocks.lastIndex
                            RobotReasoningBlock(
                                id = "${block.id}_reasoning",
                                node = block.node,
                                isHighlighted = message.uId > 0,
                                isStreaming = message.uId == MessageUIState.STREAMING.value,
                                blockType = block.blockType,
                                isFirst = isTop,
                                isLast = isBottom,
                            )
                        }
                    )
                } else {
                    val finalReasoningNode = if (blockReuseCache != null) {
                        blockReuseCache.reuseOrPut(
                            messageKey = message.completionId,
                            blockIndex = -2,
                            newNode = parsedReasoningNode
                        )
                    } else parsedReasoningNode
                    result.add(
                        RobotReasoningContent(
                            id = "${idPrefix}_reasoning",
                            reasoningAstNode = finalReasoningNode,
                            isHighlighted = message.uId > 0,
                            isStreaming = message.uId == MessageUIState.STREAMING.value,
                            isFirst = true,
                            isLast = true,
                        )
                    )
                    hasExpandedContent = true
                }
            }

            result.add(
                RobotReasoningHeader(
                    id = "${idPrefix}_reasoning_header",
                    time = message.reasoningTime,
                    preview = buildReasoningPreview(message.reasoningContent),
                    isStreaming = message.uId == MessageUIState.STREAMING.value,
                    isFirst = !hasExpandedContent,
                    isLast = false,
                )
            )
        }

        /** 机器人工具轨迹 section：结构化保留 toolCalls，不再混入 answer / reasoning 文本。 */
        private fun addRobotToolTraceSection(
            result: MutableList<MessageUiModel>,
            message: MessageEntity,
            idPrefix: String,
        ) {
            if (message.toolCalls.isBlank()) return
            result.add(
                RobotToolCallHistory(
                    id = "${idPrefix}_tool_calls",
                    toolCallsJson = message.toolCalls
                )
            )
        }

        /** 机器人搜索结果 section：引用为空时直接跳过，避免空壳展示块。 */
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
                            searchResult = searchResult
                        )
                    )
                }
            }
        }

        // 检查是否包含复杂块
        private fun containsComplexBlocks(node: AstNode): Boolean {
            var containsComplex = false
            var childNode = node.links.firstChild

            while (childNode != null && !containsComplex) {
                containsComplex = when (childNode.type) {
                    is AstFencedCodeBlock,
                    is AstTableRoot,
                    is AstListItem,
                    is AstBlockQuote -> true

                    else -> false
                }
                childNode = childNode.links.next
            }

            return containsComplex
        }

        private fun parseReasoningNodeOrFallback(
            parser: MarkdownAstNodeParser,
            reasoning: String
        ): AstNode? {
            val normalized = reasoning.normalizeReasoningWhitespace()
            if (normalized.isBlank()) return null
            val direct = kotlin.runCatching { parser.parse(normalized) }.getOrNull()
            if (direct != null) return direct

            val safe = buildString {
                append("```text\n")
                append(normalized.replace("```", "`\u200B``"))
                append("\n```")
            }
            return kotlin.runCatching { parser.parse(safe) }.getOrNull()
        }

        /**
         * 归一化 reasoning 文本：
         * - 把 CRLF 转为 LF
         * - 将连续空行（含仅含空白的行）折叠为单个段落分隔（\n\n），保留段落结构，
         *   避免深度思考卡片里出现大段空白段落。
         */
        private fun String.normalizeReasoningWhitespace(): String {
            return this.trim()
                .replace("\r\n", "\n")
                .replace(Regex("""\n(?:[ \t]*\n)+"""), "\n\n")
        }

        /**
         * 生成 reasoning 折叠态 preview。
         *
         * **仅用于非 streaming 消息**（历史消息、已完成的流）。
         * streaming 态由 [StreamContentProcessor.currentReasoningTail] 以 O(1) 直接覆盖，
         * 不走这里的逻辑。
         *
         * 简化策略：直接取最后一行非空内容（按单个 \n 分隔），只做最小清理，
         * 避免双换行产生的空白 preview。
         */
        private fun buildReasoningPreview(reasoning: String): String {
            if (reasoning.isBlank()) return ""

            val normalized = reasoning
                .replace("\r\n", "\n")
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
