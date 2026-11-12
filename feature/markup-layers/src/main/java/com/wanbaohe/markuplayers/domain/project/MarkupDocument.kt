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

package com.wanbaohe.markuplayers.domain.project

import com.wanbaohe.markuplayers.domain.model.BaseTransformSpec
import com.wanbaohe.markuplayers.domain.model.LayerEffect
import com.wanbaohe.markuplayers.domain.model.LayerMask
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.presentation.editor.CanvasBackground
import com.wanbaohe.markuplayers.presentation.export.ExportSettings
import com.wanbaohe.markuplayers.presentation.tools.adjust.BaseAdjustments

/**
 * The complete editable document. Geometry stays normalized; pixel dimensions describe the base.
 * Asset references are normalized to private, durable files when the project is saved.
 */
data class MarkupDocument(
    val baseUri: String,
    val width: Int,
    val height: Int,
    val layers: List<MarkupLayer>,
    val baseAdjustments: BaseAdjustments = BaseAdjustments(),
    val baseEffects: List<LayerEffect> = emptyList(),
    val baseMask: LayerMask? = null,
    /** 底图变换(裁剪/旋转/翻转)参数序列,重开项目可回放,撤销随文档快照 */
    val baseTransforms: List<BaseTransformSpec> = emptyList(),
    /** 底图资产版本链(AI 替换/变换产物),供 AI 回退与素材回收 */
    val baseImageHistory: List<String> = emptyList(),
    val exportSettings: ExportSettings = ExportSettings(),
    val originalUri: String? = null,
    /** 用户原始来源(相册 content:// 等),仅用于"覆盖原图"判定与展示,不参与渲染 */
    val sourceUri: String? = null,
    val saveExif: Boolean = false,
    val workspaceBackground: CanvasBackground = CanvasBackground.Default,
    /** 底图(背景)是否可见;不可见时预览/导出均为透明底,图层照常渲染 */
    val baseVisible: Boolean = true,
    /** 背景在 z 序中的位置:渲染在背景下方的图层数(0 = 背景在最底层,默认) */
    val baseOrder: Int = 0,
)
