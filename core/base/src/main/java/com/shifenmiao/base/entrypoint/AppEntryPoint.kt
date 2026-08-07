package com.shifenmiao.base.entrypoint

import com.shifenmiao.base.auth.AuthorizationCodeStateHolder
import com.shifenmiao.base.hilt.DeviceInfoModule
import com.shifenmiao.base.hilt.ResourceProvider
import com.shifenmiao.database.ClearDatabaseHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {

    fun getDeviceInfoModule(): DeviceInfoModule

    fun getResourceProvider(): ResourceProvider

    fun getClearDatabaseHelper(): ClearDatabaseHelper

    fun getAuthorizationCodeStateHolder(): AuthorizationCodeStateHolder
}