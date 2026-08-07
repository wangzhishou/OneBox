package com.shifenmiao.feature.document.domain

import com.shifenmiao.core.constants.Constants.FILE_POINTS_CONSUME_NUM
import kotlin.math.ceil

fun computeRequiredPoints(fileSizeBytes: Long): Int {
    if (fileSizeBytes <= 0) return FILE_POINTS_CONSUME_NUM
    val mbUnits = ceil(fileSizeBytes.toDouble() / (1024.0 * 1024.0))
        .toLong()
        .coerceAtLeast(1L)
    val points = mbUnits * FILE_POINTS_CONSUME_NUM.toLong()
    return points.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}

