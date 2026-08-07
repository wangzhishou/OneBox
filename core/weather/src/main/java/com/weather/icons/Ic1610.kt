package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic1610: ImageVector
    get() {
        val current = _ic1610
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic1610",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M9.035 4.136 a.25 .25 0 0 1 .177 .306 l-.167 .625 .396 -.229 a.25 .25 0 0 1 .25 .433 l-.83 .479 -.231 .866 a.257 .257 0 0 1 -.006 .02 c.335 .152 .594 .42 .74 .74 a.257 .257 0 0 1 .02 -.006 l.866 -.232 .479 -.829 a.25 .25 0 0 1 .433 .25 l-.229 .396 .625 -.167 a.25 .25 0 1 1 .13 .483 l-.625 .167 .396 .229 a.25 .25 0 0 1 -.25 .433 l-.83 -.479 -.865 .232 a.252 .252 0 0 1 -.02 .005 1.497 1.497 0 0 1 -.271 1.012 l.015 .014 .634 .634 h.957 a.25 .25 0 1 1 0 .5 h-.458 l.458 .457 a.25 .25 0 1 1 -.354 .353 l-.457 -.457 v.457 a.25 .25 0 0 1 -.5 0 v-.957 l-.634 -.634 a.25 .25 0 0 1 -.014 -.015 1.499 1.499 0 0 1 -1.012 .271 .25 .25 0 0 1 -.005 .02 l-.232 .867 .479 .828 a.25 .25 0 0 1 -.433 .25 l-.229 -.396 -.167 .625 a.25 .25 0 1 1 -.483 -.13 l.167 -.624 -.395 .229 a.25 .25 0 0 1 -.25 -.433 l.828 -.479 .232 -.866 a.257 .257 0 0 1 .007 -.02 1.499 1.499 0 0 1 -.741 -.74 .257 .257 0 0 1 -.02 .006 l-.866 .232 -.478 .829 a.25 .25 0 0 1 -.433 -.25 l.228 -.396 -.624 .167 a.25 .25 0 1 1 -.13 -.483 l.625 -.167 -.396 -.229 a.25 .25 0 0 1 .25 -.433 l.829 .479 .866 -.232 a.25 .25 0 0 1 .02 -.005 1.497 1.497 0 0 1 .27 -1.012 .253 .253 0 0 1 -.014 -.014 l-.634 -.634 h-.957 a.25 .25 0 0 1 0 -.5 h.457 l-.457 -.457 a.25 .25 0 1 1 .353 -.353 l.457 .457 v-.457 a.25 .25 0 1 1 .5 0 v.957 l.634 .634 .014 .015 a1.499 1.499 0 0 1 1.012 -.271 .25 .25 0 0 1 .005 -.02 l.232 -.866 -.479 -.83 a.25 .25 0 0 1 .433 -.25 l.229 .396 .167 -.624 a.25 .25 0 0 1 .306 -.177Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 9.035 4.136
                moveTo(x = 9.035f, y = 4.136f)
                // a 0.25 0.25 0 0 1 0.177 0.306
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.177f,
                    dy1 = 0.306f,
                )
                // l -0.167 0.625
                lineToRelative(dx = -0.167f, dy = 0.625f)
                // l 0.396 -0.229
                lineToRelative(dx = 0.396f, dy = -0.229f)
                // a 0.25 0.25 0 0 1 0.25 0.433
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = 0.433f,
                )
                // l -0.83 0.479
                lineToRelative(dx = -0.83f, dy = 0.479f)
                // l -0.231 0.866
                lineToRelative(dx = -0.231f, dy = 0.866f)
                // a 0.257 0.257 0 0 1 -0.006 0.02
                arcToRelative(
                    a = 0.257f,
                    b = 0.257f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.006f,
                    dy1 = 0.02f,
                )
                // c 0.335 0.152 0.594 0.42 0.74 0.74
                curveToRelative(
                    dx1 = 0.335f,
                    dy1 = 0.152f,
                    dx2 = 0.594f,
                    dy2 = 0.42f,
                    dx3 = 0.74f,
                    dy3 = 0.74f,
                )
                // a 0.257 0.257 0 0 1 0.02 -0.006
                arcToRelative(
                    a = 0.257f,
                    b = 0.257f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.02f,
                    dy1 = -0.006f,
                )
                // l 0.866 -0.232
                lineToRelative(dx = 0.866f, dy = -0.232f)
                // l 0.479 -0.829
                lineToRelative(dx = 0.479f, dy = -0.829f)
                // a 0.25 0.25 0 0 1 0.433 0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = 0.25f,
                )
                // l -0.229 0.396
                lineToRelative(dx = -0.229f, dy = 0.396f)
                // l 0.625 -0.167
                lineToRelative(dx = 0.625f, dy = -0.167f)
                // a 0.25 0.25 0 1 1 0.13 0.483
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.13f,
                    dy1 = 0.483f,
                )
                // l -0.625 0.167
                lineToRelative(dx = -0.625f, dy = 0.167f)
                // l 0.396 0.229
                lineToRelative(dx = 0.396f, dy = 0.229f)
                // a 0.25 0.25 0 0 1 -0.25 0.433
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = 0.433f,
                )
                // l -0.83 -0.479
                lineToRelative(dx = -0.83f, dy = -0.479f)
                // l -0.865 0.232
                lineToRelative(dx = -0.865f, dy = 0.232f)
                // a 0.252 0.252 0 0 1 -0.02 0.005
                arcToRelative(
                    a = 0.252f,
                    b = 0.252f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.02f,
                    dy1 = 0.005f,
                )
                // a 1.497 1.497 0 0 1 -0.271 1.012
                arcToRelative(
                    a = 1.497f,
                    b = 1.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.271f,
                    dy1 = 1.012f,
                )
                // l 0.015 0.014
                lineToRelative(dx = 0.015f, dy = 0.014f)
                // l 0.634 0.634
                lineToRelative(dx = 0.634f, dy = 0.634f)
                // h 0.957
                horizontalLineToRelative(dx = 0.957f)
                // a 0.25 0.25 0 1 1 0 0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = 0.5f,
                )
                // h -0.458
                horizontalLineToRelative(dx = -0.458f)
                // l 0.458 0.457
                lineToRelative(dx = 0.458f, dy = 0.457f)
                // a 0.25 0.25 0 1 1 -0.354 0.353
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.354f,
                    dy1 = 0.353f,
                )
                // l -0.457 -0.457
                lineToRelative(dx = -0.457f, dy = -0.457f)
                // v 0.457
                verticalLineToRelative(dy = 0.457f)
                // a 0.25 0.25 0 0 1 -0.5 0
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.5f,
                    dy1 = 0.0f,
                )
                // v -0.957
                verticalLineToRelative(dy = -0.957f)
                // l -0.634 -0.634
                lineToRelative(dx = -0.634f, dy = -0.634f)
                // a 0.25 0.25 0 0 1 -0.014 -0.015
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.014f,
                    dy1 = -0.015f,
                )
                // a 1.499 1.499 0 0 1 -1.012 0.271
                arcToRelative(
                    a = 1.499f,
                    b = 1.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -1.012f,
                    dy1 = 0.271f,
                )
                // a 0.25 0.25 0 0 1 -0.005 0.02
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.005f,
                    dy1 = 0.02f,
                )
                // l -0.232 0.867
                lineToRelative(dx = -0.232f, dy = 0.867f)
                // l 0.479 0.828
                lineToRelative(dx = 0.479f, dy = 0.828f)
                // a 0.25 0.25 0 0 1 -0.433 0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.433f,
                    dy1 = 0.25f,
                )
                // l -0.229 -0.396
                lineToRelative(dx = -0.229f, dy = -0.396f)
                // l -0.167 0.625
                lineToRelative(dx = -0.167f, dy = 0.625f)
                // a 0.25 0.25 0 1 1 -0.483 -0.13
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.483f,
                    dy1 = -0.13f,
                )
                // l 0.167 -0.624
                lineToRelative(dx = 0.167f, dy = -0.624f)
                // l -0.395 0.229
                lineToRelative(dx = -0.395f, dy = 0.229f)
                // a 0.25 0.25 0 0 1 -0.25 -0.433
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.25f,
                    dy1 = -0.433f,
                )
                // l 0.828 -0.479
                lineToRelative(dx = 0.828f, dy = -0.479f)
                // l 0.232 -0.866
                lineToRelative(dx = 0.232f, dy = -0.866f)
                // a 0.257 0.257 0 0 1 0.007 -0.02
                arcToRelative(
                    a = 0.257f,
                    b = 0.257f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.007f,
                    dy1 = -0.02f,
                )
                // a 1.499 1.499 0 0 1 -0.741 -0.74
                arcToRelative(
                    a = 1.499f,
                    b = 1.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.741f,
                    dy1 = -0.74f,
                )
                // a 0.257 0.257 0 0 1 -0.02 0.006
                arcToRelative(
                    a = 0.257f,
                    b = 0.257f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.02f,
                    dy1 = 0.006f,
                )
                // l -0.866 0.232
                lineToRelative(dx = -0.866f, dy = 0.232f)
                // l -0.478 0.829
                lineToRelative(dx = -0.478f, dy = 0.829f)
                // a 0.25 0.25 0 0 1 -0.433 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.433f,
                    dy1 = -0.25f,
                )
                // l 0.228 -0.396
                lineToRelative(dx = 0.228f, dy = -0.396f)
                // l -0.624 0.167
                lineToRelative(dx = -0.624f, dy = 0.167f)
                // a 0.25 0.25 0 1 1 -0.13 -0.483
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.13f,
                    dy1 = -0.483f,
                )
                // l 0.625 -0.167
                lineToRelative(dx = 0.625f, dy = -0.167f)
                // l -0.396 -0.229
                lineToRelative(dx = -0.396f, dy = -0.229f)
                // a 0.25 0.25 0 0 1 0.25 -0.433
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.25f,
                    dy1 = -0.433f,
                )
                // l 0.829 0.479
                lineToRelative(dx = 0.829f, dy = 0.479f)
                // l 0.866 -0.232
                lineToRelative(dx = 0.866f, dy = -0.232f)
                // a 0.25 0.25 0 0 1 0.02 -0.005
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.02f,
                    dy1 = -0.005f,
                )
                // a 1.497 1.497 0 0 1 0.27 -1.012
                arcToRelative(
                    a = 1.497f,
                    b = 1.497f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.27f,
                    dy1 = -1.012f,
                )
                // a 0.253 0.253 0 0 1 -0.014 -0.014
                arcToRelative(
                    a = 0.253f,
                    b = 0.253f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.014f,
                    dy1 = -0.014f,
                )
                // l -0.634 -0.634
                lineToRelative(dx = -0.634f, dy = -0.634f)
                // h -0.957
                horizontalLineToRelative(dx = -0.957f)
                // a 0.25 0.25 0 0 1 0 -0.5
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.0f,
                    dy1 = -0.5f,
                )
                // h 0.457
                horizontalLineToRelative(dx = 0.457f)
                // l -0.457 -0.457
                lineToRelative(dx = -0.457f, dy = -0.457f)
                // a 0.25 0.25 0 1 1 0.353 -0.353
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.353f,
                    dy1 = -0.353f,
                )
                // l 0.457 0.457
                lineToRelative(dx = 0.457f, dy = 0.457f)
                // v -0.457
                verticalLineToRelative(dy = -0.457f)
                // a 0.25 0.25 0 1 1 0.5 0
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.5f,
                    dy1 = 0.0f,
                )
                // v 0.957
                verticalLineToRelative(dy = 0.957f)
                // l 0.634 0.634
                lineToRelative(dx = 0.634f, dy = 0.634f)
                // l 0.014 0.015
                lineToRelative(dx = 0.014f, dy = 0.015f)
                // a 1.499 1.499 0 0 1 1.012 -0.271
                arcToRelative(
                    a = 1.499f,
                    b = 1.499f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.012f,
                    dy1 = -0.271f,
                )
                // a 0.25 0.25 0 0 1 0.005 -0.02
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.005f,
                    dy1 = -0.02f,
                )
                // l 0.232 -0.866
                lineToRelative(dx = 0.232f, dy = -0.866f)
                // l -0.479 -0.83
                lineToRelative(dx = -0.479f, dy = -0.83f)
                // a 0.25 0.25 0 0 1 0.433 -0.25
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.433f,
                    dy1 = -0.25f,
                )
                // l 0.229 0.396
                lineToRelative(dx = 0.229f, dy = 0.396f)
                // l 0.167 -0.624
                lineToRelative(dx = 0.167f, dy = -0.624f)
                // a 0.25 0.25 0 0 1 0.306 -0.177z
                arcToRelative(
                    a = 0.25f,
                    b = 0.25f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.306f,
                    dy1 = -0.177f,
                )
                close()
            }
            // m10.384 .455 5.14 5.154 a.705 .705 0 0 1 .182 .68 l-1.889 7.047 a.704 .704 0 0 1 -.497 .497 l-7.028 1.893 a.688 .688 0 0 1 -.677 -.181 l-5.14 -5.154 a.705 .705 0 0 1 -.18 -.679 l1.888 -7.047 a.705 .705 0 0 1 .496 -.498 L9.707 .274 a.693 .693 0 0 1 .677 .181Z M6.322 14.263 l6.245 -1.683 1.678 -6.263 -4.567 -4.58 -6.245 1.684 -1.678 6.262 4.567 4.58Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 10.384 0.455
                moveTo(x = 10.384f, y = 0.455f)
                // l 5.14 5.154
                lineToRelative(dx = 5.14f, dy = 5.154f)
                // a 0.705 0.705 0 0 1 0.182 0.68
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.182f,
                    dy1 = 0.68f,
                )
                // l -1.889 7.047
                lineToRelative(dx = -1.889f, dy = 7.047f)
                // a 0.704 0.704 0 0 1 -0.497 0.497
                arcToRelative(
                    a = 0.704f,
                    b = 0.704f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.497f,
                    dy1 = 0.497f,
                )
                // l -7.028 1.893
                lineToRelative(dx = -7.028f, dy = 1.893f)
                // a 0.688 0.688 0 0 1 -0.677 -0.181
                arcToRelative(
                    a = 0.688f,
                    b = 0.688f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.677f,
                    dy1 = -0.181f,
                )
                // l -5.14 -5.154
                lineToRelative(dx = -5.14f, dy = -5.154f)
                // a 0.705 0.705 0 0 1 -0.18 -0.679
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.18f,
                    dy1 = -0.679f,
                )
                // l 1.888 -7.047
                lineToRelative(dx = 1.888f, dy = -7.047f)
                // a 0.705 0.705 0 0 1 0.496 -0.498
                arcToRelative(
                    a = 0.705f,
                    b = 0.705f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.496f,
                    dy1 = -0.498f,
                )
                // L 9.707 0.274
                lineTo(x = 9.707f, y = 0.274f)
                // a 0.693 0.693 0 0 1 0.677 0.181z
                arcToRelative(
                    a = 0.693f,
                    b = 0.693f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.677f,
                    dy1 = 0.181f,
                )
                close()
                // M 6.322 14.263
                moveTo(x = 6.322f, y = 14.263f)
                // l 6.245 -1.683
                lineToRelative(dx = 6.245f, dy = -1.683f)
                // l 1.678 -6.263
                lineToRelative(dx = 1.678f, dy = -6.263f)
                // l -4.567 -4.58
                lineToRelative(dx = -4.567f, dy = -4.58f)
                // l -6.245 1.684
                lineToRelative(dx = -6.245f, dy = 1.684f)
                // l -1.678 6.262
                lineToRelative(dx = -1.678f, dy = 6.262f)
                // l 4.567 4.58z
                lineToRelative(dx = 4.567f, dy = 4.58f)
                close()
            }
        }.build().also { _ic1610 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic1610: ImageVector? = null
