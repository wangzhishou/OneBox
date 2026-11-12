/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
