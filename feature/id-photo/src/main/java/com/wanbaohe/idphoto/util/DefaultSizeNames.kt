package com.wanbaohe.idphoto.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wanbaohe.idphoto.R
import com.wanbaohe.idphoto.domain.IdPhotoSize

/**
 * 预置证件照尺寸展示名/描述本地化。
 *
 * 预置尺寸的名称与描述在播种时以中文写入数据库（见 IdPhotoSizeRepository.initPresetsIfNeeded），
 * 且播种的中文 name 稳定不变，因此展示层统一按 name 映射到多语言字符串资源；
 * 用户自建/改名的尺寸不在映射表内，直接回退展示数据库中的值。
 */
@Composable
fun localizedSizeName(sizeName: String): String = when (sizeName) {
    "一寸" -> stringResource(R.string.id_photo_preset_size_one_inch)
    "小一寸" -> stringResource(R.string.id_photo_preset_size_small_one_inch)
    "大一寸" -> stringResource(R.string.id_photo_preset_size_large_one_inch)
    "二寸" -> stringResource(R.string.id_photo_preset_size_two_inch)
    "小二寸" -> stringResource(R.string.id_photo_preset_size_small_two_inch)
    "大二寸" -> stringResource(R.string.id_photo_preset_size_large_two_inch)
    "美国签证" -> stringResource(R.string.id_photo_preset_size_us_visa)
    "韩国签证" -> stringResource(R.string.id_photo_preset_size_korea_visa)
    "驾驶证" -> stringResource(R.string.id_photo_preset_size_drivers_license)
    "社保证" -> stringResource(R.string.id_photo_preset_size_social_security)
    "公务员" -> stringResource(R.string.id_photo_preset_size_civil_service)
    "普通话水平测试" -> stringResource(R.string.id_photo_preset_size_putonghua)
    "高考报名" -> stringResource(R.string.id_photo_preset_size_gaokao)
    "计算机等级考试" -> stringResource(R.string.id_photo_preset_size_ncre)
    "教师资格证" -> stringResource(R.string.id_photo_preset_size_teacher_cert)
    "护士执业资格" -> stringResource(R.string.id_photo_preset_size_nurse)
    "会计资格" -> stringResource(R.string.id_photo_preset_size_accounting)
    "税务师职业资格" -> stringResource(R.string.id_photo_preset_size_tax_advisor)
    "国家司法考试" -> stringResource(R.string.id_photo_preset_size_judicial)
    "卫生专业技术资格" -> stringResource(R.string.id_photo_preset_size_health_professional)
    "保险执业证" -> stringResource(R.string.id_photo_preset_size_insurance)
    "导游证" -> stringResource(R.string.id_photo_preset_size_tour_guide)
    "学信网" -> stringResource(R.string.id_photo_preset_size_chsi)
    "身份证" -> stringResource(R.string.id_photo_preset_size_id_card)
    else -> sizeName
}

/**
 * 预置尺寸描述本地化。
 *
 * 仅当数据库中的描述与播种值一致（即用户未改动过）时才映射到多语言资源，
 * 用户修改过的描述直接展示数据库中的值。
 */
@Composable
fun localizedSizeDescription(sizeName: String, description: String): String {
    val presetDescription = (IdPhotoSize.PRESETS + IdPhotoSize.DEFAULT)
        .firstOrNull { it.name == sizeName }
        ?.description
    if (presetDescription == null || description != presetDescription) return description

    return when (sizeName) {
        "一寸" -> stringResource(R.string.id_photo_preset_size_desc_one_inch)
        "小一寸" -> stringResource(R.string.id_photo_preset_size_desc_small_one_inch)
        "大一寸" -> stringResource(R.string.id_photo_preset_size_desc_large_one_inch)
        "二寸" -> stringResource(R.string.id_photo_preset_size_desc_two_inch)
        "小二寸" -> stringResource(R.string.id_photo_preset_size_desc_small_two_inch)
        "大二寸" -> stringResource(R.string.id_photo_preset_size_desc_large_two_inch)
        "美国签证" -> stringResource(R.string.id_photo_preset_size_desc_us_visa)
        "韩国签证" -> stringResource(R.string.id_photo_preset_size_desc_korea_visa)
        "驾驶证" -> stringResource(R.string.id_photo_preset_size_desc_drivers_license)
        "社保证" -> stringResource(R.string.id_photo_preset_size_desc_social_security)
        "公务员" -> stringResource(R.string.id_photo_preset_size_desc_civil_service)
        "普通话水平测试" -> stringResource(R.string.id_photo_preset_size_desc_putonghua)
        "高考报名" -> stringResource(R.string.id_photo_preset_size_desc_gaokao)
        "计算机等级考试" -> stringResource(R.string.id_photo_preset_size_desc_ncre)
        "教师资格证" -> stringResource(R.string.id_photo_preset_size_desc_teacher_cert)
        "护士执业资格" -> stringResource(R.string.id_photo_preset_size_desc_nurse)
        "会计资格" -> stringResource(R.string.id_photo_preset_size_desc_accounting)
        "税务师职业资格" -> stringResource(R.string.id_photo_preset_size_desc_tax_advisor)
        "国家司法考试" -> stringResource(R.string.id_photo_preset_size_desc_judicial)
        "卫生专业技术资格" -> stringResource(R.string.id_photo_preset_size_desc_health_professional)
        "保险执业证" -> stringResource(R.string.id_photo_preset_size_desc_insurance)
        "导游证" -> stringResource(R.string.id_photo_preset_size_desc_tour_guide)
        "学信网" -> stringResource(R.string.id_photo_preset_size_desc_chsi)
        "身份证" -> stringResource(R.string.id_photo_preset_size_desc_id_card)
        else -> description
    }
}

/**
 * 预置背景色展示名本地化。
 *
 * 预置背景色在代码中以中文 name 定义（见 IdPhotoBackground.PRESETS），
 * 展示层按 name 映射到已有的背景色字符串资源，非预置值原样展示。
 */
@Composable
fun localizedBackgroundName(backgroundName: String): String = when (backgroundName) {
    "原图" -> stringResource(R.string.id_photo_bg_original)
    "透明" -> stringResource(R.string.id_photo_bg_transparent)
    "白色" -> stringResource(R.string.id_photo_bg_white)
    "蓝色" -> stringResource(R.string.id_photo_bg_blue)
    "红色" -> stringResource(R.string.id_photo_bg_red)
    "渐变蓝" -> stringResource(R.string.id_photo_bg_gradient_blue)
    else -> backgroundName
}
