package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2305: ImageVector
    get() {
        val current = _ic2305
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2305",
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
            // m10.2 8.399 -.532 .356 a3.3 3.3 0 1 0 3.665 0 l-.533 -.356 V2.5 a1.3 1.3 0 1 0 -2.6 0 v5.899Z M9 2.5 a2.5 2.5 0 0 1 5 0 v5.258 a4.5 4.5 0 1 1 -5 0 V2.5Z M3.719 1.219 a.219 .219 0 0 0 -.438 0 v.565 l-.283 -.282 a.219 .219 0 1 0 -.309 .309 l.592 .592 v.784 a.22 .22 0 0 0 .001 .019 1.31 1.31 0 0 0 -.794 .458 .215 .215 0 0 0 -.015 -.01 l-.68 -.392 -.216 -.809 a.219 .219 0 1 0 -.423 .113 l.104 .387 -.49 -.283 a.219 .219 0 1 0 -.219 .379 l.49 .283 -.386 .103 a.219 .219 0 1 0 .113 .423 l.809 -.217 .679 .392 a.22 .22 0 0 0 .016 .008 1.31 1.31 0 0 0 0 .918 .219 .219 0 0 0 -.016 .008 l-.68 .392 -.808 -.217 a.219 .219 0 1 0 -.113 .423 l.386 .103 -.49 .283 a.219 .219 0 0 0 .219 .38 l.49 -.284 -.104 .387 a.219 .219 0 1 0 .423 .113 l.216 -.81 .68 -.391 a.224 .224 0 0 0 .015 -.01 c.197 .238 .476 .405 .794 .458 a.22 .22 0 0 0 0 .019 v.784 l-.593 .592 A.219 .219 0 1 0 3 7.5 l.282 -.283 v.565 a.219 .219 0 1 0 .438 0 v-.565 l.283 .282 a.219 .219 0 1 0 .309 -.309 l-.592 -.592 v-.785 a.22 .22 0 0 0 -.001 -.018 1.31 1.31 0 0 0 .794 -.458 .22 .22 0 0 0 .015 .01 l.68 .392 .216 .809 a.219 .219 0 1 0 .423 -.113 l-.104 -.387 .49 .283 a.219 .219 0 0 0 .219 -.379 l-.49 -.283 .387 -.103 a.219 .219 0 1 0 -.114 -.423 l-.809 .217 -.679 -.392 a.219 .219 0 0 0 -.016 -.009 1.31 1.31 0 0 0 0 -.916 .22 .22 0 0 0 .016 -.009 l.68 -.392 .808 .217 a.219 .219 0 1 0 .114 -.423 l-.387 -.103 .49 -.283 a.219 .219 0 1 0 -.219 -.38 l-.49 .284 .104 -.387 a.219 .219 0 1 0 -.423 -.113 l-.216 .81 -.68 .391 a.222 .222 0 0 0 -.015 .01 1.311 1.311 0 0 0 -.794 -.458 .22 .22 0 0 0 0 -.018 v-.785 l.593 -.592 a.219 .219 0 0 0 -.31 -.31 l-.282 .283 V1.22Z M4.76 9 l1.74 1.161 -.555 .832 -1.258 -.839 -1.964 .982 L.5 10.024 l.447 -.894 1.777 .888 L4.76 9Z
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
                // M 3.719 1.219
                moveTo(x = 3.719f, y = 1.219f)
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
                // v 0.784
                verticalLineToRelative(dy = 0.784f)
                // a 0.22 0.22 0 0 0 0.001 0.019
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.001f,
                    dy1 = 0.019f,
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
                // A 0.219 0.219 0 1 0 3 7.5
                arcTo(
                    horizontalEllipseRadius = 0.219f,
                    verticalEllipseRadius = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    x1 = 3.0f,
                    y1 = 7.5f,
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
                // v -0.785
                verticalLineToRelative(dy = -0.785f)
                // a 0.22 0.22 0 0 0 -0.001 -0.018
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.001f,
                    dy1 = -0.018f,
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
                // a 0.219 0.219 0 1 0 0.114 -0.423
                arcToRelative(
                    a = 0.219f,
                    b = 0.219f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
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
                // a 0.22 0.22 0 0 0 0 -0.018
                arcToRelative(
                    a = 0.22f,
                    b = 0.22f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -0.018f,
                )
                // v -0.785
                verticalLineToRelative(dy = -0.785f)
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
                // V 1.22z
                verticalLineTo(y = 1.22f)
                close()
                // M 4.76 9
                moveTo(x = 4.76f, y = 9.0f)
                // l 1.74 1.161
                lineToRelative(dx = 1.74f, dy = 1.161f)
                // l -0.555 0.832
                lineToRelative(dx = -0.555f, dy = 0.832f)
                // l -1.258 -0.839
                lineToRelative(dx = -1.258f, dy = -0.839f)
                // l -1.964 0.982
                lineToRelative(dx = -1.964f, dy = 0.982f)
                // L 0.5 10.024
                lineTo(x = 0.5f, y = 10.024f)
                // l 0.447 -0.894
                lineToRelative(dx = 0.447f, dy = -0.894f)
                // l 1.777 0.888
                lineToRelative(dx = 1.777f, dy = 0.888f)
                // L 4.76 9z
                lineTo(x = 4.76f, y = 9.0f)
                close()
            }
            // m4.76 11 1.74 1.161 -.555 .832 -1.258 -.839 -1.964 .982 L.5 12.024 l.447 -.894 1.777 .888 L4.76 11Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 4.76 11
                moveTo(x = 4.76f, y = 11.0f)
                // l 1.74 1.161
                lineToRelative(dx = 1.74f, dy = 1.161f)
                // l -0.555 0.832
                lineToRelative(dx = -0.555f, dy = 0.832f)
                // l -1.258 -0.839
                lineToRelative(dx = -1.258f, dy = -0.839f)
                // l -1.964 0.982
                lineToRelative(dx = -1.964f, dy = 0.982f)
                // L 0.5 12.024
                lineTo(x = 0.5f, y = 12.024f)
                // l 0.447 -0.894
                lineToRelative(dx = 0.447f, dy = -0.894f)
                // l 1.777 0.888
                lineToRelative(dx = 1.777f, dy = 0.888f)
                // L 4.76 11z
                lineTo(x = 4.76f, y = 11.0f)
                close()
            }
            // M6.501 14.161 4.76 13 l-2.036 1.018 -1.777 -.888 -.447 .894 2.224 1.112 1.964 -.982 1.258 .839 .555 -.832Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 6.501 14.161
                moveTo(x = 6.501f, y = 14.161f)
                // L 4.76 13
                lineTo(x = 4.76f, y = 13.0f)
                // l -2.036 1.018
                lineToRelative(dx = -2.036f, dy = 1.018f)
                // l -1.777 -0.888
                lineToRelative(dx = -1.777f, dy = -0.888f)
                // l -0.447 0.894
                lineToRelative(dx = -0.447f, dy = 0.894f)
                // l 2.224 1.112
                lineToRelative(dx = 2.224f, dy = 1.112f)
                // l 1.964 -0.982
                lineToRelative(dx = 1.964f, dy = -0.982f)
                // l 1.258 0.839
                lineToRelative(dx = 1.258f, dy = 0.839f)
                // l 0.555 -0.832z
                lineToRelative(dx = 0.555f, dy = -0.832f)
                close()
            }
        }.build().also { _ic2305 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2305: ImageVector? = null
