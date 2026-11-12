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

package com.wanbaohe.idphoto.domain

/**
 * 证件照尺寸规格
 *
 * 预置尺寸不再硬编码在这里，而是按语言环境定义在 [IdPhotoPresetCatalog]：
 * 播种时按当前语言取本地化名称/描述写进（按语言分库的）Room 库，
 * 因此 [name]/[description] 里存的就是该语言的展示文本，UI 直接用，无需再做语言映射。
 */
data class IdPhotoSize(
    val id: Long = 0,
    val name: String,                      // 名称（预置项为该语言的本地化名称，用户自建项为用户输入）
    val widthMm: Float,                    // 宽度（毫米）
    val heightMm: Float,                   // 高度（毫米）
    val widthPx: Int,                      // 宽度（像素，300dpi）
    val heightPx: Int,                     // 高度（像素，300dpi）
    val description: String = "",          // 描述
    val isPreset: Boolean = false,         // 是否预置
    val createdAt: Long = System.currentTimeMillis(),
) {
    /**
     * 获取宽高比
     */
    val aspectRatio: Float
        get() = widthPx.toFloat() / heightPx.toFloat()

    /**
     * 格式化尺寸显示
     */
    fun formatSize(): String = "${widthMm}×${heightMm}mm"

    /**
     * 格式化像素显示
     */
    fun formatPixels(): String = "${widthPx}×${heightPx}px"
}
