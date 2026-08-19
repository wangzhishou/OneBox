@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        mavenLocal()
        includeBuild("build-logic")
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven("https://jitpack.io") { name = "JitPack" }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io") { name = "JitPack" }
        // 国内镜像仅供本地开发加速; CI(GitHub Actions, 海外网络) 下直连更快更稳,
        // 避免腾讯 nexus 对 androidx 等不存在产物的大量 404/超时拖慢依赖解析
        if (System.getenv("GITHUB_ACTIONS") != "true") {
            maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") // 腾讯云
            maven("https://maven.aliyun.com/repository/public") // 阿里云
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "WanBaoHe"

include(":app")

include(":libs:opencv-tools")
include(":libs:gesture")
include(":libs:image")
include(":libs:zoomable")
include(":libs:modalsheet")
include(":libs:collages")
include(":libs:snowfall")

//include(":libs:cropper")
include(":libs:dynamic-theme")
include(":libs:twain")
include(":libs:richtext")
include(":feature:image-viewer")


include(":feature:pick-color")
include(":feature:image-stitch")
include(":core:filters")
include(":core:weather")
include(":feature:draw")
include(":feature:filters")
include(":feature:single-edit")
include(":feature:pdf-tools")
include(":feature:resize-convert")
include(":feature:palette-tools")
include(":feature:delete-exif")
include(":feature:compare")
include(":feature:weight-resize")
include(":feature:image-preview")
include(":feature:cipher")
include(":feature:limits-resize")
include(":feature:crop")
include(":feature:load-net-image")
include(":feature:marquee")
include(":feature:online")
include(":feature:ocr-document")
include(":feature:webview")
include(":feature:login")
include(":feature:app")
include(":feature:watermarking")
include(":feature:gradient-maker")
include(":feature:gif-tools")
include(":feature:apng-tools")
include(":feature:zip")
include(":feature:media-picker")
include(":feature:quick-tiles")
include(":feature:settings")
include(":feature:svg-maker")
include(":feature:format-conversion")
include(":feature:document-scanner")
include(":feature:scan-qr-code")
include(":feature:image-stacking")
include(":feature:image-splitting")
include(":feature:color-tools")
include(":feature:webp-tools")
include(":feature:noise-generation")
include(":feature:collage-maker")
include(":feature:libraries-info")
include(":feature:markup-layers")
include(":feature:base64-tools")
include(":feature:checksum-tools")
include(":feature:mesh-gradients")
include(":feature:edit-exif")
include(":feature:image-cutting")
include(":feature:audio-cover-extractor")
include(":feature:library-details")
include(":feature:wallpapers-export")
include(":feature:ascii-art")

//include(":feature:root")
include(":feature:ai-image")
include(":feature:ai")
include(":feature:profile")
include(":feature:wechat")
include(":feature:demo")
include(":feature:file-browser")
include(":feature:common")
include(":feature:search")
include(":feature:blog")
include(":feature:decision-wheel")
include(":feature:file-transfer")
include(":feature:cloud-storage")
include(":feature:markdown-edit")
include(":feature:code-editor")
include(":feature:marktodo")
include(":feature:minesweeper")
include(":feature:blessing-wall")
include(":feature:bookkeeping")
include(":feature:habit-tracker")
include(":feature:loan-calculator")
include(":feature:lifetime")
include(":feature:camera-watermark")
include(":feature:id-photo")
include(":feature:altitude")
include(":feature:speed-test")
include(":feature:unit-converter")
include(":feature:compass")
include(":feature:measurement")
include(":feature:calendar")
include(":feature:schedule")
include(":feature:dead-pixel-test")
include(":feature:dice-roller")
include(":feature:game2048")
include(":feature:xiangqi")
include(":feature:teleprompter")
include(":feature:survive30s")
include(":feature:visual-automation")
include(":feature:password-vault")
include(":feature:poem")

include(":core:a2ui")
include(":core:settings")
include(":core:resources")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":core:di")
include(":core:crash")
include(":core:ksp")
include(":core:utils")
include(":core:base")
include(":core:r")
include(":core:model")
include(":core:network")
include(":core:theme")
include(":core:pay")
include(":core:database")
include(":core:interfaces")
include(":core:storage")
include(":core:empty")
include(":core:filters")
include(":core:tts")
