#!/usr/bin/env python3
"""Fail when a tracked text file holds text that was saved through the wrong encoding.

A tool that reads a UTF-8 file as Windows-1252 and writes it back turns each
non-ASCII character into a run of wrong characters (a middle dot becomes
A-circumflex + middle dot). Such text compiles, so only this check stops it.

Usage: python3 scripts/check-encoding.py     (exit code 1 when a file fails)

This file is ASCII only, so it cannot fail its own check.
"""
import re
import subprocess
import sys

TEXT_SUFFIXES = (
    ".kt", ".kts", ".swift", ".xml", ".json", ".md", ".yml", ".yaml", ".sq", ".sqm",
    ".properties", ".toml", ".plist", ".entitlements", ".sh", ".py", ".txt", ".pro",
)

# The characters Windows-1252 gives for the bytes 0x80-0xBF. A UTF-8 lead byte
# read as Windows-1252 always comes directly before one of these.
TAIL = (
    "\u0080-¿"
    "€‚ƒ„…†‡ˆ‰Š‹ŒŽ"
    "‘’“”•–—˜™š›œžŸ"
)
DAMAGE = re.compile(
    "[ÂÃ][" + TAIL + "]"   # 2-byte character read as Windows-1252
    "|â€"                  # dashes, quotes and bullets (E2 80 xx)
    "|ï»¿"            # byte order mark
)


def main():
    listing = subprocess.run(
        ["git", "ls-files", "-z"], capture_output=True, check=True
    ).stdout.decode("utf-8")
    failures = []
    for path in filter(None, listing.split("\0")):
        if not path.endswith(TEXT_SUFFIXES):
            continue
        try:
            with open(path, "rb") as handle:
                text = handle.read().decode("utf-8")
        except FileNotFoundError:
            continue  # deleted in the working tree, not yet staged
        except UnicodeDecodeError as error:
            failures.append(f"{path}: not valid UTF-8 ({error.reason} at byte {error.start})")
            continue
        for number, line in enumerate(text.split("\n"), start=1):
            if DAMAGE.search(line):
                failures.append(f"{path}:{number}: {line.strip()[:100]}")

    if failures:
        out = sys.stdout.buffer
        out.write(b"Wrong-encoding text found. Restore the correct characters and save as UTF-8:\n")
        for failure in failures:
            out.write(("  " + failure + "\n").encode("utf-8"))
        return 1
    print("Encoding check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
