package com.shifenmiao.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface Dimensions {
    val containerAlpha: Float
    val navigationHeight: Dp
    val cardIconSize: Dp

    /**
     * 卡片的最小高度
     */
    val cardHeight: Dp

    // Corner Radius
    val cornerRadiusSmall: Dp
    val cornerRadiusLarge: Dp
    val cornerRadiusNormal: Dp
    val defaultCornerRadius: Dp

    // Icon Sizes
    val iconSmallSize: Dp
    val iconSize: Dp

    // Spacing
    val spaceLarge: Dp
    val spaceSmall: Dp
    val spaceExtraSmall: Dp
    val spaceNormal: Dp

    // Padding
    val paddingTooSmall: Dp
    val paddingExtraSmall: Dp
    val paddingSmall: Dp
    val paddingNormal: Dp
    val paddingLarge: Dp
    val paddingExtraLarge: Dp

    // Button Dimensions
    val normalButtonHeight: Dp
    val minButtonWidth: Dp

    // Elevation
    val defaultElevation: Dp

    // Feature: File Transfer
    val fileTransferQrCodeSize: Dp
}

val normalDimensions: Dimensions = object : Dimensions {
    override val containerAlpha: Float
        get() = 0.65f
    override val navigationHeight: Dp
        get() = 72.dp
    override val cardIconSize: Dp
        get() = 32.dp
    override val cardHeight: Dp
        get() = 130.dp

    // Corner Radius
    override val cornerRadiusSmall: Dp
        get() = 12.dp
    override val cornerRadiusLarge: Dp
        get() = 12.dp
    override val cornerRadiusNormal: Dp
        get() = 12.dp
    override val defaultCornerRadius: Dp
        get() = 12.dp

    // Icon Sizes
    override val iconSmallSize: Dp
        get() = 16.dp
    override val iconSize: Dp
        get() = 24.dp

    // Spacing
    override val spaceLarge: Dp
        get() = 16.dp
    override val spaceSmall: Dp
        get() = 7.dp
    override val spaceExtraSmall: Dp
        get() = 4.dp
    override val spaceNormal: Dp
        get() = 11.dp

    // Padding
    override val paddingTooSmall: Dp
        get() = 2.dp
    override val paddingExtraSmall: Dp
        get() = 4.dp
    override val paddingSmall: Dp
        get() = 8.dp
    override val paddingNormal: Dp
        get() = 16.dp
    override val paddingLarge: Dp
        get() = 24.dp
    override val paddingExtraLarge: Dp
        get() = 32.dp

    // Button Dimensions
    override val normalButtonHeight: Dp
        get() = 56.dp
    override val minButtonWidth: Dp
        get() = 120.dp

    // Elevation
    override val defaultElevation: Dp
        get() = 2.dp

    // Feature: File Transfer
    override val fileTransferQrCodeSize: Dp
        get() = 200.dp
}