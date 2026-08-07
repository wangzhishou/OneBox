#!/usr/bin/env python3
"""从 Google Play Console 拉取各语言的商店图片到 fastlane/metadata/android/<locale>/images/。

用法:
    .venv/bin/python fetch_images.py [--key <service-account.json>] [--package com.shifenmiao.app] [--locale en-US]

- 目录布局(fastlane supply 兼容,与 push_images.py 对应):
    metadata/android/<locale>/images/<imageType>/<NN>.<png|jpg>
  imageType: icon / featureGraphic / phoneScreenshots /
             sevenInchScreenshots / tenInchScreenshots / tvBanner / tvScreenshots / wearScreenshots
- 凭据规则与 fetch_listing.py 相同(fastlane/*.json 或 --key / PLAY_API_KEY_JSON)
- 如需通过代理访问 Google API, 先 export HTTPS_PROXY=http://<host>:<port>(脚本会读取该环境变量)
- 只读操作:创建 edit → 读取 → 删除 edit,不会改动 Play Console 任何数据
"""
import argparse
import glob
import os
import re
import sys
from urllib.parse import urlparse

import httplib2
from google.oauth2 import service_account
from googleapiclient.discovery import build
from google_auth_httplib2 import AuthorizedHttp

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
HERE = os.path.dirname(os.path.abspath(__file__))

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


def resolve_key(arg):
    if arg:
        return arg
    env = os.environ.get("PLAY_API_KEY_JSON")
    if env:
        return env
    candidates = [p for p in glob.glob(os.path.join(HERE, "*.json"))]
    if len(candidates) == 1:
        return candidates[0]
    sys.exit(f"无法确定 service account json:{candidates},请用 --key 指定")


def make_http(creds):
    """构造带凭据的 Http;httplib2 不读 HTTPS_PROXY 环境变量,显式构造代理。"""
    proxy_url = os.environ.get("HTTPS_PROXY") or os.environ.get("https_proxy")
    if proxy_url:
        p = urlparse(proxy_url)
        proxy_info = httplib2.ProxyInfo(httplib2.socks.PROXY_TYPE_HTTP, p.hostname, p.port)
        return AuthorizedHttp(creds, http=httplib2.Http(proxy_info=proxy_info))
    return AuthorizedHttp(creds, http=httplib2.Http())


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--key")
    ap.add_argument("--package", default="com.shifenmiao.app")
    ap.add_argument("--locale", help="只拉某个语言(如 en-US);默认拉线上全部有 listing 的语言")
    args = ap.parse_args()

    creds = service_account.Credentials.from_service_account_file(
        resolve_key(args.key), scopes=SCOPES
    )
    http = make_http(creds)
    service = build("androidpublisher", "v3", http=http, cache_discovery=False)
    edits = service.edits()
    edit = edits.insert(body={}, packageName=args.package).execute()
    edit_id = edit["id"]
    print(f"edit created: {edit_id}")

    out_root = os.path.join(HERE, "metadata", "android")
    try:
        if args.locale:
            locales = [args.locale]
        else:
            listings = edits.listings().list(packageName=args.package, editId=edit_id).execute()
            locales = sorted(item["language"] for item in listings.get("listings", []))
        print(f"locales: {', '.join(locales)}")

        for locale in locales:
            saved = 0
            for image_type in IMAGE_TYPES:
                resp = edits.images().list(
                    packageName=args.package, editId=edit_id,
                    language=locale, imageType=image_type,
                ).execute()
                images = resp.get("images", [])
                if not images:
                    continue
                d = os.path.join(out_root, locale, "images", image_type)
                os.makedirs(d, exist_ok=True)
                names = []
                for i, img in enumerate(images, 1):
                    url = img["url"] + "=s0"  # 原始尺寸(裸 URL 只返回 ≤512px 的预览)
                    resp_http, content = http.request(url)
                    if resp_http.status != 200:
                        print(f"  ! {locale}/{image_type} 第 {i} 张下载失败: HTTP {resp_http.status}")
                        continue
                    ext = ".png" if content[:8] == b"\x89PNG\r\n\x1a\n" else ".jpg"
                    name = f"{i:02d}{ext}"
                    with open(os.path.join(d, name), "wb") as f:
                        f.write(content)
                    names.append(name)
                    saved += 1
                # 清掉本地多余的旧文件(线上数量变少或格式变化时)
                for old in os.listdir(d):
                    if re.match(r"^\d+\.(png|jpe?g)$", old) and old not in names:
                        os.remove(os.path.join(d, old))
                print(f"{locale}/{image_type}: {len(images)} 张")
            if not saved:
                print(f"{locale}: 线上无图片")
    finally:
        edits.delete(packageName=args.package, editId=edit_id).execute()
        print("edit deleted (只读,未改动 Play Console)")


if __name__ == "__main__":
    main()
