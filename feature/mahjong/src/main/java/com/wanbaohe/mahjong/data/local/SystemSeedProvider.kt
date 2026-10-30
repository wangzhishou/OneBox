package com.wanbaohe.mahjong.data.local

import com.wanbaohe.mahjong.application.port.outbound.SeedProvider
import javax.inject.Inject
import kotlin.random.Random

class SystemSeedProvider @Inject constructor() : SeedProvider {
    override fun nextSeed(): Long = Random.Default.nextLong()
}
