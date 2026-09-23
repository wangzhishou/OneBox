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
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.UcciNotation
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
 * 两段式, 因为让 Jev 在 20~44 个合法着法里直接挑是不可行的(实测 top p 只有 0.09~0.34,
 * argmax 近似随机):
 * 1. **裁剪**: 给每个合法着法算一遍局面事实(吃子/被吃/将军/威胁), 本地打分取前
 *    [JevBestMoveResolver.SHORTLIST_SIZE] 个, 并用服务端 Pikafish 的最佳着法兜一个名额;
 * 2. **判断**: 只把这几个候选连同事实描述发给 Jev, 由它在小集合里选最优。
 *
 * 走子链路本身没变: Jev 返回的着法仍要能映射回**全部**合法着法([JevBestMoveResolver.resolve])。
 */
@Singleton
class JevMoveChooser @Inject constructor(
    @Named("JevDirectService") private val jevDirectService: JevService,
    @Named("JevProxyService") private val jevProxyService: JevService,
    private val pikafishMoveChooser: PikafishMoveChooser,
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
        val facts = withContext(defaultDispatcher) {
            legalMoves.map { JevBestMoveResolver.analyze(boardState, it) }
        }
        val seed = engineSeedMove(boardState, fen, history, legalMoves, slot)
        val shortlist = withContext(defaultDispatcher) {
            JevBestMoveResolver.shortlist(facts, JevBestMoveResolver.SHORTLIST_SIZE, seed)
        }
        val request = JevBestMoveResolver.buildRequest(
            fen = fen,
            boardState = boardState,
            history = history,
            candidates = shortlist,
            legalMoveCount = legalMoves.size,
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
                    // 带上候选裁剪比: 从落库的 ai_reason 就能看出这一手是在几个候选里选的
                    reason = "jev conf=%.2f p=%.2f cand=%d/%d".format(
                        selection.confidence,
                        selection.probability,
                        shortlist.size,
                        legalMoves.size,
                    ),
                    rawResponse = selection.rawResponse,
                    fallbackUsed = false,
                )
            }
        }

        return HeuristicMoveFallback.decision(legalMoves)
            ?.copy(reason = "jev: ${lastError ?: "failed"}", fallbackUsed = true)
    }

    /**
     * 服务端 Pikafish 的最佳着法, 用作候选集的"保底名额"。
     *
     * 纯本地打分是战术口径, 遇到"最佳着法本身很安静"的局面会漏(实测 ply8 引擎首选支士,
     * 本地分 0, 挤不进前 6)。引擎不可用(或它自己回退到本地兜底)时返回 null, 退化为纯本地裁剪。
     */
    private suspend fun engineSeedMove(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): XiangqiMove? = runCatching {
        pikafishMoveChooser.choose(
            boardState = boardState,
            fen = fen,
            history = history,
            legalMoves = legalMoves,
            slot = slot,
            engineId = XiangqiAiSource.RemoteEngine.PIKAFISH,
        )
    }.getOrNull()
        ?.takeUnless { it.fallbackUsed }
        ?.move
}

internal object JevBestMoveResolver {

    const val DEFAULT_MODEL = "jev-latest"
    const val QUESTION_BEST_MOVE = "best_move"

    /** 交给 Jev 的候选数。太多它就是在猜, 太少会把好棋裁掉。 */
    const val SHORTLIST_SIZE = 6

    /** 同一个出发格最多进几个候选(防止候选全是同一门炮往同一方向挪) */
    private const val MAX_PER_ORIGIN = 2

    /** 将杀着法在裁剪打分里的权重(足够大, 一票否决其它项) */
    private const val CHECKMATE_BONUS = 1000.0
    private const val CHECK_BONUS = 3.0

    /** 威胁还没兑现, 打分时打折 */
    private const val THREAT_WEIGHT = 0.5

    /** 极小的向前推进项, 只用来把同分着法排开(否则候选会全挤在棋盘一侧) */
    private const val ADVANCE_WEIGHT = 0.05

