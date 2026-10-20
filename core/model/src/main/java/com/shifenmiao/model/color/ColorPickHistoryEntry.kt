package com.shifenmiao.model.color

import kotlinx.serialization.Serializable

/**
 * 一条图片取色记录。
 *
 * [argb] 是颜色本身（ARGB int），也是同色去重的唯一键：
 * 同一颜色再次被取到只更新时间与排序，不重复占位。
 * [isFavorite] 标记用户在取色器里点过爱心，历史页可以只看收藏。
 */
@Serializable
data class ColorPickHistoryEntry(
    val argb: Int,
    val pickedAtMillis: Long,
    val isFavorite: Boolean = false
)
