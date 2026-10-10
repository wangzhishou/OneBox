package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.usecase.ImportedHistoryCursor
import com.wanbaohe.chess.application.usecase.SkippedPly
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ImportedHistoryCursorTest {
    @Test
    fun legacyBackupsWithoutACursorOpenAtTheRecordedEnd() {
        assertEquals(4, ImportedHistoryCursor.read(null, 4))
        assertEquals(0, ImportedHistoryCursor.read(null, 0))
    }

    @Test
    fun cursorAcceptsOnlyIntegralRecordedPositions() {
        assertEquals(0, ImportedHistoryCursor.read(0, 4))
        assertEquals(2, ImportedHistoryCursor.read(2, 4))
        assertEquals(4, ImportedHistoryCursor.read(4L, 4))
        listOf<Any>(
            -1, 5, 2.5, "2", true, Any(), Double.NaN, Double.POSITIVE_INFINITY, Long.MAX_VALUE,
        ).forEach { value ->
            assertFailsWith<IllegalArgumentException>(value.toString()) { ImportedHistoryCursor.read(value, 4) }
        }
    }

    @Test
    fun skippedMovesBeforeTheCursorDoNotPushItIntoFutureHistory() {
        val skipped = listOf(SkippedPly(2, "bad"), SkippedPly(4, "bad"))
        assertEquals(0, ImportedHistoryCursor.afterSkipping(0, skipped))
        assertEquals(2, ImportedHistoryCursor.afterSkipping(3, skipped))
        assertEquals(3, ImportedHistoryCursor.afterSkipping(5, skipped))
    }
}
