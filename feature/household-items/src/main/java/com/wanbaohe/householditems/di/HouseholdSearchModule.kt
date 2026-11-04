package com.wanbaohe.householditems.di

import com.shifenmiao.common.search.GlobalSearchProvider
import com.wanbaohe.householditems.search.HouseholdSearchProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
object HouseholdSearchModule {

    @Provides
    @IntoSet
    fun provideHouseholdSearchProvider(provider: HouseholdSearchProvider): GlobalSearchProvider = provider
}
