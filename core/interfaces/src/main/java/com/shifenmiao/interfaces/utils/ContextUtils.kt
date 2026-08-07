package com.shifenmiao.interfaces.utils

import android.content.Context
import androidx.annotation.DimenRes


fun Context.dp2px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

fun Context.px(
    @DimenRes dimen: Int,
): Int = resources.getDimension(dimen).toInt()

fun Context.dp(
    @DimenRes dimen: Int,
): Float = resources.getDimensionPixelSize(dimen) / resources.displayMetrics.density