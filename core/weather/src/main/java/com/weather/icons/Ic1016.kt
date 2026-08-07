package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1016: ImageVector
    get() {
        val current = _ic1016
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1016",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 10.75 a.75 .75 0 0 1 1.5 0 v3.75 h13 v-3.75 a.75 .75 0 0 1 1.5 0 v4.5 a.75 .75 0 0 1 -.75 .75 H.75 a.75 .75 0 0 1 -.75 -.75 v-4.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 10.75
                moveTo(x = 0.0f, y = 10.75f)
                // a 0.75 0.75 0 0 1 1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                // v 3.75
                verticalLineToRelative(dy = 3.75f)
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v -3.75
                verticalLineToRelative(dy = -3.75f)
                // a 0.75 0.75 0 0 1 1.5 0
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.5f,
                    dy1 = 0.0f,
                )
                // v 4.5
                verticalLineToRelative(dy = 4.5f)
                // a 0.75 0.75 0 0 1 -0.75 0.75
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = 0.75f,
                )
                // H 0.75
                horizontalLineTo(x = 0.75f)
                // a 0.75 0.75 0 0 1 -0.75 -0.75
                arcToRelative(
                    a = 0.75f,
                    b = 0.75f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.75f,
                    dy1 = -0.75f,
                )
                // v -4.5z
                verticalLineToRelative(dy = -4.5f)
                close()
            }
            // M8.406 .406 a.406 .406 0 1 0 -.812 0 v1.05 L7.069 .932 a.406 .406 0 1 0 -.575 .575 l1.1 1.1 v1.457 l.001 .033 a2.44 2.44 0 0 0 -1.474 .851 .415 .415 0 0 0 -.029 -.018 l-1.261 -.728 L4.428 2.7 a.406 .406 0 1 0 -.785 .21 l.193 .718 -.91 -.526 a.406 .406 0 0 0 -.406 .704 l.91 .525 -.718 .192 a.406 .406 0 0 0 .21 .785 l1.502 -.402 1.262 .728 .03 .015 a2.432 2.432 0 0 0 0 1.704 .415 .415 0 0 0 -.03 .015 l-1.262 .728 -1.502 -.402 a.406 .406 0 1 0 -.21 .785 l.717 .192 -.91 .525 a.406 .406 0 0 0 .407 .704 l.91 -.526 -.193 .718 a.406 .406 0 1 0 .785 .21 L4.831 8.8 l1.261 -.728 a.416 .416 0 0 0 .029 -.018 2.44 2.44 0 0 0 1.474 .851 .411 .411 0 0 0 -.001 .034 v1.456 l-1.1 1.1 a.406 .406 0 1 0 .575 .575 l.525 -.526 v1.05 a.406 .406 0 0 0 .812 0 v-1.05 l.526 .526 a.406 .406 0 1 0 .574 -.575 l-1.1 -1.1 V8.937 l-.001 -.033 a2.435 2.435 0 0 0 1.474 -.852 l.029 .019 1.261 .728 .403 1.502 a.406 .406 0 1 0 .785 -.21 l-.192 -.718 .91 .526 a.406 .406 0 1 0 .405 -.704 l-.91 -.525 .718 -.192 a.406 .406 0 0 0 -.21 -.785 l-1.502 .402 -1.262 -.728 a.422 .422 0 0 0 -.03 -.016 2.433 2.433 0 0 0 0 -1.702 l.03 -.016 1.262 -.728 1.502 .402 a.406 .406 0 0 0 .21 -.785 l-.717 -.192 .91 -.525 a.406 .406 0 0 0 -.407 -.704 l-.91 .526 .193 -.718 a.406 .406 0 1 0 -.785 -.21 L11.17 4.2 l-1.262 .728 a.414 .414 0 0 0 -.029 .019 2.435 2.435 0 0 0 -1.474 -.852 l.001 -.034 V2.607 l1.1 -1.1 a.406 .406 0 0 0 -.574 -.575 l-.526 .526 V.407Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.406 0.406
                moveTo(x = 8.406f, y = 0.406f)
                // a 0.406 0.406 0 1 0 -0.812 0
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.812f,
                    dy1 = 0.0f,
                )
                // v 1.05
                verticalLineToRelative(dy = 1.05f)
                // L 7.069 0.932
                lineTo(x = 7.069f, y = 0.932f)
                // a 0.406 0.406 0 1 0 -0.575 0.575
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.575f,
                    dy1 = 0.575f,
                )
                // l 1.1 1.1
                lineToRelative(dx = 1.1f, dy = 1.1f)
                // v 1.457
                verticalLineToRelative(dy = 1.457f)
                // l 0.001 0.033
                lineToRelative(dx = 0.001f, dy = 0.033f)
                // a 2.44 2.44 0 0 0 -1.474 0.851
                arcToRelative(
                    a = 2.44f,
                    b = 2.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.474f,
                    dy1 = 0.851f,
                )
                // a 0.415 0.415 0 0 0 -0.029 -0.018
                arcToRelative(
                    a = 0.415f,
                    b = 0.415f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.029f,
                    dy1 = -0.018f,
                )
                // l -1.261 -0.728
                lineToRelative(dx = -1.261f, dy = -0.728f)
                // L 4.428 2.7
                lineTo(x = 4.428f, y = 2.7f)
                // a 0.406 0.406 0 1 0 -0.785 0.21
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.785f,
                    dy1 = 0.21f,
                )
                // l 0.193 0.718
                lineToRelative(dx = 0.193f, dy = 0.718f)
                // l -0.91 -0.526
                lineToRelative(dx = -0.91f, dy = -0.526f)
                // a 0.406 0.406 0 0 0 -0.406 0.704
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.406f,
                    dy1 = 0.704f,
                )
                // l 0.91 0.525
                lineToRelative(dx = 0.91f, dy = 0.525f)
                // l -0.718 0.192
                lineToRelative(dx = -0.718f, dy = 0.192f)
                // a 0.406 0.406 0 0 0 0.21 0.785
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.21f,
                    dy1 = 0.785f,
                )
                // l 1.502 -0.402
                lineToRelative(dx = 1.502f, dy = -0.402f)
                // l 1.262 0.728
                lineToRelative(dx = 1.262f, dy = 0.728f)
                // l 0.03 0.015
                lineToRelative(dx = 0.03f, dy = 0.015f)
                // a 2.432 2.432 0 0 0 0 1.704
                arcToRelative(
                    a = 2.432f,
                    b = 2.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.704f,
                )
                // a 0.415 0.415 0 0 0 -0.03 0.015
                arcToRelative(
                    a = 0.415f,
                    b = 0.415f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.03f,
                    dy1 = 0.015f,
                )
                // l -1.262 0.728
                lineToRelative(dx = -1.262f, dy = 0.728f)
                // l -1.502 -0.402
                lineToRelative(dx = -1.502f, dy = -0.402f)
                // a 0.406 0.406 0 1 0 -0.21 0.785
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.21f,
                    dy1 = 0.785f,
                )
                // l 0.717 0.192
                lineToRelative(dx = 0.717f, dy = 0.192f)
                // l -0.91 0.525
                lineToRelative(dx = -0.91f, dy = 0.525f)
                // a 0.406 0.406 0 0 0 0.407 0.704
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.407f,
                    dy1 = 0.704f,
                )
                // l 0.91 -0.526
                lineToRelative(dx = 0.91f, dy = -0.526f)
                // l -0.193 0.718
                lineToRelative(dx = -0.193f, dy = 0.718f)
                // a 0.406 0.406 0 1 0 0.785 0.21
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.785f,
                    dy1 = 0.21f,
                )
                // L 4.831 8.8
                lineTo(x = 4.831f, y = 8.8f)
                // l 1.261 -0.728
                lineToRelative(dx = 1.261f, dy = -0.728f)
                // a 0.416 0.416 0 0 0 0.029 -0.018
                arcToRelative(
                    a = 0.416f,
                    b = 0.416f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.029f,
                    dy1 = -0.018f,
                )
                // a 2.44 2.44 0 0 0 1.474 0.851
                arcToRelative(
                    a = 2.44f,
                    b = 2.44f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.474f,
                    dy1 = 0.851f,
                )
                // a 0.411 0.411 0 0 0 -0.001 0.034
                arcToRelative(
                    a = 0.411f,
                    b = 0.411f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.001f,
                    dy1 = 0.034f,
                )
                // v 1.456
                verticalLineToRelative(dy = 1.456f)
                // l -1.1 1.1
                lineToRelative(dx = -1.1f, dy = 1.1f)
                // a 0.406 0.406 0 1 0 0.575 0.575
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.575f,
                    dy1 = 0.575f,
                )
                // l 0.525 -0.526
                lineToRelative(dx = 0.525f, dy = -0.526f)
                // v 1.05
                verticalLineToRelative(dy = 1.05f)
                // a 0.406 0.406 0 0 0 0.812 0
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.812f,
                    dy1 = 0.0f,
                )
                // v -1.05
                verticalLineToRelative(dy = -1.05f)
                // l 0.526 0.526
                lineToRelative(dx = 0.526f, dy = 0.526f)
                // a 0.406 0.406 0 1 0 0.574 -0.575
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.574f,
                    dy1 = -0.575f,
                )
                // l -1.1 -1.1
                lineToRelative(dx = -1.1f, dy = -1.1f)
                // V 8.937
                verticalLineTo(y = 8.937f)
                // l -0.001 -0.033
                lineToRelative(dx = -0.001f, dy = -0.033f)
                // a 2.435 2.435 0 0 0 1.474 -0.852
                arcToRelative(
                    a = 2.435f,
                    b = 2.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.474f,
                    dy1 = -0.852f,
                )
                // l 0.029 0.019
                lineToRelative(dx = 0.029f, dy = 0.019f)
                // l 1.261 0.728
                lineToRelative(dx = 1.261f, dy = 0.728f)
                // l 0.403 1.502
                lineToRelative(dx = 0.403f, dy = 1.502f)
                // a 0.406 0.406 0 1 0 0.785 -0.21
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.785f,
                    dy1 = -0.21f,
                )
                // l -0.192 -0.718
                lineToRelative(dx = -0.192f, dy = -0.718f)
                // l 0.91 0.526
                lineToRelative(dx = 0.91f, dy = 0.526f)
                // a 0.406 0.406 0 1 0 0.405 -0.704
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.405f,
                    dy1 = -0.704f,
                )
                // l -0.91 -0.525
                lineToRelative(dx = -0.91f, dy = -0.525f)
                // l 0.718 -0.192
                lineToRelative(dx = 0.718f, dy = -0.192f)
                // a 0.406 0.406 0 0 0 -0.21 -0.785
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.21f,
                    dy1 = -0.785f,
                )
                // l -1.502 0.402
                lineToRelative(dx = -1.502f, dy = 0.402f)
                // l -1.262 -0.728
                lineToRelative(dx = -1.262f, dy = -0.728f)
                // a 0.422 0.422 0 0 0 -0.03 -0.016
                arcToRelative(
                    a = 0.422f,
                    b = 0.422f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.03f,
                    dy1 = -0.016f,
                )
                // a 2.433 2.433 0 0 0 0 -1.702
                arcToRelative(
                    a = 2.433f,
                    b = 2.433f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.702f,
                )
                // l 0.03 -0.016
                lineToRelative(dx = 0.03f, dy = -0.016f)
                // l 1.262 -0.728
                lineToRelative(dx = 1.262f, dy = -0.728f)
                // l 1.502 0.402
                lineToRelative(dx = 1.502f, dy = 0.402f)
                // a 0.406 0.406 0 0 0 0.21 -0.785
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.21f,
                    dy1 = -0.785f,
                )
                // l -0.717 -0.192
                lineToRelative(dx = -0.717f, dy = -0.192f)
                // l 0.91 -0.525
                lineToRelative(dx = 0.91f, dy = -0.525f)
                // a 0.406 0.406 0 0 0 -0.407 -0.704
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.407f,
                    dy1 = -0.704f,
                )
                // l -0.91 0.526
                lineToRelative(dx = -0.91f, dy = 0.526f)
                // l 0.193 -0.718
                lineToRelative(dx = 0.193f, dy = -0.718f)
                // a 0.406 0.406 0 1 0 -0.785 -0.21
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.785f,
                    dy1 = -0.21f,
                )
                // L 11.17 4.2
                lineTo(x = 11.17f, y = 4.2f)
                // l -1.262 0.728
                lineToRelative(dx = -1.262f, dy = 0.728f)
                // a 0.414 0.414 0 0 0 -0.029 0.019
                arcToRelative(
                    a = 0.414f,
                    b = 0.414f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.029f,
                    dy1 = 0.019f,
                )
                // a 2.435 2.435 0 0 0 -1.474 -0.852
                arcToRelative(
                    a = 2.435f,
                    b = 2.435f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.474f,
                    dy1 = -0.852f,
                )
                // l 0.001 -0.034
                lineToRelative(dx = 0.001f, dy = -0.034f)
                // V 2.607
                verticalLineTo(y = 2.607f)
                // l 1.1 -1.1
                lineToRelative(dx = 1.1f, dy = -1.1f)
                // a 0.406 0.406 0 0 0 -0.574 -0.575
                arcToRelative(
                    a = 0.406f,
                    b = 0.406f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.574f,
                    dy1 = -0.575f,
                )
                // l -0.526 0.526
                lineToRelative(dx = -0.526f, dy = 0.526f)
                // V 0.407z
                verticalLineTo(y = 0.407f)
                close()
            }
        }.build().also { _ic1016 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1016: ImageVector? = null
