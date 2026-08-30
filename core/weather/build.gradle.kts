plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.wanbaohe.core.weather"

// 和风天气 SDK 为专有 jar, foss (F-Droid) 渠道不打包:
//   - 非 foss 渠道: src/main + src/nonfoss (真实 WeatherInitializer / QWeatherDataSource) + SDK jar
//   - foss 渠道:    src/main + src/foss (同签名 stub, 天气功能静默降级, 同 core/r 的 flavor 隔离范式)
afterEvaluate {
    android.sourceSets {
        listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
            getByName(flavor).kotlin.srcDir("src/nonfoss/java")
        }
    }
}

dependencies {
    // 专有 SDK 按 flavor 注入, foss 变体不携带
    listOf("onebox", "xiaomi", "yyb", "oppo", "vivo", "huawei", "google").forEach { flavor ->
        add("${flavor}Api", files("libs/QWeather_Public_Android_V5.2.2.jar"))
    }

    // Core dependencies usually present
    implementation(projects.core.model)
    implementation(projects.core.domain)

    // Coroutines and networking
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.gson)
    implementation(libs.androidxCore)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.compose.ui)
}
