package com.wanbaohe.xiangqi.data

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.JevChoiceAnswer
import com.shifenmiao.model.ai.JevRequest
import com.shifenmiao.model.ai.JevResponse
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.JevService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.UcciNotation
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** 象棋通用子力价值。将/帅不入表(不会被吃, 也没有交换价值), 用时按最大算。 */
private val PIECE_VALUES: Map<PieceType, Double> = mapOf(
    PieceType.ROOK to 9.0,
    PieceType.CANNON to 4.5,
    PieceType.KNIGHT to 4.0,
    PieceType.BISHOP to 2.0,
    PieceType.ADVISOR to 2.0,
    PieceType.PAWN to 1.0,
)

private fun PieceType.sortValue(): Double = PIECE_VALUES[this] ?: 100.0

/** 4.5 -> "4.5", 9.0 -> "9" */
private fun PieceType.valueText(): String {
    val value = PIECE_VALUES[this] ?: return "?"
    return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
}

private fun Side.englishLabel(): String = if (this == Side.RED) "Red" else "Black"

private fun Piece.englishLabel(): String = "${side.englishLabel()} ${type.englishName()}"

/**
 * 英文子力名沿用象棋通用译名(chariot/cannon/horse/elephant/advisor/soldier),
 * 与 [com.wanbaohe.xiangqi.domain.ChineseNotationFormatter] 的中文名一一对应。
 */
private fun PieceType.englishName(): String = when (this) {
    PieceType.KING -> "general"
    PieceType.ADVISOR -> "advisor"
    PieceType.BISHOP -> "elephant"
    PieceType.KNIGHT -> "horse"
    PieceType.ROOK -> "chariot"
    PieceType.CANNON -> "cannon"
    PieceType.PAWN -> "soldier"
}

/**
 * Jev(System One)走棋。
 *
 * **全部合法着法都发给 Jev**, 不做候选裁剪 —— 靠的是每个候选都带上可比较的局面事实
 * (吃子/被吃/将军/威胁)。只给「红炮,中文记谱:炮二平五」时 40+ 个选项毫无区分度,
 * 概率会被摊平(实测 top p 只有 0.09); 补上事实后同样的全量候选就能拉开差距
 * (实测 ply6 top p 0.63)。
 *
 * 之所以留全量: 裁剪(本地战术打分 + Pikafish 兜底名额)虽然短期能提分, 但候选集会被
 * "威胁"口径带偏 —— 实测同一门炮往同一方向挪 1~6 格会霸占名额, 观感上就成了反复动同一个子;
 * 也让 Jev 自己的判断失去了覆盖面。等 Jev 进化(更强的局面理解)收益更直接。
 */
@Singleton
class JevMoveChooser @Inject constructor(
    @Named("JevDirectService") private val jevDirectService: JevService,
    @Named("JevProxyService") private val jevProxyService: JevService,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val engine = AiEngine.builtInEngine(AiProvider.Jev)
        val model = engine.model.name.takeIf { it.startsWith("jev") } ?: JevBestMoveResolver.DEFAULT_MODEL
        val url = AiRequestUrlResolver.resolveRequestUrl(engine)
        val service = if (AiRequestUrlResolver.shouldUseDirectRequest(engine)) {
            jevDirectService
        } else {
            jevProxyService
        }
        val authorization = AiRequestUrlResolver.resolveAuthorizationHeader(engine)

        // 每个候选都要推演对方应手, 属 CPU 密集操作, 不能在主线程上算
        // (componentScope 是 Dispatchers.Main.immediate)
        val (facts, inDanger) = withContext(defaultDispatcher) {
            legalMoves.map { JevBestMoveResolver.analyze(boardState, it) } to
                JevBestMoveResolver.piecesInDanger(boardState)
        }
        val request = JevBestMoveResolver.buildRequest(
            fen = fen,
            boardState = boardState,
            history = history,
            candidates = facts,
            inDanger = inDanger,
            model = model,
        )

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

        return HeuristicMoveFallback.decision(boardState, legalMoves)
            ?.withFailureReason("jev: ${lastError ?: "failed"}")
    }
}

internal object JevBestMoveResolver {

    const val DEFAULT_MODEL = "jev-latest"
    const val QUESTION_BEST_MOVE = "best_move"

    /** 每个候选最多描述几个威胁(多一个就多一次着法推演) */
    private const val MAX_THREATS = 2

    private val gson = Gson()

    data class Selection(
        val move: XiangqiMove,
        val confidence: Double,
        val probability: Double,
        val rawResponse: String,
    )

    /**
     * 一步合法着法的局面事实。英文描述只用这一份数据, 每步只推演一次。
     */
    data class CandidateFacts(
        val move: XiangqiMove,
        val givesCheck: Boolean,
        /** 对方一步都走不出来(将杀或困毙) */
        val opponentHasNoReply: Boolean,
        /** 对方能吃掉落点这枚子的最便宜一步; null = 没人能吃 */
        val cheapestAttacker: XiangqiMove?,
        /** 会被吃, 但能吃回来 */
        val defended: Boolean,
        /** 落到新位置后能吃到的目标(最多两个, 按价值降序) */
        val threats: List<Threat>,
    ) {
        val isCheckmate: Boolean get() = opponentHasNoReply && givesCheck

        val isHanging: Boolean get() = cheapestAttacker != null && !defended
    }

