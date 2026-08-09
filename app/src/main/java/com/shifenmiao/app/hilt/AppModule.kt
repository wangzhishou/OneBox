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