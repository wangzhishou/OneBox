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

package com.wanbaohe.markuplayers.domain

import com.wanbaohe.markuplayers.domain.model.MarkupLayer

/**
 * 把图层列表按 z 序逐层重绘到原图的可变副本上,输出与原图同分辨率的位图。
 * [filteredLayers] 可提供已完成顺序效果与局部 alpha 蒙版的导出内容(layerId → 位图)。
 * 不应传低分辨率预览缓存。缺失内容由实现按图层串行预算,解码/滤镜失败显式失败,
 * 不会以未处理内容替代。定位、调色、不透明度与混合模式在合成阶段应用。
 */
interface MarkupLayersApplier<I> {

    suspend fun applyToImage(
        image: I,
        layers: List<MarkupLayer>,
        filteredLayers: Map<String, I> = emptyMap(),
    ): I
}
