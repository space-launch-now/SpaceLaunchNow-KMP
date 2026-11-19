#!/usr/bin/env python3
"""Fail when a tracked text file holds text that was saved through the wrong encoding.

A tool that reads a UTF-8 file as Windows-1252 and writes it back turns each
non-ASCII character into a run of wrong characters (a middle dot becomes
A-circumflex + middle dot). Such text compiles, so only this check stops it.

Usage: python3 scripts/check-encoding.py     (exit code 1 when a file fails)

Keep this file ASCII only (it gives characters as code points), or it fails
its own check.
"""
import re
import subprocess
import sys


def chars(*code_points):
    return "".join(map(chr, code_points))


# The characters Windows-1252 gives for the bytes 0x80-0xBF. A UTF-8 lead byte
# read as Windows-1252 always comes directly before one or more of these.
# The lead bytes below cover Latin-1 accents and symbols, punctuation, arrows,
# CJK punctuation and emoji. Other scripts (Greek, Cyrillic, Vietnamese) are
# left out, because their lead characters also occur in correct text.
TAIL = "[" + chr(0x80) + "-" + chr(0xBF) + chars(
    0x20AC, 0x201A, 0x0192, 0x201E, 0x2026, 0x2020, 0x2021, 0x02C6, 0x2030,
    0x0160, 0x2039, 0x0152, 0x017D, 0x2018, 0x2019, 0x201C, 0x201D, 0x2022,
    0x2013, 0x2014, 0x02DC, 0x2122, 0x0161, 0x203A, 0x0153, 0x017E, 0x0178,
) + "]"
DAMAGE = re.compile(
    "[" + chars(0xC2, 0xC3) + "]" + TAIL                    # 2-byte: accents, middle dot
    + "|[" + chars(0xE2, 0xE3, 0xEF) + "]" + TAIL + "{2}"   # 3-byte: dashes, quotes, arrows, byte order mark
    + "|" + chr(0xF0) + TAIL + "{3}"                        # 4-byte: emoji
)


def main():
    listing = subprocess.run(
        ["git", "ls-files", "-z"], capture_output=True, check=True
    ).stdout.decode("utf-8")
    failures = []
    for path in filter(None, listing.split("\0")):
        try:
            with open(path, "rb") as handle:
                data = handle.read()
        except FileNotFoundError:
            continue  # deleted in the working tree, not yet staged
        if b"\0" in data[:8000]:
            continue  # binary, by the same rule git uses
        try:
            text = data.decode("utf-8")
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
