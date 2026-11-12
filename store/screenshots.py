"""Turns raw phone captures into Play Store screenshots (1080x1920) with a caption on the game's own background.
1. Capture on a phone/emulator:  adb exec-out screencap -p > store/<module>/raw/1.png   (then 2.png, 3.png, 4.png)
   Order: 1 = home screen, 2 = mid-game board, 3 = win screen with stars, 4 = daily/streak screen (themes screen for endless games).
2. Run from the repo root:  python3 store/screenshots.py .        (needs Pillow; add module names to do only some)
Output: store/<module>/screenshot-1.png ... ready to upload.
"""
import glob, os, sys
from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from icons import GAMES, LILITA, REPO, background  # same colours and font as the icons

W, H = 1080, 1920
LEVELS = ["{tag}", "Endless levels", "Win stars on every level", "A new daily puzzle. Keep your streak."]
ENDLESS = ["{tag}", "Simple one-finger play", "Relax and play offline", "Unlock new themes"]
ENDLESS_GAMES = {"block-puzzle", "solitaire", "merge-2048"}  # no numbered levels, no daily puzzle


def find_banner(shot):
    """Row where a banner ad starts, or None."""
    w, h = shot.size
    px = shot.convert("L").resize((40, h)).load()  # 40 samples per row is enough to spot the banner
    # The banner's top is the only place where both screen edges jump much brighter at once (also when a win dialog dims it; 4 of 6 samples, as confetti can cover some).
    # On a light theme (Sudoku) the jump is small, so pure white after off-white also counts.
    return next((y for y in range(int(h * 0.7), h) if sum(px[x, y] - px[x, y - 3] > 40 or (px[x, y] >= 252 > px[x, y - 3] + 3) for x in (0, 1, 2, 37, 38, 39)) >= 4), None)


def crop_bars(shot, banner):
    """Cuts the phone's status bar and navigation bar, and the banner ad above it (test ads must not be on Play)."""
    w, h = shot.size
    # shortcut: bars are cut by fixed shares (4% top, 5.5% bottom) measured on a 1080x2340 phone with 3-button navigation; adjust for other phones.
    return shot.crop((0, int(h * 0.04), w, banner or int(h * 0.945)))


def frame(mod, raw, caption, usual_banner=None):
    img = background(W, H, GAMES[mod])
    d = ImageDraw.Draw(img)
    size = 84
    while d.textlength(caption, font=ImageFont.truetype(LILITA, size)) > W - 120:  # shrink long captions to one line
        size -= 4
    d.text((W // 2, 170), caption, font=ImageFont.truetype(LILITA, size), anchor="mm", fill="white", stroke_width=5, stroke_fill=(0, 0, 0, 140))
    shot = Image.open(raw).convert("RGBA")
    # A dimmed banner on a light theme cannot be told apart, so game screens (captures 2 and 3) fall back to where other captures have it.
    shot = crop_bars(shot, find_banner(shot) or (usual_banner if os.path.basename(raw) in ("2.png", "3.png") else None))
    box_w, box_h = W - 200, H - 400
    scale = min(box_w / shot.width, box_h / shot.height)
    shot = shot.resize((int(shot.width * scale), int(shot.height * scale)), Image.LANCZOS)
    mask = Image.new("L", shot.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, shot.width - 1, shot.height - 1], 48, fill=255)
    x, y = (W - shot.width) // 2, 320
    d.rounded_rectangle([x - 10, y - 10, x + shot.width + 9, y + shot.height + 9], 58, fill=(255, 255, 255, 60))  # soft phone edge
    img.paste(shot, (x, y), mask)
    return img.convert("RGB")


if __name__ == "__main__":
    found = [y for y in (find_banner(Image.open(p)) for p in glob.glob(f"{REPO}/store/*/raw/2.png")) if y]
    usual_banner = max(set(found), key=found.count) if found else None  # same phone, so the banner sits at the same row
    for mod in (sys.argv[2:] or GAMES):
        raws = sorted(glob.glob(f"{REPO}/store/{mod}/raw/*.png"))
        if not raws:
            print("skip", mod, "(no captures in store/%s/raw/)" % mod)
        captions = ENDLESS if mod in ENDLESS_GAMES else LEVELS
        for i, raw in enumerate(raws):
            caption = captions[i % len(captions)].format(tag=GAMES[mod]["tag"])
            frame(mod, raw, caption, usual_banner).save(f"{REPO}/store/{mod}/screenshot-{i + 1}.png")
            print("ok", mod, i + 1, caption)
