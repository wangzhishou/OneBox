package com.shifenmiao.ai.agent.tool.builtin

import com.google.gson.Gson
import com.shifenmiao.ai.agent.tool.AgentTool
import com.shifenmiao.ai.agent.tool.AgentToolResult
import com.shifenmiao.ai.agent.tool.AgentToolTextProvider
import com.shifenmiao.ai.R
import com.shifenmiao.common.handle.navigation.AppNavigationTargetType
import com.shifenmiao.common.handle.navigation.AppNavigationRegistry
import com.shifenmiao.model.ai.ToolParameterProperty
import com.shifenmiao.model.ai.ToolParameters
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.model.ai.tool.ToolRiskLevel
import com.shifenmiao.model.chess.ChessServiceInterface
import javax.inject.Inject

class ManageChessTool @Inject constructor(
    private val chessService: ChessServiceInterface,
    private val gson: Gson,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "manage_chess"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_manage_chess)

    override val title: String =
        textProvider.string(R.string.agent_tool_manage_chess_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_manage_chess_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_manage_chess_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_manage_chess_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_action),
                enum = listOf("list", "detail", "create", "import_fen", "import_json", "delete", "export")
            ),
            "game_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_game_id)
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_title)
            ),
            "ai_as_white" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_ai_as_white)
            ),
            "export_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_export_format),
                enum = listOf("fen", "json", "both")
            ),
            "fen" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_fen)
            ),
            "json_data" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_chess_param_json_data)
            ),
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = gson.fromJson(arguments, ManageChessParams::class.java)
            when (params.action) {
                "list" -> handleList()
                "detail" -> handleDetail(params)
                "create" -> handleCreate(params)
                "import_fen" -> handleImportFen(params)
                "import_json" -> handleImportJson(params)
                "delete" -> handleDelete(params)
                "export" -> handleExport(params)
                else -> AgentToolResult(
                    content = textProvider.string(
                        R.string.agent_tool_manage_chess_unknown_action,
                        params.action
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_chess_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(): AgentToolResult {
        val games = chessService.listGames()
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
            "deepLinks" to listOf(chessDeepLink())
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleDetail(params: ManageChessParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_chess_missing_game_id)
        }
        val detail = chessService.getGameDetail(gameId)
            ?: return errorResult(R.string.agent_tool_manage_chess_not_found, gameId)

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
                chessDeepLink(
                    label = "查看对局",
                    guidance = "打开当前对局局面",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId, "type" to "analysis"),
                )
            )
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleCreate(params: ManageChessParams): AgentToolResult {
        val title = params.title
        if (title.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_chess_missing_title)
        }
        val aiSide = params.ai_as_white
        val gameResult = if (aiSide != null) {
            chessService.createAiGame(title, aiSide)
        } else {
            chessService.createLocalGame(title)
        }
        val gameId = gameResult.getOrElse {
            return errorResult(R.string.agent_tool_manage_chess_failed, it.message ?: "unknown")
        }
        val result = mapOf(
            "action" to "create",
            "success" to true,
            "game_id" to gameId,
            "title" to title,
            "mode" to if (aiSide != null) "HUMAN_VS_LLM" else "LOCAL_PVP",
            "message" to textProvider.string(R.string.agent_tool_manage_chess_created, title),
            "deepLinks" to listOf(
                chessDeepLink(
                    label = "打开对局",
                    guidance = "立即开始新创建的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleImportFen(params: ManageChessParams): AgentToolResult {
        val fen = params.fen
        if (fen.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_chess_missing_fen)
        }
        val title = params.title ?: textProvider.string(R.string.agent_tool_manage_chess_imported_fen)
        val gameId = chessService.importFen(title, fen).getOrElse {
            return errorResult(R.string.agent_tool_manage_chess_import_failed, it.message ?: "invalid FEN")
        }
        val result = mapOf(
            "action" to "import_fen",
            "success" to true,
            "game_id" to gameId,
            "title" to title,
            "message" to textProvider.string(R.string.agent_tool_manage_chess_imported, title),
            "deepLinks" to listOf(
                chessDeepLink(
                    label = "打开对局",
                    guidance = "查看导入的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleImportJson(params: ManageChessParams): AgentToolResult {
        val jsonData = params.json_data
        if (jsonData.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_chess_missing_json)
        }
        val title = params.title ?: ""
        val importResult = chessService.importJson(title, jsonData).getOrElse {
            return errorResult(R.string.agent_tool_manage_chess_import_failed, it.message ?: "invalid JSON")
        }
        val gameId = importResult.gameId
        val result = mapOf(
            "action" to "import_json",
            "success" to true,
            "game_id" to gameId,
            "imported_plies" to importResult.importedPlies,
            "skipped_plies" to importResult.skippedPlies,
            "message" to textProvider.string(R.string.agent_tool_manage_chess_imported, gameId),
            "deepLinks" to listOf(
                chessDeepLink(
                    label = "打开对局",
                    guidance = "查看导入的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleDelete(params: ManageChessParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_chess_missing_game_id)
        }
        // 先获取标题用于返回消息
        val detail = chessService.getGameDetail(gameId)
        val gameTitle = detail?.title ?: gameId

        chessService.deleteGame(gameId).getOrElse {
            return errorResult(R.string.agent_tool_manage_chess_failed, it.message ?: "unknown")
        }
        val result = mapOf(
            "action" to "delete",
            "success" to true,
            "game_id" to gameId,
            "title" to gameTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_chess_deleted, gameTitle),
            "deepLinks" to listOf(
                chessDeepLink(
                    label = "返回棋谱列表",
                    guidance = "继续浏览其他对局",
                    primary = false,
                )
            )
        )
        return AgentToolResult(content = gson.toJson(result))
    }

    private suspend fun handleExport(params: ManageChessParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_chess_missing_game_id)
        }
        val format = params.export_format ?: "both"
        val fen = if (format == "fen" || format == "both") chessService.exportFen(gameId) else null
        val json = if (format == "json" || format == "both") chessService.exportJson(gameId) else null

        if ((fen.isNullOrEmpty()) && (json.isNullOrEmpty())) {
            return errorResult(R.string.agent_tool_manage_chess_not_found, gameId)
        }

        val result = mutableMapOf<String, Any?>(
            "action" to "export",
            "success" to true,
            "game_id" to gameId,
            "deepLinks" to listOf(
                chessDeepLink(
                    label = "查看对局",
                    guidance = "在棋盘上回放这局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId, "type" to "analysis"),
                )
            )
        )
        if (!fen.isNullOrEmpty()) result["fen"] = fen
        if (!json.isNullOrEmpty()) result["json_record"] = json
        return AgentToolResult(content = gson.toJson(result))
    }

    // ── helpers ───────────────────────────────────

    private fun errorResult(resId: Int, vararg args: Any): AgentToolResult =
        AgentToolResult(content = textProvider.string(resId, *args), isError = true)

    private fun chessDeepLink(
        label: String = "国际象棋",
        guidance: String? = null,
        primary: Boolean = false,
        extraParams: Array<out Pair<String, String>> = emptyArray(),
    ): Map<String, Any?> = mapOf(
        "uri" to AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = "chess_router",
            params = extraParams.toMap(),
        ),
        "label" to label,
        "guidance" to guidance,
        "primary" to primary,
    )

    private data class ManageChessParams(
        val action: String = "",
        val game_id: String? = null,
        val title: String? = null,
        val ai_as_white: Boolean? = null,
        val export_format: String? = null,
        val fen: String? = null,
        val json_data: String? = null,
    )
}
