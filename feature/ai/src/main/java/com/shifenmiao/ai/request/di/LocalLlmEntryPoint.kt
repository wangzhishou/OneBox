package com.shifenmiao.ai.request.di

import com.shifenmiao.ai.request.OnDeviceLlmService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface LocalLlmEntryPoint {
    fun onDeviceLlmService(): OnDeviceLlmService
}
