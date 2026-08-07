package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1217: ImageVector
    get() {
        val current = _ic1217
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1217",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.017 0 a8.001 8.001 0 0 1 8 8 8 8 0 0 1 -16 0 H0 c0 -.095 .008 -.19 .022 -.282 A8.001 8.001 0 0 1 8.017 0Z M5.384 1.512 A7.019 7.019 0 0 0 1.329 5.93 c.66 -.468 1.54 -.861 2.568 -1.15 .324 -1.311 .841 -2.438 1.487 -3.268Z M3.675 5.894 c-.326 .11 -.63 .23 -.907 .36 -.53 .248 -.914 .503 -1.186 .746 h1.97 c.027 -.377 .068 -.747 .123 -1.106Z M4.555 7 h2.962 V5.257 c-1.002 .03 -1.94 .15 -2.776 .34 -.086 .447 -.15 .916 -.186 1.403Z m2.962 -2.743 V1.082 c-.597 .193 -1.24 .739 -1.81 1.751 a8.376 8.376 0 0 0 -.703 1.689 c.784 -.149 1.63 -.24 2.513 -.265Z m1 1.001 V7 h2.963 c-.037 -.484 -.1 -.95 -.184 -1.394 a14.523 14.523 0 0 0 -2.779 -.348Z m2.516 -.73 a8.387 8.387 0 0 0 -.705 -1.695 c-.57 -1.012 -1.214 -1.558 -1.81 -1.751 v3.176 a15.9 15.9 0 0 1 2.515 .27Z m1.108 .263 c1.035 .294 1.917 .695 2.575 1.17 a7.019 7.019 0 0 0 -4.065 -4.449 c.647 .832 1.165 1.963 1.49 3.279Z m.22 1.115 c.055 .356 .095 .72 .122 1.094 h1.935 c-.272 -.243 -.657 -.498 -1.186 -.746 a8.432 8.432 0 0 0 -.87 -.348Z M3.6 7 l1.9 2.1 -1.1 1.6 -2.8 .9 .2 .6 2.7 -.8 .8 1.8 .6 -.3 L5 11 l1.1 -1.5 1.7 .1 L8.9 12 l.6 -.2 L8.2 9 l-2.1 -.2 L4.4 7 h-.8Z M11 9.4 l-1.3 .3 .1 .7 1.4 -.4 1.4 1 -2.6 3.3 .5 .4 2.6 -3.3 .6 .4 .4 -.5 -2.4 -1.8 .7 -2.5 h-.7 L11 9.4Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.017 0
                moveTo(x = 8.017f, y = 0.0f)
                // a 8.001 8.001 0 0 1 8 8
                arcToRelative(
                    a = 8.001f,
                    b = 8.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 8.0f,
                    dy1 = 8.0f,
                )
                // a 8 8 0 0 1 -16 0
                arcToRelative(
                    a = 8.0f,
                    b = 8.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -16.0f,
                    dy1 = 0.0f,
                )
                // H 0
                horizontalLineTo(x = 0.0f)
                // c 0 -0.095 0.008 -0.19 0.022 -0.282
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.095f,
                    dx2 = 0.008f,
                    dy2 = -0.19f,
                    dx3 = 0.022f,
                    dy3 = -0.282f,
                )
                // A 8.001 8.001 0 0 1 8.017 0z
                arcTo(
                    horizontalEllipseRadius = 8.001f,
                    verticalEllipseRadius = 8.001f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 8.017f,
                    y1 = 0.0f,
                )
                close()
                // M 5.384 1.512
                moveTo(x = 5.384f, y = 1.512f)
                // A 7.019 7.019 0 0 0 1.329 5.93
                arcTo(
                    horizontalEllipseRadius = 7.019f,
                    verticalEllipseRadius = 7.019f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 1.329f,
                    y1 = 5.93f,
                )
                // c 0.66 -0.468 1.54 -0.861 2.568 -1.15
                curveToRelative(
                    dx1 = 0.66f,
                    dy1 = -0.468f,
                    dx2 = 1.54f,
                    dy2 = -0.861f,
                    dx3 = 2.568f,
                    dy3 = -1.15f,
                )
                // c 0.324 -1.311 0.841 -2.438 1.487 -3.268z
                curveToRelative(
                    dx1 = 0.324f,
                    dy1 = -1.311f,
                    dx2 = 0.841f,
                    dy2 = -2.438f,
                    dx3 = 1.487f,
                    dy3 = -3.268f,
                )
                close()
                // M 3.675 5.894
                moveTo(x = 3.675f, y = 5.894f)
                // c -0.326 0.11 -0.63 0.23 -0.907 0.36
                curveToRelative(
                    dx1 = -0.326f,
                    dy1 = 0.11f,
                    dx2 = -0.63f,
                    dy2 = 0.23f,
                    dx3 = -0.907f,
                    dy3 = 0.36f,
                )
                // c -0.53 0.248 -0.914 0.503 -1.186 0.746
                curveToRelative(
                    dx1 = -0.53f,
                    dy1 = 0.248f,
                    dx2 = -0.914f,
                    dy2 = 0.503f,
                    dx3 = -1.186f,
                    dy3 = 0.746f,
                )
                // h 1.97
                horizontalLineToRelative(dx = 1.97f)
                // c 0.027 -0.377 0.068 -0.747 0.123 -1.106z
                curveToRelative(
                    dx1 = 0.027f,
                    dy1 = -0.377f,
                    dx2 = 0.068f,
                    dy2 = -0.747f,
                    dx3 = 0.123f,
                    dy3 = -1.106f,
                )
                close()
                // M 4.555 7
                moveTo(x = 4.555f, y = 7.0f)
                // h 2.962
                horizontalLineToRelative(dx = 2.962f)
                // V 5.257
                verticalLineTo(y = 5.257f)
                // c -1.002 0.03 -1.94 0.15 -2.776 0.34
                curveToRelative(
                    dx1 = -1.002f,
                    dy1 = 0.03f,
                    dx2 = -1.94f,
                    dy2 = 0.15f,
                    dx3 = -2.776f,
                    dy3 = 0.34f,
                )
                // c -0.086 0.447 -0.15 0.916 -0.186 1.403z
                curveToRelative(
                    dx1 = -0.086f,
                    dy1 = 0.447f,
                    dx2 = -0.15f,
                    dy2 = 0.916f,
                    dx3 = -0.186f,
                    dy3 = 1.403f,
                )
                close()
                // m 2.962 -2.743
                moveToRelative(dx = 2.962f, dy = -2.743f)
                // V 1.082
                verticalLineTo(y = 1.082f)
                // c -0.597 0.193 -1.24 0.739 -1.81 1.751
                curveToRelative(
                    dx1 = -0.597f,
                    dy1 = 0.193f,
                    dx2 = -1.24f,
                    dy2 = 0.739f,
                    dx3 = -1.81f,
                    dy3 = 1.751f,
                )
                // a 8.376 8.376 0 0 0 -0.703 1.689
                arcToRelative(
                    a = 8.376f,
                    b = 8.376f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.703f,
                    dy1 = 1.689f,
                )
                // c 0.784 -0.149 1.63 -0.24 2.513 -0.265z
                curveToRelative(
                    dx1 = 0.784f,
                    dy1 = -0.149f,
                    dx2 = 1.63f,
                    dy2 = -0.24f,
                    dx3 = 2.513f,
                    dy3 = -0.265f,
                )
                close()
                // m 1 1.001
                moveToRelative(dx = 1.0f, dy = 1.001f)
                // V 7
                verticalLineTo(y = 7.0f)
                // h 2.963
                horizontalLineToRelative(dx = 2.963f)
                // c -0.037 -0.484 -0.1 -0.95 -0.184 -1.394
                curveToRelative(
                    dx1 = -0.037f,
                    dy1 = -0.484f,
                    dx2 = -0.1f,
                    dy2 = -0.95f,
                    dx3 = -0.184f,
                    dy3 = -1.394f,
                )
                // a 14.523 14.523 0 0 0 -2.779 -0.348z
                arcToRelative(
                    a = 14.523f,
                    b = 14.523f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -2.779f,
                    dy1 = -0.348f,
                )
                close()
                // m 2.516 -0.73
                moveToRelative(dx = 2.516f, dy = -0.73f)
                // a 8.387 8.387 0 0 0 -0.705 -1.695
                arcToRelative(
                    a = 8.387f,
                    b = 8.387f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.705f,
                    dy1 = -1.695f,
                )
                // c -0.57 -1.012 -1.214 -1.558 -1.81 -1.751
                curveToRelative(
                    dx1 = -0.57f,
                    dy1 = -1.012f,
                    dx2 = -1.214f,
                    dy2 = -1.558f,
                    dx3 = -1.81f,
                    dy3 = -1.751f,
                )
                // v 3.176
                verticalLineToRelative(dy = 3.176f)
                // a 15.9 15.9 0 0 1 2.515 0.27z
                arcToRelative(
                    a = 15.9f,
                    b = 15.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 2.515f,
                    dy1 = 0.27f,
                )
                close()
                // m 1.108 0.263
                moveToRelative(dx = 1.108f, dy = 0.263f)
                // c 1.035 0.294 1.917 0.695 2.575 1.17
                curveToRelative(
                    dx1 = 1.035f,
                    dy1 = 0.294f,
                    dx2 = 1.917f,
                    dy2 = 0.695f,
                    dx3 = 2.575f,
                    dy3 = 1.17f,
                )
                // a 7.019 7.019 0 0 0 -4.065 -4.449
                arcToRelative(
                    a = 7.019f,
                    b = 7.019f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -4.065f,
                    dy1 = -4.449f,
                )
                // c 0.647 0.832 1.165 1.963 1.49 3.279z
                curveToRelative(
                    dx1 = 0.647f,
                    dy1 = 0.832f,
                    dx2 = 1.165f,
                    dy2 = 1.963f,
                    dx3 = 1.49f,
                    dy3 = 3.279f,
                )
                close()
                // m 0.22 1.115
                moveToRelative(dx = 0.22f, dy = 1.115f)
                // c 0.055 0.356 0.095 0.72 0.122 1.094
                curveToRelative(
                    dx1 = 0.055f,
                    dy1 = 0.356f,
                    dx2 = 0.095f,
                    dy2 = 0.72f,
                    dx3 = 0.122f,
                    dy3 = 1.094f,
                )
                // h 1.935
                horizontalLineToRelative(dx = 1.935f)
                // c -0.272 -0.243 -0.657 -0.498 -1.186 -0.746
                curveToRelative(
                    dx1 = -0.272f,
                    dy1 = -0.243f,
                    dx2 = -0.657f,
                    dy2 = -0.498f,
                    dx3 = -1.186f,
                    dy3 = -0.746f,
                )
                // a 8.432 8.432 0 0 0 -0.87 -0.348z
                arcToRelative(
                    a = 8.432f,
                    b = 8.432f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.87f,
                    dy1 = -0.348f,
                )
                close()
                // M 3.6 7
                moveTo(x = 3.6f, y = 7.0f)
                // l 1.9 2.1
                lineToRelative(dx = 1.9f, dy = 2.1f)
                // l -1.1 1.6
                lineToRelative(dx = -1.1f, dy = 1.6f)
                // l -2.8 0.9
                lineToRelative(dx = -2.8f, dy = 0.9f)
                // l 0.2 0.6
                lineToRelative(dx = 0.2f, dy = 0.6f)
                // l 2.7 -0.8
                lineToRelative(dx = 2.7f, dy = -0.8f)
                // l 0.8 1.8
                lineToRelative(dx = 0.8f, dy = 1.8f)
                // l 0.6 -0.3
                lineToRelative(dx = 0.6f, dy = -0.3f)
                // L 5 11
                lineTo(x = 5.0f, y = 11.0f)
                // l 1.1 -1.5
                lineToRelative(dx = 1.1f, dy = -1.5f)
                // l 1.7 0.1
                lineToRelative(dx = 1.7f, dy = 0.1f)
                // L 8.9 12
                lineTo(x = 8.9f, y = 12.0f)
                // l 0.6 -0.2
                lineToRelative(dx = 0.6f, dy = -0.2f)
                // L 8.2 9
                lineTo(x = 8.2f, y = 9.0f)
                // l -2.1 -0.2
                lineToRelative(dx = -2.1f, dy = -0.2f)
                // L 4.4 7
                lineTo(x = 4.4f, y = 7.0f)
                // h -0.8z
                horizontalLineToRelative(dx = -0.8f)
                close()
                // M 11 9.4
                moveTo(x = 11.0f, y = 9.4f)
                // l -1.3 0.3
                lineToRelative(dx = -1.3f, dy = 0.3f)
                // l 0.1 0.7
                lineToRelative(dx = 0.1f, dy = 0.7f)
                // l 1.4 -0.4
                lineToRelative(dx = 1.4f, dy = -0.4f)
                // l 1.4 1
                lineToRelative(dx = 1.4f, dy = 1.0f)
                // l -2.6 3.3
                lineToRelative(dx = -2.6f, dy = 3.3f)
                // l 0.5 0.4
                lineToRelative(dx = 0.5f, dy = 0.4f)
                // l 2.6 -3.3
                lineToRelative(dx = 2.6f, dy = -3.3f)
                // l 0.6 0.4
                lineToRelative(dx = 0.6f, dy = 0.4f)
                // l 0.4 -0.5
                lineToRelative(dx = 0.4f, dy = -0.5f)
                // l -2.4 -1.8
                lineToRelative(dx = -2.4f, dy = -1.8f)
                // l 0.7 -2.5
                lineToRelative(dx = 0.7f, dy = -2.5f)
                // h -0.7
                horizontalLineToRelative(dx = -0.7f)
                // L 11 9.4z
                lineTo(x = 11.0f, y = 9.4f)
                close()
            }
        }.build().also { _ic1217 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1217: ImageVector? = null
