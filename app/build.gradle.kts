@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.ApplicationBuildType
import java.io.FileInputStream
import java.util.Properties
import java.util.UUID

plugins {
    alias(libs.plugins.image.toolbox.application)
    alias(libs.plugins.image.toolbox.hilt)
    // 注意: 不应用 google-services plugin
    // - google flavor 的 Firebase 由 src/google/assets/google-services.json 初始化 (debug/release 均启用)
    // - 国内 flavor 完全没有 Firebase 依赖
}

// Create a variable called keystorePropertiesFile, and initialize it to your
// keystore.properties file, in the rootProject folder.
val keystorePropertiesFile: File = rootProject.file("keystore.properties")
// Initialize a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()
// keystore.properties 为可选文件: 存在才加载; 缺失时下方签名配置使用默认占位值,
// debug 构建回退 AGP 默认 debug 签名, 保证没有签名文件的贡献者也能正常同步与构建.
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

// ---- Firebase 手动接入的数据源 -------------------------------------------------
// 未应用 google-services / Crashlytics 插件, 相关资源手动注入, 但全部从
// src/google/assets/google-services.json 单一数据源派生, 从 Firebase 控制台重新下载
// json 后无需改动本文件:
//   1) buildTypes 里的 Firebase 资源 (google_app_id/google_api_key/project_id 等,
//      即 google-services 插件原本的生成物, 由 injectFirebaseResValues 注入)
//   2) ABI flavor 里的 com.google.firebase.crashlytics.mapping_file_id (release 反混淆)
// providers.fileContents 使 json 成为 configuration cache 的输入, json 变更会正确失效缓存
val googleServicesJson =
    providers.fileContents(layout.projectDirectory.file("src/google/assets/google-services.json"))
val googleServicesRoot: Map<*, *>? = run {
    if (!googleServicesJson.asText.isPresent) return@run null
    groovy.json.JsonSlurper().parseText(googleServicesJson.asText.get()) as? Map<*, *>
}

fun googleServicesClient(packageName: String): Map<*, *>? {
    val clients = googleServicesRoot?.get("client") as? List<*> ?: return null
    return clients.firstNotNullOfOrNull { entry ->
        val client = entry as? Map<*, *> ?: return@firstNotNullOfOrNull null
        val clientInfo = client["client_info"] as? Map<*, *>
        val pkg = (clientInfo?.get("android_client_info") as? Map<*, *>)
            ?.get("package_name") as? String
        if (pkg == packageName) client else null
    }
}

// 注入 google-services 插件原本会生成的 Firebase 资源 (FirebaseOptions.fromResource 读取):
//   google_app_id / google_api_key 按包名匹配 client; project_id / gcm_defaultSenderId /
//   google_storage_bucket 取全局 project_info.
// project_id 缺失会导致 FirebaseInstallations 拿不到 FID, Crashlytics/Analytics 无法上报
// (实测 logcat 报 "Please set your Project ID"). json 缺失则不注入, Firebase 整体禁用
fun injectFirebaseResValues(buildType: ApplicationBuildType, packageName: String) {
    val client = googleServicesClient(packageName) ?: return
    val clientInfo = client["client_info"] as? Map<*, *> ?: return
    val projectInfo = googleServicesRoot?.get("project_info") as? Map<*, *>
    (clientInfo["mobilesdk_app_id"] as? String)
        ?.let { buildType.resValue("string", "google_app_id", it) }
    ((client["api_key"] as? List<*>)?.firstOrNull() as? Map<*, *>)
        ?.get("current_key")?.let { buildType.resValue("string", "google_api_key", it as String) }
    (projectInfo?.get("project_id") as? String)
        ?.let { buildType.resValue("string", "project_id", it) }
    (projectInfo?.get("project_number") as? String)
        ?.let { buildType.resValue("string", "gcm_defaultSenderId", it) }
    (projectInfo?.get("storage_bucket") as? String)
        ?.let { buildType.resValue("string", "google_storage_bucket", it) }
}

// Crashlytics 反混淆: mapping_file_id 正常由 Crashlytics 插件每次构建生成并注入,
// 这里按 versionName+versionCode+ABI 确定性生成 (UUID v3), 同一版本重复构建/上传幂等.
// 不同 ABI 的 R8 mapping 可能不同, 故按 ABI 区分 id. 构建后由
// app/build/crashlytics/mappingfileid-<abi>.xml 配套 firebase CLI 上传 mapping.txt
fun crashlyticsMappingFileId(abi: String): String = UUID.nameUUIDFromBytes(
    "onebox-${libs.versions.versionName.get()}-${libs.versions.versionCode.get()}-$abi"
        .toByteArray(Charsets.UTF_8)
).toString().replace("-", "")

