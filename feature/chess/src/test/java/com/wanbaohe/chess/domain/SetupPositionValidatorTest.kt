package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SetupPositionValidatorTest {
    private fun issue(fen: String) = SetupPositionValidator.validate(FenCodec.parse(fen))

    @Test
    fun standardOpeningIsValidForEitherSide() {
        assertNull(issue(FenCodec.INITIAL_FEN))
        assertNull(issue(FenCodec.INITIAL_FEN.replace(" w ", " b ")))
    }

    @Test
    fun eachSideNeedsExactlyOneKing() {
        assertEquals(SetupPositionIssue.KingCount(Side.WHITE), issue("4k3/8/8/8/8/8/4P3/8 w - - 0 1"))
        assertIs<SetupPositionIssue.KingCount>(issue("4k3/8/8/8/8/8/4P3/3KK3 w - - 0 1"))
    }

    @Test
    fun kingsCannotTouchAndPawnsCannotStayOnPromotionRanks() {
        assertEquals(SetupPositionIssue.KingsAdjacent, issue("8/8/8/8/8/4k3/4K3/4P3 w - - 0 1"))
        assertEquals(SetupPositionIssue.PawnOnPromotionRank, issue("P3k3/8/8/8/8/8/8/4K3 w - - 0 1"))
    }

    @Test
    fun promotedExtraQueensAreAllowedWhenThereAreMissingPawns() {
        assertNull(issue("4k3/8/8/8/8/QQQ5/8/4K3 w - - 0 1"))
        assertIs<SetupPositionIssue.ImpossibleMaterial>(issue("4k3/8/8/8/8/QQ6/PPPPPPPP/4K3 w - - 0 1"))
    }

    @Test
    fun onlyTheActiveSideMayBeInCheck() {
        assertEquals(SetupPositionIssue.WrongTurn, issue("4k3/8/8/8/8/8/4R3/4K3 w - - 0 1"))
        assertNull(issue("4k3/8/8/8/8/8/4r3/4K3 w - - 0 1"))
    }

    @Test
    fun checkmateStalemateAndDeadPositionsCannotStart() {
        assertEquals(SetupPositionIssue.NoLegalMoves, issue("7k/6Q1/5K2/8/8/8/8/8 b - - 0 1"))
        assertEquals(SetupPositionIssue.NoLegalMoves, issue("7k/5K2/6Q1/8/8/8/8/8 b - - 0 1"))
        assertEquals(SetupPositionIssue.NoLegalMoves, issue("4k3/8/8/8/8/8/8/4K3 w - - 0 1"))
        assertEquals(SetupPositionIssue.NoLegalMoves, issue(FenCodec.INITIAL_FEN.replace(" 0 1", " 100 1")))
    }

    @Test
    fun castlingRightsNeedTheOriginalKingAndRookSquares() {
        assertEquals(SetupPositionIssue.InvalidCastling, issue("4k3/8/8/8/8/8/4P3/4K3 w K - 0 1"))
        assertNull(issue("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"))
    }

    @Test
    fun enPassantMustCorrespondToADoublePawnMove() {
        assertNull(issue("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 9"))
        assertEquals(SetupPositionIssue.InvalidEnPassant, issue("4k3/8/8/4P3/8/8/8/4K3 w - d6 0 9"))
        assertEquals(SetupPositionIssue.InvalidEnPassant, issue("4k3/3p4/8/3pP3/8/8/8/4K3 w - d6 0 9"))
    }
}
