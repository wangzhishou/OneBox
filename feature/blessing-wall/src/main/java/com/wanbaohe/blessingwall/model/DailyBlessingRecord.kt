package com.wanbaohe.blessingwall.model

data class DailyBlessingRecord(
    val date: String,
    val counts: Map<BlessingType, Int>,
    val wishes: Map<BlessingType, String>,
    val total: Int,
)
