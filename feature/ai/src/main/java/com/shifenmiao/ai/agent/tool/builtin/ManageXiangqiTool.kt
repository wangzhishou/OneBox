package com.shifenmiao.ai.agent.tool.builtin

import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.model.jsonStringOf
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.xiangqi.XiangqiServiceInterface
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ManageXiangqiTool @Inject constructor(
    private val xiangqiService: XiangqiServiceInterface,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "manage_xiangqi"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_manage_xiangqi)

    override val title: String =
        textProvider.string(R.string.agent_tool_manage_xiangqi_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_manage_xiangqi_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_manage_xiangqi_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_manage_xiangqi_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_action),
                enum = listOf("list", "detail", "create", "import_preset", "import_fen", "import_json", "delete", "export")
            ),
            "game_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_game_id)
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_title)
            ),
            "ai_as_red" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_ai_as_red)
            ),
            "export_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_export_format),
                enum = listOf("fen", "json", "both")
            ),
            "fen" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_fen)
            ),
            "preset" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_preset),
                enum = listOf("rook_vs_pawn")
            ),
            "json_data" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_xiangqi_param_json_data)
            ),
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = AppJson.decodeFromString<ManageXiangqiParams>(arguments)
            when (params.action) {
                "list" -> handleList()
                "detail" -> handleDetail(params)
                "create" -> when {
                    !params.preset.isNullOrBlank() -> handleImportPreset(params)
                    !params.fen.isNullOrBlank() -> handleImportFen(params)
                    else -> handleCreate(params)
                }
                "import_preset" -> handleImportPreset(params)
                "import_fen" -> handleImportFen(params)
                "import_json" -> handleImportJson(params)
                "delete" -> handleDelete(params)
                "export" -> handleExport(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_manage_xiangqi_unknown_action,
                        params.action
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_xiangqi_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(): AgentToolResult {
        val games = xiangqiService.listGames()
        val data = games.map { game ->
            mapOf(
                "id" to game.id,
                "title" to game.title,
                "mode" to game.mode,
                "status" to game.status,
                "result" to game.resultText,
                "updated_at" to game.updatedAt,
            )
        }
        val result = mapOf(
            "action" to "list",
            "success" to true,
            "count" to games.size,
            "games" to data,
            "deepLinks" to listOf(xiangqiDeepLink())
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDetail(params: ManageXiangqiParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_game_id)
        }
        val detail = xiangqiService.getGameDetail(gameId)
            ?: return errorResult(R.string.agent_tool_manage_xiangqi_not_found, gameId)

        val movesData = detail.moves.map { move ->
            mapOf(
                "ply" to move.ply,
                "ucci" to move.moveUcci,
                "cn" to move.moveCn,
            )
        }
        val result = mapOf(
            "action" to "detail",
            "success" to true,
            "id" to detail.id,
            "title" to detail.title,
            "mode" to detail.mode,
            "status" to detail.status,
            "initial_fen" to detail.initialFen,
            "current_fen" to detail.currentFen,
            "current_ply" to detail.currentPly,
            "move_count" to detail.moves.size,
            "moves" to movesData,
            "deepLinks" to listOf(
                xiangqiDeepLink(
                    label = "查看对局",
                    guidance = "打开当前对局局面",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId, "type" to "analysis"),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreate(params: ManageXiangqiParams): AgentToolResult {
        val title = params.title
        if (title.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_title)
        }
        // 默认人机对局：不传 ai_as_red 时 AI 执黑、人类执红先行
        val aiAsRed = params.ai_as_red ?: false
        val gameId = xiangqiService.createAiGame(title, aiAsRed).getOrElse {
            return errorResult(R.string.agent_tool_manage_xiangqi_failed, it.message ?: "unknown")
        }
        val result = mapOf(
            "action" to "create",
            "success" to true,
            "game_id" to gameId,
            "title" to title,
            "mode" to "HUMAN_VS_LLM",
            "message" to textProvider.string(R.string.agent_tool_manage_xiangqi_created, title),
            "deepLinks" to listOf(
                xiangqiDeepLink(
                    label = "打开对局",
                    guidance = "立即开始新创建的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleImportFen(params: ManageXiangqiParams): AgentToolResult {
        val fen = params.fen
        if (fen.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_fen)
        }
        val title = params.title ?: textProvider.string(R.string.agent_tool_manage_xiangqi_imported_fen)
        // 残局默认人机对局：不传 ai_as_red 时人类执当前回合方
        val gameId = xiangqiService.importFenAsAiGame(title, fen, params.ai_as_red).getOrElse {
            return errorResult(R.string.agent_tool_manage_xiangqi_import_failed, it.message ?: "invalid FEN")
        }
        val result = mapOf(
            "action" to "import_fen",
            "success" to true,
            "game_id" to gameId,
            "title" to title,
            "mode" to "HUMAN_VS_LLM",
            "message" to textProvider.string(R.string.agent_tool_manage_xiangqi_imported, title),
            "deepLinks" to listOf(
                xiangqiDeepLink(
                    label = "打开对局",
                    guidance = "查看导入的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleImportPreset(params: ManageXiangqiParams): AgentToolResult {
        val key = params.preset
        if (key.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_preset)
        }
        val fen = PRESET_ENDGAMES[key]
            ?: return errorResult(R.string.agent_tool_manage_xiangqi_unknown_preset, key)
        return handleImportFen(params.copy(fen = fen, title = params.title ?: presetTitle(key)))
    }

    private fun presetTitle(key: String): String = when (key) {
        "rook_vs_pawn" -> textProvider.string(R.string.agent_tool_manage_xiangqi_preset_rook_vs_pawn_title)
        else -> key
    }

    private suspend fun handleImportJson(params: ManageXiangqiParams): AgentToolResult {
        val jsonData = params.json_data
        if (jsonData.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_json)
        }
        val title = params.title ?: ""
        val gameId = xiangqiService.importJson(title, jsonData).getOrElse {
            return errorResult(R.string.agent_tool_manage_xiangqi_import_failed, it.message ?: "invalid JSON")
        }
        val result = mapOf(
            "action" to "import_json",
            "success" to true,
            "game_id" to gameId,
            "message" to textProvider.string(R.string.agent_tool_manage_xiangqi_imported, gameId),
            "deepLinks" to listOf(
                xiangqiDeepLink(
                    label = "打开对局",
                    guidance = "查看导入的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDelete(params: ManageXiangqiParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_game_id)
        }
        // 先获取标题用于返回消息
        val detail = xiangqiService.getGameDetail(gameId)
        val gameTitle = detail?.title ?: gameId

        xiangqiService.deleteGame(gameId).getOrElse {
            return errorResult(R.string.agent_tool_manage_xiangqi_failed, it.message ?: "unknown")
        }
        val result = mapOf(
            "action" to "delete",
            "success" to true,
            "game_id" to gameId,
            "title" to gameTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_xiangqi_deleted, gameTitle),
            "deepLinks" to listOf(
                xiangqiDeepLink(
                    label = "返回棋谱列表",
                    guidance = "继续浏览其他对局",
                    primary = false,
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleExport(params: ManageXiangqiParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_xiangqi_missing_game_id)
        }
        val format = params.export_format ?: "both"
        val fen = if (format == "fen" || format == "both") xiangqiService.exportFen(gameId) else null
        val json = if (format == "json" || format == "both") xiangqiService.exportJson(gameId) else null

        if ((fen.isNullOrEmpty()) && (json.isNullOrEmpty())) {
            return errorResult(R.string.agent_tool_manage_xiangqi_not_found, gameId)
        }

        val result = mutableMapOf<String, Any?>(
            "action" to "export",
            "success" to true,
            "game_id" to gameId,
            "deepLinks" to listOf(
                xiangqiDeepLink(
                    label = "查看对局",
                    guidance = "在棋盘上回放这局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId, "type" to "analysis"),
                )
            )
        )
        if (!fen.isNullOrEmpty()) result["fen"] = fen
        if (!json.isNullOrEmpty()) result["json_record"] = json
        return AgentToolResult(content = jsonStringOf(result))
    }

    // ── helpers ───────────────────────────────────

    private fun errorResult(resId: Int, vararg args: Any): AgentToolResult =
        AgentToolResult(content = textProvider.string(resId, *args), isError = true)

    private fun xiangqiDeepLink(
        label: String = "中国象棋",
        guidance: String? = null,
        primary: Boolean = false,
        extraParams: Array<out Pair<String, String>> = emptyArray(),
    ): Map<String, Any?> = mapOf(
        "uri" to AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = "xiangqi_router",
            params = extraParams.toMap(),
        ),
        "label" to label,
        "guidance" to guidance,
        "primary" to primary,
    )

    private companion object {
        // 内置残局：preset 名 -> FEN，标题按系统语言走 presetTitle()
        val PRESET_ENDGAMES = mapOf(
            "rook_vs_pawn" to "4k4/9/9/9/9/4p4/9/9/3R5/4K4 w 0 1",
        )
    }

    @Serializable
    private data class ManageXiangqiParams(
        val action: String = "",
        val game_id: String? = null,
        val title: String? = null,
        val ai_as_red: Boolean? = null,
        val export_format: String? = null,
        val fen: String? = null,
        val json_data: String? = null,
        val preset: String? = null,
    )
}
