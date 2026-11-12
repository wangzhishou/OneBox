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

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android {
    namespace = "com.t8rin.imagetoolbox.core.resources"

    defaultConfig.vectorDrawables.useSupportLibrary = true

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
            buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
        }
        getByName("debug") {
            buildConfigField("String", "VERSION_NAME", "\"${libs.versions.versionName.get()}\"")
            buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
        }
    }
}

dependencies {
    implementation(libs.material)
    // 「更多」等自绘入口的内容改用系统图标（见 icons/line/LineMore.kt），
    // 依赖集中在本模块声明，方便以后整体摘除。
    implementation(libs.icons.extended)
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    implementation(libs.splashScreen)
    implementation(libs.kotlinx.collections.immutable)
    debugImplementation(libs.compose.ui.tooling)
}