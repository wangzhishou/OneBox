/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2025 T8RIN (Malik Mukhametzyanov)
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

package com.shifenmiao.app.functions

import com.t8rin.imagetoolbox.core.domain.model.CipherType
import com.t8rin.imagetoolbox.core.domain.model.CryptographyProvider
import com.t8rin.imagetoolbox.core.domain.model.HashingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.DefaultAlgorithmNameFinder
import java.security.Security

internal fun registerSecurityProviders() {
    // 不要替换 Android 系统自带的 BC provider —— Android 的 NetworkSecurityConfig
    // 在 TLS 握手时会通过 KeyStore.getInstance("BKS") 加载系统信任库；
    // 替换成 bcprov-jdk18on 之后部分设备/ROM 上 BKS 注册会丢失，
    // 触发 SSLHandshakeException: java.security.KeyStoreException: BKS not found，
    // 表现就是所有 HTTPS 请求统统失败。
    //
    // 改用 "持有 BouncyCastle 实例显式传给 Cipher/MessageDigest" 的方式：
    //   - 不动全局 Security provider 列表 → SSL 安全
    //   - 加解密 / 哈希工具页通过 [CryptographyProvider] 拿到这个实例
    //     仍然能用 BouncyCastle 全套算法（Twofish / Serpent / SHA3 / BLAKE 等）
    val bcProvider = BouncyCastleProvider()
    CryptographyProvider.register(bcProvider)

    // 仅在系统没有 BC 时（极少数定制 ROM）才补一个全局兜底，
    // 让 javax.crypto 默认查找仍能找到 BouncyCastle。
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
        Security.addProvider(bcProvider)
    }

    // 直接从 BouncyCastle 实例的 services 列表里枚举算法，
    // 不再依赖全局 Security.getAlgorithms()——后者只能看到系统已注册的 BC，
    // 拿不到 BouncyCastle 的扩展算法。
    val services = bcProvider.services

    HashingType.registerSecurityMessageDigests(
        services.asSequence()
            .filter { it.type == "MessageDigest" }
            .map { it.algorithm }
            .distinct()
            .toList()
    )

    CoroutineScope(Dispatchers.Default).launch {
        val finder = DefaultAlgorithmNameFinder()

        CipherType.registerSecurityCiphers(
            services.asSequence()
                .filter { it.type == "Cipher" }
                .map { it.algorithm }
                .distinct()
                .mapNotNull { cipher ->
                    if (CipherType.BROKEN.any { cipher.contains(it, true) }) return@mapNotNull null

                    val oid = cipher.removePrefix("OID.")
                    val type = if (oid.all { it.isDigit() || it.isWhitespace() || it == '.' }) {
                        CipherType.getInstance(
                            cipher = cipher,
                            name = finder.getAlgorithmName(
                                ASN1ObjectIdentifier(oid)
                            )
                        )
                    } else {
                        CipherType.getInstance(
                            cipher = cipher
                        )
                    }

                    val extraExclude = type.cipher == "DES"
                            || type.name == "DES/CBC"
                            || type.name == "THREEFISH-512"
                            || type.name == "THREEFISH-1024"
                            || type.name == "CCM"

                    if (extraExclude) null else type
                }
                .toList()
        )
    }
}
