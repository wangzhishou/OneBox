package com.wanbaohe.core.weather.di

import com.wanbaohe.core.weather.data.cache.LocationCityCache
import com.wanbaohe.core.weather.data.cache.WeatherDataCache
import com.wanbaohe.core.weather.data.repository.WeatherRepositoryImpl
import com.wanbaohe.core.weather.data.source.QWeatherDataSource
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import com.google.gson.Gson
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
    fun provideQWeatherDataSource(): QWeatherDataSource {
        return QWeatherDataSource()
    }

    @Provides
    @Singleton
    fun provideLocationCityCache(
        @ApplicationContext context: Context,
        gson: Gson
    ): LocationCityCache {
        return LocationCityCache(context, gson)
    }

    @Provides
    @Singleton
    fun provideWeatherDataCache(
        @ApplicationContext context: Context,
        gson: Gson
    ): WeatherDataCache {
        return WeatherDataCache(context, gson)
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        dataSource: QWeatherDataSource,
        cityCache: LocationCityCache,
        weatherCache: WeatherDataCache
    ): WeatherRepository {
        return WeatherRepositoryImpl(dataSource, cityCache, weatherCache)
    }
}
