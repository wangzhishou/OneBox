package com.shifenmiao.model

/**
 * 统一的数据来源枚举，所有实体的 source 字段共用。
 * 数据库存储 Int 值，通过 SourceTypeConverter 自动转换。
 */
enum class Source(val value: Int) {
    /** 网络远程同步 */
    REMOTE(0),
    /** 用户本地创建（含 AI 创作 / 草稿落地） */
    LOCAL(1),
    /** 系统预置（如 AI 生成提示词） */
    SYSTEM(2),
    /** 创建页面临时预览，不进入正式资源列表 */
    PREVIEW(3);

    companion object {
        fun fromValue(value: Int): Source =
            entries.find { it.value == value } ?: REMOTE
    }
}
