package com.t8rin.imagetoolbox.core.domain.model

import java.security.MessageDigest
import java.security.Provider
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory

/**
 * 持有可选的 [Provider]（一般是完整版 BouncyCastle），用于让加解密 / 哈希工具
 * 在不污染全局 Security provider 列表的前提下使用扩展算法。
 *
 * 替换 Android 系统的 BC provider 会破坏 NetworkSecurityConfig 的 BKS 信任库加载，
 * 所以这里用 "传 provider 实例" 的方式取代 "全局 addProvider"。
 *
 * 在应用启动时通过 [register] 注入；
 * 没有注入时回落到 JCA 默认 provider 查找，行为与原生 API 一致。
 */
object CryptographyProvider {
    @Volatile
    private var provider: Provider? = null

    fun register(provider: Provider) {
        this.provider = provider
    }

    fun get(): Provider? = provider

    fun cipher(transformation: String): Cipher =
        provider?.let { Cipher.getInstance(transformation, it) }
            ?: Cipher.getInstance(transformation)

    fun messageDigest(algorithm: String): MessageDigest =
        provider?.let { MessageDigest.getInstance(algorithm, it) }
            ?: MessageDigest.getInstance(algorithm)

    fun secretKeyFactory(algorithm: String): SecretKeyFactory =
        provider?.let { SecretKeyFactory.getInstance(algorithm, it) }
            ?: SecretKeyFactory.getInstance(algorithm)
}
