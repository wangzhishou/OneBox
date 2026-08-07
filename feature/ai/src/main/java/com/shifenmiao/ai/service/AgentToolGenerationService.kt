package com.shifenmiao.ai.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shifenmiao.ai.agent.tool.AgentToolRegistry
import com.shifenmiao.common.ai.AIPromptExecutor
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import javax.inject.Inject
import javax.inject.Singleton

data class AgentToolGenerationRequest(
    val toolPurpose: String,
    val toolNameHint: String? = null,
    val desiredCategory: String? = null,
    val isInteractive: Boolean? = null,
    val inputSchemaNotes: String? = null,
    val outputContractNotes: String? = null,
    val relatedToolNames: List<String> = emptyList()
)

data class AgentToolGenerationResult(
    val isSuccess: Boolean,
    val draft: AgentToolDraft? = null,
    val rawContent: String = "",
    val errorMessage: String? = null,
    val engineName: String = "",
    val modelName: String = ""
)

data class AgentToolDraft(
    val identity: AgentToolDraftIdentity,
    val metadata: AgentToolDraftMetadata,
    val parameterSchema: AgentToolDraftParameterSchema,
    val executionContract: AgentToolDraftExecutionContract,
    val filePlan: AgentToolDraftFilePlan,
    val codeTemplates: AgentToolDraftCodeTemplates,
    val resourceDrafts: AgentToolDraftResourceDrafts,
    val registrationDraft: AgentToolDraftRegistrationDraft,
    val implementationNotes: List<String>
)

data class AgentToolDraftIdentity(
    val toolName: String,
    val className: String,
    val packageName: String,
    val title: String
)

data class AgentToolDraftMetadata(
    val summary: String,
    val description: String,
    val category: String,
    val riskLevel: String,
    val bootstrapModes: List<String>,
    val visibleToUser: Boolean,
    val requiresConfirmation: Boolean,
    val isInteractive: Boolean,
    val sortOrder: Int,
    val keywords: List<String>,
    val examples: List<String>
)

data class AgentToolDraftParameterSchema(
    val required: List<String>,
    val properties: List<AgentToolDraftParameterProperty>
)

data class AgentToolDraftParameterProperty(
    val name: String,
    val type: String,
    val description: String,
    val enumValues: List<String> = emptyList()
)

data class AgentToolDraftExecutionContract(
    val inputParsing: String,
    val validationRules: List<String>,
    val successPayload: String,
    val failurePayload: String
)

data class AgentToolDraftFilePlan(
    val kotlinFilePath: String,
    val rawDescriptionFileName: String,
    val stringsKeys: List<String>,
    val arrayKeys: List<String>
)

data class AgentToolDraftCodeTemplates(
    val toolKotlin: String,
    val hiltBinding: String
)

data class AgentToolDraftResourceDrafts(
    val rawDescription: String,
    val stringEntries: List<AgentToolDraftStringEntry>,
    val arrayEntries: List<AgentToolDraftArrayEntry>
)

data class AgentToolDraftStringEntry(
    val key: String,
    val value: String
)

data class AgentToolDraftArrayEntry(
    val key: String,
    val values: List<String>
)

data class AgentToolDraftRegistrationDraft(
    val moduleClass: String,
    val stringKey: String,
    val provideFunctionName: String,
    val bindingSnippet: String
)

