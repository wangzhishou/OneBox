package com.wanbaohe.mahjong.di

import com.wanbaohe.mahjong.application.port.outbound.MahjongAi
import com.wanbaohe.mahjong.application.port.outbound.MahjongStore
import com.wanbaohe.mahjong.application.port.outbound.SeedProvider
import com.wanbaohe.mahjong.data.local.LocalMahjongAi
import com.wanbaohe.mahjong.data.local.MmkvMahjongStore
import com.wanbaohe.mahjong.data.local.SystemSeedProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MahjongModule {
    @Binds
    @Singleton
    abstract fun bindMahjongAi(impl: LocalMahjongAi): MahjongAi

    @Binds
    @Singleton
    abstract fun bindMahjongStore(impl: MmkvMahjongStore): MahjongStore

    @Binds
    @Singleton
    abstract fun bindSeedProvider(impl: SystemSeedProvider): SeedProvider
}
