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

    api(libs.kotlinx.coroutines.core)
    api(libs.com.squareup.retrofit2.retrofit)
    api(libs.com.squareup.retrofit2.converter.gson)
    api(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.gson)
    implementation(libs.androidx.security.crypto)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}
