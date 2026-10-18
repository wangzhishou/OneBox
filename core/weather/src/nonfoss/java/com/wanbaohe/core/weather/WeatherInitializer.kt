package com.wanbaohe.core.weather

import android.util.Log
import com.qweather.sdk.JWTGenerator
import com.qweather.sdk.QWeather
import com.shifenmiao.interfaces.singleton.AppContext

object WeatherInitializer {
    private var isInitialized = false

    // 和风天气凭据由 keystore.properties 注入 BuildConfig
    // (见 build-logic/convention/ImageToolboxLibraryPlugin.kt), 不入库;
    // 未配置时(如开源构建)为空串, 跳过初始化, 天气功能静默降级
    fun init() {
        if (isInitialized) return
        try {
            val context = AppContext.getContext().applicationContext
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

            val privateKey = if (isDebug) BuildConfig.QWeatherDebugPrivateKey else BuildConfig.QWeatherReleasePrivateKey
            val projectId = if (isDebug) BuildConfig.QWeatherDebugProjectId else BuildConfig.QWeatherReleaseProjectId
            val keyId = if (isDebug) BuildConfig.QWeatherDebugKeyId else BuildConfig.QWeatherReleaseKeyId
            val developerId = if (isDebug) BuildConfig.QWeatherDebugDeveloperId else BuildConfig.QWeatherReleaseDeveloperId

            val missing = listOf(
                "privateKey" to privateKey,
                "projectId" to projectId,
                "keyId" to keyId,
                "developerId" to developerId,
            ).filter { it.second.isBlank() }.map { it.first }
            if (missing.isNotEmpty()) {
                // 缺任一凭据都不调用 getInstance，instance 保持 null。
                // 开发者 ID 在和风控制台「设置」里，是 Q 开头的 10 位，
                // 对应 keystore.properties 的 qWeatherDebug/ReleaseDeveloperId。
                Log.w("WeatherInitializer", "QWeather credentials missing: ${missing.joinToString()}, weather disabled")
                return
            }

            val jwt = JWTGenerator(privateKey, projectId, keyId, developerId)
            QWeather.getInstance(context, "nm5rk6wehm.re.qweatherapi.com")
                .setTokenGenerator(jwt)
                .setLogEnable(isDebug)
            isInitialized = true
        } catch (e: Throwable) {
            Log.e("WeatherInitializer", "Init failed: ${e.message}", e)
        }
    }
}
