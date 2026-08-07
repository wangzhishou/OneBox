package com.weather.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val QWeatherIcons.Ic2210: ImageVector
    get() {
        val current = _ic2210
        if (current != null) return current

        return ImageVector.Builder(
            name = "QWeather.Ic2210",
            defaultWidth = 16.0.dp,
            defaultHeight = 16.0.dp,
            viewportWidth = 16.0f,
            viewportHeight = 16.0f,
        ).apply {
            // M14.619 1.958 c.575 .42 1.02 .937 1.195 1.549 a.5 .5 0 1 1 -.961 .275 c-.092 -.322 -.352 -.671 -.825 -1.017 -.469 -.344 -1.111 -.656 -1.894 -.912 -1.566 -.511 -3.624 -.773 -5.793 -.643 h-.005 c-1.708 .087 -3.14 .454 -4.072 .953 -.467 .25 -.776 .516 -.945 .765 -.158 .235 -.191 .45 -.128 .664 .047 .157 .183 .353 .468 .56 .28 .205 .673 .397 1.162 .556 .976 .316 2.268 .478 3.637 .393 h.002 c1.087 -.064 1.978 -.298 2.545 -.598 .285 -.151 .456 -.302 .542 -.426 .076 -.11 .08 -.183 .062 -.249 -.014 -.045 -.066 -.136 -.227 -.253 a2.536 2.536 0 0 0 -.692 -.327 c-.6 -.195 -1.402 -.301 -2.261 -.26 -.638 .043 -1.171 .17 -1.475 .338 a.5 .5 0 0 1 -.486 -.874 c.496 -.275 1.205 -.416 1.899 -.462 h.009 c.961 -.047 1.89 .068 2.623 .307 .366 .119 .7 .274 .97 .468 .264 .192 .502 .45 .6 .783 v.003 c.115 .401 .018 .782 -.201 1.097 -.21 .301 -.53 .545 -.895 .738 -.733 .389 -1.775 .644 -2.954 .713 -1.475 .092 -2.894 -.08 -4.007 -.44 -.556 -.18 -1.052 -.414 -1.443 -.699 -.387 -.282 -.706 -.642 -.837 -1.085 -.16 -.541 -.042 -1.06 .259 -1.506 .29 -.432 .751 -.793 1.3 -1.088 C2.894 .691 4.482 .303 6.285 .211 c2.274 -.135 4.46 .136 6.16 .691 .85 .278 1.596 .632 2.175 1.056Z m-1.323 3.974 a.5 .5 0 0 1 -.116 .697 C10.858 8.288 7.814 8.6 5.092 8 a.5 .5 0 1 1 .216 -.976 c2.522 .556 5.257 .245 7.29 -1.208 a.5 .5 0 0 1 .698 .117Z M2.979 7.342 A.5 .5 0 0 0 2 7.146 c-.129 .64 .123 1.21 .478 1.651 .354 .44 .847 .8 1.326 1.063 l.011 .006 c2.473 1.284 5.65 1.124 8.228 .158 a.5 .5 0 0 0 -.35 -.937 c-2.399 .9 -5.263 1.007 -7.412 -.106 -.407 -.224 -.78 -.507 -1.024 -.811 -.245 -.304 -.327 -.579 -.277 -.828Z m10.127 4.42 a.5 .5 0 0 1 -.201 .678 c-2.168 1.175 -4.853 1.278 -7.12 .203 a.5 .5 0 1 1 .43 -.903 c1.971 .935 4.32 .846 6.213 -.18 a.5 .5 0 0 1 .678 .202Z m.48 2.956 a.5 .5 0 0 0 -.549 -.836 c-.61 .4 -1.124 .629 -1.641 .727 -.516 .097 -1.073 .071 -1.781 -.096 a.5 .5 0 0 0 -.23 .974 c.803 .188 1.507 .235 2.197 .104 .688 -.13 1.325 -.428 2.003 -.873Z M13.5 9 a.5 .5 0 1 1 -1 0 .5 .5 0 0 1 1 0Z m-10 -5 a.5 .5 0 1 0 0 -1 .5 .5 0 0 0 0 1Z
            path(
                fill = SolidColor(Color(0xFF000000)),
            ) {
                // M 14.619 1.958
                moveTo(x = 14.619f, y = 1.958f)
                // c 0.575 0.42 1.02 0.937 1.195 1.549
                curveToRelative(
                    dx1 = 0.575f,
                    dy1 = 0.42f,
                    dx2 = 1.02f,
                    dy2 = 0.937f,
                    dx3 = 1.195f,
                    dy3 = 1.549f,
                )
                // a 0.5 0.5 0 1 1 -0.961 0.275
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -0.961f,
                    dy1 = 0.275f,
                )
                // c -0.092 -0.322 -0.352 -0.671 -0.825 -1.017
                curveToRelative(
                    dx1 = -0.092f,
                    dy1 = -0.322f,
                    dx2 = -0.352f,
                    dy2 = -0.671f,
                    dx3 = -0.825f,
                    dy3 = -1.017f,
                )
                // c -0.469 -0.344 -1.111 -0.656 -1.894 -0.912
                curveToRelative(
                    dx1 = -0.469f,
                    dy1 = -0.344f,
                    dx2 = -1.111f,
                    dy2 = -0.656f,
                    dx3 = -1.894f,
                    dy3 = -0.912f,
                )
                // c -1.566 -0.511 -3.624 -0.773 -5.793 -0.643
                curveToRelative(
                    dx1 = -1.566f,
                    dy1 = -0.511f,
                    dx2 = -3.624f,
                    dy2 = -0.773f,
                    dx3 = -5.793f,
                    dy3 = -0.643f,
                )
                // h -0.005
                horizontalLineToRelative(dx = -0.005f)
                // c -1.708 0.087 -3.14 0.454 -4.072 0.953
                curveToRelative(
                    dx1 = -1.708f,
                    dy1 = 0.087f,
                    dx2 = -3.14f,
                    dy2 = 0.454f,
                    dx3 = -4.072f,
                    dy3 = 0.953f,
                )
                // c -0.467 0.25 -0.776 0.516 -0.945 0.765
                curveToRelative(
                    dx1 = -0.467f,
                    dy1 = 0.25f,
                    dx2 = -0.776f,
                    dy2 = 0.516f,
                    dx3 = -0.945f,
                    dy3 = 0.765f,
                )
                // c -0.158 0.235 -0.191 0.45 -0.128 0.664
                curveToRelative(
                    dx1 = -0.158f,
                    dy1 = 0.235f,
                    dx2 = -0.191f,
                    dy2 = 0.45f,
                    dx3 = -0.128f,
                    dy3 = 0.664f,
                )
                // c 0.047 0.157 0.183 0.353 0.468 0.56
                curveToRelative(
                    dx1 = 0.047f,
                    dy1 = 0.157f,
                    dx2 = 0.183f,
                    dy2 = 0.353f,
                    dx3 = 0.468f,
                    dy3 = 0.56f,
                )
                // c 0.28 0.205 0.673 0.397 1.162 0.556
                curveToRelative(
                    dx1 = 0.28f,
                    dy1 = 0.205f,
                    dx2 = 0.673f,
                    dy2 = 0.397f,
                    dx3 = 1.162f,
                    dy3 = 0.556f,
                )
                // c 0.976 0.316 2.268 0.478 3.637 0.393
                curveToRelative(
                    dx1 = 0.976f,
                    dy1 = 0.316f,
                    dx2 = 2.268f,
                    dy2 = 0.478f,
                    dx3 = 3.637f,
                    dy3 = 0.393f,
                )
                // h 0.002
                horizontalLineToRelative(dx = 0.002f)
                // c 1.087 -0.064 1.978 -0.298 2.545 -0.598
                curveToRelative(
                    dx1 = 1.087f,
                    dy1 = -0.064f,
                    dx2 = 1.978f,
                    dy2 = -0.298f,
                    dx3 = 2.545f,
                    dy3 = -0.598f,
                )
                // c 0.285 -0.151 0.456 -0.302 0.542 -0.426
                curveToRelative(
                    dx1 = 0.285f,
                    dy1 = -0.151f,
                    dx2 = 0.456f,
                    dy2 = -0.302f,
                    dx3 = 0.542f,
                    dy3 = -0.426f,
                )
                // c 0.076 -0.11 0.08 -0.183 0.062 -0.249
                curveToRelative(
                    dx1 = 0.076f,
                    dy1 = -0.11f,
                    dx2 = 0.08f,
                    dy2 = -0.183f,
                    dx3 = 0.062f,
                    dy3 = -0.249f,
                )
                // c -0.014 -0.045 -0.066 -0.136 -0.227 -0.253
                curveToRelative(
                    dx1 = -0.014f,
                    dy1 = -0.045f,
                    dx2 = -0.066f,
                    dy2 = -0.136f,
                    dx3 = -0.227f,
                    dy3 = -0.253f,
                )
                // a 2.536 2.536 0 0 0 -0.692 -0.327
                arcToRelative(
                    a = 2.536f,
                    b = 2.536f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.692f,
                    dy1 = -0.327f,
                )
                // c -0.6 -0.195 -1.402 -0.301 -2.261 -0.26
                curveToRelative(
                    dx1 = -0.6f,
                    dy1 = -0.195f,
                    dx2 = -1.402f,
                    dy2 = -0.301f,
                    dx3 = -2.261f,
                    dy3 = -0.26f,
                )
                // c -0.638 0.043 -1.171 0.17 -1.475 0.338
                curveToRelative(
                    dx1 = -0.638f,
                    dy1 = 0.043f,
                    dx2 = -1.171f,
                    dy2 = 0.17f,
                    dx3 = -1.475f,
                    dy3 = 0.338f,
                )
                // a 0.5 0.5 0 0 1 -0.486 -0.874
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.486f,
                    dy1 = -0.874f,
                )
                // c 0.496 -0.275 1.205 -0.416 1.899 -0.462
                curveToRelative(
                    dx1 = 0.496f,
                    dy1 = -0.275f,
                    dx2 = 1.205f,
                    dy2 = -0.416f,
                    dx3 = 1.899f,
                    dy3 = -0.462f,
                )
                // h 0.009
                horizontalLineToRelative(dx = 0.009f)
                // c 0.961 -0.047 1.89 0.068 2.623 0.307
                curveToRelative(
                    dx1 = 0.961f,
                    dy1 = -0.047f,
                    dx2 = 1.89f,
                    dy2 = 0.068f,
                    dx3 = 2.623f,
                    dy3 = 0.307f,
                )
                // c 0.366 0.119 0.7 0.274 0.97 0.468
                curveToRelative(
                    dx1 = 0.366f,
                    dy1 = 0.119f,
                    dx2 = 0.7f,
                    dy2 = 0.274f,
                    dx3 = 0.97f,
                    dy3 = 0.468f,
                )
                // c 0.264 0.192 0.502 0.45 0.6 0.783
                curveToRelative(
                    dx1 = 0.264f,
                    dy1 = 0.192f,
                    dx2 = 0.502f,
                    dy2 = 0.45f,
                    dx3 = 0.6f,
                    dy3 = 0.783f,
                )
                // v 0.003
                verticalLineToRelative(dy = 0.003f)
                // c 0.115 0.401 0.018 0.782 -0.201 1.097
                curveToRelative(
                    dx1 = 0.115f,
                    dy1 = 0.401f,
                    dx2 = 0.018f,
                    dy2 = 0.782f,
                    dx3 = -0.201f,
                    dy3 = 1.097f,
                )
                // c -0.21 0.301 -0.53 0.545 -0.895 0.738
                curveToRelative(
                    dx1 = -0.21f,
                    dy1 = 0.301f,
                    dx2 = -0.53f,
                    dy2 = 0.545f,
                    dx3 = -0.895f,
                    dy3 = 0.738f,
                )
                // c -0.733 0.389 -1.775 0.644 -2.954 0.713
                curveToRelative(
                    dx1 = -0.733f,
                    dy1 = 0.389f,
                    dx2 = -1.775f,
                    dy2 = 0.644f,
                    dx3 = -2.954f,
                    dy3 = 0.713f,
                )
                // c -1.475 0.092 -2.894 -0.08 -4.007 -0.44
                curveToRelative(
                    dx1 = -1.475f,
                    dy1 = 0.092f,
                    dx2 = -2.894f,
                    dy2 = -0.08f,
                    dx3 = -4.007f,
                    dy3 = -0.44f,
                )
                // c -0.556 -0.18 -1.052 -0.414 -1.443 -0.699
                curveToRelative(
                    dx1 = -0.556f,
                    dy1 = -0.18f,
                    dx2 = -1.052f,
                    dy2 = -0.414f,
                    dx3 = -1.443f,
                    dy3 = -0.699f,
                )
                // c -0.387 -0.282 -0.706 -0.642 -0.837 -1.085
                curveToRelative(
                    dx1 = -0.387f,
                    dy1 = -0.282f,
                    dx2 = -0.706f,
                    dy2 = -0.642f,
                    dx3 = -0.837f,
                    dy3 = -1.085f,
                )
                // c -0.16 -0.541 -0.042 -1.06 0.259 -1.506
                curveToRelative(
                    dx1 = -0.16f,
                    dy1 = -0.541f,
                    dx2 = -0.042f,
                    dy2 = -1.06f,
                    dx3 = 0.259f,
                    dy3 = -1.506f,
                )
                // c 0.29 -0.432 0.751 -0.793 1.3 -1.088
                curveToRelative(
                    dx1 = 0.29f,
                    dy1 = -0.432f,
                    dx2 = 0.751f,
                    dy2 = -0.793f,
                    dx3 = 1.3f,
                    dy3 = -1.088f,
                )
                // C 2.894 0.691 4.482 0.303 6.285 0.211
                curveTo(
                    x1 = 2.894f,
                    y1 = 0.691f,
                    x2 = 4.482f,
                    y2 = 0.303f,
                    x3 = 6.285f,
                    y3 = 0.211f,
                )
                // c 2.274 -0.135 4.46 0.136 6.16 0.691
                curveToRelative(
                    dx1 = 2.274f,
                    dy1 = -0.135f,
                    dx2 = 4.46f,
                    dy2 = 0.136f,
                    dx3 = 6.16f,
                    dy3 = 0.691f,
                )
                // c 0.85 0.278 1.596 0.632 2.175 1.056z
                curveToRelative(
                    dx1 = 0.85f,
                    dy1 = 0.278f,
                    dx2 = 1.596f,
                    dy2 = 0.632f,
                    dx3 = 2.175f,
                    dy3 = 1.056f,
                )
                close()
                // m -1.323 3.974
                moveToRelative(dx = -1.323f, dy = 3.974f)
                // a 0.5 0.5 0 0 1 -0.116 0.697
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.116f,
                    dy1 = 0.697f,
                )
                // C 10.858 8.288 7.814 8.6 5.092 8
                curveTo(
                    x1 = 10.858f,
                    y1 = 8.288f,
                    x2 = 7.814f,
                    y2 = 8.6f,
                    x3 = 5.092f,
                    y3 = 8.0f,
                )
                // a 0.5 0.5 0 1 1 0.216 -0.976
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.216f,
                    dy1 = -0.976f,
                )
                // c 2.522 0.556 5.257 0.245 7.29 -1.208
                curveToRelative(
                    dx1 = 2.522f,
                    dy1 = 0.556f,
                    dx2 = 5.257f,
                    dy2 = 0.245f,
                    dx3 = 7.29f,
                    dy3 = -1.208f,
                )
                // a 0.5 0.5 0 0 1 0.698 0.117z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.698f,
                    dy1 = 0.117f,
                )
                close()
                // M 2.979 7.342
                moveTo(x = 2.979f, y = 7.342f)
                // A 0.5 0.5 0 0 0 2 7.146
                arcTo(
                    horizontalEllipseRadius = 0.5f,
                    verticalEllipseRadius = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    x1 = 2.0f,
                    y1 = 7.146f,
                )
                // c -0.129 0.64 0.123 1.21 0.478 1.651
                curveToRelative(
                    dx1 = -0.129f,
                    dy1 = 0.64f,
                    dx2 = 0.123f,
                    dy2 = 1.21f,
                    dx3 = 0.478f,
                    dy3 = 1.651f,
                )
                // c 0.354 0.44 0.847 0.8 1.326 1.063
                curveToRelative(
                    dx1 = 0.354f,
                    dy1 = 0.44f,
                    dx2 = 0.847f,
                    dy2 = 0.8f,
                    dx3 = 1.326f,
                    dy3 = 1.063f,
                )
                // l 0.011 0.006
                lineToRelative(dx = 0.011f, dy = 0.006f)
                // c 2.473 1.284 5.65 1.124 8.228 0.158
                curveToRelative(
                    dx1 = 2.473f,
                    dy1 = 1.284f,
                    dx2 = 5.65f,
                    dy2 = 1.124f,
                    dx3 = 8.228f,
                    dy3 = 0.158f,
                )
                // a 0.5 0.5 0 0 0 -0.35 -0.937
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.35f,
                    dy1 = -0.937f,
                )
                // c -2.399 0.9 -5.263 1.007 -7.412 -0.106
                curveToRelative(
                    dx1 = -2.399f,
                    dy1 = 0.9f,
                    dx2 = -5.263f,
                    dy2 = 1.007f,
                    dx3 = -7.412f,
                    dy3 = -0.106f,
                )
                // c -0.407 -0.224 -0.78 -0.507 -1.024 -0.811
                curveToRelative(
                    dx1 = -0.407f,
                    dy1 = -0.224f,
                    dx2 = -0.78f,
                    dy2 = -0.507f,
                    dx3 = -1.024f,
                    dy3 = -0.811f,
                )
                // c -0.245 -0.304 -0.327 -0.579 -0.277 -0.828z
                curveToRelative(
                    dx1 = -0.245f,
                    dy1 = -0.304f,
                    dx2 = -0.327f,
                    dy2 = -0.579f,
                    dx3 = -0.277f,
                    dy3 = -0.828f,
                )
                close()
                // m 10.127 4.42
                moveToRelative(dx = 10.127f, dy = 4.42f)
                // a 0.5 0.5 0 0 1 -0.201 0.678
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = -0.201f,
                    dy1 = 0.678f,
                )
                // c -2.168 1.175 -4.853 1.278 -7.12 0.203
                curveToRelative(
                    dx1 = -2.168f,
                    dy1 = 1.175f,
                    dx2 = -4.853f,
                    dy2 = 1.278f,
                    dx3 = -7.12f,
                    dy3 = 0.203f,
                )
                // a 0.5 0.5 0 1 1 0.43 -0.903
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = 0.43f,
                    dy1 = -0.903f,
                )
                // c 1.971 0.935 4.32 0.846 6.213 -0.18
                curveToRelative(
                    dx1 = 1.971f,
                    dy1 = 0.935f,
                    dx2 = 4.32f,
                    dy2 = 0.846f,
                    dx3 = 6.213f,
                    dy3 = -0.18f,
                )
                // a 0.5 0.5 0 0 1 0.678 0.202z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 0.678f,
                    dy1 = 0.202f,
                )
                close()
                // m 0.48 2.956
                moveToRelative(dx = 0.48f, dy = 2.956f)
                // a 0.5 0.5 0 0 0 -0.549 -0.836
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.549f,
                    dy1 = -0.836f,
                )
                // c -0.61 0.4 -1.124 0.629 -1.641 0.727
                curveToRelative(
                    dx1 = -0.61f,
                    dy1 = 0.4f,
                    dx2 = -1.124f,
                    dy2 = 0.629f,
                    dx3 = -1.641f,
                    dy3 = 0.727f,
                )
                // c -0.516 0.097 -1.073 0.071 -1.781 -0.096
                curveToRelative(
                    dx1 = -0.516f,
                    dy1 = 0.097f,
                    dx2 = -1.073f,
                    dy2 = 0.071f,
                    dx3 = -1.781f,
                    dy3 = -0.096f,
                )
                // a 0.5 0.5 0 0 0 -0.23 0.974
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = -0.23f,
                    dy1 = 0.974f,
                )
                // c 0.803 0.188 1.507 0.235 2.197 0.104
                curveToRelative(
                    dx1 = 0.803f,
                    dy1 = 0.188f,
                    dx2 = 1.507f,
                    dy2 = 0.235f,
                    dx3 = 2.197f,
                    dy3 = 0.104f,
                )
                // c 0.688 -0.13 1.325 -0.428 2.003 -0.873z
                curveToRelative(
                    dx1 = 0.688f,
                    dy1 = -0.13f,
                    dx2 = 1.325f,
                    dy2 = -0.428f,
                    dx3 = 2.003f,
                    dy3 = -0.873f,
                )
                close()
                // M 13.5 9
                moveTo(x = 13.5f, y = 9.0f)
                // a 0.5 0.5 0 1 1 -1 0
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = true,
                    dx1 = -1.0f,
                    dy1 = 0.0f,
                )
                // a 0.5 0.5 0 0 1 1 0z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = true,
                    dx1 = 1.0f,
                    dy1 = 0.0f,
                )
                close()
                // m -10 -5
                moveToRelative(dx = -10.0f, dy = -5.0f)
                // a 0.5 0.5 0 1 0 0 -1
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = true,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = -1.0f,
                )
                // a 0.5 0.5 0 0 0 0 1z
                arcToRelative(
                    a = 0.5f,
                    b = 0.5f,
                    theta = 0.0f,
                    isMoreThanHalf = false,
                    isPositiveArc = false,
                    dx1 = 0.0f,
                    dy1 = 1.0f,
                )
                close()
            }
        }.build().also { _ic2210 = it }
    }

@Suppress("ObjectPropertyName")
private var _ic2210: ImageVector? = null
