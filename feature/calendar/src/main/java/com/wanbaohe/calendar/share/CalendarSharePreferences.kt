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

package com.wanbaohe.calendar.share

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 分享页的轻量偏好：模板、配色、显示开关，以及按日期存的心语草稿。
 *
 * 用 SharedPreferences 而不是 Room：这些都是单页 UI 偏好，不需要查询。
 */
@Singleton
class CalendarSharePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("calendar_share_page", Context.MODE_PRIVATE)

    fun load(): Settings = Settings(
        colorStyle = CalendarShareColorStyle.fromId(prefs.getString(KEY_COLOR_STYLE, null)),
        showTodayFestival = prefs.getBoolean(KEY_SHOW_TODAY, true),
        showUpcomingFestivals = prefs.getBoolean(KEY_SHOW_UPCOMING, true),
        showWeather = prefs.getBoolean(KEY_SHOW_WEATHER, true),
        showNote = prefs.getBoolean(KEY_SHOW_NOTE, true),
    )

    fun save(settings: Settings) {
        prefs.edit()
            .putString(KEY_COLOR_STYLE, settings.colorStyle.id)
            .putBoolean(KEY_SHOW_TODAY, settings.showTodayFestival)
            .putBoolean(KEY_SHOW_UPCOMING, settings.showUpcomingFestivals)
            .putBoolean(KEY_SHOW_WEATHER, settings.showWeather)
            .putBoolean(KEY_SHOW_NOTE, settings.showNote)
            .apply()
    }

    fun note(dateKey: String): String = prefs.getString(noteKey(dateKey), null).orEmpty()

    fun saveNote(dateKey: String, note: String) {
        prefs.edit().apply {
            if (note.isBlank()) remove(noteKey(dateKey)) else putString(noteKey(dateKey), note)
        }.apply()
    }

    private fun noteKey(dateKey: String) = "note_$dateKey"

    data class Settings(
        val colorStyle: CalendarShareColorStyle,
        val showTodayFestival: Boolean,
        val showUpcomingFestivals: Boolean,
        val showWeather: Boolean,
        val showNote: Boolean,
    )

    private companion object {
        const val KEY_COLOR_STYLE = "color_style"
        const val KEY_SHOW_TODAY = "show_today"
        const val KEY_SHOW_UPCOMING = "show_upcoming"
        const val KEY_SHOW_WEATHER = "show_weather"
        const val KEY_SHOW_NOTE = "show_note"
    }
}
