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

import androidx.annotation.StringRes
import com.shifenmiao.model.imageprocess.RetouchParams
import com.wanbaohe.idphoto.R

/**
 * AI 人像美化档位:每档对应一组百度 AI 修图(retouching)参数,
 * 实际调用走 core/network 的 BaiduImageProcessRepository.retouch()。
 * 单项微调见 [BEAUTY_PARAM_CATALOG];参数与某档完全一致时面板高亮该档,否则视为自定义。
 */
enum class BeautyLevel(
    @StringRes val labelRes: Int,
    val params: RetouchParams
) {
    LIGHT(
        labelRes = R.string.id_photo_beauty_light,
        params = RetouchParams(
            mapOf(
                "face_smooth" to 0.3f,
                "skin_white" to 0.2f,
                "remove_face_flaw" to 0.4f,
                "skin_red" to 0.1f,
                "shiny_eye" to 0.1f,
            )
        )
    ),
    STANDARD(
        labelRes = R.string.id_photo_beauty_standard,
        params = RetouchParams(
            mapOf(
                "face_smooth" to 0.5f,
                "skin_white" to 0.35f,
                "remove_face_flaw" to 0.6f,
                "skin_red" to 0.2f,
                "shiny_eye" to 0.2f,
            )
        )
    ),
    DEEP(
        labelRes = R.string.id_photo_beauty_deep,
        params = RetouchParams(
            mapOf(
                "face_smooth" to 0.75f,
                "skin_white" to 0.5f,
                "remove_face_flaw" to 0.85f,
                "skin_red" to 0.35f,
                "shiny_eye" to 0.35f,
            )
        )
    ),
}
