import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import crowdin_assign_tm as m  # noqa: E402


class MergedWithTest(unittest.TestCase):
    def test_adds_after_existing(self):
        out = m.merged_with({"111": {"priority": 1}}, 406219)
        self.assertEqual(out, {"111": {"priority": 1}, "406219": {"priority": 2}})

    def test_empty_gets_priority_one(self):
        self.assertEqual(m.merged_with({}, 5), {"5": {"priority": 1}})

    def test_present_is_unchanged(self):
        cur = {"406219": {"priority": 3}}
        self.assertEqual(m.merged_with(cur, 406219), cur)


if __name__ == "__main__":
    unittest.main()
