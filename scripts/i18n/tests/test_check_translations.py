"""Tests for check_translations.py. Run: python3 -m unittest discover -s scripts/i18n/tests"""
import io
import json
import sys
import tempfile
import unittest
from contextlib import redirect_stdout
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import check_translations as ct  # noqa: E402

MASTER = "composeApp/src/commonMain/composeResources/values/strings.xml"
FEATURE = "composeApp/src/commonMain/composeResources/values/strings_home.xml"
CATALOG = ct.CATALOGS[1]
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


class FeatureFileTest(unittest.TestCase):
    """Every values/*.xml is a master, not only strings.xml."""

    def run_main(self, root):
        out = io.StringIO()
        with redirect_stdout(out):
            code = ct.main(["--root", root])
        return code, out.getvalue()

    def build(self, german):
        root = tempfile.TemporaryDirectory()
        self.addCleanup(root.cleanup)
        write(root.name, MASTER, ENGLISH)
        write(root.name, FEATURE, resources('<string name="home_title">Hello %1$s</string>'))
        if german is not None:
            write(root.name, ct.target_path(FEATURE, "de"), resources(german))
        return root.name

    def test_glob_finds_feature_files_and_keeps_fixed_masters(self):
        root = self.build(None)
        self.assertEqual(ct.find_masters(root), (MASTER, FEATURE, *ct.FIXED_MASTERS))

    def test_placeholder_error_in_feature_file_fails(self):
        code, out = self.run_main(self.build('<string name="home_title">Hallo %2$s</string>'))
        self.assertEqual(code, 1)
        self.assertIn("strings_home.xml", out)
        self.assertIn("error:", out)

    def test_good_feature_file_passes_and_missing_key_only_warns(self):
        code, out = self.run_main(self.build('<string name="home_title">Hallo %1$s</string>'))
        self.assertEqual(code, 0)
        self.assertIn("strings_home.xml (1 keys)", out)

    def test_language_not_started_is_not_a_failure(self):
        code, out = self.run_main(self.build(None))
        self.assertEqual(code, 0)
        self.assertIn("0 of 10 languages started", out)


class CatalogTest(unittest.TestCase):
    def catalog(self, **languages):
        """English `Last tried: %@` plus one translation per language code given."""

        def unit(value):
            return {"stringUnit": {"state": "translated", "value": value}}

        strings = {
            "last_tried": {"localizations": {"en": unit("Last tried: %@"), **{c: unit(v) for c, v in languages.items()}}},
            "title": {"localizations": {"en": unit("Widget Error"), **{c: unit("T") for c in languages}}},
        }
        return json.dumps({"sourceLanguage": "en", "strings": strings, "version": "1.0"})

    def run_main(self, text):
        with tempfile.TemporaryDirectory() as root:
            write(root, CATALOG, text)
            out = io.StringIO()
            with redirect_stdout(out):
                code = ct.main(["--root", root])
        return code, out.getvalue()

    def test_placeholder_mismatch_fails(self):
        code, out = self.run_main(self.catalog(de="Zuletzt: %d"))
        self.assertEqual(code, 1)
        self.assertIn("error:", out)
        self.assertIn("last_tried", out)

    def test_matching_placeholder_passes_with_a_position_number(self):
        code, _ = self.run_main(self.catalog(de="Zuletzt: %1$@"))
        self.assertEqual(code, 0)

    def test_key_with_no_value_warns_and_does_not_fail(self):
        text = json.loads(self.catalog(de="Zuletzt: %@"))
        text["strings"]["title"]["localizations"]["de"]["stringUnit"]["value"] = ""
        code, out = self.run_main(json.dumps(text))
        self.assertEqual(code, 0)
        self.assertIn("no value for 1 keys, first title", out)

    def test_catalog_codes_are_pt_br_and_zh_hans(self):
        code, out = self.run_main(self.catalog(**{"pt-BR": "Última: %@", "zh-Hans": "最后：%d", "zh-CN": "ignored %d"}))
        self.assertEqual(code, 1)  # zh-Hans is read; zh-CN is not a catalog code
        self.assertEqual(out.count("error:"), 1)
        self.assertIn("[zh-Hans]", out)
        self.assertIn("2 of 10 languages started", out)


class RepoCatalogsTest(unittest.TestCase):
    def test_notification_text_catalog_is_checked_and_clean(self):
        catalog = "iosApp/iosApp/NotificationText.xcstrings"
        self.assertIn(catalog, ct.CATALOGS)
        root = Path(__file__).resolve().parents[3]
        rows, errors, _ = ct.check_catalog(root / catalog, catalog)
        self.assertEqual(errors, [])
        self.assertEqual(len(rows), len(ct.LANGUAGES))
        self.assertTrue(all(done == total > 0 for _, _, done, total in rows))


if __name__ == "__main__":
    unittest.main()
