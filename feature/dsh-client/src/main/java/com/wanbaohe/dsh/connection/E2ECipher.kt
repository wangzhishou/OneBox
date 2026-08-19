package com.wanbaohe.dsh.connection

import android.util.Base64
import com.wanbaohe.dsh.wire.CarrierException
import com.wanbaohe.dsh.wire.DshJson
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 云端中继端到端加密(App ↔ Mac connector,网关只见密文)。
 *
 * 契约(与 connector 端 JS 实现严格一致):
 * - 密钥:扫码 QR 的 k 参数,base64url(无填充,43 字符)解码 = 32 字节,
 *   直接作 AES-256-GCM 密钥
 * - 每条消息 12 字节随机 nonce;密文帧 = JSON 字符串
 *   `{"e2e":1,"d":"<base64url无填充(nonce ‖ ciphertext ‖ tag16)>"}`
 *   (nonce 头、密文中、GCM tag 尾;JCE doFinal 输出即 ct‖tag 布局)
 * - 发送必加密(持有 key 时);接收宽容:不是带 "e2e" 字段的 JSON 就按明文
 *   原样返回(兼容旧插件/明文);有标记但解密失败抛 [CarrierException]
 *
 * 不进 Hilt:由组件层按当前连接条目的密钥现构造,注入 ApiClient / Downlink。
 */
class E2ECipher private constructor(key: ByteArray) {

    private val secretKey = SecretKeySpec(key, "AES")
    private val random = SecureRandom()

    /** 明文 → 密文帧 JSON 字符串(`{"e2e":1,"d":"…"}`) */
    fun encryptText(plain: String): String {
        val nonce = ByteArray(NonceBytes).also(random::nextBytes)
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TagBits, nonce))
        val sealed = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val frame = buildJsonObject {
            put(FrameMarker, 1)
            put(FrameData, encodeBase64Url(nonce + sealed))
        }
        return frame.toString()
    }

    /**
     * 密文帧 → 明文;无 "e2e" 标记的文本按明文原样返回(接收宽容)。
     * 有标记但帧畸形/解密失败(密钥不符、被篡改)抛 [CarrierException]。
     */
    fun decryptText(text: String): String {
        val element = runCatching { DshJson.parseToJsonElement(text) }.getOrNull()
        val frame = element as? JsonObject ?: return text
        if (!frame.containsKey(FrameMarker)) return text
        val encoded = (frame[FrameData] as? JsonPrimitive)?.contentOrNull
            ?: throw CarrierException("e2e frame missing data")
        val payload = runCatching { decodeBase64Url(encoded) }.getOrElse {
            throw CarrierException("e2e frame bad base64", cause = it)
        }
        if (payload.size < NonceBytes + TagBits / 8) {
            throw CarrierException("e2e frame too short")
        }
        val nonce = payload.copyOfRange(0, NonceBytes)
        val sealed = payload.copyOfRange(NonceBytes, payload.size)
        val plain = runCatching {
            val cipher = Cipher.getInstance(Transformation)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TagBits, nonce))
            cipher.doFinal(sealed)
        }.getOrElse {
            throw CarrierException("e2e decrypt failed: ${it.message}", cause = it)
        }
        return String(plain, Charsets.UTF_8)
    }

    companion object {
        private const val Transformation = "AES/GCM/NoPadding"
        private const val KeyBytes = 32
        private const val NonceBytes = 12
        private const val TagBits = 128
        private const val FrameMarker = "e2e"
        private const val FrameData = "d"
        private const val EncodeFlags =
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        private const val DecodeFlags = Base64.URL_SAFE or Base64.NO_WRAP

        /**
         * 从扫码 QR 的 k 参数构造:base64url(无填充)解码必须正好 32 字节,
         * 否则返回 null(非法/被截断的密钥)。
         */
        fun fromBase64Url(keyB64: String): E2ECipher? {
            val key = runCatching { decodeBase64Url(keyB64.trim()) }.getOrNull()
                ?: return null
            if (key.size != KeyBytes) return null
            return E2ECipher(key)
        }

        /** base64url 无填充编码(编码/解码两侧标志组合保持一致) */
        internal fun encodeBase64Url(bytes: ByteArray): String =
            Base64.encodeToString(bytes, EncodeFlags)

        internal fun decodeBase64Url(text: String): ByteArray =
            Base64.decode(text, DecodeFlags)
    }
}
