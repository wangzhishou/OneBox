package com.wanbaohe.core.weather.data

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import java.util.Locale

/**
 * 应用内语言优先。API 33+ 上 per-app locale 不会写进 [Locale.getDefault]，
 * 跟系统语言不一致时天气文案会留在旧语言。
 */
internal fun Context.weatherLocale(): Locale {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val selected = getSystemService(LocaleManager::class.java)
            ?.applicationLocales
            ?.takeIf { !it.isEmpty }
            ?.get(0)
        if (selected != null) return selected
    }
    return Locale.getDefault()
}
