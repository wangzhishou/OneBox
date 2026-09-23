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
    fun buildRequestOnlyCarriesShortlistedCandidates() {
        // 稀疏局面: 黑车 a7 无保护, e-file 上的黑卒只是挡住双将照面(在红车 e5 之后方);
        // 红车 a5 可吃它(a5a7)、可直上 a4 送吃(a5a4)、也可平到 e5 将军(a5e5)
        val board = FenCodec.parse(sparseFen)
        val moves = GameArbiter.legalMoves(board)
        val facts = moves.map { JevBestMoveResolver.analyze(board, it) }
        val shortlist = JevBestMoveResolver.shortlist(facts, limit = 2)

        val request = JevBestMoveResolver.buildRequest(
            fen = sparseFen,
            boardState = board,
            history = listOf("a3a4", "a6a5"),
            candidates = shortlist,
            legalMoveCount = moves.size,
            model = "jev-latest",
        )

        assertEquals("jev-latest", request.model)
        assertEquals("Chinese Chess (Xiangqi)", request.state.get("game").asString)
        assertEquals("Red", request.state.get("side_to_move").asString)
        assertEquals(2, request.state.getAsJsonArray("recent_moves").size())
        assertEquals(moves.size, request.state.get("legal_move_count").asInt)
        assertEquals(2, request.state.get("candidate_count").asInt)
        assertEquals(9.0, request.state.getAsJsonObject("piece_values").get("chariot").asDouble, 1e-9)

        val bestMove = request.questions.getAsJsonObject(JevBestMoveResolver.QUESTION_BEST_MOVE)
        assertEquals("choice", bestMove.get("type").asString)
        assertTrue(bestMove.get("instructions").asString.contains("checkmate"))
        // criteria 里只有裁剪后的候选
        val criteria = bestMove.getAsJsonObject("criteria")
        assertEquals(2, criteria.size())
    }

    @Test
    fun shortlistDropsHangingMoveAndKeepsEngineSeed() {
        val board = FenCodec.parse(sparseFen)
        val byUcci = GameArbiter.legalMoves(board).associateBy { it.notationUcci }
        val facts = byUcci.values.map { JevBestMoveResolver.analyze(board, it) }

        // 送吃的 a5a4(-9 分)挤不进前 2; 吃车 a5a7(+9)与将军 a5e5(+3)进
        val picked = JevBestMoveResolver.shortlist(facts, limit = 2).map { it.move.notationUcci }
        assertEquals(2, picked.size)
        assertTrue(picked.toString(), picked.contains("a5a7"))
        assertTrue(picked.toString(), picked.contains("a5e5"))
        assertFalse(picked.toString(), picked.contains("a5a4"))

        // 引擎兜底名额: 即使本地分最低也一定会进候选(替代分最低的那个)
        val seeded = JevBestMoveResolver.shortlist(facts, limit = 2, seed = byUcci.getValue("a5a4"))
            .map { it.move.notationUcci }
        assertEquals(2, seeded.size)
        assertTrue(seeded.toString(), seeded.contains("a5a4"))
        assertTrue(seeded.toString(), seeded.contains("a5a7"))

        // 合法着法本身不多于上限时原样返回, 不做裁剪
        val few = facts.take(3)
        assertEquals(few.size, JevBestMoveResolver.shortlist(few, limit = 5).size)
    }

    @Test
    fun candidateFactsDescribeCaptureCheckAndSafety() {
        val board = FenCodec.parse(sparseFen)
        val byUcci = GameArbiter.legalMoves(board).associateBy { it.notationUcci }
        fun describe(ucci: String) =
            JevBestMoveResolver.describe(JevBestMoveResolver.analyze(board, byUcci.getValue(ucci)))

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

        // 每个候选描述都必须带事实, 否则 Jev 无从分辨(旧版只有「红车,中文记谱:车九进一」)
        byUcci.values.forEach { move ->
            val text = describe(move.notationUcci)
            assertTrue(text, text.contains("check"))
            assertTrue(text, text.contains("threatens "))
        }
    }
}
