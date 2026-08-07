# Play Store listing 维护

本目录以 [fastlane supply](https://docs.fastlane.tools/actions/supply/) 兼容格式维护 OneBox(`com.shifenmiao.app`)的 Google Play 商店文案:

```
metadata/android/<locale>/title.txt              # 应用标题(≤30 字符)
metadata/android/<locale>/short_description.txt  # 简短描述(≤80 字符)
metadata/android/<locale>/full_description.txt   # 完整描述(≤4000 字符)
metadata/android/<locale>/changelogs/<versionCode>.txt  # 各版本更新说明
```

现有 locale:`en-US`(基准)、`zh-CN`、`es-ES`、`pt-BR`、`id`、`hi-IN`、`ru-RU`、`tr-TR`、`ja-JP`、`ko-KR`、`fil`、`de-DE`。新增语言时按 Play 语言码建目录(如 `pt-BR`、`hi-IN`),从 `en-US` 翻译,注意上面的字符限制;品牌词 OneBox 不翻。

## 商店图片(按语言)

每个 locale 下可放 `images/<imageType>/<NN>.<png|jpg>`,文件名顺序即商店展示顺序:

```
metadata/android/<locale>/images/icon/01.png              # 高分辨率图标,必须 512x512
metadata/android/<locale>/images/featureGraphic/01.png    # 功能图,必须 1024x500
metadata/android/<locale>/images/phoneScreenshots/01.png  # 手机截图,单边 320~3840,最多 8 张
metadata/android/<locale>/images/sevenInchScreenshots/    # 7 寸平板(可选)
metadata/android/<locale>/images/tenInchScreenshots/      # 10 寸平板(可选)
```

`fetch_images.py` 拉取线上原图覆盖到本目录(内部给图片 URL 追加 `=s0` 拿原图,裸 URL 只有 ≤512px 预览);`push_images.py` 按语言/类型推送,有序 sha256 一致则跳过,否则 deleteall 后按序重传(本地空目录 = 删除该类型全部线上图):

```bash
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python fetch_images.py              # 拉线上图(只读)
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python push_images.py --dry-run     # 预览差异
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python push_images.py [--locale de-DE]  # 正式推
```

权限与 listing 推送相同("管理商店商品详情"),失败自动 delete edit 回滚。注意线上各语言的截图**互不复用**:新语言不配图则展示 en-US 的图。

## 抓取线上文案

`fetch_listing.py` 用 Play Developer API 只读拉取线上 listing 与 changelog 覆盖到本目录:

```bash
cd fastlane
python3 -m venv .venv && .venv/bin/pip install google-api-python-client google-auth pysocks
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python fetch_listing.py   # 本机访问 Google API 需 Clash 代理
```

凭据:Play Console → API 访问 创建的服务账号 JSON,放本目录下(任意 `*.json`,已 gitignore,**勿入库**),或用 `--key` / 环境变量 `PLAY_API_KEY_JSON` 指定。脚本只创建临时 edit 读取后即删除,不会改动 Play Console。

## 推送改动

`push_listing.py` 把本地文案推到 Play Console(覆盖所推 locale 的 title/short/full/video 四字段;内部先校验字符、列差异):

```bash
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python push_listing.py --dry-run            # 预览差异
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python push_listing.py [--locale es-ES]     # 正式推
```

生产操作:先 dry-run 核对再推。changelog 不随 listing 推(跟随版本发布)。服务账号需"管理商店商品详情"权限,403 时去 Play Console → 用户和权限授权。配套 skill:`~/.kimi-code/skills/play-publish/`。

## 发布版本(AAB)

`push_release.py` 把本地构建的 AAB 上传并发布到指定轨道(内部 edits.insert → bundles.upload → tracks.update → edits.commit,失败自动 delete edit 回滚;版本名形如 `130 (1.3.0)`,changelog 取 `metadata/android/<locale>/changelogs/<versionCode>.txt`,单条 ≤500 字符):

```bash
./gradlew :app:bundleGoogleUniversalRelease                        # 构建 AAB(在 android/ 下)
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python push_release.py --dry-run    # 预览(默认最新 AAB + production)
HTTPS_PROXY=http://127.0.0.1:7890 .venv/bin/python push_release.py              # 正式发布
```

可选参数:`--aab <路径>`、`--track internal|alpha|beta|production`、`--status draft|completed`。服务账号需"发布应用"(Releases)权限。`tracks.update` 会整体替换该轨道 releases,completed 全量发布时旧版本自然被取代。
