"""Fail if two games in IDEAS.md share a Family (core rule). Run: python3 store/check_ideas.py"""
import re, sys
text = open(sys.argv[1] if len(sys.argv) > 1 else "IDEAS.md").read()
fams = []
for line in text.split("\n## Left out")[0].splitlines():
    cells = [c.strip() for c in line.strip().strip("|").split("|")]
    if len(cells) == 7 and re.fullmatch(r"\d+", cells[0]):
        fams.append((cells[0] + " " + cells[1], cells[4]))
    elif len(cells) == 2 and cells[0] not in ("Game", "---"):
        fams.append((cells[0], cells[1]))
seen = {}
for game, fam in fams:
    assert fam, f"{game}: empty family"
    assert fam.lower() not in seen, f"duplicate family '{fam}': {seen[fam.lower()]} and {game}"
    seen[fam.lower()] = game
nums = sorted(int(g.split()[0]) for g, _ in fams if g.split()[0].isdigit())
assert nums == sorted(set(nums)), "duplicate game number"
print(f"OK: {len(fams)} games, all families unique")
