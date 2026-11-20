#!/usr/bin/env python3
"""Fill the missing translations of the app text with a language model.

Reads the English masters (check_translations.MASTERS), the glossary and the key
comments. For each target language it finds the keys that have no translation yet,
asks the model for them, checks the answer and appends only the good ones to
values-<lang>/strings.xml. A key that exists in a target file is never touched, so
text a person approved or edited in Crowdin stays as it is. The new text is
unapproved: upload it with `crowdin upload translations --auto-approve-imported=false`
so people can review it (spec 017, section 7.4).

A text that fails the checks (placeholders, plural categories, length limit from the
key comment) is sent back once with the reasons. If it fails again it is not written
and the report lists it.

Keys named `shared_*` are translated once and written to every master that has them.

Usage: python3 scripts/i18n/translate.py [--lang de,fr] [--dry-run] [--batch-size 40]
Key: ANTHROPIC_API_KEY (environment only; never put it in a file). Model: I18N_MODEL.
"""
import argparse
import json
import os
import re
import sys
import urllib.request
from dataclasses import dataclass, field
from pathlib import Path
from xml.sax.saxutils import escape

sys.path.insert(0, str(Path(__file__).resolve().parent))
import check_translations as ct  # noqa: E402

GLOSSARY = "specs/017-app-text-translation/glossary.md"
DEFAULT_MODEL = "claude-sonnet-5-5"
API_URL = "https://api.anthropic.com/v1/messages"
SHARED_PREFIX = "shared_"
HEADER = '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n</resources>\n'


@dataclass
class Job:
    """One English entry that needs a translation into one language."""

    id: str
    entry: ct.Entry
    masters: list = field(default_factory=list)  # masters that get the result


@dataclass
class Report:
    written: dict = field(default_factory=dict)  # language -> keys written
    failed: list = field(default_factory=list)  # (language, key, reasons)

    def to_json(self):
        return {
            "written": self.written,
            "failed": [{"language": l, "key": k, "reasons": r} for l, k, r in self.failed],
        }


# ---------------------------------------------------------------- model call

def anthropic_call(prompt, model=None, api_key=None):
    """Send one prompt to the Anthropic Messages API and return the reply text."""
    api_key = api_key or os.environ.get("ANTHROPIC_API_KEY")
    if not api_key:
        raise SystemExit("ANTHROPIC_API_KEY is not set; export it in the shell (never commit it).")
    body = json.dumps(
        {
            "model": model or os.environ.get("I18N_MODEL", DEFAULT_MODEL),
            "max_tokens": 8192,
            "messages": [{"role": "user", "content": prompt}],
        }
    ).encode()
    request = urllib.request.Request(
        API_URL,
        data=body,
        headers={
            "x-api-key": api_key,
            "anthropic-version": "2023-06-01",
            "content-type": "application/json",
        },
    )
    with urllib.request.urlopen(request, timeout=300) as response:
        reply = json.load(response)
    return "".join(part.get("text", "") for part in reply["content"])


# -------------------------------------------------------------------- prompt

def build_prompt(language, jobs, glossary, feedback=None):
    """The prompt for one batch. `feedback` maps job id -> reasons of the first try."""
    info = ct.LANGUAGES[language]
    items = {}
    for job in jobs:
        item = {"comment": job.entry.comment}
        if job.entry.kind == "string":
            item["english"] = job.entry.text
        else:
            item["english_plural_items"] = job.entry.items
            item["required_categories"] = list(info["plurals"])
        if feedback and job.id in feedback:
            item["fix_this"] = feedback[job.id]
        items[job.id] = item
    return (
        f"Translate Android app text from English into {info['name']} ({language}) for a "
        "rocket launch schedule app.\n\n"
        "Rules:\n"
        "- Keep every placeholder exactly: %1$s, %2$d, %s, %d, %%, and the escapes \\n and \\t. "
        "You may move a placeholder to fit the grammar of the language.\n"
        "- Do not add or remove placeholders. Do not add XML tags or entities.\n"
        "- Follow the glossary below. Terms marked keep stay in English.\n"
        "- The comment of each key says where the text shows and any length limit. Respect it.\n"
        "- For a plural entry give one text for each required category (CLDR). "
        "Write the number with its placeholder where English does.\n"
        "- Reply with one JSON object and nothing else. Each key is the id from the input. "
        "The value is the translated string, or for a plural entry an object of category -> string.\n\n"
        f"Glossary:\n{glossary}\n\n"
        f"Input:\n{json.dumps(items, ensure_ascii=False, indent=2)}\n"
    )


def parse_reply(text):
    """Read the JSON object out of a model reply (it may hold a code fence)."""
    match = re.search(r"\{.*\}", text, re.DOTALL)
    if not match:
        raise ValueError("no JSON object in the reply")
    data = json.loads(match.group(0))
    if not isinstance(data, dict):
        raise ValueError("reply is not a JSON object")
    return data


# ----------------------------------------------------------------- checking

def candidate(job, value):
    """Build the target Entry for a model answer, or return a reason string."""
    source = job.entry
    if source.kind == "string":
        if not isinstance(value, str) or not value.strip():
            return "expected a non-empty string"
        return ct.Entry(source.name, "string", text=value.replace("\\'", "'"))
    if not isinstance(value, dict):
        return "expected an object of plural categories"
    return ct.Entry(
        source.name, "plurals", items={q: str(v).replace("\\'", "'") for q, v in value.items()}
    )


def validate(job, value, language):
    """Return (Entry or None, reasons). Any reason, warnings included, means retry."""
    target = candidate(job, value)
    if isinstance(target, str):
        return None, [target]
    errors, warnings = ct.problems(job.entry, target, language)
    return target, errors + warnings


