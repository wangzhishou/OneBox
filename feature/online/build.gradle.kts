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

android.namespace = "com.shifenmiao.online"

dependencies {
    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.network)
    api(projects.core.storage)
    implementation(projects.core.utils)
    api(projects.core.theme)
    api(projects.core.database)
    api(projects.feature.common)
    api(projects.feature.settings)
    api(projects.feature.webview)
    api(projects.feature.markdownEdit)
    api(projects.feature.codeEditor)
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)

    /**
     * 数据库
     */
    api(libs.androidx.paging.compose)
    api(libs.androidx.room.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)

    /**
     * 发起网络请求
     */
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.okhttp3.logging.interceptor)
    api(libs.com.squareup.okhttp3.okhttp)

    /**
     * Json
     */
    api(libs.kotlinx.serialization.json)

    /**
     * 图片处理
     */
    api(libs.avif.coder)
    api(libs.androidx.lifecycle)
    api(libs.androidx.lifecycle.runtime)
    /**
     * Decompose 组件化框架
     */
    api(libs.decomposelifecycle)

    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)
}