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
