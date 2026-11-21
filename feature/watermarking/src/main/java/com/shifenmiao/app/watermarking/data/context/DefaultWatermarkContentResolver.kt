/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package com.shifenmiao.app.watermarking.data.context

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.t8rin.imagetoolbox.core.domain.image.model.MetadataTag
import com.shifenmiao.app.watermarking.domain.model.ExifFieldKey
import com.shifenmiao.app.watermarking.domain.model.GeoPoint
import com.shifenmiao.app.watermarking.domain.model.LocationSnapshot
import com.shifenmiao.app.watermarking.domain.model.PhotoWatermarkContext
import com.shifenmiao.app.watermarking.domain.model.ResolvedWatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkContent
import com.shifenmiao.app.watermarking.domain.model.WatermarkDocument
import com.shifenmiao.app.watermarking.domain.model.WatermarkElement
import com.shifenmiao.app.watermarking.domain.model.WatermarkResolution
import com.shifenmiao.app.watermarking.domain.model.WatermarkValueSource
import com.shifenmiao.app.watermarking.domain.model.WeatherSnapshot
import com.shifenmiao.app.watermarking.domain.render.WatermarkContentResolver
import com.wanbaohe.core.weather.domain.repository.WeatherRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class DefaultWatermarkContentResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val weatherRepository: WeatherRepository,
    dispatchersHolder: DispatchersHolder,
) : WatermarkContentResolver, DispatchersHolder by dispatchersHolder {
    private val photoCache = mutableMapOf<String, PhotoWatermarkContext>()
    private val photoMutex = Mutex()
    private val weatherCache = mutableMapOf<String, WeatherSnapshot?>()
    private val weatherMutex = Mutex()

    override suspend fun resolve(
        uri: String,
        document: WatermarkDocument,
        deviceLocation: LocationSnapshot?,
    ): WatermarkResolution {
        val photo = photoContext(uri)
        var needsDeviceLocation = false
        var weatherAvailable = true
        val resolved = document.elements.map { element ->
            when (val content = element.content) {
                is WatermarkContent.Text -> element.text(content.text)
                is WatermarkContent.Stamp -> element.text(content.text)
                is WatermarkContent.Image -> {
                    val data = content.imageData.ifBlank {
                        content.builtinResId.takeIf { it != 0 }?.let { "builtin://$it" }
                    }
                    ResolvedWatermarkElement(
                        element = element,
                        imageData = data,
                        hasData = !data.isNullOrBlank(),
                    )
                }
                is WatermarkContent.Timestamp -> element.text(formatTimestamp(content, photo))
                is WatermarkContent.Altitude -> {
                    val photoValue = photo.altitudeMeters?.formatAltitude(content.useFeet)
                    val deviceValue = deviceLocation?.altitudeMeters?.formatAltitude(content.useFeet)
                    val value = when (content.source) {
                        WatermarkValueSource.MANUAL -> content.manualValue
                        WatermarkValueSource.PHOTO -> photoValue ?: deviceValue
                        WatermarkValueSource.DEVICE -> deviceValue
                    }
                    if (value.isNullOrBlank()) needsDeviceLocation = content.source != WatermarkValueSource.MANUAL
                    element.text(
                        value = value.orMissing(),
                        fromPhoto = content.source != WatermarkValueSource.DEVICE && photoValue != null,
                    )
                }
                is WatermarkContent.Coordinates -> {
                    val point = coordinatesFor(content.source, photo, deviceLocation)
                    val value = when (content.source) {
                        WatermarkValueSource.MANUAL -> content.manualValue
                        else -> point?.formatCoordinates(content.useDecimalFormat)
                    }
                    if (value.isNullOrBlank()) needsDeviceLocation = content.source != WatermarkValueSource.MANUAL
                    element.text(
                        value = value.orMissing(),
                        fromPhoto = content.source == WatermarkValueSource.PHOTO &&
                            photo.coordinates != null,
                    )
                }
                is WatermarkContent.Weather -> {
                    if (content.source == WatermarkValueSource.MANUAL) {
                        element.text(content.manualValue)
                    } else {
                        val point = coordinatesFor(content.source, photo, deviceLocation)
                        if (point == null) needsDeviceLocation = true
                        val weather = if (point == null) null else weatherAt(point)
                        if (weather == null) weatherAvailable = false
                        element.text(
                            value = weather?.format(content).orMissing(),
                            fromPhoto = content.source == WatermarkValueSource.PHOTO &&
                                photo.coordinates != null,
                        )
                    }
                }
                is WatermarkContent.CameraParams -> {
                    // 缺字段一律省略，不画 "--"、绝不填假值。
                    val value = formatCameraParams(content, photo)
                    ResolvedWatermarkElement(
                        element = element,
                        text = value,
                        hasData = value.isNotBlank(),
                        fromPhoto = true,
                    )
                }
                is WatermarkContent.Divider -> ResolvedWatermarkElement(
                    element = element,
                    hasData = true,
                )
            }
        }
        return WatermarkResolution(
            elements = resolved,
            photoContext = photo,
            needsDeviceLocation = needsDeviceLocation,
            weatherAvailable = weatherAvailable,
            frame = document.frame,
            mount = document.mount,
        )
    }

    private suspend fun photoContext(uri: String): PhotoWatermarkContext {
        photoMutex.withLock { photoCache[uri]?.let { return it } }
        val exif = withContext(ioDispatcher) {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                    ExifInterface(input)
                }
            }.getOrNull()
        }
        val context = PhotoWatermarkContext(
            dateTime = sequenceOf(
                MetadataTag.DatetimeOriginal,
                MetadataTag.DatetimeDigitized,
                MetadataTag.Datetime,
            ).mapNotNull { exif?.getAttribute(it.key)?.trim() }
                .firstOrNull { parseExifDate(it) != null },
            coordinates = parseCoordinates(
                latitude = exif?.getAttribute(MetadataTag.GpsLatitude.key),
                latitudeRef = exif?.getAttribute(MetadataTag.GpsLatitudeRef.key),
                longitude = exif?.getAttribute(MetadataTag.GpsLongitude.key),
                longitudeRef = exif?.getAttribute(MetadataTag.GpsLongitudeRef.key),
            ),
            altitudeMeters = parseRational(exif?.getAttribute(MetadataTag.GpsAltitude.key))?.let { altitude ->
                if (exif?.getAttribute(MetadataTag.GpsAltitudeRef.key)?.trim() == "1") -altitude else altitude
            },
            cameraMake = exif?.getAttribute(MetadataTag.Make.key)?.trim()?.takeIf(String::isNotEmpty),
            cameraModel = exif?.getAttribute(MetadataTag.Model.key)?.trim()?.takeIf(String::isNotEmpty),
            lensModel = exif?.getAttribute(MetadataTag.LensModel.key)?.trim()?.takeIf(String::isNotEmpty),
            focalLength = parseRational(exif?.getAttribute(MetadataTag.FocalLength.key))
                ?.let { "%.0fmm".format(Locale.US, it) },
            focalLength35mm = exif?.getAttribute(MetadataTag.FocalLengthIn35mmFilm.key)
                ?.trim()?.toIntOrNull()?.let { "${it}mm" },
            aperture = exif?.getAttribute(MetadataTag.FNumber.key).let(::formatFNumber),
            shutter = exif?.getAttribute(MetadataTag.ExposureTime.key).let(::formatShutter),
            iso = (
                exif?.getAttribute(MetadataTag.PhotographicSensitivity.key)
                    ?: exif?.getAttribute(ISO_SPEED_RATINGS)
                    ?: exif?.getAttribute(MetadataTag.IsoSpeed.key)
                )?.trim()?.takeIf(String::isNotEmpty)?.let { "ISO$it" },
        )
        photoMutex.withLock { photoCache[uri] = context }
        return context
    }

    private suspend fun weatherAt(point: GeoPoint): WeatherSnapshot? {
        val key = "%.3f,%.3f".format(Locale.US, point.latitude, point.longitude)
        weatherMutex.withLock {
            if (weatherCache.containsKey(key)) return weatherCache[key]
        }
        val snapshot = weatherRepository.getPlaceWeather(point.latitude, point.longitude)
            .getOrNull()?.current?.let { current ->
                WeatherSnapshot(
                    text = current.text,
                    temperatureCelsius = current.temp,
                    observationTime = current.obsTime.ifBlank { current.updateTime },
                    iconCode = current.icon,
                )
            }
        if (snapshot != null) {
            weatherMutex.withLock { weatherCache[key] = snapshot }
        }
        return snapshot
    }

    private fun coordinatesFor(
        source: WatermarkValueSource,
        photo: PhotoWatermarkContext,
        device: LocationSnapshot?,
    ): GeoPoint? = when (source) {
        WatermarkValueSource.PHOTO -> photo.coordinates ?: device?.coordinates
        WatermarkValueSource.DEVICE -> device?.coordinates
        WatermarkValueSource.MANUAL -> null
    }

    /**
     * 拍摄模式优先用照片的 EXIF 时间，缺失时才用手填值，不用当前时间冒充。
     * usePhotoTime=false 是「当前时间」语义，now() 合法。
     */
    private fun formatTimestamp(
        content: WatermarkContent.Timestamp,
        photo: PhotoWatermarkContext,
    ): String {
        if (!content.usePhotoTime) return formatDate(Date(), content.format)
        val date = photo.dateTime?.let(::parseExifDate)
            ?: return content.fallbackText.trim().lineSequence().firstOrNull().orEmpty()
        return formatDate(date, content.format)
    }

    private fun formatDate(date: Date, pattern: String): String =
        runCatching { SimpleDateFormat(pattern.ifBlank { "yyyy-MM-dd HH:mm" }, Locale.US).format(date) }
            .getOrElse { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(date) }

    private fun parseExifDate(value: String): Date? {
        val position = ParsePosition(0)
        val parser = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).apply { isLenient = false }
        return parser.parse(value, position)?.takeIf { position.index == value.length }
    }

    private fun formatCameraParams(
        content: WatermarkContent.CameraParams,
        photo: PhotoWatermarkContext,
    ): String {
        val cameraMake = if (ExifFieldKey.CAMERA_MAKE in content.fields) {
            content.manualOverrides[ExifFieldKey.CAMERA_MAKE.name]
                ?.takeIf(String::isNotBlank) ?: photo.cameraMake
        } else {
            null
        }
        return buildList {
            content.fields.forEach { field ->
                val manual = content.manualOverrides[field.name]?.takeIf(String::isNotBlank)
                val rawValue = manual ?: photo.valueOf(field)
                val value = if (field == ExifFieldKey.CAMERA_MODEL) {
                    rawValue?.removeCameraMake(cameraMake)
                } else {
                    rawValue
                }
                when {
                    !value.isNullOrBlank() -> add(value)
                    !content.omitMissing -> add("—")
                }
            }
        }.joinToString(content.separator)
    }

    private fun String.removeCameraMake(cameraMake: String?): String {
        val prefix = cameraMake?.trim().orEmpty()
        if (prefix.isEmpty() || !startsWith(prefix, ignoreCase = true)) return this
        return substring(prefix.length)
            .trimStart(' ', '-', '–', '—', '_', ',', ':')
            .ifBlank { this }
    }

    /** EXIF 的 FNumber 常见 "4.1" / "f/4.1" / "41/10"，统一成 f/x.x 且锁定 Locale.US。 */
    private fun formatFNumber(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val value = raw.trim().removePrefix("f/").removePrefix("F/")
        val numeric = parseRational(value) ?: return raw.trim()
        return "f/%.1f".format(Locale.US, numeric)
    }

    /** ExposureTime 是秒的 rational，>=1s 用 Ns，否则用 1/N。 */
    private fun formatShutter(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val value = parseRational(raw) ?: return raw.trim()
        if (value <= 0.0) return null
        return if (value >= 1.0) {
            "%.1fs".format(Locale.US, value)
        } else {
            "1/%.0f".format(Locale.US, 1.0 / value)
        }
    }

    private fun WeatherSnapshot.format(content: WatermarkContent.Weather): String = buildString {
        append(text)
        if (content.includeTemperature && temperatureCelsius.isNotBlank()) {
            if (isNotEmpty()) append(' ')
            append(temperatureCelsius).append("°C")
        }
        if (content.includeObservationTime && observationTime.isNotBlank()) {
            if (isNotEmpty()) append(" · ")
            append(observationTime)
        }
    }

    private fun Double.formatAltitude(useFeet: Boolean): String {
        val value = if (useFeet) this * 3.28084 else this
        return "≈ %.0f %s".format(Locale.US, value, if (useFeet) "ft" else "m")
    }

    private fun GeoPoint.formatCoordinates(decimal: Boolean): String {
        if (decimal) return "%.5f°, %.5f°".format(Locale.US, latitude, longitude)
        return "${dms(latitude, true)} ${dms(longitude, false)}"
    }

    private fun dms(value: Double, latitude: Boolean): String {
        val absolute = abs(value)
        val degrees = absolute.toInt()
        val minutesFloat = (absolute - degrees) * 60
        val minutes = minutesFloat.toInt()
        val seconds = (minutesFloat - minutes) * 60
        val direction = when {
            latitude && value >= 0 -> "N"
            latitude -> "S"
            value >= 0 -> "E"
            else -> "W"
        }
        return "%d°%d'%.1f\"%s".format(Locale.US, degrees, minutes, seconds, direction)
    }

    private fun parseCoordinates(
        latitude: String?,
        latitudeRef: String?,
        longitude: String?,
        longitudeRef: String?,
    ): GeoPoint? {
        val lat = parseDms(latitude) ?: return null
        val lon = parseDms(longitude) ?: return null
        return GeoPoint(
            latitude = if (latitudeRef.equals("S", true)) -lat else lat,
            longitude = if (longitudeRef.equals("W", true)) -lon else lon,
        ).takeIf { it.latitude in -90.0..90.0 && it.longitude in -180.0..180.0 }
    }

    private fun parseDms(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        val parts = value.split(',')
        if (parts.size == 3) {
            val degrees = parseRational(parts[0]) ?: return null
            val minutes = parseRational(parts[1]) ?: return null
            val seconds = parseRational(parts[2]) ?: return null
            return abs(degrees) + minutes / 60.0 + seconds / 3600.0
        }
        return value.toDoubleOrNull()?.let(::abs)
    }

    private fun parseRational(value: String?): Double? {
        if (value.isNullOrBlank()) return null
        val parts = value.trim().split('/')
        return when (parts.size) {
            2 -> {
                val numerator = parts[0].toDoubleOrNull() ?: return null
                val denominator = parts[1].toDoubleOrNull()?.takeIf { it != 0.0 } ?: return null
                numerator / denominator
            }
            else -> value.toDoubleOrNull()
        }
    }

    private fun WatermarkElement.text(
        value: String,
        fromPhoto: Boolean = true,
    ) = ResolvedWatermarkElement(
        element = this,
        text = value,
        hasData = value.isNotBlank() && value != "—",
        fromPhoto = fromPhoto,
    )

    // 缺数据统一用空串表达「省略」：visualFactory 会跳过空文本图层，
    // 绝不能回落成 "—"——那会让无 EXIF 图在导出成品里残留一个破折号。
    private fun String?.orMissing() = this?.takeIf(String::isNotBlank).orEmpty()

    private companion object {
        const val ISO_SPEED_RATINGS = "ISOSpeedRatings"
    }
}
