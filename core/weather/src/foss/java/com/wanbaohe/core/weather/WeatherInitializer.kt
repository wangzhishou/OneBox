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
