import re
import sys
import tempfile
import unittest
import xml.etree.ElementTree as ET
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))
import pseudo_locale  # noqa: E402

PLACEHOLDER = re.compile(r"%(?:\d+\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]|\{[^{}]*\}|\\.")

SOURCE_XML = """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- comment -->
    <string name="title">Next Launch</string>
    <string name="version">Version: %1$s (%2$d)</string>
    <string name="mixed">100%% sure, {name}\\n done\\'s</string>
    <string name="skip" translatable="false">Keep</string>
    <string name="markup">Hello <b>big</b> world</string>
    <plurals name="launches">
        <item quantity="one">%d launch</item>
        <item quantity="other">%d launches</item>
    </plurals>
</resources>
"""


class PseudoLocaleTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        base = Path(self.tmp.name)
        self.source = base / "values" / "strings.xml"
        self.source.parent.mkdir()
        self.source.write_text(SOURCE_XML, encoding="utf-8")

    def generate(self):
        out = pseudo_locale.default_out(self.source, "en-rXA")
        pseudo_locale.generate(self.source, out)
        return out, ET.parse(out).getroot()

    def test_output_folder(self):
        out, _ = self.generate()
        self.assertEqual("values-en-rXA", out.parent.name)
        self.assertTrue(out.read_text(encoding="utf-8").startswith("<?xml"))

    def test_vowels_accented_and_wrapped(self):
        _, root = self.generate()
        text = root.find("string[@name='title']").text
        self.assertTrue(text.startswith("[!! ") and text.endswith(" !!]"))
        self.assertNotRegex(text, r"[aeiouAEIOU]")

    def test_text_grows_by_40_percent(self):
        _, root = self.generate()
        inner = root.find("string[@name='title']").text[len("[!! "):-len(" !!]")]
        self.assertGreaterEqual(len(inner), int(len("Next Launch") * 1.4))

    def test_placeholders_kept_in_order(self):
        _, root = self.generate()
        for name in ("version", "mixed"):
            src = ET.fromstring(SOURCE_XML).find(f"string[@name='{name}']").text
            got = root.find(f"string[@name='{name}']").text
            self.assertEqual(PLACEHOLDER.findall(src), PLACEHOLDER.findall(got), name)

    def test_plural_items_converted_and_keep_placeholders(self):
        _, root = self.generate()
        items = root.findall("plurals[@name='launches']/item")
        self.assertEqual(["one", "other"], [i.get("quantity") for i in items])
        for item in items:
            self.assertIn("%d", item.text)
            self.assertTrue(item.text.startswith("[!! "))

    def test_non_translatable_dropped(self):
        _, root = self.generate()
        self.assertIsNone(root.find("string[@name='skip']"))

    def test_markup_tags_survive(self):
        _, root = self.generate()
        el = root.find("string[@name='markup']")
        self.assertEqual(["b"], [c.tag for c in el])
        self.assertTrue(el.text.startswith("[!! "))
        self.assertTrue(el[0].tail.endswith(" !!]"))

    def test_every_string_key_present(self):
        _, root = self.generate()
        names = {e.get("name") for e in root}
        self.assertEqual({"title", "version", "mixed", "markup", "launches"}, names)


if __name__ == "__main__":
    unittest.main()
