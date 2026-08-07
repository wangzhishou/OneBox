package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2085: ImageVector
    get() {
        val current = _ic2085
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2085",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M0 5.375 a.375 .375 0 1 1 .75 0 V7.25 h6.5 V5.375 a.375 .375 0 1 1 .75 0 v2.25 A.375 .375 0 0 1 7.625 8 H.375 A.375 .375 0 0 1 0 7.625 v-2.25Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 0 5.375
                moveTo(x = 0.0f, y = 5.375f)
                // a 0.375 0.375 0 1 1 0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                // V 7.25
                verticalLineTo(y = 7.25f)
                // h 6.5
                horizontalLineToRelative(dx = 6.5f)
                // V 5.375
                verticalLineTo(y = 5.375f)
                // a 0.375 0.375 0 1 1 0.75 0
                arcToRelative(
                    a = 0.375f,
                    b = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.75f,
                    dy1 = 0.0f,
                )
                // v 2.25
                verticalLineToRelative(dy = 2.25f)
                // A 0.375 0.375 0 0 1 7.625 8
                arcTo(
                    horizontalEllipseRadius = 0.375f,
                    verticalEllipseRadius = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 7.625f,
                    y1 = 8.0f,
                )
                // H 0.375
                horizontalLineTo(x = 0.375f)
                // A 0.375 0.375 0 0 1 0 7.625
                arcTo(
                    horizontalEllipseRadius = 0.375f,
                    verticalEllipseRadius = 0.375f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    x1 = 0.0f,
                    y1 = 7.625f,
                )
                // v -2.25z
                verticalLineToRelative(dy = -2.25f)
                close()
            }
            // M4.203 .203 a.203 .203 0 0 0 -.406 0 v.525 L3.534 .466 a.203 .203 0 0 0 -.287 .287 l.55 .55 v.745 c-.295 .05 -.554 .205 -.737 .426 a.203 .203 0 0 0 -.014 -.01 l-.63 -.363 -.202 -.752 a.203 .203 0 0 0 -.392 .106 l.096 .358 -.455 -.262 a.203 .203 0 0 0 -.203 .351 l.455 .263 -.36 .096 a.203 .203 0 1 0 .106 .393 l.751 -.202 .631 .365 .015 .007 a1.216 1.216 0 0 0 0 .852 .21 .21 0 0 0 -.015 .007 l-.63 .365 -.752 -.202 a.203 .203 0 1 0 -.105 .393 l.359 .096 -.455 .263 a.203 .203 0 0 0 .203 .351 l.455 -.262 -.096 .358 a.203 .203 0 0 0 .392 .106 l.201 -.752 .631 -.364 a.205 .205 0 0 0 .014 -.009 c.183 .221 .442 .377 .738 .426 l-.001 .017 v.728 l-.55 .55 a.203 .203 0 0 0 .287 .287 l.263 -.262 v.525 a.203 .203 0 0 0 .406 0 v-.525 l.263 .262 a.203 .203 0 0 0 .287 -.287 l-.55 -.55 v-.745 c.295 -.05 .554 -.205 .737 -.426 l.014 .01 .63 .363 .202 .752 a.203 .203 0 0 0 .392 -.106 l-.096 -.358 .455 .262 a.203 .203 0 0 0 .203 -.351 l-.455 -.263 .36 -.096 a.203 .203 0 0 0 -.106 -.393 l-.751 .202 -.63 -.365 a.205 .205 0 0 0 -.016 -.007 1.216 1.216 0 0 0 0 -.852 .202 .202 0 0 0 .015 -.007 l.63 -.365 .752 .202 a.203 .203 0 1 0 .105 -.393 l-.359 -.096 .455 -.263 a.203 .203 0 1 0 -.203 -.351 l-.455 .262 .096 -.358 a.203 .203 0 1 0 -.392 -.106 l-.201 .752 -.631 .364 a.204 .204 0 0 0 -.014 .009 1.218 1.218 0 0 0 -.738 -.426 l.001 -.017 v-.728 l.55 -.55 a.203 .203 0 0 0 -.287 -.287 l-.263 .262 V.203Z M16 4 6.5 9 l3.5 3 -6 4 h9 l2.5 -5 L11 9 l5 -3 V4Z m-1.45 1.878 -.1 -.173 .5 -.291 .1 .172 -.5 .292Z m-2 1.167 -.1 -.173 1 -.583 .1 .172 -1 .584Z m-2 1.166 -.1 -.172 1 -.584 .1 .173 -1 .583Z m-1 .584 -.376 .22 .265 .22 -.128 .154 -.485 -.403 .624 -.364 .1 .173Z m1.389 1.69 -.128 .154 -.75 -.625 .128 -.153 .75 .625Z m1.196 .998 -.366 .513 -.163 -.117 .259 -.362 -.304 -.253 .128 -.153 .446 .372Z m-1.616 2.263 -.163 -.117 .625 -.875 .163 .117 -.625 .875Z m-.938 1.312 -.162 -.116 .312 -.438 .163 .117 -.313 .437Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.203 0.203
                moveTo(x = 4.203f, y = 0.203f)
                // a 0.203 0.203 0 0 0 -0.406 0
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.406f,
                    dy1 = 0.0f,
                )
                // v 0.525
                verticalLineToRelative(dy = 0.525f)
                // L 3.534 0.466
                lineTo(x = 3.534f, y = 0.466f)
                // a 0.203 0.203 0 0 0 -0.287 0.287
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.287f,
                    dy1 = 0.287f,
                )
                // l 0.55 0.55
                lineToRelative(dx = 0.55f, dy = 0.55f)
                // v 0.745
                verticalLineToRelative(dy = 0.745f)
                // c -0.295 0.05 -0.554 0.205 -0.737 0.426
                curveToRelative(
                    dx1 = -0.295f,
                    dy1 = 0.05f,
                    dx2 = -0.554f,
                    dy2 = 0.205f,
                    dx3 = -0.737f,
                    dy3 = 0.426f,
                )
                // a 0.203 0.203 0 0 0 -0.014 -0.01
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.014f,
                    dy1 = -0.01f,
                )
                // l -0.63 -0.363
                lineToRelative(dx = -0.63f, dy = -0.363f)
                // l -0.202 -0.752
                lineToRelative(dx = -0.202f, dy = -0.752f)
                // a 0.203 0.203 0 0 0 -0.392 0.106
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.392f,
                    dy1 = 0.106f,
                )
                // l 0.096 0.358
                lineToRelative(dx = 0.096f, dy = 0.358f)
                // l -0.455 -0.262
                lineToRelative(dx = -0.455f, dy = -0.262f)
                // a 0.203 0.203 0 0 0 -0.203 0.351
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.203f,
                    dy1 = 0.351f,
                )
                // l 0.455 0.263
                lineToRelative(dx = 0.455f, dy = 0.263f)
                // l -0.36 0.096
                lineToRelative(dx = -0.36f, dy = 0.096f)
                // a 0.203 0.203 0 1 0 0.106 0.393
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.106f,
                    dy1 = 0.393f,
                )
                // l 0.751 -0.202
                lineToRelative(dx = 0.751f, dy = -0.202f)
                // l 0.631 0.365
                lineToRelative(dx = 0.631f, dy = 0.365f)
                // l 0.015 0.007
                lineToRelative(dx = 0.015f, dy = 0.007f)
                // a 1.216 1.216 0 0 0 0 0.852
                arcToRelative(
                    a = 1.216f,
                    b = 1.216f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.852f,
                )
                // a 0.21 0.21 0 0 0 -0.015 0.007
                arcToRelative(
                    a = 0.21f,
                    b = 0.21f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.015f,
                    dy1 = 0.007f,
                )
                // l -0.63 0.365
                lineToRelative(dx = -0.63f, dy = 0.365f)
                // l -0.752 -0.202
                lineToRelative(dx = -0.752f, dy = -0.202f)
                // a 0.203 0.203 0 1 0 -0.105 0.393
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.105f,
                    dy1 = 0.393f,
                )
                // l 0.359 0.096
                lineToRelative(dx = 0.359f, dy = 0.096f)
                // l -0.455 0.263
                lineToRelative(dx = -0.455f, dy = 0.263f)
                // a 0.203 0.203 0 0 0 0.203 0.351
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.203f,
                    dy1 = 0.351f,
                )
                // l 0.455 -0.262
                lineToRelative(dx = 0.455f, dy = -0.262f)
                // l -0.096 0.358
                lineToRelative(dx = -0.096f, dy = 0.358f)
                // a 0.203 0.203 0 0 0 0.392 0.106
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.392f,
                    dy1 = 0.106f,
                )
                // l 0.201 -0.752
                lineToRelative(dx = 0.201f, dy = -0.752f)
                // l 0.631 -0.364
                lineToRelative(dx = 0.631f, dy = -0.364f)
                // a 0.205 0.205 0 0 0 0.014 -0.009
                arcToRelative(
                    a = 0.205f,
                    b = 0.205f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.014f,
                    dy1 = -0.009f,
                )
                // c 0.183 0.221 0.442 0.377 0.738 0.426
                curveToRelative(
                    dx1 = 0.183f,
                    dy1 = 0.221f,
                    dx2 = 0.442f,
                    dy2 = 0.377f,
                    dx3 = 0.738f,
                    dy3 = 0.426f,
                )
                // l -0.001 0.017
                lineToRelative(dx = -0.001f, dy = 0.017f)
                // v 0.728
                verticalLineToRelative(dy = 0.728f)
                // l -0.55 0.55
                lineToRelative(dx = -0.55f, dy = 0.55f)
                // a 0.203 0.203 0 0 0 0.287 0.287
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.287f,
                    dy1 = 0.287f,
                )
                // l 0.263 -0.262
                lineToRelative(dx = 0.263f, dy = -0.262f)
                // v 0.525
                verticalLineToRelative(dy = 0.525f)
                // a 0.203 0.203 0 0 0 0.406 0
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.406f,
                    dy1 = 0.0f,
                )
                // v -0.525
                verticalLineToRelative(dy = -0.525f)
                // l 0.263 0.262
                lineToRelative(dx = 0.263f, dy = 0.262f)
                // a 0.203 0.203 0 0 0 0.287 -0.287
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.287f,
                    dy1 = -0.287f,
                )
                // l -0.55 -0.55
                lineToRelative(dx = -0.55f, dy = -0.55f)
                // v -0.745
                verticalLineToRelative(dy = -0.745f)
                // c 0.295 -0.05 0.554 -0.205 0.737 -0.426
                curveToRelative(
                    dx1 = 0.295f,
                    dy1 = -0.05f,
                    dx2 = 0.554f,
                    dy2 = -0.205f,
                    dx3 = 0.737f,
                    dy3 = -0.426f,
                )
                // l 0.014 0.01
                lineToRelative(dx = 0.014f, dy = 0.01f)
                // l 0.63 0.363
                lineToRelative(dx = 0.63f, dy = 0.363f)
                // l 0.202 0.752
                lineToRelative(dx = 0.202f, dy = 0.752f)
                // a 0.203 0.203 0 0 0 0.392 -0.106
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.392f,
                    dy1 = -0.106f,
                )
                // l -0.096 -0.358
                lineToRelative(dx = -0.096f, dy = -0.358f)
                // l 0.455 0.262
                lineToRelative(dx = 0.455f, dy = 0.262f)
                // a 0.203 0.203 0 0 0 0.203 -0.351
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.203f,
                    dy1 = -0.351f,
                )
                // l -0.455 -0.263
                lineToRelative(dx = -0.455f, dy = -0.263f)
                // l 0.36 -0.096
                lineToRelative(dx = 0.36f, dy = -0.096f)
                // a 0.203 0.203 0 0 0 -0.106 -0.393
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.106f,
                    dy1 = -0.393f,
                )
                // l -0.751 0.202
                lineToRelative(dx = -0.751f, dy = 0.202f)
                // l -0.63 -0.365
                lineToRelative(dx = -0.63f, dy = -0.365f)
                // a 0.205 0.205 0 0 0 -0.016 -0.007
                arcToRelative(
                    a = 0.205f,
                    b = 0.205f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.016f,
                    dy1 = -0.007f,
                )
                // a 1.216 1.216 0 0 0 0 -0.852
                arcToRelative(
                    a = 1.216f,
                    b = 1.216f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.852f,
                )
                // a 0.202 0.202 0 0 0 0.015 -0.007
                arcToRelative(
                    a = 0.202f,
                    b = 0.202f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.015f,
                    dy1 = -0.007f,
                )
                // l 0.63 -0.365
                lineToRelative(dx = 0.63f, dy = -0.365f)
                // l 0.752 0.202
                lineToRelative(dx = 0.752f, dy = 0.202f)
                // a 0.203 0.203 0 1 0 0.105 -0.393
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.105f,
                    dy1 = -0.393f,
                )
                // l -0.359 -0.096
                lineToRelative(dx = -0.359f, dy = -0.096f)
                // l 0.455 -0.263
                lineToRelative(dx = 0.455f, dy = -0.263f)
                // a 0.203 0.203 0 1 0 -0.203 -0.351
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.203f,
                    dy1 = -0.351f,
                )
                // l -0.455 0.262
                lineToRelative(dx = -0.455f, dy = 0.262f)
                // l 0.096 -0.358
                lineToRelative(dx = 0.096f, dy = -0.358f)
                // a 0.203 0.203 0 1 0 -0.392 -0.106
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.392f,
                    dy1 = -0.106f,
                )
                // l -0.201 0.752
                lineToRelative(dx = -0.201f, dy = 0.752f)
                // l -0.631 0.364
                lineToRelative(dx = -0.631f, dy = 0.364f)
                // a 0.204 0.204 0 0 0 -0.014 0.009
                arcToRelative(
                    a = 0.204f,
                    b = 0.204f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.014f,
                    dy1 = 0.009f,
                )
                // a 1.218 1.218 0 0 0 -0.738 -0.426
                arcToRelative(
                    a = 1.218f,
                    b = 1.218f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.738f,
                    dy1 = -0.426f,
                )
                // l 0.001 -0.017
                lineToRelative(dx = 0.001f, dy = -0.017f)
                // v -0.728
                verticalLineToRelative(dy = -0.728f)
                // l 0.55 -0.55
                lineToRelative(dx = 0.55f, dy = -0.55f)
                // a 0.203 0.203 0 0 0 -0.287 -0.287
                arcToRelative(
                    a = 0.203f,
                    b = 0.203f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.287f,
                    dy1 = -0.287f,
                )
                // l -0.263 0.262
                lineToRelative(dx = -0.263f, dy = 0.262f)
                // V 0.203z
                verticalLineTo(y = 0.203f)
                close()
                // M 16 4
                moveTo(x = 16.0f, y = 4.0f)
                // L 6.5 9
                lineTo(x = 6.5f, y = 9.0f)
                // l 3.5 3
                lineToRelative(dx = 3.5f, dy = 3.0f)
                // l -6 4
                lineToRelative(dx = -6.0f, dy = 4.0f)
                // h 9
                horizontalLineToRelative(dx = 9.0f)
                // l 2.5 -5
                lineToRelative(dx = 2.5f, dy = -5.0f)
                // L 11 9
                lineTo(x = 11.0f, y = 9.0f)
                // l 5 -3
                lineToRelative(dx = 5.0f, dy = -3.0f)
                // V 4z
                verticalLineTo(y = 4.0f)
                close()
                // m -1.45 1.878
                moveToRelative(dx = -1.45f, dy = 1.878f)
                // l -0.1 -0.173
                lineToRelative(dx = -0.1f, dy = -0.173f)
                // l 0.5 -0.291
                lineToRelative(dx = 0.5f, dy = -0.291f)
                // l 0.1 0.172
                lineToRelative(dx = 0.1f, dy = 0.172f)
                // l -0.5 0.292z
                lineToRelative(dx = -0.5f, dy = 0.292f)
                close()
                // m -2 1.167
                moveToRelative(dx = -2.0f, dy = 1.167f)
                // l -0.1 -0.173
                lineToRelative(dx = -0.1f, dy = -0.173f)
                // l 1 -0.583
                lineToRelative(dx = 1.0f, dy = -0.583f)
                // l 0.1 0.172
                lineToRelative(dx = 0.1f, dy = 0.172f)
                // l -1 0.584z
                lineToRelative(dx = -1.0f, dy = 0.584f)
                close()
                // m -2 1.166
                moveToRelative(dx = -2.0f, dy = 1.166f)
                // l -0.1 -0.172
                lineToRelative(dx = -0.1f, dy = -0.172f)
                // l 1 -0.584
                lineToRelative(dx = 1.0f, dy = -0.584f)
                // l 0.1 0.173
                lineToRelative(dx = 0.1f, dy = 0.173f)
                // l -1 0.583z
                lineToRelative(dx = -1.0f, dy = 0.583f)
                close()
                // m -1 0.584
                moveToRelative(dx = -1.0f, dy = 0.584f)
                // l -0.376 0.22
                lineToRelative(dx = -0.376f, dy = 0.22f)
                // l 0.265 0.22
                lineToRelative(dx = 0.265f, dy = 0.22f)
                // l -0.128 0.154
                lineToRelative(dx = -0.128f, dy = 0.154f)
                // l -0.485 -0.403
                lineToRelative(dx = -0.485f, dy = -0.403f)
                // l 0.624 -0.364
                lineToRelative(dx = 0.624f, dy = -0.364f)
                // l 0.1 0.173z
                lineToRelative(dx = 0.1f, dy = 0.173f)
                close()
                // m 1.389 1.69
                moveToRelative(dx = 1.389f, dy = 1.69f)
                // l -0.128 0.154
                lineToRelative(dx = -0.128f, dy = 0.154f)
                // l -0.75 -0.625
                lineToRelative(dx = -0.75f, dy = -0.625f)
                // l 0.128 -0.153
                lineToRelative(dx = 0.128f, dy = -0.153f)
                // l 0.75 0.625z
                lineToRelative(dx = 0.75f, dy = 0.625f)
                close()
                // m 1.196 0.998
                moveToRelative(dx = 1.196f, dy = 0.998f)
                // l -0.366 0.513
                lineToRelative(dx = -0.366f, dy = 0.513f)
                // l -0.163 -0.117
                lineToRelative(dx = -0.163f, dy = -0.117f)
                // l 0.259 -0.362
                lineToRelative(dx = 0.259f, dy = -0.362f)
                // l -0.304 -0.253
                lineToRelative(dx = -0.304f, dy = -0.253f)
                // l 0.128 -0.153
                lineToRelative(dx = 0.128f, dy = -0.153f)
                // l 0.446 0.372z
                lineToRelative(dx = 0.446f, dy = 0.372f)
                close()
                // m -1.616 2.263
                moveToRelative(dx = -1.616f, dy = 2.263f)
                // l -0.163 -0.117
                lineToRelative(dx = -0.163f, dy = -0.117f)
                // l 0.625 -0.875
                lineToRelative(dx = 0.625f, dy = -0.875f)
                // l 0.163 0.117
                lineToRelative(dx = 0.163f, dy = 0.117f)
                // l -0.625 0.875z
                lineToRelative(dx = -0.625f, dy = 0.875f)
                close()
                // m -0.938 1.312
                moveToRelative(dx = -0.938f, dy = 1.312f)
                // l -0.162 -0.116
                lineToRelative(dx = -0.162f, dy = -0.116f)
                // l 0.312 -0.438
                lineToRelative(dx = 0.312f, dy = -0.438f)
                // l 0.163 0.117
                lineToRelative(dx = 0.163f, dy = 0.117f)
                // l -0.313 0.437z
                lineToRelative(dx = -0.313f, dy = 0.437f)
                close()
            }
        }.build().also { _ic2085 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2085: ImageVector? = null
