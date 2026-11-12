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
import com.t8rin.imagetoolbox.core.utils.LocaleUtils
import com.wanbaohe.idphoto.R

/**
 * 证件照预置尺寸目录（按语言环境）。
 *
 * 设计要点：
 * - 预置尺寸随语言环境不同：zh 走大陆/台湾规格，ja/ko/en/in/hi/fil/pt/de/es/ru/tr/ar 各自一套当地规格
 *   （另含少量国际通用规格，如 35×45mm 护照、2×2in 美国签证）；
 * - 名称/描述只存字符串资源 id，播种进库时按当前语言解析（见 IdPhotoSizeRepository），
 *   因此各语言库里就是当地语言的文案，UI 不再做任何中文名映射；
 * - 每个目录第一条即该语言的默认尺寸（进页面默认选中）；
 * - [REVISION] 变更（增删预置、改尺寸、改文案）会让各语言库在下一次进页面时整表重播。
 */
object IdPhotoPresetCatalog {

    /** 目录版本：预置集合/尺寸有变时 +1 */
    const val REVISION = 1

    /** 当前语言的预置目录（第一条为默认尺寸） */
    fun current(): List<IdPhotoPreset> = forLocale(LocaleUtils.getCurrentLocaleTag())

    /** 当前语言库应记录的目录 id（目录 + 版本），用于判断库里播的是哪一版 */
    fun currentCatalogId(): String = "${catalogKeyFor(LocaleUtils.getCurrentLocaleTag())}@$REVISION"

    fun forLocale(localeTag: String): List<IdPhotoPreset> = when (catalogKeyFor(localeTag)) {
        KEY_CN -> CN
        KEY_TW -> TW
        KEY_JP -> JP
        KEY_KR -> KR
        KEY_ID -> ID
        KEY_IN -> IN
        KEY_PH -> PH
        KEY_BR -> BR
        KEY_EU -> EU
        KEY_ES -> ES
        KEY_RU -> RU
        KEY_TR -> TR
        KEY_AR -> AR
        else -> INTL
    }

    private const val KEY_CN = "cn"
    private const val KEY_TW = "tw"
    private const val KEY_JP = "jp"
    private const val KEY_KR = "kr"
    private const val KEY_ID = "id"
    private const val KEY_IN = "in"
    private const val KEY_PH = "ph"
    private const val KEY_BR = "br"
    private const val KEY_EU = "eu"
    private const val KEY_ES = "es"
    private const val KEY_RU = "ru"
    private const val KEY_TR = "tr"
    private const val KEY_AR = "ar"
    private const val KEY_INTL = "intl"

    /**
     * 语言 tag → 目录。
     * zh 按地区分大陆/台湾（台湾规格与大陆不同）；
     * 印尼的传统语言码是 in，新码是 id，两者都归印尼；
     * 西语单独一套（西班牙身份证/护照是 2.6×3.2cm），葡语按地区分巴西/欧标；
     * de/fr/it 等未上架的欧洲语言走欧标 3.5×4.5。
     */
    private fun catalogKeyFor(localeTag: String): String {
        val tag = localeTag.lowercase().replace('_', '-')
        val language = tag.substringBefore('-')
        val region = tag.substringAfter('-', "")
        return when (language) {
            "zh" -> if (region == "tw" || region == "hk" || region == "mo") KEY_TW else KEY_CN
            "ja" -> KEY_JP
            "ko" -> KEY_KR
            "in", "id" -> KEY_ID
            "hi" -> KEY_IN
            "fil", "tl" -> KEY_PH
            "pt" -> if (region == "br") KEY_BR else KEY_EU
            "es" -> KEY_ES
            "de", "fr", "it", "nl", "pl", "ca" -> KEY_EU
            "ru" -> KEY_RU
            "tr" -> KEY_TR
            "ar" -> KEY_AR
            else -> KEY_INTL
        }
    }

