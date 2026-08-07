package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.model.GameSetup
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportGameUseCase @Inject constructor(
    private val createGame: CreateGameUseCase,
    private val playMove: PlayMoveUseCase,
    private val query: GameQueryUseCase,
) {

    suspend fun importFen(title: String, fen: String): String {
        FenCodec.parse(fen)
        return createGame.create(title, GameSetup.local(), fen)
    }

    suspend fun importJson(title: String, json: String): String {
        val parsed = JSONObject(json)
        val gameTitle = title.ifBlank { parsed.optString("title", "Imported") }
        val initialFen = parsed.optString("initialFen", FenCodec.INITIAL_FEN)
        val gameId = createGame.create(gameTitle, GameSetup.local(), initialFen)

        val movesArray = parsed.optJSONArray("moves") ?: JSONArray()
        repeat(movesArray.length()) { i ->
            val moveObj = movesArray.getJSONObject(i)
            val moveUcci = moveObj.optString("move", "")
            val current = query.getById(gameId) ?: return@repeat
            val before = FenCodec.parse(current.currentFen)
            val legal = GameArbiter.legalMoves(before).firstOrNull { it.notationUcci == moveUcci }
                ?: return@repeat
            playMove.commit(gameId, legal)
        }

        return gameId
    }
}
