#!/usr/bin/env python3
"""Stop new hard-coded English UI text in Kotlin sources (spec 017, section 6.3).

Finds the string literals of Appendix A (a literal with a letter inside Text(...) or
in a named argument such as title =, label =, contentDescription =) in composeApp,
wearApp and androidApp Kotlin sources. Debug files and test folders are skipped. Each finding
is compared with scripts/i18n/hardcoded-baseline.txt, one line per finding:
"<file><TAB><literal>". The comparison ignores line numbers; a literal that appears
twice in a file needs two baseline lines. A finding with no baseline line fails the build.

A line comment "// i18n-ignore: <reason>" on the same line or the line above
exempts one literal (for example a log tag).

Also checks every *.xcstrings String Catalog: no entry in state "new" or "stale".

Usage:
  python3 scripts/i18n/check_hardcoded_text.py [--root DIR] [--baseline FILE]
  python3 scripts/i18n/check_hardcoded_text.py --update   # rewrite the baseline

Move tasks delete lines from the baseline (run --update after a move, or after
files move on disk). The baseline must never grow by hand to hide new text.
"""
import argparse
import collections
import json
import os
import re
import sys
from pathlib import Path

LIT = r'"((?:[^"\\n]|\.)*[A-Za-z][^"\n]*?)"'
KEYS = (r"(?:text|title|subtitle|headline|label|placeholder|supportingText|message|description|"
        r"summary|hint|buttonText|actionLabel|confirmText|dismissText|contentDescription)")
PATTERNS = [
    re.compile(r"\bText\(\s*(?:text\s*=\s*)?" + LIT),
    re.compile(r"\b" + KEYS + r"\s*=\s*" + LIT),
]
SKIP_DIRS = ("/build", "/openApi", "/.git", "Test/", "/test/")
SOURCE_ROOT = re.compile(r"(composeApp|wearApp|androidApp)/src/[^/]+/")
IGNORE = re.compile(r"//\s*i18n-ignore:\s*\S")
DEFAULT_BASELINE = Path(__file__).resolve().parent / "hardcoded-baseline.txt"
HEADER = (
    "# Hard-coded UI text that exists today (spec 017, section 6.3). One line per literal:\n"
    "# <file><TAB><literal>. Delete a line when a move task puts the text in strings.xml.\n"
    "# Regenerate: python3 scripts/i18n/check_hardcoded_text.py --update\n"
)


def kotlin_files(root: Path):
    """Yield (relative posix path, absolute path) for every scanned .kt file."""
    for d, dirs, files in os.walk(root):
        dirs.sort()
        rel_dir = "/" + os.path.relpath(d, root).replace(os.sep, "/") + "/"
        if any(s in rel_dir for s in SKIP_DIRS):
            continue
        for f in sorted(files):
            if not f.endswith(".kt"):
                continue
            rel = os.path.relpath(os.path.join(d, f), root).replace(os.sep, "/")
            if "ebug" in rel or not SOURCE_ROOT.match(rel):
                continue
            yield rel, os.path.join(d, f)


def scan_text(text: str):
    """Return [(line number, literal)] for the literals that are not exempt."""
    lines = text.splitlines()
    found, seen = [], set()
    for rx in PATTERNS:
        for m in rx.finditer(text):
            start = m.start(1)
            if start in seen:
                continue
            seen.add(start)
            literal = m.group(1)
            if re.fullmatch(r"[a-z_]+", literal) and not m.group(0).startswith("Text"):
                continue  # identifiers, not UI text
            lineno = text.count("\n", 0, start) + 1
            if IGNORE.search(lines[lineno - 1]) or (lineno > 1 and IGNORE.search(lines[lineno - 2])):
                continue
            found.append((lineno, literal))
    return sorted(found)


def scan(root: Path):
    """Return [(file, line, literal)] over the whole tree."""
    out = []
    for rel, path in kotlin_files(root):
        with open(path, encoding="utf-8", errors="ignore") as fh:
            out.extend((rel, n, lit) for n, lit in scan_text(fh.read()))
    return out


def entry(rel: str, literal: str) -> str:
    return f"{rel}\t{literal}"


def load_baseline(path: Path) -> collections.Counter:
    entries = collections.Counter()
    if path.exists():
        for line in path.read_text(encoding="utf-8").splitlines():
            if line.strip() and not line.startswith("#"):
                entries[line] += 1
    return entries


def new_findings(findings, baseline: collections.Counter):
    """Findings beyond the baseline count for their (file, literal) pair."""
    left = collections.Counter(baseline)
    fresh = []
    for rel, lineno, literal in findings:
        key = entry(rel, literal)
        if left[key] > 0:
            left[key] -= 1
        else:
            fresh.append((rel, lineno, literal))
    stale = sum(left.values())
    return fresh, stale


def write_baseline(path: Path, findings) -> None:
    lines = sorted(entry(rel, lit) for rel, _, lit in findings)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(HEADER + "\n".join(lines) + ("\n" if lines else ""))


def catalog_problems(root: Path):
    """String Catalog entries in state new or stale (xcstrings JSON)."""
    problems = []
    for d, dirs, files in os.walk(root):
        dirs[:] = [x for x in dirs if x not in (".git", "build", "Pods", "node_modules")]
        for f in files:
            if not f.endswith(".xcstrings"):
                continue
            path = Path(d) / f
            rel = path.relative_to(root).as_posix()
            data = json.loads(path.read_text(encoding="utf-8"))
            for key, item in data.get("strings", {}).items():
                if item.get("extractionState") == "stale":
                    problems.append(f"{rel}: '{key}' is stale")
                for lang, loc in item.get("localizations", {}).items():
                    if loc.get("stringUnit", {}).get("state") == "new":
                        problems.append(f"{rel}: '{key}' ({lang}) is in state new")
    return problems


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    ap.add_argument("--baseline", type=Path, default=DEFAULT_BASELINE)
    ap.add_argument("--update", action="store_true", help="rewrite the baseline from the current sources")
    args = ap.parse_args(argv)

    findings = scan(args.root)
    if args.update:
        write_baseline(args.baseline, findings)
        print(f"Wrote {len(findings)} literals to {args.baseline}")
        return 0

    fresh, stale = new_findings(findings, load_baseline(args.baseline))
    failed = False
    if fresh:
        failed = True
        print(f"{len(fresh)} new hard-coded text literal(s). Move each to strings.xml "
              "(stringResource), or add '// i18n-ignore: <reason>' on the line:")
        for rel, lineno, literal in fresh:
            print(f"  {rel}:{lineno}: \"{literal}\"")
    if stale:
        print(f"Note: {stale} baseline line(s) match no literal; run with --update to drop them.")
    problems = catalog_problems(args.root)
    if problems:
        failed = True
        print("String Catalog entries to fix (open the catalog in Xcode):")
        for p in problems:
            print(f"  {p}")
    if not failed:
        print(f"OK: {len(findings)} literals, all in the baseline or exempt.")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
