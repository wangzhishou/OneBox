plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.hilt)
}

android.namespace = "com.wanbaohe.core.weather"

dependencies {
    api(files("libs/QWeather_Public_Android_V5.2.2.jar"))
    
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
