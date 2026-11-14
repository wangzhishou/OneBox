/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.di

import android.graphics.Bitmap
import com.shifenmiao.app.watermarking.data.context.DefaultWatermarkContentResolver
import com.shifenmiao.app.watermarking.data.export.AndroidWatermarkMetadataWriter
import com.shifenmiao.app.watermarking.data.preset.FileWatermarkPresetStore
import com.shifenmiao.app.watermarking.data.render.AndroidHiddenWatermarkHandler
import com.shifenmiao.app.watermarking.data.render.BitmapWatermarkRenderer
import com.shifenmiao.app.watermarking.data.render.BitmapWatermarkVisualFactory
import com.shifenmiao.app.watermarking.domain.export.WatermarkMetadataWriter
import com.shifenmiao.app.watermarking.domain.preset.WatermarkPresetStore
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkDetector
import com.shifenmiao.app.watermarking.domain.render.HiddenWatermarkEmbedder
import com.shifenmiao.app.watermarking.domain.render.WatermarkContentResolver
import com.shifenmiao.app.watermarking.domain.render.WatermarkRenderer
import com.shifenmiao.app.watermarking.domain.render.WatermarkVisualFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface WatermarkingModule {
    @Singleton
    @Binds
    fun bindContentResolver(
        resolver: DefaultWatermarkContentResolver,
    ): WatermarkContentResolver

    @Singleton
    @Binds
    fun bindVisualFactory(
        factory: BitmapWatermarkVisualFactory,
    ): WatermarkVisualFactory<Bitmap>

    @Singleton
    @Binds
    fun bindRenderer(
        renderer: BitmapWatermarkRenderer,
    ): WatermarkRenderer<Bitmap>

    @Singleton
    @Binds
    fun bindHiddenWatermarkDetector(
        handler: AndroidHiddenWatermarkHandler,
    ): HiddenWatermarkDetector<Bitmap>

    @Singleton
    @Binds
    fun bindHiddenWatermarkEmbedder(
        handler: AndroidHiddenWatermarkHandler,
    ): HiddenWatermarkEmbedder<Bitmap>

    @Singleton
    @Binds
    fun bindPresetStore(
        store: FileWatermarkPresetStore,
    ): WatermarkPresetStore

    @Singleton
    @Binds
    fun bindMetadataWriter(
        writer: AndroidWatermarkMetadataWriter,
    ): WatermarkMetadataWriter
}