    private val gson = Gson()

    data class Selection(
        val move: XiangqiMove,
        val confidence: Double,
        val probability: Double,
        val rawResponse: String,
    )

    /**
     * 一步合法着法的局面事实。裁剪打分与英文描述都只用这一份数据, 避免重复推演。
     */
    data class CandidateFacts(
        val move: XiangqiMove,
        /** 吃到的子力价值; 不吃子为 0 */
        val captureValue: Double,
        val givesCheck: Boolean,
        /** 对方一步都走不出来(将杀或困毙) */
        val opponentHasNoReply: Boolean,
        /** 对方能吃掉落点这枚子的最便宜一步; null = 没人能吃 */
        val cheapestAttacker: XiangqiMove?,
        /** 会被吃, 但能吃回来 */
        val defended: Boolean,
        /** 落到新位置后能吃到的最多两个目标(按价值降序) */
        val threats: List<XiangqiMove>,
    ) {
        val isCheckmate: Boolean get() = opponentHasNoReply && givesCheck

        val isHanging: Boolean get() = cheapestAttacker != null && !defended

        val bestThreatValue: Double
            get() = threats.maxOfOrNull { it.captured?.type?.sortValue() ?: 0.0 } ?: 0.0

        /**
         * 这一步的净子力风险: 落点没人能吃 = 0; 会被吃且无人保护 = 白送这枚子;
         * 会被吃但有保护 = 交换差价(我方子力 - 对方来吃的最便宜子力, 不小于 0)。
         */
        val materialRisk: Double
            get() {
                val attacker = cheapestAttacker ?: return 0.0
                val own = move.piece.type.sortValue()
                return if (defended) maxOf(0.0, own - attacker.piece.type.sortValue()) else own
            }

        /** 向前推进的格数(红方 rank 递减、黑方递增), 只用于打破同分 */
        val forwardProgress: Int
            get() = if (move.piece.side == Side.RED) {
                move.from.rank - move.to.rank
            } else {
                move.to.rank - move.from.rank
            }
    }

    /**
     * 题型说明。**必须用英文**: TypeSafe 官方明确 Jev 英语优先,
     * 中日韩文字"可以处理但准确率较低"。
     */
    private const val BEST_MOVE_INSTRUCTIONS =
        "You are a strong Chinese Chess (Xiangqi) player. Each option is one of the strongest " +
            "candidate moves for the side to move, written in English and stating: what it takes, " +
            "whether it gives check, whether the moving piece is safe on its destination square, " +
            "and what that piece threatens next. Choose the strongest move. Weigh them roughly in " +
            "this order: checkmate > a capture or threat that wins material while staying safe > " +
            "a move that keeps your pieces protected and active > a capture that loses material > " +
            "a move flagged HANGING (the piece can be taken next move with nothing recapturing), " +
            "which loses material for nothing. Coordinates use UCCI: files a-i from Red's left, " +
            "ranks 0-9 with rank 0 on Red's home rank."

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

        // 把"能吃将/帅"排除掉: 那是将军(已单列), 不是威胁; 否则任何将军着法的威胁分都会被顶到最高
        val threats = GameArbiter.legalMoves(after, mover)
            .filter {
                it.from == move.to && it.captured != null &&
                    it.captured!!.type != PieceType.KING
            }
            .distinctBy { it.to }
            .sortedByDescending { it.captured!!.type.sortValue() }
            .take(2)

