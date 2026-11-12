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

    /**
     * 取复数资源。[quantity] 只用于选择复数形式，[formatArgs] 用于填充占位符
     * （英文等语言下二者通常是同一个数字，中文只有一个复数形式）。
     */
    fun getQuantityString(resId: Int, quantity: Int, vararg formatArgs: Any): String {
        if (!::context.isInitialized) {
            throw IllegalStateException("ApplicationSingleton is not initialized")
        }
        return context.resources.getQuantityString(resId, quantity, *formatArgs)
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