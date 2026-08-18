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
