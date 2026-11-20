"""Tests for scripts/i18n/sync_shared_keys.py. Run: python3 -m unittest discover scripts/i18n/tests"""
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "sync_shared_keys.py"
spec = importlib.util.spec_from_file_location("sync_shared_keys", SCRIPT)
sync_mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(sync_mod)

QUOTE_LINE = '    <string name="shared_quote">Caleb\'s &amp; \\"co\\"\\nline</string>\n'
MASTER = (
    '<?xml version="1.0" encoding="utf-8"?>\n'
    "<resources>\n"
    '    <string name="common_back">Back</string>\n'
    "    <!-- Empty list text. One line. -->\n"
    '    <string name="shared_no_upcoming_launches">No upcoming launches</string>\n'
    '    <string name="shared_last_updated">Last updated %1$s by %2$s (%3$d)</string>\n'
    + QUOTE_LINE +
    "</resources>\n"
)
WEAR = (
    '<?xml version="1.0" encoding="utf-8"?>\n'
    "<resources>\n"
    '    <string name="app_name">Space Launch Now</string>\n'
    "</resources>\n"
)


class SyncTest(unittest.TestCase):
    def setUp(self):
        tmp = tempfile.TemporaryDirectory()
        self.addCleanup(tmp.cleanup)
        d = Path(tmp.name)
        self.master, self.wear, self.catalog = d / "m.xml", d / "w.xml", d / "L.xcstrings"
        self.master.write_text(MASTER, encoding="utf-8")
        self.wear.write_text(WEAR, encoding="utf-8")

    def run_sync(self, check=False):
        return sync_mod.sync(self.master, self.wear, self.catalog, check=check)

    def wear_text(self):
        return self.wear.read_text(encoding="utf-8")

    def strings(self):
        return json.loads(self.catalog.read_text(encoding="utf-8"))["strings"]

    def test_key_reaches_wear_with_same_text(self):
        self.run_sync()
        wear = self.wear_text()
        self.assertIn('<string name="shared_no_upcoming_launches">No upcoming launches</string>', wear)
        self.assertIn("<!-- Empty list text. One line. -->", wear)
        self.assertIn('<string name="app_name">Space Launch Now</string>', wear)
        self.assertNotIn("common_back", wear)

    def test_key_reaches_catalog_with_same_text(self):
        self.run_sync()
        item = self.strings()["shared_no_upcoming_launches"]
        self.assertEqual(item["localizations"]["en"]["stringUnit"]["value"], "No upcoming launches")
        self.assertEqual(item["comment"], "Empty list text. One line.")
        self.assertNotIn("common_back", self.strings())

    def test_placeholders_kept(self):
        self.run_sync()
        self.assertIn("Last updated %1$s by %2$s (%3$d)", self.wear_text())
        value = self.strings()["shared_last_updated"]["localizations"]["en"]["stringUnit"]["value"]
        self.assertEqual(value, "Last updated %1$@ by %2$@ (%3$d)")

    def test_escapes(self):
        self.run_sync()
        self.assertIn('Caleb\\\'s &amp; \\"co\\"\\nline', self.wear_text())
        value = self.strings()["shared_quote"]["localizations"]["en"]["stringUnit"]["value"]
        self.assertEqual(value, 'Caleb\'s & "co"\nline')

    def test_second_run_changes_nothing(self):
        self.assertEqual(len(self.run_sync()), 2)
        before = (self.wear.read_bytes(), self.catalog.read_bytes())
        self.assertEqual(self.run_sync(), [])
        self.assertEqual((self.wear.read_bytes(), self.catalog.read_bytes()), before)

    def test_check_writes_nothing_and_reports_drift(self):
        self.assertEqual(len(self.run_sync(check=True)), 2)
        self.assertEqual(self.wear_text(), WEAR)
        self.assertFalse(self.catalog.exists())
        self.run_sync()
        self.assertEqual(self.run_sync(check=True), [])

    def test_check_catches_edited_wear_text(self):
        self.run_sync()
        self.wear.write_text(self.wear_text().replace("No upcoming launches", "No launches"), encoding="utf-8")
        self.assertEqual(self.run_sync(check=True), [self.wear])

    def test_master_change_and_removal_propagate(self):
        self.run_sync()
        self.master.write_text(
            MASTER.replace("No upcoming launches", "Nothing planned").replace(QUOTE_LINE, ""),
            encoding="utf-8")
        self.run_sync()
        self.assertIn("Nothing planned", self.wear_text())
        self.assertNotIn("shared_quote", self.wear_text())
        self.assertNotIn("shared_quote", self.strings())

    def test_catalog_keeps_other_keys_and_languages(self):
        self.catalog.write_text(json.dumps({
            "sourceLanguage": "en", "version": "1.0",
            "strings": {
                "widget_title": {"localizations": {"en": {"stringUnit": {"state": "translated", "value": "Next"}}}},
                "shared_no_upcoming_launches": {"localizations": {
                    "en": {"stringUnit": {"state": "translated", "value": "old"}},
                    "pl": {"stringUnit": {"state": "translated", "value": "Brak"}}}},
            }}), encoding="utf-8")
        self.run_sync()
        strings = self.strings()
        self.assertIn("widget_title", strings)
        locs = strings["shared_no_upcoming_launches"]["localizations"]
        self.assertEqual(locs["en"]["stringUnit"]["value"], "No upcoming launches")
        self.assertEqual(locs["pl"]["stringUnit"]["value"], "Brak")

    def test_shared_plural_is_rejected(self):
        plural = '<plurals name="shared_n"><item quantity="one">x</item></plurals>'
        self.master.write_text(MASTER.replace("</resources>", plural + "</resources>"), encoding="utf-8")
        with self.assertRaises(SystemExit):
            self.run_sync()

    def test_real_files_are_in_step(self):
        """The repo files must match their master (the CI check)."""
        self.assertEqual(sync_mod.sync(check=True), [])


if __name__ == "__main__":
    unittest.main()
