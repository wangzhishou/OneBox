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

package com.shifenmiao.common.file

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

object TempBitmapFileUtils {

    /**
     * 保存位图到临时文件并返回 URI 字符串
     */
    fun saveBitmapToTempFile(context: Context, bitmap: Bitmap): String {
        val tempFile = File(context.cacheDir, "temp_webview_${System.currentTimeMillis()}.png")
        try {
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            return tempFile.toURI().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}

