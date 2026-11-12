/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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
