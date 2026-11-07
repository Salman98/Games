"""Generates launcher icons (all densities, adaptive + legacy), Play icon 512 and feature graphic 1024x500 per game.
Run from the repo root: python3 store/icons.py .   (needs Pillow)
"""
import os, sys
from PIL import Image, ImageDraw, ImageFont, ImageFilter

REPO = sys.argv[1]
LILITA = f"{REPO}/core/src/main/assets/fonts/LilitaOne-Regular.ttf"
DEJAVU = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
S = 2048  # master size (downscaled for anti-aliasing)


def hx(v):
    return tuple(int(v[i:i + 2], 16) for i in (0, 2, 4))


GEMS = [("FF8A8A", "D81E3A"), ("9CC4FF", "2257E0"), ("8FF0A0", "1FA64A"), ("FFF08A", "F0B400"), ("D9A8FF", "8A2AD8"),
        ("FFC27A", "EF7A00"), ("8BF0FF", "0AA3C2"), ("FFB0D8", "D8247A")]

GAMES = {
    "block-puzzle": dict(name="BLOCK GRID", tag="Drag. Drop. Blast lines.", bg=["2A1470", "170A45", "0B0526"], bloom="A06EFF", logo=["FFFFFF", "D8C8FF"]),
    "color-sort": dict(name="COLOR SORT", tag="Pour, match, relax.", bg=["0B5A63", "063A47", "021C27"], bloom="5AF0E6", logo=["FFFFFF", "A8F6FF"]),
    "crown-logic": dict(name="CROWN LOGIC", tag="One crown per row, column & colour.", bg=["1B2A6B", "111A48", "080D29"], bloom="FFD25A", logo=["FFF7CF", "FFD34D"]),
    "arrow-escape": dict(name="ARROW ESCAPE", tag="Clear the board, one arrow at a time.", bg=["3A6FE0", "2B49B8", "1B2A78"], bloom="B4DCFF", logo=["FFFFFF", "CFE2FF"]),
    "tile-mahjong": dict(name="TILE MAHJONG", tag="Match the free tiles. Clear the table.", bg=["0F6B47", "0A4A31", "04261A"], bloom="8CFFC8", logo=["FFF7D6", "FFD96A"]),
    "sudoku": dict(name="SUDOKU", tag="Calm logic. Daily focus.", bg=["5D7CFF", "3550E0", "2338B5"], bloom="FFFFFF", logo=["FFFFFF", "DDE4FF"]),
    "word-search": dict(name="WORD SEARCH", tag="Find every hidden word.", bg=["FF8B6B", "D9467A", "6B1F6E"], bloom="FFDCA0", logo=["FFFFFF", "FFD9E6"]),
    "solitaire": dict(name="SOLITAIRE", tag="The classic, beautifully dealt.", bg=["1F8A52", "0F5A35", "062D1A"], bloom="AAFFC8", logo=["FFF7CF", "FFD34D"]),
    "car-escape": dict(name="CAR ESCAPE", tag="Slide the cars. Free the red one.", bg=["1B1F3A", "11132A", "07081A"], bloom="FF4678", logo=["FFFFFF", "FFC2CF"]),
    "merge-2048": dict(name="2048", tag="Swipe. Merge. Go higher.", bg=["4A2A1A", "2E170C", "170A04"], bloom="FFAA50", logo=["FFF3C4", "FFC65A"]),
}


def vgrad(w, h, stops):
    img = Image.new("RGB", (w, h))
    px = img.load()
    cols = [hx(c) for c in stops]
    for y in range(h):
        t = y / max(1, h - 1) * (len(cols) - 1)
        i = min(int(t), len(cols) - 2)
        f = t - i
        c = tuple(int(cols[i][k] + (cols[i + 1][k] - cols[i][k]) * f) for k in range(3))
        for x in range(w):
            px[x, y] = c
    return img


