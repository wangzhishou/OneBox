#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# build_release.sh — 打各个渠道的 Release 包 (仅 64 位: arm64 / universal)
#
# 用法:
#   ./build_release.sh                       # 7 渠道 × 2 架构 = 14 个 APK
#   ./build_release.sh --channel xiaomi huawei
#   ./build_release.sh --abi arm64
#   ./build_release.sh --channel onebox --abi arm64
#   ./build_release.sh --no-offline --clean
#   ./build_release.sh --skip-build          # 不跑 gradlew，只重新归集产物
#   ./build_release.sh --skip-upload         # 不上传 google 渠道的 Crashlytics mapping
#
# 产物统一复制到 release/ 目录，命名沿用 Gradle 生成的
#   OneBox-<version>-<channel>-<abi>-release.apk
#
# google 渠道的 release 经 R8 混淆，构建后会自动用 firebase CLI 上传
# mapping.txt 到 Crashlytics 反混淆 (需 npm i -g firebase-tools 且已
# firebase login / 配置 FIREBASE_TOKEN；缺工具或未登录仅提示跳过，不影响打包)
# -----------------------------------------------------------------------------
set -euo pipefail

# ---- 常量 -------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

ALL_CHANNELS=("xiaomi" "yyb" "oppo" "vivo" "huawei" "onebox" "google")
ALL_ABIS=("arm64" "universal")
OUT_DIR="release"
GRADLE="./gradlew"
KEYSTORE_FILE="keystore.properties"

# PascalCase: xiaomi -> Xiaomi, onebox -> Onebox
to_pascal() {
    awk -F'-' '{for (i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2)} 1' OFS=''
}

# ---- 参数解析 ---------------------------------------------------------------
CHANNELS=()
ABIS=()
USE_OFFLINE=1
DO_CLEAN=0
SKIP_BUILD=0
SKIP_UPLOAD=0

print_usage() {
    sed -n '2,19p' "$0" | sed 's/^# \{0,1\}//'
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --channel)
            shift
            while [[ $# -gt 0 && "$1" != --* ]]; do
                CHANNELS+=("$1"); shift
            done
            ;;
        --abi)
            shift
            while [[ $# -gt 0 && "$1" != --* ]]; do
                ABIS+=("$1"); shift
            done
            ;;
        --no-offline) USE_OFFLINE=0; shift ;;
        --offline)    USE_OFFLINE=1; shift ;;
        --clean)      DO_CLEAN=1; shift ;;
        --skip-build) SKIP_BUILD=1; shift ;;
        --skip-upload) SKIP_UPLOAD=1; shift ;;
        -h|--help)
            print_usage; exit 0
            ;;
        *)
            echo "未知参数: $1" >&2
            print_usage >&2
            exit 2
            ;;
    esac
done

