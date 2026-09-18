#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""把已打好的 APK 发布到 GitCode 镜像仓库。

为什么需要这个脚本
------------------
GitCode 的仓库镜像只同步**分支 / 标签 / 提交**,不同步 GitHub Release 及其附件
(官方文档: 「Wiki、Issue、PR 及 Release 暂不支持迁移和导入」)。而国内渠道 App 的
更新提醒读的是 GitCode OpenAPI(见 core/network/.../OpenSourceReleaseChecker.kt),
所以国内发版必须在 GitCode 上也建一次 release、传一次包,否则国内客户端永远查不到新版本。

用法
----
    # 1) 先在 https://gitcode.com/setting/token-classic 生成访问令牌
    export GITCODE_TOKEN=xxxxxxxx

    # 2) 发布(把 --apk 换成实际产物;国内渠道包由 build_release.sh 产出到 release/)
    ./scripts/publish_gitcode_release.py --tag 1.4.0 --dir release \
        --channels xiaomi yyb oppo vivo huawei onebox --abi arm64

    # 想先看看要做什么:
    ./scripts/publish_gitcode_release.py --tag 1.4.0 --dir release --dry-run

说明
----
- 幂等:同名 release 已存在时不会重建(只补传缺失的附件);上传失败会打印建议的手工步骤。
- 附件走 GitCode 给的华为 OBS 预签名 PUT,单文件 50-60MB 的 APK 实测可用;
  官方文档未写单文件上限与同名覆盖策略,所以脚本对已存在的同名附件默认跳过
  (需要覆盖时加 --overwrite,脚本会先删同名附件再传)。
