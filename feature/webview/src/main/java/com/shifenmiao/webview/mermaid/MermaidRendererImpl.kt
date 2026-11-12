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

package com.shifenmiao.webview.mermaid

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.mermaid.MermaidRenderer
import com.shifenmiao.interfaces.singleton.AppContext
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Size
import coil3.svg.SvgDecoder
import coil3.toBitmap
import java.io.File

class MermaidRendererImpl : MermaidRenderer {

    @Composable
    override fun RenderDiagram(
        code: String,
        modifier: Modifier,
        language: String,
    ) {
        NativeMermaidDiagram(code = code, modifier = modifier, language = language)
    }

    override suspend fun getCachedBitmap(code: String, language: String): Bitmap? {
        val file = getCachedSvgFile(code, language) ?: return null
        return try {
            val context = AppContext.getContext()
            val imageLoader = ImageLoader.Builder(context)
                .components { add(SvgDecoder.Factory()) }
                .build()

            val request = ImageRequest.Builder(context)
                .data(file)
                .size(Size.ORIGINAL)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)
            result.image?.toBitmap()
        } catch (_: Exception) {
            null
        }
    }

    override fun getCachedSvg(code: String, language: String): String? {
        return MermaidBitmapCache.get(code, isDark = true, language = language)?.svgString
            ?: MermaidBitmapCache.get(code, isDark = false, language = language)?.svgString
    }

    override fun getCachedSvgFile(code: String, language: String): File? {
        return MermaidBitmapCache.get(code, isDark = true, language = language)?.svgFile
            ?: MermaidBitmapCache.get(code, isDark = false, language = language)?.svgFile
    }
}
