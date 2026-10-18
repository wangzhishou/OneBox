package com.wanbaohe.core.weather.data.location

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset

/**
 * 不申请定位权限时，用出口 IP 判断当前城市。
 * 国内走太平洋电脑网，海外走 ipwho.is。只取城市名或经纬度，不保留 IP。
 */
internal object NetworkCityLocator {

    data class Hit(
        val name: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
    )

    fun locate(): Hit? {
        locatePconline()?.let { return it }
        return locateIpwho()
    }

    private fun locatePconline(): Hit? {
        val body = get(
            url = "https://whois.pconline.com.cn/ipJson.jsp?json=true",
            charset = Charset.forName("GBK"),
        ) ?: return null
        val json = parseObject(body) ?: return null
        if (json.optString("err").isNotBlank()) return null
        val name = preferredName(json.optString("city"), json.optString("pro")) ?: return null
        return Hit(name = name)
    }

    private fun locateIpwho(): Hit? {
        val body = get(url = "https://ipwho.is/", charset = Charsets.UTF_8) ?: return null
        val json = parseObject(body) ?: return null
        if (!json.optBoolean("success", false)) return null
        val latitude = json.optDouble("latitude", Double.NaN)
        val longitude = json.optDouble("longitude", Double.NaN)
        if (latitude.isNaN() || longitude.isNaN()) return null
        return Hit(latitude = latitude, longitude = longitude)
    }

    private fun preferredName(city: String, province: String): String? {
        val cityName = city.trim()
        val provinceName = province.trim()
        return when {
            cityName.isNotEmpty() && cityName != "市辖区" && cityName != "0" -> cityName
            provinceName.isNotEmpty() && provinceName != "0" -> provinceName
            else -> null
        }
    }

    private fun parseObject(body: String): JSONObject? {
        val start = body.indexOf('{')
        val end = body.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return runCatching { JSONObject(body.substring(start, end + 1)) }.getOrNull()
    }

    private fun get(url: String, charset: Charset): String? = runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 4000
            readTimeout = 4000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "OneBox")
        }
        try {
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.use { it.readBytes().toString(charset) }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}
