buildscript {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        // 同 settings.gradle.kts: 国内镜像在 CI(GITHUB_ACTIONS=true) 下跳过
        if (System.getenv("GITHUB_ACTIONS") != "true") {
            maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") // 腾讯云
            maven("https://maven.aliyun.com/repository/public") // 阿里云
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }

    dependencies {
        classpath(libs.kotlinx.serialization.gradle)
        classpath(libs.ksp.gradle)
        classpath(libs.agp.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.hilt.gradle)
        classpath(libs.baselineprofile.gradle)
        classpath(libs.detekt.gradle)
        classpath(libs.aboutlibraries.gradle)
        classpath(libs.compose.compiler.gradle)
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}


configurations.all {
    resolutionStrategy {
        force(libs.com.squareup.okhttp3.okhttp)
        // Force the newer annotations version
        force("org.jetbrains:annotations:26.1.0")

        // Exclude the older annotations-java5 dependency
        exclude(group = "org.jetbrains", module = "annotations-java5")
    }
}

