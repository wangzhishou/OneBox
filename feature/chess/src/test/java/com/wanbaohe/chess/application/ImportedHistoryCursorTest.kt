/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
