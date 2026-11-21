#!/usr/bin/env python3
"""Check that moving a text literal to a string resource did not change the text.

Spec 017, section 6.2, layer 1. For each diff hunk, every added Res.string.<key>
must match a removed Kotlin string literal: the key's English value, after XML and
Android unescaping, equals the literal, with $var and ${expr} mapped to %1$s style
placeholders. A key with no equal literal fails the check.

Usage:
  python3 scripts/i18n/verify_move.py [--base origin/main] [--diff FILE|-]
                                      [--strings PATH ...] [--only-if-strings-changed]
Exit code: 0 pass, 1 mismatch or unknown key.
"""
import argparse
import re
import subprocess
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

VALUES_DIR = Path("composeApp/src/commonMain/composeResources/values")


def default_strings():
    """Every English strings*.xml (strings.xml, strings_time.xml, ...) in the values folder."""
    return sorted(VALUES_DIR.glob("strings*.xml"))

KEY_RE = re.compile(r"\bRes\.string\.([A-Za-z0-9_]+)")
# A string literal; `${...}` may hold quotes (`${a ?: ""}`) and stays inside the literal.
LITERAL_RE = re.compile(r'(?<!")"((?:[^"\\\n$]|\\.|\$\{[^{}\n]*\}|\$)*)"(?!")')
# Two literals joined by `+` (same line or across lines) form one literal.
CONCAT_RE = re.compile(r'"\s*\+\s*"')
KT_VAR_RE = re.compile(r"\$(?:\{([^{}]*)\}|([A-Za-z_][A-Za-z0-9_]*))")
PRINTF_PATTERN = r"%(?:(\d+)\$)?[-#+ 0,(]*\d*(?:\.\d+)?([a-zA-Z%])"  # shared with pseudo_locale
PRINTF_RE = re.compile(PRINTF_PATTERN)
SIMPLE_ESCAPES = {"n": "\n", "t": "\t", "b": "\b", "r": "\r"}
DOLLAR_MARK = "\x00"


def _unescape(text: str, dollar: str) -> str:
    """Resolve backslash escapes (\\n, \\uXXXX, \\', ...). `dollar` replaces a literal \\$."""
    out, i = [], 0
    while i < len(text):
        ch = text[i]
        if ch == "\\" and i + 1 < len(text):
            nxt = text[i + 1]
            if nxt == "u" and re.fullmatch(r"[0-9a-fA-F]{4}", text[i + 2:i + 6]):
                out.append(chr(int(text[i + 2:i + 6], 16)))
                i += 6
                continue
            out.append(dollar if nxt == "$" else SIMPLE_ESCAPES.get(nxt, nxt))
            i += 2
            continue
        out.append(ch)
        i += 1
    return "".join(out)


def normalize_literal(body: str) -> str:
    """Kotlin literal body -> comparable text, {1}, {2} placeholders in order of first use."""
    seen = {}

    def repl(m):
        expr = (m.group(1) or m.group(2)).strip()
        return "{%d}" % seen.setdefault(expr, len(seen) + 1)

    text = KT_VAR_RE.sub(repl, _unescape(body, DOLLAR_MARK))
    return text.replace(DOLLAR_MARK, "$")


def normalize_resource(value: str) -> str:
    """Resource value -> comparable text with {N} placeholders (%1$s, %2$d, %s, %d)."""
    if len(value) >= 2 and value[0] == '"' and value[-1] == '"':
        value = value[1:-1]  # Android quoting keeps whitespace
    auto = [0]

    def repl(m):
        if m.group(2) == "%":
            return "%"
        if m.group(1):
            return "{%s}" % m.group(1)
        auto[0] += 1
        return "{%d}" % auto[0]

    return PRINTF_RE.sub(repl, _unescape(value, "$"))


def load_strings(paths):
    """Return ({key: raw value} for <string>, {names of plurals and arrays})."""
    strings, other = {}, set()
    for path in paths:
        for el in ET.parse(path).getroot():
            if not isinstance(el.tag, str) or "name" not in el.attrib:
                continue
            if el.tag == "string":
                strings[el.get("name")] = "".join(el.itertext())
            else:
                other.add(el.get("name"))
    return strings, other


def parse_hunks(diff: str):
    """Yield (file, removed_lines, added_lines) for each hunk of a unified diff."""
    path, removed, added, in_hunk = None, [], [], False

    def flush():
        return in_hunk and (removed or added)

    for line in diff.splitlines():
        if line.startswith("diff --git") or line.startswith("@@"):
            if flush():
                yield path, removed, added
            removed, added = [], []
            in_hunk = line.startswith("@@")
        elif not in_hunk:
            if line.startswith("+++ "):
                path = line[4:].removeprefix("b/")
        elif line.startswith("-"):
            removed.append(line[1:])
        elif line.startswith("+"):
            added.append(line[1:])
    if flush():
        yield path, removed, added


def removed_literals(removed):
    """Normalized Kotlin literals in removed lines; `"a" +` newline `"b"` counts as one."""
    text = CONCAT_RE.sub("", "\n".join(removed))
    return [normalize_literal(m.group(1)) for m in LITERAL_RE.finditer(text)]


def check_hunk(pool, added, strings, other):
    """Return a list of problem strings for one hunk; `pool` (the file's removed literals) is consumed."""
    problems = []
    for line in added:
        for key in KEY_RE.findall(line):
            if key in other:
                continue  # plurals and arrays: the plural test covers them
            if key not in strings:
                problems.append(f"key '{key}' is not in any strings.xml")
                continue
            want = normalize_resource(strings[key])
            if want in pool:
                pool.remove(want)
            else:
                problems.append(f"key '{key}' = {want!r} has no equal removed literal"
                                f" (removed: {pool!r})")
    return problems


def verify(diff: str, strings, other):
    """Match added keys to removed literals per file, so a literal hoisted into a
    `val` in another hunk of the same file still counts."""
    by_file = {}
    for path, removed, added in parse_hunks(diff):
        if (path or "").endswith(".kt"):
            by_file.setdefault(path, []).append((removed, added))
    failures = []
    for path, hunks in by_file.items():
        pool = removed_literals([line for removed, _ in hunks for line in removed])
        for _, added in hunks:
            failures += [f"{path}: {p}" for p in check_hunk(pool, added, strings, other)]
    return failures


def git_diff(base: str) -> str:
    cmd = ["git", "diff", "--no-color", "-U0", f"{base}...HEAD"]
    return subprocess.run(cmd, check=True, capture_output=True, text=True, encoding="utf-8").stdout


def touches_strings(diff: str) -> bool:
    return bool(re.search(r"^diff --git .*strings[^/\s]*\.xml\b", diff, re.M))


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("--base", default="origin/main", help="git base ref for the diff")
    ap.add_argument("--diff", help="read a unified diff from FILE or - instead of git")
    ap.add_argument("--strings", type=Path, action="append", help="English strings.xml (repeatable)")
    ap.add_argument("--only-if-strings-changed", action="store_true",
                    help="exit 0 at once when the diff touches no strings*.xml")
    args = ap.parse_args(argv)

    if args.diff:
        diff = sys.stdin.read() if args.diff == "-" else Path(args.diff).read_text(encoding="utf-8")
    else:
        diff = git_diff(args.base)
    if args.only_if_strings_changed and not touches_strings(diff):
        print("verify_move: no strings*.xml in the diff, skipped")
        return 0

    strings, other = load_strings(args.strings or default_strings())
    failures = verify(diff, strings, other)
    for f in failures:
        print(f"FAIL {f}")
    print(f"verify_move: {'FAIL' if failures else 'ok'} ({len(failures)} problem(s))")
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
