package com.wanbaohe.chess.domain

import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameArbiterTest {
    @Test
    fun bareKingsAndOneMinorPieceCannotProduceMate() {
        listOf(
            "4k3/8/8/8/8/8/8/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2B5/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2N5/4K3 w - - 0 1",
            "4k3/8/8/8/4b3/8/2B5/4K3 w - - 0 1",
        ).forEach { assertTrue(GameArbiter.isInsufficientMaterial(FenCodec.parse(it)), it) }
    }

    @Test
    fun promotionsAndHelpMateMaterialAreNotMistakenForDeadPositions() {
        listOf(
            "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2BB4/4K3 w - - 0 1",
            "4k3/8/8/8/8/8/2NN4/4K3 w - - 0 1",
            "4k3/8/8/8/8/4n3/2B5/4K3 w - - 0 1",
        ).forEach { assertFalse(GameArbiter.isInsufficientMaterial(FenCodec.parse(it)), it) }
    }
}
