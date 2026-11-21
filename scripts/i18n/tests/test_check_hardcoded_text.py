import contextlib
import io
import json
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import check_hardcoded_text as chk  # noqa: E402

SCREEN = "composeApp/src/commonMain/kotlin/ui/Home.kt"


class CheckHardcodedTextTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.baseline = self.root / "baseline.txt"

    def write(self, rel, text):
        path = self.root / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8")

    def run_check(self, *extra):
        out = io.StringIO()
        with contextlib.redirect_stdout(out):
            code = chk.main(["--root", str(self.root), "--baseline", str(self.baseline), *extra])
        return code, out.getvalue()

    def test_new_literal_fails(self):
        self.write(SCREEN, 'fun A() { Text("Next Launch") }\n')
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn('Home.kt:1: "Next Launch"', out)

    def test_new_literal_in_android_app_fails(self):
        self.write("androidApp/src/main/kotlin/ui/Home.kt", 'fun A() { Text("Next Launch") }\n')
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn('androidApp/src/main/kotlin/ui/Home.kt:1: "Next Launch"', out)

    def test_baseline_literal_passes_at_any_line(self):
        self.write(SCREEN, 'fun A() { Text("Next Launch") }\n')
        self.run_check("--update")
        self.write(SCREEN, '// moved down\n\n\nfun A() { Text("Next Launch") }\n')
        code, _ = self.run_check()
        self.assertEqual(code, 0)

    def test_second_copy_of_baseline_literal_fails(self):
        self.write(SCREEN, 'Text("Hi")\n')
        self.run_check("--update")
        self.write(SCREEN, 'Text("Hi")\nText("Hi")\n')
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("1 new", out)

    def test_ignore_comment_same_line_and_line_above(self):
        self.write(SCREEN, 'Text("A b") // i18n-ignore: log tag\n'
                           '// i18n-ignore: log tag\nTitle(title = "C d")\n')
        code, _ = self.run_check()
        self.assertEqual(code, 0)

    def test_ignore_needs_a_reason(self):
        self.write(SCREEN, 'Text("A b") // i18n-ignore:\n')
        code, _ = self.run_check()
        self.assertEqual(code, 1)

    def test_named_argument_and_identifier_rules(self):
        self.write(SCREEN, 'X(contentDescription = "Close dialog")\nX(label = "some_id")\nX(other = "Not UI")\n')
        findings = chk.scan(self.root)
        self.assertEqual([f[2] for f in findings], ["Close dialog"])

    def test_debug_and_test_files_are_skipped(self):
        self.write("composeApp/src/commonMain/kotlin/ui/DebugScreen.kt", 'Text("Debug")\n')
        self.write("composeApp/src/commonTest/kotlin/ui/HomeTest.kt", 'Text("Test")\n')
        self.write("composeApp/src/desktopTest/kotlin/ui/X.kt", 'Text("Test")\n')
        self.write("iosApp/Other.kt", 'Text("Outside")\n')
        self.assertEqual(chk.scan(self.root), [])

    def test_update_writes_sorted_baseline(self):
        self.write(SCREEN, 'Text("B b")\nText("A a")\n')
        self.run_check("--update")
        lines = [x for x in self.baseline.read_text(encoding="utf-8").splitlines() if not x.startswith("#")]
        self.assertEqual(lines, [f"{SCREEN}\tA a", f"{SCREEN}\tB b"])

    def test_stale_baseline_line_is_a_note_not_a_failure(self):
        self.write(SCREEN, 'Text("Gone")\n')
        self.run_check("--update")
        self.write(SCREEN, "fun A() {}\n")
        code, out = self.run_check()
        self.assertEqual(code, 0)
        self.assertIn("1 baseline line", out)

    def test_string_catalog_new_and_stale_fail(self):
        catalog = {"strings": {
            "ok": {"localizations": {"en": {"stringUnit": {"state": "translated", "value": "x"}}}},
            "old": {"extractionState": "stale"},
            "fresh": {"localizations": {"de": {"stringUnit": {"state": "new", "value": "y"}}}},
        }}
        self.write("iosApp/Localizable.xcstrings", json.dumps(catalog))
        code, out = self.run_check()
        self.assertEqual(code, 1)
        self.assertIn("'old' is stale", out)
        self.assertIn("'fresh' (de) is in state new", out)

    def test_clean_string_catalog_passes(self):
        catalog = {"strings": {"ok": {"localizations": {"en": {"stringUnit": {"state": "translated"}}}}}}
        self.write("iosApp/Localizable.xcstrings", json.dumps(catalog))
        code, _ = self.run_check()
        self.assertEqual(code, 0)


if __name__ == "__main__":
    unittest.main()
