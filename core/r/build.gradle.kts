plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android {
    namespace = "com.shifenmiao.core"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            buildConfigField("String", "VERSION_NAME", "\"${System.getenv("VERSION_NAME") ?: libs.versions.versionName.get()}\"")
            buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
        }
        getByName("debug") {
            buildConfigField("String", "VERSION_NAME", "\"${System.getenv("VERSION_NAME") ?: libs.versions.versionName.get()}\"")
            buildConfigField("int", "VERSION_CODE", libs.versions.versionCode.get())
        }
    }
    sourceSets {
        getByName("main") {
            assets {
                srcDirs("src/main/assets")
            }
        }
    }

    // 为国内渠道增加 src/domestic sourceSet，与 src/google 形成 flavor 隔离。
    // 这样 UrlConstantsFlavor 可以按 sourceSet 独立维护：
    //   - 国内渠道：src/main + src/domestic
    //   - Google 渠道：src/main + src/google
}

afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/domestic/java")
        }
    }
}

dependencies {
    implementation(libs.material)
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    implementation(libs.splashScreen)
    implementation(libs.kotlinx.collections.immutable)
    debugImplementation(libs.compose.ui.tooling)
}