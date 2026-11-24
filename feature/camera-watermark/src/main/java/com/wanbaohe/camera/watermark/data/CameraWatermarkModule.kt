/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.data

import com.wanbaohe.camera.watermark.domain.CameraWatermarkPreferences
import com.wanbaohe.camera.watermark.domain.GalleryLauncher
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraWatermarkModule {
    @Binds
    abstract fun bindPreferences(
        implementation: SharedPreferencesCameraWatermarkPreferences,
    ): CameraWatermarkPreferences

    @Binds
    abstract fun bindGalleryLauncher(
        implementation: AndroidGalleryLauncher,
    ): GalleryLauncher
}
