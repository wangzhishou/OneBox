plugins {
    alias(libs.plugins.image.toolbox.library)
    alias(libs.plugins.image.toolbox.feature)
    alias(libs.plugins.image.toolbox.hilt)
    alias(libs.plugins.image.toolbox.compose)
}

android.namespace = "com.wanbaohe.xiangqi"
// 端侧引擎构建任务要从 NDK 目录取交叉编译器;版本与 app 模块同源(libs.versions.toml)
android.ndkVersion = libs.versions.androidNdk.get()

/**
 * 端侧象棋引擎(Fairy-Stockfish, GPL-3.0)构建任务。
 *
 * 源码 vendor 在 `third_party/fairy-stockfish`(来源/许可/更新方式见其 README.onebox.md),
 * **二进制不入库**,每次构建从源码编出来 —— 这样 GPL 有对应源码可提供,`foss` 渠道也能
 * 从源码可复现构建(F-Droid 不接受预编译 .so)。
 *
 * 产物是 **PIE 可执行文件但命名为 `libfairystockfish.so`**:AGP 会按 ABI 打包它,
 * 安装时解压到可执行的 native 库目录,App 侧以 UCI 子进程驱动(见 `LocalXiangqiEngine`)。
 *
 * 只产出 arm64-v8a —— 两个 abi flavor 的 abiFilters 都是 arm64-v8a;若将来新增 ABI,
 * 这里与 [engineAbi] 一起改。
 */
val engineAbi = "arm64-v8a"
val engineMinSdk = 24
val engineSourceDir = layout.projectDirectory.dir("third_party/fairy-stockfish/src")
val engineOutputDir = layout.buildDirectory.dir("generated/fairyStockfish")

val buildFairyStockfish = tasks.register("buildFairyStockfish") {
    group = "build"
    description = "从 vendor 源码编译端侧象棋引擎(Fairy-Stockfish, $engineAbi)"

    val sourceDir = engineSourceDir.asFile
    val workDir = layout.buildDirectory.dir("fairyStockfish-work").get().asFile
    val outputDir = engineOutputDir.get().asFile
    val ndkDirProvider = androidComponents.sdkComponents.ndkDirectory
    val abi = engineAbi
    val minSdk = engineMinSdk
    val workers = Runtime.getRuntime().availableProcessors().coerceIn(1, 8)

    // 源码或构建参数变化才重编(引擎全量编译在 M 系列机器上约 1 分钟)
    inputs.dir(sourceDir).withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("abi", abi)
    inputs.property("minSdk", minSdk)
    inputs.property("engineBuildFlags", "largeboards=yes nnue=no static-libstdc++")
    outputs.dir(outputDir)
    // 产物是纯编译结果, 允许 build cache 复用:CI 的 tag 构建是 8 渠道 × 2 ABI 的矩阵,
    // 每个 job 都重编一次引擎太浪费(setup-gradle 已开远端 build cache)
    outputs.cacheIf { true }

    // NDK 目录只能在执行期解析(与 app 模块覆盖 libc++_shared 的任务同一处理方式):
    // 配置期解析会让未安装 NDK 的机器连 sync 都过不去
    notCompatibleWithConfigurationCache("NDK 目录在执行期解析")

    doLast {
        val ndk = ndkDirProvider.get().asFile
        val prebuilt = ndk.resolve("toolchains/llvm/prebuilt")
            .listFiles { file: File -> file.isDirectory }
            ?.firstOrNull()
            ?: error("找不到 NDK 预编译工具链: $ndk")
        val bin = prebuilt.resolve("bin")
        val cxx = bin.resolve("aarch64-linux-android$minSdk-clang++")
        val strip = bin.resolve("llvm-strip")
        if (!cxx.isFile) error("没有可用的交叉编译器: $cxx(请确认 NDK $minSdk 版本已安装)")

        // 上游 Makefile 是就地编译的:先同步到 build 目录再编,别把 *.o 丢进 vendor 源码树
        workDir.deleteRecursively()
        sourceDir.copyRecursively(workDir, overwrite = true)

        fun run(vararg args: String) {
            val result = providers.exec {
                commandLine(*args)
                workingDir = workDir
                isIgnoreExitValue = true
            }.result.get()
            if (result.exitValue != 0) {
                error("命令失败(exit=${result.exitValue}): ${args.joinToString(" ")}")
            }
        }

        // largeboards=yes 是必须的:xiangqi 变体在 variant.cpp 里被 #ifdef LARGEBOARDS 包着;
        // KERNEL/OS 也必须显式给,否则 macOS 上会按 Darwin 加 -arch,非 Android 分支还会加
        // bionic 没有的 -lpthread(详见 docs/xiangqi-local-engine.md 第 1 节)。
        run(
            "make", "-C", workDir.absolutePath, "build",
            "ARCH=armv8", "COMP=ndk", "KERNEL=Linux", "OS=Android",
            "largeboards=yes", "nnue=no",
            "CXX=${cxx.absolutePath}", "STRIP=${strip.absolutePath}",
            "EXE=libfairystockfish.so",
            "EXTRALDFLAGS=-static-libstdc++",
            "-j$workers",
        )

        val built = File(workDir, "libfairystockfish.so")
        if (!built.isFile) error("引擎未产出: $built")

        // 先剥离再落盘:未剥离约 6.9MB,剥离后约 1.5MB
        val target = File(File(outputDir, abi).apply { mkdirs() }, "libfairystockfish.so")
        run(strip.absolutePath, "--strip-unneeded", "-o", target.absolutePath, built.absolutePath)
        if (!target.isFile || target.length() == 0L) error("引擎剥离失败: $target")
        logger.lifecycle("端侧象棋引擎: $target (${target.length() / 1024} KB)")
    }
}

// 生成的 jniLibs 目录参与打包。
// 走 variant API 而不是 android.sourceSets.jniLibs:后者在 AGP 9 + 本项目的约定插件组合下
// 会抛 ClassCastException(DefaultAndroidLibrarySourceSet_Decorated -> AndroidLibrarySourceSet)。
// 任务依赖挂在 preBuild 上 —— 它早于该变体的所有任务,最稳。
androidComponents.onVariants { variant ->
    variant.sources.jniLibs?.addStaticSourceDirectory(engineOutputDir.get().asFile.absolutePath)
}
tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(buildFairyStockfish) }

dependencies {
    implementation(projects.feature.common)
    implementation(projects.feature.boardgame)
    implementation(projects.core.tts)
    implementation(libs.coil.compose)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
}
