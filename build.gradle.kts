/*
 * OneBox is a free AI agent and toolbox for Android
 * This file is part of OneBox, a derivative work based on
 * ImageToolbox (https://github.com/T8RIN/ImageToolbox) by T8RIN.
 *
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file has been modified from the original ImageToolbox source.
 *
 * Origin: ImageToolbox derivative, modified
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

buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        // 同 settings.gradle.kts: 国内镜像在 CI(GITHUB_ACTIONS=true) 下跳过
        if (System.getenv("GITHUB_ACTIONS") != "true") {
            maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") // 腾讯云
            maven("https://maven.aliyun.com/repository/public") // 阿里云
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        classpath(libs.kotlinx.serialization.gradle)
        classpath(libs.ksp.gradle)
        classpath(libs.agp.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.hilt.gradle)
        classpath(libs.baselineprofile.gradle)
        classpath(libs.detekt.gradle)
        classpath(libs.aboutlibraries.gradle)
        classpath(libs.compose.compiler.gradle)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}


configurations.all {
    resolutionStrategy {
        force(libs.com.squareup.okhttp3.okhttp)
        // Force the newer annotations version
        force("org.jetbrains:annotations:26.1.0")

        // Exclude the older annotations-java5 dependency
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
}

allprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "com.atlassian.commonmark") {
                useTarget("org.commonmark:${requested.name}:${libs.versions.commonmark.get()}")
                because("Markwon 4.6.2 still uses the legacy CommonMark group")
            }
        }
    }
}

