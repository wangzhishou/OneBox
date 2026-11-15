plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.markdown"
// Upstream renderer: huarangmeng/Markdown release-1.7.2 (a60a426).
android.sourceSets["main"].kotlin.directories.add("src/upstream/java")
kotlin.compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
android.compileOptions.targetCompatibility = JavaVersion.VERSION_21
android.defaultConfig.consumerProguardFiles("consumer-rules.pro")

dependencies {
    api(libs.hrm.markdown.parser)
    api(libs.hrm.markdown.runtime)
    api(libs.hrm.codehigh.render)
    implementation(libs.hrm.latex.renderer)
    implementation(libs.hrm.latex.fonts.katex)
    implementation(projects.libs.diagram)
    implementation(libs.bundles.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network)
    implementation(libs.ktor)
    implementation(projects.core.ui)
}
