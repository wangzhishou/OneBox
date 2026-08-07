package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2217: ImageVector
    get() {
        val current = _ic2217
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2217",
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
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z m-5.281 .719 a.219 .219 0 0 0 -.438 0 v.565 l-.283 -.282 a.219 .219 0 1 0 -.309 .309 l.592 .592 v.785 a.22 .22 0 0 0 .001 .018 1.31 1.31 0 0 0 -.794 .458 .215 .215 0 0 0 -.015 -.01 l-.68 -.392 -.216 -.809 a.219 .219 0 1 0 -.423 .113 l.104 .387 -.49 -.283 a.219 .219 0 1 0 -.219 .379 l.49 .283 -.386 .103 a.219 .219 0 1 0 .113 .423 l.809 -.217 .679 .392 a.22 .22 0 0 0 .016 .008 1.31 1.31 0 0 0 0 .918 .219 .219 0 0 0 -.016 .008 l-.68 .392 -.808 -.217 a.219 .219 0 1 0 -.113 .423 l.386 .103 -.49 .283 a.219 .219 0 0 0 .219 .38 l.49 -.284 -.104 .387 a.219 .219 0 1 0 .423 .113 l.216 -.81 .68 -.391 a.224 .224 0 0 0 .015 -.01 c.197 .238 .476 .405 .794 .458 a.22 .22 0 0 0 0 .019 v.784 l-.593 .592 A.219 .219 0 1 0 3 9.5 l.282 -.283 v.565 a.219 .219 0 1 0 .438 0 v-.565 l.283 .282 a.219 .219 0 1 0 .309 -.309 l-.592 -.592 v-.784 a.22 .22 0 0 0 -.001 -.019 1.31 1.31 0 0 0 .794 -.458 .22 .22 0 0 0 .015 .01 l.68 .392 .216 .809 a.219 .219 0 1 0 .423 -.113 l-.104 -.387 .49 .283 a.219 .219 0 0 0 .219 -.379 l-.49 -.283 .387 -.103 a.219 .219 0 1 0 -.114 -.423 l-.809 .217 -.679 -.392 a.219 .219 0 0 0 -.016 -.009 1.31 1.31 0 0 0 0 -.916 .22 .22 0 0 0 .016 -.009 l.68 -.392 .808 .217 a.219 .219 0 0 0 .114 -.423 l-.387 -.103 .49 -.283 a.219 .219 0 1 0 -.219 -.38 l-.49 .284 .104 -.387 a.219 .219 0 1 0 -.423 -.113 l-.216 .81 -.68 .391 a.222 .222 0 0 0 -.015 .01 1.311 1.311 0 0 0 -.794 -.458 .22 .22 0 0 0 0 -.019 v-.784 l.593 -.592 a.219 .219 0 0 0 -.31 -.31 l-.282 .283 V3.22Z M6.44 1.578 4.559 .5 l-2.16 .929 -1.36 -.78 -.599 .773 L2.321 2.5 l2.16 -.929 1.36 .78 .599 -.773Z m0 10 L4.559 10.5 l-2.16 .928 -1.36 -.779 -.599 .773 L2.321 12.5 l2.16 -.928 1.36 .779 .599 -.773Z
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
                // m -5.281 0.719
                moveToRelative(dx = -5.281f, dy = 0.719f)
                // a 0.219 0.219 0 0 0 -0.438 0
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.438f,
                    dy1 = 0.0f,
                )
                // v 0.565
                verticalLineToRelative(dy = 0.565f)
                // l -0.283 -0.282
                lineToRelative(dx = -0.283f, dy = -0.282f)
                // a 0.219 0.219 0 1 0 -0.309 0.309
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.309f,
                    dy1 = 0.309f,
                )
                // l 0.592 0.592
                lineToRelative(dx = 0.592f, dy = 0.592f)
                // v 0.785
                verticalLineToRelative(dy = 0.785f)
                // a 0.22 0.22 0 0 0 0.001 0.018
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.001f,
                    dy1 = 0.018f,
                )
                // a 1.31 1.31 0 0 0 -0.794 0.458
                arcToRelative(
                    a = 1.31f,
                    b = 1.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.794f,
                    dy1 = 0.458f,
                )
                // a 0.215 0.215 0 0 0 -0.015 -0.01
                arcToRelative(
                    a = 0.215f,
                    b = 0.215f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.015f,
                    dy1 = -0.01f,
                )
                // l -0.68 -0.392
                lineToRelative(dx = -0.68f, dy = -0.392f)
                // l -0.216 -0.809
                lineToRelative(dx = -0.216f, dy = -0.809f)
                // a 0.219 0.219 0 1 0 -0.423 0.113
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.423f,
                    dy1 = 0.113f,
                )
                // l 0.104 0.387
                lineToRelative(dx = 0.104f, dy = 0.387f)
                // l -0.49 -0.283
                lineToRelative(dx = -0.49f, dy = -0.283f)
                // a 0.219 0.219 0 1 0 -0.219 0.379
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.219f,
                    dy1 = 0.379f,
                )
                // l 0.49 0.283
                lineToRelative(dx = 0.49f, dy = 0.283f)
                // l -0.386 0.103
                lineToRelative(dx = -0.386f, dy = 0.103f)
                // a 0.219 0.219 0 1 0 0.113 0.423
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.113f,
                    dy1 = 0.423f,
                )
                // l 0.809 -0.217
                lineToRelative(dx = 0.809f, dy = -0.217f)
                // l 0.679 0.392
                lineToRelative(dx = 0.679f, dy = 0.392f)
                // a 0.22 0.22 0 0 0 0.016 0.008
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.016f,
                    dy1 = 0.008f,
                )
                // a 1.31 1.31 0 0 0 0 0.918
                arcToRelative(
                    a = 1.31f,
                    b = 1.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.918f,
                )
                // a 0.219 0.219 0 0 0 -0.016 0.008
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.016f,
                    dy1 = 0.008f,
                )
                // l -0.68 0.392
                lineToRelative(dx = -0.68f, dy = 0.392f)
                // l -0.808 -0.217
                lineToRelative(dx = -0.808f, dy = -0.217f)
                // a 0.219 0.219 0 1 0 -0.113 0.423
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.113f,
                    dy1 = 0.423f,
                )
                // l 0.386 0.103
                lineToRelative(dx = 0.386f, dy = 0.103f)
                // l -0.49 0.283
                lineToRelative(dx = -0.49f, dy = 0.283f)
                // a 0.219 0.219 0 0 0 0.219 0.38
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.219f,
                    dy1 = 0.38f,
                )
                // l 0.49 -0.284
                lineToRelative(dx = 0.49f, dy = -0.284f)
                // l -0.104 0.387
                lineToRelative(dx = -0.104f, dy = 0.387f)
                // a 0.219 0.219 0 1 0 0.423 0.113
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.423f,
                    dy1 = 0.113f,
                )
                // l 0.216 -0.81
                lineToRelative(dx = 0.216f, dy = -0.81f)
                // l 0.68 -0.391
                lineToRelative(dx = 0.68f, dy = -0.391f)
                // a 0.224 0.224 0 0 0 0.015 -0.01
                arcToRelative(
                    a = 0.224f,
                    b = 0.224f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.015f,
                    dy1 = -0.01f,
                )
                // c 0.197 0.238 0.476 0.405 0.794 0.458
                curveToRelative(
                    dx1 = 0.197f,
                    dy1 = 0.238f,
                    dx2 = 0.476f,
                    dy2 = 0.405f,
                    dx3 = 0.794f,
                    dy3 = 0.458f,
                )
                // a 0.22 0.22 0 0 0 0 0.019
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 0.019f,
                )
                // v 0.784
                verticalLineToRelative(dy = 0.784f)
                // l -0.593 0.592
                lineToRelative(dx = -0.593f, dy = 0.592f)
                // A 0.219 0.219 0 1 0 3 9.5
                arcTo(
                    horizontalEllipseRadius = 0.219f,
                    verticalEllipseRadius = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 3.0f,
                    y1 = 9.5f,
                )
                // l 0.282 -0.283
                lineToRelative(dx = 0.282f, dy = -0.283f)
                // v 0.565
                verticalLineToRelative(dy = 0.565f)
                // a 0.219 0.219 0 1 0 0.438 0
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.438f,
                    dy1 = 0.0f,
                )
                // v -0.565
                verticalLineToRelative(dy = -0.565f)
                // l 0.283 0.282
                lineToRelative(dx = 0.283f, dy = 0.282f)
                // a 0.219 0.219 0 1 0 0.309 -0.309
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.309f,
                    dy1 = -0.309f,
                )
                // l -0.592 -0.592
                lineToRelative(dx = -0.592f, dy = -0.592f)
                // v -0.784
                verticalLineToRelative(dy = -0.784f)
                // a 0.22 0.22 0 0 0 -0.001 -0.019
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.001f,
                    dy1 = -0.019f,
                )
                // a 1.31 1.31 0 0 0 0.794 -0.458
                arcToRelative(
                    a = 1.31f,
                    b = 1.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.794f,
                    dy1 = -0.458f,
                )
                // a 0.22 0.22 0 0 0 0.015 0.01
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.015f,
                    dy1 = 0.01f,
                )
                // l 0.68 0.392
                lineToRelative(dx = 0.68f, dy = 0.392f)
                // l 0.216 0.809
                lineToRelative(dx = 0.216f, dy = 0.809f)
                // a 0.219 0.219 0 1 0 0.423 -0.113
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.423f,
                    dy1 = -0.113f,
                )
                // l -0.104 -0.387
                lineToRelative(dx = -0.104f, dy = -0.387f)
                // l 0.49 0.283
                lineToRelative(dx = 0.49f, dy = 0.283f)
                // a 0.219 0.219 0 0 0 0.219 -0.379
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.219f,
                    dy1 = -0.379f,
                )
                // l -0.49 -0.283
                lineToRelative(dx = -0.49f, dy = -0.283f)
                // l 0.387 -0.103
                lineToRelative(dx = 0.387f, dy = -0.103f)
                // a 0.219 0.219 0 1 0 -0.114 -0.423
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.114f,
                    dy1 = -0.423f,
                )
                // l -0.809 0.217
                lineToRelative(dx = -0.809f, dy = 0.217f)
                // l -0.679 -0.392
                lineToRelative(dx = -0.679f, dy = -0.392f)
                // a 0.219 0.219 0 0 0 -0.016 -0.009
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.016f,
                    dy1 = -0.009f,
                )
                // a 1.31 1.31 0 0 0 0 -0.916
                arcToRelative(
                    a = 1.31f,
                    b = 1.31f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.916f,
                )
                // a 0.22 0.22 0 0 0 0.016 -0.009
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.016f,
                    dy1 = -0.009f,
                )
                // l 0.68 -0.392
                lineToRelative(dx = 0.68f, dy = -0.392f)
                // l 0.808 0.217
                lineToRelative(dx = 0.808f, dy = 0.217f)
                // a 0.219 0.219 0 0 0 0.114 -0.423
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.114f,
                    dy1 = -0.423f,
                )
                // l -0.387 -0.103
                lineToRelative(dx = -0.387f, dy = -0.103f)
                // l 0.49 -0.283
                lineToRelative(dx = 0.49f, dy = -0.283f)
                // a 0.219 0.219 0 1 0 -0.219 -0.38
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.219f,
                    dy1 = -0.38f,
                )
                // l -0.49 0.284
                lineToRelative(dx = -0.49f, dy = 0.284f)
                // l 0.104 -0.387
                lineToRelative(dx = 0.104f, dy = -0.387f)
                // a 0.219 0.219 0 1 0 -0.423 -0.113
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = -0.423f,
                    dy1 = -0.113f,
                )
                // l -0.216 0.81
                lineToRelative(dx = -0.216f, dy = 0.81f)
                // l -0.68 0.391
                lineToRelative(dx = -0.68f, dy = 0.391f)
                // a 0.222 0.222 0 0 0 -0.015 0.01
                arcToRelative(
                    a = 0.222f,
                    b = 0.222f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.015f,
                    dy1 = 0.01f,
                )
                // a 1.311 1.311 0 0 0 -0.794 -0.458
                arcToRelative(
                    a = 1.311f,
                    b = 1.311f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.794f,
                    dy1 = -0.458f,
                )
                // a 0.22 0.22 0 0 0 0 -0.019
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.019f,
                )
                // v -0.784
                verticalLineToRelative(dy = -0.784f)
                // l 0.593 -0.592
                lineToRelative(dx = 0.593f, dy = -0.592f)
                // a 0.219 0.219 0 0 0 -0.31 -0.31
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.31f,
                    dy1 = -0.31f,
                )
                // l -0.282 0.283
                lineToRelative(dx = -0.282f, dy = 0.283f)
                // V 3.22z
                verticalLineTo(y = 3.22f)
                close()
                // M 6.44 1.578
                moveTo(x = 6.44f, y = 1.578f)
                // L 4.559 0.5
                lineTo(x = 4.559f, y = 0.5f)
                // l -2.16 0.929
                lineToRelative(dx = -2.16f, dy = 0.929f)
                // l -1.36 -0.78
                lineToRelative(dx = -1.36f, dy = -0.78f)
                // l -0.599 0.773
                lineToRelative(dx = -0.599f, dy = 0.773f)
                // L 2.321 2.5
                lineTo(x = 2.321f, y = 2.5f)
                // l 2.16 -0.929
                lineToRelative(dx = 2.16f, dy = -0.929f)
                // l 1.36 0.78
                lineToRelative(dx = 1.36f, dy = 0.78f)
                // l 0.599 -0.773z
                lineToRelative(dx = 0.599f, dy = -0.773f)
                close()
                // m 0 10
                moveToRelative(dx = 0.0f, dy = 10.0f)
                // L 4.559 10.5
                lineTo(x = 4.559f, y = 10.5f)
                // l -2.16 0.928
                lineToRelative(dx = -2.16f, dy = 0.928f)
                // l -1.36 -0.779
                lineToRelative(dx = -1.36f, dy = -0.779f)
                // l -0.599 0.773
                lineToRelative(dx = -0.599f, dy = 0.773f)
                // L 2.321 12.5
                lineTo(x = 2.321f, y = 12.5f)
                // l 2.16 -0.928
                lineToRelative(dx = 2.16f, dy = -0.928f)
                // l 1.36 0.779
                lineToRelative(dx = 1.36f, dy = 0.779f)
                // l 0.599 -0.773z
                lineToRelative(dx = 0.599f, dy = -0.773f)
                close()
            }
        }.build().also { _ic2217 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2217: ImageVector? = null
