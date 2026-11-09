/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
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

import com.android.build.api.dsl.LibraryExtension
import com.t8rin.imagetoolbox.configureDetekt
import com.t8rin.imagetoolbox.configureKotlinAndroid
import com.t8rin.imagetoolbox.implementation
import com.t8rin.imagetoolbox.libs
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.util.Properties

private fun String?.toBuildConfigStringLiteral(): String = "\"${this.orEmpty()}\""

// git 短哈希缓存:本插件被全部 ~127 个模块应用,git 只需跑一次
private var gitHashCache: Pair<File, String?>? = null

private fun gitHeadShortHash(project: Project): String? {
    val dir = project.rootDir
    gitHashCache?.takeIf { it.first == dir }?.let { return it.second }
    // providers.exec 是配置缓存兼容的外部进程方式(裸 ProcessBuilder 会让构建失败)
    // 截断到 7 位而不靠 --short:短哈希长度随克隆深度变化(浅克隆 7 位、全克隆歧义自动延长),RB 构建两边必须一致
    val hash = runCatching {
        project.providers.exec {
            commandLine("git", "rev-parse", "HEAD")
            workingDir(dir)
            isIgnoreExitValue = true
        }.standardOutput.asText.get().trim().take(7).takeIf { it.length == 7 && it.all(Char::isLetterOrDigit) }
    }.getOrNull()
    gitHashCache = dir to hash
    return hash
}

// GitVersion 优先级:CI 注入的 GIT_VERSION > 构建时 git rev-parse(F-Droid 评审要求动态获取)> 版本目录兜底
private fun resolveGitVersion(fallback: String, project: Project): String =
    System.getenv("GIT_VERSION")?.takeIf(String::isNotBlank)
        ?: gitHeadShortHash(project)
        ?: fallback

@Suppress("UNUSED")
class ImageToolboxLibraryPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("kotlin-parcelize")
                apply("kotlinx-serialization")
                apply(plugin = "org.jetbrains.kotlin.plugin.compose")


                apply(libs.detekt.gradle.get().group)
            }

            configureDetekt(extensions.getByType<DetektExtension>())

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.minSdk = libs.versions.androidMinSdk.get().toIntOrNull()
                defaultConfig.buildConfigField("String", "VersionName", "\"${System.getenv("VERSION_NAME") ?: libs.versions.versionName.get()}\"")
                defaultConfig.buildConfigField("String", "VersionCode", "\"${libs.versions.versionCode.get()}\"")
                defaultConfig.buildConfigField("String", "GitVersion", "\"${resolveGitVersion(libs.versions.gitVersion.get(), target)}\"")

                // 添加 abi 维度，与 app 模块保持一致
                flavorDimensions += "abi"
                productFlavors {
                    create("arm64") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.add("arm64-v8a")
                        }
                    }
                }

                val properties = Properties()
                val localPropertiesFile = rootProject.file("keystore.properties")
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.inputStream().use { properties.load(it) }
                }

                val xiaomiAuthorizationCode = properties.getProperty("xiaomiAuthorizationCode")
                defaultConfig.buildConfigField("String", "XiaomiAuthorizationCode", xiaomiAuthorizationCode.toBuildConfigStringLiteral())

                val douBaoAuthorizationCode = properties.getProperty("douBaoAuthorizationCode")
                defaultConfig.buildConfigField("String", "DouBaoAuthorizationCode", douBaoAuthorizationCode.toBuildConfigStringLiteral())

                val kimiAuthorizationCode = properties.getProperty("kimiAuthorizationCode")
                defaultConfig.buildConfigField("String", "KimiAuthorizationCode", kimiAuthorizationCode.toBuildConfigStringLiteral())

                val baiduAuthorizationCode = properties.getProperty("baiduAuthorizationCode")
                defaultConfig.buildConfigField("String", "BaiduAuthorizationCode", baiduAuthorizationCode.toBuildConfigStringLiteral())

                val openAIAuthorizationCode = properties.getProperty("openAIAuthorizationCode")
                defaultConfig.buildConfigField("String", "OpenAIAuthorizationCode", openAIAuthorizationCode.toBuildConfigStringLiteral())

                val qWenAIAuthorizationCode = properties.getProperty("qWenAIAuthorizationCode")
                defaultConfig.buildConfigField("String", "QWenAIAuthorizationCode", qWenAIAuthorizationCode.toBuildConfigStringLiteral())

                val deepSeekAuthorizationCode = properties.getProperty("deepSeekAuthorizationCode")
                defaultConfig.buildConfigField("String", "DeepSeekAuthorizationCode", deepSeekAuthorizationCode.toBuildConfigStringLiteral())

                val tencentAuthorizationCode = properties.getProperty("tencentAuthorizationCode")
                defaultConfig.buildConfigField("String", "TencentAuthorizationCode", tencentAuthorizationCode.toBuildConfigStringLiteral())

                // 和风天气 JWT 凭据 (core/weather 使用; release/debug 两组,
                // 未配置时为空串, 天气功能静默降级)
                listOf(
                    "QWeatherReleasePrivateKey",
                    "QWeatherReleaseProjectId",
                    "QWeatherReleaseKeyId",
                    "QWeatherReleaseDeveloperId",
                    "QWeatherDebugPrivateKey",
                    "QWeatherDebugProjectId",
                    "QWeatherDebugKeyId",
                    "QWeatherDebugDeveloperId",
                ).forEach { field ->
                    val key = field.replaceFirstChar { it.lowercase() }
                    defaultConfig.buildConfigField("String", field, properties.getProperty(key).toBuildConfigStringLiteral())
                }

                // 后端域名与游客 token (core/r 的 UrlConstantsFlavor 读取; 国内/海外两组,
                // 未配置时为空串, 开源构建不会触达生产服务器)
                listOf(
                    "ApiBaseUrlDomestic",
                    "ApiBaseUrlGoogle",
                    "ApiDebugUrlDomestic",
                    "GuestAccessToken",
                    "RemoteConfigAccessTokenDomestic",
                    "RemoteConfigAccessTokenGoogle",
                ).forEach { field ->
                    val key = field.replaceFirstChar { it.lowercase() }
                    defaultConfig.buildConfigField("String", field, properties.getProperty(key).toBuildConfigStringLiteral())
                }

            }


            dependencies {
                implementation(libs.androidxCore)
            }
        }
    }
}