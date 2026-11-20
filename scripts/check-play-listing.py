#!/usr/bin/env python3
"""Check the Google Play listing files in fastlane/metadata/android.

Each locale folder (en-US, es-419, ...) must hold title.txt, short_description.txt
and full_description.txt. Play limits: title 30, short description 80, full
description 4000 characters. The en-US folder is required (the default language).
A file must not end in a newline or space: fastlane sends the bytes as they are.

Usage: python3 scripts/check-play-listing.py     (exit code 1 on any error)
"""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "fastlane" / "metadata" / "android"
LIMITS = {"title.txt": 30, "short_description.txt": 80, "full_description.txt": 4000}
LOCALE = re.compile(r"^[a-z]{2,3}(-[A-Za-z0-9]{2,4})?$")


def check_locale(folder):
    errors = []
    for name, limit in LIMITS.items():
        path = folder / name
        if not path.is_file():
            errors.append(f"{folder.name}/{name}: missing")
            continue
        text = path.read_text(encoding="utf-8")
        if not text.strip():
            errors.append(f"{folder.name}/{name}: empty")
        elif text != text.rstrip():
            errors.append(f"{folder.name}/{name}: ends in whitespace")
        elif len(text) > limit:
            errors.append(f"{folder.name}/{name}: {len(text)} characters, limit {limit}")
    return errors


def main():
    folders = sorted(p for p in ROOT.iterdir() if p.is_dir()) if ROOT.is_dir() else []
    errors = [] if any(p.name == "en-US" for p in folders) else ["en-US: folder missing"]
    for folder in folders:
        if not LOCALE.match(folder.name):
            errors.append(f"{folder.name}: not a Play locale code")
        errors += check_locale(folder)
    for line in errors:
        print(line)
    print(f"{len(folders)} locale(s) checked, {len(errors)} error(s)")
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
