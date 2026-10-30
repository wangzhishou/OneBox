package com.wanbaohe.mahjong.data.local

import com.wanbaohe.mahjong.domain.model.ClaimChoice
import com.wanbaohe.mahjong.domain.model.ClaimSource
import com.wanbaohe.mahjong.domain.model.ClaimType
import com.wanbaohe.mahjong.domain.model.GamePhase
import com.wanbaohe.mahjong.domain.model.MahjongState
import com.wanbaohe.mahjong.domain.model.Meld
import com.wanbaohe.mahjong.domain.model.MeldType
import com.wanbaohe.mahjong.domain.model.PendingClaim
import com.wanbaohe.mahjong.domain.model.PlayerState
import org.json.JSONArray
import org.json.JSONObject

internal object MahjongSnapshotCodec {
    private const val VERSION = 1

    fun encode(state: MahjongState): String = JSONObject().apply {
        put("version", VERSION)
        put("seed", state.seed)
        put("revision", state.revision)
        put("dealer", state.dealerSeat)
        put("current", state.currentSeat)
        put("phase", state.phase.name)
        state.drawnTileId?.let { put("drawn", it) }
        put("wall", state.wall.toJsonArray())
        put("players", JSONArray().apply {
            state.players.forEach { player ->
                put(JSONObject().apply {
                    put("hand", player.hand.toJsonArray())
                    put("discards", player.discards.toJsonArray())
                    put("score", player.score)
                    put("melds", JSONArray().apply {
                        player.melds.forEach { meld ->
                            put(JSONObject().apply {
                                put("type", meld.type.name)
                                put("tiles", meld.tileIds.toJsonArray())
                                put("from", meld.fromSeat)
                                put("concealed", meld.concealed)
                            })
                        }
                    })
                })
            }
        })
        state.pending?.let { pending ->
            put("pending", JSONObject().apply {
                put("sourceSeat", pending.sourceSeat)
                put("tile", pending.tileId)
                put("source", pending.source.name)
                put("addedMeld", pending.addedMeldIndex)
                put("responders", pending.responders.toList().toJsonArray())
                put("choices", JSONArray().apply {
                    pending.choices.forEach { (seat, choice) ->
                        put(JSONObject().apply {
                            put("seat", seat)
                            put("type", choice.type.name)
                            put("chiStart", choice.chiStart)
                        })
                    }
                })
            })
        }
    }.toString()

    fun decode(json: String): MahjongState {
        val root = JSONObject(json)
        require(root.getInt("version") == VERSION)
        val players = root.getJSONArray("players").let { array ->
            (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                PlayerState(
                    hand = item.getJSONArray("hand").toIntList(),
                    discards = item.getJSONArray("discards").toIntList(),
                    score = item.getInt("score"),
                    melds = item.getJSONArray("melds").let { melds ->
                        (0 until melds.length()).map { meldIndex ->
                            val meld = melds.getJSONObject(meldIndex)
                            Meld(
                                type = MeldType.valueOf(meld.getString("type")),
                                tileIds = meld.getJSONArray("tiles").toIntList(),
                                fromSeat = meld.getInt("from"),
                                concealed = meld.optBoolean("concealed", false),
                            )
                        }
                    },
                )
            }
        }
        val pending = root.optJSONObject("pending")?.let { item ->
            val choicesArray = item.getJSONArray("choices")
            PendingClaim(
                sourceSeat = item.getInt("sourceSeat"),
                tileId = item.getInt("tile"),
                source = ClaimSource.valueOf(item.getString("source")),
                responders = item.getJSONArray("responders").toIntList().toSet(),
                choices = (0 until choicesArray.length()).associate { index ->
                    val choice = choicesArray.getJSONObject(index)
                    choice.getInt("seat") to ClaimChoice(
                        type = ClaimType.valueOf(choice.getString("type")),
                        chiStart = choice.optInt("chiStart", -1),
                    )
                },
                addedMeldIndex = item.optInt("addedMeld", -1),
            )
        }
        return MahjongState(
            players = players,
            wall = root.getJSONArray("wall").toIntList(),
            dealerSeat = root.getInt("dealer"),
            currentSeat = root.getInt("current"),
            phase = GamePhase.valueOf(root.getString("phase")),
            drawnTileId = if (root.has("drawn")) root.getInt("drawn") else null,
            pending = pending,
            revision = root.optLong("revision", 0),
            seed = root.getLong("seed"),
        )
    }
}

private fun List<Int>.toJsonArray(): JSONArray = JSONArray().apply { forEach(::put) }

private fun JSONArray.toIntList(): List<Int> =
    (0 until length()).map(::getInt)
