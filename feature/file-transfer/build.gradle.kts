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

android.namespace = "com.wanbaohe.file_transfer"

dependencies {
    // Hilt DI
    implementation(libs.dagger.hilt.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // NanoHTTPD - 轻量级HTTP服务器
    implementation(libs.nanohttpd)
    implementation(libs.nanohttpd.websocket)

    // ZXing - 二维码生成
    implementation(libs.zxing.android.embedded )

    // Coil - 图片加载
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // Core modules
    implementation(projects.core.r)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.core.theme)
    implementation(projects.core.interfaces)
    implementation(projects.core.base)
    implementation(projects.core.database)

    // Feature common
    implementation(projects.feature.common)

    /**
     * 数据库
     */
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)

    // Unit tests
    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)

    // Instrumented tests (Room needs Android Context)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
}
