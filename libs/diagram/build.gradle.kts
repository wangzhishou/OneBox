plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.diagram"
// Upstream renderer: huarangmeng/diagram release-1.1.0 (e442bc7).
android.sourceSets["main"].kotlin.directories.add("src/upstream/java")
kotlin.compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
android.compileOptions.targetCompatibility = JavaVersion.VERSION_21
android.defaultConfig.consumerProguardFiles("consumer-rules.pro")

dependencies {
    implementation(libs.bundles.compose)
    api(libs.kotlinx.coroutines.core)
    api(libs.hrm.diagram.core)
    api(libs.hrm.diagram.layout)
    api(libs.hrm.diagram.parser)
}
