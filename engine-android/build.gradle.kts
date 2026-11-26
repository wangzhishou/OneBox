/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 * Licensed under the Apache License, Version 2.0
 */

plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android {
    namespace = "com.wanbaohe.engine.android"
    ndkVersion = libs.versions.androidNdk.get()

    defaultConfig {
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20", "-fvisibility=hidden")
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildFeatures {
        prefab = true
    }
}

dependencies {
    implementation(libs.oboe)
    implementation(libs.games.frame.pacing)
}
