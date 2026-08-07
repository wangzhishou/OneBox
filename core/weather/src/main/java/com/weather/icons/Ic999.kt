package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic999: ImageVector
    get() {
        val current = _ic999
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic999",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.03 9.283 c.265 0 .45 -.221 .53 -.475 a.866 .866 0 0 1 .309 -.423 l.285 -.222 c.288 -.225 .484 -.41 .585 -.556 .174 -.239 .261 -.533 .261 -.882 0 -.57 -.2 -1 -.602 -1.29 C9 5.145 8.498 5 7.894 5 c-.46 0 -.847 .102 -1.163 .306 -.36 .23 -.6 .578 -.717 1.04 -.08 .315 .188 .588 .51 .588 .314 0 .56 -.268 .705 -.547 a1.63 1.63 0 0 1 .056 -.098 c.13 -.206 .351 -.31 .661 -.31 .316 0 .533 .085 .65 .254 a.93 .93 0 0 1 .18 .556 c0 .177 -.053 .34 -.16 .488 a1.026 1.026 0 0 1 -.232 .237 l-.293 .23 c-.289 .226 -.468 .426 -.538 .6 -.036 .09 -.065 .218 -.087 .384 -.039 .302 .21 .555 .513 .555 h.051Z M8.013 11 a.581 .581 0 0 0 0 -1.165 h-.037 a.582 .582 0 0 0 0 1.165 h.037Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.03 9.283
                moveTo(x = 8.03f, y = 9.283f)
                // c 0.265 0 0.45 -0.221 0.53 -0.475
                curveToRelative(
                    dx1 = 0.265f,
                    dy1 = 0.0f,
                    dx2 = 0.45f,
                    dy2 = -0.221f,
                    dx3 = 0.53f,
                    dy3 = -0.475f,
                )
                // a 0.866 0.866 0 0 1 0.309 -0.423
                arcToRelative(
                    a = 0.866f,
                    b = 0.866f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.309f,
                    dy1 = -0.423f,
                )
                // l 0.285 -0.222
                lineToRelative(dx = 0.285f, dy = -0.222f)
                // c 0.288 -0.225 0.484 -0.41 0.585 -0.556
                curveToRelative(
                    dx1 = 0.288f,
                    dy1 = -0.225f,
                    dx2 = 0.484f,
                    dy2 = -0.41f,
                    dx3 = 0.585f,
                    dy3 = -0.556f,
                )
                // c 0.174 -0.239 0.261 -0.533 0.261 -0.882
                curveToRelative(
                    dx1 = 0.174f,
                    dy1 = -0.239f,
                    dx2 = 0.261f,
                    dy2 = -0.533f,
                    dx3 = 0.261f,
                    dy3 = -0.882f,
                )
                // c 0 -0.57 -0.2 -1 -0.602 -1.29
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.57f,
                    dx2 = -0.2f,
                    dy2 = -1.0f,
                    dx3 = -0.602f,
                    dy3 = -1.29f,
                )
                // C 9 5.145 8.498 5 7.894 5
                curveTo(
                    x1 = 9.0f,
                    y1 = 5.145f,
                    x2 = 8.498f,
                    y2 = 5.0f,
                    x3 = 7.894f,
                    y3 = 5.0f,
                )
                // c -0.46 0 -0.847 0.102 -1.163 0.306
                curveToRelative(
                    dx1 = -0.46f,
                    dy1 = 0.0f,
                    dx2 = -0.847f,
                    dy2 = 0.102f,
                    dx3 = -1.163f,
                    dy3 = 0.306f,
                )
                // c -0.36 0.23 -0.6 0.578 -0.717 1.04
                curveToRelative(
                    dx1 = -0.36f,
                    dy1 = 0.23f,
                    dx2 = -0.6f,
                    dy2 = 0.578f,
                    dx3 = -0.717f,
                    dy3 = 1.04f,
                )
                // c -0.08 0.315 0.188 0.588 0.51 0.588
                curveToRelative(
                    dx1 = -0.08f,
                    dy1 = 0.315f,
                    dx2 = 0.188f,
                    dy2 = 0.588f,
                    dx3 = 0.51f,
                    dy3 = 0.588f,
                )
                // c 0.314 0 0.56 -0.268 0.705 -0.547
                curveToRelative(
                    dx1 = 0.314f,
                    dy1 = 0.0f,
                    dx2 = 0.56f,
                    dy2 = -0.268f,
                    dx3 = 0.705f,
                    dy3 = -0.547f,
                )
                // a 1.63 1.63 0 0 1 0.056 -0.098
                arcToRelative(
                    a = 1.63f,
                    b = 1.63f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.056f,
                    dy1 = -0.098f,
                )
                // c 0.13 -0.206 0.351 -0.31 0.661 -0.31
                curveToRelative(
                    dx1 = 0.13f,
                    dy1 = -0.206f,
                    dx2 = 0.351f,
                    dy2 = -0.31f,
                    dx3 = 0.661f,
                    dy3 = -0.31f,
                )
                // c 0.316 0 0.533 0.085 0.65 0.254
                curveToRelative(
                    dx1 = 0.316f,
                    dy1 = 0.0f,
                    dx2 = 0.533f,
                    dy2 = 0.085f,
                    dx3 = 0.65f,
                    dy3 = 0.254f,
                )
                // a 0.93 0.93 0 0 1 0.18 0.556
                arcToRelative(
                    a = 0.93f,
                    b = 0.93f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.18f,
                    dy1 = 0.556f,
                )
                // c 0 0.177 -0.053 0.34 -0.16 0.488
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.177f,
                    dx2 = -0.053f,
                    dy2 = 0.34f,
                    dx3 = -0.16f,
                    dy3 = 0.488f,
                )
                // a 1.026 1.026 0 0 1 -0.232 0.237
                arcToRelative(
                    a = 1.026f,
                    b = 1.026f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.232f,
                    dy1 = 0.237f,
                )
                // l -0.293 0.23
                lineToRelative(dx = -0.293f, dy = 0.23f)
                // c -0.289 0.226 -0.468 0.426 -0.538 0.6
                curveToRelative(
                    dx1 = -0.289f,
                    dy1 = 0.226f,
                    dx2 = -0.468f,
                    dy2 = 0.426f,
                    dx3 = -0.538f,
                    dy3 = 0.6f,
                )
                // c -0.036 0.09 -0.065 0.218 -0.087 0.384
                curveToRelative(
                    dx1 = -0.036f,
                    dy1 = 0.09f,
                    dx2 = -0.065f,
                    dy2 = 0.218f,
                    dx3 = -0.087f,
                    dy3 = 0.384f,
                )
                // c -0.039 0.302 0.21 0.555 0.513 0.555
                curveToRelative(
                    dx1 = -0.039f,
                    dy1 = 0.302f,
                    dx2 = 0.21f,
                    dy2 = 0.555f,
                    dx3 = 0.513f,
                    dy3 = 0.555f,
                )
                // h 0.051z
                horizontalLineToRelative(dx = 0.051f)
                close()
                // M 8.013 11
                moveTo(x = 8.013f, y = 11.0f)
                // a 0.581 0.581 0 0 0 0 -1.165
                arcToRelative(
                    a = 0.581f,
                    b = 0.581f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.165f,
                )
                // h -0.037
                horizontalLineToRelative(dx = -0.037f)
                // a 0.582 0.582 0 0 0 0 1.165
                arcToRelative(
                    a = 0.582f,
                    b = 0.582f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.165f,
                )
                // h 0.037z
                horizontalLineToRelative(dx = 0.037f)
                close()
            }
            // M11.575 11.39 A4.986 4.986 0 0 1 7.9 13 a4.99 4.99 0 0 1 -3.629 -1.56 .334 .334 0 0 0 -.345 -.086 3 3 0 1 1 -.596 -5.836 .334 .334 0 0 0 .32 -.153 A4.997 4.997 0 0 1 7.9 3 c1.804 0 3.385 .956 4.264 2.388 a.335 .335 0 0 0 .341 .153 3 3 0 1 1 -.566 5.767 .335 .335 0 0 0 -.364 .082Z m-.325 -1.203 c.117 -.18 .383 -.212 .555 -.083 a2 2 0 1 0 .335 -3.41 c-.194 .092 -.449 .01 -.53 -.19 a4.001 4.001 0 0 0 -7.396 -.059 c-.078 .183 -.302 .264 -.486 .191 a2 2 0 1 0 .362 3.54 c.166 -.107 .401 -.073 .513 .09 A3.996 3.996 0 0 0 7.9 12 a3.996 3.996 0 0 0 3.35 -1.813Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.575 11.39
                moveTo(x = 11.575f, y = 11.39f)
                // A 4.986 4.986 0 0 1 7.9 13
                arcTo(
                    horizontalEllipseRadius = 4.986f,
                    verticalEllipseRadius = 4.986f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 13.0f,
                )
                // a 4.99 4.99 0 0 1 -3.629 -1.56
                arcToRelative(
                    a = 4.99f,
                    b = 4.99f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -3.629f,
                    dy1 = -1.56f,
                )
                // a 0.334 0.334 0 0 0 -0.345 -0.086
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.345f,
                    dy1 = -0.086f,
                )
                // a 3 3 0 1 1 -0.596 -5.836
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.596f,
                    dy1 = -5.836f,
                )
                // a 0.334 0.334 0 0 0 0.32 -0.153
                arcToRelative(
                    a = 0.334f,
                    b = 0.334f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.32f,
                    dy1 = -0.153f,
                )
                // A 4.997 4.997 0 0 1 7.9 3
                arcTo(
                    horizontalEllipseRadius = 4.997f,
                    verticalEllipseRadius = 4.997f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.9f,
                    y1 = 3.0f,
                )
                // c 1.804 0 3.385 0.956 4.264 2.388
                curveToRelative(
                    dx1 = 1.804f,
                    dy1 = 0.0f,
                    dx2 = 3.385f,
                    dy2 = 0.956f,
                    dx3 = 4.264f,
                    dy3 = 2.388f,
                )
                // a 0.335 0.335 0 0 0 0.341 0.153
                arcToRelative(
                    a = 0.335f,
                    b = 0.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.341f,
                    dy1 = 0.153f,
                )
                // a 3 3 0 1 1 -0.566 5.767
                arcToRelative(
                    a = 3.0f,
                    b = 3.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.566f,
                    dy1 = 5.767f,
                )
                // a 0.335 0.335 0 0 0 -0.364 0.082z
                arcToRelative(
                    a = 0.335f,
                    b = 0.335f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.364f,
                    dy1 = 0.082f,
                )
                close()
                // m -0.325 -1.203
                moveToRelative(dx = -0.325f, dy = -1.203f)
                // c 0.117 -0.18 0.383 -0.212 0.555 -0.083
                curveToRelative(
                    dx1 = 0.117f,
                    dy1 = -0.18f,
                    dx2 = 0.383f,
                    dy2 = -0.212f,
                    dx3 = 0.555f,
                    dy3 = -0.083f,
                )
                // a 2 2 0 1 0 0.335 -3.41
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.335f,
                    dy1 = -3.41f,
                )
                // c -0.194 0.092 -0.449 0.01 -0.53 -0.19
                curveToRelative(
                    dx1 = -0.194f,
                    dy1 = 0.092f,
                    dx2 = -0.449f,
                    dy2 = 0.01f,
                    dx3 = -0.53f,
                    dy3 = -0.19f,
                )
                // a 4.001 4.001 0 0 0 -7.396 -0.059
                arcToRelative(
                    a = 4.001f,
                    b = 4.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -7.396f,
                    dy1 = -0.059f,
                )
                // c -0.078 0.183 -0.302 0.264 -0.486 0.191
                curveToRelative(
                    dx1 = -0.078f,
                    dy1 = 0.183f,
                    dx2 = -0.302f,
                    dy2 = 0.264f,
                    dx3 = -0.486f,
                    dy3 = 0.191f,
                )
                // a 2 2 0 1 0 0.362 3.54
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.362f,
                    dy1 = 3.54f,
                )
                // c 0.166 -0.107 0.401 -0.073 0.513 0.09
                curveToRelative(
                    dx1 = 0.166f,
                    dy1 = -0.107f,
                    dx2 = 0.401f,
                    dy2 = -0.073f,
                    dx3 = 0.513f,
                    dy3 = 0.09f,
                )
                // A 3.996 3.996 0 0 0 7.9 12
                arcTo(
                    horizontalEllipseRadius = 3.996f,
                    verticalEllipseRadius = 3.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 7.9f,
                    y1 = 12.0f,
                )
                // a 3.996 3.996 0 0 0 3.35 -1.813z
                arcToRelative(
                    a = 3.996f,
                    b = 3.996f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.35f,
                    dy1 = -1.813f,
                )
                close()
            }
        }.build().also { _ic999 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic999: ImageVector? = null