        return CandidateFacts(
            move = move,
            captureValue = move.captured?.type?.sortValue() ?: 0.0,
            givesCheck = givesCheck,
            opponentHasNoReply = replies.isEmpty(),
            cheapestAttacker = cheapest,
            defended = defended,
            threats = threats,
        )
    }

    /**
     * 裁剪打分, **只用来挑候选, 不决定最终着法**(最终仍由 Jev 选)。
     *
     * 口径: 吃子 + 打折后的威胁 + 将军 - 被吃风险, 再加一个极小的推进项打破同分。
     */
    fun score(facts: CandidateFacts): Double {
        if (facts.isCheckmate) return CHECKMATE_BONUS
        var score = facts.captureValue + THREAT_WEIGHT * facts.bestThreatValue
        if (facts.givesCheck) score += CHECK_BONUS
        score -= facts.materialRisk
        score += ADVANCE_WEIGHT * facts.forwardProgress
        return score
    }

    /**
     * 候选裁剪: 本地打分取前 [limit] 个, 并把 [seed](Pikafish 的最佳着法)顶进来。
     *
     * 两条约束:
     * - **同一个出发格最多两个候选**: 否则打分会把"同一门炮往同一方向挪 1~6 格"全塞进候选
     *   (实测开局前 6 名全是炮的横move), Jev 看不到别处的好棋, 等于没裁剪;
     * - 送吃着法(HANGING 且没有更大补偿)会被扣到负分, 自然落榜 —— Jev 连"白送一手"都看不到。
     *
     * 合法着法本身不多于 [limit] 时原样返回。
     */
    fun shortlist(
        facts: List<CandidateFacts>,
        limit: Int,
        seed: XiangqiMove? = null,
    ): List<CandidateFacts> {
        require(limit > 0) { "limit must be positive" }
        if (facts.size <= limit) return facts

        val ranked = facts.sortedWith(
            compareByDescending<CandidateFacts> { score(it) }
                .thenByDescending { it.forwardProgress }
                .thenBy { it.move.notationUcci }
        )
        val picked = mutableListOf<CandidateFacts>()
        for (candidate in ranked) {
            if (picked.size >= limit) break
            val sameOrigin = picked.count { it.move.from == candidate.move.from }
            if (sameOrigin >= MAX_PER_ORIGIN) continue
            picked += candidate
        }

        if (seed != null) {
            val seeded = facts.firstOrNull { it.move.notationUcci == seed.notationUcci }
            if (seeded != null && seeded !in picked) {
                // 用引擎着法顶掉本地分最低的那一个, 保证强手一定在候选里
                if (picked.size >= limit) picked.removeAt(picked.lastIndex)
                picked += seeded
            }
        }
        return picked
    }

    /**
     * 组装 System One 请求。`criteria` 里只有裁剪后的候选。
     *
     * 早期版本把 20~44 个合法着法全塞进去, 且描述只有「红炮,中文记谱:炮二平五」——
     * 候选几乎不可区分, 实测 top p 只有 0.09~0.34, argmax 近似随机落子。
     */
    fun buildRequest(
        fen: String,
        boardState: BoardState,
        history: List<String>,
        candidates: List<CandidateFacts>,
        legalMoveCount: Int,
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
            addProperty("legal_move_count", legalMoveCount)
            addProperty("candidate_count", candidates.size)
            addProperty(
                "task",
                "The options are the strongest ${candidates.size} of the $legalMoveCount legal " +
                    "moves. Pick the best one for the side to move.",
            )
        }
        val criteria = JsonObject().apply {
            candidates.forEach { addProperty(it.move.notationUcci, describe(it)) }
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
    fun describe(facts: CandidateFacts): String {
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
            facts.defended -> "protected (${facts.attackerText()} can take it, but we recapture)"
            else -> "HANGING (${facts.attackerText()} can take it next move, nothing recaptures)"
        }

        val threatText = if (facts.threats.isEmpty()) {
            "threatens nothing"
        } else {
            "threatens " + facts.threats.joinToString(", ") { threat ->
                val target = threat.captured!!
                "${target.englishLabel()} on ${UcciNotation.point(threat.to)}" +
                    " (${target.type.valueText()})"
            }
        }

        val head = "${move.piece.englishLabel().replaceFirstChar { it.uppercase() }} " +
            "${UcciNotation.point(move.from)} -> ${UcciNotation.point(move.to)}"
        return "$head: $capture. $check. $safety. $threatText."
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
