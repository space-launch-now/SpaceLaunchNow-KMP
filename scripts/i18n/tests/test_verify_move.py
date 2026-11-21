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
    <string name="two_lines">One\\nTwo</string>
    <plurals name="launches"><item quantity="one">%d launch</item></plurals>
</resources>
"""


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
        return verify_move.verify(diff_of(removed, added), self.strings, self.other)

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
        self.assertEqual([], verify_move.verify(diff, self.strings, self.other))

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
        self.assertEqual([], verify_move.verify(self.hoisted_diff("About"), self.strings, self.other))

    def test_hoisted_literal_with_wrong_text_fails(self):
        self.assertEqual(1, len(verify_move.verify(self.hoisted_diff("About us"), self.strings, self.other)))

    def test_removed_literal_in_another_file_does_not_match(self):
        diff = (diff_of(['Text("About")'], ["Text(1)"], path="composeApp/src/commonMain/kotlin/B.kt")
                + diff_of([], ["Text(stringResource(Res.string.about_title))"]))
        self.assertEqual(1, len(verify_move.verify(diff, self.strings, self.other)))

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

    @staticmethod
    def hoisted_diff(removed_text):
        """A key added in one hunk, its literal removed in a later hunk of the same file."""
        path = "composeApp/src/commonMain/kotlin/A.kt"
        return (f"diff --git a/{path} b/{path}\n--- a/{path}\n+++ b/{path}\n"
                "@@ -5,0 +6 @@\n+val d = stringResource(Res.string.about_title)\n"
                f'@@ -40 +41 @@\n-Text("{removed_text}")\n+Text(d)\n')

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
