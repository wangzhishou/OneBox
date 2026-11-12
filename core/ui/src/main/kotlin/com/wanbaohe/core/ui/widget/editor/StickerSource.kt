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

package com.wanbaohe.core.ui.widget.editor

/**
 * 贴纸来源(自 markup-layers 上移 core/ui,贴纸选择面板跨模块共用)。
 * 素材路径相对 assets(如 stickers/decor/flower.svg),emoji 沿用 core/resources/emoji 的矢量图。
 */
sealed interface StickerSource {

    /** assets 内置素材,path 为 assets 内相对路径 */
    data class Asset(val path: String) : StickerSource

    /** emoji,[emojiIndex] 对应 core emoji 表中的下标 */
    data class Emoji(val emojiIndex: Int) : StickerSource

    /** AI 生成贴纸,path 为生成结果的本地文件绝对路径 */
    data class Generated(val path: String) : StickerSource
}
