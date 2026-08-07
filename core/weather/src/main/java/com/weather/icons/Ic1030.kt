package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1030: ImageVector
    get() {
        val current = _ic1030
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1030",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M11.5 7 a.5 .5 0 0 0 -.5 .5 v2.063 a2 2 0 1 0 1 0 V7.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 11.5 7
                moveTo(x = 11.5f, y = 7.0f)
                // a 0.5 0.5 0 0 0 -0.5 0.5
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = 0.5f,
                )
                // v 2.063
                verticalLineToRelative(dy = 2.063f)
                // a 2 2 0 1 0 1 0
                arcToRelative(
                    a = 2.0f,
                    b = 2.0f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 7.5
                verticalLineTo(y = 7.5f)
                // a 0.5 0.5 0 0 0 -0.5 -0.5z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.5f,
                    dy1 = -0.5f,
                )
                close()
            }
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z m-6.349 -.65 a.35 .35 0 1 1 .7 0 v.544 l.471 -.272 a.35 .35 0 0 1 .35 .606 L3.7 3 l.47 .272 a.35 .35 0 0 1 -.35 .606 l-.47 -.272 v.544 a.35 .35 0 1 1 -.7 0 v-.544 l-.47 .272 a.35 .35 0 0 1 -.35 -.606 L2.3 3 l-.47 -.272 a.35 .35 0 0 1 .35 -.606 l.47 .272 V1.85Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.2 8.399
                moveTo(x = 10.2f, y = 8.399f)
                // l -0.532 0.356
                lineToRelative(dx = -0.532f, dy = 0.356f)
                // a 3.3 3.3 0 1 0 3.665 0
                arcToRelative(
                    a = 3.3f,
                    b = 3.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 3.665f,
                    dy1 = 0.0f,
                )
                // l -0.533 -0.356
                lineToRelative(dx = -0.533f, dy = -0.356f)
                // V 2.5
                verticalLineTo(y = 2.5f)
                // a 1.3 1.3 0 1 0 -2.6 0
                arcToRelative(
                    a = 1.3f,
                    b = 1.3f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -2.6f,
                    dy1 = 0.0f,
                )
                // v 5.899z
                verticalLineToRelative(dy = 5.899f)
                close()
                // M 9 2.5
                moveTo(x = 9.0f, y = 2.5f)
                // a 2.5 2.5 0 0 1 5 0
                arcToRelative(
                    a = 2.5f,
                    b = 2.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 5.0f,
                    dy1 = 0.0f,
                )
                // v 5.258
                verticalLineToRelative(dy = 5.258f)
                // a 4.5 4.5 0 1 1 -5 0
                arcToRelative(
                    a = 4.5f,
                    b = 4.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -5.0f,
                    dy1 = 0.0f,
                )
                // V 2.5z
                verticalLineTo(y = 2.5f)
                close()
                // m -6.349 -0.65
                moveToRelative(dx = -6.349f, dy = -0.65f)
                // a 0.35 0.35 0 1 1 0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.7f,
                    dy1 = 0.0f,
                )
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // l 0.471 -0.272
                lineToRelative(dx = 0.471f, dy = -0.272f)
                // a 0.35 0.35 0 0 1 0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = 0.606f,
                )
                // L 3.7 3
                lineTo(x = 3.7f, y = 3.0f)
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 1 -0.35 0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = 0.606f,
                )
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // v 0.544
                verticalLineToRelative(dy = 0.544f)
                // a 0.35 0.35 0 1 1 -0.7 0
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.7f,
                    dy1 = 0.0f,
                )
                // v -0.544
                verticalLineToRelative(dy = -0.544f)
                // l -0.47 0.272
                lineToRelative(dx = -0.47f, dy = 0.272f)
                // a 0.35 0.35 0 0 1 -0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.35f,
                    dy1 = -0.606f,
                )
                // L 2.3 3
                lineTo(x = 2.3f, y = 3.0f)
                // l -0.47 -0.272
                lineToRelative(dx = -0.47f, dy = -0.272f)
                // a 0.35 0.35 0 0 1 0.35 -0.606
                arcToRelative(
                    a = 0.35f,
                    b = 0.35f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.35f,
                    dy1 = -0.606f,
                )
                // l 0.47 0.272
                lineToRelative(dx = 0.47f, dy = 0.272f)
                // V 1.85z
                verticalLineTo(y = 1.85f)
                close()
            }
            // M5.493 1.404 3.131 .036 a.26 .26 0 0 0 -.263 0 L.507 1.404 a.264 .264 0 0 0 -.132 .228 v2.736 c0 .094 .05 .181 .132 .228 l2.361 1.368 a.258 .258 0 0 0 .263 0 l2.362 -1.368 a.264 .264 0 0 0 .132 -.228 V1.632 a.264 .264 0 0 0 -.132 -.228Z M5.1 4.216 3 5.432 .901 4.216 V1.785 L3 .569 l2.099 1.215 v2.432Z m.393 4.188 L3.131 7.036 a.26 .26 0 0 0 -.263 0 L.507 8.404 a.264 .264 0 0 0 -.132 .228 v2.736 c0 .094 .05 .181 .132 .228 l2.361 1.368 a.258 .258 0 0 0 .263 0 l2.362 -1.368 a.264 .264 0 0 0 .132 -.228 V8.632 a.264 .264 0 0 0 -.132 -.228Z M5.1 11.216 3 12.432 .901 11.216 V8.785 L3 7.569 l2.099 1.215 v2.432Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 5.493 1.404
                moveTo(x = 5.493f, y = 1.404f)
                // L 3.131 0.036
                lineTo(x = 3.131f, y = 0.036f)
                // a 0.26 0.26 0 0 0 -0.263 0
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.263f,
                    dy1 = 0.0f,
                )
                // L 0.507 1.404
                lineTo(x = 0.507f, y = 1.404f)
                // a 0.264 0.264 0 0 0 -0.132 0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.132f,
                    dy1 = 0.228f,
                )
                // v 2.736
                verticalLineToRelative(dy = 2.736f)
                // c 0 0.094 0.05 0.181 0.132 0.228
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.094f,
                    dx2 = 0.05f,
                    dy2 = 0.181f,
                    dx3 = 0.132f,
                    dy3 = 0.228f,
                )
                // l 2.361 1.368
                lineToRelative(dx = 2.361f, dy = 1.368f)
                // a 0.258 0.258 0 0 0 0.263 0
                arcToRelative(
                    a = 0.258f,
                    b = 0.258f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.263f,
                    dy1 = 0.0f,
                )
                // l 2.362 -1.368
                lineToRelative(dx = 2.362f, dy = -1.368f)
                // a 0.264 0.264 0 0 0 0.132 -0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.132f,
                    dy1 = -0.228f,
                )
                // V 1.632
                verticalLineTo(y = 1.632f)
                // a 0.264 0.264 0 0 0 -0.132 -0.228z
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.132f,
                    dy1 = -0.228f,
                )
                close()
                // M 5.1 4.216
                moveTo(x = 5.1f, y = 4.216f)
                // L 3 5.432
                lineTo(x = 3.0f, y = 5.432f)
                // L 0.901 4.216
                lineTo(x = 0.901f, y = 4.216f)
                // V 1.785
                verticalLineTo(y = 1.785f)
                // L 3 0.569
                lineTo(x = 3.0f, y = 0.569f)
                // l 2.099 1.215
                lineToRelative(dx = 2.099f, dy = 1.215f)
                // v 2.432z
                verticalLineToRelative(dy = 2.432f)
                close()
                // m 0.393 4.188
                moveToRelative(dx = 0.393f, dy = 4.188f)
                // L 3.131 7.036
                lineTo(x = 3.131f, y = 7.036f)
                // a 0.26 0.26 0 0 0 -0.263 0
                arcToRelative(
                    a = 0.26f,
                    b = 0.26f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.263f,
                    dy1 = 0.0f,
                )
                // L 0.507 8.404
                lineTo(x = 0.507f, y = 8.404f)
                // a 0.264 0.264 0 0 0 -0.132 0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.132f,
                    dy1 = 0.228f,
                )
                // v 2.736
                verticalLineToRelative(dy = 2.736f)
                // c 0 0.094 0.05 0.181 0.132 0.228
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = 0.094f,
                    dx2 = 0.05f,
                    dy2 = 0.181f,
                    dx3 = 0.132f,
                    dy3 = 0.228f,
                )
                // l 2.361 1.368
                lineToRelative(dx = 2.361f, dy = 1.368f)
                // a 0.258 0.258 0 0 0 0.263 0
                arcToRelative(
                    a = 0.258f,
                    b = 0.258f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.263f,
                    dy1 = 0.0f,
                )
                // l 2.362 -1.368
                lineToRelative(dx = 2.362f, dy = -1.368f)
                // a 0.264 0.264 0 0 0 0.132 -0.228
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.132f,
                    dy1 = -0.228f,
                )
                // V 8.632
                verticalLineTo(y = 8.632f)
                // a 0.264 0.264 0 0 0 -0.132 -0.228z
                arcToRelative(
                    a = 0.264f,
                    b = 0.264f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.132f,
                    dy1 = -0.228f,
                )
                close()
                // M 5.1 11.216
                moveTo(x = 5.1f, y = 11.216f)
                // L 3 12.432
                lineTo(x = 3.0f, y = 12.432f)
                // L 0.901 11.216
                lineTo(x = 0.901f, y = 11.216f)
                // V 8.785
                verticalLineTo(y = 8.785f)
                // L 3 7.569
                lineTo(x = 3.0f, y = 7.569f)
                // l 2.099 1.215
                lineToRelative(dx = 2.099f, dy = 1.215f)
                // v 2.432z
                verticalLineToRelative(dy = 2.432f)
                close()
            }
            // M2.293 11.207 A1 1 0 0 1 2 10.5 c0 -.5 .555 -1.395 1 -2 .445 .605 1 1.5 1 2 a1 1 0 0 1 -1.707 .707Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.293 11.207
                moveTo(x = 2.293f, y = 11.207f)
                // A 1 1 0 0 1 2 10.5
                arcTo(
                    horizontalEllipseRadius = 1.0f,
                    verticalEllipseRadius = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 2.0f,
                    y1 = 10.5f,
                )
                // c 0 -0.5 0.555 -1.395 1 -2
                curveToRelative(
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                    dx2 = 0.555f,
                    dy2 = -1.395f,
                    dx3 = 1.0f,
                    dy3 = -2.0f,
                )
                // c 0.445 0.605 1 1.5 1 2
                curveToRelative(
                    dx1 = 0.445f,
                    dy1 = 0.605f,
                    dx2 = 1.0f,
                    dy2 = 1.5f,
                    dx3 = 1.0f,
                    dy3 = 2.0f,
                )
                // a 1 1 0 0 1 -1.707 0.707z
                arcToRelative(
                    a = 1.0f,
                    b = 1.0f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.707f,
                    dy1 = 0.707f,
                )
                close()
            }
        }.build().also { _ic1030 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1030: ImageVector? = null
