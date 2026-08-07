package com.wanbaohe.core.weather

import android.content.Context
import android.util.Log
import com.qweather.sdk.JWTGenerator
import com.qweather.sdk.QWeather
import com.shifenmiao.interfaces.singleton.AppContext

object WeatherInitializer {
    private var isInitialized = false

    // TODO: 替换为正式环境的配置参数
    private const val RELEASE_PRIVATE_KEY = "MC4CAQAwBQYDK2VwBCIEIJQEepyJXLKQ6/blxMg7AkiYgiYqtry1woZTSm/F0mp2"
    private const val RELEASE_PROJECT_ID = "4JDXGKJVTT"
    private const val RELEASE_KEY_ID = "CFPNWW277D"

    private const val DEBUG_PRIVATE_KEY = "MC4CAQAwBQYDK2VwBCIEIDhIs8M/ZQeTjgitjeGGWyZubM6ZycTX3JsVQeXOo3Kb"
    private const val DEBUG_PROJECT_ID = "4JDXGKJVTT"
    private const val DEBUG_KEY_ID = "TBPQJXJWRY"

    fun init() {
        if (isInitialized) return
        try {
            val context = AppContext.getContext().applicationContext
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            
            val privateKey = if (isDebug) DEBUG_PRIVATE_KEY else RELEASE_PRIVATE_KEY
            val projectId = if (isDebug) DEBUG_PROJECT_ID else RELEASE_PROJECT_ID
            val keyId = if (isDebug) DEBUG_KEY_ID else RELEASE_KEY_ID

            val jwt = JWTGenerator(privateKey, projectId, keyId)
            QWeather.getInstance(context, "nm5rk6wehm.re.qweatherapi.com")
                .setTokenGenerator(jwt)
                .setLogEnable(isDebug)
            isInitialized = true
        } catch (e: Throwable) {
            Log.e("WeatherInitializer", "Init failed: ${e.message}", e)
        }
    }
}