    /**
     * 2×2in 美国护照/签证照（51×51mm，官方数字照 600×600px）：
     * 加拿大、韩国、印度、菲律宾、巴西、欧盟、西班牙、俄罗斯、土耳其、阿拉伯语目录共用同一条，
     * 避免每个目录各写一份字面量。
     */
    private val US_VISA_2X2 = p(
        "us_2x2in", 51f, 51f, 600, 600,
        R.string.id_photo_preset_size_us_visa, R.string.id_photo_preset_size_desc_us_visa
    )

    // ── 默认/国际通用（en，也是未覆盖语言的兜底） ────────────────────────────────

    private val INTL: List<IdPhotoPreset> = listOf(
        // 35×45mm：英国、申根、澳洲等大多数国家的护照/签证标准
        p("intl_passport_35x45", 35f, 45f, 413, 531, R.string.id_photo_preset_intl_passport, R.string.id_photo_preset_intl_passport_desc),
        // 2×2in：美国护照/签证/绿卡，名称与描述复用旧的「美国签证」条目
        US_VISA_2X2,
        p("intl_canada_50x70", 50f, 70f, 591, 827, R.string.id_photo_preset_intl_canada, R.string.id_photo_preset_intl_canada_desc),
        p("intl_china_visa_33x48", 33f, 48f, 390, 567, R.string.id_photo_preset_intl_china_visa, R.string.id_photo_preset_intl_china_visa_desc),
        p("intl_japan_visa_45x45", 45f, 45f, 531, 531, R.string.id_photo_preset_intl_japan_visa, R.string.id_photo_preset_intl_japan_visa_desc),
        p("intl_korea_visa_35x45", 35f, 45f, 413, 531, R.string.id_photo_preset_size_korea_visa, R.string.id_photo_preset_size_desc_korea_visa),
        p("intl_photo_30x40", 30f, 40f, 354, 472, R.string.id_photo_preset_intl_photo_3x4, R.string.id_photo_preset_intl_photo_3x4_desc),
    )

    // ── 中国大陆 ────────────────────────────────────────────────────────────

