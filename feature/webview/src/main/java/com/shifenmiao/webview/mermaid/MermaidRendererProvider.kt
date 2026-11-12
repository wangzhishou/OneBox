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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.halilibo.richtext.ui.mermaid.LocalMermaidRenderer
import com.halilibo.richtext.ui.mermaid.MermaidRenderer
import java.util.concurrent.atomic.AtomicReference

@Composable
fun ProvideMermaidRenderer(
    content: @Composable () -> Unit
) {
    val renderer = rememberMermaidRenderer()
    CompositionLocalProvider(
        LocalMermaidRenderer provides renderer,
        content = content
    )
}

@Composable
fun rememberMermaidRenderer(): MermaidRenderer = remember {
    LazyMermaidRenderer { MermaidRendererImpl() }
}

private class LazyMermaidRenderer(
    private val factory: () -> MermaidRenderer
) : MermaidRenderer {

    private val delegateRef = AtomicReference<MermaidRenderer?>(null)

    private fun delegate(): MermaidRenderer {
        delegateRef.get()?.let { return it }

        val created = factory()
        return if (delegateRef.compareAndSet(null, created)) {
            created
        } else {
            delegateRef.get() ?: created
        }
    }

    @Composable
    override fun RenderDiagram(
        code: String,
        modifier: Modifier,
    ) {
        delegate().RenderDiagram(code = code, modifier = modifier)
    }

    override suspend fun getCachedBitmap(code: String) = delegate().getCachedBitmap(code)

    override fun getCachedSvg(code: String) = delegate().getCachedSvg(code)

    override fun getCachedSvgFile(code: String): java.io.File? = delegate().getCachedSvgFile(code)
}
