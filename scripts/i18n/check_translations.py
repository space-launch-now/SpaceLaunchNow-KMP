#!/usr/bin/env python3
"""Check the translated strings.xml files against the English masters.

Reports coverage per language and fails (exit 1) on a placeholder mismatch,
because a wrong placeholder crashes at runtime (spec 017, section 7.3).
A language under 95 percent coverage, a missing plural category and a text
over the length limit in the key comment are warnings; they never fail the
build. Pass --strict-plurals to make a missing plural category fail too.

Usage: python3 scripts/i18n/check_translations.py [--root DIR] [--strict-plurals]

translate.py imports the parsing and the checks from this file.
"""
import argparse
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

# Target languages (spec 017, section 8.1): Crowdin code -> Android folder suffix
# and the CLDR plural categories Android and Compose resources use.
LANGUAGES = {
    "de": {"folder": "de", "name": "German", "plurals": ("one", "other")},
    "es-ES": {"folder": "es", "name": "Spanish", "plurals": ("one", "many", "other")},
    "fr": {"folder": "fr", "name": "French", "plurals": ("one", "many", "other")},
    "zh-CN": {"folder": "zh-rCN", "name": "Chinese (Simplified)", "plurals": ("other",)},
    "nl": {"folder": "nl", "name": "Dutch", "plurals": ("one", "other")},
    "uk": {"folder": "uk", "name": "Ukrainian", "plurals": ("one", "few", "many", "other")},
    "it": {"folder": "it", "name": "Italian", "plurals": ("one", "many", "other")},
    "pl": {"folder": "pl", "name": "Polish", "plurals": ("one", "few", "many", "other")},
    "pt-BR": {"folder": "pt-rBR", "name": "Portuguese (Brazil)", "plurals": ("one", "many", "other")},
    "cs": {"folder": "cs", "name": "Czech", "plurals": ("one", "few", "many", "other")},
}

# English masters in Android XML. The String Catalog masters are not covered yet.
MASTERS = (
    "composeApp/src/commonMain/composeResources/values/strings.xml",
    "composeApp/src/androidMain/res/values/strings.xml",
    "wearApp/src/main/res/values/strings.xml",
)

# Never translated, whatever the file says.
NEVER_TRANSLATE = frozenset({"app_name", "app_name_debug"})

COVERAGE_TARGET = 0.95

# %1$s, %d, %.2f, %% and the backslash escapes that change layout. A quote escape only quotes.
_FORMAT = r"%(?:\d+\$)?[-#+0,(]*\d*(?:\.\d+)?[a-zA-Z%]"
_ESCAPE = r"\\(?:n|t|\\|u[0-9a-fA-F]{4}|@|\?)"
_TOKEN = re.compile(f"{_FORMAT}|{_ESCAPE}")
_LIMIT = re.compile(r"max\s+(\d+)\s+chars", re.IGNORECASE)


@dataclass
class Entry:
    name: str
    kind: str  # "string" or "plurals"
    comment: str = ""
    text: str = ""  # kind == "string"
    items: dict = field(default_factory=dict)  # kind == "plurals": quantity -> text
    translatable: bool = True

    @property
    def reference(self):
        """The text whose placeholders a translation must keep."""
        return self.text if self.kind == "string" else self.items.get("other", "")


def parse_resources(path):
    """Return {name: Entry} for a strings.xml, in file order. A missing file gives {}."""
    path = Path(path)
    if not path.exists():
        return {}
    parser = ET.XMLParser(target=ET.TreeBuilder(insert_comments=True))
    root = ET.parse(path, parser).getroot()
    entries, comment = {}, ""
    for node in root:
        if node.tag is ET.Comment:
            comment = " ".join((node.text or "").split())
            continue
        if node.tag in ("string", "plurals"):
            entry = Entry(
                name=node.get("name"),
                kind=node.tag,
                comment=comment,
                translatable=node.get("translatable", "true") != "false"
                and node.get("name") not in NEVER_TRANSLATE,
            )
            if node.tag == "string":
                entry.text = "".join(node.itertext())
            else:
                entry.items = {i.get("quantity"): "".join(i.itertext()) for i in node.findall("item")}
            entries[entry.name] = entry
        comment = ""
    return entries


