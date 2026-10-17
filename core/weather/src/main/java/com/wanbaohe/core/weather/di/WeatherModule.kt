package com.wanbaohe.core.weather.di

import com.wanbaohe.core.weather.data.cache.LocationCityCache
import com.wanbaohe.core.weather.data.cache.WeatherDataCache
import com.wanbaohe.core.weather.data.location.AndroidLocationProvider
import com.wanbaohe.core.weather.data.repository.WeatherRepositoryImpl
import com.wanbaohe.core.weather.data.source.QWeatherDataSource
import com.wanbaohe.core.weather.domain.repository.LocationProvider
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import android.content.Context

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideQWeatherDataSource(
        @ApplicationContext context: Context,
    ): QWeatherDataSource {
        return QWeatherDataSource(context)
    }

    @Provides
    @Singleton
    fun provideLocationCityCache(
        @ApplicationContext context: Context,
    ): LocationCityCache {
        return LocationCityCache(context)
    }

    @Provides
    @Singleton
    fun provideWeatherDataCache(
        @ApplicationContext context: Context,
    ): WeatherDataCache {
        return WeatherDataCache(context)
    }

    @Provides
    @Singleton
    fun provideLocationProvider(
        @ApplicationContext context: Context,
    ): LocationProvider {
        return AndroidLocationProvider(context)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        dataSource: QWeatherDataSource,
        cityCache: LocationCityCache,
        weatherCache: WeatherDataCache,
        @ApplicationContext context: Context,
    ): WeatherRepository {
        return WeatherRepositoryImpl(dataSource, cityCache, weatherCache, context)
    }
}
