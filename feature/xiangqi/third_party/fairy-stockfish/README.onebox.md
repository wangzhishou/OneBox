# Fairy-Stockfish(vendored)

本目录是 [Fairy-Stockfish](https://github.com/fairy-stockfish/Fairy-Stockfish) 的 **源码副本**,用于
给中国象棋提供离线强引擎(见 `docs/xiangqi-local-engine.md`)。

## 来源与许可

| | |
|---|---|
| 上游 | https://github.com/fairy-stockfish/Fairy-Stockfish |
| 版本 | commit `9f778da667f6e07dae1e85d3e2ea204fc6dee94d`(2026-09-23) |
| 许可 | **GPL-3.0**(见 [Copying.txt](Copying.txt)),署名见 [AUTHORS](AUTHORS) |
| 改动 | **无**。仅剔除了构建产物(`*.o`、`.depend`、编译出的可执行文件)与 `src/variants.ini` |

**为什么 vendor 源码而不是直接放预编译二进制**:Fairy-Stockfish 是 GPL-3.0,分发二进制必须能提供
对应源码;而且本项目的 `foss` 渠道要过 F-Droid 的可复现构建校验,预编译 `.so` 不满足。
因此这里只提交源码,二进制由构建期从源码编译产出(见下),不进仓库。

**没有携带的东西**:

- `src/variants.ini` —— 那是给自定义变体用的运行时文件;中国象棋是**编译期内置变体**
  (`variant.cpp` 的 `add("xiangqi", ...)`),不需要它;
- NNUE 权重(`xiangqi-*.nnue`,10.74MB)—— 属于数据,运行时从 R2 下载,不进 APK 也不进仓库。

## 怎么被编译

由 `feature/xiangqi/build.gradle.kts` 里的 `buildFairyStockfish` 任务驱动,产物落在
`build/generated/fairyStockfish/arm64-v8a/libfairystockfish.so`(因此 `feature/xiangqi/src/main/jniLibs/`
与生成目录都在 `.gitignore` 里)。

关键构建参数(踩坑记录见 `docs/xiangqi-local-engine.md` 第 1 节):

```
make -C <src> build ARCH=armv8 COMP=ndk KERNEL=Linux OS=Android \
     largeboards=yes nnue=no \
     CXX=<ndk>/aarch64-linux-android24-clang++ STRIP=<ndk>/llvm-strip \
     EXE=libfairystockfish.so EXTRALDFLAGS=-static-libstdc++
```

- `largeboards=yes` **必须**:中国象棋变体被 `#ifdef LARGEBOARDS` 包着,不加就整个编译掉;
- `KERNEL=Linux OS=Android` **必须**:否则 macOS 上会按 Darwin 追加 `-arch/-mmacosx-version-min`,
  且非 Android 分支会追加 bionic 没有的 `-lpthread`;
- `nnue=no`:权重外挂,运行时用 UCI `setoption name EvalFile` 指定路径;
- `-static-libstdc++`:Android 平台不提供 `libc++_shared.so` 作公开库,而这是要 exec 的可执行文件;
- 产物是 **PIE 可执行文件**、但命名为 `lib*.so`:这样 AGP 会把它当 native 库按 ABI 打包并解压到
  可执行的 native 库目录,App 侧以子进程方式驱动它(不走 JNI,见文档中的取舍说明)。

## 怎么更新到新版本

1. 从上游 checkout 目标 commit,把 `src/` 下的 `*.cpp`/`*.h`/`Makefile`(含 `nnue/`、`syzygy/`、
   `incbin/` 子目录)覆盖到本目录,同时更新 `Copying.txt`/`AUTHORS`;
2. 更新本文档表格里的 commit 与日期;
3. 若上游改了默认网络名或变体注册方式,同步检查 `docs/xiangqi-local-engine.md` 里的相关结论;
4. 重新构建并**在真机上验证**:权重能加载(`NNUE evaluation using ... enabled`)、能正常出招。
