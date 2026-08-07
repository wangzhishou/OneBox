package com.wanbaohe.blessingwall.component

import com.shifenmiao.model.remote.BlessingWallTabText
import com.wanbaohe.blessingwall.model.BlessingTabCustomization
import com.wanbaohe.blessingwall.model.BlessingType

data class BlessingWallUiState(
    val currentPage: Int = 0,
    val todayCounts: Map<BlessingType, Int> = emptyMap(),
    val wishes: Map<BlessingType, String> = emptyMap(),
    val tabCustomizations: Map<BlessingType, BlessingTabCustomization> = emptyMap(),
    val remoteTabTexts: Map<BlessingType, BlessingWallTabText> = emptyMap(),
)
