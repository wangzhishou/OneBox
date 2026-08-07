#!/usr/bin/env python3
"""从 Google Play Console 拉取应用 listing 与 changelog 到 fastlane/metadata/android/。

用法:
    .venv/bin/python fetch_listing.py [--key <service-account.json>] [--package com.shifenmiao.app]

- key 默认取环境变量 PLAY_API_KEY_JSON,再退化为 fastlane/ 目录下唯一的 *.json(该文件被 gitignore,勿入库)
- 如需通过代理访问 Google API, 先 export HTTPS_PROXY=http://<host>:<port>(脚本会读取该环境变量)
- 只读操作:创建 edit → 读取 → 删除 edit,不会改动 Play Console 任何数据
"""
import argparse
import glob
import os
import sys
from urllib.parse import urlparse

import httplib2
from google.oauth2 import service_account
from googleapiclient.discovery import build
from google_auth_httplib2 import AuthorizedHttp

SCOPES = ["https://www.googleapis.com/auth/androidpublisher"]
HERE = os.path.dirname(os.path.abspath(__file__))


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


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--key")
    ap.add_argument("--package", default="com.shifenmiao.app")
    args = ap.parse_args()

    creds = service_account.Credentials.from_service_account_file(
        resolve_key(args.key), scopes=SCOPES
    )
    # httplib2 不读 HTTPS_PROXY 环境变量,显式构造代理 Http
    proxy_url = os.environ.get("HTTPS_PROXY") or os.environ.get("https_proxy")
    if proxy_url:
        p = urlparse(proxy_url)
        proxy_info = httplib2.ProxyInfo(httplib2.socks.PROXY_TYPE_HTTP, p.hostname, p.port)
        http = AuthorizedHttp(creds, http=httplib2.Http(proxy_info=proxy_info))
        service = build("androidpublisher", "v3", http=http, cache_discovery=False)
    else:
        service = build("androidpublisher", "v3", credentials=creds, cache_discovery=False)
    edits = service.edits()
    edit = edits.insert(body={}, packageName=args.package).execute()
    edit_id = edit["id"]
    print(f"edit created: {edit_id}")

    out_root = os.path.join(HERE, "metadata", "android")
    try:
        listings = edits.listings().list(packageName=args.package, editId=edit_id).execute()
        for item in listings.get("listings", []):
            lang = item["language"]
            d = os.path.join(out_root, lang)
            os.makedirs(d, exist_ok=True)
            for api_field, fname in [
                ("title", "title.txt"),
                ("shortDescription", "short_description.txt"),
                ("fullDescription", "full_description.txt"),
                ("video", "video.txt"),
            ]:
                val = item.get(api_field)
                if val:
                    with open(os.path.join(d, fname), "w", encoding="utf-8") as f:
                        f.write(val)
            print(f"listing {lang}: title={item.get('title', '')!r}")

        # changelog:遍历所有 track 的 release,按 versionCode 收集 releaseNotes
        tracks = edits.tracks().list(packageName=args.package, editId=edit_id).execute()
        for track in tracks.get("tracks", []):
            for rel in track.get("releases", []):
                notes = rel.get("releaseNotes") or []
                codes = rel.get("versionCodes") or []
                if not notes or not codes:
                    continue
                vc = codes[0]
                for note in notes:
                    d = os.path.join(out_root, note["language"], "changelogs")
                    os.makedirs(d, exist_ok=True)
                    path = os.path.join(d, f"{vc}.txt")
                    if not os.path.exists(path):
                        with open(path, "w", encoding="utf-8") as f:
                            f.write(note.get("text", ""))
                        print(f"changelog {note['language']}/{vc}.txt (track {track['track']})")
    finally:
        edits.delete(packageName=args.package, editId=edit_id).execute()
        print("edit deleted (只读,未改动 Play Console)")


if __name__ == "__main__":
    main()
