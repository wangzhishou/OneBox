package com.wanbaohe.mahjong.application.port.outbound

interface SeedProvider {
    fun nextSeed(): Long
}
