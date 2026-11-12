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

package com.wanbaohe.camera.watermark.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.LogoType
import com.wanbaohe.camera.watermark.domain.WatermarkStyle

/**
 * 预置模板展示名本地化。
 *
 * 预置模板的名称在播种时以中文写入数据库（见 WatermarkTemplateRepository.getPresetTemplates），
 * 数据库中的值是历史数据，也作为映射依据，保持不变；展示层仅对预置模板（isPreset=true）
 * 按 logoType 映射到多语言字符串资源。
 * 用户自建模板（isPreset=false）不参与映射，直接展示数据库中用户自己起的名称。
 */
@Composable
fun localizedTemplateName(style: WatermarkStyle): String {
    if (!style.isPreset) return style.name
    return when (style.logoType) {
        LogoType.LEICA -> stringResource(R.string.camera_watermark_style_leica)
        LogoType.WANBAOHE -> stringResource(R.string.camera_watermark_style_wanbaohe)
        LogoType.HUAWEI -> stringResource(R.string.camera_watermark_style_huawei)
        LogoType.XIAOMI -> stringResource(R.string.camera_watermark_style_xiaomi)
        // APPLE/GOOGLE/OPPO/VIVO 等播种值本身即为拉丁字母，直接展示库中的名称
        else -> style.name
    }
}
