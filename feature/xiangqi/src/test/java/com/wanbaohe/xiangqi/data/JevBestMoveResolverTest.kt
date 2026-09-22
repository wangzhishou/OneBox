package com.wanbaohe.xiangqi.data

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

    private val legalMoves = listOf(
        XiangqiMove(
            from = BoardPoint(4, 6), to = BoardPoint(4, 5),
            piece = redPawn, notationUcci = "e6e5", notationCn = "兵五进一",
        ),
        XiangqiMove(
            from = BoardPoint(0, 9), to = BoardPoint(0, 8),
            piece = redRook, notationUcci = "a9a8", notationCn = "车九进一",
        ),
        XiangqiMove(
            from = BoardPoint(1, 7), to = BoardPoint(1, 4),
            piece = redRook, captured = blackCannon,
            notationUcci = "b7b4", notationCn = "车八进三",
        ),
    )

    @Test
    fun resolvePicksArgmaxFromProbabilitiesNotSampledChoice() {
        val raw = """
        {
          "model": "jev-latest",
          "answers": {
            "best_move": {
              "type": "choice",
              "choice": "e6e5",
              "confidence": 0.9,
              "probabilities": { "e6e5": 0.2, "a9a8": 0.7, "b7b4": 0.1 }
            }
          },
          "usage": {}
        }
        """.trimIndent()

        val selection = JevBestMoveResolver.resolve(raw, legalMoves)

        assertNotNull(selection)
        assertEquals("a9a8", selection!!.move.notationUcci)
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
            "best_move": { "type": "choice", "choice": "b7b4", "confidence": 0.5 }
          }
        }
        """.trimIndent()

        val selection = JevBestMoveResolver.resolve(raw, legalMoves)

        assertNotNull(selection)
        assertEquals("b7b4", selection!!.move.notationUcci)
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
              "choice": "e6e5",
              "confidence": 0.9,
              "probabilities": { "e6e5": 0.4, "z0z1": 0.6 }
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
        assertEquals("b7b4", decision.move.notationUcci)

        val withoutCapture = legalMoves.filter { it.captured == null }
        val fallback = HeuristicMoveFallback.decision(withoutCapture)
        assertNotNull(fallback)
        assertTrue(fallback!!.move.notationCn.contains("进"))
        assertFalse(fallback.move.captured != null)
    }

    @Test
    fun buildRequestEmbedsStateAndCriteria() {
        val request = JevBestMoveResolver.buildRequest(
            fen = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w",
            side = Side.RED,
            history = listOf("炮二平五", "马8进7"),
            legalMoves = legalMoves,
            model = "jev-latest",
        )

        assertEquals("jev-latest", request.model)
        assertEquals("红方", request.state.get("side_to_move").asString)
        assertEquals(2, request.state.getAsJsonArray("recent_moves").size())

        val bestMove = request.questions.getAsJsonObject(JevBestMoveResolver.QUESTION_BEST_MOVE)
        assertEquals("choice", bestMove.get("type").asString)
        val criteria = bestMove.getAsJsonObject("criteria")
        assertEquals(legalMoves.size, criteria.size())
        assertTrue(criteria.get("e6e5").asString.startsWith("红兵"))
        assertTrue(criteria.get("e6e5").asString.contains("兵五进一"))
    }
}
