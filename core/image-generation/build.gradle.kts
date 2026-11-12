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
}

android.namespace = "com.shifenmiao.imagegeneration"

// OpenAI 生图 provider 仅海外渠道可见(纯 BYOK,不走 Go 网关代理),同 core/r 的范式:
//   - google / foss:src/main + src/overseas(OpenAiImageModule 在此注册 provider)
//   - 国内 6 渠道:仅 src/main,编译产物里没有 OpenAI provider 的注册
afterEvaluate {
    android.sourceSets {
        listOf("google", "foss").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/overseas/java")
        }
    }
}

dependencies {
    api(projects.core.r)
    api(projects.core.network)
    implementation(projects.core.data)
    implementation(projects.core.model)

    api(libs.kotlinx.coroutines.core)
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.androidx.security.crypto)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}