    /**
     * 落点这枚子下一步能吃掉的目标。
     *
     * [defended] 决定措辞强弱: 吃掉一个有人保护的目标只是"攻击", 不是收益 ——
     * 否则同一门炮会在原地来回挪动时一直重复宣传同一个吃不到的威胁, 把模型钉死在这个子上。
     */
    data class Threat(
        val target: Piece,
        val square: BoardPoint,
        val defended: Boolean,
    )

    /**
     * 当前局面里"被攻击且没人保护"的己方子: 真正需要处理的问题。
     *
     * 候选描述清一色是"我能威胁什么", 模型看不到自己的危险(实测对局里它一步都不回防),
     * 所以把危险单独列进 state, 并给"把这枚子走开"的候选加一句解围说明。
     */
    data class DangerPiece(
        val piece: Piece,
        val square: BoardPoint,
        val attacker: Piece,
        val attackerSquare: BoardPoint,
    ) {
        fun text(): String =
            "${piece.englishLabel()} on ${UcciNotation.point(square)} is attacked by the " +
                "${attacker.englishLabel()} on ${UcciNotation.point(attackerSquare)} " +
                "and nothing defends it"
    }

    /**
     * 题型说明。**必须用英文**: TypeSafe 官方明确 Jev 英语优先,
     * 中日韩文字"可以处理但准确率较低"。
     */
    private const val BEST_MOVE_INSTRUCTIONS =
        "You are a strong Chinese Chess (Xiangqi) player. Each option is one legal move for the " +
            "side to move, written in English and stating: what it takes, " +
            "whether it gives check, whether the moving piece is safe on its destination square, " +
            "and what that piece threatens next. Choose the strongest move. Weigh them roughly in " +
            "this order: checkmate > a capture or threat that wins material while staying safe > " +
            "a move that keeps your pieces protected and active > a capture that loses material > " +
            "a move flagged HANGING (the piece can be taken next move with nothing recapturing), " +
            "which loses material for nothing. If state lists pieces of yours in danger, prefer a " +
            "move that saves or defends them. Do not shuffle one piece back and forth: moving the " +
            "same piece again is only good when it wins material, escapes an attack or answers a " +
            "check. Coordinates use UCCI: files a-i from Red's left, ranks 0-9 with rank 0 on " +
            "Red's home rank."

    /**
     * 推演一步着法之后的事实(吃子 / 将军 / 落点是否被吃被保 / 下一步威胁什么)。
     *
     * 「落点是否安全」与「威胁」都要看对方应手才能得出, 模型自己从 FEN 推不出来;
     * 它们也是分开一堆"同样安全、同样不吃子"的候选的关键。
     */
    fun analyze(board: BoardState, move: XiangqiMove): CandidateFacts {
        val mover = move.piece.side
        val after = board.withPieceMoved(move)
        val opponent = after.sideToMove
        val replies = GameArbiter.legalMoves(after)
        val givesCheck = GameArbiter.isInCheck(after, opponent)

        // 对方能否吃掉落点上的这枚子; 能的话我们能否吃回来(受保护)
        val cheapest = replies
            .filter { it.to == move.to }
            .minByOrNull { it.piece.type.sortValue() }
        val defended = cheapest != null &&
            GameArbiter.legalMoves(after.withPieceMoved(cheapest)).any { it.to == move.to }

        // 把"能吃将/帅"排除掉: 那是将军(已单列), 不是威胁
        val threats = GameArbiter.legalMoves(after, mover)
            .filter {
                it.from == move.to && it.captured != null &&
                    it.captured!!.type != PieceType.KING
            }
            .distinctBy { it.to }
            .sortedByDescending { it.captured!!.type.sortValue() }
            .take(MAX_THREATS)
            .map { capture ->
                // 目标有人保护 = 吃了会被吃回, 只能算"攻击", 不能宣传成收益
                val targetDefended = GameArbiter.legalMoves(after.withPieceMoved(capture), opponent)
                    .any { it.to == capture.to }
                Threat(
                    target = capture.captured!!,
                    square = capture.to,
                    defended = targetDefended,
                )
            }

        return CandidateFacts(
            move = move,
            givesCheck = givesCheck,
            opponentHasNoReply = replies.isEmpty(),
            cheapestAttacker = cheapest,
            defended = defended,
            threats = threats,
        )
    }

