#!/usr/bin/env python3
"""Check the translated string files against the English masters.

Covers every Compose resource file (values/*.xml), the Android res and Wear
strings.xml, and the two iOS String Catalogs. Reports coverage per file and
language and fails (exit 1) on a placeholder mismatch, because a wrong
placeholder crashes at runtime (spec 017, section 7.3).
A language under 95 percent coverage, a catalog key with no value, a missing
plural category and a text over the length limit in the key comment are
warnings; they never fail the build. A language with no file yet is "not
started", also a warning. Pass --strict-plurals to make a missing plural
category fail too.

Usage: python3 scripts/i18n/check_translations.py [--root DIR] [--strict-plurals]

translate.py imports the parsing and the checks from this file.
"""
import argparse
import json
import re
import sys
import xml.etree.ElementTree as ET
from dataclasses import dataclass, field
from pathlib import Path

# Target languages (spec 017, section 8.1): Crowdin code -> Android folder suffix, the code a
# String Catalog uses, and the CLDR plural categories Android and Compose resources use.
LANGUAGES = {
    "de": {"folder": "de", "catalog": "de", "name": "German", "plurals": ("one", "other")},
    "es-ES": {"folder": "es", "catalog": "es", "name": "Spanish", "plurals": ("one", "many", "other")},
    "fr": {"folder": "fr", "catalog": "fr", "name": "French", "plurals": ("one", "many", "other")},
    "zh-CN": {"folder": "zh-rCN", "catalog": "zh-Hans", "name": "Chinese (Simplified)", "plurals": ("other",)},
    "nl": {"folder": "nl", "catalog": "nl", "name": "Dutch", "plurals": ("one", "other")},
    "uk": {"folder": "uk", "catalog": "uk", "name": "Ukrainian", "plurals": ("one", "few", "many", "other")},
    "it": {"folder": "it", "catalog": "it", "name": "Italian", "plurals": ("one", "many", "other")},
    "pl": {"folder": "pl", "catalog": "pl", "name": "Polish", "plurals": ("one", "few", "many", "other")},
    "pt-BR": {"folder": "pt-rBR", "catalog": "pt-BR", "name": "Portuguese (Brazil)", "plurals": ("one", "many", "other")},
    "cs": {"folder": "cs", "catalog": "cs", "name": "Czech", "plurals": ("one", "few", "many", "other")},
}

# English masters in Android XML. The Compose masters are every file the glob finds, so a new
# values/strings_<feature>.xml is covered without an edit here. The Android res and Wear masters
# are fixed. Each master is compared with the file of the same name in values-<lang>.
COMPOSE_MASTERS = "composeApp/src/commonMain/composeResources/values/*.xml"
FIXED_MASTERS = (
    "composeApp/src/androidMain/res/values/strings.xml",
    "wearApp/src/main/res/values/strings.xml",
)
# iOS String Catalogs: English is in the file itself. Read only.
CATALOGS = (
    "iosApp/iosApp/InfoPlist.xcstrings",
    "iosApp/LaunchWidget/Localizable.xcstrings",
)

# Never translated, whatever the file says.
NEVER_TRANSLATE = frozenset({"app_name", "app_name_debug"})

COVERAGE_TARGET = 0.95
# Every plural warning holds this text; --strict-plurals tests for it.
PLURAL_WARNING = "plural categories"

# %1$s, %d, %.2f, %% and the backslash escapes that change layout. A quote escape only quotes.
_FORMAT = r"%(?:\d+\$)?[-#+0,(]*\d*(?:\.\d+)?[a-zA-Z%]"
_ESCAPE = r"\\(?:n|t|\\|u[0-9a-fA-F]{4}|@|\?)"
_TOKEN = re.compile(f"{_FORMAT}|{_ESCAPE}")
# Catalog text: %@, %1$@, %lld, %d, %%. The position number is dropped when comparing, because
# a translation may add one to reorder.
_CATALOG_TOKEN = re.compile(r"%(?:\d+\$)?[-#+0,(]*\d*(?:\.\d+)?(?:hh|h|ll|l|q|z|t|j)?[a-zA-Z%@]")
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
            warnings.append(f"{source.name}: missing {PLURAL_WARNING} {missing}")
        extra = [q for q in target.items if q not in wanted]
        if extra:
            warnings.append(f"{source.name}: {PLURAL_WARNING} {extra} not used by {language}")
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


