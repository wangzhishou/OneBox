package com.wanbaohe.devicestatus.di

import com.wanbaohe.devicestatus.data.AndroidPhoneStatusSource
import com.wanbaohe.devicestatus.domain.PhoneStatusSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DeviceStatusModule {
    @Binds
    abstract fun bindPhoneStatusSource(impl: AndroidPhoneStatusSource): PhoneStatusSource
}
