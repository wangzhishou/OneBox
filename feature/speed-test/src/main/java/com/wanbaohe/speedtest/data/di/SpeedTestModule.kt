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

package com.wanbaohe.speedtest.data.di

import com.wanbaohe.speedtest.data.SpeedTestConfigRepository
import com.wanbaohe.speedtest.data.SpeedTestConfigRepositoryImpl
import com.wanbaohe.speedtest.data.SpeedTestRepository
import com.wanbaohe.speedtest.data.SpeedTestRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeedTestModule {

    /** 将 SpeedTestRepositoryImpl 绑定为 SpeedTestRepository */
    @Binds
    @Singleton
    abstract fun bindSpeedTestRepository(impl: SpeedTestRepositoryImpl): SpeedTestRepository

    @Binds
    @Singleton
    abstract fun bindSpeedTestConfigRepository(impl: SpeedTestConfigRepositoryImpl): SpeedTestConfigRepository

    companion object {
        /** 专用 OkHttpClient：无鉴权拦截，超时适配大文件下载 */
        @Provides
        @Named("SpeedTestOkHttpClient")
        fun provideSpeedTestOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
