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
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.t8rin.imagetoolbox.core.crash"

// 与 core/r 相同的 flavor 隔离模式:
//   - 国内渠道 + foss: src/main + src/domestic (no-op AnalyticsManagerImpl)
//   - Google 渠道: src/main + src/google (Firebase AnalyticsManagerImpl)
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "foss").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/domestic/java")
        }
    }
}

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.settings)
    implementation(projects.core.domain)
    implementation(project(":core:storage"))

    // Google Play 渠道专属: Firebase BoM + Crashlytics / Analytics (国内渠道不引入)
    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.firebase.crashlytics.ktx)
    "googleImplementation"(libs.firebase.analytics)
}