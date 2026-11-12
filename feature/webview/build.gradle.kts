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

android.namespace = "com.shifenmiao.webview"

dependencies {
    implementation(libs.androidx.webkit)
    /**
     * core module
     */
    implementation(projects.core.interfaces)
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.network)
    implementation(projects.core.base)
    implementation(projects.core.theme)
    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)
    implementation(projects.feature.common)

    /**
     * core module
     */
    implementation(projects.feature.login)
    implementation(projects.feature.pdfTools)

    /**
     * 发起网络请求
     */
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.okhttp3.logging.interceptor)
    api(libs.com.squareup.okhttp3.okhttp)

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)

    // Coil SVG decoder for mermaid SVG rendering
    implementation(libs.coilSvg)
    implementation(libs.coil.compose)



}
