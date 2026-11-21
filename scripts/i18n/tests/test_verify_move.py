import os
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import verify_move  # noqa: E402

STRINGS_XML = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="about_title">About</string>
    <string name="version">Version: %1$s (%2$d)</string>
    <string name="quote">It\\'s &amp; \\"fine\\"</string>
    <string name="about_alt">About</string>
    <string name="other_title">Other</string>
    <string name="version_short">Version: %1$s</string>
    <string name="two_lines">One\\nTwo</string>
    <plurals name="launches"><item quantity="one">%d launch</item></plurals>
</resources>
"""


def failures_of(diff, strings, other):
    return verify_move.verify(diff, strings, other)[0]


def diff_of(removed, added, path="composeApp/src/commonMain/kotlin/A.kt"):
    return (f"diff --git a/{path} b/{path}\n--- a/{path}\n+++ b/{path}\n@@ -1,1 +1,1 @@\n"
            + "".join(f"-{r}\n" for r in removed) + "".join(f"+{a}\n" for a in added))


class VerifyMoveTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.tmp = tempfile.TemporaryDirectory()
        path = Path(cls.tmp.name) / "strings.xml"
        path.write_text(STRINGS_XML, encoding="utf-8")
        cls.strings, cls.other = verify_move.load_strings([path])

    @classmethod
    def tearDownClass(cls):
        cls.tmp.cleanup()

    def run_diff(self, removed, added):
        return failures_of(diff_of(removed, added), self.strings, self.other)

    def test_equal_text_passes(self):
        self.assertEqual([], self.run_diff(['Text("About")'], ["Text(stringResource(Res.string.about_title))"]))

    def test_different_text_fails(self):
        out = self.run_diff(['Text("About us")'], ["Text(stringResource(Res.string.about_title))"])
        self.assertEqual(1, len(out))
        self.assertIn("about_title", out[0])

    def test_trailing_space_difference_fails(self):
        self.assertEqual(1, len(self.run_diff(['Text("About ")'], ["Text(stringResource(Res.string.about_title))"])))

    def test_template_maps_to_positional_placeholders(self):
        removed = ['Text("Version: $name ($build)")']
        added = ["Text(stringResource(Res.string.version, name, build))"]
        self.assertEqual([], self.run_diff(removed, added))

    def test_braced_template_maps(self):
        removed = ['Text("Version: ${info.name} (${info.build})")']
        added = ["Text(stringResource(Res.string.version, info.name, info.build))"]
        self.assertEqual([], self.run_diff(removed, added))

    def test_placeholder_count_mismatch_fails(self):
        self.assertEqual(1, len(self.run_diff(['Text("Version: $name")'], ["Text(stringResource(Res.string.version, name))"])))

    def test_xml_and_android_escapes_unescape(self):
        self.assertEqual([], self.run_diff([r'Text("It' + "'" + r's & \"fine\"")'], ["Text(stringResource(Res.string.quote))"]))

    def test_newline_escape(self):
        self.assertEqual([], self.run_diff([r'Text("One\nTwo")'], ["Text(stringResource(Res.string.two_lines))"]))

    def test_unknown_key_fails(self):
        out = self.run_diff(['Text("Hi")'], ["Text(stringResource(Res.string.nope))"])
        self.assertIn("not in any strings.xml", out[0])

    def test_plural_key_is_skipped(self):
        self.assertEqual([], self.run_diff(['Text("3 launches")'], ["Text(pluralStringResource(Res.plurals.launches, 3, 3))"]))

    def test_non_kotlin_file_is_ignored(self):
        diff = diff_of(['<x>"Hi"</x>'], ["Res.string.nope"], path="other/file.xml")
        self.assertEqual([], failures_of(diff, self.strings, self.other))

    def test_touches_strings(self):
        self.assertTrue(verify_move.touches_strings(diff_of(["a"], ["b"], path="x/values/strings.xml")))
        self.assertTrue(verify_move.touches_strings(diff_of(["a"], ["b"], path="x/values-de/strings_extra.xml")))
        self.assertFalse(verify_move.touches_strings(diff_of(["a"], ["b"])))

    def test_concatenated_literals_match_one_resource(self):
        removed = ['Text("Version: " +', '     "$name ($build)")']
        added = ["Text(stringResource(Res.string.version, name, build))"]
        self.assertEqual([], self.run_diff(removed, added))

    def test_concatenation_with_wrong_text_fails(self):
        removed = ['Text("Version: " +', '     "$name")']
        added = ["Text(stringResource(Res.string.version, name, build))"]
        self.assertEqual(1, len(self.run_diff(removed, added)))

    def test_template_with_nested_quotes_matches(self):
        removed = ['Modifier.semantics { contentDescription = "Version: ${a ?: ""} ($b)" }']
        added = ['val d = stringResource(Res.string.version, a ?: "", b)']
        self.assertEqual([], self.run_diff(removed, added))

    def test_hoisted_literal_in_another_hunk_matches(self):
        self.assertEqual([], failures_of(self.hoisted_diff("About"), self.strings, self.other))

    def test_hoisted_literal_with_wrong_text_fails(self):
        self.assertEqual(1, len(failures_of(self.hoisted_diff("About us"), self.strings, self.other)))

    def test_removed_literal_in_another_file_does_not_match(self):
        diff = (diff_of(['Text("About")'], ["Text(1)"], path="composeApp/src/commonMain/kotlin/B.kt")
                + diff_of([], ["Text(stringResource(Res.string.about_title))"]))
        self.assertEqual(1, len(failures_of(diff, self.strings, self.other)))

    def test_default_strings_reads_every_strings_file(self):
        root = Path(self.tmp.name) / "repo"
        values = root / verify_move.VALUES_DIR
        values.mkdir(parents=True)
        for name in ("strings.xml", "strings_time.xml", "other.xml"):
            (values / name).write_text(STRINGS_XML, encoding="utf-8")
        cwd = Path.cwd()
        try:
            os.chdir(root)
            found = [p.name for p in verify_move.default_strings()]
        finally:
            os.chdir(cwd)
        self.assertEqual(["strings.xml", "strings_time.xml"], found)

    def test_key_swap_with_equal_english_passes(self):
        out = self.run_diff(["Text(stringResource(Res.string.about_alt))"],
                            ["Text(stringResource(Res.string.about_title))"])
        self.assertEqual([], out)

    def test_key_swap_with_different_english_fails(self):
        out = self.run_diff(["Text(stringResource(Res.string.other_title))"],
                            ["Text(stringResource(Res.string.about_title))"])
        self.assertEqual(1, len(out))
        self.assertIn("about_title", out[0])

    def test_key_swap_with_different_placeholder_fails(self):
        out = self.run_diff(["Text(stringResource(Res.string.version_short, name))"],
                            ["Text(stringResource(Res.string.version, name, build))"])
        self.assertEqual(1, len(out))

    def test_key_swap_passes_when_old_key_is_deleted_in_the_same_diff(self):
        xml = "composeApp/src/commonMain/composeResources/values/strings.xml"
        diff = (diff_of(["Text(stringResource(Res.string.gone_key))"], ["Text(stringResource(Res.string.about_title))"])
                + f"diff --git a/{xml} b/{xml}\n--- a/{xml}\n+++ b/{xml}\n@@ -9 +8,0 @@\n"
                  '-    <string name="gone_key">About</string>\n')
        self.assertEqual([], failures_of(diff, self.strings, self.other))
        # the same swap fails when the removed XML line holds different English
        bad = diff.replace(">About</string>", ">Elsewhere</string>")
        self.assertEqual(1, len(failures_of(bad, self.strings, self.other)))

    def test_key_swap_passes_when_old_key_is_only_in_the_base_revision(self):
        diff = diff_of(["Text(stringResource(Res.string.gone_key))"], ["Text(stringResource(Res.string.about_title))"])
        self.assertEqual([], verify_move.verify(diff, self.strings, self.other, {"gone_key": "About"})[0])
        self.assertEqual(1, len(verify_move.verify(diff, self.strings, self.other, {"gone_key": "Nope"})[0]))

    @staticmethod
    def hoisted_diff(removed_text):
        """A key added in one hunk, its literal removed in a later hunk of the same file."""
        path = "composeApp/src/commonMain/kotlin/A.kt"
        return (f"diff --git a/{path} b/{path}\n--- a/{path}\n+++ b/{path}\n"
                "@@ -5,0 +6 @@\n+val d = stringResource(Res.string.about_title)\n"
                f'@@ -40 +41 @@\n-Text("{removed_text}")\n+Text(d)\n')

    def test_key_in_a_new_file_counts_as_new_text(self):
        path = "composeApp/src/commonMain/kotlin/New.kt"
        diff = (f"diff --git a/{path} b/{path}\nnew file mode 100644\n--- /dev/null\n+++ b/{path}\n"
                "@@ -0,0 +1 @@\n+val t = getString(Res.string.about_title)\n")
        failures, new_text = verify_move.verify(diff, self.strings, self.other)
        self.assertEqual([], failures)
        self.assertEqual([f"{path}: about_title"], new_text)

    def test_key_in_an_existing_file_is_not_new_text(self):
        failures, new_text = verify_move.verify(
            diff_of([], ["Text(stringResource(Res.string.about_title))"]), self.strings, self.other)
        self.assertEqual(1, len(failures))
        self.assertEqual([], new_text)

    def test_literal_in_braces_inside_a_template_matches(self):
        removed = ['Text("${name.ifBlank { "About" }}")']
        self.assertEqual([], self.run_diff(removed, ["Text(stringResource(Res.string.about_title))"]))

    def test_braces_inside_a_template_become_one_placeholder(self):
        removed = ['Text("Version: ${name.ifBlank { "?" }} ($build)")']
        added = ["Text(stringResource(Res.string.version, name, build))"]
        self.assertEqual([], self.run_diff(removed, added))

    def test_literal_nested_in_template_expression_matches(self):
        removed = ['Text("${a ?: "About"} km")']
        self.assertEqual([], self.run_diff(removed, ["Text(stringResource(Res.string.about_title))"]))

    def test_template_expression_across_lines_matches(self):
        added = ["stringResource(Res.string.version, f, g)"]
        self.assertEqual(1, len(self.run_diff(['"Version: ${', "    f", '}"'], added)))  # needs 2 placeholders
        self.assertEqual([], self.run_diff(['"Version: ${', "    f", '} (${g})"'], added))

    def test_main_exit_codes(self):
        base = Path(self.tmp.name)
        good, bad = base / "good.diff", base / "bad.diff"
        good.write_text(diff_of(['Text("About")'], ["stringResource(Res.string.about_title)"]), encoding="utf-8")
        bad.write_text(diff_of(['Text("Abou")'], ["stringResource(Res.string.about_title)"]), encoding="utf-8")
        strings = str(base / "strings.xml")
        self.assertEqual(0, verify_move.main(["--diff", str(good), "--strings", strings]))
        self.assertEqual(1, verify_move.main(["--diff", str(bad), "--strings", strings]))


if __name__ == "__main__":
    unittest.main()
