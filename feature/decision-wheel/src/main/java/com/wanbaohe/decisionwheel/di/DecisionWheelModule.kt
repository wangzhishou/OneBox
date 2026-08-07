package com.wanbaohe.decisionwheel.di

import android.content.Context
import com.shifenmiao.database.decision_wheel.dao.WheelDao
import com.wanbaohe.decisionwheel.data.DecisionWheelPresetsProvider
import com.wanbaohe.decisionwheel.data.WheelRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DecisionWheelModule {

    @Provides
    @Singleton
    fun provideWheelRepository(wheelDao: WheelDao): WheelRepository {
        return WheelRepository(wheelDao)
    }

    @Provides
    @Singleton
    fun provideDecisionWheelPresetsProvider(
        @ApplicationContext context: Context
    ): DecisionWheelPresetsProvider {
        return DecisionWheelPresetsProvider(context)
    }
}
