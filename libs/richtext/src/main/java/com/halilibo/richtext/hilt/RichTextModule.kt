package com.halilibo.richtext.hilt

import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.ui.RichTextStyle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RichTextModule {

    @Provides
    @Singleton
    fun provideCommonMarkdownParseOptions(): CommonMarkdownParseOptions {
        return CommonMarkdownParseOptions(
            enableCodeHighlight = true,
            autolink = true
        )
    }

    @Provides
    @Singleton
    fun provideRichTextStyle(): RichTextStyle {
        return RichTextStyle()
    }
}