#!/usr/bin/env bash
#
# 为 Android arm64 构建 Fairy-Stockfish(中国象棋 + NNUE 权重)。
#
# 用途:评估把「端侧象棋引擎」作为离线棋力方案(见 docs/spike-xiangqi-local-engine.md)。
# 本脚本只做构建与体积测量,不做 JNI 接入;源码与权重都拉取到仓库外的临时工作目录,
# 不入库(GPL-3.0 第三方源码不随本仓库分发)。
#
# 用法:
#   ./scripts/build_fairy_stockfish_android.sh                 # 构建可执行文件 + 共享库
#   WORK_DIR=~/tmp/fsf ./scripts/build_fairy_stockfish_android.sh
#   NDK_VERSION=29.0.14206865 ./scripts/build_fairy_stockfish_android.sh
#
# 产物(打印在末尾):
#   $WORK_DIR/stockfish-android            可在设备 /data/local/tmp 直接跑的 UCI 可执行文件
#   $WORK_DIR/libfairystockfish-arm64.so   供 JNI 接入的共享库
#   $WORK_DIR/xiangqi-*.nnue               象棋 NNUE 权重(运行时下载或内嵌)
#
# 踩过的坑(改动前请先读):
#   1. 必须 largeboards=yes —— 否则 xiangqi 变体被 #ifdef LARGEBOARDS 整段编译掉,
#      引擎里根本没有象棋可选(代价只有约 +0.13MB)。
#   2. 必须显式传 KERNEL=Linux OS=Android —— 在 macOS 上构建时 Makefile 会把宿主机
#      当成 Darwin 并追加 -arch/-mmacosx-version-min,编译直接失败;而 OS 不为 Android
#      时又会追加 -lpthread(bionic 没有独立 libpthread)。
#   3. 共享库不能走 Makefile 的默认链接:OS=Android 会给 CXXFLAGS 追加 -fPIE、给
#      LDFLAGS 追加 -pie,在 -flto 下会被 LTO 后端采用,链接共享库时报
#      "relocation R_AARCH64_... cannot be used against symbol ... recompile with -fPIC"。
#      因此 .so 用命令行覆盖 CXXFLAGS/LDFLAGS(Make 的命令行变量会屏蔽 Makefile 的 +=),
#      完全自己给标志。
#   4. 象棋的 nnueAlias 为空,NNUE 只在权重文件名以 xiangqi 开头时才启用
#      (见 src/evaluate.cpp 的 NNUE::init);通用 nn-*.nnue 只会让国际象棋用上 NNUE。
#   5. 象棋的 UCI 纵线是 1-based(a1-i10),本 App 与标准 UCCI 是 0-based(a0-i9),
#      JNI 桥接必须换算纵线,否则引擎返回的着法一条都对不上。

set -euo pipefail

WORK_DIR="${WORK_DIR:-${TMPDIR:-/tmp}/onebox-fsf-spike}"
NDK_VERSION="${NDK_VERSION:-28.1.13356709}"
ANDROID_API="${ANDROID_API:-24}"   # 与 App 的 minSdk 24 对齐

FSF_REPO="${FSF_REPO:-https://github.com/fairy-stockfish/Fairy-Stockfish.git}"
FSF_COMMIT="${FSF_COMMIT:-9f778da667f6e07dae1e85d3e2ea204fc6dee94d}"
NNUE_FILE="xiangqi-83f16c17fe26.nnue"
NNUE_URL="https://github.com/fairy-stockfish/Fairy-Stockfish/releases/download/fairy_sf_14_0_1_xq/${NNUE_FILE}"
NNUE_SHA256="83f16c17fe266f8d0904cb7cd8997777ee6a618a82b5d7fd32d52f570c760a25"

# 国内直连 GitHub 常常超时;需要时用 HTTPS_PROXY=http://127.0.0.1:7890 跑本脚本。
CURL=(curl -sSL)

log() { printf '\033[1m==> %s\033[0m\n' "$*"; }

find_ndk() {
  local candidates=(
    "${ANDROID_NDK_HOME:-}"
    "${ANDROID_NDK_ROOT:-}"
    "$HOME/Library/Android/sdk/ndk/${NDK_VERSION}"
    "$HOME/Android/Sdk/ndk/${NDK_VERSION}"
  )
  for candidate in "${candidates[@]}"; do
    if [[ -n "$candidate" && -x "$candidate/toolchains/llvm/prebuilt" ]]; then
      echo "$candidate"; return 0
    fi
  done
  echo "找不到 NDK ${NDK_VERSION};请设 ANDROID_NDK_HOME 或 NDK_VERSION" >&2
  return 1
}

NDK="$(find_ndk)"
HOST_TAG="$(ls "$NDK/toolchains/llvm/prebuilt" | head -n 1)"
TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/$HOST_TAG/bin"
CXX="$TOOLCHAIN/aarch64-linux-android${ANDROID_API}-clang++"
STRIP="$TOOLCHAIN/llvm-strip"
[[ -x "$CXX" ]] || { echo "没有可用的交叉编译器: $CXX" >&2; exit 1; }
log "NDK: $NDK"