# ------------------------------------------------------------------ writing

def render(entry, android_res):
    """XML for one translated entry. Android res needs a quote escaped; Compose resources do not."""

    def text(value):
        value = escape(value)
        return re.sub(r"(?<!\\)(['\"])", r"\\\1", value) if android_res else value

    if entry.kind == "string":
        return f'    <string name="{entry.name}">{text(entry.text)}</string>'
    lines = [f'    <plurals name="{entry.name}">']
    lines += [f'        <item quantity="{q}">{text(v)}</item>' for q, v in entry.items.items()]
    lines.append("    </plurals>")
    return "\n".join(lines)


def append_entries(path, entries, android_res):
    """Insert entries before </resources>. Writes only names the file lacks."""
    path = Path(path)
    existing = ct.parse_resources(path)
    entries = [e for e in entries if e.name not in existing]
    if not entries:
        return []
    raw = path.read_bytes().decode("utf-8") if path.exists() else HEADER
    newline = "\r\n" if "\r\n" in raw else "\n"
    block = "\n".join(render(e, android_res) for e in entries).replace("\n", newline)
    head, tail = raw.rsplit("</resources>", 1)
    if not head.endswith(("\n", "\r")):
        head += newline
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes((head + block + newline + "</resources>" + tail).encode("utf-8"))
    return [e.name for e in entries]


# --------------------------------------------------------------------- flow

def missing_jobs(root, language, masters):
    """Jobs for the keys a language lacks. A shared_ key is one job across masters."""
    jobs = {}
    for master in masters:
        source = ct.parse_resources(Path(root) / master)
        have = ct.parse_resources(ct.target_path(Path(root) / master, language))
        for name, entry in source.items():
            if not entry.translatable or name in have:
                continue
            job_id = name if name.startswith(SHARED_PREFIX) else f"{master}::{name}"
            jobs.setdefault(job_id, Job(job_id, entry)).masters.append(master)
    return list(jobs.values())


def translate_language(root, language, jobs, call_model, glossary, batch_size):
    """Translate the jobs of one language. Returns ({job id: Entry}, {job id: reasons})."""
    good, failed = {}, {}
    pending, feedback = list(jobs), None
    for attempt in (1, 2):
        retry = []
        for start in range(0, len(pending), batch_size):
            batch = pending[start : start + batch_size]
            try:
                reply = parse_reply(call_model(build_prompt(language, batch, glossary, feedback)))
            except ValueError as error:  # includes JSON errors
                reply = {}
                for job in batch:
                    failed[job.id] = [f"bad reply: {error}"]
            for job in batch:
                if job.id not in reply:
                    failed.setdefault(job.id, ["no answer for this key"])
                    retry.append(job)
                    continue
                target, reasons = validate(job, reply[job.id], language)
                if reasons:
                    failed[job.id] = reasons
                    retry.append(job)
                else:
                    good[job.id] = target
                    failed.pop(job.id, None)
        if not retry:
            break
        pending, feedback = retry, {j.id: failed[j.id] for j in retry}
    return good, {k: v for k, v in failed.items() if k not in good}


def run(root, languages, call_model, glossary, masters=ct.MASTERS, batch_size=40, dry_run=False):
    root = Path(root)
    report = Report()
    for language in languages:
        jobs = missing_jobs(root, language, masters)
        if dry_run:
            report.written[language] = [j.id for j in jobs]
            continue
        if not jobs:
            report.written[language] = []
            continue
        good, failed = translate_language(root, language, jobs, call_model, glossary, batch_size)
        by_id = {j.id: j for j in jobs}
        per_master = {}
        for job_id, entry in good.items():
            for master in by_id[job_id].masters:
                per_master.setdefault(master, []).append(entry)
        written = []
        for master, entries in per_master.items():
            android_res = "composeResources" not in master
            written += append_entries(ct.target_path(root / master, language), entries, android_res)
        report.written[language] = written
        for job_id, reasons in failed.items():
            report.failed.append((language, by_id[job_id].entry.name, reasons))
    return report


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    parser.add_argument("--root", default=Path(__file__).resolve().parents[2], type=Path)
    parser.add_argument("--lang", help="comma list of codes; default all 10")
    parser.add_argument("--batch-size", type=int, default=40)
    parser.add_argument("--dry-run", action="store_true", help="list the missing keys; no model call")
    parser.add_argument("--report", type=Path, help="write the report as JSON")
    args = parser.parse_args(argv)

    languages = args.lang.split(",") if args.lang else list(ct.LANGUAGES)
    unknown = [l for l in languages if l not in ct.LANGUAGES]
    if unknown:
        parser.error(f"unknown language {unknown}; use {list(ct.LANGUAGES)}")
    glossary_path = args.root / GLOSSARY
    if not glossary_path.exists():
        parser.error(f"glossary not found: {glossary_path}")

    report = run(
        args.root,
        languages,
        anthropic_call,
        glossary_path.read_text(encoding="utf-8"),
        batch_size=args.batch_size,
        dry_run=args.dry_run,
    )
    verb = "missing" if args.dry_run else "written"
    for language, keys in report.written.items():
        print(f"{language:6} {verb}: {len(keys)}")
    for language, key, reasons in report.failed:
        print(f"FAILED {language} {key}: {'; '.join(reasons)}")
    if args.report:
        args.report.write_text(json.dumps(report.to_json(), indent=2, ensure_ascii=False), encoding="utf-8")
    return 1 if report.failed else 0


if __name__ == "__main__":
    sys.exit(main())
