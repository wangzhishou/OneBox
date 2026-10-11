package com.shifenmiao.webview.browser

import com.shifenmiao.interfaces.bookmark.UrlBookmarkService
import com.shifenmiao.interfaces.browser.BrowserAutomationService
import com.shifenmiao.webview.browser.bookmark.UrlBookmarkServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BrowserModule {

    @Binds
    @Singleton
    abstract fun bindBrowserRepository(impl: MmkvBrowserRepository): BrowserRepository

    @Binds
    @Singleton
    abstract fun bindBrowserAutomationService(impl: BrowserAutomationServiceImpl): BrowserAutomationService

    @Binds
    @Singleton
    abstract fun bindUrlBookmarkService(impl: UrlBookmarkServiceImpl): UrlBookmarkService
}
