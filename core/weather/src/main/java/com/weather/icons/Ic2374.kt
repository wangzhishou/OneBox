package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2374: ImageVector
    get() {
        val current = _ic2374
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2374",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M7.5 0 v3.066 l-.723 -.482 -.554 .832 1.5 1 L8 4.601 l.277 -.185 1.5 -1 -.554 -.832 -.723 .482 V0 h-1Z M4.416 7.723 l-1 -1.5 -.832 .554 .482 .723 H0 v1 h3.066 l-.482 .723 .832 .554 1 -1.5 L4.601 8 l-.185 -.277Z m9 1.5 -.482 -.723 H16 v-1 h-3.066 l.482 -.723 -.832 -.554 -1 1.5 -.185 .277 .185 .277 1 1.5 .832 -.554Z M8 12 l.277 -.416 h.001 l.002 .002 .008 .005 .03 .02 .111 .074 .379 .252 .97 .647 -.555 .832 -.723 -.482 V16 h-1 v-3.066 l-.723 .482 -.554 -.832 1.5 -1 L8 11.399 l.277 .185 L8 12Z M5.734 8.239 l1.287 -.816 a.108 .108 0 0 0 -.025 -.194 L6.3 7.01 c.083 -.148 .185 -.285 .303 -.407 .18 -.18 .392 -.323 .625 -.423 a1.982 1.982 0 0 1 1.532 0 1.972 1.972 0 0 1 1.047 1.05 .263 .263 0 0 0 .485 -.205 2.487 2.487 0 0 0 -1.327 -1.33 2.507 2.507 0 0 0 -1.942 0 2.482 2.482 0 0 0 -.793 .535 2.48 2.48 0 0 0 -.439 .62 l-.652 -.205 a.107 .107 0 0 0 -.138 .085 .108 .108 0 0 0 .006 .06 l.57 1.397 a.109 .109 0 0 0 .156 .05Z m5.258 .903 -.57 -1.398 a.107 .107 0 0 0 -.112 -.066 .107 .107 0 0 0 -.044 .016 l-1.287 .815 a.108 .108 0 0 0 .026 .195 l.716 .225 a1.97 1.97 0 0 1 -.335 .466 1.958 1.958 0 0 1 -2.157 .423 1.966 1.966 0 0 1 -1.047 -1.05 .263 .263 0 0 0 -.484 .205 c.125 .297 .305 .565 .534 .795 a2.485 2.485 0 0 0 3.526 0 2.52 2.52 0 0 0 .472 -.68 l.63 .198 a.108 .108 0 0 0 .132 -.144Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.5 0
                moveTo(x = 7.5f, y = 0.0f)
                // v 3.066
                verticalLineToRelative(dy = 3.066f)
                // l -0.723 -0.482
                lineToRelative(dx = -0.723f, dy = -0.482f)
                // l -0.554 0.832
                lineToRelative(dx = -0.554f, dy = 0.832f)
                // l 1.5 1
                lineToRelative(dx = 1.5f, dy = 1.0f)
                // L 8 4.601
                lineTo(x = 8.0f, y = 4.601f)
                // l 0.277 -0.185
                lineToRelative(dx = 0.277f, dy = -0.185f)
                // l 1.5 -1
                lineToRelative(dx = 1.5f, dy = -1.0f)
                // l -0.554 -0.832
                lineToRelative(dx = -0.554f, dy = -0.832f)
                // l -0.723 0.482
                lineToRelative(dx = -0.723f, dy = 0.482f)
                // V 0
                verticalLineTo(y = 0.0f)
                // h -1z
                horizontalLineToRelative(dx = -1.0f)
                close()
                // M 4.416 7.723
                moveTo(x = 4.416f, y = 7.723f)
                // l -1 -1.5
                lineToRelative(dx = -1.0f, dy = -1.5f)
                // l -0.832 0.554
                lineToRelative(dx = -0.832f, dy = 0.554f)
                // l 0.482 0.723
                lineToRelative(dx = 0.482f, dy = 0.723f)
                // H 0
                horizontalLineTo(x = 0.0f)
                // v 1
                verticalLineToRelative(dy = 1.0f)
                // h 3.066
                horizontalLineToRelative(dx = 3.066f)
                // l -0.482 0.723
                lineToRelative(dx = -0.482f, dy = 0.723f)
                // l 0.832 0.554
                lineToRelative(dx = 0.832f, dy = 0.554f)
                // l 1 -1.5
                lineToRelative(dx = 1.0f, dy = -1.5f)
                // L 4.601 8
                lineTo(x = 4.601f, y = 8.0f)
                // l -0.185 -0.277z
                lineToRelative(dx = -0.185f, dy = -0.277f)
                close()
                // m 9 1.5
                moveToRelative(dx = 9.0f, dy = 1.5f)
                // l -0.482 -0.723
                lineToRelative(dx = -0.482f, dy = -0.723f)
                // H 16
                horizontalLineTo(x = 16.0f)
                // v -1
                verticalLineToRelative(dy = -1.0f)
                // h -3.066
                horizontalLineToRelative(dx = -3.066f)
                // l 0.482 -0.723
                lineToRelative(dx = 0.482f, dy = -0.723f)
                // l -0.832 -0.554
                lineToRelative(dx = -0.832f, dy = -0.554f)
                // l -1 1.5
                lineToRelative(dx = -1.0f, dy = 1.5f)
                // l -0.185 0.277
                lineToRelative(dx = -0.185f, dy = 0.277f)
                // l 0.185 0.277
                lineToRelative(dx = 0.185f, dy = 0.277f)
                // l 1 1.5
                lineToRelative(dx = 1.0f, dy = 1.5f)
                // l 0.832 -0.554z
                lineToRelative(dx = 0.832f, dy = -0.554f)
                close()
                // M 8 12
                moveTo(x = 8.0f, y = 12.0f)
                // l 0.277 -0.416
                lineToRelative(dx = 0.277f, dy = -0.416f)
                // h 0.001
                horizontalLineToRelative(dx = 0.001f)
                // l 0.002 0.002
                lineToRelative(dx = 0.002f, dy = 0.002f)
                // l 0.008 0.005
                lineToRelative(dx = 0.008f, dy = 0.005f)
                // l 0.03 0.02
                lineToRelative(dx = 0.03f, dy = 0.02f)
                // l 0.111 0.074
                lineToRelative(dx = 0.111f, dy = 0.074f)
                // l 0.379 0.252
                lineToRelative(dx = 0.379f, dy = 0.252f)
                // l 0.97 0.647
                lineToRelative(dx = 0.97f, dy = 0.647f)
                // l -0.555 0.832
                lineToRelative(dx = -0.555f, dy = 0.832f)
                // l -0.723 -0.482
                lineToRelative(dx = -0.723f, dy = -0.482f)
                // V 16
                verticalLineTo(y = 16.0f)
                // h -1
                horizontalLineToRelative(dx = -1.0f)
                // v -3.066
                verticalLineToRelative(dy = -3.066f)
                // l -0.723 0.482
                lineToRelative(dx = -0.723f, dy = 0.482f)
                // l -0.554 -0.832
                lineToRelative(dx = -0.554f, dy = -0.832f)
                // l 1.5 -1
                lineToRelative(dx = 1.5f, dy = -1.0f)
                // L 8 11.399
                lineTo(x = 8.0f, y = 11.399f)
                // l 0.277 0.185
                lineToRelative(dx = 0.277f, dy = 0.185f)
                // L 8 12z
                lineTo(x = 8.0f, y = 12.0f)
                close()
                // M 5.734 8.239
                moveTo(x = 5.734f, y = 8.239f)
                // l 1.287 -0.816
                lineToRelative(dx = 1.287f, dy = -0.816f)
                // a 0.108 0.108 0 0 0 -0.025 -0.194
                arcToRelative(
                    a = 0.108f,
                    b = 0.108f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.025f,
                    dy1 = -0.194f,
                )
                // L 6.3 7.01
                lineTo(x = 6.3f, y = 7.01f)
                // c 0.083 -0.148 0.185 -0.285 0.303 -0.407
                curveToRelative(
                    dx1 = 0.083f,
                    dy1 = -0.148f,
                    dx2 = 0.185f,
                    dy2 = -0.285f,
                    dx3 = 0.303f,
                    dy3 = -0.407f,
                )
                // c 0.18 -0.18 0.392 -0.323 0.625 -0.423
                curveToRelative(
                    dx1 = 0.18f,
                    dy1 = -0.18f,
                    dx2 = 0.392f,
                    dy2 = -0.323f,
                    dx3 = 0.625f,
                    dy3 = -0.423f,
                )
                // a 1.982 1.982 0 0 1 1.532 0
                arcToRelative(
                    a = 1.982f,
                    b = 1.982f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.532f,
                    dy1 = 0.0f,
                )
                // a 1.972 1.972 0 0 1 1.047 1.05
                arcToRelative(
                    a = 1.972f,
                    b = 1.972f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.047f,
                    dy1 = 1.05f,
                )
                // a 0.263 0.263 0 0 0 0.485 -0.205
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.485f,
                    dy1 = -0.205f,
                )
                // a 2.487 2.487 0 0 0 -1.327 -1.33
                arcToRelative(
                    a = 2.487f,
                    b = 2.487f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.327f,
                    dy1 = -1.33f,
                )
                // a 2.507 2.507 0 0 0 -1.942 0
                arcToRelative(
                    a = 2.507f,
                    b = 2.507f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.942f,
                    dy1 = 0.0f,
                )
                // a 2.482 2.482 0 0 0 -0.793 0.535
                arcToRelative(
                    a = 2.482f,
                    b = 2.482f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.793f,
                    dy1 = 0.535f,
                )
                // a 2.48 2.48 0 0 0 -0.439 0.62
                arcToRelative(
                    a = 2.48f,
                    b = 2.48f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.439f,
                    dy1 = 0.62f,
                )
                // l -0.652 -0.205
                lineToRelative(dx = -0.652f, dy = -0.205f)
                // a 0.107 0.107 0 0 0 -0.138 0.085
                arcToRelative(
                    a = 0.107f,
                    b = 0.107f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.138f,
                    dy1 = 0.085f,
                )
                // a 0.108 0.108 0 0 0 0.006 0.06
                arcToRelative(
                    a = 0.108f,
                    b = 0.108f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.006f,
                    dy1 = 0.06f,
                )
                // l 0.57 1.397
                lineToRelative(dx = 0.57f, dy = 1.397f)
                // a 0.109 0.109 0 0 0 0.156 0.05z
                arcToRelative(
                    a = 0.109f,
                    b = 0.109f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.156f,
                    dy1 = 0.05f,
                )
                close()
                // m 5.258 0.903
                moveToRelative(dx = 5.258f, dy = 0.903f)
                // l -0.57 -1.398
                lineToRelative(dx = -0.57f, dy = -1.398f)
                // a 0.107 0.107 0 0 0 -0.112 -0.066
                arcToRelative(
                    a = 0.107f,
                    b = 0.107f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.112f,
                    dy1 = -0.066f,
                )
                // a 0.107 0.107 0 0 0 -0.044 0.016
                arcToRelative(
                    a = 0.107f,
                    b = 0.107f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.044f,
                    dy1 = 0.016f,
                )
                // l -1.287 0.815
                lineToRelative(dx = -1.287f, dy = 0.815f)
                // a 0.108 0.108 0 0 0 0.026 0.195
                arcToRelative(
                    a = 0.108f,
                    b = 0.108f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.026f,
                    dy1 = 0.195f,
                )
                // l 0.716 0.225
                lineToRelative(dx = 0.716f, dy = 0.225f)
                // a 1.97 1.97 0 0 1 -0.335 0.466
                arcToRelative(
                    a = 1.97f,
                    b = 1.97f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.335f,
                    dy1 = 0.466f,
                )
                // a 1.958 1.958 0 0 1 -2.157 0.423
                arcToRelative(
                    a = 1.958f,
                    b = 1.958f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -2.157f,
                    dy1 = 0.423f,
                )
                // a 1.966 1.966 0 0 1 -1.047 -1.05
                arcToRelative(
                    a = 1.966f,
                    b = 1.966f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.047f,
                    dy1 = -1.05f,
                )
                // a 0.263 0.263 0 0 0 -0.484 0.205
                arcToRelative(
                    a = 0.263f,
                    b = 0.263f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.484f,
                    dy1 = 0.205f,
                )
                // c 0.125 0.297 0.305 0.565 0.534 0.795
                curveToRelative(
                    dx1 = 0.125f,
                    dy1 = 0.297f,
                    dx2 = 0.305f,
                    dy2 = 0.565f,
                    dx3 = 0.534f,
                    dy3 = 0.795f,
                )
                // a 2.485 2.485 0 0 0 3.526 0
                arcToRelative(
                    a = 2.485f,
                    b = 2.485f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 3.526f,
                    dy1 = 0.0f,
                )
                // a 2.52 2.52 0 0 0 0.472 -0.68
                arcToRelative(
                    a = 2.52f,
                    b = 2.52f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.472f,
                    dy1 = -0.68f,
                )
                // l 0.63 0.198
                lineToRelative(dx = 0.63f, dy = 0.198f)
                // a 0.108 0.108 0 0 0 0.132 -0.144z
                arcToRelative(
                    a = 0.108f,
                    b = 0.108f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.132f,
                    dy1 = -0.144f,
                )
                close()
            }
        }.build().also { _ic2374 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2374: ImageVector? = null