android {

    val supportedAbi = arrayOf("armeabi-v7a", "arm64-v8a")

    namespace = "com.shifenmiao.app"

    defaultConfig {
        vectorDrawables.useSupportLibrary = true

        applicationId = "com.shifenmiao.app"
        versionCode = libs.versions.versionCode.get().toIntOrNull()
        versionName = System.getenv("VERSION_NAME") ?: libs.versions.versionName.get()


    }

    /**
     * 这里设置打包的时候打包几种语言。
     * 必须包含所有允许用户切换到的语言，否则 AppCompatDelegate.setApplicationLocales()
     * 切换到一个 APK 中不存在的 locale 时，Android 15 的
     * ConfigurationController.updateLocaleListFromAppContext 会因 locale list
     * 不一致而抛出 NPE。
     * generateLocaleConfig = true 会让 AGP 根据 localeFilters 自动生成 locale_config.xml，
     * 系统（Android 13+）据此感知 App 支持哪些语言。
     */
    androidResources {
        generateLocaleConfig = true
    }

    // 指定 NDK 版本：r28+ 才支持 16 KB 页大小
    ndkVersion = "28.1.13356709"

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // 声明两个维度
    flavorDimensions += listOf("app", "abi")

    fun createFlavor(name: String, dimension: String) {
        productFlavors.create(name) {
            manifestPlaceholders["flavorName"] = name
            buildConfigField("String", "FLAVOR", "\"$name\"")
            this.dimension = dimension
            // 国内渠道只打中文包 (节省体积, 海外 locale 不需要)
            androidResources {
                localeFilters += listOf("zh-rCN")
            }
            // 渠道 BuildConfig 字段默认值 (与 google flavor 字段保持一致, 业务代码统一读取)
            // API 域名从 keystore.properties 读取, 不入库; 未配置时(如开源构建)为空串
            buildConfigField("String", "API_BASE_URL", "\"${keystoreProperties["apiBaseUrlDomestic"] as? String ?: ""}\"")
            buildConfigField("String", "WEB_BASE_URL", "\"https://www.wanbaohe.com/\"")
            buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://www.shifenmiao.com/privacy/example.html\"")
            buildConfigField("String", "USER_AGREEMENT_URL", "\"https://www.shifenmiao.com/privacy/agreement.html\"")
            buildConfigField("boolean", "ENABLE_WECHAT", "true")
            buildConfigField("boolean", "ENABLE_ALIPAY", "true")
            buildConfigField("boolean", "ENABLE_HMS", "true")
            buildConfigField("boolean", "ENABLE_GMS", "false")
            buildConfigField("boolean", "ENABLE_PLAY_BILLING", "false")
            // 国内渠道只打中文包, 不展示语言切换入口
            buildConfigField("boolean", "SHOW_LANGUAGE_SETTING", "false")
        }
    }

    createFlavor("xiaomi", "app")
    createFlavor("yyb", "app")
    createFlavor("oppo", "app")
    createFlavor("vivo", "app")
    createFlavor("huawei", "app")
    createFlavor("onebox", "app")

    // Google Play 渠道: 海外发行, 默认不携带微信/小米推送等国内 SDK
    // 业务侧可通过 BuildConfig.ENABLE_WECHAT / ENABLE_HMS / ENABLE_GMS / API_BASE_URL 守门
    productFlavors.create("google") {
        manifestPlaceholders["flavorName"] = "google"
        // Google Places API key (core/a2ui 位置选择), 从 keystore.properties 读取, 不入库
        manifestPlaceholders["googlePlacesApiKey"] = keystoreProperties["googlePlacesApiKey"] as? String ?: ""
        // 未应用 Crashlytics Gradle 插件(SDK 从 string 资源读取配置, manifest meta-data 无效):
        // 1) 注入 build ID 资源; 2) RequireBuildId=false 兜底, 避免 SDK 初始化抛
        //    IllegalStateException 中断整个 FirebaseApp.initializeApp
        resValue("string", "com.crashlytics.android.build_id", "onebox-${libs.versions.versionName.get()}-${libs.versions.versionCode.get()}")
        resValue("string", "com.crashlytics.RequireBuildId", "false")
        buildConfigField("String", "FLAVOR", "\"google\"")
        dimension = "app"
        // 海外包: 英文 + 简体中文 + 西班牙语 + 巴西葡萄牙语 + 印尼语 + 印地语 + 俄语 + 土耳其语 + 日语 + 韩语 + 菲律宾语 + 德语, 更多语种翻译就绪后再放开
        // 完整列表参考 https://support.google.com/googleplay/android-developer/answer/9888077
        androidResources {
            localeFilters += listOf(
                "en",        // 英文 (默认)
                "zh-rCN",    // 简体中文
                "es",        // 西班牙语
                "pt-rBR",    // 巴西葡萄牙语
                "in",        // 印尼语
                "hi",        // 印地语
                "ru",        // 俄语
                "tr",        // 土耳其语
                "ja",        // 日语
                "ko",        // 韩语
                "fil",       // 菲律宾语
                "de",        // 德语
            )
        }
        // 海外后端域名 (Cloud Run), 从 keystore.properties 读取, 不入库
        buildConfigField("String", "API_BASE_URL", "\"${keystoreProperties["apiBaseUrlGoogle"] as? String ?: ""}\"")
        buildConfigField("String", "WEB_BASE_URL", "\"https://www.oneboxable.com/\"")
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://www.oneboxable.com/privacy/global.html\"")
        buildConfigField("String", "USER_AGREEMENT_URL", "\"https://www.oneboxable.com/agreement/global.html\"")
        buildConfigField("boolean", "ENABLE_WECHAT", "false")
        buildConfigField("boolean", "ENABLE_ALIPAY", "false")
        buildConfigField("boolean", "ENABLE_HMS", "false")
        buildConfigField("boolean", "ENABLE_GMS", "true")
        // Google Play 渠道数字商品(积分)必须走 Play Billing
        buildConfigField("boolean", "ENABLE_PLAY_BILLING", "true")
        // 海外多语言包, 展示语言切换入口
        buildConfigField("boolean", "SHOW_LANGUAGE_SETTING", "true")
    }

    // 3. 【关键修改】手动定义架构风味 (Abi Dimension)，指定 abiFilters
    // 不要使用上面的 createFlavor 函数，因为我们需要定制 ndk 块
    productFlavors {
        create("arm64") {
            dimension = "abi"
            ndk {
                abiFilters.clear()
                abiFilters.add("arm64-v8a")
            }
            manifestPlaceholders["flavorAbi"] = "arm64"
            // Crashlytics 反混淆 id, 仅 google flavor 的 SDK 会读; 国内包无 Firebase, 资源闲置
            resValue("string", "com.google.firebase.crashlytics.mapping_file_id", crashlyticsMappingFileId("arm64"))
        }

        create("arm32") {
            dimension = "abi"
            ndk {
                abiFilters.clear()
                abiFilters.add("armeabi-v7a")
            }
            manifestPlaceholders["flavorAbi"] = "arm32"
            resValue("string", "com.google.firebase.crashlytics.mapping_file_id", crashlyticsMappingFileId("arm32"))
        }

        create("universal") {
            dimension = "abi"
            ndk {
                abiFilters.clear()
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }
            manifestPlaceholders["flavorAbi"] = "universal"
            resValue("string", "com.google.firebase.crashlytics.mapping_file_id", crashlyticsMappingFileId("universal"))
        }
    }


    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as? String ?: "defaultKeyAlias"
            keyPassword = keystoreProperties["keyPassword"] as? String ?: "defaultKeyPassword"
            storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
                ?: file("defaultStoreFile")
            storePassword = keystoreProperties["storePassword"] as? String ?: "defaultStorePassword"
            // 国内备案要V1签名
            enableV1Signing = true
        }

        // Google Play 渠道独立签名: 国内 / Google 各自一把 keystore,
        // 任何一把泄露不会影响另一边的发布能力. 读取 keystore-google.properties,
        // 不存在时回退到 release 配置, 保持向后兼容.
        create("google") {
            val googleKeystoreFile = rootProject.file("keystore-google.properties")
            val googleKeystoreProperties = Properties()
            if (googleKeystoreFile.exists()) {
                googleKeystoreFile.inputStream().use { googleKeystoreProperties.load(it) }
                keyAlias = googleKeystoreProperties["keyAlias"] as? String
                    ?: keystoreProperties["keyAlias"] as? String
                    ?: "defaultKeyAlias"
                keyPassword = googleKeystoreProperties["keyPassword"] as? String
                    ?: keystoreProperties["keyPassword"] as? String
                    ?: "defaultKeyPassword"
                storeFile = googleKeystoreProperties["storeFile"]?.let { file(it as String) }
                    ?: keystoreProperties["storeFile"]?.let { file(it as String) }
                    ?: file("defaultStoreFile")
                storePassword = googleKeystoreProperties["storePassword"] as? String
                    ?: keystoreProperties["storePassword"] as? String
                    ?: "defaultStorePassword"
                // Google Play 现在要求 v2 签名 (APK Signature Scheme v2+),
                // Play App Signing 还会额外做 v3+ 签名, 这里开启 v2/v3
                enableV1Signing = false
                enableV2Signing = true
                enableV3Signing = true
            } else {
                // 回退到默认 release 签名配置 (无 google 专属 keystore 时)
                keyAlias = keystoreProperties["keyAlias"] as? String ?: "defaultKeyAlias"
                keyPassword = keystoreProperties["keyPassword"] as? String ?: "defaultKeyPassword"
                storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
                    ?: file("defaultStoreFile")
                storePassword = keystoreProperties["storePassword"] as? String ?: "defaultStorePassword"
                enableV1Signing = true
            }
        }
    }

    buildTypes {
        debug {
            // 有 keystore.properties 时沿用 release 签名; 缺失时回退 AGP 默认 debug 签名
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            applicationIdSuffix = ".debug"
            resValue("string", "file_provider", "com.shifenmiao.app.fileprovider.debug")
            manifestPlaceholders["fileProviderAuthority"] = "com.shifenmiao.app.fileprovider.debug"
            // 未应用 google-services 插件, 手动注入 Firebase 资源 (.debug client);
            // 国内 flavor 无 Firebase, 资源不会被读取
            injectFirebaseResValues(this, "com.shifenmiao.app.debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 不在这里直接设 signingConfig: 由 androidComponents.onVariants 按 flavor 分发
            // 国内 flavor 走 signingConfigs.release, google flavor 走 signingConfigs.google
            ndk {
                debugSymbolLevel = "NONE"  // 移除调试符号
            }
            resValue("string", "file_provider", "com.shifenmiao.app.fileprovider")
            manifestPlaceholders["fileProviderAuthority"] = "com.shifenmiao.app.fileprovider"
            // 同上: 匹配 com.shifenmiao.app client
            injectFirebaseResValues(this, "com.shifenmiao.app")
        }
    }

//    splits {
//        abi {
//            // Detect app bundle and conditionally disable split abis
//            // This is needed due to a "Sequence contains more than one matching element" error
//            // present since AGP 8.9.0, for more info see:
//            // https://issuetracker.google.com/issues/402800800
//
//            // AppBundle tasks usually contain "bundle" in their name
//            //noinspection WrongGradleMethod
//            val isBuildingBundle =
//                gradle.startParameter.taskNames.any { it.lowercase().contains("bundle") }
//            isEnable = !isBuildingBundle
//            reset()
//            include(*supportedAbi)
//            isUniversalApk = true
//        }
//    }
    packaging {
        jniLibs {
            // 保留 libc++_shared.so，优先使用 NDK r28+ 编译生成的 16 KB 对齐版本，避免多个 AAR 提供的副本冲突
            pickFirsts.add("**/libc++_shared.so")
            pickFirsts.add("lib/*/libcoder.so")
            useLegacyPackaging = true
            // 精简小众图像格式 Native 库（共约 8-10MB，国内用户极少用到）
            excludes += "lib/**/libdjvu-coder.so"
            excludes += "lib/**/libjxl.so"
            excludes += "lib/**/libjxl_cms.so"
            excludes += "lib/**/libjxl_threads.so"
            excludes += "lib/**/libheif.so"
            excludes += "lib/**/libde265.so"
            excludes += "lib/**/libx265.so"
            excludes += "lib/**/libaom.so"
        }
        resources {
            excludes += "META-INF/"
            // BouncyCastle 1.85 起三个 jar 都打包了相同的 META-INF/LICENSE.md,只保留一份避免 merge 冲突
            pickFirsts += "META-INF/LICENSE.md"
            excludes += "kotlin/"
            excludes += "org/"
            excludes += ".properties"
            excludes += ".bin"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            // 排除 BouncyCastle 后量子密码 Picnic 预计算表（约 1.1MB，几乎不会用到）
            excludes += "org/bouncycastle/pqc/**"
        }
    }


    buildFeatures {
        resValues = true
    }

    lint {
        // 禁用 32 位架构警告，因为我们已经通过 productFlavors 分开打包
        disable += "ChromeOsAbiSupport"
        // 误报: google flavor 用 raw-zh-rCN/initial_data.sql 覆盖 core:database 的同名资源,
        // 基础 raw 在库模块里存在, lintVital 不跨模块认基础资源, 会导致 release 构建失败
        disable += "MissingDefaultResource"
    }
}

