package com.wanbaohe.xiangqi.data

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.JevChoiceAnswer
import com.shifenmiao.model.ai.JevRequest
import com.shifenmiao.model.ai.JevResponse
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.JevService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class JevMoveChooser @Inject constructor(
    private val aiEngineManager: AIEngineManager,
    @Named("JevDirectService") private val jevDirectService: JevService,
    @Named("JevProxyService") private val jevProxyService: JevService,
    dispatchersHolder: DispatchersHolder,
) : MoveChooser, DispatchersHolder by dispatchersHolder {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val engine = slot.toEngine()
        val model = engine.model.name.takeIf { it.startsWith("jev") } ?: JevBestMoveResolver.DEFAULT_MODEL
        val url = AiRequestUrlResolver.resolveRequestUrl(engine)
        val service = if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
            jevDirectService
        } else {
            jevProxyService
        }
        val authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine)
        val request = JevBestMoveResolver.buildRequest(fen, boardState.sideToMove, history, legalMoves, model)

        var lastError: String? = null
        repeat(2) {
            val selection = withContext(ioDispatcher) {
                runCatching {
                    val response = service.systemOne(
                        url = url,
                        authorization = authorization,
                        body = request,
                    ).execute()
                    if (!response.isSuccessful) {
                        lastError = "http ${response.code()}"
                        return@runCatching null
                    }
                    val body = response.body()?.string()
                    if (body == null) {
                        lastError = "empty body"
                        return@runCatching null
                    }
                    JevBestMoveResolver.resolve(body, legalMoves).also {
                        if (it == null) lastError = "unmapped choice"
                    }
                }.getOrElse {
                    lastError = it.message ?: "request failed"
                    null
                }
            }
            if (selection != null) {
                return MoveDecision(
                    move = selection.move,
                    reason = "jev conf=%.2f p=%.2f".format(selection.confidence, selection.probability),
                    rawResponse = selection.rawResponse,
                    fallbackUsed = false,
                )
            }
        }

        return HeuristicMoveFallback.decision(legalMoves)
            ?.copy(reason = "jev: ${lastError ?: "failed"}", fallbackUsed = true)
    }

    private fun EngineSlot.toEngine(): AiEngine = when (this) {
        EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
        EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
        EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
    }
}

internal object JevBestMoveResolver {

    const val DEFAULT_MODEL = "jev-latest"
    const val QUESTION_BEST_MOVE = "best_move"

    private val gson = Gson()

    data class Selection(
        val move: XiangqiMove,
        val confidence: Double,
        val probability: Double,
        val rawResponse: String,
    )

    fun buildRequest(
        fen: String,
        side: Side,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        model: String,
    ): JevRequest {
        val state = JsonObject().apply {
            addProperty("game", "中国象棋(Xiangqi)")
            addProperty("board_fen", fen)
            addProperty("side_to_move", if (side == Side.RED) "红方" else "黑方")
            add("recent_moves", JsonArray().apply { history.takeLast(6).forEach(::add) })
            addProperty("task", "当前局面轮到己方走棋,从合法着法中选出最强的一步")
        }
        val criteria = JsonObject().apply {
            legalMoves.forEach { move ->
                val notation = move.notationCn.ifBlank { move.notationUcci }
                addProperty(move.notationUcci, "${pieceLabel(move.piece)},中文记谱:$notation")
            }
        }
        val questions = JsonObject().apply {
            add(QUESTION_BEST_MOVE, JsonObject().apply {
                addProperty("type", "choice")
                addProperty(
                    "instructions",
                    "你是中国象棋高手。根据 FEN 局面,从候选合法着法中选出最强的一步棋。" +
                        "优先考虑:能否将杀对方、能否白吃子或占优交换、避免送子、子力位置。"
                )
                add("criteria", criteria)
            })
        }
        return JevRequest(state = state, model = model, questions = questions)
    }

    fun resolve(rawJson: String, legalMoves: List<XiangqiMove>): Selection? {
        val response = runCatching {
            gson.fromJson(rawJson, JevResponse::class.java)
        }.getOrNull() ?: return null
        val answer = response.answers[QUESTION_BEST_MOVE] ?: return null
        val chosenUcci = selectUcci(answer) ?: return null
        val move = legalMoves.firstOrNull { it.notationUcci == chosenUcci } ?: return null
        return Selection(
            move = move,
            confidence = answer.confidence,
            probability = answer.probabilities[chosenUcci] ?: 0.0,
            rawResponse = rawJson,
        )
    }

    private fun selectUcci(answer: JevChoiceAnswer): String? {
        if (answer.probabilities.isNotEmpty()) {
            return answer.probabilities.maxByOrNull { it.value }?.key
        }
        return answer.choice.takeIf { it.isNotBlank() }
    }

    private fun pieceLabel(piece: Piece): String {
        val sideLabel = if (piece.side == Side.RED) "红" else "黑"
        val name = when (piece.type) {
            PieceType.KING -> if (piece.side == Side.RED) "帅" else "将"
            PieceType.ADVISOR -> if (piece.side == Side.RED) "仕" else "士"
            PieceType.BISHOP -> if (piece.side == Side.RED) "相" else "象"
            PieceType.KNIGHT -> "马"
            PieceType.ROOK -> "车"
            PieceType.CANNON -> "炮"
            PieceType.PAWN -> if (piece.side == Side.RED) "兵" else "卒"
        }
        return "$sideLabel$name"
    }
}