    /**
     * 列出当前局面里被攻击且没人保护的己方子。
     *
     * 一次推演覆盖全部己方子(对方能吃到的格子 + 逐个判断能不能吃回), 与候选数无关。
     */
    fun piecesInDanger(board: BoardState): List<DangerPiece> {
        val us = board.sideToMove
        return GameArbiter.legalMoves(board, us.opposite())
            .filter { it.captured != null && it.captured!!.type != PieceType.KING }
            .groupBy { it.to }
            .mapNotNull { (square, captures) ->
                val cheapest = captures.minByOrNull { it.piece.type.sortValue() }
                    ?: return@mapNotNull null
                val target = cheapest.captured ?: return@mapNotNull null
                val defended = GameArbiter.legalMoves(board.withPieceMoved(cheapest), us)
                    .any { it.to == square }
                if (defended) {
                    null
                } else {
                    DangerPiece(target, square, cheapest.piece, cheapest.from)
                }
            }
    }

    /**
     * 组装 System One 请求: `criteria` 覆盖**全部**合法着法, 每个都带局面事实。
     *
     * 早期版本描述只有「红炮,中文记谱:炮二平五」, 20~44 个候选彼此不可区分,
     * 实测 top p 只有 0.09~0.34, argmax 近似随机落子; 补上事实后才拉开差距。
     */
    fun buildRequest(
        fen: String,
        boardState: BoardState,
        history: List<String>,
        candidates: List<CandidateFacts>,
        inDanger: List<DangerPiece> = emptyList(),
        model: String,
    ): JevRequest {
        val side = boardState.sideToMove
        val state = JsonObject().apply {
            addProperty("game", "Chinese Chess (Xiangqi)")
            addProperty("board_fen", fen)
            addProperty("side_to_move", side.englishLabel())
            add("recent_moves", JsonArray().apply { history.takeLast(6).forEach(::add) })
            add("piece_values", JsonObject().apply {
                PIECE_VALUES.forEach { (type, value) -> addProperty(type.englishName(), value) }
            })
            addProperty("legal_move_count", candidates.size)
            add("your_pieces_in_danger", JsonArray().apply {
                inDanger.forEach { add(it.text()) }
            })
            addProperty(
                "task",
                "The options below are all ${candidates.size} legal moves for the side to move. " +
                    "Pick the strongest one.",
            )
        }
        val criteria = JsonObject().apply {
            candidates.forEach { addProperty(it.move.notationUcci, describe(it, inDanger)) }
        }
        val questions = JsonObject().apply {
            add(QUESTION_BEST_MOVE, JsonObject().apply {
                addProperty("type", "choice")
                addProperty("instructions", BEST_MOVE_INSTRUCTIONS)
                add("criteria", criteria)
            })
        }
        return JevRequest(state = state, model = model, questions = questions)
    }

    /**
     * 把事实翻译成 Jev 能横向比较的英文短句(吃子 / 将军 / 落点安全 / 下一步威胁)。
     */
    fun describe(facts: CandidateFacts, inDanger: List<DangerPiece> = emptyList()): String {
        val move = facts.move
        val opponent = move.piece.side.opposite()

        val capture = move.captured?.let { captured ->
            "takes the ${captured.englishLabel()} on ${UcciNotation.point(move.to)}" +
                " (${captured.type.valueText()})"
        } ?: "quiet move"

        val check = when {
            facts.isCheckmate ->
                "checkmate, ${opponent.englishLabel()} has no legal reply"
            facts.opponentHasNoReply ->
                "no legal reply for ${opponent.englishLabel()} " +
                    "(stalemate, the side that cannot move loses)"
            facts.givesCheck -> "check"
            else -> "no check"
        }

        val safety = when {
            facts.cheapestAttacker == null -> "safe (nothing can take it next move)"
            facts.isHanging -> "HANGING (${facts.attackerText()} can take it next move, nothing recaptures)"
            else -> "protected (${facts.attackerText()} can take it, but we recapture)"
        }

        val threatText = if (facts.threats.isEmpty()) {
            "no threat"
        } else {
            facts.threats.joinToString("; ") { threat ->
                val label = "${threat.target.englishLabel()} on ${UcciNotation.point(threat.square)}" +
                    " (${threat.target.type.valueText()})"
                when {
                    // 自己都站不住(下一步会被白吃)就别谈收益: 对方先手把这枚子吃掉, 威胁根本兑现不了
                    threat.defended -> "attacks the $label, but it is defended"
                    facts.isHanging -> "attacks the $label (but this piece can be taken first)"
                    else -> "wins the $label next move, nothing defends it"
                }
            }.replaceFirstChar { it.uppercase() }
        }

        val escape = inDanger.firstOrNull { it.square == move.from }
        val escapeText = escape?.let { " This also saves your ${it.piece.englishLabel()}." } ?: ""

        val head = "${move.piece.englishLabel().replaceFirstChar { it.uppercase() }} " +
            "${UcciNotation.point(move.from)} -> ${UcciNotation.point(move.to)}"
        return "$head: $capture. $check. $safety. $threatText.$escapeText"
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

    /** "Black chariot a7" —— 能吃掉落点这枚子的那一步 */
    private fun CandidateFacts.attackerText(): String {
        val attacker = cheapestAttacker ?: return ""
        return "${attacker.piece.englishLabel()} ${UcciNotation.point(attacker.from)}"
    }
}
