package com.wanbaohe.chess.application.usecase

internal object ImportedHistoryCursor {
    fun read(value: Any?, recordedPlies: Int): Int {
        require(recordedPlies >= 0)
        if (value == null) return recordedPlies
        require(value is Number) { "currentPly must be an integer" }
        val cursor = value.toDouble()
        require(cursor.isFinite() && cursor in 0.0..recordedPlies.toDouble() &&
            cursor == cursor.toInt().toDouble()
        ) { "currentPly must refer to a recorded position" }
        return cursor.toInt()
    }

    fun afterSkipping(cursor: Int, skipped: List<SkippedPly>): Int =
        cursor - skipped.count { it.ply <= cursor }
}
