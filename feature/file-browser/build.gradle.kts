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
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.file.browser"

dependencies {
    // Core dependencies
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.theme)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.utils)
    implementation(projects.core.domain)

    // Feature dependencies
    implementation(projects.feature.common)

    // Dependency Injection
    implementation(libs.dagger.hilt.android)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Coroutines
    implementation(libs.coroutinesAndroid)
}

