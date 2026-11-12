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

import com.wanbaohe.chess.application.usecase.GameMutationLock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class GameMutationLockTest {
    @Test
    fun aPauseOrHistoryMutationWaitsForTheAcceptedCommit() = runBlocking {
        val lock = GameMutationLock()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val order = mutableListOf<String>()
        val commit = async { lock.withGame("game") { entered.complete(Unit); release.await(); order += "commit" } }
        entered.await()
        val pause = async { lock.withGame("game") { order += "pause" } }
        yield()
        assertEquals(emptyList<String>(), order)
        release.complete(Unit)
        commit.await()
        pause.await()
        assertEquals(listOf("commit", "pause"), order)
    }

    @Test
    fun anotherGameIsNotBlockedAndCancelledWaitersNeverWrite() = runBlocking {
        val lock = GameMutationLock()
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val first = async { lock.withGame("first") { entered.complete(Unit); release.await() } }
        entered.await()
        assertEquals("second", lock.withGame("second") { "second" })
        var wrote = false
        val cancelled = async { lock.withGame("first") { wrote = true } }
        yield()
        cancelled.cancelAndJoin()
        release.complete(Unit)
        first.await()
        assertFalse(wrote)
    }
}
