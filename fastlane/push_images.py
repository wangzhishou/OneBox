#!/usr/bin/env python3
"""把 fastlane/metadata/android/<locale>/images/ 下的商店图片推送到 Google Play Console。

用法:
    .venv/bin/python push_images.py --dry-run            # 只校验+预览差异, 不改动线上
    .venv/bin/python push_images.py --locale es-ES       # 推单个语言
    .venv/bin/python push_images.py                      # 推全部本地有 images/ 的语言

- 目录布局(fastlane supply 兼容,与 fetch_images.py 对应):
    metadata/android/<locale>/images/<imageType>/<NN>.<png|jpg>
  文件按文件名顺序即商店展示顺序;空目录 = 删除该类型全部线上图片
- imageType: icon / featureGraphic / phoneScreenshots /
             sevenInchScreenshots / tenInchScreenshots / tvBanner / tvScreenshots / wearScreenshots
- 推送前校验:png/jpg 格式;icon 必须 512x512;featureGraphic 必须 1024x500;截图单边 320~3840
- 比对方式:线上/本地有序 sha256 列表一致则跳过,否则 deleteall 后按序重传
- 只覆盖本地存在的 <imageType> 目录对应的图片类型;线上有而本地没有目录的类型不受影响
- 凭据规则与 fetch_listing.py 相同(fastlane/*.json 或 --key / PLAY_API_KEY_JSON)
- 需要 HTTPS_PROXY 访问 Google API 时自行 export(本机: HTTPS_PROXY=http://127.0.0.1:7890)
- 属生产操作: 先 --dry-run 核对, 正式推送前向用户确认
"""
import argparse
import glob
import hashlib
import os
import struct
import sys
import time
from urllib.parse import urlparse

import httplib2
from google.oauth2 import service_account
from googleapiclient.discovery import build
from googleapiclient.errors import HttpError
from googleapiclient.http import MediaFileUpload
from google_auth_httplib2 import AuthorizedHttp

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
HERE = os.path.dirname(os.path.abspath(__file__))
META = os.path.join(HERE, "metadata", "android")

IMAGE_TYPES = [
    "icon",
    "featureGraphic",
    "phoneScreenshots",
    "sevenInchScreenshots",
    "tenInchScreenshots",
    "tvBanner",
    "tvScreenshots",
    "wearScreenshots",
]
EXACT_DIMS = {"icon": (512, 512), "featureGraphic": (1024, 500)}  # 必须精确尺寸
SINGLE_IMAGE_TYPES = {"icon", "featureGraphic", "tvBanner"}
SCREENSHOT_MIN, SCREENSHOT_MAX = 320, 3840


def resolve_key(arg):
    if arg:
        return arg
    env = os.environ.get("PLAY_API_KEY_JSON")
    if env:
        return env
    candidates = glob.glob(os.path.join(HERE, "*.json"))
    if len(candidates) == 1:
        return candidates[0]
    sys.exit(f"无法确定 service account json:{candidates},请用 --key 指定")


def image_size(path):
    """无依赖读取 PNG/JPEG 宽高,失败返回 None。"""
    with open(path, "rb") as f:
        head = f.read(32)
        if head[:8] == b"\x89PNG\r\n\x1a\n":
            w, h = struct.unpack(">II", head[16:24])
            return w, h
        if head[:2] == b"\xff\xd8":  # JPEG: 扫描 SOF 段
            f.seek(2)
            while True:
                b = f.read(1)
                if not b:
                    return None
                if b != b"\xff":
                    continue
                marker = f.read(1)
                if marker in (b"\xc0", b"\xc1", b"\xc2", b"\xc3", b"\xc5", b"\xc6",
                              b"\xc7", b"\xc9", b"\xca", b"\xcb", b"\xcd", b"\xce", b"\xcf"):
                    f.read(3)
                    h, w = struct.unpack(">HH", f.read(4))
                    return w, h
                if marker in (b"\xd8", b"\xd9") or b"\xd0" <= marker <= b"\xd7":
                    continue
                seg_len = struct.unpack(">H", f.read(2))[0]
                f.seek(seg_len - 2, 1)
    return None


def load_local(locales):
    """读取本地 images/,{locale: {imageType: [(path, sha256)]}};同时做格式/尺寸校验。"""
    data, errors = {}, []
    for locale in locales:
        img_root = os.path.join(META, locale, "images")
        if not os.path.isdir(img_root):
            continue
        body = {}
        for image_type in IMAGE_TYPES:
            d = os.path.join(img_root, image_type)
            if not os.path.isdir(d):
                continue
            files = sorted(
                f for f in os.listdir(d)
                if os.path.splitext(f)[1].lower() in (".png", ".jpg", ".jpeg")
            )
            bad = [f for f in os.listdir(d)
                   if os.path.splitext(f)[1].lower() not in (".png", ".jpg", ".jpeg")
                   and not f.startswith(".")]
            for f in bad:
                errors.append(f"{locale}/{image_type}/{f}: 只支持 png/jpg")
            if image_type in SINGLE_IMAGE_TYPES and len(files) > 1:
                errors.append(f"{locale}/{image_type}: 该类型只允许 1 张图,现有 {len(files)} 张")
                continue
            entries = []
            for f in files:
                path = os.path.join(d, f)
                raw = open(path, "rb").read()
                size = image_size(path)
                if size is None:
                    errors.append(f"{locale}/{image_type}/{f}: 无法解析 PNG/JPEG 尺寸")
                    continue
                w, h = size
                if image_type in EXACT_DIMS and (w, h) != EXACT_DIMS[image_type]:
                    errors.append(
                        f"{locale}/{image_type}/{f}: {w}x{h},要求 {EXACT_DIMS[image_type][0]}x{EXACT_DIMS[image_type][1]}")
                elif image_type.endswith("Screenshots") and not (
                        SCREENSHOT_MIN <= min(w, h) and max(w, h) <= SCREENSHOT_MAX):
                    errors.append(
                        f"{locale}/{image_type}/{f}: {w}x{h},截图单边须在 {SCREENSHOT_MIN}~{SCREENSHOT_MAX}")
                entries.append((path, hashlib.sha256(raw).hexdigest()))
            body[image_type] = entries
        if body:
            data[locale] = body
    return data, errors


