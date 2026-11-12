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

android {
    namespace = "com.wanbaohe.idphoto"
}

dependencies {
    // 项目模块依赖
    implementation(projects.core.base)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.di)
    implementation(projects.core.domain)
    implementation(projects.core.network)
    implementation(projects.core.resources)
    implementation(projects.core.settings)
    implementation(projects.core.ui)
    implementation(projects.core.utils)
    implementation(projects.feature.common)

    // 裁剪相关依赖
    implementation(libs.onebox.cropper)
    implementation(projects.feature.crop)


    ksp(libs.dagger.hilt.compiler)
}

