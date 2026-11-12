/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox.core.data.image.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.t8rin.imagetoolbox.core.domain.model.Position

fun Canvas.drawBitmap(
    bitmap: Bitmap,
    position: Position,
    paint: Paint? = null,
    horizontalPadding: Float = 0f,
    verticalPadding: Float = 0f
) {
    val left = when (position) {
        Position.TopLeft, Position.CenterLeft, Position.BottomLeft -> horizontalPadding
        Position.TopCenter, Position.Center, Position.BottomCenter -> (width - bitmap.width) / 2f
        Position.TopRight, Position.CenterRight, Position.BottomRight -> (width - bitmap.width - horizontalPadding)
    }

    val top = when (position) {
        Position.TopLeft, Position.TopCenter, Position.TopRight -> verticalPadding
        Position.CenterLeft, Position.Center, Position.CenterRight -> (height - bitmap.height) / 2f
        Position.BottomLeft, Position.BottomCenter, Position.BottomRight -> (height - bitmap.height - verticalPadding)
    }

    drawBitmap(
        bitmap,
        null,
        RectF(
            left,
            top,
            bitmap.width + left,
            bitmap.height + top
        ),
        paint
    )
}

fun Canvas.drawBitmap(
    bitmap: Bitmap,
    left: Float = 0f,
    top: Float = 0f
) = drawBitmap(bitmap, left, top, Paint(Paint.ANTI_ALIAS_FLAG))