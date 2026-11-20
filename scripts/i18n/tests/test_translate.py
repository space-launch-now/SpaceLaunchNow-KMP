"""Tests for translate.py with a stubbed model. No test calls the network.

Run: python3 -m unittest discover -s scripts/i18n/tests
"""
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_translations as ct  # noqa: E402
import translate as tr  # noqa: E402

COMPOSE, ANDROID, WEAR = ct.MASTERS
ALL_PLURAL_ITEMS = {"one": "%d x", "few": "%d y", "many": "%d z", "other": "%d w"}

COMPOSE_XML = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Button, max 20 chars. -->
    <string name="common_retry">Retry</string>
    <string name="about_version">Version: %1$s (%2$d)\\nbuild</string>
    <string name="shared_no_launches">No upcoming launches</string>
    <plurals name="launch_count">
        <item quantity="one">%d launch</item>
        <item quantity="other">%d launches</item>
    </plurals>
</resources>
"""
ANDROID_XML = """<resources>
    <string name="app_name">Space Launch Now</string>
    <string name="shared_no_launches">No upcoming launches</string>
    <string name="notification_ok">It's a success at %s</string>
</resources>"""


def put(root, relative, text):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(text.encode("utf-8"))
    return path


class StubModel:
    """Answers each requested id from `answers`; a list gives one answer per call."""

    def __init__(self, answers):
        self.answers = answers
        self.prompts = []

    def __call__(self, prompt):
        self.prompts.append(prompt)
        answers = self.answers
        if isinstance(answers, list):
            answers = answers[min(len(self.prompts), len(answers)) - 1]
        wanted = [i for i in answers if f'"{i}"' in prompt.split("Input:", 1)[1]]
        return "```json\n" + json.dumps({i: answers[i] for i in wanted}) + "\n```"


class TranslateTest(unittest.TestCase):
    def setUp(self):
        self._tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self._tmp.cleanup)
        self.root = Path(self._tmp.name)
        put(self.root, COMPOSE, COMPOSE_XML)
        put(self.root, ANDROID, ANDROID_XML)
        self.masters = (COMPOSE, ANDROID)
        self.good = {
            f"{COMPOSE}::common_retry": "Erneut versuchen",
            f"{COMPOSE}::about_version": "Version: %1$s (%2$d)\\nBuild",
            "shared_no_launches": "Keine bevorstehenden Starts",
            f"{COMPOSE}::launch_count": {"one": "%d Start", "other": "%d Starts"},
            f"{ANDROID}::notification_ok": "Das war ein Erfolg um %s, l'heure est là",
        }

    def run_de(self, model, **kwargs):
        return tr.run(self.root, ["de"], model, "glossary text", masters=self.masters, **kwargs)

    def read(self, master, language="de"):
        return ct.target_path(self.root / master, language)

    def test_writes_missing_keys_to_each_master(self):
        report = self.run_de(StubModel(self.good))
        self.assertEqual(report.failed, [])
        compose = ct.parse_resources(self.read(COMPOSE))
        self.assertEqual(set(compose), {"common_retry", "about_version", "shared_no_launches", "launch_count"})
        self.assertEqual(compose["launch_count"].items["one"], "%d Start")
        android = ct.parse_resources(self.read(ANDROID))
        self.assertEqual(set(android), {"shared_no_launches", "notification_ok"})  # app_name skipped

    def test_shared_key_is_translated_once_and_written_twice(self):
        model = StubModel(self.good)
        self.run_de(model)
        self.assertEqual(sum(p.count('"shared_no_launches"') for p in model.prompts), 1)
        for master in self.masters:
            self.assertEqual(ct.parse_resources(self.read(master))["shared_no_launches"].text, "Keine bevorstehenden Starts")

    def test_existing_key_is_never_rewritten_and_not_requested(self):
        target = put(
            self.root,
            self.read(COMPOSE).relative_to(self.root),
            '<?xml version="1.0" encoding="utf-8"?>\r\n<resources>\r\n'
            '    <string name="common_retry">Nochmal (von Hand)</string>\r\n</resources>\r\n',
        )
        model = StubModel(self.good)
        self.run_de(model)
        self.assertNotIn(f'"{COMPOSE}::common_retry"', "".join(model.prompts))
        text = target.read_bytes().decode()
        self.assertIn("Nochmal (von Hand)", text)
        self.assertNotIn("Erneut versuchen", text)
        self.assertNotIn("\r\n\n", text.replace("\r\n", "\n\n"))  # still CRLF only
        self.assertEqual(text.count("\n"), text.count("\r\n"))

    def test_second_run_writes_nothing(self):
        self.run_de(StubModel(self.good))
        before = {m: self.read(m).read_bytes() for m in self.masters}
        model = StubModel(self.good)
        report = self.run_de(model)
        self.assertEqual(model.prompts, [])
        self.assertEqual(report.written, {"de": []})
        self.assertEqual(before, {m: self.read(m).read_bytes() for m in self.masters})

    def test_changed_placeholder_is_retried_once_then_accepted(self):
        bad = dict(self.good)
        bad[f"{COMPOSE}::about_version"] = "Version: %1$s (%2$s)\\nBuild"  # %d became %s
        model = StubModel([bad, self.good])
        report = self.run_de(model)
        self.assertEqual(report.failed, [])
        self.assertEqual(len(model.prompts), 2)
        retry_prompt = model.prompts[1]
        self.assertIn("fix_this", retry_prompt)
        self.assertIn("about_version", retry_prompt)
        self.assertNotIn('"common_retry"', retry_prompt.split("Input:", 1)[1])  # only the failed key
        self.assertEqual(ct.parse_resources(self.read(COMPOSE))["about_version"].text, "Version: %1$s (%2$d)\\nBuild")

    def test_placeholder_still_wrong_after_retry_is_not_written_and_reported(self):
        bad = dict(self.good)
        bad[f"{COMPOSE}::about_version"] = "Version: %1$s"
        model = StubModel(bad)
        report = self.run_de(model)
        self.assertEqual(len(model.prompts), 2)  # one retry, no third try
        self.assertEqual([(l, k) for l, k, _ in report.failed], [("de", "about_version")])
        self.assertNotIn("about_version", ct.parse_resources(self.read(COMPOSE)))
        self.assertIn("common_retry", ct.parse_resources(self.read(COMPOSE)))

    def test_missing_newline_escape_is_rejected(self):
        bad = dict(self.good)
        bad[f"{COMPOSE}::about_version"] = "Version: %1$s (%2$d)\nBuild"  # real newline, not \n
        report = self.run_de(StubModel(bad))
        self.assertEqual(len(report.failed), 1)

    def test_plural_categories_must_be_complete_for_language(self):
        answers = {**self.good, f"{COMPOSE}::launch_count": ALL_PLURAL_ITEMS}
        incomplete = {**self.good, f"{COMPOSE}::launch_count": {"one": "%d x", "other": "%d w"}}
        put(self.root, COMPOSE, COMPOSE_XML)
        model = StubModel([incomplete, answers])
        report = tr.run(self.root, ["uk"], model, "g", masters=(COMPOSE,))
        uk = ct.parse_resources(self.read(COMPOSE, "uk"))
        self.assertEqual(len(model.prompts), 2)
        self.assertIn("missing plural categories", model.prompts[1])
        self.assertEqual(set(uk["launch_count"].items), {"one", "few", "many", "other"})
        self.assertEqual(report.failed, [])

    def test_chinese_plural_needs_only_other(self):
        answers = {**self.good, f"{COMPOSE}::launch_count": {"other": "%d 次发射"}}
        model = StubModel(answers)
        tr.run(self.root, ["zh-CN"], model, "g", masters=(COMPOSE,))
        self.assertEqual(len(model.prompts), 1)
        path = self.root / "composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml"
        self.assertEqual(set(ct.parse_resources(path)["launch_count"].items), {"other"})

    def test_apostrophe_escaped_for_android_res_but_not_compose(self):
        answers = {
            f"{COMPOSE}::common_retry": "L'essai",
            "shared_no_launches": "Pas d'envol",
            f"{ANDROID}::notification_ok": "Succès à %s, c'est l'heure",
        }
        self.run_de(StubModel({**self.good, **answers}))
        compose_raw = self.read(COMPOSE).read_text(encoding="utf-8")
        android_raw = self.read(ANDROID).read_text(encoding="utf-8")
        self.assertIn(">L'essai<", compose_raw)
        self.assertIn("c\\'est l\\'heure", android_raw)
        self.assertEqual(ct.parse_resources(self.read(ANDROID))["notification_ok"].text, "Succès à %s, c\\'est l\\'heure")

    def test_xml_special_characters_are_escaped(self):
        answers = {**self.good, f"{COMPOSE}::common_retry": "A & B <C>"}
        self.run_de(StubModel(answers))
        self.assertEqual(ct.parse_resources(self.read(COMPOSE))["common_retry"].text, "A & B <C>")

    def test_dry_run_calls_no_model_and_writes_nothing(self):
        model = StubModel(self.good)
        report = self.run_de(model, dry_run=True)
        self.assertEqual(model.prompts, [])
        self.assertEqual(len(report.written["de"]), 5)
        self.assertFalse(self.read(COMPOSE).exists())

    def test_prompt_carries_glossary_comment_and_language(self):
        model = StubModel(self.good)
        self.run_de(model)
        prompt = model.prompts[0]
        self.assertIn("glossary text", prompt)
        self.assertIn("Button, max 20 chars.", prompt)
        self.assertIn("German (de)", prompt)

    def test_unparseable_reply_is_reported_not_raised(self):
        prompts = []
        report = self.run_de(lambda prompt: prompts.append(prompt) or "sorry, no")
        self.assertEqual(len(prompts), 2)  # one retry
        self.assertEqual(len(report.failed), 5)
        self.assertFalse(self.read(COMPOSE).exists())

    def test_no_api_key_in_repo_files(self):
        here = Path(tr.__file__).parent
        for path in list(here.glob("*.py")) + list(here.glob("tests/*.py")):
            self.assertNotRegex(path.read_text(encoding="utf-8"), r"sk-ant-[A-Za-z0-9_-]{10,}")


if __name__ == "__main__":
    unittest.main()
