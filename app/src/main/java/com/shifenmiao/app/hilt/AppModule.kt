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

package com.shifenmiao.app.hilt

import android.content.Context
import com.shifenmiao.ai.usecase.MessageListUseCase
import com.shifenmiao.app.BuildConfig as AppBuildConfig
import com.shifenmiao.base.channel.ChannelConfig
import com.shifenmiao.base.hilt.DeviceInfoModule
import com.shifenmiao.base.hilt.ResourceProvider
import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.common.manager.AIEngineRepository
import com.shifenmiao.core.common.EnvironmentModule
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.database.AppDatabase
import com.shifenmiao.database.activity.ActivityLogRecorder
import com.shifenmiao.database.ai.dao.MessageDao
import com.shifenmiao.network.api.ApiService
import com.shifenmiao.network.di.EnvironmentModuleImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesDeviceInfoModule(@ApplicationContext context: Context) = DeviceInfoModule(context)

    @Provides
    @Singleton
    fun providesResourceProvider(@ApplicationContext context: Context) = ResourceProvider(context)

    @Singleton
    @Provides
    fun providesEnvironmentModule(): EnvironmentModule {
        return EnvironmentModuleImpl()
    }

    @Singleton
    @Provides
    fun providesMessageListUseCase(
        messageDao: MessageDao,
        activityLogRecorder: ActivityLogRecorder,
        apiService: ApiService
    ): MessageListUseCase {
        return MessageListUseCase(
            messageDao,
            activityLogRecorder,
            apiService
        )
    }


    @Provides
    @Singleton
    fun provideAIEngineRepository(
        appDatabase: AppDatabase
    ): AIEngineRepository {
        return AIEngineRepository(appDatabase)
    }

    @Provides
    @Singleton
    fun provideAIEngineManager(
        aiEngineRepository: AIEngineRepository,
    ): AIEngineManager {
        return AIEngineManager(aiEngineRepository)
    }

    @Provides
    @Singleton
    fun provideChannelConfig(): ChannelConfig = ChannelConfig(
        enableWechat = AppBuildConfig.ENABLE_WECHAT,
        enableAlipay = AppBuildConfig.ENABLE_ALIPAY,
        enableHms = AppBuildConfig.ENABLE_HMS,
        enableGms = AppBuildConfig.ENABLE_GMS,
        apiBaseUrl = AppBuildConfig.API_BASE_URL,
        webBaseUrl = AppBuildConfig.WEB_BASE_URL,
        privacyPolicyUrl = AppBuildConfig.PRIVACY_POLICY_URL,
        userAgreementUrl = AppBuildConfig.USER_AGREEMENT_URL,
        showLanguageSetting = AppBuildConfig.SHOW_LANGUAGE_SETTING,
        enablePlayBilling = AppBuildConfig.ENABLE_PLAY_BILLING,
    )

}