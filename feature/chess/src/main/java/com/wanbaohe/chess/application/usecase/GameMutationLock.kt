package com.wanbaohe.chess.application.usecase

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class GameMutationLock @Inject constructor() {
    private val locks = ConcurrentHashMap<String, Mutex>()

    suspend fun <T> withGame(gameId: String, action: suspend () -> T): T =
        locks.getOrPut(gameId) { Mutex() }.withLock { action() }
}
