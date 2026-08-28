package com.shifenmiao.network.update

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface OpenSourceReleaseEntryPoint {

    fun openSourceReleaseChecker(): OpenSourceReleaseChecker
}
