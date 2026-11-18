/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.watermark.template.domain

/**
 * 内置模板的唯一 id 来源。
 *
 * 注册表元数据与图片水印侧的 document 提供者都必须对这个枚举做穷尽 when，
 * 新增模板时漏掉任何一侧都会在编译期报错，不需要运行时 id 对齐自检。
 */
enum class BuiltinTemplateId(val id: String) {
    COPYRIGHT_TILE("copyright_tile"),
    BRAND_LOGO("brand_logo"),
    SIMPLE_STAMP("simple_stamp"),
    PHOTO_TIME("photo_time"),
    OUTDOOR("outdoor"),
    WEATHER_CHECKIN("weather_checkin"),
    COORDINATES("coordinates"),
    HIDDEN_COPYRIGHT("hidden_copyright"),
    LEICA_CLASSIC("leica_classic"),
    PRIVACY_REDACTION("privacy_redaction"),
    ;

    companion object {
        fun fromId(value: String?): BuiltinTemplateId? = entries.firstOrNull { it.id == value }
    }
}