    private val CN: List<IdPhotoPreset> = listOf(
        p("cn_1inch", 25f, 35f, 295, 413, R.string.id_photo_preset_size_one_inch, R.string.id_photo_preset_size_desc_one_inch),
        p("cn_small_1inch", 22f, 32f, 260, 378, R.string.id_photo_preset_size_small_one_inch, R.string.id_photo_preset_size_desc_small_one_inch),
        p("cn_large_1inch", 33f, 48f, 390, 567, R.string.id_photo_preset_size_large_one_inch, R.string.id_photo_preset_size_desc_large_one_inch),
        p("cn_small_2inch", 35f, 45f, 413, 531, R.string.id_photo_preset_size_small_two_inch, R.string.id_photo_preset_size_desc_small_two_inch),
        p("cn_2inch", 35f, 49f, 413, 579, R.string.id_photo_preset_size_two_inch, R.string.id_photo_preset_size_desc_two_inch),
        p("cn_id_card", 26f, 32f, 358, 441, R.string.id_photo_preset_size_id_card, R.string.id_photo_preset_size_desc_id_card),
        p("cn_large_2inch", 35f, 53f, 413, 626, R.string.id_photo_preset_size_large_two_inch, R.string.id_photo_preset_size_desc_large_two_inch),
        p("cn_chsi", 41f, 54f, 480, 640, R.string.id_photo_preset_size_chsi, R.string.id_photo_preset_size_desc_chsi),
        p("cn_tour_guide", 23f, 33f, 285, 385, R.string.id_photo_preset_size_tour_guide, R.string.id_photo_preset_size_desc_tour_guide),
        p("cn_insurance", 18f, 31f, 210, 370, R.string.id_photo_preset_size_insurance, R.string.id_photo_preset_size_desc_insurance),
        p("cn_health", 40f, 54f, 480, 640, R.string.id_photo_preset_size_health_professional, R.string.id_photo_preset_size_desc_health_professional),
        p("cn_judicial", 35f, 53f, 413, 626, R.string.id_photo_preset_size_judicial, R.string.id_photo_preset_size_desc_judicial),
        p("cn_tax_advisor", 25f, 35f, 295, 413, R.string.id_photo_preset_size_tax_advisor, R.string.id_photo_preset_size_desc_tax_advisor),
        p("cn_accounting", 25f, 35f, 295, 413, R.string.id_photo_preset_size_accounting, R.string.id_photo_preset_size_desc_accounting),
        p("cn_nurse", 10.2f, 13.5f, 120, 160, R.string.id_photo_preset_size_nurse, R.string.id_photo_preset_size_desc_nurse),
        p("cn_teacher", 15.2f, 20.3f, 180, 240, R.string.id_photo_preset_size_teacher_cert, R.string.id_photo_preset_size_desc_teacher_cert),
        p("cn_ncre", 12.2f, 16.3f, 144, 192, R.string.id_photo_preset_size_ncre, R.string.id_photo_preset_size_desc_ncre),
        p("cn_gaokao", 41f, 54f, 480, 640, R.string.id_photo_preset_size_gaokao, R.string.id_photo_preset_size_desc_gaokao),
        p("cn_putonghua", 33f, 48f, 390, 567, R.string.id_photo_preset_size_putonghua, R.string.id_photo_preset_size_desc_putonghua),
        p("cn_civil_service", 35f, 45f, 413, 531, R.string.id_photo_preset_size_civil_service, R.string.id_photo_preset_size_desc_civil_service),
        p("cn_social_security", 26f, 32f, 358, 441, R.string.id_photo_preset_size_social_security, R.string.id_photo_preset_size_desc_social_security),
        p("cn_drivers_license", 22f, 32f, 260, 378, R.string.id_photo_preset_size_drivers_license, R.string.id_photo_preset_size_desc_drivers_license),
        p("cn_korea_visa", 35f, 45f, 413, 531, R.string.id_photo_preset_size_korea_visa, R.string.id_photo_preset_size_desc_korea_visa),
        US_VISA_2X2,
    )

    // ── 台湾 ────────────────────────────────────────────────────────────────

    private val TW: List<IdPhotoPreset> = listOf(
        // 身分證與護照同為直4.5×橫3.5cm
        p("tw_id_card", 35f, 45f, 413, 531, R.string.id_photo_preset_tw_id_card, R.string.id_photo_preset_tw_id_card_desc),
        p("tw_passport", 35f, 45f, 413, 531, R.string.id_photo_preset_tw_passport, R.string.id_photo_preset_tw_passport_desc),
        // 2吋大頭照(相館通稱)4.2×4.7cm
        p("tw_photo_2inch", 42f, 47f, 496, 555, R.string.id_photo_preset_tw_photo_2inch, R.string.id_photo_preset_tw_photo_2inch_desc),
        // 駕照用1吋 2.5×3.5cm(道路交通安全規則)
        p("tw_drivers_license", 25f, 35f, 295, 413, R.string.id_photo_preset_tw_drivers_license, R.string.id_photo_preset_tw_drivers_license_desc),
        // 香港特區護照/身分證 40×50mm(zh-Hant 用户同样适用，见 catalogKeyFor 的 hk/mo 归并)
        p("tw_hk_travel_doc", 40f, 50f, 472, 591, R.string.id_photo_preset_tw_hk_travel_doc, R.string.id_photo_preset_tw_hk_travel_doc_desc),
        US_VISA_2X2,
    )

    // ── 日本 ────────────────────────────────────────────────────────────────

