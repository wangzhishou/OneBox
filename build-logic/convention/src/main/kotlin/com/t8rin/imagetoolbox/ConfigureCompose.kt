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

package com.t8rin.imagetoolbox

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal fun Project.configureCompose(
    commonExtension: CommonExtension
) {
    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions.freeCompilerArgs.addAll(
            listOf(
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi",
                "-opt-in=androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi",
                "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi",
                "-opt-in=androidx.compose.ui.unit.ExperimentalUnitApi",
                "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
                "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
                "-opt-in=androidx.compose.ui.text.ExperimentalTextApi"
            )
        )
    }

    commonExtension.apply {
        buildFeatures.apply {
            compose = true
        }

        dependencies {
            implementation(libs.androidx.material3)
            implementation(libs.window.sizeclass)
            implementation(libs.androidx.material)
            implementation(libs.icons.extended)
            implementation(libs.compose.runtime)
            implementation(libs.androidx.ui.tooling.preview.android)
        }
    }

    extensions.configure<ComposeCompilerGradlePluginExtension> {

        stabilityConfigurationFiles.addAll(
            rootProject.layout.projectDirectory.file("compose_compiler_config.conf")
        )

        metricsDestination = rootProject.layout.buildDirectory.dir("compose-metrics")
        reportsDestination = rootProject.layout.buildDirectory.dir("compose-reports")
    }
}
