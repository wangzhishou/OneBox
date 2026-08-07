package com.wanbaohe.a2ui.catalog.builtin.layout

import androidx.compose.ui.Alignment

fun parseHorizontalAlignment(value: String?): Alignment.Horizontal = when (value?.lowercase()) {
    "start" -> Alignment.Start
    "center", "centre" -> Alignment.CenterHorizontally
    "end" -> Alignment.End
    else -> Alignment.Start
}

fun parseVerticalAlignment(value: String?): Alignment.Vertical = when (value?.lowercase()) {
    "top" -> Alignment.Top
    "center", "centre" -> Alignment.CenterVertically
    "bottom" -> Alignment.Bottom
    else -> Alignment.Top
}