- GitCode 侧要求仓库**公开**,否则 API 一律 403。
"""

from __future__ import annotations

import argparse
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request

API_BASE = "https://api.gitcode.com/api/v5"
REPO = "wangzhishou/OneBox"
RELEASES_PAGE = f"https://gitcode.com/{REPO}/releases"
DOMESTIC_CHANNELS = ["xiaomi", "yyb", "oppo", "vivo", "huawei", "onebox"]


def request(method: str, url: str, token: str, body=None, headers=None, raw=False):
    data = None
    req_headers = {"User-Agent": "OneBox-release-script"}
    if body is not None:
        data = json.dumps(body).encode("utf-8")
        req_headers["Content-Type"] = "application/json"
    if headers:
        req_headers.update(headers)
    if token:
        req_headers["private-token"] = token
    req = urllib.request.Request(url, data=data, headers=req_headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            payload = resp.read()
            if raw:
                return resp.status, payload
            text = payload.decode("utf-8", "replace")
            return resp.status, (json.loads(text) if text.strip() else None)
    except urllib.error.HTTPError as exc:
        detail = exc.read().decode("utf-8", "replace")
        return exc.code, {"_error": detail}


def with_token(url: str, token: str) -> str:
    parts = urllib.parse.urlsplit(url)
    query = urllib.parse.parse_qsl(parts.query)
    query.append(("access_token", token))
    return urllib.parse.urlunsplit(parts._replace(query=urllib.parse.urlencode(query)))


def create_release(tag: str, token: str, body: str, dry_run: bool) -> None:
    url = with_token(f"{API_BASE}/repos/{REPO}/releases", token)
    payload = {
        "tag_name": tag,
        "name": tag,
        "body": body or f"OneBox {tag}",
        "target_commitish": tag,
        "release_status": "latest" if "-" not in tag else "pre",
    }
    if dry_run:
        print(f"[dry-run] POST {url} {json.dumps(payload, ensure_ascii=False)}")
        return
    status, resp = request("POST", url, token, body=payload)
    if status in (200, 201):
        print(f"[ok] 已创建 GitCode release {tag}")
    elif status == 400 and "已存在" in json.dumps(resp, ensure_ascii=False):
        print(f"[skip] GitCode release {tag} 已存在")
    else:
        print(f"[warn] 创建 release 返回 {status}: {json.dumps(resp, ensure_ascii=False)[:300]}")


def list_attachments(tag: str, token: str):
    status, resp = request("GET", with_token(f"{API_BASE}/repos/{REPO}/releases/tags/{tag}", token), token)
    if status != 200 or not isinstance(resp, dict):
        return []
    return [a.get("name") for a in (resp.get("assets") or []) if a.get("name")]


def delete_attachment(tag: str, name: str, token: str) -> None:
    status, _ = request(
        "DELETE",
        with_token(f"{API_BASE}/repos/{REPO}/releases/{tag}/attach_files/{urllib.parse.quote(name)}", token),
        token,
    )
    print(f"[{'ok' if status in (200, 204) else 'warn'}] 删除同名附件 {name} -> {status}")


def upload(tag: str, path: str, token: str, overwrite: bool, existing, dry_run: bool) -> bool:
    name = os.path.basename(path)
    size = os.path.getsize(path)
    if name in existing and not overwrite:
        print(f"[skip] {name} 已存在于 GitCode({size / 1048576:.1f}MB)")
        return True
    if name in existing and overwrite and not dry_run:
        delete_attachment(tag, name, token)

    if dry_run:
        print(f"[dry-run] 上传 {name}({size / 1048576:.1f}MB)→ {RELEASES_PAGE}")
        return True

    meta_url = with_token(
        f"{API_BASE}/repos/{REPO}/releases/{tag}/upload_url?file_name={urllib.parse.quote(name)}", token
    )
    status, resp = request("GET", meta_url, token)
    if status != 200 or not isinstance(resp, dict) or not resp.get("url"):
        print(f"[fail] {name}: 取上传地址失败 {status} {json.dumps(resp, ensure_ascii=False)[:200]}")
        return False

    # 预签名地址是华为 OBS:必须原样带上 GitCode 返回的 headers,且不要再附加 access_token
    put_headers = {k: str(v) for k, v in (resp.get("headers") or {}).items()}
    put_headers.setdefault("Content-Type", "application/octet-stream")
    with open(path, "rb") as fh:
        data = fh.read()
    # 预签名 PUT 要发二进制 + 自定义 header,直接用 Request(不要带 access_token,签名里已经含权限)
    req = urllib.request.Request(resp["url"], data=data, headers=put_headers, method="PUT")
    try:
        with urllib.request.urlopen(req, timeout=600) as r:
            code = r.status
    except urllib.error.HTTPError as exc:
        code = exc.code
        print(f"[fail] {name}: PUT 失败 {code} {exc.read().decode('utf-8', 'replace')[:200]}")
        return False
    if 200 <= code < 300:
        print(f"[ok] 已上传 {name}({size / 1048576:.1f}MB)")
        return True
    print(f"[fail] {name}: PUT 返回 {code}")
    return False


def collect_apks(args) -> list:
    paths = list(args.apk or [])
    if args.dir:
        for dirpath, _dirnames, filenames in os.walk(args.dir):
            for fn in sorted(filenames):
                if fn.endswith(".apk"):
                    paths.append(os.path.join(dirpath, fn))
    if args.channels:
        wanted = set(args.channels)
        paths = [p for p in paths if any(f"-{c}-" in os.path.basename(p) for c in wanted)]
    if args.abi:
        paths = [p for p in paths if f"-{args.abi}-" in os.path.basename(p)]
    if args.tag:
        paths = [p for p in paths if f"-{args.tag.lstrip('v')}-" in os.path.basename(p)] or paths
    seen, unique = set(), []
    for p in paths:
        if p not in seen:
            seen.add(p)
            unique.append(p)
    return unique


def main() -> int:
    parser = argparse.ArgumentParser(description="发布 APK 到 GitCode 镜像仓库(国内渠道更新源)")
    parser.add_argument("--tag", required=True, help="版本 tag,如 1.4.0(与 GitHub Release 一致)")
    parser.add_argument("--apk", nargs="*", help="要上传的 APK 路径(可多个)")
    parser.add_argument("--dir", help="从目录里收集 APK(会递归查找)")
    parser.add_argument("--channels", nargs="*", default=DOMESTIC_CHANNELS, help="只上传这些渠道的包")
    parser.add_argument("--abi", help="只上传该架构的包,如 arm64 / universal")
    parser.add_argument("--body", default="", help="release 说明(默认用 tag)")
    parser.add_argument("--overwrite", action="store_true", help="覆盖 GitCode 上已有的同名附件")
    parser.add_argument("--dry-run", action="store_true", help="只打印将要执行的动作")
    args = parser.parse_args()

    token = os.environ.get("GITCODE_TOKEN", "").strip()
    if not token and not args.dry_run:
        print("缺少 GITCODE_TOKEN(在 https://gitcode.com/setting/token-classic 生成后导出)", file=sys.stderr)
        return 2

    apks = collect_apks(args)
    if not apks:
        # 不算失败:tag 推送只出 google/foss 包,国内渠道包由手动 dispatch 或本地
        # build_release.sh 产出。CI 里这一步在"这次没打国内包"时就该安静跳过。
        print(f"没有匹配到要上传的 APK(--dir={args.dir} --channels={args.channels} --abi={args.abi}),跳过")
        return 0

    print(f"tag={args.tag} 待上传 {len(apks)} 个包:")
    for p in apks:
        print(f"  - {os.path.basename(p)} ({os.path.getsize(p) / 1048576:.1f}MB)")

    create_release(args.tag, token, args.body, args.dry_run)
    existing = [] if args.dry_run else list_attachments(args.tag, token)
    ok = all(upload(args.tag, p, token, args.overwrite, existing, args.dry_run) for p in apks)

    print(f"\nGitCode release: {RELEASES_PAGE}")
    if not ok:
        print("有附件上传失败。GitCode 的附件上限/覆盖策略官方没有文档,失败时可到发行版编辑页手工补传。")
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
