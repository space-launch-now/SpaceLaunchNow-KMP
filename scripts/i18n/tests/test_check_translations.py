"""Tests for check_translations.py. Run: python3 -m unittest discover -s scripts/i18n/tests"""
import io
import sys
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_translations as ct  # noqa: E402

MASTER = ct.MASTERS[0]
ENGLISH = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Button, one line, max 6 chars. -->
    <string name="common_retry">Retry</string>
    <string name="about_version">Version: %1$s (%2$d)\\nline two</string>
    <string name="app_name">Space Launch Now</string>
    <plurals name="launch_count">
        <item quantity="one">%d launch</item>
        <item quantity="other">%d launches</item>
    </plurals>
</resources>
"""


def write(root, relative, text):
    path = Path(root) / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")
    return path


def resources(body):
    return f'<?xml version="1.0" encoding="utf-8"?>\n<resources>\n{body}\n</resources>\n'


class ParseTest(unittest.TestCase):
    def test_reads_comments_plurals_and_skips_app_name(self):
        with tempfile.TemporaryDirectory() as root:
            entries = ct.parse_resources(write(root, MASTER, ENGLISH))
        self.assertEqual(entries["common_retry"].comment, "Button, one line, max 6 chars.")
        self.assertEqual(entries["launch_count"].items["other"], "%d launches")
        self.assertFalse(entries["app_name"].translatable)

    def test_missing_file_gives_empty(self):
        self.assertEqual(ct.parse_resources("/no/such/strings.xml"), {})


class PlaceholderTest(unittest.TestCase):
    def test_tokens(self):
        self.assertEqual(
            ct.placeholders("A %1$s, %2$d, %% \\n and \\' quote"), ["%%", "%1$s", "%2$d", "\\n"]
        )

    def test_percent_sign_in_prose_is_not_a_placeholder(self):
        self.assertEqual(ct.placeholders("50 % Rabatt, 50% off"), [])

    def test_length_limit(self):
        self.assertEqual(ct.length_limit("Button. max 14 chars."), 14)
        self.assertIsNone(ct.length_limit("Heading."))


class ProblemsTest(unittest.TestCase):
    source = ct.Entry("k", "string", text="Version: %1$s (%2$d)\\n")

    def string(self, text):
        return ct.Entry("k", "string", text=text)

    def test_matching_placeholders_pass(self):
        self.assertEqual(ct.problems(self.source, self.string("Version: %2$d / %1$s\\n"), "de"), ([], []))

    def test_changed_format_fails(self):
        errors, _ = ct.problems(self.source, self.string("Version: %1$s (%2$s)\\n"), "de")
        self.assertEqual(len(errors), 1)

    def test_dropped_newline_escape_fails(self):
        errors, _ = ct.problems(self.source, self.string("Version: %1$s (%2$d)"), "de")
        self.assertEqual(len(errors), 1)

    def test_length_limit_is_a_warning(self):
        source = ct.Entry("k", "string", text="Retry", comment="max 6 chars")
        errors, warnings = ct.problems(source, self.string("Erneut versuchen"), "de")
        self.assertEqual(errors, [])
        self.assertEqual(len(warnings), 1)


class PluralTest(unittest.TestCase):
    source = ct.Entry("n", "plurals", items={"one": "%d launch", "other": "%d launches"})

    def plurals(self, *quantities):
        return ct.Entry("n", "plurals", items={q: "%d" for q in quantities})

    def test_required_categories_per_language(self):
        expected = {
            "de": ("one", "other"),
            "es-ES": ("one", "many", "other"),
            "fr": ("one", "many", "other"),
            "zh-CN": ("other",),
            "nl": ("one", "other"),
            "uk": ("one", "few", "many", "other"),
            "it": ("one", "many", "other"),
            "pl": ("one", "few", "many", "other"),
            "pt-BR": ("one", "many", "other"),
            "cs": ("one", "few", "many", "other"),
        }
        self.assertEqual({k: v["plurals"] for k, v in ct.LANGUAGES.items()}, expected)

    def test_complete_set_has_no_warning_for_every_language(self):
        for language, info in ct.LANGUAGES.items():
            with self.subTest(language=language):
                self.assertEqual(ct.problems(self.source, self.plurals(*info["plurals"]), language), ([], []))

    def test_missing_category_warns(self):
        for language in ("uk", "pl", "cs"):
            with self.subTest(language=language):
                _, warnings = ct.problems(self.source, self.plurals("one", "other"), language)
                self.assertIn("missing plural categories ['few', 'many']", warnings[0])

    def test_extra_category_warns_for_chinese(self):
        _, warnings = ct.problems(self.source, self.plurals("one", "other"), "zh-CN")
        self.assertIn("not used by zh-CN", warnings[0])

    def test_plural_item_may_drop_number_but_not_add_one(self):
        ok = ct.Entry("n", "plurals", items={"one": "Ein Start", "other": "%d Starts"})
        self.assertEqual(ct.problems(self.source, ok, "de")[0], [])
        bad = ct.Entry("n", "plurals", items={"one": "%s Start", "other": "%d Starts"})
        self.assertEqual(len(ct.problems(self.source, bad, "de")[0]), 1)


class CheckRunTest(unittest.TestCase):
    def build(self, root, german):
        write(root, MASTER, ENGLISH)
        write(root, ct.target_path(MASTER, "de"), resources(german))

    def run_main(self, root, *flags):
        out = io.StringIO()
        with redirect_stdout(out):
            code = ct.main(["--root", root, *flags])
        return code, out.getvalue()

    def test_coverage_and_exit_code_ok(self):
        with tempfile.TemporaryDirectory() as root:
            self.build(
                root,
                '<string name="common_retry">Neu</string>\n'
                '<string name="about_version">V: %1$s (%2$d)\\nzwei</string>',
            )
            code, out = self.run_main(root)
        self.assertEqual(code, 0)
        self.assertIn("2/3", out)
        self.assertIn("below 95%", out)

    def test_placeholder_mismatch_fails_build(self):
        with tempfile.TemporaryDirectory() as root:
            self.build(root, '<string name="about_version">V: %1$s</string>')
            code, out = self.run_main(root)
        self.assertEqual(code, 1)
        self.assertIn("error:", out)

    def test_missing_plural_category_fails_only_when_strict(self):
        german = '<plurals name="launch_count"><item quantity="other">%d</item></plurals>'
        with tempfile.TemporaryDirectory() as root:
            self.build(root, german)
            lenient = self.run_main(root)[0]
            strict = self.run_main(root, "--strict-plurals")[0]
        self.assertEqual((lenient, strict), (0, 1))


if __name__ == "__main__":
    unittest.main()
