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

package com.wanbaohe.markuplayers.presentation.editor

/**
 * 操作台(画布)背景:默认(跟随主题 surface 背景色)或纯色。
 * 仅影响编辑器画布显示,不参与导出;会话级状态,不持久化。
 * 底图自身的透明区域仍由图片下方的透明棋盘格标识,与本背景无关。
 */
sealed interface CanvasBackground {

    data object Default : CanvasBackground

    data class Solid(val color: Int) : CanvasBackground

}
