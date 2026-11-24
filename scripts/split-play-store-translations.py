#!/usr/bin/env python3
"""Write one Play "What's new" file per listing language.

The release workflow translates the English notes in one model call. The model
answers with one block per language:

    === de-DE ===
    <notes>

This script splits that answer into whatsnew-<locale> files. The languages come
from the listing folders in fastlane/metadata/android (the same folders Play
gets), so a new listing language needs no edit here. A language with a missing,
empty or broken block gets the English notes. Play limits notes to 500
characters per language; this counts characters, not bytes.

Usage: split-play-store-translations.py <whatsnew-dir> [<translations-file>]
Reads the model answer from the file, or from stdin when no file is given.
"""
import re
import sys
from pathlib import Path

LIMIT = 500  # Play limit; the workflow prompt asks the model for 480 to leave room
DEFAULT_LOCALE = "en-US"
LISTING = Path(__file__).resolve().parent.parent / "fastlane" / "metadata" / "android"
HEADER = re.compile(r"^=== *([A-Za-z-]+) *===\s*$", re.MULTILINE)


def listing_locales():
    return sorted(p.name for p in LISTING.iterdir() if p.is_dir() and p.name != DEFAULT_LOCALE)


def fit(text):
    """Trim to the Play limit on whole lines; hard-cut a single line that is too long."""
    text = text.strip()
    if len(text) <= LIMIT:
        return text
    kept = ""
    for line in text.splitlines():
        candidate = f"{kept}\n{line}" if kept else line
        if len(candidate) > LIMIT:
            break
        kept = candidate
    return kept or text[:LIMIT].rstrip()


def parse(answer):
    parts = HEADER.split(answer)
    return {parts[i]: parts[i + 1] for i in range(1, len(parts) - 1, 2)}


def main(argv):
    if not argv:
        print(__doc__)
        return 2
    out = Path(argv[0])
    if len(argv) > 1:
        answer = Path(argv[1]).read_text(encoding="utf-8")
    else:
        sys.stdin.reconfigure(encoding="utf-8")
        answer = sys.stdin.read()
    answer = answer.replace("\r\n", "\n")
    english = (out / f"whatsnew-{DEFAULT_LOCALE}").read_text(encoding="utf-8").strip()
    blocks = parse(answer)
    translated = 0
    for locale in listing_locales():
        text = fit(blocks.get(locale, ""))
        if text:
            translated += 1
        (out / f"whatsnew-{locale}").write_text(text or english, encoding="utf-8", newline="")
    print(f"{translated} translated, {len(listing_locales()) - translated} fell back to English")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
