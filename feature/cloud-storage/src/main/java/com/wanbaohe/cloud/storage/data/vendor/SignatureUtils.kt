package com.wanbaohe.cloud.storage.data.vendor

import android.util.Base64
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object SignatureUtils {

    fun sha256Hex(value: String): String = sha256Hex(value.toByteArray(StandardCharsets.UTF_8))

    fun sha256Hex(value: ByteArray): String = digestHex("SHA-256", value)

    fun sha1Hex(value: String): String = digestHex("SHA-1", value.toByteArray(StandardCharsets.UTF_8))

    fun hmacSha256(key: ByteArray, value: String): ByteArray = hmac("HmacSHA256", key, value)

    fun hmacSha1(key: ByteArray, value: String): ByteArray = hmac("HmacSHA1", key, value)

    fun hmacSha256Hex(key: ByteArray, value: String): String = hmacSha256(key, value).toHex()

    fun hmacSha1Hex(key: ByteArray, value: String): String = hmacSha1(key, value).toHex()

    fun hmacSha1Base64(key: String, value: String): String =
        Base64.encodeToString(hmacSha1(key.toByteArray(StandardCharsets.UTF_8), value), Base64.NO_WRAP)

    fun ByteArray.toHex(): String = joinToString(separator = "") { "%02x".format(it) }

    fun awsUriEncode(input: String, encodeSlash: Boolean): String {
        val bytes = input.toByteArray(StandardCharsets.UTF_8)
        val builder = StringBuilder(bytes.size * 2)
        bytes.forEach { byte ->
            val char = byte.toInt().toChar()
            val isSafe = char.isLetterOrDigit() || char == '-' || char == '_' || char == '.' || char == '~'
            when {
                isSafe -> builder.append(char)
                char == '/' && !encodeSlash -> builder.append(char)
                else -> builder.append('%').append("%02X".format(byte))
            }
        }
        return builder.toString()
    }

    fun standardQueryEncode(input: String): String =
        URLEncoder.encode(input, "UTF-8").replace("+", "%20").replace("*", "%2A").replace("%7E", "~")

    private fun digestHex(algorithm: String, value: ByteArray): String =
        MessageDigest.getInstance(algorithm).digest(value).toHex()

    private fun hmac(algorithm: String, key: ByteArray, value: String): ByteArray {
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, algorithm))
        return mac.doFinal(value.toByteArray(StandardCharsets.UTF_8))
    }
}
