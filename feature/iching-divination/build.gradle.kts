plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.iching"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)

    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.storage)
    api(projects.core.database)
    api(projects.feature.common)
    implementation(projects.libs.richtext)
    implementation(libs.kotlinx.serialization.json)

    "arm64Api"(libs.com.tencent.mmkv)
    "universalApi"(libs.com.tencent.mmkv)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}