// 渠道签名分发: google flavor 用独立 google 签名 (需 keystore-google.properties),
// 其他 flavor 走默认 'release' 签名 (国内 keystore).
// 必须在 buildTypes 之后, 此时所有变体已注册, 才能正确覆盖 signingConfig.
androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val flavorName = variant.productFlavors.firstOrNull()?.second
        val googleKeystoreFile = rootProject.file("keystore-google.properties")
        val targetConfig = if (flavorName == "google" && googleKeystoreFile.exists()) {
            android.signingConfigs.getByName("google")
        } else {
            android.signingConfigs.getByName("release")
        }
        variant.signingConfig.setConfig(targetConfig)
    }
}

base {
    archivesName = "OneBox-${android.defaultConfig.versionName}"
}

// 生成 firebase CLI 上传 mapping.txt 所需的 --resource-file (每个 ABI 一份),
// id 与注入 APK 的 com.google.firebase.crashlytics.mapping_file_id 一致,
// 由 build_release.sh 在 google release 构建后用于手动上传
val writeCrashlyticsMappingFileIds by tasks.registering {
    val outputDir = layout.buildDirectory.dir("crashlytics")
    // 配置期算好三个 id, doLast 只捕获普通 Map, 避免引用脚本对象破坏 configuration cache
    val idByAbi = mapOf(
        "arm64" to crashlyticsMappingFileId("arm64"),
        "arm32" to crashlyticsMappingFileId("arm32"),
        "universal" to crashlyticsMappingFileId("universal"),
    )
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        idByAbi.forEach { (abi, id) ->
            // 与 firebase crashlytics:mappingfile:generateid 的官方输出格式保持一致,
            // buildtools 对 resource name 校验严格, 格式不符会拒绝上传
            File(dir, "mappingfileid-$abi.xml").writeText(
                """
                |<?xml version="1.0" encoding="utf-8"?>
                |<resources xmlns:tools="http://schemas.android.com/tools">
                |<string name="com.google.firebase.crashlytics.mapping_file_id" tools:ignore="UnusedResources,TypographyDashes" translatable="false">$id</string>
                |</resources>
                """.trimMargin() + "\n"
            )
        }
    }
}

