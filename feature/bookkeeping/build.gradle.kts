plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.bookkeeping"

dependencies {
    api(projects.core.base)
    api(projects.core.model)
    api(projects.core.theme)
    api(projects.core.database)
    api(projects.feature.common)
}

