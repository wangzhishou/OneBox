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
}

android.namespace = "com.wanbaohe.codeeditor"

dependencies {
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(libs.androidx.material3)
    api(libs.kotlinx.coroutines.core)

    // 词法分析（codehighlight-parser）与 token→颜色映射（codehighlight-render）复用
    // libs/markdown 代码块同一套引擎，保证编辑器与预览的语法着色同源。
    api(libs.hrm.codehigh.parser)
    api(libs.hrm.codehigh.render)
}
