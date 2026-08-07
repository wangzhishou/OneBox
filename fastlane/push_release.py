#!/usr/bin/env python3
"""把本地构建的 AAB 上传并发布到 Google Play 指定轨道。

用法:
    .venv/bin/python push_release.py --dry-run                      # 只预览, 不改动线上
    .venv/bin/python push_release.py                                # 上传 AAB 并发布到 production
    .venv/bin/python push_release.py --track internal               # 发到内测轨道
    .venv/bin/python push_release.py --aab path/to/app.aab          # 指定 AAB 路径

- 凭据规则与 push_listing.py 相同(fastlane/*.json 或 --key / PLAY_API_KEY_JSON)
- 需要 HTTPS_PROXY 访问 Google API 时自行 export(本机: HTTPS_PROXY=http://127.0.0.1:7890)
- changelog 从 metadata/android/<locale>/changelogs/<versionCode>.txt 读取(单条 ≤500 字符),
  versionCode 在 AAB 上传后由 Play 返回;没有 changelog 也能发布(release 不带 releaseNotes)
- 发布版本名形如 "130 (1.3.0)",versionName 默认取 gradle/libs.versions.toml
- tracks.update 会整体替换该轨道的 releases(completed 全量发布时旧版本自然被取代)
- 属生产操作: 先 --dry-run 核对, 正式推送前向用户确认;失败自动 delete edit 回滚
"""
import argparse
import glob
import os
import re
import sys

from googleapiclient.http import MediaFileUpload

from push_listing import make_service, resolve_key

HERE = os.path.dirname(os.path.abspath(__file__))
META = os.path.join(HERE, "metadata", "android")
DEFAULT_AAB_GLOB = os.path.join(
    HERE, "..", "app", "build", "outputs", "bundle", "googleUniversalRelease", "*.aab"
)
VERSIONS_TOML = os.path.join(HERE, "..", "gradle", "libs.versions.toml")
NOTES_LIMIT = 500


def resolve_aab(arg):
    if arg:
        if not os.path.isfile(arg):
            sys.exit(f"AAB 不存在:{arg}")
        return arg
    candidates = sorted(glob.glob(DEFAULT_AAB_GLOB), key=os.path.getmtime)
    if not candidates:
        sys.exit(f"找不到 AAB:{DEFAULT_AAB_GLOB},请用 --aab 指定")
    return candidates[-1]


def read_version_name(arg):
    if arg:
        return arg
    text = open(VERSIONS_TOML, encoding="utf-8").read()
    m = re.search(r'^versionName\s*=\s*"([^"]+)"', text, re.M)
    if not m:
        sys.exit(f"无法从 {VERSIONS_TOML} 解析 versionName,请用 --version-name 指定")
    return m.group(1)


def load_notes(version_code):
    """读取 {locale: changelog},{locale: text};同时做字符校验。"""
    notes, errors = {}, []
    if not os.path.isdir(META):
        return notes, errors
    for locale in sorted(os.listdir(META)):
        path = os.path.join(META, locale, "changelogs", f"{version_code}.txt")
        if not os.path.isfile(path):
            continue
        text = open(path, encoding="utf-8").read().strip()
        if not text:
            continue
        if len(text) > NOTES_LIMIT:
            errors.append(f"{locale}/changelogs/{version_code}.txt: {len(text)} 字符,超过上限 {NOTES_LIMIT}")
        notes[locale] = text
    return notes, errors


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--key")
    ap.add_argument("--package", default="com.shifenmiao.app")
    ap.add_argument("--aab", help=f"AAB 路径;默认取最新 {DEFAULT_AAB_GLOB}")
    ap.add_argument("--track", default="production", help="目标轨道(production/beta/alpha/internal)")
    ap.add_argument("--status", default="completed", choices=["completed", "draft"], help="发布状态")
    ap.add_argument("--version-name", help="发布版本名;默认读 gradle/libs.versions.toml")
    ap.add_argument("--dry-run", action="store_true", help="只校验和预览,不上传不发布")
    args = ap.parse_args()

    aab = resolve_aab(args.aab)
    version_name = read_version_name(args.version_name)
    size_mb = os.path.getsize(aab) / 1024 / 1024
    print(f"AAB: {aab} ({size_mb:.1f} MB)")
    print(f"目标: package={args.package} track={args.track} status={args.status}")

    service = make_service(resolve_key(args.key))
    edits = service.edits()
    edit = edits.insert(body={}, packageName=args.package).execute()
    edit_id = edit["id"]
    print(f"edit created: {edit_id}")

    try:
        track = edits.tracks().get(
            packageName=args.package, editId=edit_id, track=args.track
        ).execute()
        current = [
            f"{r.get('name')} [{r.get('status')}]" for r in track.get("releases", [])
        ]
        print(f"当前 {args.track} 轨道: {current or '(空)'}")

        if args.dry_run:
            print(f"\n[dry-run] 将上传 {aab} 并以 completed/draft 形式发布到 {args.track},未执行。")
            return

        print("上传 AAB 中...")
        bundle = edits.bundles().upload(
            packageName=args.package,
            editId=edit_id,
            media_body=MediaFileUpload(aab, mimetype="application/octet-stream"),
        ).execute()
        version_code = bundle["versionCode"]
        print(f"上传完成: versionCode={version_code} sha256={bundle.get('sha256', '')[:12]}...")

        notes, errors = load_notes(version_code)
        if errors:
            sys.exit("changelog 校验失败,edit 已回滚:\n" + "\n".join(errors))

        release = {
            "name": f"{version_code} ({version_name})",
            "versionCodes": [str(version_code)],
            "status": args.status,
        }
        if notes:
            release["releaseNotes"] = [
                {"language": locale, "text": text} for locale, text in sorted(notes.items())
            ]
            print(f"changelog: {sorted(notes)}")
        else:
            print(f"警告: 未找到 changelogs/{version_code}.txt,发布不带 releaseNotes")

        edits.tracks().update(
            packageName=args.package,
            editId=edit_id,
            track=args.track,
            body={"track": args.track, "releases": [release]},
        ).execute()
        edits.commit(packageName=args.package, editId=edit_id).execute()
        print(f"edit committed: {release['name']} 已发布到 {args.track} ({args.status})")
    finally:
        # dry-run 或任何中途失败:删除未提交的 edit,不改动线上
        if args.dry_run or sys.exc_info()[0] is not None:
            try:
                edits.delete(packageName=args.package, editId=edit_id).execute()
                print("edit deleted (未改动线上)")
            except Exception as e:  # edit 已 commit 过会删除失败,忽略
                print(f"edit delete skipped: {e}")


if __name__ == "__main__":
    main()