    private val JP: List<IdPhotoPreset> = listOf(
        p("jp_resume_30x40", 30f, 40f, 354, 472, R.string.id_photo_preset_jp_resume_30x40, R.string.id_photo_preset_jp_resume_30x40_desc),
        p("jp_passport", 35f, 45f, 413, 531, R.string.id_photo_preset_jp_passport, R.string.id_photo_preset_jp_passport_desc),
        p("jp_mynumber", 35f, 45f, 413, 531, R.string.id_photo_preset_jp_mynumber, R.string.id_photo_preset_jp_mynumber_desc),
        p("jp_residence_card", 30f, 40f, 354, 472, R.string.id_photo_preset_jp_residence_card, R.string.id_photo_preset_jp_residence_card_desc),
        p("jp_drivers_license", 24f, 30f, 283, 354, R.string.id_photo_preset_jp_drivers_license, R.string.id_photo_preset_jp_drivers_license_desc),
        // 査証申請用はパスポートと違い 4.5×4.5cm
        p("jp_visa", 45f, 45f, 531, 531, R.string.id_photo_preset_intl_japan_visa, R.string.id_photo_preset_intl_japan_visa_desc),
    )

    // ── 韩国 ────────────────────────────────────────────────────────────────
    //
    // 韩国自 2015 年起护照/주민등록증/운전면허/사증 全部统一 3.5×4.5cm，
    // 相馆传统的 반명함판 3×4cm 仍用于履历/증명사진，因此只保留这两条。

    private val KR: List<IdPhotoPreset> = listOf(
        p("kr_id_30x40", 30f, 40f, 354, 472, R.string.id_photo_preset_kr_id_30x40, R.string.id_photo_preset_kr_id_30x40_desc),
        p("kr_id_35x45", 35f, 45f, 413, 531, R.string.id_photo_preset_kr_id_35x45, R.string.id_photo_preset_kr_id_35x45_desc),
        US_VISA_2X2,
    )

    // ── 印尼 ────────────────────────────────────────────────────────────────

    private val ID: List<IdPhotoPreset> = listOf(
        p("id_photo_2x3", 20f, 30f, 236, 354, R.string.id_photo_preset_id_2x3, R.string.id_photo_preset_id_2x3_desc),
        p("id_photo_3x4", 30f, 40f, 354, 472, R.string.id_photo_preset_id_3x4, R.string.id_photo_preset_id_3x4_desc),
        p("id_photo_4x6", 40f, 60f, 472, 709, R.string.id_photo_preset_id_4x6, R.string.id_photo_preset_id_4x6_desc),
    )

    // ── 印度 ────────────────────────────────────────────────────────────────

    private val IN: List<IdPhotoPreset> = listOf(
        // 印度护照用 3.5×4.5cm（印度驻慕尼黑领事馆材料）
        p("in_passport", 35f, 45f, 413, 531, R.string.id_photo_preset_in_passport, R.string.id_photo_preset_in_passport_desc),
        // 印度签证/美国签证同为 2×2in
        p("in_visa", 51f, 51f, 600, 600, R.string.id_photo_preset_in_visa, R.string.id_photo_preset_in_visa_desc),
        // PAN 卡与 stamp size 同为 2.5×3.5cm
        p("in_pan_card", 25f, 35f, 295, 413, R.string.id_photo_preset_in_pan_card, R.string.id_photo_preset_in_pan_card_desc),
    )

    // ── 菲律宾 ──────────────────────────────────────────────────────────────

    private val PH: List<IdPhotoPreset> = listOf(
        // NBI/政府表单通用 2×2in
        p("ph_id_2x2", 51f, 51f, 600, 600, R.string.id_photo_preset_ph_2x2, R.string.id_photo_preset_ph_2x2_desc),
        p("ph_id_1x1", 25f, 25f, 295, 295, R.string.id_photo_preset_ph_1x1, R.string.id_photo_preset_ph_1x1_desc),
        p("ph_passport_size", 35f, 45f, 413, 531, R.string.id_photo_preset_intl_passport, R.string.id_photo_preset_intl_passport_desc),
    )

    // ── 巴西 ────────────────────────────────────────────────────────────────

