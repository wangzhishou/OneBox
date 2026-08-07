package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2004: ImageVector
    get() {
        val current = _ic2004
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2004",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.59 13.18 c1.21 0 1.778 .725 2.27 1.297 .453 .532 .642 .761 1.133 .761 .227 0 .454 .153 .454 .381 a.38 .38 0 0 1 -.378 .381 c-.87 0 -1.286 -.495 -1.702 -1.028 -.454 -.534 -.832 -1.03 -1.702 -1.03 s-1.247 .496 -1.701 1.03 C9.51 15.505 9.094 16 8.262 16 h-.076 c-.87 0 -1.286 -.495 -1.702 -1.028 -.453 -.534 -.832 -1.03 -1.702 -1.03 s-1.248 .496 -1.702 1.03 C2.626 15.505 2.21 16 1.378 16 1.15 16 1 15.848 1 15.62 a.38 .38 0 0 1 .378 -.382 c.491 0 .719 -.266 1.135 -.761 .491 -.572 1.059 -1.296 2.27 -1.296 1.21 0 1.776 .724 2.268 1.296 .416 .495 .643 .723 1.06 .761 h.113 c.453 0 .68 -.266 1.096 -.761 .492 -.572 1.06 -1.296 2.27 -1.296Z M6.673 0 c1.021 0 1.891 .838 1.891 1.904 V4.23 c0 .914 -.643 1.676 -1.513 1.867 v.914 h2.837 c.075 0 1.323 -.038 2.08 .685 .302 .305 .492 .762 .492 1.257 v1.144 c.718 .228 1.134 .723 1.513 1.18 .453 .534 .643 .762 1.134 .762 a.38 .38 0 0 1 0 .762 c-.87 0 -1.286 -.495 -1.702 -1.029 -.454 -.533 -.832 -1.028 -1.702 -1.028 s-1.248 .495 -1.702 1.028 c-.454 .534 -.87 1.029 -1.702 1.029 h-.075 c-.87 0 -1.287 -.495 -1.703 -1.029 -.453 -.533 -.832 -1.028 -1.702 -1.028 s-1.247 .495 -1.7 1.028 c-.455 .534 -.871 1.029 -1.703 1.029 a.38 .38 0 0 1 0 -.762 c.492 0 .719 -.266 1.135 -.762 .491 -.571 1.058 -1.294 2.268 -1.295 1.21 0 1.778 .724 2.27 1.295 .378 .496 .606 .762 1.06 .762 h.113 c.454 0 .68 -.267 1.096 -.762 .492 -.571 1.06 -1.295 2.27 -1.295 l.037 -1.029 a.978 .978 0 0 0 -.303 -.723 c-.524 -.491 -1.531 -.496 -1.55 -.496 H1.379 a.38 .38 0 0 1 0 -.761 h4.917 v-.914 c-.87 -.153 -1.513 -.953 -1.513 -1.868 V1.904 C4.782 .876 5.652 0 6.672 0Z m0 .762 c-.643 0 -1.135 .495 -1.135 1.142 V4.19 c0 .534 .303 .953 .757 1.143 V3.886 a.38 .38 0 0 1 .378 -.381 .38 .38 0 0 1 .378 .38 v1.448 c.454 -.152 .795 -.61 .795 -1.104 V1.942 c0 -.647 -.53 -1.18 -1.173 -1.18Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.59 13.18
                moveTo(x = 11.59f, y = 13.18f)
                // c 1.21 0 1.778 0.725 2.27 1.297
                curveToRelative(
                    dx1 = 1.21f,
                    dy1 = 0.0f,
                    dx2 = 1.778f,
                    dy2 = 0.725f,
                    dx3 = 2.27f,
                    dy3 = 1.297f,
                )
                // c 0.453 0.532 0.642 0.761 1.133 0.761
                curveToRelative(
                    dx1 = 0.453f,
                    dy1 = 0.532f,
                    dx2 = 0.642f,
                    dy2 = 0.761f,
                    dx3 = 1.133f,
                    dy3 = 0.761f,
                )
                // c 0.227 0 0.454 0.153 0.454 0.381
                curveToRelative(
                    dx1 = 0.227f,
                    dy1 = 0.0f,
                    dx2 = 0.454f,
                    dy2 = 0.153f,
                    dx3 = 0.454f,
                    dy3 = 0.381f,
                )
                // a 0.38 0.38 0 0 1 -0.378 0.381
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.378f,
                    dy1 = 0.381f,
                )
                // c -0.87 0 -1.286 -0.495 -1.702 -1.028
                curveToRelative(
                    dx1 = -0.87f,
                    dy1 = 0.0f,
                    dx2 = -1.286f,
                    dy2 = -0.495f,
                    dx3 = -1.702f,
                    dy3 = -1.028f,
                )
                // c -0.454 -0.534 -0.832 -1.03 -1.702 -1.03
                curveToRelative(
                    dx1 = -0.454f,
                    dy1 = -0.534f,
                    dx2 = -0.832f,
                    dy2 = -1.03f,
                    dx3 = -1.702f,
                    dy3 = -1.03f,
                )
                // s -1.247 0.496 -1.701 1.03
                reflectiveCurveToRelative(
                    dx1 = -1.247f,
                    dy1 = 0.496f,
                    dx2 = -1.701f,
                    dy2 = 1.03f,
                )
                // C 9.51 15.505 9.094 16 8.262 16
                curveTo(
                    x1 = 9.51f,
                    y1 = 15.505f,
                    x2 = 9.094f,
                    y2 = 16.0f,
                    x3 = 8.262f,
                    y3 = 16.0f,
                )
                // h -0.076
                horizontalLineToRelative(dx = -0.076f)
                // c -0.87 0 -1.286 -0.495 -1.702 -1.028
                curveToRelative(
                    dx1 = -0.87f,
                    dy1 = 0.0f,
                    dx2 = -1.286f,
                    dy2 = -0.495f,
                    dx3 = -1.702f,
                    dy3 = -1.028f,
                )
                // c -0.453 -0.534 -0.832 -1.03 -1.702 -1.03
                curveToRelative(
                    dx1 = -0.453f,
                    dy1 = -0.534f,
                    dx2 = -0.832f,
                    dy2 = -1.03f,
                    dx3 = -1.702f,
                    dy3 = -1.03f,
                )
                // s -1.248 0.496 -1.702 1.03
                reflectiveCurveToRelative(
                    dx1 = -1.248f,
                    dy1 = 0.496f,
                    dx2 = -1.702f,
                    dy2 = 1.03f,
                )
                // C 2.626 15.505 2.21 16 1.378 16
                curveTo(
                    x1 = 2.626f,
                    y1 = 15.505f,
                    x2 = 2.21f,
                    y2 = 16.0f,
                    x3 = 1.378f,
                    y3 = 16.0f,
                )
                // C 1.15 16 1 15.848 1 15.62
                curveTo(
                    x1 = 1.15f,
                    y1 = 16.0f,
                    x2 = 1.0f,
                    y2 = 15.848f,
                    x3 = 1.0f,
                    y3 = 15.62f,
                )
                // a 0.38 0.38 0 0 1 0.378 -0.382
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.378f,
                    dy1 = -0.382f,
                )
                // c 0.491 0 0.719 -0.266 1.135 -0.761
                curveToRelative(
                    dx1 = 0.491f,
                    dy1 = 0.0f,
                    dx2 = 0.719f,
                    dy2 = -0.266f,
                    dx3 = 1.135f,
                    dy3 = -0.761f,
                )
                // c 0.491 -0.572 1.059 -1.296 2.27 -1.296
                curveToRelative(
                    dx1 = 0.491f,
                    dy1 = -0.572f,
                    dx2 = 1.059f,
                    dy2 = -1.296f,
                    dx3 = 2.27f,
                    dy3 = -1.296f,
                )
                // c 1.21 0 1.776 0.724 2.268 1.296
                curveToRelative(
                    dx1 = 1.21f,
                    dy1 = 0.0f,
                    dx2 = 1.776f,
                    dy2 = 0.724f,
                    dx3 = 2.268f,
                    dy3 = 1.296f,
                )
                // c 0.416 0.495 0.643 0.723 1.06 0.761
                curveToRelative(
                    dx1 = 0.416f,
                    dy1 = 0.495f,
                    dx2 = 0.643f,
                    dy2 = 0.723f,
                    dx3 = 1.06f,
                    dy3 = 0.761f,
                )
                // h 0.113
                horizontalLineToRelative(dx = 0.113f)
                // c 0.453 0 0.68 -0.266 1.096 -0.761
                curveToRelative(
                    dx1 = 0.453f,
                    dy1 = 0.0f,
                    dx2 = 0.68f,
                    dy2 = -0.266f,
                    dx3 = 1.096f,
                    dy3 = -0.761f,
                )
                // c 0.492 -0.572 1.06 -1.296 2.27 -1.296z
                curveToRelative(
                    dx1 = 0.492f,
                    dy1 = -0.572f,
                    dx2 = 1.06f,
                    dy2 = -1.296f,
                    dx3 = 2.27f,
                    dy3 = -1.296f,
                )
                close()
                // M 6.673 0
                moveTo(x = 6.673f, y = 0.0f)
                // c 1.021 0 1.891 0.838 1.891 1.904
                curveToRelative(
                    dx1 = 1.021f,
                    dy1 = 0.0f,
                    dx2 = 1.891f,
                    dy2 = 0.838f,
                    dx3 = 1.891f,
                    dy3 = 1.904f,
                )
                // V 4.23
                verticalLineTo(y = 4.23f)
                // c 0 0.914 -0.643 1.676 -1.513 1.867
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.914f,
                    dx2 = -0.643f,
                    dy2 = 1.676f,
                    dx3 = -1.513f,
                    dy3 = 1.867f,
                )
                // v 0.914
                verticalLineToRelative(dy = 0.914f)
                // h 2.837
                horizontalLineToRelative(dx = 2.837f)
                // c 0.075 0 1.323 -0.038 2.08 0.685
                curveToRelative(
                    dx1 = 0.075f,
                    dy1 = 0.0f,
                    dx2 = 1.323f,
                    dy2 = -0.038f,
                    dx3 = 2.08f,
                    dy3 = 0.685f,
                )
                // c 0.302 0.305 0.492 0.762 0.492 1.257
                curveToRelative(
                    dx1 = 0.302f,
                    dy1 = 0.305f,
                    dx2 = 0.492f,
                    dy2 = 0.762f,
                    dx3 = 0.492f,
                    dy3 = 1.257f,
                )
                // v 1.144
                verticalLineToRelative(dy = 1.144f)
                // c 0.718 0.228 1.134 0.723 1.513 1.18
                curveToRelative(
                    dx1 = 0.718f,
                    dy1 = 0.228f,
                    dx2 = 1.134f,
                    dy2 = 0.723f,
                    dx3 = 1.513f,
                    dy3 = 1.18f,
                )
                // c 0.453 0.534 0.643 0.762 1.134 0.762
                curveToRelative(
                    dx1 = 0.453f,
                    dy1 = 0.534f,
                    dx2 = 0.643f,
                    dy2 = 0.762f,
                    dx3 = 1.134f,
                    dy3 = 0.762f,
                )
                // a 0.38 0.38 0 0 1 0 0.762
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.762f,
                )
                // c -0.87 0 -1.286 -0.495 -1.702 -1.029
                curveToRelative(
                    dx1 = -0.87f,
                    dy1 = 0.0f,
                    dx2 = -1.286f,
                    dy2 = -0.495f,
                    dx3 = -1.702f,
                    dy3 = -1.029f,
                )
                // c -0.454 -0.533 -0.832 -1.028 -1.702 -1.028
                curveToRelative(
                    dx1 = -0.454f,
                    dy1 = -0.533f,
                    dx2 = -0.832f,
                    dy2 = -1.028f,
                    dx3 = -1.702f,
                    dy3 = -1.028f,
                )
                // s -1.248 0.495 -1.702 1.028
                reflectiveCurveToRelative(
                    dx1 = -1.248f,
                    dy1 = 0.495f,
                    dx2 = -1.702f,
                    dy2 = 1.028f,
                )
                // c -0.454 0.534 -0.87 1.029 -1.702 1.029
                curveToRelative(
                    dx1 = -0.454f,
                    dy1 = 0.534f,
                    dx2 = -0.87f,
                    dy2 = 1.029f,
                    dx3 = -1.702f,
                    dy3 = 1.029f,
                )
                // h -0.075
                horizontalLineToRelative(dx = -0.075f)
                // c -0.87 0 -1.287 -0.495 -1.703 -1.029
                curveToRelative(
                    dx1 = -0.87f,
                    dy1 = 0.0f,
                    dx2 = -1.287f,
                    dy2 = -0.495f,
                    dx3 = -1.703f,
                    dy3 = -1.029f,
                )
                // c -0.453 -0.533 -0.832 -1.028 -1.702 -1.028
                curveToRelative(
                    dx1 = -0.453f,
                    dy1 = -0.533f,
                    dx2 = -0.832f,
                    dy2 = -1.028f,
                    dx3 = -1.702f,
                    dy3 = -1.028f,
                )
                // s -1.247 0.495 -1.7 1.028
                reflectiveCurveToRelative(
                    dx1 = -1.247f,
                    dy1 = 0.495f,
                    dx2 = -1.7f,
                    dy2 = 1.028f,
                )
                // c -0.455 0.534 -0.871 1.029 -1.703 1.029
                curveToRelative(
                    dx1 = -0.455f,
                    dy1 = 0.534f,
                    dx2 = -0.871f,
                    dy2 = 1.029f,
                    dx3 = -1.703f,
                    dy3 = 1.029f,
                )
                // a 0.38 0.38 0 0 1 0 -0.762
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.762f,
                )
                // c 0.492 0 0.719 -0.266 1.135 -0.762
                curveToRelative(
                    dx1 = 0.492f,
                    dy1 = 0.0f,
                    dx2 = 0.719f,
                    dy2 = -0.266f,
                    dx3 = 1.135f,
                    dy3 = -0.762f,
                )
                // c 0.491 -0.571 1.058 -1.294 2.268 -1.295
                curveToRelative(
                    dx1 = 0.491f,
                    dy1 = -0.571f,
                    dx2 = 1.058f,
                    dy2 = -1.294f,
                    dx3 = 2.268f,
                    dy3 = -1.295f,
                )
                // c 1.21 0 1.778 0.724 2.27 1.295
                curveToRelative(
                    dx1 = 1.21f,
                    dy1 = 0.0f,
                    dx2 = 1.778f,
                    dy2 = 0.724f,
                    dx3 = 2.27f,
                    dy3 = 1.295f,
                )
                // c 0.378 0.496 0.606 0.762 1.06 0.762
                curveToRelative(
                    dx1 = 0.378f,
                    dy1 = 0.496f,
                    dx2 = 0.606f,
                    dy2 = 0.762f,
                    dx3 = 1.06f,
                    dy3 = 0.762f,
                )
                // h 0.113
                horizontalLineToRelative(dx = 0.113f)
                // c 0.454 0 0.68 -0.267 1.096 -0.762
                curveToRelative(
                    dx1 = 0.454f,
                    dy1 = 0.0f,
                    dx2 = 0.68f,
                    dy2 = -0.267f,
                    dx3 = 1.096f,
                    dy3 = -0.762f,
                )
                // c 0.492 -0.571 1.06 -1.295 2.27 -1.295
                curveToRelative(
                    dx1 = 0.492f,
                    dy1 = -0.571f,
                    dx2 = 1.06f,
                    dy2 = -1.295f,
                    dx3 = 2.27f,
                    dy3 = -1.295f,
                )
                // l 0.037 -1.029
                lineToRelative(dx = 0.037f, dy = -1.029f)
                // a 0.978 0.978 0 0 0 -0.303 -0.723
                arcToRelative(
                    a = 0.978f,
                    b = 0.978f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.303f,
                    dy1 = -0.723f,
                )
                // c -0.524 -0.491 -1.531 -0.496 -1.55 -0.496
                curveToRelative(
                    dx1 = -0.524f,
                    dy1 = -0.491f,
                    dx2 = -1.531f,
                    dy2 = -0.496f,
                    dx3 = -1.55f,
                    dy3 = -0.496f,
                )
                // H 1.379
                horizontalLineTo(x = 1.379f)
                // a 0.38 0.38 0 0 1 0 -0.761
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.761f,
                )
                // h 4.917
                horizontalLineToRelative(dx = 4.917f)
                // v -0.914
                verticalLineToRelative(dy = -0.914f)
                // c -0.87 -0.153 -1.513 -0.953 -1.513 -1.868
                curveToRelative(
                    dx1 = -0.87f,
                    dy1 = -0.153f,
                    dx2 = -1.513f,
                    dy2 = -0.953f,
                    dx3 = -1.513f,
                    dy3 = -1.868f,
                )
                // V 1.904
                verticalLineTo(y = 1.904f)
                // C 4.782 0.876 5.652 0 6.672 0z
                curveTo(
                    x1 = 4.782f,
                    y1 = 0.876f,
                    x2 = 5.652f,
                    y2 = 0.0f,
                    x3 = 6.672f,
                    y3 = 0.0f,
                )
                close()
                // m 0 0.762
                moveToRelative(dx = 0.0f, dy = 0.762f)
                // c -0.643 0 -1.135 0.495 -1.135 1.142
                curveToRelative(
                    dx1 = -0.643f,
                    dy1 = 0.0f,
                    dx2 = -1.135f,
                    dy2 = 0.495f,
                    dx3 = -1.135f,
                    dy3 = 1.142f,
                )
                // V 4.19
                verticalLineTo(y = 4.19f)
                // c 0 0.534 0.303 0.953 0.757 1.143
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.534f,
                    dx2 = 0.303f,
                    dy2 = 0.953f,
                    dx3 = 0.757f,
                    dy3 = 1.143f,
                )
                // V 3.886
                verticalLineTo(y = 3.886f)
                // a 0.38 0.38 0 0 1 0.378 -0.381
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.378f,
                    dy1 = -0.381f,
                )
                // a 0.38 0.38 0 0 1 0.378 0.38
                arcToRelative(
                    a = 0.38f,
                    b = 0.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.378f,
                    dy1 = 0.38f,
                )
                // v 1.448
                verticalLineToRelative(dy = 1.448f)
                // c 0.454 -0.152 0.795 -0.61 0.795 -1.104
                curveToRelative(
                    dx1 = 0.454f,
                    dy1 = -0.152f,
                    dx2 = 0.795f,
                    dy2 = -0.61f,
                    dx3 = 0.795f,
                    dy3 = -1.104f,
                )
                // V 1.942
                verticalLineTo(y = 1.942f)
                // c 0 -0.647 -0.53 -1.18 -1.173 -1.18z
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.647f,
                    dx2 = -0.53f,
                    dy2 = -1.18f,
                    dx3 = -1.173f,
                    dy3 = -1.18f,
                )
                close()
            }
        }.build().also { _ic2004 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2004: ImageVector? = null
