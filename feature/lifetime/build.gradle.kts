plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.shifenmiao.lifetime"

dependencies {
    implementation(libs.androidxCore)
    implementation(libs.appCompat)
    implementation(libs.material)

    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.storage)
    api(projects.core.database)
    api(projects.feature.common)
    api(projects.feature.calendar)


    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.paging)

    /**
     * DataStore for preferences
     */
    implementation(libs.datastore.preferences.android)
}

