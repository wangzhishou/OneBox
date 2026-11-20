/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.domain.model

import kotlin.math.roundToInt

/**
 * 输出画布版式。
 *
 * [BottomBand] 的带高按图片宽度成比例（与旧相机水印渲染器一致），
 * 因此带内元素不能归一化到「原图 + 白带」的总画布高度——总高随原图宽高比变化，
 * 同一个 centerY 在横图与竖图里会落到带内完全不同的位置。
 * 带内元素一律使用 [WatermarkAnchor.BAND]，归一化到带自己的 rect。
 */
sealed interface WatermarkFrame {
    /** 画布 = 原图，所有元素叠在图上（历史默认行为）。 */
    data object InPlace : WatermarkFrame

    data class BottomBand(
        val heightRatio: Float = DEFAULT_HEIGHT_RATIO,
        val color: Int = -1,
    ) : WatermarkFrame {
        /**
         * 按「源图宽度」算带高并加像素护栏。预览必须先用它得到源图尺度下的带高，
         * 再乘 fit 缩放，不能拿预览宽度直接算——否则窄图会被护栏拉成完全不同的比例。
         */
        fun bandHeightFor(width: Int): Int {
            val minHeight = minOf(MIN_BAND_HEIGHT, width / 10).coerceAtLeast(1)
            return (width * heightRatio).roundToInt().coerceIn(minHeight, MAX_BAND_HEIGHT)
        }

        companion object {
            val DEFAULT_HEIGHT_RATIO: Float = 120f / 1080f
            const val MIN_BAND_HEIGHT = 50
            const val MAX_BAND_HEIGHT = 1200
        }
    }
}

/** 元素坐标所归属的矩形空间。预览与导出必须用同一套换算。 */
enum class WatermarkAnchor { PHOTO, BAND }
