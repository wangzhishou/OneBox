package com.wanbaohe.idphoto.domain

import androidx.annotation.StringRes
import com.wanbaohe.idphoto.R

/**
 * AI 美化单项参数目录:按百度修图文档分组,每组若干可调项。
 * key 即接口功能名;[BeautyParamSpec.min]/[BeautyParamSpec.max] 为取值范围(面部重塑类为 [-1,1]);
 * 开关型(isSwitch)取值 0/1;[BeautyParamSpec.companions] 为文档要求"组内一起使用"的联动参数,
 * 设置主项时自动以给定值补齐,清除主项时一并移除。
 * [BeautyParamSpec.descRes] 为卡片上的小字说明(数值含义),来自接口文档描述。
 *
 * 妆容/滤镜等枚举类型参数(makeup_*_id、lut_id 等)暂不覆盖。
 */
data class BeautyParamSpec(
    val key: String,
    @StringRes val labelRes: Int,
    @StringRes val descRes: Int,
    val min: Float = 0f,
    val max: Float = 1f,
    val isSwitch: Boolean = false,
    val companions: Map<String, Float> = emptyMap(),
)

data class BeautyParamGroup(
    @StringRes val titleRes: Int,
    val specs: List<BeautyParamSpec>,
)

val BEAUTY_PARAM_CATALOG: List<BeautyParamGroup> = listOf(
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_skin,
        specs = listOf(
            BeautyParamSpec("face_smooth", R.string.id_photo_bp_face_smooth, R.string.id_photo_bp_desc_face_smooth),
            BeautyParamSpec("face_smooth_gray", R.string.id_photo_bp_face_smooth_gray, R.string.id_photo_bp_desc_face_smooth_gray),
            BeautyParamSpec(
                "face_smooth_fine", R.string.id_photo_bp_face_smooth_fine, R.string.id_photo_bp_desc_face_smooth_fine,
                companions = mapOf("face_smooth_highpass" to 0f, "face_smooth_lowpass" to 0f)
            ),
            BeautyParamSpec("face_smooth_new", R.string.id_photo_bp_face_smooth_new, R.string.id_photo_bp_desc_face_smooth_new),
            BeautyParamSpec("skin_white", R.string.id_photo_bp_skin_white, R.string.id_photo_bp_desc_skin_white),
            BeautyParamSpec("skin_bright", R.string.id_photo_bp_skin_bright, R.string.id_photo_bp_desc_skin_bright),
            BeautyParamSpec("skin_red", R.string.id_photo_bp_skin_red, R.string.id_photo_bp_desc_skin_red),
            BeautyParamSpec("skin_sharpen", R.string.id_photo_bp_skin_sharpen, R.string.id_photo_bp_desc_skin_sharpen),
            BeautyParamSpec("face_color_same", R.string.id_photo_bp_face_color_same, R.string.id_photo_bp_desc_face_color_same),
            BeautyParamSpec("skin_prefer", R.string.id_photo_bp_skin_prefer, R.string.id_photo_bp_desc_skin_prefer, min = -1f),
            BeautyParamSpec("face_highlight", R.string.id_photo_bp_face_highlight, R.string.id_photo_bp_desc_face_highlight),
            BeautyParamSpec("face_shadow", R.string.id_photo_bp_face_shadow, R.string.id_photo_bp_desc_face_shadow),
        )
    ),
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_flaw,
        specs = listOf(
            BeautyParamSpec("remove_face_flaw", R.string.id_photo_bp_remove_face_flaw, R.string.id_photo_bp_desc_remove_face_flaw),
            BeautyParamSpec("remove_dark_circles", R.string.id_photo_bp_remove_dark_circles, R.string.id_photo_bp_desc_remove_dark_circles),
            BeautyParamSpec("remove_eye_around_wrinkles", R.string.id_photo_bp_remove_eye_around_wrinkles, R.string.id_photo_bp_desc_remove_eye_around_wrinkles),
            BeautyParamSpec("remove_forehead_wrinkles", R.string.id_photo_bp_remove_forehead_wrinkles, R.string.id_photo_bp_desc_remove_forehead_wrinkles),
            BeautyParamSpec("remove_laugh_line", R.string.id_photo_bp_remove_laugh_line, R.string.id_photo_bp_desc_remove_laugh_line),
            BeautyParamSpec("remove_neck_wrinkles", R.string.id_photo_bp_remove_neck_wrinkles, R.string.id_photo_bp_desc_remove_neck_wrinkles),
            BeautyParamSpec("remove_double_chin", R.string.id_photo_bp_remove_double_chin, R.string.id_photo_bp_desc_remove_double_chin),
            BeautyParamSpec("remove_lip_wrinkles", R.string.id_photo_bp_remove_lip_wrinkles, R.string.id_photo_bp_desc_remove_lip_wrinkles),
            BeautyParamSpec("remove_face_glossy", R.string.id_photo_bp_remove_face_glossy, R.string.id_photo_bp_desc_remove_face_glossy),
            BeautyParamSpec("remove_face_moles", R.string.id_photo_bp_remove_face_moles, R.string.id_photo_bp_desc_remove_face_moles, isSwitch = true),
        )
    ),
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_reshape,
        specs = listOf(
            BeautyParamSpec("face_small", R.string.id_photo_bp_face_small, R.string.id_photo_bp_desc_face_small, min = -1f),
            BeautyParamSpec("face_thin", R.string.id_photo_bp_face_thin, R.string.id_photo_bp_desc_face_thin, min = -1f),
            BeautyParamSpec("face_v", R.string.id_photo_bp_face_v, R.string.id_photo_bp_desc_face_v, min = -1f),
            BeautyParamSpec("jaw_width", R.string.id_photo_bp_jaw_width, R.string.id_photo_bp_desc_jaw_width, min = -1f),
            BeautyParamSpec("cheekbone_width", R.string.id_photo_bp_cheekbone_width, R.string.id_photo_bp_desc_cheekbone_width, min = -1f),
            BeautyParamSpec("face_width", R.string.id_photo_bp_face_width, R.string.id_photo_bp_desc_face_width, min = -1f),
            BeautyParamSpec("forehead_height", R.string.id_photo_bp_forehead_height, R.string.id_photo_bp_desc_forehead_height, min = -1f),
            BeautyParamSpec("chin_height", R.string.id_photo_bp_chin_height, R.string.id_photo_bp_desc_chin_height, min = -1f),
            BeautyParamSpec("face_symmetry", R.string.id_photo_bp_face_symmetry, R.string.id_photo_bp_desc_face_symmetry),
            BeautyParamSpec("eyebrow_thickness", R.string.id_photo_bp_eyebrow_thickness, R.string.id_photo_bp_desc_eyebrow_thickness, min = -1f),
            BeautyParamSpec("eyebrow_distance", R.string.id_photo_bp_eyebrow_distance, R.string.id_photo_bp_desc_eyebrow_distance, min = -1f),
            BeautyParamSpec("eyebrow_height", R.string.id_photo_bp_eyebrow_height, R.string.id_photo_bp_desc_eyebrow_height, min = -1f),
            BeautyParamSpec("eye_scale", R.string.id_photo_bp_eye_scale, R.string.id_photo_bp_desc_eye_scale, min = -1f),
            BeautyParamSpec("eye_distance", R.string.id_photo_bp_eye_distance, R.string.id_photo_bp_desc_eye_distance, min = -1f),
            BeautyParamSpec("eye_angle", R.string.id_photo_bp_eye_angle, R.string.id_photo_bp_desc_eye_angle, min = -1f),
            BeautyParamSpec("nose_scale", R.string.id_photo_bp_nose_scale, R.string.id_photo_bp_desc_nose_scale, min = -1f),
            BeautyParamSpec("nose_height", R.string.id_photo_bp_nose_height, R.string.id_photo_bp_desc_nose_height, min = -1f),
            BeautyParamSpec("nose_bridge", R.string.id_photo_bp_nose_bridge, R.string.id_photo_bp_desc_nose_bridge, min = -1f),
            BeautyParamSpec("nose_wing", R.string.id_photo_bp_nose_wing, R.string.id_photo_bp_desc_nose_wing, min = -1f),
            BeautyParamSpec("nose_tip", R.string.id_photo_bp_nose_tip, R.string.id_photo_bp_desc_nose_tip, min = -1f),
            BeautyParamSpec("mouth_scale", R.string.id_photo_bp_mouth_scale, R.string.id_photo_bp_desc_mouth_scale, min = -1f),
            BeautyParamSpec("mouth_width", R.string.id_photo_bp_mouth_width, R.string.id_photo_bp_desc_mouth_width, min = -1f),
            BeautyParamSpec("mouth_position", R.string.id_photo_bp_mouth_position, R.string.id_photo_bp_desc_mouth_position, min = -1f),
        )
    ),
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_eye,
        specs = listOf(
            BeautyParamSpec("shiny_eye", R.string.id_photo_bp_shiny_eye, R.string.id_photo_bp_desc_shiny_eye, min = -1f),
            BeautyParamSpec("remove_eye_streaks", R.string.id_photo_bp_remove_eye_streaks, R.string.id_photo_bp_desc_remove_eye_streaks, min = -1f),
        )
    ),
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_hair,
        specs = listOf(
            BeautyParamSpec("fill_hair_part", R.string.id_photo_bp_fill_hair_part, R.string.id_photo_bp_desc_fill_hair_part),
            BeautyParamSpec("calvaria_height", R.string.id_photo_bp_calvaria_height, R.string.id_photo_bp_desc_calvaria_height, min = -1f),
            BeautyParamSpec("hairline_height", R.string.id_photo_bp_hairline_height, R.string.id_photo_bp_desc_hairline_height, min = -1f),
            BeautyParamSpec("remove_white_hair", R.string.id_photo_bp_remove_white_hair, R.string.id_photo_bp_desc_remove_white_hair),
            BeautyParamSpec(
                "remove_burst_hair", R.string.id_photo_bp_remove_burst_hair, R.string.id_photo_bp_desc_remove_burst_hair,
                companions = mapOf("remove_burst_hair_back" to 1f, "remove_burst_hair_body" to 1f)
            ),
        )
    ),
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_teeth,
        specs = listOf(
            BeautyParamSpec(
                "teeth_white", R.string.id_photo_bp_teeth_white, R.string.id_photo_bp_desc_teeth_white,
                companions = mapOf("teeth_white_add_bright" to 1f, "teeth_white_des_yellow" to 1f)
            ),
            BeautyParamSpec("teeth_repair", R.string.id_photo_bp_teeth_repair, R.string.id_photo_bp_desc_teeth_repair, isSwitch = true),
        )
    ),
    BeautyParamGroup(
        titleRes = R.string.id_photo_beauty_group_body,
        specs = listOf(
            BeautyParamSpec("ai_body_thin", R.string.id_photo_bp_ai_body_thin, R.string.id_photo_bp_desc_ai_body_thin),
            BeautyParamSpec("body_thin", R.string.id_photo_bp_body_thin, R.string.id_photo_bp_desc_body_thin),
            BeautyParamSpec("head_small", R.string.id_photo_bp_head_small, R.string.id_photo_bp_desc_head_small, min = -1f),
            BeautyParamSpec("neck_length", R.string.id_photo_bp_neck_length, R.string.id_photo_bp_desc_neck_length, min = -1f),
            BeautyParamSpec("neck_thin", R.string.id_photo_bp_neck_thin, R.string.id_photo_bp_desc_neck_thin),
            BeautyParamSpec("arm_thin", R.string.id_photo_bp_arm_thin, R.string.id_photo_bp_desc_arm_thin),
            BeautyParamSpec("waist_thin", R.string.id_photo_bp_waist_thin, R.string.id_photo_bp_desc_waist_thin),
            BeautyParamSpec("leg_thin", R.string.id_photo_bp_leg_thin, R.string.id_photo_bp_desc_leg_thin),
            BeautyParamSpec("leg_long", R.string.id_photo_bp_leg_long, R.string.id_photo_bp_desc_leg_long),
            BeautyParamSpec("body_heighten", R.string.id_photo_bp_body_heighten, R.string.id_photo_bp_desc_body_heighten),
        )
    ),
)