@Singleton
class AgentToolGenerationService @Inject constructor(
    private val aiPromptExecutor: AIPromptExecutor,
    private val agentToolRegistry: AgentToolRegistry,
    private val creationMetaService: CreationMetaService,
    private val gson: Gson
) {

    suspend fun generateDraft(request: AgentToolGenerationRequest): AgentToolGenerationResult {
        val relatedTools = agentToolRegistry.getVisibleTools()
            .filter { tool ->
                request.relatedToolNames.isEmpty() || tool.name in request.relatedToolNames
            }
            .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
            .take(8)

        val result = aiPromptExecutor.execute(
            input = buildUserInput(request, relatedTools),
            systemPrompt = buildSystemPrompt()
        )
        if (!result.isSuccess) {
            return AgentToolGenerationResult(
                isSuccess = false,
                rawContent = result.content,
                errorMessage = result.errorMessage,
                engineName = result.engineName,
                modelName = result.modelName
            )
        }

        val payload = parseDraftPayload(result.content)
            ?: return AgentToolGenerationResult(
                isSuccess = false,
                rawContent = result.content,
                errorMessage = "无法解析工具草案 JSON",
                engineName = result.engineName,
                modelName = result.modelName
            )

        val draft = payload.toolDraft
        val resolvedToolNames = creationMetaService.resolveSuggestedToolNames(
            payload.recommendedBindings.ifEmpty { request.relatedToolNames }
        )

        return AgentToolGenerationResult(
            isSuccess = true,
            draft = draft.copy(
                implementationNotes = draft.implementationNotes +
                    buildList {
                        if (resolvedToolNames.isNotEmpty()) {
                            add("建议联动工具: ${resolvedToolNames.joinToString()}")
                        }
                    }
            ),
            rawContent = result.content,
            engineName = result.engineName,
            modelName = result.modelName
        )
    }

    private fun parseDraftPayload(content: String): AgentToolDraftPayload? {
        if (content.isBlank()) return null
        return runCatching {
            val root = gson.fromJson(content, JsonObject::class.java) ?: return null
            val draftRoot = root.getAsJsonObject("tool_draft") ?: root
            AgentToolDraftPayload(
                toolDraft = gson.fromJson(draftRoot, AgentToolDraft::class.java),
                recommendedBindings = root.getAsJsonArray("recommended_related_tools")
                    ?.mapNotNull { element ->
                        element?.takeIf { it.isJsonPrimitive }?.asString?.trim()?.takeIf(String::isNotEmpty)
                    }
                    .orEmpty()
            )
        }.getOrNull()
    }

    private fun buildUserInput(
        request: AgentToolGenerationRequest,
        relatedTools: List<ToolCatalogItem>
    ): String {
        return buildString {
            appendLine("目标功能: ${request.toolPurpose}")
            appendLine("工具名提示: ${request.toolNameHint.orEmpty()}")
            appendLine("期望分类: ${request.desiredCategory.orEmpty()}")
            appendLine("是否交互式: ${request.isInteractive?.toString().orEmpty()}")
            appendLine("入参说明: ${request.inputSchemaNotes.orEmpty()}")
            appendLine("返回约定: ${request.outputContractNotes.orEmpty()}")
            appendLine("相关工具提示: ${request.relatedToolNames.joinToString()}")
            appendLine()
            appendLine("当前可参考工具目录（节选）:")
            relatedTools.forEach { tool ->
                appendLine(
                    "- ${tool.name} | ${tool.title} | ${tool.category.name} | " +
                        "interactive=${tool.isInteractive} | risk=${tool.riskLevel.name}"
                )
                appendLine("  summary=${tool.summary}")
                if (tool.keywords.isNotEmpty()) {
                    appendLine("  keywords=${tool.keywords.joinToString()}")
                }
            }
        }
    }

    private fun buildSystemPrompt(): String {
        return """
你是 Android Agent Tool 架构设计助手。请根据需求输出一个“可直接落地的工具草案 JSON”。

只输出 JSON，顶层结构如下：
{
  "tool_draft": {
    "identity": {
      "toolName": "snake_case",
      "className": "PascalCaseTool",
      "packageName": "com.shifenmiao.ai.agent.tool.builtin",
      "title": "工具标题"
    },
    "metadata": {
      "summary": "一句话摘要",
      "description": "面向 LLM 的详细描述",
      "category": "SYSTEM|DEVICE|FORM|FILE|KNOWLEDGE|NETWORK|BUSINESS|IMAGE",
      "riskLevel": "SAFE|SENSITIVE|DANGEROUS",
      "bootstrapModes": ["ASK", "PLAN", "AGENT"],
      "visibleToUser": true,
      "requiresConfirmation": false,
      "isInteractive": false,
      "sortOrder": 0,
      "keywords": ["关键词1", "关键词2"],
      "examples": ["示例1", "示例2"]
    },
    "parameterSchema": {
      "required": ["field_a"],
      "properties": [
        {
          "name": "field_a",
          "type": "string",
          "description": "参数说明",
          "enumValues": []
        }
      ]
    },
    "executionContract": {
      "inputParsing": "如何解析 arguments",
      "validationRules": ["校验规则1"],
      "successPayload": "成功时返回什么 JSON/文本",
      "failurePayload": "失败时返回什么错误"
    },
    "filePlan": {
      "kotlinFilePath": "feature/ai/src/main/java/.../MyTool.kt",
      "rawDescriptionFileName": "agent_tool_description_xxx.txt",
      "stringsKeys": ["agent_tool_xxx_title"],
      "arrayKeys": ["agent_tool_xxx_keywords"]
    },
    "codeTemplates": {
      "toolKotlin": "完整 Kotlin 类草案",
      "hiltBinding": "BuiltinToolModule 中的 @Provides 绑定片段"
    },
    "resourceDrafts": {
      "rawDescription": "写入 raw 文本的详细描述",
      "stringEntries": [{"key":"k","value":"v"}],
      "arrayEntries": [{"key":"k","values":["a","b"]}]
    },
    "registrationDraft": {
      "moduleClass": "BuiltinToolModule",
      "stringKey": "tool_name",
      "provideFunctionName": "provideXxxTool",
      "bindingSnippet": "@Provides ..."
    },
    "implementationNotes": ["实现注意点1", "实现注意点2"]
  },
  "recommended_related_tools": ["discover_tools", "discover_apps"]
}

要求：
1. 产出必须符合当前 Android/Hilt/AgentTool 架构。
2. `toolName` 必须 snake_case，`className` 以 Tool 结尾。
3. Kotlin 草案必须基于 `AgentTool` 或 `InteractiveAgentTool`。
4. 默认放到 `feature/ai/.../agent/tool/builtin/`，除非场景明确要求其他模块。
5. 描述、keywords、examples、strings key、arrays key、raw file 名都要完整。
6. `bootstrapModes` 表示该工具在哪些工作模式下会进入首轮 tools；大多数普通工具应返回空数组，仅 discover_tools / discover_apps / route 类工具才进入首轮。
7. 如果工具有副作用，优先把 riskLevel 提高，并决定是否 requiresConfirmation。
8. 优先推荐低风险、可组合、可查询优先的工具设计，不要把职责做得过大。
9. 不要输出 Markdown，不要省略字段。
""".trimIndent()
    }

    private data class AgentToolDraftPayload(
        val toolDraft: AgentToolDraft,
        val recommendedBindings: List<String>
    )
}
