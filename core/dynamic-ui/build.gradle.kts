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
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.wanbaohe.dynamicui"

dependencies {
    // JSON parsing
    api(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.coroutinesAndroid)
    implementation(libs.kotlinx.coroutines.core)

    // Coil (AsyncImage)
    implementation(libs.coil.compose)

    // Lottie
    implementation(libs.com.airbnb.lottie)

    // OkHttp (HttpAction)
    implementation(libs.com.squareup.okhttp3.okhttp)

    // Material3
    implementation(libs.androidx.material3)

    // IconRegistry from core/ui for efficient icon resolution
    implementation(projects.core.ui)
}
