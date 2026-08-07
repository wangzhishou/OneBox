package com.wanbaohe.camera.watermark.data

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.wanbaohe.camera.watermark.domain.WatermarkMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * EXIF 元数据解析器
 * 从图片中提取拍摄信息
 */
@Singleton
class ExifMetadataParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * 从 Uri 解析 EXIF 信息
     */
    fun parseFromUri(uri: Uri): WatermarkMetadata {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                parseFromExif(exif)
            } ?: WatermarkMetadata.EMPTY
        } catch (e: Exception) {
            e.printStackTrace()
            WatermarkMetadata.EMPTY
        }
    }

    /**
     * 从 ExifInterface 解析元数据
     */
    private fun parseFromExif(exif: ExifInterface): WatermarkMetadata {
        return WatermarkMetadata(
            cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE)?.trim() ?: "",
            cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL)?.trim() ?: "",
            lensMake = exif.getAttribute(ExifInterface.TAG_LENS_MAKE)?.trim() ?: "",
            lensModel = exif.getAttribute(ExifInterface.TAG_LENS_MODEL)?.trim() ?: "",
            focalLength = formatFocalLength(exif),
            aperture = formatAperture(exif),
            shutterSpeed = formatShutterSpeed(exif),
            iso = formatIso(exif),
            dateTime = formatDateTime(exif),
            latitude = formatLatitude(exif),
            longitude = formatLongitude(exif),
        )
    }

    /**
     * 格式化焦距: 120mm
     */
    private fun formatFocalLength(exif: ExifInterface): String {
        val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH) ?: return ""
        return try {
            val parts = focalLength.split("/")
            if (parts.size == 2) {
                val value = parts[0].toDouble() / parts[1].toDouble()
                "${value.toInt()}mm"
            } else {
                "${focalLength.toDouble().toInt()}mm"
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化光圈: f/4.1
     */
    private fun formatAperture(exif: ExifInterface): String {
        val fNumber = exif.getAttribute(ExifInterface.TAG_F_NUMBER) ?: return ""
        return try {
            val parts = fNumber.split("/")
            val value = if (parts.size == 2) {
                parts[0].toDouble() / parts[1].toDouble()
            } else {
                fNumber.toDouble()
            }
            "f/%.1f".format(value)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化快门速度: 1/100
     */
    private fun formatShutterSpeed(exif: ExifInterface): String {
        val exposureTime = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) ?: return ""
        return try {
            val parts = exposureTime.split("/")
            val value = if (parts.size == 2) {
                parts[0].toDouble() / parts[1].toDouble()
            } else {
                exposureTime.toDouble()
            }
            if (value >= 1) {
                "${value.toInt()}s"
            } else {
                "1/${(1 / value).toInt()}"
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化 ISO: ISO90
     */
    private fun formatIso(exif: ExifInterface): String {
        val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
            ?: exif.getAttribute(ExifInterface.TAG_ISO_SPEED)
            ?: return ""
        return try {
            "ISO${iso.toInt()}"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化日期时间: 2023.03.19 08:46:12
     */
    private fun formatDateTime(exif: ExifInterface): String {
        val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: return ""
        return try {
            // EXIF 格式: 2023:03:19 08:46:12
            val inputFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 格式化纬度: 40°3'13"N
     */
    private fun formatLatitude(exif: ExifInterface): String {
        val lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) ?: return ""
        val ref = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) ?: "N"
        return formatGpsCoordinate(lat, ref)
    }

    /**
     * 格式化经度: 116°19'25"E
     */
    private fun formatLongitude(exif: ExifInterface): String {
        val lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) ?: return ""
        val ref = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) ?: "E"
        return formatGpsCoordinate(lon, ref)
    }

    /**
     * 格式化 GPS 坐标
     * 输入: 40/1,3/1,13/1 -> 输出: 40°3'13"N
     */
    private fun formatGpsCoordinate(coordinate: String, ref: String): String {
        return try {
            val parts = coordinate.split(",")
            if (parts.size != 3) return ""

            val degrees = parseRational(parts[0])
            val minutes = parseRational(parts[1])
            val seconds = parseRational(parts[2])

            "${abs(degrees.toInt())}°${abs(minutes.toInt())}'${abs(seconds.toInt())}\"$ref"
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * 解析有理数: "40/1" -> 40.0
     */
    private fun parseRational(rational: String): Double {
        val parts = rational.trim().split("/")
        return if (parts.size == 2) {
            parts[0].toDouble() / parts[1].toDouble()
        } else {
            rational.toDouble()
        }
    }
}

