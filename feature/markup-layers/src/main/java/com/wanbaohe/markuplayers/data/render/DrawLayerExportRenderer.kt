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

package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.wanbaohe.core.ui.widget.editor.renderEditorStrokesBitmap
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import kotlin.reflect.KClass

class DrawLayerExportRenderer @Inject constructor() : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Draw::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        filtered: Bitmap?,
    ) {
        val type = layer.type as? LayerType.Draw ?: return
        if ((type.strokes.isEmpty() && filtered == null) || imageWidth <= 0 || imageHeight <= 0) return
        val layerBitmap = filtered ?: renderStrokesBitmap(type.strokes, imageWidth, imageHeight)
        canvas.drawBitmap(
            layerBitmap,
            null,
            RectF(-imageWidth / 2f, -imageHeight / 2f, imageWidth / 2f, imageHeight / 2f),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    fun renderStrokesBitmap(
        strokes: List<DrawStroke>,
        imageWidth: Int,
        imageHeight: Int,
    ): Bitmap = renderEditorStrokesBitmap(strokes, imageWidth, imageHeight)
}
