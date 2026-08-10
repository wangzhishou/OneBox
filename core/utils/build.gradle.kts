plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.t8rin.imagetoolbox.core.utils"

dependencies {
    implementation(projects.core.r)
    implementation(projects.core.domain)
    implementation(projects.core.resources)
    implementation(projects.core.settings)
    implementation(libs.quickie.foss)
    implementation(libs.zxing.android.embedded)
    implementation(libs.androidx.documentfile)
    // AppCompatDelegate.getApplicationLocales(): LocaleUtils 读应用 per-app 语言用
    implementation(libs.appCompat)
}