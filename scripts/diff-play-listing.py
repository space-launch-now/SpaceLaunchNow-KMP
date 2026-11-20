#!/usr/bin/env python3
"""Compare the live Google Play listing text with fastlane/metadata/android.

Read only: it changes no file. The live listing is a folder that
`fastlane supply init` downloaded (see the `play_listing_diff` lane).

Usage: python3 scripts/diff-play-listing.py <downloaded-metadata-dir>
Exit code 0 = no diff, 1 = diff, 2 = bad input.
"""
import difflib
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent / "fastlane" / "metadata" / "android"
FILES = ("title.txt", "short_description.txt", "full_description.txt")


def read(path):
    """Text with CRLF folded and a trailing newline dropped (supply may add one)."""
    if not path.is_file():
        return None
    return path.read_text(encoding="utf-8").replace("\r\n", "\n").rstrip("\n")


def diff_locale(locale, live_root):
    lines = []
    for name in FILES:
        git, live = read(REPO / locale / name), read(live_root / locale / name)
        if git == live:
            continue
        lines.append(f"--- {locale}/{name}: differs")
        lines.extend(difflib.unified_diff(
            (live or "").splitlines(), (git or "").splitlines(),
            fromfile="play (live)", tofile="git", lineterm=""))
    return lines


def locales(root):
    return {p.name for p in root.iterdir() if p.is_dir()} if root.is_dir() else set()


def main():
    if len(sys.argv) != 2 or not Path(sys.argv[1]).is_dir():
        print(__doc__)
        return 2
    live_root = Path(sys.argv[1])
    in_git, on_play = locales(REPO), locales(live_root)
    out = []
    # Compare the locales git holds. A locale only on Play is reported, not diffed:
    # an upload leaves it alone.
    for locale in sorted(in_git):
        out.extend(diff_locale(locale, live_root))
    only_play = sorted(on_play - in_git)
    if only_play:
        out.append("Locales on Play that git does not hold: " + ", ".join(only_play))
    if not out:
        print("no diff: the live Play listing matches git for " + ", ".join(sorted(in_git)))
        return 0
    print("\n".join(out))
    return 1


if __name__ == "__main__":
    sys.exit(main())
