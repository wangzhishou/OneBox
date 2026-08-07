package com.shifenmiao.interfaces.singleton

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.core.view.WindowInsetsCompat

/**
 * Created by wangzhishou@gmail.com in 2020-01-05
 * Singleton class to provide application context
 */
@SuppressLint("StaticFieldLeak")
object AppContext {

    var isAppColdStart = true

    val NAV_BARS = WindowInsetsCompat.Type.navigationBars()
    val SYSTEM_BARS = WindowInsetsCompat.Type.systemBars()
    val STATUS_BARS = WindowInsetsCompat.Type.statusBars()

    @SuppressLint("StaticFieldLeak")
    private lateinit var context: Context

    private var currentScreen: String = "-1"

    private var currentScreenName: String = "NewApp"

    private var isDarkTheme: Boolean = false

    private var colorScheme: ColorScheme = lightColorScheme()

    fun init(context: Context) {
        this.context = context.applicationContext
        this.currentScreen = "-1"
        this.currentScreenName = "NewApp"
    }

    fun isDarkTheme(): Boolean {
        return isDarkTheme
    }

    fun setColorScheme(colorScheme: ColorScheme) {
        this.colorScheme = colorScheme
    }

    fun getColorScheme(): ColorScheme {
        return colorScheme
    }

    fun setDarkTheme(isDarkTheme: Boolean) {
        this.isDarkTheme = isDarkTheme
    }

    fun getString(resId: Int): String {
        if (!::context.isInitialized) {
            throw IllegalStateException("ApplicationSingleton is not initialized")
        }
        return context.getString(resId)
    }


    fun getString(resId: Int, string: String): String {
        if (!::context.isInitialized) {
            throw IllegalStateException("ApplicationSingleton is not initialized")
        }
        return context.getString(resId, string)
    }

    fun getContext(): Context {
        if (!::context.isInitialized) {
            throw IllegalStateException("ApplicationSingleton is not initialized")
        }
        return context
    }

    fun getCurrentScreen(): String {
        return currentScreen
    }

    fun setCurrentScreen(screen: String) {
        currentScreen = screen
    }

    fun getCurrentScreenName(): String {
        return currentScreenName
    }

    fun setCurrentScreenName(screenName: String) {
        currentScreenName = screenName
    }
}