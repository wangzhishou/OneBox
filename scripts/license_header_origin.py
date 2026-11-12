#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
OneBox 文件头来源标记工具。

按 git blob SHA 把 tracked 源文件与上游 ImageToolbox 比对（不下载正文），
再给文件头写入显式的 Origin: 标记。归属不再依赖包名或路径推断。

用法：
  python3 scripts/license_header_origin.py classify [--csv PATH]
  python3 scripts/license_header_origin.py apply [--groups LIST] [--write] [--force-review]

apply 默认 dry-run；只有传 --write 才写盘。U_UNMODIFIED（与上游逐字节一致）
永远跳过，NEEDS_REVIEW 的文件默认也跳过。
"""

import argparse
import csv
import hashlib
import json
import os
import re
import subprocess
import sys
import urllib.request

# --- 项目身份：如要换版权主体（公司法人 / GitHub ID 写法），只改 COPYRIGHT_HOLDER 一行 ---
PROJECT_NAME = "OneBox"
PROJECT_DESC = "a free AI agent and toolbox for Android"
COPYRIGHT_HOLDER = "zhishouwang(网址兽)"
COPYRIGHT_YEAR = "2026"

UPSTREAM_REPO = "T8RIN/ImageToolbox"
UPSTREAM_URL = "https://github.com/T8RIN/ImageToolbox"
UPSTREAM_AUTHOR = "T8RIN (Malik Mukhametzyanov)"
FORK_SNAPSHOT_DATE = "2026-08-08T00:00:00Z"

CACHE_DIR = "/tmp/onebox_header_audit"
SOURCE_EXTS = (".kt", ".kts", ".java", ".xml", ".properties", ".gradle")
# 第三方 vendored 代码，文件头/许可证一律保持原样
SKIP_PREFIXES = (
    "libs/richtext/", "libs/twain/", "libs/zoomable/",
    "feature/on-device-ocr/onnxruntime/",
)

# Apache-2.0 声明段：逐字保留，不得增删改（原头没有 "obtain a copy" 那句，也不要补）
LICENSE_BODY = [
    'Licensed under the Apache License, Version 2.0 (the "License");',
    "you may not use this file except in compliance with the License.",
    "",
    "Unless required by applicable law or agreed to in writing, software",
    'distributed under the License is distributed on an "AS IS" BASIS,',
    "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
    "See the License for the specific language governing permissions and",
    "limitations under the License.",
    "",
    "You should have received a copy of the Apache License",
    "along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.",
]

GROUPS = (
    "U_UNMODIFIED",      # 与上游逐字节一致：保留原头
    "U_MODIFIED",        # 上游文件/上游遗留，已修改：衍生头 A
    "OWN_OWN_PKG",       # 自研包名下的自研代码：自研头 B
    "OWN_UPSTREAM_PKG",  # 自研代码但放在 com.t8rin.* 下：自研头 C
    "CONFIG_OWN",        # 构建脚本/资源，自研
    "CONFIG_UPSTREAM",   # 构建脚本/资源，上游
)


def git(*args):
    return subprocess.check_output(["git", *args], text=True, errors="ignore")


def repo_root():
    return git("rev-parse", "--show-toplevel").strip()


def fetch_upstream():
    """拉取 fork 时点与当前上游的文件树，缓存 path/sha。只需 4 次 API 调用。"""
    os.makedirs(CACHE_DIR, exist_ok=True)
    cache = os.path.join(CACHE_DIR, "upstream_blobs.tsv")
    if os.path.exists(cache):
        rows = {}
        for line in open(cache, encoding="utf-8"):
            p, s = line.rstrip("\n").split("\t", 1)
            rows[p] = s
        return rows

    def api(url):
        req = urllib.request.Request(url, headers={"User-Agent": "onebox-header-audit"})
        with urllib.request.urlopen(req, timeout=60) as r:
            return json.load(r)

    base = "https://api.github.com/repos/" + UPSTREAM_REPO
    meta = api(base)
    branch = meta["default_branch"]
    current = api(base + "/git/trees/" + branch + "?recursive=1")
    commits = api(base + "/commits?until=" + FORK_SNAPSHOT_DATE + "&per_page=1")
    fork = api(base + "/git/trees/" + commits[0]["sha"] + "?recursive=1")
    for t in (current, fork):
        if t.get("truncated"):
            print("WARNING: upstream tree truncated", file=sys.stderr)

    rows = {}
    for t in (fork, current):
        for e in t.get("tree", []):
            if e["type"] == "blob":
                rows.setdefault(e["path"], e["sha"])
    with open(cache, "w", encoding="utf-8") as f:
        for p, s in sorted(rows.items()):
            f.write(p + "\t" + s + "\n")
    return rows


def path_key(p):
    """剥掉 source-set 前缀，只留包路径 + 文件名，规避 flavor 目录差异。"""
    m = re.search(r"(?:java|kotlin)/((?:com|ru|org)/.*)$", p)
    return m.group(1) if m else p


def blob_sha(data):
    return hashlib.sha1(b"blob %d\0" % len(data) + data).hexdigest()


def first_commits():
    """每个文件首次出现的 commit；初始快照之后新增的一定是自研（无 upstream merge）。"""
    out = git("log", "--reverse", "--diff-filter=A", "--format=@@@%H", "--name-only")
    first, cur = {}, None
    for line in out.splitlines():
        if line.startswith("@@@"):
            cur = line[3:]
        elif line.strip() and cur:
            first.setdefault(line.strip(), cur)
    initial = git("rev-list", "--max-parents=0", "HEAD").strip().splitlines()[0]
    return first, initial


def header_block(lines, style, decl=""):
    if style == "c":
        body = "/*\n" + "".join((" * " + l if l else " *") + "\n" for l in lines) + " */\n"
    elif style == "hash":
        body = "".join(("# " + l if l else "#") + "\n" for l in lines) + "#\n"
    else:
        body = "<!--\n" + "".join(("  ~ " + l if l else "  ~") + "\n" for l in lines) + "  -->\n"
    return (decl + "\n" if decl else "") + body + "\n"


def template(kind, upstream_year=None):
    first = PROJECT_NAME + " is " + PROJECT_DESC
    own = "Origin: " + PROJECT_NAME + " original work, not derived from ImageToolbox"
    if kind == "A":
        copyright_lines = []
        # 原文件本来就没有上游版权行时不臆造年份，只保留修改声明与自有版权
        if upstream_year:
            copyright_lines.append("Copyright (c) " + upstream_year + " " + UPSTREAM_AUTHOR)
        copyright_lines.append("Copyright (c) " + COPYRIGHT_YEAR + " " + COPYRIGHT_HOLDER)
        body = [
            first,
            "This file is part of " + PROJECT_NAME + ", a derivative work based on",
            "ImageToolbox (" + UPSTREAM_URL + ") by T8RIN.",
            "",
        ] + copyright_lines + [
            "",
            "This file has been modified from the original ImageToolbox source.",
            "",
            "Origin: ImageToolbox derivative, modified",
        ]
    elif kind == "C":
        body = [
            first,
            "Copyright (c) " + COPYRIGHT_YEAR + " " + COPYRIGHT_HOLDER,
            "",
            "This file was newly written for " + PROJECT_NAME + " and is not derived",
            "from ImageToolbox source. The com.t8rin.* package name is retained",
            "only for compatibility with the existing upstream module layout.",
            "",
            "Origin: OneBox original work (upstream package name retained)",
        ]
    else:
        body = [
            first,
            "Copyright (c) " + COPYRIGHT_YEAR + " " + COPYRIGHT_HOLDER,
            "",
            own,
        ]
    return body + [""] + LICENSE_BODY


def split_header(text, ext):
    """返回 (prelude, decl, style, upstream_year, body)；保留 @file: 注解和 XML 声明。"""
    head = text[:2500]
    ym = re.search(r"Copyright \(c\) (\d{4}) T8RIN", head)
    upstream_year = ym.group(1) if ym else None

    prelude = ""
    rest = text
    am = re.match(r"^((?:@file:[^\n]*\n)+)", rest)
    if am:
        prelude = am.group(1) + "\n"
        rest = rest[am.end():]

    decl, style = "", ("xml" if ext == ".xml" else
                       "hash" if ext in (".properties", ".gradle") else "c")
    xm = re.match(r"^(<\?xml[^\n]*\?>)\s*", rest)
    if xm:
        decl, style = xm.group(1), "xml"
        rest = rest[xm.end():]

    def is_license(block):
        # 只有确为许可声明的注释块才剥离；KDoc / 普通注释必须原样保留
        return ("Licensed under the Apache License" in block or
                "ImageToolbox is an image editor" in block)

    s = rest.lstrip("\n")
    if s.startswith("<!--"):
        end = s.find("-->")
        block = s[:end + 3] if end >= 0 else s
        if is_license(block):
            style, rest = "xml", (s[end + 3:] if end >= 0 else s)
        else:
            rest = s
    elif s.startswith("/*"):
        end = s.find("*/")
        block = s[:end + 2] if end >= 0 else s
        if is_license(block):
            style, rest = "c", (s[end + 2:] if end >= 0 else s)
        else:
            rest = s
    elif s.startswith("#"):
        lines = s.splitlines(True)
        i = 0
        while i < len(lines) and (lines[i].startswith("#") or not lines[i].strip()):
            i += 1
        if is_license("".join(lines[:i])):
            style, rest = "hash", "".join(lines[i:])
        else:
            rest = s
    else:
        rest = s
    body = rest.lstrip("\n")
    # 极少数文件把旧许可块放在 package/import 之后，摘掉它以免出现两份许可声明
    for mm in re.finditer(r"/\*[\s\S]{0,2000}?\*/", body[:3000]):
        if is_license(mm.group(0)):
            if not upstream_year:
                ym2 = re.search(r"Copyright \(c\) (\d{4}) T8RIN", mm.group(0))
                if ym2:
                    upstream_year = ym2.group(1)
            body = (body[:mm.start()] + body[mm.end():]).lstrip("\n")
            break
    return prelude, decl, style, upstream_year, body


def classify(root, upstream_rows):
    up_shas = set(upstream_rows.values())
    up_keys = {path_key(p) for p in upstream_rows}
    up_names = {os.path.basename(p) for p in upstream_rows}
    first, initial = first_commits()
    tracked = git("ls-files").splitlines()
    result = []
    for p in tracked:
        if not p.endswith(SOURCE_EXTS) or p.startswith(SKIP_PREFIXES):
            continue
        full = os.path.join(root, p)
        if not os.path.isfile(full):
            continue
        data = open(full, "rb").read()
        text = data.decode("utf-8", "ignore")
        head = text[:2500]
        pm = re.search(r"^package\s+([\w.]+)", head, re.M)
        pkg = pm.group(1) if pm else ""
        upkg = pkg.startswith(("com.t8rin", "ru.tech"))
        t8y = re.search(r"Copyright \(c\) (\d{4}) T8RIN", head)
        has_t8 = bool(t8y) or "ImageToolbox is an image editor" in head
        # 已带 Origin: 标记的文件在多批次推进时自动跳过，避免重复处理
        header_state = "origin" if "Origin:" in head else ("upstream" if has_t8 else "none")
        sha = blob_sha(data)
        after_initial = p in first and first[p] != initial
        name_hit = os.path.basename(p) in up_names

        if sha in up_shas:
            group = "U_UNMODIFIED"
        elif after_initial:
            group = "OWN_UPSTREAM_PKG" if upkg else ("OWN_OWN_PKG" if pkg else "CONFIG_OWN")
        elif path_key(p) in up_keys or (name_hit and has_t8):
            group = "U_MODIFIED"
        elif upkg:
            group = "OWN_UPSTREAM_PKG"
        elif pkg:
            group = "OWN_OWN_PKG"
        else:
            group = "CONFIG_UPSTREAM" if (name_hit or has_t8) else "CONFIG_OWN"

        # 带旧 T8RIN 头、上游又找不到同路径的，多为上游重构遗留，必须人工复核
        review = group == "OWN_UPSTREAM_PKG" and bool(t8y) and t8y.group(1) <= "2025"
        result.append({
            "path": p, "group": group, "package": pkg,
            "header": header_state,
            "upstream_year": t8y.group(1) if t8y else "",
            "after_initial": "yes" if after_initial else "no",
            "needs_review": "yes" if review else "no",
            "blob_sha": sha,
        })
    return result


KIND_FOR_GROUP = {
    "U_MODIFIED": "A",
    "OWN_OWN_PKG": "B",
    "OWN_UPSTREAM_PKG": "C",
    "CONFIG_OWN": "B",
    "CONFIG_UPSTREAM": "B",
}


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("command", choices=("classify", "apply"))
    ap.add_argument("--csv", default=os.path.join(CACHE_DIR, "classification.csv"))
    ap.add_argument("--groups", default=",".join(KIND_FOR_GROUP))
    ap.add_argument("--only-no-header", action="store_true",
                    help="只处理原本没有文件头的文件，带旧上游头的一律留到下一批复核")
    ap.add_argument("--write", action="store_true")
    ap.add_argument("--reapply", action="store_true",
                    help="连同已带 Origin: 标记的文件一起重刷（例如整体更换版权主体）")
    ap.add_argument("--force-review", action="store_true",
                    help="处理 needs_review 组：一律套 A 模板当作上游衍生，保留 T8RIN 署名")
    a = ap.parse_args()

    root = repo_root()
    os.chdir(root)
    upstream_rows = fetch_upstream()
    rows = classify(root, upstream_rows)

    if a.command == "classify":
        os.makedirs(os.path.dirname(a.csv) or ".", exist_ok=True)
        with open(a.csv, "w", newline="", encoding="utf-8") as f:
            w = csv.DictWriter(f, fieldnames=list(rows[0]))
            w.writeheader()
            w.writerows(rows)
        import collections
        c = collections.Counter((r["group"], r["needs_review"]) for r in rows)
        for k, v in sorted(c.items()):
            print("%5d  %-20s needs_review=%s" % (v, k[0], k[1]))
        print("total %d -> %s" % (len(rows), a.csv))
        return

    wanted = set(a.groups.split(","))
    changed = []
    for r in rows:
        kind = KIND_FOR_GROUP.get(r["group"]) if r["group"] in wanted else None
        if not kind or (r["needs_review"] == "yes" and not a.force_review):
            continue
        if a.only_no_header and r["header"] != "none":
            continue
        if r["header"] == "origin" and not a.reapply:
            continue
        # 待复核文件一律按上游衍生处理，绝不套原创模板抹掉 T8RIN 署名
        if r["needs_review"] == "yes":
            kind = "A"
        # 上游矢量图标等带旧版权行的配置资源，同样保留上游署名
        if r["group"] == "CONFIG_UPSTREAM" and r["header"] == "upstream":
            kind = "A"
        p = r["path"]
        text = open(p, encoding="utf-8").read()
        ext = os.path.splitext(p)[1]
        prelude, decl, style, old_year, body = split_header(text, ext)
        new = prelude + header_block(template(kind, old_year or r["upstream_year"]), style, decl) + body
        if new != text:
            changed.append((p, kind, style))
            if a.write:
                open(p, "w", encoding="utf-8", newline="").write(new)
    import collections
    c = collections.Counter(k for _, k, _ in changed)
    print(("WRITTEN " if a.write else "DRY-RUN ") + "%d files: %s" % (len(changed), dict(c)))
    for p, k, s in changed[:12]:
        print("   [%s/%s] %s" % (k, s, p))


if __name__ == "__main__":
    main()
