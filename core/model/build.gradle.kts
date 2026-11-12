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

android.namespace = "com.shifenmiao.model"

// 微信 OpenSDK 为专有依赖, foss (F-Droid) 渠道不打包:
//   - 非 foss 渠道: src/main + src/nonfoss (真实 Wechat 对象) + SDK
//   - foss 渠道:    src/main + src/foss (同签名 no-op stub), 同 core/r 的 flavor 隔离范式
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nonfoss/java")
        }
    }
}

dependencies {
    /**
     * Json
     */
    api(libs.kotlinx.serialization.json)
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.okhttp3.okhttp)
    api(projects.core.r)
    api(projects.core.resources)
    api(projects.core.interfaces)

    // 专有 SDK 按 flavor 注入, foss 变体不携带
    listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
        add("${flavor}Api", libs.com.tencent.opensdk.get())
    }
    api(libs.kotlinx.coroutines.core)
}