# 默认全量
[[ ${#CHANNELS[@]} -eq 0 ]] && CHANNELS=("${ALL_CHANNELS[@]}")
[[ ${#ABIS[@]} -eq 0 ]]     && ABIS=("${ALL_ABIS[@]}")

# 校验渠道 / 架构
for c in "${CHANNELS[@]}"; do
    if [[ ! " ${ALL_CHANNELS[*]} " =~ " $c " ]]; then
        echo "非法渠道: $c (合法值: ${ALL_CHANNELS[*]})" >&2; exit 2
    fi
done
for a in "${ABIS[@]}"; do
    if [[ ! " ${ALL_ABIS[*]} " =~ " $a " ]]; then
        echo "非法架构: $a (合法值: ${ALL_ABIS[*]})" >&2; exit 2
    fi
done

# ---- 前置检查 ---------------------------------------------------------------
if [[ "$SKIP_BUILD" -eq 0 ]]; then
    if [[ ! -x "$GRADLE" ]]; then
        echo "找不到 $GRADLE，请在项目根目录执行" >&2; exit 1
    fi
    if [[ "$(uname)" != "Linux" && ! -f "$KEYSTORE_FILE" ]]; then
        echo "缺少 $KEYSTORE_FILE，Release 签名配置会读取失败" >&2; exit 1
    fi
fi

# ---- 输出目录 ----------------------------------------------------------------
mkdir -p "$OUT_DIR"
# 清空旧的产物与清单（保留目录）
rm -f "$OUT_DIR"/*.apk "$OUT_DIR/manifest.txt"

# 构造本次要跑的 variant 列表
VARIANTS=()
for c in "${CHANNELS[@]}"; do
    for a in "${ABIS[@]}"; do
        VARIANTS+=("$(to_pascal <<< "$c")$(to_pascal <<< "$a")Release")
    done
done

echo "==> 渠道: ${CHANNELS[*]}"
echo "==> 架构: ${ABIS[*]}"
echo "==> 计划构建 ${#VARIANTS[@]} 个 variant: ${VARIANTS[*]}"
echo "==> 输出目录: $OUT_DIR/"
echo

# ---- 构建 --------------------------------------------------------------------
if [[ "$SKIP_BUILD" -eq 0 ]]; then
    GRADLE_ARGS=(":app:assemble${VARIANTS[0]}")
    for v in "${VARIANTS[@]:1}"; do
        GRADLE_ARGS+=(":app:assemble${v}")
    done

    if [[ "$DO_CLEAN" -eq 1 ]]; then
        echo "==> 清理…"
        "$GRADLE" clean
    fi

    OFFLINE_FLAG=()
    [[ "$USE_OFFLINE" -eq 1 ]] && OFFLINE_FLAG=("--offline")

    # R8 / lint 在大项目中容易 OOM：
    #   - android.r8.maxMemory: 给 R8 worker 分配更大堆
    #   - --max-workers=1: 串行执行避免并发内存峰值
    #   - -x lintVitalXxxRelease: 跳过 lint vital 检查(只是 release 门禁，不影响产物)
    R8_EXTRA=("-Pandroid.r8.maxMemory=8g" "--max-workers=1")
    for v in "${VARIANTS[@]}"; do
        R8_EXTRA+=("-x" "lintVital${v}" "-x" "lintVitalAnalyze${v}")
    done

    echo "==> 开始构建…"
    "$GRADLE" "${GRADLE_ARGS[@]}" ${OFFLINE_FLAG[@]+"${OFFLINE_FLAG[@]}"} "${R8_EXTRA[@]}"
    echo
fi

# ---- 归集产物 ----------------------------------------------------------------
echo "==> 归集产物到 $OUT_DIR/"
MANIFEST="$OUT_DIR/manifest.txt"
{
    echo "Wanbaohe Release 构建产物"
    echo "生成时间: $(date '+%Y-%m-%d %H:%M:%S')"
    echo "渠道: ${CHANNELS[*]}"
    echo "架构: ${ABIS[*]}"
    echo "------------------------------------------------------------"
    printf "%-50s %12s\n" "文件" "大小(MB)"
    echo "------------------------------------------------------------"
} > "$MANIFEST"

FOUND=0
for c in "${CHANNELS[@]}"; do
    for a in "${ABIS[@]}"; do
        variant_dir="app/build/outputs/apk/${c}${a}/release"
        # 取该目录下第一个 .apk（正常只有一个 release apk）
        apk="$(find "$variant_dir" -maxdepth 1 -name '*.apk' -type f 2>/dev/null | head -n1 || true)"
        if [[ -z "$apk" ]]; then
            echo "  [跳过] $c-$a: 未找到 $variant_dir/*.apk" >&2
            continue
        fi
        cp "$apk" "$OUT_DIR/"
        size_mb=$(du -m "$apk" | awk '{print $1}')
        printf "%-50s %12s\n" "$(basename "$apk")" "$size_mb" >> "$MANIFEST"
        FOUND=$((FOUND + 1))
    done
done

echo
cat "$MANIFEST"
echo
if [[ "$FOUND" -eq 0 ]]; then
    echo "未找到任何 APK，请检查上方构建日志。" >&2
    exit 1
fi

# ---- Crashlytics mapping 上传 (仅 google 渠道) ---------------------------------
# google release 经 R8 混淆，但未应用 Crashlytics 插件，需手动上传 mapping.txt:
#   mapping 文件: app/build/outputs/mapping/Google<Abc>Release/mapping.txt
#   resource 文件: app/build/crashlytics/mappingfileid-<abi>.xml
#     (Gradle 任务 :app:writeCrashlyticsMappingFileIds 生成, id 与注入 APK 的一致)
# 上传失败不阻断发布, 但该版本崩溃堆栈将保持混淆, 需手动补传
if [[ " ${CHANNELS[*]} " =~ " google " && "$SKIP_UPLOAD" -eq 0 ]]; then
    echo
    echo "==> 上传 Crashlytics mapping (google 渠道)…"

    APP_ID="$(python3 -c '
import json
with open("app/src/google/assets/google-services.json") as f:
    data = json.load(f)
for c in data.get("client", []):
    info = c.get("client_info", {})
    if info.get("android_client_info", {}).get("package_name") == "com.shifenmiao.app":
        print(info.get("mobilesdk_app_id", ""))
        break
' 2>/dev/null || true)"

    if ! command -v firebase >/dev/null 2>&1; then
        echo "  [跳过] 未安装 firebase CLI (npm i -g firebase-tools), 请稍后手动上传 mapping"
    elif [[ -z "$APP_ID" ]]; then
        echo "  [跳过] 未能从 google-services.json 解析 com.shifenmiao.app 的 mobilesdk_app_id"
    else
        for a in "${ABIS[@]}"; do
            variant="Google$(to_pascal <<< "$a")Release"
            mapping="app/build/outputs/mapping/$variant/mapping.txt"
            resource_file="app/build/crashlytics/mappingfileid-$a.xml"
            if [[ ! -f "$mapping" || ! -f "$resource_file" ]]; then
                echo "  [跳过] google-$a: 缺 $mapping 或 $resource_file"
                continue
            fi
            upload_ok=0
            for attempt in 1 2; do
                # firebase CLI 在上传成功后的收尾阶段偶发报错且 exit code 不可靠
                # (实测成功输出后仍 exit 非 0), 以成功文案为准, 失败重试一次
                output="$(firebase crashlytics:mappingfile:upload --app "$APP_ID" --resource-file "$resource_file" "$mapping" 2>&1 || true)"
                echo "$output"
                if grep -q "Successfully uploaded mapping file" <<< "$output"; then
                    upload_ok=1
                    break
                fi
                echo "  [重试] google-$a 第 $attempt 次上传未成功" >&2
            done
            if [[ "$upload_ok" -eq 1 ]]; then
                echo "  [完成] google-$a mapping 已上传"
            else
                echo "  [警告] google-$a mapping 上传失败, 该版本崩溃堆栈将保持混淆" >&2
            fi
        done
    fi
fi

echo "完成，共 $FOUND 个 APK，路径: $OUT_DIR/"
