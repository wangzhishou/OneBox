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

package com.wanbaohe.textcard.domain

import android.graphics.Bitmap
import com.wanbaohe.textcard.domain.model.TextCardRenderState

/**
 * 文字卡片导出渲染器:按实际画布规格离屏绘制完整卡片。
 * 排版几何与颜色常量与预览侧共用(见 [com.wanbaohe.textcard.domain.render.CardLayout])。
 */
interface TextCardExportRenderer {

    suspend fun render(state: TextCardRenderState): Bitmap
}
