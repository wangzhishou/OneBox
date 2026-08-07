package com.wanbaohe.passwordvault.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * 密码保险箱专用加密器。
 *
 * 设计原则：
 * - 主密码不落盘，仅在内存中派生 AES-256 密钥。
 * - 每条记录使用独立的随机 salt 与 IV。
 * - 密文格式：salt(16) || iv(12) || ciphertext || authTag(16)。
 * - 使用 PBKDF2WithHmacSHA256 + AES/GCM/NoPadding。
 */
object VaultCrypto {

    private const val AES_KEY_SIZE_BITS = 256
    private const val GCM_IV_LENGTH_BYTES = 12
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val SALT_LENGTH_BYTES = 16
    private const val PBKDF2_ITERATION_COUNT = 120_000
    private const val ALGORITHM_PBE = "PBKDF2WithHmacSHA256"
    private const val ALGORITHM_CIPHER = "AES/GCM/NoPadding"
    private const val ALGORITHM_AES = "AES"

    class VaultKey internal constructor(
        internal val secretKey: SecretKey,
        internal val salt: ByteArray
    )

    class DecryptResult(
        val plaintext: String,
        val usedSalt: ByteArray
    )

    /**
     * 从主密码派生密钥。返回的 [VaultKey] 包含密钥与 salt（新 salt 或传入 salt）。
     */
    suspend fun deriveKey(
        masterPassword: String,
        existingSalt: ByteArray? = null
    ): VaultKey = withContext(Dispatchers.Default) {
        val salt = existingSalt ?: generateSalt()
        val factory = SecretKeyFactory.getInstance(ALGORITHM_PBE)
        val spec: KeySpec = PBEKeySpec(
            masterPassword.toCharArray(),
            salt,
            PBKDF2_ITERATION_COUNT,
            AES_KEY_SIZE_BITS
        )
        val tmp = factory.generateSecret(spec)
        VaultKey(
            secretKey = SecretKeySpec(tmp.encoded, ALGORITHM_AES),
            salt = salt
        )
    }

    /**
     * 使用派生密钥加密明文。
     * 返回的密文包含 salt + iv + ciphertext + authTag。
     */
    suspend fun encrypt(
        plaintext: String,
        key: VaultKey
    ): ByteArray = withContext(Dispatchers.Default) {
        val iv = generateIv()
        val cipher = Cipher.getInstance(ALGORITHM_CIPHER).apply {
            init(Cipher.ENCRYPT_MODE, key.secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        }
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        ByteBuffer.allocate(key.salt.size + iv.size + ciphertext.size)
            .put(key.salt)
            .put(iv)
            .put(ciphertext)
            .array()
    }

    /**
     * 使用主密码解密 [encryptedBundle]。
     * 从密文头部读取 salt，再派生密钥进行 AES-GCM 解密。
     * 如果主密码错误或数据被篡改，将抛出异常。
     */
    suspend fun decrypt(
        masterPassword: String,
        encryptedBundle: ByteArray
    ): DecryptResult = withContext(Dispatchers.Default) {
        if (encryptedBundle.size < SALT_LENGTH_BYTES + GCM_IV_LENGTH_BYTES + 1) {
            throw VaultWrongKeyException("Encrypted data is too short")
        }

        val buffer = ByteBuffer.wrap(encryptedBundle)
        val salt = ByteArray(SALT_LENGTH_BYTES)
        val iv = ByteArray(GCM_IV_LENGTH_BYTES)
        buffer.get(salt)
        buffer.get(iv)
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)

        val key = deriveKey(masterPassword, salt)
        val cipher = Cipher.getInstance(ALGORITHM_CIPHER).apply {
            init(
                Cipher.DECRYPT_MODE,
                key.secretKey,
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            )
        }

        try {
            val plaintext = cipher.doFinal(ciphertext).toString(Charsets.UTF_8)
            DecryptResult(plaintext, salt)
        } catch (e: Exception) {
            throw VaultWrongKeyException(cause = e)
        }
    }

    private fun generateSalt(): ByteArray {
        return ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
    }

    private fun generateIv(): ByteArray {
        return ByteArray(GCM_IV_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
    }
}

class VaultWrongKeyException(
    message: String = "Wrong master password or corrupted data",
    cause: Throwable? = null
) : Exception(message, cause)
