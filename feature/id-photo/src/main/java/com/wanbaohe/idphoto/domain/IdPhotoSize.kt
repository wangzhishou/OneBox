package com.wanbaohe.idphoto.domain

/**
 * 证件照尺寸规格
 * 定义各种常用的证件照尺寸
 */
data class IdPhotoSize(
    val id: Long = 0,
    val name: String,                      // 名称（如 "一寸"、"二寸"）
    val widthMm: Float,                    // 宽度（毫米）
    val heightMm: Float,                   // 高度（毫米）
    val widthPx: Int,                      // 宽度（像素，300dpi）
    val heightPx: Int,                     // 高度（像素，300dpi）
    val description: String = "",          // 描述（如 "身份证、护照"）
    val isPreset: Boolean = false,         // 是否预置
    val createdAt: Long = System.currentTimeMillis(),
) {
    /**
     * 获取宽高比
     */
    val aspectRatio: Float
        get() = widthPx.toFloat() / heightPx.toFloat()

    /**
     * 格式化尺寸显示
     */
    fun formatSize(): String = "${widthMm}×${heightMm}mm"

    /**
     * 格式化像素显示
     */
    fun formatPixels(): String = "${widthPx}×${heightPx}px"

    companion object {
        /**
         * 默认尺寸（一寸）
         */
        val DEFAULT = IdPhotoSize(
            name = "一寸",
            widthMm = 25f,
            heightMm = 35f,
            widthPx = 295,
            heightPx = 413,
            description = "常用于身份证、驾驶证"
        )

        /**
         * 预置的常用证件照尺寸
         */
        val PRESETS = listOf(
            // 美国签证
            IdPhotoSize(
                name = "美国签证",
                widthMm = 51f,
                heightMm = 51f,
                widthPx = 600,
                heightPx = 600,
                description = "美国签证专用",
                isPreset = true
            ),
            // 韩国签证
            IdPhotoSize(
                name = "韩国签证",
                widthMm = 35f,
                heightMm = 45f,
                widthPx = 413,
                heightPx = 531,
                description = "韩国签证专用",
                isPreset = true
            ),
            // 驾驶证
            IdPhotoSize(
                name = "驾驶证",
                widthMm = 22f,
                heightMm = 32f,
                widthPx = 260,
                heightPx = 378,
                description = "驾驶证专用",
                isPreset = true
            ),
            // 社保证
            IdPhotoSize(
                name = "社保证",
                widthMm = 26f,
                heightMm = 32f,
                widthPx = 358,
                heightPx = 441,
                description = "社保卡专用",
                isPreset = true
            ),
            // 公务员
            IdPhotoSize(
                name = "公务员",
                widthMm = 35f,
                heightMm = 45f,
                widthPx = 413,
                heightPx = 531,
                description = "公务员考试专用",
                isPreset = true
            ),
            // 普通话水平测试
            IdPhotoSize(
                name = "普通话水平测试",
                widthMm = 33f,
                heightMm = 48f,
                widthPx = 390,
                heightPx = 567,
                description = "普通话水平测试专用",
                isPreset = true
            ),
            // 高考报名
            IdPhotoSize(
                name = "高考报名",
                widthMm = 41f,
                heightMm = 54f,
                widthPx = 480,
                heightPx = 640,
                description = "高考报名专用",
                isPreset = true
            ),
            // 计算机等级考试
            IdPhotoSize(
                name = "计算机等级考试",
                widthMm = 12.2f,
                heightMm = 16.3f,
                widthPx = 144,
                heightPx = 192,
                description = "计算机等级考试专用",
                isPreset = true
            ),
            // 教师资格证
            IdPhotoSize(
                name = "教师资格证",
                widthMm = 15.2f,
                heightMm = 20.3f,
                widthPx = 180,
                heightPx = 240,
                description = "教师资格证报名专用",
                isPreset = true
            ),
            // 护士执业资格
            IdPhotoSize(
                name = "护士执业资格",
                widthMm = 10.2f,
                heightMm = 13.5f,
                widthPx = 120,
                heightPx = 160,
                description = "护士执业资格报名专用",
                isPreset = true
            ),
            // 会计资格
            IdPhotoSize(
                name = "会计资格",
                widthMm = 25f,
                heightMm = 35f,
                widthPx = 295,
                heightPx = 413,
                description = "初级会计资格考试专用",
                isPreset = true
            ),
            // 税务师职业资格
            IdPhotoSize(
                name = "税务师职业资格",
                widthMm = 25f,
                heightMm = 35f,
                widthPx = 295,
                heightPx = 413,
                description = "税务师职业资格考试专用",
                isPreset = true
            ),
            // 国家司法考试
            IdPhotoSize(
                name = "国家司法考试",
                widthMm = 35f,
                heightMm = 53f,
                widthPx = 413,
                heightPx = 626,
                description = "国家司法考试专用",
                isPreset = true
            ),
            // 卫生专业技术资格
            IdPhotoSize(
                name = "卫生专业技术资格",
                widthMm = 40f,
                heightMm = 54f,
                widthPx = 480,
                heightPx = 640,
                description = "卫生专业技术资格考试专用",
                isPreset = true
            ),
            // 保险执业证
            IdPhotoSize(
                name = "保险执业证",
                widthMm = 18f,
                heightMm = 31f,
                widthPx = 210,
                heightPx = 370,
                description = "保险执业证专用",
                isPreset = true
            ),
            // 导游证
            IdPhotoSize(
                name = "导游证",
                widthMm = 23f,
                heightMm = 33f,
                widthPx = 285,
                heightPx = 385,
                description = "导游证专用",
                isPreset = true
            ),
            // 学信网
            IdPhotoSize(
                name = "学信网",
                widthMm = 41f,
                heightMm = 54f,
                widthPx = 480,
                heightPx = 640,
                description = "学信网图像采集专用",
                isPreset = true
            ),
            // 大二寸
            IdPhotoSize(
                name = "大二寸",
                widthMm = 35f,
                heightMm = 53f,
                widthPx = 413,
                heightPx = 626,
                description = "常用于部分签证",
                isPreset = true
            ),
            // 身份证
            IdPhotoSize(
                name = "身份证",
                widthMm = 26f,
                heightMm = 32f,
                widthPx = 358,
                heightPx = 441,
                description = "第二代身份证专用",
                isPreset = true
            ),
            // 二寸
            IdPhotoSize(
                name = "二寸",
                widthMm = 35f,
                heightMm = 49f,
                widthPx = 413,
                heightPx = 579,
                description = "常用于护照、签证",
                isPreset = true
            ),
            // 小二寸
            IdPhotoSize(
                name = "小二寸",
                widthMm = 35f,
                heightMm = 45f,
                widthPx = 413,
                heightPx = 531,
                description = "常用于中国护照",
                isPreset = true
            ),
            // 大一寸
            IdPhotoSize(
                name = "大一寸",
                widthMm = 33f,
                heightMm = 48f,
                widthPx = 390,
                heightPx = 567,
                description = "常用于护照、港澳通行证",
                isPreset = true
            ),
            // 小一寸
            IdPhotoSize(
                name = "小一寸",
                widthMm = 22f,
                heightMm = 32f,
                widthPx = 260,
                heightPx = 378,
                description = "常用于简历、考试报名",
                isPreset = true
            ),
            // 一寸
            IdPhotoSize(
                name = "一寸",
                widthMm = 25f,
                heightMm = 35f,
                widthPx = 295,
                heightPx = 413,
                description = "常用于身份证、驾驶证",
                isPreset = true
            ),
        )
    }
}
