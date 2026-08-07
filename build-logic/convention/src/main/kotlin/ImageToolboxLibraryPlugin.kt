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
import java.util.Properties

private fun String?.toBuildConfigStringLiteral(): String = "\"${this.orEmpty()}\""

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
                defaultConfig.buildConfigField("String", "GitVersion", "\"${System.getenv("GIT_VERSION") ?: libs.versions.gitVersion.get()}\"")

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
                    create("arm32") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.add("armeabi-v7a")
                        }
                    }
                    create("universal") {
                        dimension = "abi"
                        ndk {
                            abiFilters.clear()
                            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
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

                val tencentAuthorizationCOde = properties.getProperty("tencentAuthorizationCOde")
                defaultConfig.buildConfigField("String", "TencentAuthorizationCOde", tencentAuthorizationCOde.toBuildConfigStringLiteral())

            }


            dependencies {
                implementation(libs.androidxCore)
            }
        }
    }
}