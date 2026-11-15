plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.richtext.editor"

dependencies {
    api(libs.compose.foundation)
    api(libs.compose.ui)
    api(libs.androidx.material3)
    api(libs.kotlinx.coroutines.core)
}
