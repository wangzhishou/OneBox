/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
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

android.namespace = "com.shifenmiao.base"

dependencies {
    implementation(libs.reimagined)
    implementation(libs.reimagined.hilt)

    implementation(libs.androidx.documentfile)

    //AndroidX
    implementation(libs.activityCompose)
    implementation(libs.splashScreen)
    implementation(libs.androidx.exifinterface)
    implementation(libs.appCompat)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //Konfetti
    implementation(libs.konfetti.compose)

    //Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coilGif)
    implementation(libs.coilSvg)

    //Modules
    implementation(libs.onebox.cropper)
    implementation(projects.libs.dynamicTheme)
    implementation(libs.onebox.colordetector)
    implementation(libs.onebox.beforeafter)
    implementation(projects.libs.gesture)
    implementation(libs.onebox.screenshot)
    implementation(libs.onebox.systemuicontroller)
    implementation(libs.onebox.placeholder)
    api(libs.toolbox.logger)
    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)

    implementation(projects.libs.zoomable)
    implementation(project(":core:domain"))

    implementation(libs.onebox.colorpicker) {
        exclude("com.github.SmartToolFactory", "Compose-Color-Detector")
    }

    implementation(libs.reorderable)

    implementation(libs.shadowGadgets)
    implementation(libs.shadowsPlus)

    implementation(libs.kotlinx.collections.immutable)

    implementation(libs.fadingEdges)
    implementation(libs.scrollbar)

    implementation(libs.androidx.palette.ktx)
    implementation(libs.m3color)
    implementation(libs.org.greenrebot.eventbus)
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    /**
     * lottie
     */
    implementation(libs.com.airbnb.lottie)

    /**
     * Json
     */
    implementation(libs.kotlinx.serialization.json)

    /**
     * markdown
     */

    implementation(libs.datastore.preferences.android)
    implementation(libs.coroutinesAndroid)


    implementation(projects.core.resources)
    implementation(projects.core.r)
    implementation(projects.core.ui)
    implementation(projects.core.settings)

    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.core.interfaces)
    implementation(projects.core.storage)
}