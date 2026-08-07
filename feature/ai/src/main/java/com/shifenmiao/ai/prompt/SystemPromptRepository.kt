package com.shifenmiao.ai.prompt

import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.chat_prompt.entity.PromptEntity
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ChatWorkingMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemPromptRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val textProvider: AgentToolTextProvider,
    private val environmentContextProvider: EnvironmentContextProvider,
) {

    suspend fun getSystemPrompt(title: String, fallback: String = ""): String {
        return appDatabase.chatPromptDao()
            .getSystemPromptByKey(title)
            ?.prompt
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: fallback.trim()
    }

    fun composeToolAwarePrompt(
        systemRules: String,
        interactionProtocol: String = "",
        agentExecutionProtocol: String = "",
        agentRole: String = "",
        taskPrompt: String = "",
        userOverride: String = "",
        environmentContext: String = "",
        tokenBudget: Int = 0
    ): PromptComposition {
        return PromptComposer.compose(
            layers = listOf(
                PromptLayer(
                    type = PromptLayerType.SYSTEM_RULES,
                    content = systemRules,
                    required = true
                ),
                PromptLayer(
                    type = PromptLayerType.ENVIRONMENT_CONTEXT,
                    content = environmentContext,
                    required = false
                ),
                PromptLayer(
                    type = PromptLayerType.INTERACTION_PROTOCOL,
                    content = interactionProtocol,
                    required = false
                ),
                PromptLayer(
                    type = PromptLayerType.AGENT_EXECUTION_PROTOCOL,
                    content = agentExecutionProtocol,
                    required = false
                ),
                PromptLayer(
                    type = PromptLayerType.AGENT_ROLE,
                    content = agentRole,
                    required = false
                ),
                PromptLayer(
                    type = PromptLayerType.TASK_PROMPT,
                    content = taskPrompt,
                    required = false
                ),
                PromptLayer(
                    type = PromptLayerType.USER_OVERRIDE,
                    content = userOverride,
                    required = false
                )
            ),
            tokenBudget = tokenBudget
        )
    }

    suspend fun composeConversationPrompt(
        conversation: Conversation,
        workingMode: ChatWorkingMode,
        userOverride: String? = null,
        taskPrompt: String = "",
        tokenBudget: Int = 0
    ): PromptComposition {
        val systemRules = getSystemPrompt(
            title = PromptEntity.SYSTEM_PROMPT_KEY_DEFAULT_PROMPT,
            fallback = textProvider.rawAsync(com.shifenmiao.database.R.raw.prompt_system_default)
        )
        val agentExecutionProtocol = when (workingMode) {
            ChatWorkingMode.ASK -> getSystemPrompt(
                title = PromptEntity.SYSTEM_PROMPT_KEY_WORKING_MODE_ASK,
                fallback = textProvider.rawAsync(com.shifenmiao.database.R.raw.chat_working_mode_ask)
            )
            ChatWorkingMode.PLAN -> getSystemPrompt(
                title = PromptEntity.SYSTEM_PROMPT_KEY_WORKING_MODE_PLAN,
                fallback = textProvider.rawAsync(com.shifenmiao.database.R.raw.chat_working_mode_plan)
            )
            ChatWorkingMode.AGENT -> getSystemPrompt(
                title = PromptEntity.SYSTEM_PROMPT_KEY_WORKING_MODE_AGENT,
                fallback = textProvider.rawAsync(com.shifenmiao.database.R.raw.chat_working_mode_agent)
            )
        }
        val composition = composeToolAwarePrompt(
            systemRules = systemRules,
            interactionProtocol = "",
            agentExecutionProtocol = agentExecutionProtocol,
            agentRole = conversation.prompt,
            taskPrompt = taskPrompt,
            userOverride = userOverride.orEmpty(),
            environmentContext = environmentContextProvider.buildContextText(),
            tokenBudget = tokenBudget
        )
        return composition
    }

    /**
     * 计算系统 prompt 的 token 预算。
     *
     * 按模型上下文窗口大小分档：
     * - ≤16K：25%（小窗口模型需为对话历史保留更多空间）
     * - ≤64K：20%
     * - ≤128K：15%
     * - >128K：固定 8K（大窗口模型无需更多 prompt）
     */
    fun calculatePromptBudget(model: AiModel): Int {
        val contextWindow = model.effectiveContextWindow()
        return when {
            contextWindow <= 16_384 -> (contextWindow * 0.25).toInt()
            contextWindow <= 65_536 -> (contextWindow * 0.20).toInt()
            contextWindow <= 131_072 -> (contextWindow * 0.15).toInt()
            else -> 8192
        }
    }
}


