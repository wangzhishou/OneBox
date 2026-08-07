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

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.t8rin.imagetoolbox.core.ui"

// 为国内渠道增加 src/domestic sourceSet,与 src/google 形成 flavor 隔离(同 core/r 的做法):
//   - 国内渠道:src/main + src/domestic(InAppReviewPrompt 空实现)
//   - Google 渠道:src/main + src/google(InAppReviewPrompt 走 Play In-App Review)
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/domestic/java")
        }
    }
}

dependencies {
    api(projects.core.resources)
    api(projects.core.domain)
    api(projects.core.utils)
    implementation(libs.androidx.compose.material3)
    implementation(projects.core.di)
    implementation(projects.core.settings)

    // Navigation

    api(libs.reimagined)
    api(libs.reimagined.hilt)
    api(libs.decompose)
    api(libs.decomposeExtensions)

    //AndroidX
    api(libs.activityCompose)
    api(libs.splashScreen)
    api(libs.androidx.exifinterface)
    api(libs.appCompat)
    api(libs.androidx.documentfile)

    //Konfetti
    api(libs.konfetti.compose)

    //Coil
    api(libs.coil)
    api(libs.coil.compose)
    api(libs.coilGif)
    api(libs.coilSvg)
    api(libs.coil.network)
    api(libs.ktor)

    //Modules
    api(libs.onebox.ucrop)
    api(libs.onebox.cropper)
    api(projects.libs.dynamicTheme)
    api(libs.onebox.colordetector)
    api(projects.libs.gesture)
    api(libs.onebox.beforeafter)
    api(libs.onebox.image)
    api(projects.libs.modalsheet)
    api(libs.onebox.gpuimage)
    api(libs.onebox.screenshot)
    api(libs.onebox.systemuicontroller)
    api(libs.onebox.placeholder)
    api(libs.toolbox.logger)
    api(projects.libs.zoomable)
    api(libs.onebox.colorpicker)
    api(libs.onebox.jp2decoder)
    api(libs.onebox.tiff.decoder)
    api(projects.libs.snowfall)
    api(libs.onebox.extendedcolors)
    api(libs.onebox.histogram)
    api(projects.core.domain)

    api(libs.reorderable)

    api(libs.shadowGadgets)
    api(libs.shadowsPlus)

    api(libs.kotlinx.collections.immutable)

    api(libs.fadingEdges)
    api(libs.scrollbar)

    implementation(libs.datastore.preferences.android)
    googleImplementation(libs.play.review)
    api(libs.documentscanner)
    api(libs.quickie.foss)
    api(libs.material)

    api(libs.documentscanner)
    implementation(libs.zxing.android.embedded)
    implementation(libs.onebox.qrose)

    implementation(libs.jsoup)

    api(libs.androidliquidglass)
    api(libs.capsule)
    api(libs.evaluator)

    api(libs.capturable)
    api(libs.evaluator)

    api(projects.core.resources)
    api(projects.core.r)
    api(projects.core.settings)
    api(projects.core.model)
    api(projects.core.theme)

    implementation(projects.core.interfaces)
    implementation(project(":core:storage"))
    implementation(libs.org.greenrebot.eventbus)
    implementation(libs.com.airbnb.lottie)

    api(libs.squircle.shape)

    api(libs.evaluator)

    api(libs.flinger)
}