def make_service(key_path):
    creds = service_account.Credentials.from_service_account_file(key_path, scopes=SCOPES)
    proxy_url = os.environ.get("HTTPS_PROXY") or os.environ.get("https_proxy")
    if proxy_url:  # httplib2 不读 HTTPS_PROXY 环境变量,显式构造代理
        p = urlparse(proxy_url)
        proxy_info = httplib2.ProxyInfo(httplib2.socks.PROXY_TYPE_HTTP, p.hostname, p.port)
        http = AuthorizedHttp(creds, http=httplib2.Http(proxy_info=proxy_info))
        return build("androidpublisher", "v3", http=http, cache_discovery=False)
    return build("androidpublisher", "v3", credentials=creds, cache_discovery=False)


def upload_with_retry(edits, package, edit_id, locale, image_type, path, attempts=4):
    """单张上传,失败重试(非 resumable 单发 POST,避免慢代理下 resumable 会话超时)。"""
    mime = "image/png" if path.lower().endswith(".png") else "image/jpeg"
    for i in range(attempts):
        try:
            edits.images().upload(
                packageName=package, editId=edit_id,
                language=locale, imageType=image_type,
                media_body=MediaFileUpload(path, mimetype=mime, resumable=False),
            ).execute()
            return
        except HttpError as e:
            retriable = e.resp.status in (408, 429) or e.resp.status >= 500
            if not retriable or i == attempts - 1:
                raise
            wait = 10 * (i + 1)
            print(f"  ! HTTP {e.resp.status},{wait}s 后重试 ({i + 1}/{attempts - 1}): {os.path.basename(path)}")
            time.sleep(wait)
        except (httplib2.HttpLib2Error, TimeoutError, ConnectionError, OSError) as e:
            if i == attempts - 1:
                raise
            wait = 10 * (i + 1)
            print(f"  ! 网络错误({e}),{wait}s 后重试 ({i + 1}/{attempts - 1}): {os.path.basename(path)}")
            time.sleep(wait)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--key")
    ap.add_argument("--package", default="com.shifenmiao.app")
    ap.add_argument("--locale", help="只推某个语言(如 es-ES);默认推 metadata/android/ 下全部含 images/ 的语言")
    ap.add_argument("--dry-run", action="store_true", help="只校验和预览差异,不改动线上")
    args = ap.parse_args()

    if args.locale:
        locales = [args.locale]
    else:
        locales = sorted(
            d for d in os.listdir(META)
            if os.path.isdir(os.path.join(META, d, "images"))
        ) if os.path.isdir(META) else []
    if not locales:
        sys.exit(f"没有找到任何含 images/ 的 locale 目录:{META}")

    local, errors = load_local(locales)
    if errors:
        sys.exit("校验失败,未推送:\n" + "\n".join(errors))
    if not local:
        sys.exit("所选 locale 下没有任何图片目录,未推送。")

    service = make_service(resolve_key(args.key))
    edits = service.edits()

    # 每个 locale 独立 edit + commit:单语言失败不影响已完成的语言,重跑时靠 sha256 自动跳过
    total_changed = 0
    for locale, types in sorted(local.items()):
        edit = edits.insert(body={}, packageName=args.package).execute()
        edit_id = edit["id"]
        print(f"edit created: {edit_id} ({locale})")

        committed = False
        try:
            changed = []
            for image_type, entries in sorted(types.items()):
                try:
                    online = edits.images().list(
                        packageName=args.package, editId=edit_id,
                        language=locale, imageType=image_type,
                    ).execute().get("images", [])
                except HttpError as e:
                    if e.resp.status == 404:  # 该语言线上还没有 listing,视为无线上图
                        online = []
                    else:
                        raise
                online_hashes = [img.get("sha256", "") for img in online]
                local_hashes = [h for _, h in entries]
                if online_hashes == local_hashes:
                    print(f"[{locale}/{image_type}] 无变化({len(entries)} 张)")
                    continue
                print(f"[{locale}/{image_type}] 变更:线上 {len(online)} 张 → 本地 {len(entries)} 张")
                changed.append((image_type, entries))

            if args.dry_run:
                continue
            if not changed:
                continue

            for image_type, entries in changed:
                edits.images().deleteall(
                    packageName=args.package, editId=edit_id,
                    language=locale, imageType=image_type,
                ).execute()
                for path, _ in entries:
                    upload_with_retry(edits, args.package, edit_id, locale, image_type, path)
                    print(f"uploaded: {locale}/{image_type}/{os.path.basename(path)}")
            edits.commit(packageName=args.package, editId=edit_id).execute()
            committed = True
            total_changed += len(changed)
            print(f"edit committed: {locale} 的 {len(changed)} 个类型已上线")
        finally:
            if not committed:
                try:
                    edits.delete(packageName=args.package, editId=edit_id).execute()
                    print(f"edit deleted ({locale} 未提交,线上未改动)")
                except HttpError:
                    pass  # edit 可能已过期,忽略

    if args.dry_run:
        print("\n[dry-run] 未改动线上。")
    else:
        print(f"\n全部完成:共 {total_changed} 个 locale/类型组合已上线")


if __name__ == "__main__":
    main()
