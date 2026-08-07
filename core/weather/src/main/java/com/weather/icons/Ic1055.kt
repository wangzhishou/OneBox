package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1055: ImageVector
    get() {
        val current = _ic1055
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1055",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M8.924 0 C6.038 -.05 3.844 1.95 3.64 4.383 c-.225 2.684 1.618 3.8 1.865 4.02 -1.464 -.023 -3.206 -1.65 -3.499 -3.175 a.08 .08 0 0 0 -.076 -.066 .079 .079 0 0 0 -.08 .088 c.31 2.707 2.616 4.762 5.215 4.75 2.769 -.012 5.007 -1.65 5.293 -4.336 .269 -2.531 -1.62 -3.846 -1.862 -4.065 1.554 .015 3.197 1.652 3.487 3.174 a.08 .08 0 0 0 .076 .066 .079 .079 0 0 0 .076 -.055 c.004 -.01 .005 -.022 .004 -.033 -.312 -2.704 -2.616 -4.705 -5.215 -4.75Z m-.93 6.155 a1.154 1.154 0 1 1 0 -2.308 1.154 1.154 0 0 1 0 2.308Z M.777 11.644 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.033 .517 a1.5 1.5 0 0 0 1.503 -.094 l1.017 -.678 a.5 .5 0 1 0 -.554 -.832 l-1.017 .678 a.5 .5 0 0 1 -.501 .031 l-1.034 -.517 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.679Z m0 2.5 a.5 .5 0 1 0 -.554 .832 l1.017 .678 a1.5 1.5 0 0 0 1.503 .094 l1.033 -.517 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.105 .553 a1.5 1.5 0 0 0 1.342 0 l1.105 -.553 a.5 .5 0 0 1 .448 0 l1.033 .517 a1.5 1.5 0 0 0 1.503 -.094 l1.017 -.678 a.5 .5 0 1 0 -.554 -.832 l-1.017 .678 a.5 .5 0 0 1 -.501 .031 l-1.034 -.517 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.105 .553 a.5 .5 0 0 1 -.448 0 l-1.105 -.553 a1.5 1.5 0 0 0 -1.342 0 l-1.034 .517 a.5 .5 0 0 1 -.5 -.03 l-1.018 -.679Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 8.924 0
                moveTo(x = 8.924f, y = 0.0f)
                // C 6.038 -0.05 3.844 1.95 3.64 4.383
                curveTo(
                    x1 = 6.038f,
                    y1 = -0.05f,
                    x2 = 3.844f,
                    y2 = 1.95f,
                    x3 = 3.64f,
                    y3 = 4.383f,
                )
                // c -0.225 2.684 1.618 3.8 1.865 4.02
                curveToRelative(
                    dx1 = -0.225f,
                    dy1 = 2.684f,
                    dx2 = 1.618f,
                    dy2 = 3.8f,
                    dx3 = 1.865f,
                    dy3 = 4.02f,
                )
                // c -1.464 -0.023 -3.206 -1.65 -3.499 -3.175
                curveToRelative(
                    dx1 = -1.464f,
                    dy1 = -0.023f,
                    dx2 = -3.206f,
                    dy2 = -1.65f,
                    dx3 = -3.499f,
                    dy3 = -3.175f,
                )
                // a 0.08 0.08 0 0 0 -0.076 -0.066
                arcToRelative(
                    a = 0.08f,
                    b = 0.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.076f,
                    dy1 = -0.066f,
                )
                // a 0.079 0.079 0 0 0 -0.08 0.088
                arcToRelative(
                    a = 0.079f,
                    b = 0.079f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.08f,
                    dy1 = 0.088f,
                )
                // c 0.31 2.707 2.616 4.762 5.215 4.75
                curveToRelative(
                    dx1 = 0.31f,
                    dy1 = 2.707f,
                    dx2 = 2.616f,
                    dy2 = 4.762f,
                    dx3 = 5.215f,
                    dy3 = 4.75f,
                )
                // c 2.769 -0.012 5.007 -1.65 5.293 -4.336
                curveToRelative(
                    dx1 = 2.769f,
                    dy1 = -0.012f,
                    dx2 = 5.007f,
                    dy2 = -1.65f,
                    dx3 = 5.293f,
                    dy3 = -4.336f,
                )
                // c 0.269 -2.531 -1.62 -3.846 -1.862 -4.065
                curveToRelative(
                    dx1 = 0.269f,
                    dy1 = -2.531f,
                    dx2 = -1.62f,
                    dy2 = -3.846f,
                    dx3 = -1.862f,
                    dy3 = -4.065f,
                )
                // c 1.554 0.015 3.197 1.652 3.487 3.174
                curveToRelative(
                    dx1 = 1.554f,
                    dy1 = 0.015f,
                    dx2 = 3.197f,
                    dy2 = 1.652f,
                    dx3 = 3.487f,
                    dy3 = 3.174f,
                )
                // a 0.08 0.08 0 0 0 0.076 0.066
                arcToRelative(
                    a = 0.08f,
                    b = 0.08f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.076f,
                    dy1 = 0.066f,
                )
                // a 0.079 0.079 0 0 0 0.076 -0.055
                arcToRelative(
                    a = 0.079f,
                    b = 0.079f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.076f,
                    dy1 = -0.055f,
                )
                // c 0.004 -0.01 0.005 -0.022 0.004 -0.033
                curveToRelative(
                    dx1 = 0.004f,
                    dy1 = -0.01f,
                    dx2 = 0.005f,
                    dy2 = -0.022f,
                    dx3 = 0.004f,
                    dy3 = -0.033f,
                )
                // c -0.312 -2.704 -2.616 -4.705 -5.215 -4.75z
                curveToRelative(
                    dx1 = -0.312f,
                    dy1 = -2.704f,
                    dx2 = -2.616f,
                    dy2 = -4.705f,
                    dx3 = -5.215f,
                    dy3 = -4.75f,
                )
                close()
                // m -0.93 6.155
                moveToRelative(dx = -0.93f, dy = 6.155f)
                // a 1.154 1.154 0 1 1 0 -2.308
                arcToRelative(
                    a = 1.154f,
                    b = 1.154f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -2.308f,
                )
                // a 1.154 1.154 0 0 1 0 2.308z
                arcToRelative(
                    a = 1.154f,
                    b = 1.154f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 2.308f,
                )
                close()
                // M 0.777 11.644
                moveTo(x = 0.777f, y = 11.644f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.033 0.517
                lineToRelative(dx = 1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 0 1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = -0.094f,
                )
                // l 1.017 -0.678
                lineToRelative(dx = 1.017f, dy = -0.678f)
                // a 0.5 0.5 0 1 0 -0.554 -0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = -0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 0.5 0.5 0 0 1 -0.501 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.501f,
                    dy1 = 0.031f,
                )
                // l -1.034 -0.517
                lineToRelative(dx = -1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.679z
                lineToRelative(dx = -1.018f, dy = -0.679f)
                close()
                // m 0 2.5
                moveToRelative(dx = 0.0f, dy = 2.5f)
                // a 0.5 0.5 0 1 0 -0.554 0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = 0.832f,
                )
                // l 1.017 0.678
                lineToRelative(dx = 1.017f, dy = 0.678f)
                // a 1.5 1.5 0 0 0 1.503 0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = 0.094f,
                )
                // l 1.033 -0.517
                lineToRelative(dx = 1.033f, dy = -0.517f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.105 0.553
                lineToRelative(dx = 1.105f, dy = 0.553f)
                // a 1.5 1.5 0 0 0 1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.342f,
                    dy1 = 0.0f,
                )
                // l 1.105 -0.553
                lineToRelative(dx = 1.105f, dy = -0.553f)
                // a 0.5 0.5 0 0 1 0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.448f,
                    dy1 = 0.0f,
                )
                // l 1.033 0.517
                lineToRelative(dx = 1.033f, dy = 0.517f)
                // a 1.5 1.5 0 0 0 1.503 -0.094
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 1.503f,
                    dy1 = -0.094f,
                )
                // l 1.017 -0.678
                lineToRelative(dx = 1.017f, dy = -0.678f)
                // a 0.5 0.5 0 1 0 -0.554 -0.832
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.554f,
                    dy1 = -0.832f,
                )
                // l -1.017 0.678
                lineToRelative(dx = -1.017f, dy = 0.678f)
                // a 0.5 0.5 0 0 1 -0.501 0.031
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.501f,
                    dy1 = 0.031f,
                )
                // l -1.034 -0.517
                lineToRelative(dx = -1.034f, dy = -0.517f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.105 0.553
                lineToRelative(dx = -1.105f, dy = 0.553f)
                // a 0.5 0.5 0 0 1 -0.448 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.448f,
                    dy1 = 0.0f,
                )
                // l -1.105 -0.553
                lineToRelative(dx = -1.105f, dy = -0.553f)
                // a 1.5 1.5 0 0 0 -1.342 0
                arcToRelative(
                    a = 1.5f,
                    b = 1.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -1.342f,
                    dy1 = 0.0f,
                )
                // l -1.034 0.517
                lineToRelative(dx = -1.034f, dy = 0.517f)
                // a 0.5 0.5 0 0 1 -0.5 -0.03
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = -0.03f,
                )
                // l -1.018 -0.679z
                lineToRelative(dx = -1.018f, dy = -0.679f)
                close()
            }
        }.build().also { _ic1055 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1055: ImageVector? = null