tasks.matching { it.name.startsWith("assembleGoogle") && it.name.endsWith("Release") }
    .configureEach { dependsOn(writeCrashlyticsMappingFileIds) }

aboutLibraries {
    export.excludeFields.addAll("generated")
}

dependencies {
    implementation(libs.compose.runtime)

    implementation(project(":feature:app"))
    implementation(project(":feature:visual-automation"))
    coreLibraryDesugaring(libs.desugaring)
    api(libs.toolbox.logger)
    implementation(libs.coil.video)
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.compiler)


    implementation(libs.bouncycastle.pkix)
    implementation(libs.bouncycastle.provider)

    /**
     * mmkv
     */
    "arm64Api"(libs.com.tencent.mmkv)
    "arm32Api"(libs.com.tencent.mmkv.arm32)
    "universalApi"(libs.com.tencent.mmkv)
    api(libs.coil)
    api(libs.coil.network)
    api(libs.ktor)
    implementation(libs.coilGif)
    implementation(libs.coilSvg)
    implementation(libs.onebox.tiff.decoder)
    implementation(libs.onebox.qoiCoder)
    implementation(libs.onebox.jp2decoder)
    implementation(libs.onebox.awebp)
    implementation(libs.onebox.psd)
    implementation(libs.onebox.apng)
    implementation(libs.onebox.djvuCoder)
    implementation(libs.trickle)
    implementation(projects.core.domain)
    implementation(projects.core.resources)
    implementation(libs.avif.coder.coil) {
        exclude(module = "com.github.awxkee:avif-coder")
    }
    implementation(libs.avif.coder)
    implementation(libs.jxl.coder.coil) {
        exclude(module = "com.github.awxkee:jxl-coder")
    }

    //Compose
    api(libs.androidx.material3)
    api(libs.window.sizeclass)
    api(libs.icons.extended)
    api(libs.androidx.material)
    implementation(libs.org.greenrebot.eventbus)
    implementation(projects.libs.twain)
    implementation(projects.libs.richtext)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.settings)
    implementation(projects.core.filters)
    implementation(projects.core.crash)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.storage)
    implementation(projects.core.utils)
    implementation(projects.feature.mediaPicker)
    implementation(projects.feature.quickTiles)
    implementation(projects.feature.app)
    implementation(projects.feature.ai)
    implementation(projects.feature.wechat)
    implementation(projects.feature.login)
    implementation(projects.feature.common)
    implementation(projects.feature.imageViewer)
    implementation(projects.feature.markdownEdit)
    implementation(projects.feature.webview)
    implementation(projects.libs.opencvTools)

    implementation(libs.pdfbox)

    // Google Play 渠道专属依赖: Firebase BoM + Crashlytics / Analytics
    // 国内渠道不引入, 减少 APK 体积 + 避免 google-services.json 找不到报错
    // BoM 自动管理 firebase-* 版本, 子模块不需要指定 version
    "googleImplementation"(platform(libs.firebase.bom))
    "googleImplementation"(libs.firebase.crashlytics.ktx)
    "googleImplementation"(libs.firebase.analytics)
}

