/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.privacy.redaction.di

import android.graphics.Bitmap
import com.wanbaohe.privacy.redaction.data.OcrPrivacyAnalyzer
import com.wanbaohe.privacy.redaction.data.render.BitmapPrivacyRedactionRenderer
import com.wanbaohe.privacy.redaction.domain.render.PrivacyRedactionRenderer
import com.wanbaohe.privacy.redaction.domain.service.PrivacyAnalyzer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface PrivacyRedactionModule {
    @Binds
    @Singleton
    fun bindAnalyzer(analyzer: OcrPrivacyAnalyzer): PrivacyAnalyzer

    @Binds
    @Singleton
    fun bindRenderer(
        renderer: BitmapPrivacyRedactionRenderer,
    ): PrivacyRedactionRenderer<Bitmap>
}
