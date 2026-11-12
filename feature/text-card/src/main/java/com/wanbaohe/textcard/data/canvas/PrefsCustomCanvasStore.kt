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

package com.wanbaohe.textcard.data.canvas

import android.content.Context
import com.wanbaohe.textcard.domain.CustomCanvasStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** SharedPreferences 持久化上次自定义画布宽高 */
@Singleton
internal class PrefsCustomCanvasStore @Inject constructor(
    @ApplicationContext context: Context,
) : CustomCanvasStore {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun lastCustom(): Pair<Int, Int>? {
        val width = prefs.getInt(KEY_WIDTH, 0)
        val height = prefs.getInt(KEY_HEIGHT, 0)
        if (width <= 0 || height <= 0) return null
        return width to height
    }

    override fun saveLastCustom(width: Int, height: Int) {
        prefs.edit()
            .putInt(KEY_WIDTH, width)
            .putInt(KEY_HEIGHT, height)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "text_card"
        const val KEY_WIDTH = "custom_canvas_width"
        const val KEY_HEIGHT = "custom_canvas_height"
    }
}
