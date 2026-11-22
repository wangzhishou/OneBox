/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppWidthMode {
    Narrow,
    Standard,
    Medium,
    Expanded,
}

enum class AppFontMode {
    Regular,
    Large,
    ExtraLarge,
}

data class AppUiProfile(
    val widthMode: AppWidthMode,
    val fontMode: AppFontMode,
) {
    companion object {
        val Default = AppUiProfile(
            widthMode = AppWidthMode.Standard,
            fontMode = AppFontMode.Regular,
        )
    }
}

@Composable
fun AppAdaptiveProvider(
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val windowWidth = with(density) {
        LocalWindowInfo.current.containerSize.width.toDp()
    }
    val widthMode = windowWidth.toWidthMode()
    val fontMode = density.fontScale.toFontMode()
    val profile = remember(widthMode, fontMode) {
        AppUiProfile(widthMode = widthMode, fontMode = fontMode)
    }
    val dimensions = remember(widthMode) {
        dimensionsFor(widthMode)
    }

    CompositionLocalProvider(
        LocalAppUiProfile provides profile,
        LocalAppDimens provides dimensions,
        content = content,
    )
}

private fun Dp.toWidthMode(): AppWidthMode = when {
    this <= 0.dp -> AppWidthMode.Standard
    this < 360.dp -> AppWidthMode.Narrow
    this < 600.dp -> AppWidthMode.Standard
    this < 840.dp -> AppWidthMode.Medium
    else -> AppWidthMode.Expanded
}

private fun Float.toFontMode(): AppFontMode = when {
    this >= 1.3f -> AppFontMode.ExtraLarge
    this >= 1.15f -> AppFontMode.Large
    else -> AppFontMode.Regular
}

private val narrowDimensions = object : Dimensions by normalDimensions {
    override val pageHorizontal: Dp
        get() = 12.dp
    override val contentGap: Dp
        get() = 12.dp
    override val cardContentPadding: Dp
        get() = 12.dp
}

private val mediumDimensions = object : Dimensions by normalDimensions {
    override val pageHorizontal: Dp
        get() = 24.dp
    override val contentGap: Dp
        get() = 20.dp
    override val cardContentPadding: Dp
        get() = 20.dp
}

private val expandedDimensions = object : Dimensions by mediumDimensions {
    override val contentGap: Dp
        get() = 24.dp
}

private fun dimensionsFor(widthMode: AppWidthMode): Dimensions = when (widthMode) {
    AppWidthMode.Narrow -> narrowDimensions
    AppWidthMode.Standard -> normalDimensions
    AppWidthMode.Medium -> mediumDimensions
    AppWidthMode.Expanded -> expandedDimensions
}