def background(w, h, g):
    img = vgrad(w, h, g["bg"]).convert("RGBA")
    bloom = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(bloom)
    r = int(min(w, h) * 0.55)
    d.ellipse([w // 2 - r, int(h * 0.38) - r, w // 2 + r, int(h * 0.38) + r], fill=hx(g["bloom"]) + (110,))
    return Image.alpha_composite(img, bloom.filter(ImageFilter.GaussianBlur(min(w, h) * 0.18)))


def mix(a, t=0.55):
    return tuple(int(c + (255 - c) * t) for c in hx(a))


def gem(d, box, top, base, radius):
    x0, y0, x1, y1 = box
    h = y1 - y0
    d.rounded_rectangle(box, radius, fill=hx(base))
    d.rounded_rectangle([x0, y0, x1, y0 + h * 0.62], radius, fill=hx(top))
    d.rounded_rectangle([x0 + h * 0.12, y0 + h * 0.1, x1 - h * 0.12, y0 + h * 0.3], radius * 0.5, fill=mix(top))
    d.rounded_rectangle(box, radius, outline=(0, 0, 0, 60), width=max(2, int(h * 0.03)))


def shadow(img, box, radius, blur=40, alpha=110):
    sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(sh).rounded_rectangle([box[0], box[1] + blur // 2, box[2], box[3] + blur // 2], radius, fill=(0, 0, 0, alpha))
    return Image.alpha_composite(img, sh.filter(ImageFilter.GaussianBlur(blur)))


def text_c(d, xy, txt, font, fill, stroke=0, stroke_fill=None):
    d.text(xy, txt, font=font, fill=fill, anchor="mm", stroke_width=stroke, stroke_fill=stroke_fill)


# ---- art: each draws on a transparent S x S canvas, content roughly in the middle 80% ----

def art_block(img):
    d = ImageDraw.Draw(img)
    img2 = shadow(img, [300, 300, 1748, 1748], 160)
    d = ImageDraw.Draw(img2)
    d.rounded_rectangle([300, 300, 1748, 1748], 160, fill=(20, 10, 60, 235))
    layout = [[4, 4, 3], [1, 0, 3], [1, 2, 2]]
    cell = 420
    for r in range(3):
        for c in range(3):
            x, y = 364 + c * (cell + 30), 364 + r * (cell + 30)
            if r == 1 and c == 1:
                d.rounded_rectangle([x, y, x + cell, y + cell], 70, outline=(255, 255, 255, 70), width=10)
            else:
                top, base = GEMS[layout[r][c]]
                gem(d, [x, y, x + cell, y + cell], top, base, 80)
    return img2


def art_sort(img):
    d = ImageDraw.Draw(img)
    cols = [[0, 7, 0, 0], [7, 7, 6], [6, 6, 7, 6]]
    for i, balls in enumerate(cols):
        x = 300 + i * 520
        tube = [x, 420, x + 400, 1780]
        d.rounded_rectangle(tube, 200, fill=(255, 255, 255, 50), outline=(220, 250, 255, 230), width=26)
        d.rectangle([x, 420, x + 400, 600], fill=(0, 0, 0, 0))
        d.rounded_rectangle([x - 40, 400, x + 440, 450], 25, fill=(220, 250, 255, 240))
        for k, b in enumerate(balls):
            cy = 1580 - k * 300
            top, base = GEMS[b]
            d.ellipse([x + 50, cy - 150, x + 350, cy + 150], fill=hx(base))
            d.ellipse([x + 80, cy - 130, x + 300, cy + 60], fill=hx(top))
            d.ellipse([x + 120, cy - 110, x + 190, cy - 40], fill=mix(top, 0.75))
    # lifted ball above the middle tube
    top, base = GEMS[6]
    cx, cy = 300 + 520 + 200, 230
    d.ellipse([cx - 150, cy - 150, cx + 150, cy + 150], fill=hx(base))
    d.ellipse([cx - 120, cy - 130, cx + 100, cy + 60], fill=hx(top))
    d.ellipse([cx - 80, cy - 110, cx - 10, cy - 40], fill=mix(top, 0.75))
    return img


def crown(d, cx, cy, w):
    h = w * 0.72
    x0, y0 = cx - w / 2, cy - h / 2
    pts = [(x0, y0 + h * 0.85), (x0, y0 + h * 0.22), (x0 + w * 0.27, y0 + h * 0.52), (cx, y0), (x0 + w * 0.73, y0 + h * 0.52),
           (x0 + w, y0 + h * 0.22), (x0 + w, y0 + h * 0.85)]
    d.polygon(pts, fill=hx("F0A800"))
    inner = [(x + (cx - x) * 0.08, y + h * 0.06) for x, y in pts]
    d.polygon(inner, fill=hx("FFD34D"))
    d.rounded_rectangle([x0, y0 + h * 0.8, x0 + w, y0 + h], w * 0.04, fill=hx("E08A00"))
    d.rounded_rectangle([x0 + w * 0.03, y0 + h * 0.8, x0 + w * 0.97, y0 + h * 0.88], w * 0.03, fill=hx("FFE58A"))
    for px, py in [(x0, y0 + h * 0.22), (cx, y0), (x0 + w, y0 + h * 0.22)]:
        r = w * 0.07
        d.ellipse([px - r, py - r, px + r, py + r], fill=hx("FFF3B0"))
    r = w * 0.08
    d.ellipse([cx - r, y0 + h * 0.55 - r, cx + r, y0 + h * 0.55 + r], fill=hx("E53950"))


def art_crown(img):
    d = ImageDraw.Draw(img)
    regions = ["7E9CFF", "FF9DB5", "8FE3B0", "FFD27A"]
    n, cell, x0 = 4, 330, 364
    img = shadow(img, [x0 - 30, x0 - 30, x0 + n * cell + 30, x0 + n * cell + 30], 120)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([x0 - 30, x0 - 30, x0 + n * cell + 30, x0 + n * cell + 30], 120, fill=hx("FFE7A0"))
    for r in range(n):
        for c in range(n):
            col = regions[(r // 2) * 2 + (c // 2) if (r + c) % 3 else (r + 1) % 4]
            d.rectangle([x0 + c * cell, x0 + r * cell, x0 + (c + 1) * cell, x0 + (r + 1) * cell], fill=hx(col))
    for i in range(n + 1):
        d.line([x0 + i * cell, x0, x0 + i * cell, x0 + n * cell], fill=(60, 40, 0, 90), width=8)
        d.line([x0, x0 + i * cell, x0 + n * cell, x0 + i * cell], fill=(60, 40, 0, 90), width=8)
    crown(d, 1024, 1060, 1180)
    return img


def arrow(d, cx, cy, size, angle_dir, fill):
    s = size / 2
    shapes = {"up": [(0, -1), (0.8, -0.1), (0.3, -0.1), (0.3, 1), (-0.3, 1), (-0.3, -0.1), (-0.8, -0.1)]}
    pts = shapes["up"]
    rot = {"up": lambda x, y: (x, y), "right": lambda x, y: (-y, x), "left": lambda x, y: (y, -x), "down": lambda x, y: (-x, -y)}[angle_dir]
    d.polygon([(cx + rot(x, y)[0] * s, cy + rot(x, y)[1] * s) for x, y in pts], fill=fill)


def art_arrow(img):
    d = ImageDraw.Draw(img)
    dirs = [["right", "down", "left"], ["up", "up", "down"], ["right", "left", "up"]]
    cell = 430
    for r in range(3):
        for c in range(3):
            x, y = 344 + c * (cell + 25), 344 + r * (cell + 25)
            hot = r == 1 and c == 1
            d.rounded_rectangle([x, y, x + cell, y + cell], 90, fill=hx("FF7A2E") if hot else (255, 255, 255, 235))
            arrow(d, x + cell / 2, y + cell / 2, cell * 0.62, dirs[r][c], (255, 255, 255) if hot else hx("2B49B8"))
    return img


def art_mahjong(img):
    f = ImageFont.truetype(LILITA, 300)
    for i, (dx, dy) in enumerate([(-240, -170), (180, 130)]):
        box = [604 + dx, 384 + dy, 1444 + dx, 1664 + dy]
        img = shadow(img, box, 110)
        d = ImageDraw.Draw(img)
        d.rounded_rectangle([box[0], box[1] + 60, box[2], box[3] + 60], 110, fill=hx("1F8A52"))
        d.rounded_rectangle(box, 110, fill=hx("FFF8E6"))
        d.rounded_rectangle([box[0] + 40, box[1] + 40, box[2] - 40, box[3] - 40], 80, outline=hx("E9DDBF"), width=14)
        cx, cy = (box[0] + box[2]) / 2, (box[1] + box[3]) / 2
        if i == 0:
            for ox, oy, col in [(-170, -300, "E5383B"), (170, -300, "1F8A52"), (0, 0, "2257E0"), (-170, 300, "1F8A52"), (170, 300, "E5383B")]:
                d.ellipse([cx + ox - 120, cy + oy - 120, cx + ox + 120, cy + oy + 120], fill=hx(col))
                d.ellipse([cx + ox - 55, cy + oy - 55, cx + ox + 55, cy + oy + 55], fill=hx("FFF8E6"))
        else:
            d.ellipse([cx - 260, cy - 260, cx + 260, cy + 260], fill=hx("E5383B"))
            d.ellipse([cx - 150, cy - 150, cx + 150, cy + 150], fill=hx("FFF8E6"))
            d.ellipse([cx - 80, cy - 80, cx + 80, cy + 80], fill=hx("E5383B"))
    return img


def art_sudoku(img):
    box = [300, 300, 1748, 1748]
    img = shadow(img, box, 140)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(box, 140, fill=hx("FFFDF7"))
    cell = (box[2] - box[0] - 120) / 3
    x0 = box[0] + 60
    d.rounded_rectangle([x0 + cell, x0 + cell, x0 + 2 * cell, x0 + 2 * cell], 40, fill=hx("FFE3A6"))
    for i in (1, 2):
        d.line([x0 + i * cell, x0, x0 + i * cell, x0 + 3 * cell], fill=hx("C9CFEA"), width=18)
        d.line([x0, x0 + i * cell, x0 + 3 * cell, x0 + i * cell], fill=hx("C9CFEA"), width=18)
    f = ImageFont.truetype(LILITA, 330)
    nums = [["5", "", "3"], ["", "7", ""], ["1", "", "9"]]
    for r in range(3):
        for c in range(3):
            if nums[r][c]:
                text_c(d, (x0 + c * cell + cell / 2, x0 + r * cell + cell / 2 + 10), nums[r][c], f, hx("F29A1B") if (r, c) == (1, 1) else hx("2338B5"))
    return img


def art_word(img):
    box = [300, 300, 1748, 1748]
    img = shadow(img, box, 140)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle(box, 140, fill=hx("FFF5F8"))
    letters = ["WORD", "AXPL", "FINE", "YKQS"]
    cell = (box[2] - box[0] - 120) / 4
    x0 = box[0] + 60
    d.rounded_rectangle([x0 + 15, x0 + 20, x0 + 4 * cell - 15, x0 + cell - 20], cell / 2, fill=hx("2BD06A"))
    f = ImageFont.truetype(LILITA, 230)
    for r in range(4):
        for c in range(4):
            text_c(d, (x0 + c * cell + cell / 2, x0 + r * cell + cell / 2 + 8), letters[r][c], f, (255, 255, 255) if r == 0 else hx("6B1F6E"))
    return img


def card(img, box, rank, suit, red, angle):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    d.rounded_rectangle(box, 90, fill=(255, 255, 255))
    d.rounded_rectangle(box, 90, outline=hx("D8D8D8"), width=10)
    col = hx("D8203A") if red else hx("1B1B2E")
    fr = ImageFont.truetype(LILITA, 260)
    fs = ImageFont.truetype(DEJAVU, 560)
    fs_small = ImageFont.truetype(DEJAVU, 170)
    d.text((box[0] + 70, box[1] + 50), rank, font=fr, fill=col)
    d.text((box[0] + 80, box[1] + 320), suit, font=fs_small, fill=col)
    text_c(d, ((box[0] + box[2]) / 2 + 40, (box[1] + box[3]) / 2 + 120), suit, fs, col)
    layer = layer.rotate(angle, center=((box[0] + box[2]) / 2, box[3]), resample=Image.BICUBIC)
    sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
    sh.paste((0, 0, 0, 120), mask=layer.split()[3])
    img = Image.alpha_composite(img, sh.filter(ImageFilter.GaussianBlur(35)).transform(img.size, Image.AFFINE, (1, 0, 0, 0, 1, -30)))
    return Image.alpha_composite(img, layer)


def art_solitaire(img):
    img = card(img, [520, 330, 1360, 1650], "K", "♥", True, 14)
    img = card(img, [720, 400, 1560, 1720], "A", "♠", False, -10)
    return img


def art_car(img):
    d = ImageDraw.Draw(img)
    # road lanes
    for i in range(4):
        d.rounded_rectangle([200 + i * 420, 980, 420 + i * 420, 1060], 40, fill=(255, 255, 255, 70))
    body = [330, 640, 1640, 1400]
    img = shadow(img, body, 220, blur=50, alpha=140)
    d = ImageDraw.Draw(img)
    for wx in (520, 1380):  # wheels
        d.rounded_rectangle([wx - 150, 560, wx + 150, 700], 50, fill=hx("15151F"))
        d.rounded_rectangle([wx - 150, 1340, wx + 150, 1480], 50, fill=hx("15151F"))
    d.rounded_rectangle(body, 220, fill=hx("C4002F"))
    d.rounded_rectangle([body[0] + 30, body[1] + 30, body[2] - 30, body[3] - 120], 200, fill=hx("FF2D55"))
    d.rounded_rectangle([1080, 760, 1380, 1280], 100, fill=hx("9FE7FF"))  # windshield
    d.rounded_rectangle([560, 780, 820, 1260], 90, fill=hx("7FC8E8"))  # rear window
    d.rounded_rectangle([1560, 720, 1640, 860], 30, fill=hx("FFF3A0"))  # lights
    d.rounded_rectangle([1560, 1180, 1640, 1320], 30, fill=hx("FFF3A0"))
    arrow(d, 1860, 1020, 300, "right", (255, 255, 255, 230))
    return img


def art_2048(img):
    def tile(img, box, txt, size, top, base):
        img = shadow(img, box, 120)
        d = ImageDraw.Draw(img)
        gem(d, box, top, base, 120)
        text_c(d, ((box[0] + box[2]) / 2, (box[1] + box[3]) / 2 + 15), txt, ImageFont.truetype(LILITA, size), (255, 255, 255), 12, hx("8A3A00"))
        return img
    img = tile(img, [250, 250, 790, 790], "2", 360, "FFF3C4", "FFC65A")
    img = tile(img, [1258, 1258, 1798, 1798], "4", 360, "FFD9A0", "FF9A3A")
    img = tile(img, [390, 600, 1658, 1450], "2048", 420, "FFB36B", "D65200")
    return img


ART = {"block-puzzle": art_block, "color-sort": art_sort, "crown-logic": art_crown, "arrow-escape": art_arrow,
       "tile-mahjong": art_mahjong, "sudoku": art_sudoku, "word-search": art_word, "solitaire": art_solitaire,
       "car-escape": art_car, "merge-2048": art_2048}


def art(mod):
    return ART[mod](Image.new("RGBA", (S, S), (0, 0, 0, 0)))


def place(canvas_size, a, frac):
    out = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    n = int(canvas_size * frac)
    out.alpha_composite(a.resize((n, n), Image.LANCZOS), ((canvas_size - n) // 2, (canvas_size - n) // 2))
    return out


DENS = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}


def build(mod):
    g = GAMES[mod]
    a = art(mod)
    res = f"{REPO}/{mod}/src/main/res"
    bg_master = background(1024, 1024, g)
    for dn, k in DENS.items():
        os.makedirs(f"{res}/mipmap-{dn}", exist_ok=True)
        fg = int(108 * k)
        place(fg, a, 0.60).save(f"{res}/mipmap-{dn}/ic_launcher_foreground.png")  # adaptive safe zone is the middle 66dp
        bg_master.resize((fg, fg), Image.LANCZOS).convert("RGB").save(f"{res}/mipmap-{dn}/ic_launcher_background.png")
        lg = int(48 * k)
        legacy = bg_master.resize((lg, lg), Image.LANCZOS)
        legacy.alpha_composite(place(lg, a, 0.84))
        mask = Image.new("L", (lg * 4, lg * 4), 0)
        ImageDraw.Draw(mask).rounded_rectangle([0, 0, lg * 4 - 1, lg * 4 - 1], lg * 4 * 0.22, fill=255)
        legacy.putalpha(mask.resize((lg, lg), Image.LANCZOS))
        legacy.save(f"{res}/mipmap-{dn}/ic_launcher.png")
    os.makedirs(f"{res}/mipmap-anydpi-v26", exist_ok=True)
    open(f"{res}/mipmap-anydpi-v26/ic_launcher.xml", "w").write(
        '<?xml version="1.0" encoding="utf-8"?>\n<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@mipmap/ic_launcher_background" />\n'
        '    <foreground android:drawable="@mipmap/ic_launcher_foreground" />\n'
        '</adaptive-icon>\n')

    store = f"{REPO}/store/{mod}"
    os.makedirs(store, exist_ok=True)
    icon = background(512, 512, g)
    icon.alpha_composite(place(512, a, 0.84))
    icon.convert("RGB").save(f"{store}/icon-512.png")  # Play applies its own rounding

    W, H = 1024, 500
    fgx = background(W, H, g)
    fgx.alpha_composite(a.resize((430, 430), Image.LANCZOS), (40, 35))
    d = ImageDraw.Draw(fgx)
    words = g["name"].split(" ")
    size = 120 if max(len(w) for w in words) <= 6 else 96
    f = ImageFont.truetype(LILITA, size)
    ts = 34
    while ImageFont.truetype(LILITA, ts).getlength(g["tag"]) > W - 500 - 50:  # keep tagline inside the right half
        ts -= 1
    ft = ImageFont.truetype(LILITA, ts)
    cx = 500 + (W - 500) // 2
    y = H // 2 - (len(words) - 1) * size * 0.5 - 30
    for i, w in enumerate(words):
        d.text((cx, y + i * size * 0.95), w, font=f, anchor="mm", fill=hx(g["logo"][i % 2]), stroke_width=7, stroke_fill=(0, 0, 0, 150))
    d.text((cx, y + len(words) * size * 0.95 - size * 0.25), g["tag"], font=ft, anchor="mm", fill=(255, 255, 255), stroke_width=3, stroke_fill=(0, 0, 0, 120))
    fgx.convert("RGB").save(f"{store}/feature-graphic-1024x500.png")


if __name__ == "__main__":
    for m in (sys.argv[2:] or GAMES):
        build(m)
        print("ok", m)
