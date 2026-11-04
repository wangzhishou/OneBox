#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WORK_DIR="${1:-$ROOT_DIR/build/onnxruntime-ocr}"
ORT_VERSION="1.31.1"
VCPKG_VERSION="2026.07.29"
ORT_SOURCE_DIR="$WORK_DIR/onnxruntime"
VCPKG_DIR="$WORK_DIR/vcpkg"
TOOLS_DIR="$WORK_DIR/tools"
OCR_DIR="$ROOT_DIR/feature/on-device-ocr"
BUILD_DIR="$WORK_DIR/mac-build"
OUTPUT_NAME="onnxruntime-android-$ORT_VERSION-ppocrv6.aar"

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "This script currently supports macOS builds." >&2
  exit 1
fi

mkdir -p "$WORK_DIR"
if [[ ! -d "$ORT_SOURCE_DIR/.git" ]]; then
  git clone --depth 1 --branch "v$ORT_VERSION" https://github.com/microsoft/onnxruntime.git "$ORT_SOURCE_DIR"
fi
git -C "$ORT_SOURCE_DIR" submodule update --init --depth 1 cmake/external/onnx

if [[ ! -d "$VCPKG_DIR/.git" ]]; then
  git clone --depth 1 --branch "$VCPKG_VERSION" --recurse-submodules --shallow-submodules \
    https://github.com/microsoft/vcpkg.git "$VCPKG_DIR"
fi

if command -v cmake >/dev/null 2>&1; then
  CMAKE_BIN="$(command -v cmake)"
else
  CMAKE_DIR="$TOOLS_DIR/cmake"
  if [[ ! -x "$CMAKE_DIR/CMake.app/Contents/bin/cmake" ]]; then
    mkdir -p "$CMAKE_DIR"
    curl -L --fail https://github.com/Kitware/CMake/releases/download/v3.31.5/cmake-3.31.5-macos-universal.tar.gz \
      -o "$WORK_DIR/cmake-macos.tar.gz"
    echo "cc8e3d9bef7eee70db52601a5ed60d221436a8def18388effdab0e7d0866f50d  $WORK_DIR/cmake-macos.tar.gz" | shasum -a 256 -c -
    tar -xzf "$WORK_DIR/cmake-macos.tar.gz" --strip-components=1 -C "$CMAKE_DIR"
  fi
  CMAKE_BIN="$CMAKE_DIR/CMake.app/Contents/bin/cmake"
fi

if command -v ninja >/dev/null 2>&1; then
  NINJA_BIN="$(command -v ninja)"
else
  if [[ ! -x "$TOOLS_DIR/ninja/ninja" ]]; then
    mkdir -p "$TOOLS_DIR/ninja"
    curl -L --fail https://github.com/ninja-build/ninja/releases/download/v1.13.2/ninja-mac.zip \
      -o "$WORK_DIR/ninja-mac.zip"
    echo "c99048673aa765960a99cf10c6ddb9f1fad506099ff0a0e137ad8960a88f321b  $WORK_DIR/ninja-mac.zip" | shasum -a 256 -c -
    unzip -q -o "$WORK_DIR/ninja-mac.zip" -d "$TOOLS_DIR/ninja"
  fi
  NINJA_BIN="$TOOLS_DIR/ninja/ninja"
fi

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export ANDROID_NDK_HOME="${ANDROID_NDK_HOME:-$ANDROID_HOME/ndk/28.1.13356709}"
export VCPKG_INSTALLATION_ROOT="$VCPKG_DIR"
export PATH="$(dirname "$CMAKE_BIN"):$(dirname "$NINJA_BIN"):$PATH"

python3 "$ORT_SOURCE_DIR/tools/ci_build/github/android/build_aar_package.py" \
  --build_dir="$BUILD_DIR" \
  --config=Release \
  --include_ops_by_config="$OCR_DIR/onnxruntime/ppocrv6-ops.config" \
  "$OCR_DIR/onnxruntime/android-build-settings.json"

AAR_PATH="$BUILD_DIR/aar_out/Release/com/microsoft/onnxruntime/onnxruntime-android/$ORT_VERSION/onnxruntime-android-$ORT_VERSION.aar"
mkdir -p "$OCR_DIR/libs"
cp "$AAR_PATH" "$OCR_DIR/libs/$OUTPUT_NAME"
