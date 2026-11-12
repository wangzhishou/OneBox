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

android.namespace = "com.shifenmiao.network"

dependencies {
    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    /**
     * 事件总线
     */
    api(libs.org.greenrebot.eventbus)

    /**
     * 数据库
     */
    api(libs.androidx.paging.compose)
    api(libs.androidx.room.ktx)

    /**
     * 发起网络请求
     */
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.kotlinx.serialization)
    api(libs.okhttp3.logging.interceptor)
    api(libs.com.squareup.okhttp3.okhttp)

    /**
     * Charset detection
     */
    api(libs.juniversalchardet)

    /**
     * HTML parsing
     */
    api(libs.jsoup)

    /**
     * Json
     */
    api(libs.kotlinx.serialization.json)

    /**
     * 图片处理
     */
    api(libs.coil)
    api(libs.coil.compose)
    api(libs.coilGif)
    api(libs.coilSvg)
    api(libs.datastore.preferences.android)

    api(libs.avif.coder.coil) {
        exclude(module = "com.github.awxkee:avif-coder")
    }
    api(libs.avif.coder)
    api(libs.jxl.coder.coil) {
        exclude(module = "com.github.awxkee:jxl-coder")
    }
    api(libs.jxl.coder)
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:resources"))
    implementation(project(":core:settings"))
    implementation(project(":core:di"))
    implementation(project(":core:crash"))
    implementation(project(":core:r"))
    implementation(project(":core:base"))
    implementation(project(":core:model"))
    implementation(project(":core:theme"))
    implementation(project(":core:database"))
    implementation(project(":core:interfaces"))
    implementation(project(":core:storage"))
}
