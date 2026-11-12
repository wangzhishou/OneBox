/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

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
