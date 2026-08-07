package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic508Fill: ImageVector
    get() {
        val current = _ic508Fill
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic508Fill",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // m15.914 6.185 -1.571 1.268 c-.141 .115 -.343 .006 -.343 -.184 V7 H1 a1 1 0 1 1 0 -2 h13 v-.269 c0 -.19 .202 -.299 .343 -.184 l1.571 1.268 a.242 .242 0 0 1 0 .37Z m0 4 -1.571 1.268 c-.141 .115 -.343 .006 -.343 -.184 V11 H1 a1 1 0 1 1 0 -2 h13 v-.269 c0 -.19 .202 -.299 .343 -.184 l1.571 1.268 a.242 .242 0 0 1 0 .37Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 15.914 6.185
                moveTo(x = 15.914f, y = 6.185f)
                // l -1.571 1.268
                lineToRelative(dx = -1.571f, dy = 1.268f)
                // c -0.141 0.115 -0.343 0.006 -0.343 -0.184
                curveToRelative(
                    dx1 = -0.141f,
                    dy1 = 0.115f,
                    dx2 = -0.343f,
                    dy2 = 0.006f,
                    dx3 = -0.343f,
                    dy3 = -0.184f,
                )
                // V 7
                verticalLineTo(y = 7.0f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v -0.269
                verticalLineToRelative(dy = -0.269f)
                // c 0 -0.19 0.202 -0.299 0.343 -0.184
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.19f,
                    dx2 = 0.202f,
                    dy2 = -0.299f,
                    dx3 = 0.343f,
                    dy3 = -0.184f,
                )
                // l 1.571 1.268
                lineToRelative(dx = 1.571f, dy = 1.268f)
                // a 0.242 0.242 0 0 1 0 0.37z
                arcToRelative(
                    a = 0.242f,
                    b = 0.242f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.37f,
                )
                close()
                // m 0 4
                moveToRelative(dx = 0.0f, dy = 4.0f)
                // l -1.571 1.268
                lineToRelative(dx = -1.571f, dy = 1.268f)
                // c -0.141 0.115 -0.343 0.006 -0.343 -0.184
                curveToRelative(
                    dx1 = -0.141f,
                    dy1 = 0.115f,
                    dx2 = -0.343f,
                    dy2 = 0.006f,
                    dx3 = -0.343f,
                    dy3 = -0.184f,
                )
                // V 11
                verticalLineTo(y = 11.0f)
                // H 1
                horizontalLineTo(x = 1.0f)
                // a 1 1 0 1 1 0 -2
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.0f,
                )
                // h 13
                horizontalLineToRelative(dx = 13.0f)
                // v -0.269
                verticalLineToRelative(dy = -0.269f)
                // c 0 -0.19 0.202 -0.299 0.343 -0.184
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.19f,
                    dx2 = 0.202f,
                    dy2 = -0.299f,
                    dx3 = 0.343f,
                    dy3 = -0.184f,
                )
                // l 1.571 1.268
                lineToRelative(dx = 1.571f, dy = 1.268f)
                // a 0.242 0.242 0 0 1 0 0.37z
                arcToRelative(
                    a = 0.242f,
                    b = 0.242f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.37f,
                )
                close()
            }
            // M11.978 3.146 c-.464 .32 -1.128 .234 -1.516 -.156 -.35 -.351 -.662 -.605 -.938 -.76 -.441 -.265 -.961 -.397 -1.56 -.397 -.584 0 -1.08 .167 -1.49 .5 a1.614 1.614 0 0 0 -.591 1.292 c0 .514 .189 .965 .567 1.354 L6.47 5 H4.092 a3.518 3.518 0 0 1 -.29 -1.375 c0 -1.014 .395 -1.868 1.183 -2.563 C5.788 .355 6.821 0 8.082 0 c.977 0 1.828 .222 2.553 .667 .515 .305 1.021 .719 1.52 1.24 .36 .377 .263 .936 -.177 1.239Z M5.847 7 h3.366 c1.075 .698 1.912 1.365 2.512 2 h-3.03 c-.334 -.25 -.712 -.513 -1.133 -.792 A39.401 39.401 0 0 1 5.847 7Z m4.764 4 c.143 .31 .214 .629 .214 .958 0 .611 -.292 1.146 -.875 1.604 -.584 .459 -1.285 .688 -2.105 .688 -1.06 0 -2.033 -.58 -2.92 -1.738 a1.175 1.175 0 0 0 -1.43 -.328 c-.464 .236 -.643 .754 -.359 1.154 C4.396 15.113 6.044 16 8.082 16 c1.308 0 2.451 -.41 3.428 -1.23 .993 -.832 1.49 -1.812 1.49 -2.937 0 -.282 -.034 -.56 -.102 -.833 h-2.287Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.978 3.146
                moveTo(x = 11.978f, y = 3.146f)
                // c -0.464 0.32 -1.128 0.234 -1.516 -0.156
                curveToRelative(
                    dx1 = -0.464f,
                    dy1 = 0.32f,
                    dx2 = -1.128f,
                    dy2 = 0.234f,
                    dx3 = -1.516f,
                    dy3 = -0.156f,
                )
                // c -0.35 -0.351 -0.662 -0.605 -0.938 -0.76
                curveToRelative(
                    dx1 = -0.35f,
                    dy1 = -0.351f,
                    dx2 = -0.662f,
                    dy2 = -0.605f,
                    dx3 = -0.938f,
                    dy3 = -0.76f,
                )
                // c -0.441 -0.265 -0.961 -0.397 -1.56 -0.397
                curveToRelative(
                    dx1 = -0.441f,
                    dy1 = -0.265f,
                    dx2 = -0.961f,
                    dy2 = -0.397f,
                    dx3 = -1.56f,
                    dy3 = -0.397f,
                )
                // c -0.584 0 -1.08 0.167 -1.49 0.5
                curveToRelative(
                    dx1 = -0.584f,
                    dy1 = 0.0f,
                    dx2 = -1.08f,
                    dy2 = 0.167f,
                    dx3 = -1.49f,
                    dy3 = 0.5f,
                )
                // a 1.614 1.614 0 0 0 -0.591 1.292
                arcToRelative(
                    a = 1.614f,
                    b = 1.614f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.591f,
                    dy1 = 1.292f,
                )
                // c 0 0.514 0.189 0.965 0.567 1.354
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.514f,
                    dx2 = 0.189f,
                    dy2 = 0.965f,
                    dx3 = 0.567f,
                    dy3 = 1.354f,
                )
                // L 6.47 5
                lineTo(x = 6.47f, y = 5.0f)
                // H 4.092
                horizontalLineTo(x = 4.092f)
                // a 3.518 3.518 0 0 1 -0.29 -1.375
                arcToRelative(
                    a = 3.518f,
                    b = 3.518f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.29f,
                    dy1 = -1.375f,
                )
                // c 0 -1.014 0.395 -1.868 1.183 -2.563
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -1.014f,
                    dx2 = 0.395f,
                    dy2 = -1.868f,
                    dx3 = 1.183f,
                    dy3 = -2.563f,
                )
                // C 5.788 0.355 6.821 0 8.082 0
                curveTo(
                    x1 = 5.788f,
                    y1 = 0.355f,
                    x2 = 6.821f,
                    y2 = 0.0f,
                    x3 = 8.082f,
                    y3 = 0.0f,
                )
                // c 0.977 0 1.828 0.222 2.553 0.667
                curveToRelative(
                    dx1 = 0.977f,
                    dy1 = 0.0f,
                    dx2 = 1.828f,
                    dy2 = 0.222f,
                    dx3 = 2.553f,
                    dy3 = 0.667f,
                )
                // c 0.515 0.305 1.021 0.719 1.52 1.24
                curveToRelative(
                    dx1 = 0.515f,
                    dy1 = 0.305f,
                    dx2 = 1.021f,
                    dy2 = 0.719f,
                    dx3 = 1.52f,
                    dy3 = 1.24f,
                )
                // c 0.36 0.377 0.263 0.936 -0.177 1.239z
                curveToRelative(
                    dx1 = 0.36f,
                    dy1 = 0.377f,
                    dx2 = 0.263f,
                    dy2 = 0.936f,
                    dx3 = -0.177f,
                    dy3 = 1.239f,
                )
                close()
                // M 5.847 7
                moveTo(x = 5.847f, y = 7.0f)
                // h 3.366
                horizontalLineToRelative(dx = 3.366f)
                // c 1.075 0.698 1.912 1.365 2.512 2
                curveToRelative(
                    dx1 = 1.075f,
                    dy1 = 0.698f,
                    dx2 = 1.912f,
                    dy2 = 1.365f,
                    dx3 = 2.512f,
                    dy3 = 2.0f,
                )
                // h -3.03
                horizontalLineToRelative(dx = -3.03f)
                // c -0.334 -0.25 -0.712 -0.513 -1.133 -0.792
                curveToRelative(
                    dx1 = -0.334f,
                    dy1 = -0.25f,
                    dx2 = -0.712f,
                    dy2 = -0.513f,
                    dx3 = -1.133f,
                    dy3 = -0.792f,
                )
                // A 39.401 39.401 0 0 1 5.847 7z
                arcTo(
                    horizontalEllipseRadius = 39.401f,
                    verticalEllipseRadius = 39.401f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 5.847f,
                    y1 = 7.0f,
                )
                close()
                // m 4.764 4
                moveToRelative(dx = 4.764f, dy = 4.0f)
                // c 0.143 0.31 0.214 0.629 0.214 0.958
                curveToRelative(
                    dx1 = 0.143f,
                    dy1 = 0.31f,
                    dx2 = 0.214f,
                    dy2 = 0.629f,
                    dx3 = 0.214f,
                    dy3 = 0.958f,
                )
                // c 0 0.611 -0.292 1.146 -0.875 1.604
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.611f,
                    dx2 = -0.292f,
                    dy2 = 1.146f,
                    dx3 = -0.875f,
                    dy3 = 1.604f,
                )
                // c -0.584 0.459 -1.285 0.688 -2.105 0.688
                curveToRelative(
                    dx1 = -0.584f,
                    dy1 = 0.459f,
                    dx2 = -1.285f,
                    dy2 = 0.688f,
                    dx3 = -2.105f,
                    dy3 = 0.688f,
                )
                // c -1.06 0 -2.033 -0.58 -2.92 -1.738
                curveToRelative(
                    dx1 = -1.06f,
                    dy1 = 0.0f,
                    dx2 = -2.033f,
                    dy2 = -0.58f,
                    dx3 = -2.92f,
                    dy3 = -1.738f,
                )
                // a 1.175 1.175 0 0 0 -1.43 -0.328
                arcToRelative(
                    a = 1.175f,
                    b = 1.175f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.43f,
                    dy1 = -0.328f,
                )
                // c -0.464 0.236 -0.643 0.754 -0.359 1.154
                curveToRelative(
                    dx1 = -0.464f,
                    dy1 = 0.236f,
                    dx2 = -0.643f,
                    dy2 = 0.754f,
                    dx3 = -0.359f,
                    dy3 = 1.154f,
                )
                // C 4.396 15.113 6.044 16 8.082 16
                curveTo(
                    x1 = 4.396f,
                    y1 = 15.113f,
                    x2 = 6.044f,
                    y2 = 16.0f,
                    x3 = 8.082f,
                    y3 = 16.0f,
                )
                // c 1.308 0 2.451 -0.41 3.428 -1.23
                curveToRelative(
                    dx1 = 1.308f,
                    dy1 = 0.0f,
                    dx2 = 2.451f,
                    dy2 = -0.41f,
                    dx3 = 3.428f,
                    dy3 = -1.23f,
                )
                // c 0.993 -0.832 1.49 -1.812 1.49 -2.937
                curveToRelative(
                    dx1 = 0.993f,
                    dy1 = -0.832f,
                    dx2 = 1.49f,
                    dy2 = -1.812f,
                    dx3 = 1.49f,
                    dy3 = -2.937f,
                )
                // c 0 -0.282 -0.034 -0.56 -0.102 -0.833
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.282f,
                    dx2 = -0.034f,
                    dy2 = -0.56f,
                    dx3 = -0.102f,
                    dy3 = -0.833f,
                )
                // h -2.287z
                horizontalLineToRelative(dx = -2.287f)
                close()
            }
        }.build().also { _ic508Fill = it }
    }

@Suppress("ObjectPropertyName")
private var _ic508Fill: ImageVector? = null
