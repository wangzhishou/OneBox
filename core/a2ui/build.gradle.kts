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

android.namespace = "com.wanbaohe.a2ui"

// 为国内渠道增加 src/domestic sourceSet,与 src/google 形成 flavor 隔离(同 core/r、core/ui 的做法):
//   - 国内渠道:src/main + src/domestic(平台位置选择走行政区 CityPicker)
//   - Google 渠道:src/main + src/google(平台位置选择走 Google Places Autocomplete)
//   - foss 渠道:src/main + src/foss(与 Google 同一逻辑,可编辑文本输入;无 GMS 降级为手输)
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/domestic/java")
        }
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    googleImplementation(libs.places)
}
