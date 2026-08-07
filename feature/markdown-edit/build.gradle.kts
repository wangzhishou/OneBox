plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.markdown.edit"

dependencies {
    implementation(projects.core.ui)
    implementation(projects.core.resources)
    implementation(projects.core.model)
    implementation(projects.libs.richtext)
    implementation(projects.libs.twain)
    implementation(projects.feature.common)
    implementation(projects.feature.webview)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.webkit)

    implementation(libs.org.greenrebot.eventbus)

    ksp(libs.androidx.room.compiler)
}

