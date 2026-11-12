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

package com.wanbaohe.fileviewer.data

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 每份文档读到哪。
 *
 * 只存字符串，键用 URI + 长度算出来——同一路径的另一个版本不会串到上一份的页码上。
 * 阅读位置对 Word 是页码（`"12"`），对 EPUB 是页面回报的 `"at 3 1200"` 原样保存。
 *
 * 夜间不在这里：那是全局主题设置，见 [FileViewerComponent] 里的 `ThemeSettingService`。
 */
@Singleton
class ViewerPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun position(key: String): String? = prefs.getString(KEY_POSITION + key, null)

    fun savePosition(key: String, value: String) {
        prefs.edit().putString(KEY_POSITION + key, value).apply()
    }

    companion object {
        private const val PREFS_NAME = "file_viewer"
        private const val KEY_POSITION = "position_"

        /** 同一份内容（URI + 字节数）得到同一个键。 */
        fun keyFor(uri: Uri, length: Long): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest("$uri|$length".toByteArray())
            return digest.joinToString("") { "%02x".format(it) }.take(32)
        }
    }
}
