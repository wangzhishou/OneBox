package com.wanbaohe.core.weather

import android.util.Log

/**
 * foss (F-Droid) 渠道 stub: 不打包和风天气 SDK, 初始化为 no-op,
 * 天气功能整体不可用 (数据源见同 sourceSet 的 QWeatherDataSource stub)。
 * 签名必须与 src/nonfoss 的真实实现保持一致。
 */
object WeatherInitializer {

    fun init() {
        Log.w("WeatherInitializer", "foss build without QWeather SDK, weather disabled")
    }
}
