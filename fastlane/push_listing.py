#!/usr/bin/env python3
"""把 fastlane/metadata/android/ 下的本地 listing 推送到 Google Play Console。

用法:
    .venv/bin/python push_listing.py --dry-run            # 只校验+预览, 不改动线上
    .venv/bin/python push_listing.py --locale es-ES       # 推单个语言
    .venv/bin/python push_listing.py                      # 推全部本地语言

- 凭据规则与 fetch_listing.py 相同(fastlane/*.json 或 --key / PLAY_API_KEY_JSON)
- 需要 HTTPS_PROXY 访问 Google API 时自行 export(本机: HTTPS_PROXY=http://127.0.0.1:7890)
- 只推 listing(title/short/full/video);changelog 跟随版本发布走 edits.tracks,不在本脚本范围
- 推送前强制字符校验: title ≤30 / short_description ≤80 / full_description ≤4000 / video 为 YouTube URL
- 属生产操作: 先 --dry-run 核对, 正式推送前向用户确认
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
META = os.path.join(HERE, "metadata", "android")

FIELDS = [  # (文件名, API 字段, 字符上限)
    ("title.txt", "title", 30),
    ("short_description.txt", "shortDescription", 80),
    ("full_description.txt", "fullDescription", 4000),
    ("video.txt", "video", None),
]


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


def load_local(locales):
    """读取本地 listing,{locale: {api_field: text}};同时做字符校验。"""
    data, errors = {}, []
    for locale in locales:
        d = os.path.join(META, locale)
        if not os.path.isdir(d):
            errors.append(f"{locale}: 目录不存在 {d}")
            continue
        body = {}
        for fname, field, limit in FIELDS:
            path = os.path.join(d, fname)
            if not os.path.exists(path):
                continue
            text = open(path, encoding="utf-8").read().strip()
            if not text:
                continue
            if limit and len(text) > limit:
                errors.append(f"{locale}/{fname}: {len(text)} 字符,超过上限 {limit}")
            body[field] = text
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


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--key")
    ap.add_argument("--package", default="com.shifenmiao.app")
    ap.add_argument("--locale", help="只推某个语言(如 es-ES);默认推 metadata/android/ 下全部")
    ap.add_argument("--dry-run", action="store_true", help="只校验和预览差异,不改动线上")
    args = ap.parse_args()

    if args.locale:
        locales = [args.locale]
    else:
        locales = sorted(
            d for d in os.listdir(META) if os.path.isdir(os.path.join(META, d))
        ) if os.path.isdir(META) else []
    if not locales:
        sys.exit(f"没有找到任何 locale 目录:{META}")

    local, errors = load_local(locales)
    if errors:
        sys.exit("校验失败,未推送:\n" + "\n".join(errors))

    service = make_service(resolve_key(args.key))
    edits = service.edits()
    edit = edits.insert(body={}, packageName=args.package).execute()
    edit_id = edit["id"]
    print(f"edit created: {edit_id}")

    try:
        online = {
            item["language"]: item
            for item in edits.listings().list(packageName=args.package, editId=edit_id)
            .execute().get("listings", [])
        }
        changed = []
        for locale, body in sorted(local.items()):
            cur = online.get(locale, {})
            diff = [f for f in body if cur.get(f, "") != body[f]]
            status = f"变更字段 {diff}" if diff else "无变化"
            print(f"[{locale}] {status};title={body.get('title', '')!r}")
            if diff:
                changed.append((locale, body))

        if args.dry_run:
            print(f"\n[dry-run] {len(changed)}/{len(local)} 个 locale 有变更,未推送。")
            return

        if not changed:
            print("全部无变化,无需推送。")
            return

        for locale, body in changed:
            body = dict(body, language=locale)
            edits.listings().update(
                packageName=args.package, editId=edit_id, language=locale, body=body
            ).execute()
            print(f"updated: {locale}")
        edits.commit(packageName=args.package, editId=edit_id).execute()
        print(f"edit committed: {len(changed)} 个 locale 已上线")
    finally:
        if args.dry_run:
            edits.delete(packageName=args.package, editId=edit_id).execute()
            print("edit deleted (dry-run 未改动线上)")


if __name__ == "__main__":
    main()
