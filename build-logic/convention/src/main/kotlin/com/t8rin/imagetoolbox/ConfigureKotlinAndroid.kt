/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

package com.t8rin.imagetoolbox

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
    createFlavors: Boolean = true
) {
    commonExtension.apply {
        compileSdk = libs.versions.androidCompileSdk.get().toIntOrNull()
        compileSdkMinor = libs.versions.androidCompileSdkMinor.get().toInt()

        defaultConfig.apply {
            minSdk = libs.versions.androidMinSdk.get().toIntOrNull()
        }

        if (createFlavors) {
            flavorDimensions += "app"

            fun createFlavor(name: String) {
                productFlavors.apply {
                    create(name) {
                        manifestPlaceholders["flavorName"] = name
                        buildConfigField("String", "FLAVOR", "\"$name\"")
                        dimension = "app"
                    }
                }
            }
            // 注意: 新增渠道时必须在这里同步 createFlavor, 否则所有 library 模块编译失败;
            // 同时别忘了 app/build.gradle.kts 的 flavor 块与各 *Config.getConfigByFlavor 映射
            createFlavor("onebox")
            createFlavor("xiaomi")
            createFlavor("yyb")
            createFlavor("oppo")
            createFlavor("vivo")
            createFlavor("huawei")
            createFlavor("google")
            // F-Droid 渠道: 各 library 模块借此获得 foss flavor 与 src/foss sourceSet,
            // 专有 SDK 的 stub 实现放在 src/foss/java 下
            createFlavor("foss")
        }

        compileOptions.apply {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
            isCoreLibraryDesugaringEnabled = true
        }

        buildFeatures.apply {
            compose = true
            aidl = false
            shaders = false
            buildConfig = true
            resValues = false
        }

        packaging.apply {
            resources {
                excludes.add("/META-INF/{AL2.0,LGPL2.1}")
            }
        }

        lint.apply {
            disable += "UsingMaterialAndMaterial3Libraries"
            disable += "ModifierParameter"
            disable += "ConvertLongToDuration"
        }
    }

    configureKotlin<KotlinAndroidProjectExtension>()

    dependencies {
        coreLibraryDesugaring(libs.desugaring)
        implementation(libs.compose.runtime)
    }
}

val Project.javaVersion: JavaVersion
    get() = JavaVersion.toVersion(
        libs.versions.jvmTarget.get()
    )


/**
 * Configure base Kotlin options
 */
private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
    val args = listOf(
        "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        "-opt-in=kotlinx.coroutines.FlowPreview",
        "-opt-in=kotlin.contracts.ExperimentalContracts",
        "-opt-in=kotlinx.coroutines.DelicateCoroutinesApi",
    )
    // Treat all Kotlin warnings as errors (disabled by default)
    // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
    val warningsAsErrors = (project.findProperty("warningsAsErrors") as? String).toBoolean()
    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> error("Unsupported project extension $this ${T::class}")
    }.apply {
        jvmTarget = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
        allWarningsAsErrors = warningsAsErrors
        freeCompilerArgs.addAll(args)
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions.freeCompilerArgs.addAll(args)
        // Compose 的 opt-in 由 compose 约定插件加。这里只补不在每个模块类路径上的标记。
        if (usesGroupPrefix("com.arkivanov.decompose")) {
            compilerOptions.freeCompilerArgs.add(
                "-opt-in=com.arkivanov.decompose.ExperimentalDecomposeApi",
            )
        }
        if (usesGroupPrefix("io.coil-kt")) {
            compilerOptions.freeCompilerArgs.addAll(coilOptIns)
        }
    }
}

private val coilOptIns = listOf(
    "-opt-in=coil3.annotation.ExperimentalCoilApi",
    "-opt-in=coil3.annotation.DelicateCoilApi",
)

private fun Project.usesGroupPrefix(prefix: String): Boolean = configurations.any { configuration ->
    runCatching {
        configuration.dependencies.any { dependency ->
            val group = dependency.group ?: return@any false
            group == prefix || group.startsWith("$prefix.")
        }
    }.getOrDefault(false)
}