allprojects {
    configurations.all {
        resolutionStrategy {
            // 强制使用 4.11.0 版本
            force("org.opencv:opencv:4.11.0")
            dependencySubstitution {
                substitute(module("com.caverock:androidsvg-aar:1.4")).using(module("com.github.deckerst:androidsvg:cc9d59a88f"))
                substitute(module("org.opencv:opencv:4.11.0")).using(module("org.opencv:opencv:4.11.0"))
                substitute(module("org.jetbrains.kotlin:kotlin-android-extensions-runtime:1.4.0")).using(
                    module("org.jetbrains.kotlin:kotlin-parcelize-runtime:2.2.20")
                )
            }
        }
        // 排除旧版本的 OpenCV
        exclude(group = "com.websitebeaver", module = "opencv")
        // 排除远程 zoomable，使用本地 :libs:zoomable 模块，避免重复类
        exclude(group = "com.wanbaohe.libs", module = "zoomable")
        // 排除旧版本 BouncyCastle (jdk15to18)，统一使用 jdk18on 版本，避免重复类
        exclude(group = "org.bouncycastle", module = "bcprov-jdk15to18")
        exclude(group = "org.bouncycastle", module = "bcpkix-jdk15to18")
        exclude(group = "org.bouncycastle", module = "bcutil-jdk15to18")
    }
}

