package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2388: ImageVector
    get() {
        val current = _ic2388
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2388",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M2.5 0 a.5 .5 0 0 0 -.5 .5 v15 a.5 .5 0 0 0 1 0 V9 l11 -4 L3 1 V.5 a.5 .5 0 0 0 -.5 -.5Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 2.5 0
                moveTo(x = 2.5f, y = 0.0f)
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
                // v 15
                verticalLineToRelative(dy = 15.0f)
                // a 0.5 0.5 0 0 0 1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                // V 9
                verticalLineTo(y = 9.0f)
                // l 11 -4
                lineToRelative(dx = 11.0f, dy = -4.0f)
                // L 3 1
                lineTo(x = 3.0f, y = 1.0f)
                // V 0.5
                verticalLineTo(y = 0.5f)
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
            // M7.742 11.629 v.001 l-.007 .01 a3.608 3.608 0 0 1 -.137 .203 5.394 5.394 0 0 1 -.392 .49 2.913 2.913 0 0 1 -.513 .459 c-.178 .12 -.324 .169 -.436 .169 -.111 0 -.258 -.05 -.436 -.17 a2.913 2.913 0 0 1 -.513 -.459 5.394 5.394 0 0 1 -.529 -.692 l-.006 -.01 v-.001 l-.387 .293 c-.387 .293 -.386 .294 -.386 .294 l.001 .001 .003 .005 .01 .016 a4.828 4.828 0 0 0 .169 .25 c.112 .157 .273 .368 .466 .582 a3.9 3.9 0 0 0 .689 .612 c.258 .173 .574 .318 .92 .318 .344 0 .66 -.145 .918 -.318 .262 -.175 .497 -.4 .689 -.612 .097 -.107 .185 -.213 .263 -.312 .079 .1 .167 .205 .264 .312 .192 .212 .427 .437 .688 .612 .258 .173 .574 .318 .92 .318 .345 0 .661 -.145 .92 -.318 .26 -.175 .495 -.4 .688 -.612 a6.08 6.08 0 0 0 .263 -.312 c.078 .1 .167 .205 .264 .312 .192 .212 .427 .437 .688 .612 .258 .173 .574 .318 .92 .318 .345 0 .66 -.145 .919 -.318 .261 -.175 .496 -.4 .688 -.612 a6.429 6.429 0 0 0 .635 -.832 l.01 -.016 .004 -.005 .001 -.002 -.386 -.293 -.387 -.293 v.001 l-.007 .01 a5.38 5.38 0 0 1 -.53 .692 2.912 2.912 0 0 1 -.511 .46 c-.179 .12 -.325 .169 -.437 .169 -.111 0 -.257 -.05 -.436 -.17 a2.915 2.915 0 0 1 -.512 -.459 5.4 5.4 0 0 1 -.53 -.692 l-.006 -.01 v-.002 L11.87 11 l-.386 .629 -.001 .001 -.006 .01 -.028 .043 a5.397 5.397 0 0 1 -.502 .65 2.915 2.915 0 0 1 -.512 .459 c-.178 .12 -.325 .169 -.436 .169 -.112 0 -.258 -.05 -.436 -.17 a2.913 2.913 0 0 1 -.513 -.459 5.394 5.394 0 0 1 -.53 -.692 l-.005 -.01 -.001 -.002 L8.128 11 l-.386 .629Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 7.742 11.629
                moveTo(x = 7.742f, y = 11.629f)
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l -0.007 0.01
                lineToRelative(dx = -0.007f, dy = 0.01f)
                // a 3.608 3.608 0 0 1 -0.137 0.203
                arcToRelative(
                    a = 3.608f,
                    b = 3.608f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.137f,
                    dy1 = 0.203f,
                )
                // a 5.394 5.394 0 0 1 -0.392 0.49
                arcToRelative(
                    a = 5.394f,
                    b = 5.394f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.392f,
                    dy1 = 0.49f,
                )
                // a 2.913 2.913 0 0 1 -0.513 0.459
                arcToRelative(
                    a = 2.913f,
                    b = 2.913f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.513f,
                    dy1 = 0.459f,
                )
                // c -0.178 0.12 -0.324 0.169 -0.436 0.169
                curveToRelative(
                    dx1 = -0.178f,
                    dy1 = 0.12f,
                    dx2 = -0.324f,
                    dy2 = 0.169f,
                    dx3 = -0.436f,
                    dy3 = 0.169f,
                )
                // c -0.111 0 -0.258 -0.05 -0.436 -0.17
                curveToRelative(
                    dx1 = -0.111f,
                    dy1 = 0.0f,
                    dx2 = -0.258f,
                    dy2 = -0.05f,
                    dx3 = -0.436f,
                    dy3 = -0.17f,
                )
                // a 2.913 2.913 0 0 1 -0.513 -0.459
                arcToRelative(
                    a = 2.913f,
                    b = 2.913f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.513f,
                    dy1 = -0.459f,
                )
                // a 5.394 5.394 0 0 1 -0.529 -0.692
                arcToRelative(
                    a = 5.394f,
                    b = 5.394f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.529f,
                    dy1 = -0.692f,
                )
                // l -0.006 -0.01
                lineToRelative(dx = -0.006f, dy = -0.01f)
                // v -0.001
                verticalLineToRelative(dy = -0.001f)
                // l -0.387 0.293
                lineToRelative(dx = -0.387f, dy = 0.293f)
                // c -0.387 0.293 -0.386 0.294 -0.386 0.294
                curveToRelative(
                    dx1 = -0.387f,
                    dy1 = 0.293f,
                    dx2 = -0.386f,
                    dy2 = 0.294f,
                    dx3 = -0.386f,
                    dy3 = 0.294f,
                )
                // l 0.001 0.001
                lineToRelative(dx = 0.001f, dy = 0.001f)
                // l 0.003 0.005
                lineToRelative(dx = 0.003f, dy = 0.005f)
                // l 0.01 0.016
                lineToRelative(dx = 0.01f, dy = 0.016f)
                // a 4.828 4.828 0 0 0 0.169 0.25
                arcToRelative(
                    a = 4.828f,
                    b = 4.828f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.169f,
                    dy1 = 0.25f,
                )
                // c 0.112 0.157 0.273 0.368 0.466 0.582
                curveToRelative(
                    dx1 = 0.112f,
                    dy1 = 0.157f,
                    dx2 = 0.273f,
                    dy2 = 0.368f,
                    dx3 = 0.466f,
                    dy3 = 0.582f,
                )
                // a 3.9 3.9 0 0 0 0.689 0.612
                arcToRelative(
                    a = 3.9f,
                    b = 3.9f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.689f,
                    dy1 = 0.612f,
                )
                // c 0.258 0.173 0.574 0.318 0.92 0.318
                curveToRelative(
                    dx1 = 0.258f,
                    dy1 = 0.173f,
                    dx2 = 0.574f,
                    dy2 = 0.318f,
                    dx3 = 0.92f,
                    dy3 = 0.318f,
                )
                // c 0.344 0 0.66 -0.145 0.918 -0.318
                curveToRelative(
                    dx1 = 0.344f,
                    dy1 = 0.0f,
                    dx2 = 0.66f,
                    dy2 = -0.145f,
                    dx3 = 0.918f,
                    dy3 = -0.318f,
                )
                // c 0.262 -0.175 0.497 -0.4 0.689 -0.612
                curveToRelative(
                    dx1 = 0.262f,
                    dy1 = -0.175f,
                    dx2 = 0.497f,
                    dy2 = -0.4f,
                    dx3 = 0.689f,
                    dy3 = -0.612f,
                )
                // c 0.097 -0.107 0.185 -0.213 0.263 -0.312
                curveToRelative(
                    dx1 = 0.097f,
                    dy1 = -0.107f,
                    dx2 = 0.185f,
                    dy2 = -0.213f,
                    dx3 = 0.263f,
                    dy3 = -0.312f,
                )
                // c 0.079 0.1 0.167 0.205 0.264 0.312
                curveToRelative(
                    dx1 = 0.079f,
                    dy1 = 0.1f,
                    dx2 = 0.167f,
                    dy2 = 0.205f,
                    dx3 = 0.264f,
                    dy3 = 0.312f,
                )
                // c 0.192 0.212 0.427 0.437 0.688 0.612
                curveToRelative(
                    dx1 = 0.192f,
                    dy1 = 0.212f,
                    dx2 = 0.427f,
                    dy2 = 0.437f,
                    dx3 = 0.688f,
                    dy3 = 0.612f,
                )
                // c 0.258 0.173 0.574 0.318 0.92 0.318
                curveToRelative(
                    dx1 = 0.258f,
                    dy1 = 0.173f,
                    dx2 = 0.574f,
                    dy2 = 0.318f,
                    dx3 = 0.92f,
                    dy3 = 0.318f,
                )
                // c 0.345 0 0.661 -0.145 0.92 -0.318
                curveToRelative(
                    dx1 = 0.345f,
                    dy1 = 0.0f,
                    dx2 = 0.661f,
                    dy2 = -0.145f,
                    dx3 = 0.92f,
                    dy3 = -0.318f,
                )
                // c 0.26 -0.175 0.495 -0.4 0.688 -0.612
                curveToRelative(
                    dx1 = 0.26f,
                    dy1 = -0.175f,
                    dx2 = 0.495f,
                    dy2 = -0.4f,
                    dx3 = 0.688f,
                    dy3 = -0.612f,
                )
                // a 6.08 6.08 0 0 0 0.263 -0.312
                arcToRelative(
                    a = 6.08f,
                    b = 6.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.263f,
                    dy1 = -0.312f,
                )
                // c 0.078 0.1 0.167 0.205 0.264 0.312
                curveToRelative(
                    dx1 = 0.078f,
                    dy1 = 0.1f,
                    dx2 = 0.167f,
                    dy2 = 0.205f,
                    dx3 = 0.264f,
                    dy3 = 0.312f,
                )
                // c 0.192 0.212 0.427 0.437 0.688 0.612
                curveToRelative(
                    dx1 = 0.192f,
                    dy1 = 0.212f,
                    dx2 = 0.427f,
                    dy2 = 0.437f,
                    dx3 = 0.688f,
                    dy3 = 0.612f,
                )
                // c 0.258 0.173 0.574 0.318 0.92 0.318
                curveToRelative(
                    dx1 = 0.258f,
                    dy1 = 0.173f,
                    dx2 = 0.574f,
                    dy2 = 0.318f,
                    dx3 = 0.92f,
                    dy3 = 0.318f,
                )
                // c 0.345 0 0.66 -0.145 0.919 -0.318
                curveToRelative(
                    dx1 = 0.345f,
                    dy1 = 0.0f,
                    dx2 = 0.66f,
                    dy2 = -0.145f,
                    dx3 = 0.919f,
                    dy3 = -0.318f,
                )
                // c 0.261 -0.175 0.496 -0.4 0.688 -0.612
                curveToRelative(
                    dx1 = 0.261f,
                    dy1 = -0.175f,
                    dx2 = 0.496f,
                    dy2 = -0.4f,
                    dx3 = 0.688f,
                    dy3 = -0.612f,
                )
                // a 6.429 6.429 0 0 0 0.635 -0.832
                arcToRelative(
                    a = 6.429f,
                    b = 6.429f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.635f,
                    dy1 = -0.832f,
                )
                // l 0.01 -0.016
                lineToRelative(dx = 0.01f, dy = -0.016f)
                // l 0.004 -0.005
                lineToRelative(dx = 0.004f, dy = -0.005f)
                // l 0.001 -0.002
                lineToRelative(dx = 0.001f, dy = -0.002f)
                // l -0.386 -0.293
                lineToRelative(dx = -0.386f, dy = -0.293f)
                // l -0.387 -0.293
                lineToRelative(dx = -0.387f, dy = -0.293f)
                // v 0.001
                verticalLineToRelative(dy = 0.001f)
                // l -0.007 0.01
                lineToRelative(dx = -0.007f, dy = 0.01f)
                // a 5.38 5.38 0 0 1 -0.53 0.692
                arcToRelative(
                    a = 5.38f,
                    b = 5.38f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.53f,
                    dy1 = 0.692f,
                )
                // a 2.912 2.912 0 0 1 -0.511 0.46
                arcToRelative(
                    a = 2.912f,
                    b = 2.912f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.511f,
                    dy1 = 0.46f,
                )
                // c -0.179 0.12 -0.325 0.169 -0.437 0.169
                curveToRelative(
                    dx1 = -0.179f,
                    dy1 = 0.12f,
                    dx2 = -0.325f,
                    dy2 = 0.169f,
                    dx3 = -0.437f,
                    dy3 = 0.169f,
                )
                // c -0.111 0 -0.257 -0.05 -0.436 -0.17
                curveToRelative(
                    dx1 = -0.111f,
                    dy1 = 0.0f,
                    dx2 = -0.257f,
                    dy2 = -0.05f,
                    dx3 = -0.436f,
                    dy3 = -0.17f,
                )
                // a 2.915 2.915 0 0 1 -0.512 -0.459
                arcToRelative(
                    a = 2.915f,
                    b = 2.915f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.512f,
                    dy1 = -0.459f,
                )
                // a 5.4 5.4 0 0 1 -0.53 -0.692
                arcToRelative(
                    a = 5.4f,
                    b = 5.4f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.53f,
                    dy1 = -0.692f,
                )
                // l -0.006 -0.01
                lineToRelative(dx = -0.006f, dy = -0.01f)
                // v -0.002
                verticalLineToRelative(dy = -0.002f)
                // L 11.87 11
                lineTo(x = 11.87f, y = 11.0f)
                // l -0.386 0.629
                lineToRelative(dx = -0.386f, dy = 0.629f)
                // l -0.001 0.001
                lineToRelative(dx = -0.001f, dy = 0.001f)
                // l -0.006 0.01
                lineToRelative(dx = -0.006f, dy = 0.01f)
                // l -0.028 0.043
                lineToRelative(dx = -0.028f, dy = 0.043f)
                // a 5.397 5.397 0 0 1 -0.502 0.65
                arcToRelative(
                    a = 5.397f,
                    b = 5.397f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.502f,
                    dy1 = 0.65f,
                )
                // a 2.915 2.915 0 0 1 -0.512 0.459
                arcToRelative(
                    a = 2.915f,
                    b = 2.915f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.512f,
                    dy1 = 0.459f,
                )
                // c -0.178 0.12 -0.325 0.169 -0.436 0.169
                curveToRelative(
                    dx1 = -0.178f,
                    dy1 = 0.12f,
                    dx2 = -0.325f,
                    dy2 = 0.169f,
                    dx3 = -0.436f,
                    dy3 = 0.169f,
                )
                // c -0.112 0 -0.258 -0.05 -0.436 -0.17
                curveToRelative(
                    dx1 = -0.112f,
                    dy1 = 0.0f,
                    dx2 = -0.258f,
                    dy2 = -0.05f,
                    dx3 = -0.436f,
                    dy3 = -0.17f,
                )
                // a 2.913 2.913 0 0 1 -0.513 -0.459
                arcToRelative(
                    a = 2.913f,
                    b = 2.913f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.513f,
                    dy1 = -0.459f,
                )
                // a 5.394 5.394 0 0 1 -0.53 -0.692
                arcToRelative(
                    a = 5.394f,
                    b = 5.394f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.53f,
                    dy1 = -0.692f,
                )
                // l -0.005 -0.01
                lineToRelative(dx = -0.005f, dy = -0.01f)
                // l -0.001 -0.002
                lineToRelative(dx = -0.001f, dy = -0.002f)
                // L 8.128 11
                lineTo(x = 8.128f, y = 11.0f)
                // l -0.386 0.629z
                lineToRelative(dx = -0.386f, dy = 0.629f)
                close()
            }
        }.build().also { _ic2388 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2388: ImageVector? = null
