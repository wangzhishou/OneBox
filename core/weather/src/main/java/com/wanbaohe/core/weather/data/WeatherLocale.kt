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
