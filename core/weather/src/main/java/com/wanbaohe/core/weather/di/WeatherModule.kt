/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