mkdir -p "$WORK_DIR"

log "获取源码 @ ${FSF_COMMIT:0:7} → $WORK_DIR/Fairy-Stockfish"
if [[ ! -d "$WORK_DIR/Fairy-Stockfish/.git" ]]; then
  git clone "$FSF_REPO" "$WORK_DIR/Fairy-Stockfish"
fi
git -C "$WORK_DIR/Fairy-Stockfish" fetch --all --tags --quiet
git -C "$WORK_DIR/Fairy-Stockfish" checkout --quiet "$FSF_COMMIT"

log "获取象棋 NNUE 权重并校验"
if [[ ! -f "$WORK_DIR/$NNUE_FILE" ]]; then
  "${CURL[@]}" -o "$WORK_DIR/$NNUE_FILE" "$NNUE_URL"
fi
actual_sha="$(shasum -a 256 "$WORK_DIR/$NNUE_FILE" | cut -d' ' -f1)"
if [[ "$actual_sha" != "$NNUE_SHA256" ]]; then
  echo "权重校验失败:期望 $NNUE_SHA256,实际 $actual_sha" >&2
  exit 1
fi
log "权重校验通过 ($(du -h "$WORK_DIR/$NNUE_FILE" | cut -f1))"

SRC="$WORK_DIR/Fairy-Stockfish/src"
COMMON_ARGS=(
  ARCH=armv8 COMP=clang KERNEL=Linux OS=Android
  largeboards=yes nnue=no
  CXX="$CXX" STRIP="$STRIP"
)

log "构建 Android 可执行文件(用于真机功能验证)"
# -static-libstdc++ 是必须的:Android 平台不提供 libc++_shared.so(它不是公开平台库),
# 动态链接的产物推到 /data/local/tmp 会以
# cannot locate symbol "_ZTVNSt6__ndk114basic_ifstream..." 启动失败。
# 注意:App 内的 JNI .so 反而适合用动态 libc++(App 本来就打包 libc++_shared.so),体积更小。
make -C "$SRC" clean >/dev/null
make -C "$SRC" build "${COMMON_ARGS[@]}" EXE=stockfish-android \
  EXTRALDFLAGS="-static-libstdc++" -j"$(sysctl -n hw.ncpu 2>/dev/null || nproc)"
"$STRIP" --strip-unneeded -o "$WORK_DIR/stockfish-android" "$SRC/stockfish-android"

log "构建共享库(供 JNI 接入;命令行覆盖 CXXFLAGS/LDFLAGS 以避开 -fPIE/-pie)"
# 标志对齐 Makefile 的 clang + largeboards + optimize=yes 路径,只是把 -fPIE 换成 -fPIC。
SO_CXXFLAGS="-Wall -Wcast-qual -fno-exceptions -std=c++17 -fPIC -Wno-profile-instr-out-of-date \
-pedantic -Wextra -Wshadow -m64 -DLARGEBOARDS -DPRECOMPUTED_MAGICS -DNNUE_EMBEDDING_OFF \
-DUSE_PTHREADS -DNDEBUG -O3 -fno-strict-aliasing -DIS_64BIT -DUSE_POPCNT -DUSE_NEON -flto"
SO_LDFLAGS="-shared -fPIC -static-libstdc++ -O3 -flto -lm -latomic"
make -C "$SRC" clean >/dev/null
make -C "$SRC" build "${COMMON_ARGS[@]}" \
  EXE=libfairystockfish.so \
  CXXFLAGS="$SO_CXXFLAGS" LDFLAGS="$SO_LDFLAGS" \
  -j"$(sysctl -n hw.ncpu 2>/dev/null || nproc)"
"$STRIP" --strip-unneeded -o "$WORK_DIR/libfairystockfish-arm64.so" "$SRC/libfairystockfish.so"

log "产物体积(strip 后)"
printf '  %-28s %s\n' "可执行文件" "$(du -h "$WORK_DIR/stockfish-android" | cut -f1)"
printf '  %-28s %s\n' "共享库" "$(du -h "$WORK_DIR/libfairystockfish-arm64.so" | cut -f1)"
printf '  %-28s %s\n' "象棋 NNUE 权重" "$(du -h "$WORK_DIR/$NNUE_FILE" | cut -f1)"
echo
echo "真机验证(需要 adb 与 arm64 设备):"
echo "  adb push '$WORK_DIR/stockfish-android' '$WORK_DIR/$NNUE_FILE' /data/local/tmp/"
echo "  adb shell 'chmod 755 /data/local/tmp/stockfish-android'"
echo "  printf 'uci\nsetoption name UCI_Variant value xiangqi\n"
echo "setoption name EvalFile value /data/local/tmp/$NNUE_FILE\nisready\nposition startpos\ngo depth 20\n' \\"
echo "    | adb shell 'cd /data/local/tmp && ./stockfish-android'"
