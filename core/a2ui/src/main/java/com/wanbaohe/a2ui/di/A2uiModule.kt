package com.wanbaohe.a2ui.di

import com.wanbaohe.a2ui.transport.A2uiTransport
import com.wanbaohe.a2ui.transport.impl.StubA2uiTransport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class A2uiModule {

    @Binds
    @Singleton
    abstract fun bindTransport(impl: StubA2uiTransport): A2uiTransport
}