    private val BR: List<IdPhotoPreset> = listOf(
        p("br_photo_3x4", 30f, 40f, 354, 472, R.string.id_photo_preset_br_3x4, R.string.id_photo_preset_br_3x4_desc),
        p("br_passport", 50f, 70f, 591, 827, R.string.id_photo_preset_br_passport, R.string.id_photo_preset_br_passport_desc),
        US_VISA_2X2,
    )

    // ── 欧盟/申根（de、fr、it 等 35×45 体系） ─────────────────────────────────

    private val EU: List<IdPhotoPreset> = listOf(
        p("eu_biometric_35x45", 35f, 45f, 413, 531, R.string.id_photo_preset_eu_biometric, R.string.id_photo_preset_eu_biometric_desc),
        p("eu_photo_30x40", 30f, 40f, 354, 472, R.string.id_photo_preset_intl_photo_3x4, R.string.id_photo_preset_intl_photo_3x4_desc),
        US_VISA_2X2,
        p("eu_china_visa", 33f, 48f, 390, 567, R.string.id_photo_preset_intl_china_visa, R.string.id_photo_preset_intl_china_visa_desc),
    )

    // ── 西班牙（身份证/护照是 2.6×3.2cm，与欧盟他国不同） ──────────────────────

    private val ES: List<IdPhotoPreset> = listOf(
        p("es_dni", 26f, 32f, 307, 378, R.string.id_photo_preset_es_dni, R.string.id_photo_preset_es_dni_desc),
        // 申根签证仍按 3.5×4.5cm
        p("es_schengen_visa", 35f, 45f, 413, 531, R.string.id_photo_preset_intl_passport, R.string.id_photo_preset_intl_passport_desc),
        US_VISA_2X2,
        p("es_china_visa", 33f, 48f, 390, 567, R.string.id_photo_preset_intl_china_visa, R.string.id_photo_preset_intl_china_visa_desc),
    )

    // ── 俄罗斯 ──────────────────────────────────────────────────────────────

    private val RU: List<IdPhotoPreset> = listOf(
        p("ru_passport", 35f, 45f, 413, 531, R.string.id_photo_preset_ru_passport, R.string.id_photo_preset_ru_passport_desc),
        US_VISA_2X2,
        p("ru_china_visa", 33f, 48f, 390, 567, R.string.id_photo_preset_intl_china_visa, R.string.id_photo_preset_intl_china_visa_desc),
    )

    // ── 土耳其 ──────────────────────────────────────────────────────────────

    private val TR: List<IdPhotoPreset> = listOf(
        p("tr_biometric_50x60", 50f, 60f, 591, 709, R.string.id_photo_preset_tr_biometric, R.string.id_photo_preset_tr_biometric_desc),
        p("tr_schengen_visa", 35f, 45f, 413, 531, R.string.id_photo_preset_intl_passport, R.string.id_photo_preset_intl_passport_desc),
        US_VISA_2X2,
    )

    // ── 阿拉伯语地区 ────────────────────────────────────────────────────────

    private val AR: List<IdPhotoPreset> = listOf(
        p("ar_photo_4x6", 40f, 60f, 472, 709, R.string.id_photo_preset_ar_4x6, R.string.id_photo_preset_ar_4x6_desc),
        p("ar_passport_35x45", 35f, 45f, 413, 531, R.string.id_photo_preset_intl_passport, R.string.id_photo_preset_intl_passport_desc),
        US_VISA_2X2,
    )

    private fun p(
        key: String,
        widthMm: Float,
        heightMm: Float,
        widthPx: Int,
        heightPx: Int,
        @StringRes nameRes: Int,
        @StringRes descRes: Int,
    ) = IdPhotoPreset(
        key = key,
        widthMm = widthMm,
        heightMm = heightMm,
        widthPx = widthPx,
        heightPx = heightPx,
        nameRes = nameRes,
        descRes = descRes,
    )
}