afterEvaluate {
    android.productFlavors.forEach { flavor ->
        tasks.matching { task ->
            task.name.contains(
                flavor.name.replaceFirstChar(Char::uppercase)
            )
        }
    }
}

// 强制用 NDK 自带的 16 KB 对齐 libc++_shared.so 覆盖合并后的副本，避免 AAR 带下来的旧版 .so 导致 16 KB 警告
val abiToNdkTriple = mapOf(
    "arm64-v8a" to "aarch64-linux-android",
    "armeabi-v7a" to "arm-linux-androideabi"
)

// Defer NDK resolution to execution time so Gradle configuration / IDE sync
// doesn't fail when the NDK isn't installed yet.
val ndkDirForLibCppProvider = androidComponents.sdkComponents.ndkDirectory

tasks.withType<com.android.build.gradle.internal.tasks.MergeNativeLibsTask>().configureEach {
    notCompatibleWithConfigurationCache("Overrides libc++_shared.so from NDK at execution time")
    doLast {
        val ndkDirForLibCpp = ndkDirForLibCppProvider.get().asFile
        val prebuilt = ndkDirForLibCpp.resolve("toolchains/llvm/prebuilt")
            .listFiles { f: File -> f.isDirectory }
            ?.firstOrNull()
            ?: return@doLast
        val outputDirFile = outputDir.get().asFile
        abiToNdkTriple.forEach { (abi, triple) ->
            val source = prebuilt.resolve("sysroot/usr/lib/$triple/libc++_shared.so")
            val dest = outputDirFile.resolve("lib/$abi/libc++_shared.so")
            if (source.exists() && dest.parentFile?.exists() == true) {
                source.copyTo(dest, overwrite = true)
            }
        }
    }
}
