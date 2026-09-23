package com.wanbaohe.xiangqi.data

import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.UcciNotation
import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JevBestMoveResolverTest {

    private val redPawn = Piece(Side.RED, PieceType.PAWN)
    private val redRook = Piece(Side.RED, PieceType.ROOK)
    private val blackCannon = Piece(Side.BLACK, PieceType.CANNON)

    // notationUcci 使用标准 UCCI(UCCI rank0=红方底线), 与 Pikafish bestmove 同一约定
    private val legalMoves = listOf(
        XiangqiMove(
            from = BoardPoint(4, 6), to = BoardPoint(4, 5),
            piece = redPawn, notationUcci = "e3e4", notationCn = "兵五进一",
        ),
        XiangqiMove(
            from = BoardPoint(0, 9), to = BoardPoint(0, 8),
            piece = redRook, notationUcci = "a0a1", notationCn = "车九进一",
        ),
        XiangqiMove(
            from = BoardPoint(1, 7), to = BoardPoint(1, 4),
            piece = redRook, captured = blackCannon,
            notationUcci = "b2b5", notationCn = "车八进三",
        ),
    )

    @Test
    fun ucciFormatUsesRedHomeRankZero() {
        assertEquals("a0a1", UcciNotation.format(BoardPoint(0, 9), BoardPoint(0, 8)))
        assertEquals("e3e4", UcciNotation.format(BoardPoint(4, 6), BoardPoint(4, 5)))
        // 初始局面红方兵 (file0, rank6) 标准 UCCI 为 a3, 与 Pikafish bestmove a3a4 一致
        assertEquals("a3a4", UcciNotation.format(BoardPoint(0, 6), BoardPoint(0, 5)))
    }

    @Test
    fun resolvePicksArgmaxFromProbabilitiesNotSampledChoice() {
        val raw = """
        {
          "model": "jev-latest",
          "answers": {
            "best_move": {
              "type": "choice",
              "choice": "e3e4",
              "confidence": 0.9,
              "probabilities": { "e3e4": 0.2, "a0a1": 0.7, "b2b5": 0.1 }
            }
          },
          "usage": {}
        }
        """.trimIndent()

        val selection = JevBestMoveResolver.resolve(raw, legalMoves)

        assertNotNull(selection)
        assertEquals("a0a1", selection!!.move.notationUcci)
        assertEquals(0.9, selection.confidence, 1e-9)
        assertEquals(0.7, selection.probability, 1e-9)
        assertEquals(raw, selection.rawResponse)
    }

    @Test
    fun resolveFallsBackToChoiceWhenProbabilitiesMissing() {
        val raw = """
        {
          "model": "jev-latest",
          "answers": {
            "best_move": { "type": "choice", "choice": "b2b5", "confidence": 0.5 }
          }
        }
        """.trimIndent()

        val selection = JevBestMoveResolver.resolve(raw, legalMoves)

        assertNotNull(selection)
        assertEquals("b2b5", selection!!.move.notationUcci)
        assertEquals(0.0, selection.probability, 1e-9)
    }

    @Test
    fun resolveReturnsNullWhenSelectedMoveNotLegal() {
        val raw = """
        {
          "model": "jev-latest",
          "answers": {
            "best_move": {
              "type": "choice",
              "choice": "e3e4",
              "confidence": 0.9,
              "probabilities": { "e3e4": 0.4, "z0z1": 0.6 }
            }
          }
        }
        """.trimIndent()

        assertNull(JevBestMoveResolver.resolve(raw, legalMoves))
    }

    @Test
    fun resolveReturnsNullOnMalformedJson() {
        assertNull(JevBestMoveResolver.resolve("not json", legalMoves))
        assertNull(JevBestMoveResolver.resolve("{}", legalMoves))
    }

    @Test
    fun fallbackPrefersCaptureThenAdvance() {
        val decision = HeuristicMoveFallback.decision(legalMoves)

        assertNotNull(decision)
        assertTrue(decision!!.fallbackUsed)
        assertEquals("b2b5", decision.move.notationUcci)

        val withoutCapture = legalMoves.filter { it.captured == null }
        val fallback = HeuristicMoveFallback.decision(withoutCapture)
        assertNotNull(fallback)
        assertTrue(fallback!!.move.notationCn.contains("进"))
        assertFalse(fallback.move.captured != null)
    }

    private val sparseFen = "4k4/9/r8/9/R8/9/4p4/9/9/4K4 w 0 1"

    @Test
    fun buildRequestCarriesEveryLegalMoveWithFacts() {
        // 稀疏局面: 黑车 a7 无保护, e-file 上的黑卒只是挡住双将照面(在红车 e5 之后方);
        // 红车 a5 可吃它(a5a7)、可直上 a4 送吃(a5a4)、也可平到 e5 将军(a5e5)
        val board = FenCodec.parse(sparseFen)
        val moves = GameArbiter.legalMoves(board)
        val facts = moves.map { JevBestMoveResolver.analyze(board, it) }

        val request = JevBestMoveResolver.buildRequest(
            fen = sparseFen,
            boardState = board,
            history = listOf("a3a4", "a6a5"),
            candidates = facts,
            model = "jev-latest",
        )

        assertEquals("jev-latest", request.model)
        assertEquals("Chinese Chess (Xiangqi)", request.state.get("game").asString)
        assertEquals("Red", request.state.get("side_to_move").asString)
        assertEquals(2, request.state.getAsJsonArray("recent_moves").size())
        assertEquals(9.0, request.state.getAsJsonObject("piece_values").get("chariot").asDouble, 1e-9)

        val bestMove = request.questions.getAsJsonObject(JevBestMoveResolver.QUESTION_BEST_MOVE)
        assertEquals("choice", bestMove.get("type").asString)
        assertTrue(bestMove.get("instructions").asString.contains("checkmate"))

        // 不做裁剪: criteria 覆盖全部合法着法, 且 key 就是 UCCI
        val criteria = bestMove.getAsJsonObject("criteria")
        assertEquals(moves.size, criteria.size())
        assertEquals(moves.size, request.state.get("legal_move_count").asInt)
        moves.forEach { assertTrue(it.notationUcci, criteria.has(it.notationUcci)) }
    }

    @Test
    fun describesThreatAsWinOnlyWhenTargetIsUndefended() {
        val board = FenCodec.parse(sparseFen)
        val byUcci = GameArbiter.legalMoves(board).associateBy { it.notationUcci }
        fun describe(ucci: String) =
            JevBestMoveResolver.describe(JevBestMoveResolver.analyze(board, byUcci.getValue(ucci)))

        // 红车 a5 -> e5 落点安全, 且能白吃无保护的黑卒: 这是"赢子", 措辞必须强
        val wins = describe("a5e5")
        assertTrue(wins, wins.contains("Wins the Black soldier on e3 (1) next move, nothing defends it"))

        // 红车 a5 -> a6 自己也挂在黑车口上: 收益兑现不了, 不能宣传成"赢子"
        val contested = describe("a5a6")
        assertTrue(contested, contested.contains("Attacks the Black chariot on a7 (9)"))
        assertTrue(contested, contested.contains("but this piece can be taken first"))
        assertFalse(contested, contested.contains("Wins the"))
    }

    @Test
    fun candidateFactsDescribeCaptureCheckAndSafety() {
        val board = FenCodec.parse(sparseFen)
        val byUcci = GameArbiter.legalMoves(board).associateBy { it.notationUcci }
        val inDanger = JevBestMoveResolver.piecesInDanger(board)
        fun describe(ucci: String) = JevBestMoveResolver.describe(
            JevBestMoveResolver.analyze(board, byUcci.getValue(ucci)),
            inDanger,
        )

        // 吃子: 红车 a5 x 黑车 a7, 吃完对方只剩将, 落点安全
        val capture = describe("a5a7")
        assertTrue(capture, capture.contains("Red chariot a5 -> a7"))
        assertTrue(capture, capture.contains("takes the Black chariot on a7 (9)"))
        assertTrue(capture, capture.contains("no check"))
        assertTrue(capture, capture.contains("safe (nothing can take it next move)"))

        // 送吃: 红车 a5 -> a4, 会被黑车 a7 直接吃掉且无人保护
        val hanging = describe("a5a4")
        assertTrue(hanging, hanging.contains("quiet move"))
        assertTrue(hanging, hanging.contains("HANGING"))
        assertTrue(hanging, hanging.contains("Black chariot a7"))

        // 将军: 红车 a5 -> e5 与黑将同线, 且自身不会被吃
        val check = describe("a5e5")
        assertTrue(check, check.contains("check"))
        assertTrue(check, check.contains("safe (nothing can take it next move)"))

        // 危险清单: 红车 a5 正被黑车 a7 攻击且无人保护
        assertEquals(1, inDanger.size)
        assertTrue(inDanger[0].text(), inDanger[0].text().contains("Red chariot on a5"))
        assertTrue(inDanger[0].text(), inDanger[0].text().contains("nothing defends it"))
        // 走开这枚子的候选要说明解围; 与它无关的着法不带这句
        assertTrue(capture, capture.contains("This also saves your Red chariot."))
        assertFalse(describe("e0d0"), describe("e0d0").contains("This also saves"))

        // 每个候选描述都必须带事实, 否则 Jev 无从分辨(旧版只有「红车,中文记谱:车九进一」)
        byUcci.values.forEach { move ->
            val text = describe(move.notationUcci)
            assertTrue(text, text.contains("check"))
            assertTrue(
                text,
                text.contains("safe (") || text.contains("HANGING") || text.contains("protected"),
            )
        }
    }
}
