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
