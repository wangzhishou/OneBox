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
import com.shifenmiao.model.gomoku.GomokuServiceInterface
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString

class ManageGomokuTool @Inject constructor(
    private val gomokuService: GomokuServiceInterface,
    private val textProvider: AgentToolTextProvider,
) : AgentTool {

    override val name: String = "manage_gomoku"

    override val description: String =
        textProvider.raw(R.raw.agent_tool_description_manage_gomoku)

    override val title: String =
        textProvider.string(R.string.agent_tool_manage_gomoku_title)

    override val summary: String =
        textProvider.string(R.string.agent_tool_manage_gomoku_summary)

    override val category: ToolCategory = ToolCategory.BUSINESS

    override val riskLevel: ToolRiskLevel = ToolRiskLevel.SAFE

    override val keywords: List<String> =
        textProvider.array(R.array.agent_tool_manage_gomoku_keywords)

    override val examples: List<String> =
        textProvider.array(R.array.agent_tool_manage_gomoku_examples)

    override val parametersSchema: ToolParameters = ToolParameters(
        type = "object",
        properties = mapOf(
            "action" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_action),
                enum = listOf("list", "detail", "create", "import_preset", "import_fen", "import_json", "delete", "export")
            ),
            "game_id" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_game_id)
            ),
            "title" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_title)
            ),
            "ai_as_black" to ToolParameterProperty(
                type = "boolean",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_ai_as_black)
            ),
            "export_format" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_export_format),
                enum = listOf("fen", "json", "both")
            ),
            "fen" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_fen)
            ),
            "preset" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_preset),
                enum = listOf("center_two_black")
            ),
            "json_data" to ToolParameterProperty(
                type = "string",
                description = textProvider.string(R.string.agent_tool_manage_gomoku_param_json_data)
            ),
        ),
        required = listOf("action")
    )

    override suspend fun execute(arguments: String): AgentToolResult {
        return try {
            val params = AppJson.decodeFromString<ManageGomokuParams>(arguments)
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
                        R.string.agent_tool_manage_gomoku_unknown_action,
                        params.action
                    ),
                    isError = true
                )
            }
        } catch (e: Exception) {
            AgentToolResult(
                content = textProvider.string(
                    R.string.agent_tool_manage_gomoku_failed,
                    e.message ?: textProvider.string(R.string.agent_tool_unknown_error)
                ),
                isError = true
            )
        }
    }

    // ── action handlers ──────────────────────────────

    private suspend fun handleList(): AgentToolResult {
        val games = gomokuService.listGames()
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
            "deepLinks" to listOf(gomokuDeepLink())
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDetail(params: ManageGomokuParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_game_id)
        }
        val detail = gomokuService.getGameDetail(gameId)
            ?: return errorResult(R.string.agent_tool_manage_gomoku_not_found, gameId)

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
                gomokuDeepLink(
                    label = "查看对局",
                    guidance = "打开当前对局局面",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId, "type" to "analysis"),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleCreate(params: ManageGomokuParams): AgentToolResult {
        val title = params.title
        if (title.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_title)
        }
        // 默认人机对局：不传 ai_as_black 时 AI 执白、人类执黑先行
        val aiAsBlack = params.ai_as_black ?: false
        val gameId = gomokuService.createAiGame(title, aiAsBlack).getOrElse {
            return errorResult(R.string.agent_tool_manage_gomoku_failed, it.message ?: "unknown")
        }
        val result = mapOf(
            "action" to "create",
            "success" to true,
            "game_id" to gameId,
            "title" to title,
            "mode" to "HUMAN_VS_LLM",
            "message" to textProvider.string(R.string.agent_tool_manage_gomoku_created, title),
            "deepLinks" to listOf(
                gomokuDeepLink(
                    label = "打开对局",
                    guidance = "立即开始新创建的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleImportFen(params: ManageGomokuParams): AgentToolResult {
        val fen = params.fen
        if (fen.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_fen)
        }
        val title = params.title ?: textProvider.string(R.string.agent_tool_manage_gomoku_imported_fen)
        // 残局默认人机对局：不传 ai_as_black 时人类执当前回合方
        val gameId = gomokuService.importFenAsAiGame(title, fen, params.ai_as_black).getOrElse {
            return errorResult(R.string.agent_tool_manage_gomoku_import_failed, it.message ?: "invalid FEN")
        }
        val result = mapOf(
            "action" to "import_fen",
            "success" to true,
            "game_id" to gameId,
            "title" to title,
            "mode" to "HUMAN_VS_LLM",
            "message" to textProvider.string(R.string.agent_tool_manage_gomoku_imported, title),
            "deepLinks" to listOf(
                gomokuDeepLink(
                    label = "打开对局",
                    guidance = "查看导入的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleImportPreset(params: ManageGomokuParams): AgentToolResult {
        val key = params.preset
        if (key.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_preset)
        }
        val fen = PRESET_ENDGAMES[key]
            ?: return errorResult(R.string.agent_tool_manage_gomoku_unknown_preset, key)
        return handleImportFen(params.copy(fen = fen, title = params.title ?: presetTitle(key)))
    }

    private fun presetTitle(key: String): String = when (key) {
        "center_two_black" -> textProvider.string(R.string.agent_tool_manage_gomoku_preset_center_two_black_title)
        else -> key
    }

    private suspend fun handleImportJson(params: ManageGomokuParams): AgentToolResult {
        val jsonData = params.json_data
        if (jsonData.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_json)
        }
        val title = params.title ?: ""
        val importResult = gomokuService.importJson(title, jsonData).getOrElse {
            return errorResult(R.string.agent_tool_manage_gomoku_import_failed, it.message ?: "invalid JSON")
        }
        val gameId = importResult.gameId
        val result = mapOf(
            "action" to "import_json",
            "success" to true,
            "game_id" to gameId,
            "imported_plies" to importResult.importedPlies,
            "skipped_plies" to importResult.skippedPlies,
            "message" to textProvider.string(R.string.agent_tool_manage_gomoku_imported, gameId),
            "deepLinks" to listOf(
                gomokuDeepLink(
                    label = "打开对局",
                    guidance = "查看导入的对局",
                    primary = true,
                    extraParams = arrayOf("game_id" to gameId),
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleDelete(params: ManageGomokuParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_game_id)
        }
        // 先获取标题用于返回消息
        val detail = gomokuService.getGameDetail(gameId)
        val gameTitle = detail?.title ?: gameId

        gomokuService.deleteGame(gameId).getOrElse {
            return errorResult(R.string.agent_tool_manage_gomoku_failed, it.message ?: "unknown")
        }
        val result = mapOf(
            "action" to "delete",
            "success" to true,
            "game_id" to gameId,
            "title" to gameTitle,
            "message" to textProvider.string(R.string.agent_tool_manage_gomoku_deleted, gameTitle),
            "deepLinks" to listOf(
                gomokuDeepLink(
                    label = "返回棋谱列表",
                    guidance = "继续浏览其他对局",
                    primary = false,
                )
            )
        )
        return AgentToolResult(content = jsonStringOf(result))
    }

    private suspend fun handleExport(params: ManageGomokuParams): AgentToolResult {
        val gameId = params.game_id
        if (gameId.isNullOrBlank()) {
            return errorResult(R.string.agent_tool_manage_gomoku_missing_game_id)
        }
        val format = params.export_format ?: "both"
        val fen = if (format == "fen" || format == "both") gomokuService.exportFen(gameId) else null
        val json = if (format == "json" || format == "both") gomokuService.exportJson(gameId) else null

        if ((fen.isNullOrEmpty()) && (json.isNullOrEmpty())) {
            return errorResult(R.string.agent_tool_manage_gomoku_not_found, gameId)
        }

        val result = mutableMapOf<String, Any?>(
            "action" to "export",
            "success" to true,
            "game_id" to gameId,
            "deepLinks" to listOf(
                gomokuDeepLink(
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

    private fun gomokuDeepLink(
        label: String = "五子棋",
        guidance: String? = null,
        primary: Boolean = false,
        extraParams: Array<out Pair<String, String>> = emptyArray(),
    ): Map<String, Any?> = mapOf(
        "uri" to AppNavigationRegistry.buildStructuredDeeplink(
            targetType = AppNavigationTargetType.SCREEN,
            routeKey = "gomoku_router",
            params = extraParams.toMap(),
        ),
        "label" to label,
        "guidance" to guidance,
        "primary" to primary,
    )

    private companion object {
        // 内置残局：preset 名 -> FEN，标题按系统语言走 presetTitle()
        val PRESET_ENDGAMES = mapOf(
            "center_two_black" to "15/15/15/15/15/15/15/7bb6/7w7/15/15/15/15/15/15 b 4",
        )
    }

    @Serializable
    private data class ManageGomokuParams(
        val action: String = "",
        val game_id: String? = null,
        val title: String? = null,
        val ai_as_black: Boolean? = null,
        val export_format: String? = null,
        val fen: String? = null,
        val json_data: String? = null,
        val preset: String? = null,
    )
}
