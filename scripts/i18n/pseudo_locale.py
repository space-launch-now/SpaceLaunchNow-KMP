#!/usr/bin/env python3
"""Write a test-only pseudo-locale from the English master strings.xml.

Spec 017, section 9.3. Every vowel gets an accent, the text grows by 40 percent,
and the result sits inside markers: "Next Launch" -> "[!! Next Launch~~~~~ !!]"
with accented vowels. A string with no markers on screen is still hard-coded;
a clipped marker shows overflow.

Placeholders (%1$s, %d, %%, {name}, backslash escapes such as \\n) and markup
tags pass through unchanged.

Usage:
  python3 scripts/i18n/pseudo_locale.py [--source PATH] [--qualifier en-rXA] [--out PATH]
"""
import argparse
import math
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_SOURCE = Path("composeApp/src/commonMain/composeResources/values/strings.xml")
DEFAULT_QUALIFIER = "en-rXA"
OPEN_MARK, CLOSE_MARK = "[!! ", " !!]"
GROWTH = 0.4
PAD_CHAR = "~"

ACCENTS = str.maketrans(
    "aeiouyAEIOUY",
    "åéîøüÿÅÉÎØÜŸ",
)

# Anything that must survive untouched: printf placeholders, brace names, backslash escapes.
PROTECTED = re.compile(
    r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]"
    r"|\{[^{}]*\}"
    r"|\\u[0-9a-fA-F]{4}"
    r"|\\."
)


def accent(text: str) -> str:
    """Accent every vowel outside a protected token."""
    out, pos = [], 0
    for m in PROTECTED.finditer(text):
        out.append(text[pos:m.start()].translate(ACCENTS))
        out.append(m.group(0))
        pos = m.end()
    out.append(text[pos:].translate(ACCENTS))
    return "".join(out)


def pseudo(text: str) -> str:
    """Accent, lengthen by 40 percent and wrap one string value."""
    if not text.strip():
        return text
    padding = PAD_CHAR * math.ceil(len(text) * GROWTH)
    return f"{OPEN_MARK}{accent(text)}{padding}{CLOSE_MARK}"


def _transform_item(el: ET.Element) -> None:
    """Pseudo-localise the text of an element; inner tags keep their place."""
    children = list(el)
    if not children:
        el.text = pseudo(el.text or "")
        return
    # Mixed content (markup): accent the text runs, wrap once around the whole value.
    el.text = OPEN_MARK + accent(el.text or "")
    for child in children:
        child.tail = accent(child.tail or "")
    children[-1].tail = (children[-1].tail or "") + CLOSE_MARK


def build_pseudo_tree(tree: ET.ElementTree) -> ET.ElementTree:
    root = tree.getroot()
    for el in list(root):
        if not isinstance(el.tag, str) or el.get("translatable") == "false":
            root.remove(el)
        elif el.tag == "string":
            _transform_item(el)
        elif el.tag in ("plurals", "string-array"):
            for item in el.findall("item"):
                _transform_item(item)
        else:
            root.remove(el)
    ET.indent(tree, space="    ")
    return tree


def generate(source_path: Path, out_path: Path) -> int:
    tree = build_pseudo_tree(ET.parse(source_path))
    out_path.parent.mkdir(parents=True, exist_ok=True)
    tree.write(out_path, encoding="utf-8", xml_declaration=True)
    with out_path.open("a", encoding="utf-8") as fh:
        fh.write("\n")
    return len(tree.getroot())


def default_out(source_path: Path, qualifier: str) -> Path:
    return source_path.parent.parent / f"values-{qualifier}" / source_path.name


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("--source", type=Path, default=DEFAULT_SOURCE)
    ap.add_argument("--qualifier", default=DEFAULT_QUALIFIER,
                    help="resource qualifier of the output folder (values-<qualifier>)")
    ap.add_argument("--out", type=Path, help="output file (default: next to the source)")
    args = ap.parse_args(argv)
    out = args.out or default_out(args.source, args.qualifier)
    count = generate(args.source, out)
    print(f"wrote {count} resources to {out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
