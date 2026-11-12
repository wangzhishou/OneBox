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

package com.wanbaohe.textcard.domain.model

import androidx.annotation.StringRes
import com.shifenmiao.model.canvas.CreationCanvasSpec
import com.wanbaohe.textcard.R

/**
 * 画布规格:导出位图尺寸即 width × height。
 * 内置竖版 3:4 / 方形 1:1 / 屏幕 9:16 + 用户自定义尺寸([Custom],持久化记忆上次输入)。
 */
sealed class CanvasSpec(
    val width: Int,
    val height: Int,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int,
    val ratioLabel: String,
) {

    data object Portrait : CanvasSpec(
        width = CreationCanvasSpec.PORTRAIT.width,
        height = CreationCanvasSpec.PORTRAIT.height,
        titleRes = R.string.textcard_canvas_portrait,
        descRes = R.string.textcard_canvas_portrait_desc,
        ratioLabel = CreationCanvasSpec.PORTRAIT.ratioLabel
    )

    data object Square : CanvasSpec(
        width = CreationCanvasSpec.SQUARE.width,
        height = CreationCanvasSpec.SQUARE.height,
        titleRes = R.string.textcard_canvas_square,
        descRes = R.string.textcard_canvas_square_desc,
        ratioLabel = CreationCanvasSpec.SQUARE.ratioLabel
    )

    data object Screen : CanvasSpec(
        width = CreationCanvasSpec.STORY.width,
        height = CreationCanvasSpec.STORY.height,
        titleRes = R.string.textcard_canvas_screen,
        descRes = R.string.textcard_canvas_screen_desc,
        ratioLabel = CreationCanvasSpec.STORY.ratioLabel
    )

    /** 用户自定义宽高(px,输入侧钳制 256..4096) */
    data class Custom(
        val customWidth: Int,
        val customHeight: Int,
    ) : CanvasSpec(
        width = customWidth,
        height = customHeight,
        titleRes = R.string.textcard_canvas_custom,
        descRes = R.string.textcard_canvas_custom_desc,
        ratioLabel = "${customWidth}×${customHeight}"
    )

    val aspectRatio: Float get() = width.toFloat() / height.toFloat()

    companion object {
        /**
         * 内置社交尺寸(自定义规格单独入口)。
         * 必须 lazy:直接初始化会在「首个被触发的子类是 Portrait」时
         * 因类初始化循环(父类 clinit 读取尚在初始化中的子类静态字段)
         * 读到 null。
         */
        val builtIn: List<CanvasSpec> by lazy { listOf(Portrait, Square, Screen) }
    }
}