def find_masters(root):
    """Relative paths of every Android XML master under root: the Compose glob, sorted, then the fixed ones."""
    root = Path(root)
    compose = sorted(p.relative_to(root).as_posix() for p in root.glob(COMPOSE_MASTERS))
    return (*compose, *FIXED_MASTERS)


def catalog_placeholders(text):
    """Sorted placeholder tokens of a catalog text, position numbers removed."""
    return sorted(re.sub(r"(?<=%)\d+\$", "", t) for t in _CATALOG_TOKEN.findall(text))


def parse_catalog(path):
    """Return {key: {language: [values]}} of a String Catalog. A missing file gives {}.

    A localization holds one value, or the values of its plural and device variations.
    """
    path = Path(path)
    if not path.exists():
        return {}

    def values(node):
        found = []
        if isinstance(node, dict):
            if "value" in node and "state" in node:
                found.append(node["value"])
            for child in node.values():
                found += values(child)
        return found

    data = json.loads(path.read_text(encoding="utf-8"))
    return {
        key: {lang: values(loc) for lang, loc in item.get("localizations", {}).items()}
        for key, item in data.get("strings", {}).items()
    }


def check_catalog(path, label):
    """Check one catalog. Returns (rows, errors, warnings); a row is (label, language, done, total)."""
    catalog = parse_catalog(path)
    source = {key: texts["en"] for key, texts in catalog.items() if texts.get("en")}
    rows, errors, warnings = [], [], []
    for language, info in LANGUAGES.items():
        code, done, empty = info["catalog"], 0, []
        for key, english in source.items():
            texts = catalog[key].get(code)
            if not texts or not all(t.strip() for t in texts):
                empty.append(key)
                continue
            done += 1
            expected = catalog_placeholders(english[0])
            for text in texts:
                found = catalog_placeholders(text)
                same = found == expected if len(texts) == 1 else _sub_multiset(found, expected)
                if not same:
                    errors.append(f"{label} [{code}] {key}: placeholders {found} do not match English {expected}")
        if empty and done:
            warnings.append(f"{label} [{code}]: no value for {len(empty)} keys, first {empty[0]}")
        rows.append((label, language, done, len(source)))
    return rows, errors, warnings


def check(root):
    """Check every language of every master and catalog. Returns (rows, errors, warnings)."""
    rows, errors, warnings = [], [], []
    for label in CATALOGS:
        part = check_catalog(Path(root) / label, label)
        rows += part[0]
        errors += part[1]
        warnings += part[2]
    for master in find_masters(root):
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
    for master in dict.fromkeys(r[0] for r in rows):
        part = [r for r in rows if r[0] == master]
        if not part[0][3]:
            continue
        lines.append(f"{master} ({part[0][3]} keys)")
        for _, language, done, total in part:
            share = done / total
            flag = "" if share >= COVERAGE_TARGET else "  below 95%"
            lines.append(f"  {language:6} {done:4}/{total:<4} {share:6.1%}{flag}")
    return "\n".join(lines)


def summary(rows, errors, warnings):
    """One line: how much was checked and how many problems."""
    files = {r[0] for r in rows if r[3]}
    catalogs = sum(f in CATALOGS for f in files)
    started = {r[1] for r in rows if r[2]}
    missing = sum(r[3] - r[2] for r in rows if r[1] in started)
    return (
        f"Checked {len(files) - catalogs} string files and {catalogs} catalogs; "
        f"{len(started)} of {len(LANGUAGES)} languages started; "
        f"{missing} missing keys in them; {len(errors)} placeholder errors, {len(warnings)} warnings."
    )


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("--root", default=Path(__file__).resolve().parents[2], type=Path)
    parser.add_argument("--strict-plurals", action="store_true")
    args = parser.parse_args(argv)
    rows, errors, warnings = check(args.root)
    print(coverage_report(rows) or "No English masters found.")
    print(summary(rows, errors, warnings))
    for message in warnings:
        print(f"warning: {message}")
    for message in errors:
        print(f"error: {message}")
    failed = bool(errors) or (args.strict_plurals and any(PLURAL_WARNING in w for w in warnings))
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