def placeholders(text):
    """Sorted placeholder and escape tokens of a text."""
    return sorted(_TOKEN.findall(text))


def length_limit(comment):
    """The `max N chars` limit named in a key comment, or None."""
    match = _LIMIT.search(comment or "")
    return int(match.group(1)) if match else None


def _sub_multiset(small, big):
    pool = list(big)
    for token in small:
        if token not in pool:
            return False
        pool.remove(token)
    return True


def problems(source, target, language):
    """Return (placeholder_errors, warnings) for one translated entry."""
    errors, warnings = [], []
    if target.kind != source.kind:
        return [f"{source.name}: translated as {target.kind}, English is {source.kind}"], warnings
    expected = placeholders(source.reference)
    if source.kind == "string":
        texts = {"": target.text}
        if placeholders(target.text) != expected:
            errors.append(
                f"{source.name}: placeholders {placeholders(target.text)} do not match English {expected}"
            )
    else:
        texts = target.items
        for quantity, text in target.items.items():
            if not _sub_multiset(placeholders(text), expected):
                errors.append(
                    f"{source.name}[{quantity}]: placeholders {placeholders(text)} not in English {expected}"
                )
        wanted = LANGUAGES[language]["plurals"]
        missing = [q for q in wanted if q not in target.items]
        if missing:
            warnings.append(f"{source.name}: missing plural categories {missing}")
        extra = [q for q in target.items if q not in wanted]
        if extra:
            warnings.append(f"{source.name}: plural categories {extra} not used by {language}")
    limit = length_limit(source.comment)
    if limit:
        for quantity, text in texts.items():
            if len(text) > limit:
                label = f"[{quantity}]" if quantity else ""
                warnings.append(f"{source.name}{label}: {len(text)} chars, limit {limit}")
    return errors, warnings


def target_path(master, language):
    """values/strings.xml -> values-<folder>/strings.xml."""
    master = Path(master)
    return master.parent.parent / f"values-{LANGUAGES[language]['folder']}" / master.name


def check(root):
    """Check every language of every master. Returns (rows, errors, warnings)."""
    rows, errors, warnings = [], [], []
    for master in MASTERS:
        source = {n: e for n, e in parse_resources(Path(root) / master).items() if e.translatable}
        for language in LANGUAGES:
            path = target_path(Path(root) / master, language)
            translated = parse_resources(path)
            done = 0
            for name, entry in source.items():
                if name in translated:
                    done += 1
                    errs, warns = problems(entry, translated[name], language)
                    errors += [f"{path}: {e}" for e in errs]
                    warnings += [f"{path}: {w}" for w in warns]
            rows.append((master, language, done, len(source)))
    return rows, errors, warnings


def coverage_report(rows):
    lines = []
    for master in MASTERS:
        part = [r for r in rows if r[0] == master]
        if not part or not part[0][3]:
            continue
        lines.append(f"{master} ({part[0][3]} keys)")
        for _, language, done, total in part:
            share = done / total
            flag = "" if share >= COVERAGE_TARGET else "  below 95%"
            lines.append(f"  {language:6} {done:4}/{total:<4} {share:6.1%}{flag}")
    return "\n".join(lines)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("--root", default=Path(__file__).resolve().parents[2], type=Path)
    parser.add_argument("--strict-plurals", action="store_true")
    args = parser.parse_args(argv)
    rows, errors, warnings = check(args.root)
    print(coverage_report(rows) or "No English masters found.")
    for message in warnings:
        print(f"warning: {message}")
    for message in errors:
        print(f"error: {message}")
    failed = bool(errors) or (args.strict_plurals and any("plural" in w for w in warnings))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
