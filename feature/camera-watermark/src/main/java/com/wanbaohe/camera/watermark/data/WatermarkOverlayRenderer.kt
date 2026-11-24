/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.wanbaohe.camera.watermark.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import androidx.camera.effects.Frame
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.wanbaohe.camera.watermark.R
import com.wanbaohe.camera.watermark.domain.CameraWatermarkStyle
import com.wanbaohe.watermark.template.R as TemplateR
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.min

data class WatermarkOverlayState(
    val style: CameraWatermarkStyle = CameraWatermarkStyle.NONE,
    val deviceName: String = "",
    val cameraParameters: String = "",
    val locationLabel: String = "",
    val coordinatesLabel: String = "",
    val altitudeLabel: String = "",
    val weatherLabel: String = "",
    val weatherDetailsLabel: String = "",
    val showLocation: Boolean = false,
    val showAltitude: Boolean = true,
    val showWeather: Boolean = true,
    val showTimestamp: Boolean = true,
    val showCameraParameters: Boolean = true,
    val hideUnavailableInfo: Boolean = false,
)

class WatermarkOverlayRenderer @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val appContext = context.applicationContext
    private val leicaLogo: Drawable? = ContextCompat.getDrawable(appContext, TemplateR.drawable.leica_logo)
    private val sunIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_sun)
    private val cloudIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_cloud)
    private val rainIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_rain)
    private val snowIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_snow)
    private val windIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_wind)
    private val altitudeIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_altitude)
    private val locationIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_location)
    private val coordinatesIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_coordinates)
    private val timeIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_time)
    private val apertureIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_watermark_aperture)
    private var lastTimestampSecond = -1L
    private var timestamp = ""
    private var displayTimestamp = ""
    private var signatureDate = ""

    @Volatile
    private var topLuminance = 0.35f

    @Volatile
    private var bottomLuminance = 0.35f

    fun updateLuminance(top: Float, bottom: Float) {
        topLuminance = top.coerceIn(0f, 1f)
        bottomLuminance = bottom.coerceIn(0f, 1f)
    }

    fun draw(frame: Frame, state: WatermarkOverlayState): Boolean {
        val canvas = frame.overlayCanvas
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (state.style == CameraWatermarkStyle.NONE) return true
        updateTime()
        val crop = frame.cropRect
        val saveCount = canvas.save()
        canvas.clipRect(crop)
        canvas.translate(crop.exactCenterX(), crop.exactCenterY())
        canvas.rotate(-frame.rotationDegrees.toFloat())
        if (frame.isMirroring) canvas.scale(-1f, 1f)
        val rotated = frame.rotationDegrees == 90 || frame.rotationDegrees == 270
        val width = (if (rotated) crop.height() else crop.width()).toFloat()
        val height = (if (rotated) crop.width() else crop.height()).toFloat()
        val bounds = RectF(-width / 2f, -height / 2f, width / 2f, height / 2f)
        drawContent(canvas, bounds, state, topLuminance, bottomLuminance, isPreview = true)
        canvas.restoreToCount(saveCount)
        return true
    }

    fun drawPhoto(canvas: Canvas, bitmap: Bitmap, state: WatermarkOverlayState) {
        if (state.style == CameraWatermarkStyle.NONE) return
        updateTime()
        drawContent(
            canvas = canvas,
            bounds = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()),
            state = state,
            topLuminance = sampleBitmapLuminance(bitmap, 0.02f, 0.16f),
            bottomLuminance = sampleBitmapLuminance(bitmap, 0.8f, 0.98f),
            isPreview = false,
        )
    }

    private fun drawContent(
        canvas: Canvas,
        bounds: RectF,
        state: WatermarkOverlayState,
        topLuminance: Float,
        bottomLuminance: Float,
        isPreview: Boolean,
    ) {
        when (state.style) {
            CameraWatermarkStyle.NONE -> Unit
            CameraWatermarkStyle.LEICA -> drawLeica(canvas, bounds, state, isPreview)
            CameraWatermarkStyle.TIMESTAMP -> {
                val location = state.locationLabel.ifBlank { state.coordinatesLabel }
                val primary = if (state.showTimestamp) {
                    timestamp
                } else {
                    location.displayValue(appContext.getString(R.string.camera_location_not_available), state)
                }
                val secondary = if (state.showLocation) {
                    location.displayValue(appContext.getString(R.string.camera_location_not_available), state)
                } else {
                    state.deviceName
                }
                if (primary.isNotBlank() || secondary.isNotBlank()) {
                    drawCaption(canvas, bounds, primary, secondary, isPreview)
                }
            }
            CameraWatermarkStyle.MINIMAL -> drawMinimal(
                canvas = canvas,
                bounds = bounds,
                state = state,
                topLuminance = topLuminance,
                bottomLuminance = bottomLuminance,
                isPreview = isPreview,
            )
            CameraWatermarkStyle.COORDINATES -> {
                val primary = if (state.showLocation) {
                    state.coordinatesLabel.ifBlank { state.locationLabel }
                        .displayValue("--°N --°E", state)
                } else {
                    state.deviceName
                }
                val secondary = if (state.showTimestamp) timestamp else ""
                if (primary.isNotBlank() || secondary.isNotBlank()) {
                    drawCaption(canvas, bounds, primary, secondary, isPreview)
                }
            }
            CameraWatermarkStyle.SIGNATURE -> drawSignature(canvas, bounds, state, isPreview)
        }
    }

    private fun updateTime() {
        val now = System.currentTimeMillis()
        val second = now / 1000L
        if (second == lastTimestampSecond) return
        lastTimestampSecond = second
        val date = Date(now)
        timestamp = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(date)
        displayTimestamp = SimpleDateFormat("yyyy.MM.dd E HH:mm", Locale.getDefault()).format(date)
        signatureDate = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(date)
    }

    private fun drawMinimal(
        canvas: Canvas,
        bounds: RectF,
        state: WatermarkOverlayState,
        topLuminance: Float,
        bottomLuminance: Float,
        isPreview: Boolean,
    ) {
        val width = bounds.width()
        val height = bounds.height()
        val margin = 12f * appContext.resources.displayMetrics.density
        val textReference = if (isPreview) width else minOf(width, height)
        val textSize = (textReference * 0.031f).coerceAtLeast(12f)
        val secondarySize = textSize * 0.84f
        val topBaseline = if (isPreview) {
            bounds.top + height * 0.14f
        } else {
            bounds.top + margin + textSize
        }
        val bottomBaseline = if (isPreview) bounds.bottom - height * 0.29f else bounds.bottom - margin
        val topRightWidth = width * if (isPreview) 0.3f else 0.32f
        val bottomRightWidth = width * if (isPreview) 0.36f else 0.38f
        val topTone = WatermarkTone.forLuminance(topLuminance)
        val bottomTone = WatermarkTone.forLuminance(bottomLuminance)
        if (state.showWeather) {
            drawMinimalBlock(
                canvas = canvas,
                x = bounds.left + margin,
                baseline = topBaseline,
                maxWidth = width * 0.43f,
                primary = state.weatherLabel.displayValue(
                    appContext.getString(R.string.camera_weather_unavailable),
                    state,
                ),
                secondary = state.weatherDetailsLabel.displayValue(
                    appContext.getString(R.string.camera_weather_details_unavailable),
                    state,
                ),
                primaryIcon = weatherIconFor(state.weatherLabel),
                secondaryIcon = windIcon,
                textSize = textSize,
                secondarySize = secondarySize,
                tone = topTone,
            )
        }
        if (state.showAltitude || state.showLocation) {
            val altitude = if (state.showAltitude) {
                state.altitudeLabel.displayValue(
                    appContext.getString(R.string.camera_altitude_unavailable),
                    state,
                )
            } else {
                ""
            }
            val coordinates = if (state.showLocation) {
                state.coordinatesLabel.displayValue("--°N --°E", state)
            } else {
                ""
            }
            drawMinimalBlock(
                canvas = canvas,
                x = bounds.right - margin,
                baseline = topBaseline,
                maxWidth = topRightWidth,
                alignEnd = true,
                primary = altitude,
                secondary = coordinates,
                primaryIcon = altitudeIcon,
                secondaryIcon = coordinatesIcon,
                textSize = textSize,
                secondarySize = secondarySize,
                tone = topTone,
            )
        }
        if (state.showLocation || state.showTimestamp) {
            val location = if (state.showLocation) {
                state.locationLabel.ifBlank { state.coordinatesLabel }.displayValue(
                    appContext.getString(R.string.camera_location_not_available),
                    state,
                )
            } else {
                ""
            }
            val time = if (state.showTimestamp) displayTimestamp else ""
            drawMinimalBlock(
                canvas = canvas,
                x = bounds.left + margin,
                baseline = bottomBaseline,
                maxWidth = width * if (isPreview) 0.56f else 0.52f,
                bottomAligned = !isPreview,
                primary = location,
                secondary = time,
                primaryIcon = locationIcon,
                secondaryIcon = timeIcon,
                textSize = textSize,
                secondarySize = secondarySize,
                tone = bottomTone,
            )
        }
        if (state.showCameraParameters) {
            drawMinimalBlock(
                canvas = canvas,
                x = bounds.right - margin,
                baseline = if (isPreview) bottomBaseline + textSize * 0.45f else bottomBaseline,
                maxWidth = bottomRightWidth,
                alignEnd = true,
                bottomAligned = !isPreview,
                primary = state.cameraParameters.displayValue("--", state),
                secondary = "",
                primaryIcon = apertureIcon,
                secondaryIcon = null,
                textSize = secondarySize,
                secondarySize = secondarySize,
                tone = bottomTone,
            )
        }
    }

    private fun drawMinimalBlock(
        canvas: Canvas,
        x: Float,
        baseline: Float,
        maxWidth: Float,
        primary: String,
        secondary: String,
        primaryIcon: Drawable?,
        secondaryIcon: Drawable?,
        textSize: Float,
        secondarySize: Float,
        tone: WatermarkTone,
        alignEnd: Boolean = false,
        bottomAligned: Boolean = false,
    ) {
        val lines = listOf(
            Triple(primary, textSize, primaryIcon),
            Triple(secondary, secondarySize, secondaryIcon),
        ).filter { it.first.isNotBlank() }
        if (lines.isEmpty()) return
        val lineHeight = textSize * 1.12f
        val blockWidth = lines.maxOf { (text, size, icon) ->
            val iconWidth = if (icon == null) 0f else size * 1.35f
            textPaint(Color.WHITE, size, false).measureText(text) + iconWidth
        }.coerceAtMost(maxWidth)
        val startX = if (alignEnd) x - blockWidth else x
        val firstBaseline = if (bottomAligned) {
            baseline - (lines.size - 1) * lineHeight - lines.last().second * 0.25f
        } else {
            baseline
        }
        val top = firstBaseline - textSize
        val bottom = firstBaseline + (lines.size - 1) * lineHeight + textSize * 0.35f
        val scrim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                startX - textSize,
                0f,
                startX + blockWidth + textSize * 2f,
                0f,
                tone.scrimColor,
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(
            startX - textSize,
            top - textSize * 0.5f,
            startX + blockWidth + textSize * 2f,
            bottom + textSize * 0.5f,
            scrim,
        )
        lines.forEachIndexed { index, (text, size, icon) ->
            val y = firstBaseline + index * lineHeight
            val iconWidth = if (icon == null) 0f else size * 1.35f
            if (icon != null) {
                drawWatermarkIcon(
                    canvas = canvas,
                    icon = icon,
                    x = startX,
                    baseline = y,
                    textSize = size,
                    color = tone.textColor,
                )
            }
            drawContrastText(
                canvas = canvas,
                text = text,
                x = startX + iconWidth,
                y = y,
                maxWidth = blockWidth - iconWidth,
                textSize = size,
                tone = tone,
            )
        }
    }

    private fun weatherIconFor(label: String): Drawable? = when {
        label.contains("雪") || label.contains("snow", ignoreCase = true) -> snowIcon
        label.contains("雨") || label.contains("雷") ||
            label.contains("rain", ignoreCase = true) ||
            label.contains("storm", ignoreCase = true) -> rainIcon
        label.contains("云") || label.contains("阴") ||
            label.contains("cloud", ignoreCase = true) ||
            label.contains("overcast", ignoreCase = true) -> cloudIcon
        else -> sunIcon
    }

    private fun drawWatermarkIcon(
        canvas: Canvas,
        icon: Drawable,
        x: Float,
        baseline: Float,
        textSize: Float,
        color: Int,
    ) {
        val iconSize = textSize * 1.05f
        val metrics = textPaint(Color.WHITE, textSize, false).fontMetrics
        val textCenter = baseline + (metrics.ascent + metrics.descent) / 2f
        val left = x.toInt()
        val top = (textCenter - iconSize / 2f).toInt()
        val right = left + iconSize.toInt()
        val bottom = top + iconSize.toInt()
        DrawableCompat.setTint(icon, color)
        icon.setBounds(left, top, right, bottom)
        icon.draw(canvas)
    }

    private fun drawLeica(
        canvas: Canvas,
        bounds: RectF,
        state: WatermarkOverlayState,
        isPreview: Boolean,
    ) {
        val width = bounds.width()
        val bandHeight = min(width * 0.13f, bounds.height() * 0.24f)
        val bandBottom = bounds.bottom - if (isPreview) bounds.height() * 0.29f else 0f
        val band = RectF(bounds.left, bandBottom - bandHeight, bounds.right, bandBottom)
        canvas.drawRect(band, paint(Color.WHITE))
        val horizontalPadding = bandHeight * 0.18f
        val primarySize = bandHeight * 0.22f
        val secondarySize = bandHeight * 0.15f
        val left = band.left + horizontalPadding
        val rightSectionWidth = band.width() * 0.32f
        val dividerX = band.right - rightSectionWidth - horizontalPadding
        val logoSize = bandHeight * 0.52f
        val logoCenterX = dividerX - bandHeight * 0.55f
        val centerY = band.centerY()
        leicaLogo?.let { logo ->
            val ratio = if (logo.intrinsicHeight > 0) {
                logo.intrinsicWidth.toFloat() / logo.intrinsicHeight
            } else {
                1f
            }
            val logoWidth = logoSize * ratio
            logo.setBounds(
                (logoCenterX - logoWidth / 2f).toInt(),
                (centerY - logoSize / 2f).toInt(),
                (logoCenterX + logoWidth / 2f).toInt(),
                (centerY + logoSize / 2f).toInt(),
            )
            logo.draw(canvas)
        }
        canvas.drawLine(
            dividerX,
            band.top + bandHeight * 0.2f,
            dividerX,
            band.bottom - bandHeight * 0.2f,
            paint(Color.rgb(224, 224, 224)).apply {
                strokeWidth = (bandHeight * 0.012f).coerceAtLeast(1f)
            },
        )
        val leftWidth = logoCenterX - logoSize - left - horizontalPadding
        drawFittedText(
            canvas, state.deviceName.uppercase(Locale.ROOT), left, band.top + bandHeight * 0.42f,
            leftWidth, primarySize, Color.rgb(26, 26, 26), true,
        )
        if (state.showTimestamp) {
            drawFittedText(
                canvas, timestamp, left, band.top + bandHeight * 0.72f,
                leftWidth, secondarySize, Color.rgb(102, 102, 102),
            )
        }
        val rightX = dividerX + horizontalPadding
        if (state.showCameraParameters) {
            drawFittedText(
                canvas, state.cameraParameters, rightX, band.top + bandHeight * 0.42f,
                band.right - rightX - horizontalPadding, primarySize, Color.rgb(26, 26, 26), true,
            )
        }
        if (state.showLocation) {
            drawFittedText(
                canvas,
                state.locationLabel.ifBlank { state.coordinatesLabel },
                rightX,
                band.top + bandHeight * 0.72f,
                band.right - rightX - horizontalPadding,
                secondarySize,
                Color.rgb(102, 102, 102),
            )
        }
    }

    private fun drawCaption(
        canvas: Canvas,
        bounds: RectF,
        primary: String,
        secondary: String,
        isPreview: Boolean,
    ) {
        val margin = bounds.width() * 0.035f
        val bottomMargin = if (isPreview) bounds.height() * 0.29f else margin
        val textSize = (bounds.width() * 0.028f).coerceAtLeast(12f)
        val primaryPaint = textPaint(Color.WHITE, textSize, true)
        val secondaryPaint = textPaint(Color.rgb(220, 220, 220), textSize * 0.72f, false)
        val contentWidth = maxOf(
            primaryPaint.measureText(primary),
            secondaryPaint.measureText(secondary),
        ).coerceAtMost(bounds.width() * 0.72f)
        val padding = textSize * 0.65f
        val height = padding * 2f + textSize * 1.85f
        val background = RectF(
            bounds.left + margin,
            bounds.bottom - bottomMargin - height,
            bounds.left + margin + contentWidth + padding * 2f,
            bounds.bottom - bottomMargin,
        )
        canvas.drawRoundRect(background, textSize * 0.45f, textSize * 0.45f, paint(Color.argb(150, 0, 0, 0)))
        if (primary.isNotBlank()) {
            canvas.drawText(primary, background.left + padding, background.top + padding + textSize, primaryPaint)
        }
        if (secondary.isNotBlank()) {
            canvas.drawText(secondary, background.left + padding, background.bottom - padding * 0.7f, secondaryPaint)
        }
    }

    private fun drawSignature(
        canvas: Canvas,
        bounds: RectF,
        state: WatermarkOverlayState,
        isPreview: Boolean,
    ) {
        val text = if (state.showTimestamp) "© OneBox  $signatureDate" else "© OneBox"
        val textSize = (bounds.width() * 0.025f).coerceAtLeast(12f)
        val paint = textPaint(Color.WHITE, textSize, false).apply {
            textAlign = Paint.Align.RIGHT
            setShadowLayer(textSize * 0.16f, 0f, 1f, Color.BLACK)
        }
        val bottomMargin = if (isPreview) bounds.height() * 0.29f else bounds.width() * 0.035f
        canvas.drawText(text, bounds.right - bounds.width() * 0.035f, bounds.bottom - bottomMargin, paint)
    }

    private fun drawContrastText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        textSize: Float,
        tone: WatermarkTone,
    ) {
        if (text.isBlank() || maxWidth <= 0f) return
        val fill = textPaint(tone.textColor, textSize, false)
        val fitted = TextUtils.ellipsize(text, fill, maxWidth, TextUtils.TruncateAt.END).toString()
        val stroke = textPaint(tone.strokeColor, textSize, false).apply {
            style = Paint.Style.STROKE
            strokeWidth = (textSize * 0.035f).coerceAtLeast(0.6f)
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawText(fitted, x, y, stroke)
        canvas.drawText(fitted, x, y, fill)
    }

    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        textSize: Float,
        color: Int,
        bold: Boolean = false,
    ) {
        if (text.isBlank() || maxWidth <= 0f) return
        val paint = textPaint(color, textSize, bold)
        val fitted = TextUtils.ellipsize(text, paint, maxWidth, TextUtils.TruncateAt.END).toString()
        canvas.drawText(fitted, x, y, paint)
    }

    private fun sampleBitmapLuminance(bitmap: Bitmap, startFraction: Float, endFraction: Float): Float {
        val startY = (bitmap.height * startFraction).toInt().coerceIn(0, bitmap.height - 1)
        val endY = (bitmap.height * endFraction).toInt().coerceIn(startY + 1, bitmap.height)
        val stepY = ((endY - startY) / 8).coerceAtLeast(1)
        val stepX = (bitmap.width / 12).coerceAtLeast(1)
        var sum = 0L
        var count = 0
        for (y in startY until endY step stepY) {
            for (x in 0 until bitmap.width step stepX) {
                val color = bitmap.getPixel(x, y)
                sum += (Color.red(color) * 0.2126 + Color.green(color) * 0.7152 + Color.blue(color) * 0.0722).toLong()
                count++
            }
        }
        return if (count == 0) 0.35f else sum.toFloat() / count / 255f
    }

    private fun String.displayValue(placeholder: String, state: WatermarkOverlayState): String =
        ifBlank { if (state.hideUnavailableInfo) "" else placeholder }

    private fun paint(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private fun textPaint(color: Int, size: Float, bold: Boolean) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        textAlign = Paint.Align.LEFT
        textSize = size
        typeface = android.graphics.Typeface.create(
            android.graphics.Typeface.DEFAULT,
            if (bold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL,
        )
    }

    private data class WatermarkTone(
        val textColor: Int,
        val strokeColor: Int,
        val scrimColor: Int,
    ) {
        companion object {
            fun forLuminance(luminance: Float): WatermarkTone = if (luminance > 0.6f) {
                WatermarkTone(Color.rgb(24, 28, 32), Color.argb(150, 255, 255, 255), Color.argb(36, 255, 255, 255))
            } else {
                WatermarkTone(Color.WHITE, Color.argb(150, 0, 0, 0), Color.argb(48, 0, 0, 0))
            }
        }
    }
}
