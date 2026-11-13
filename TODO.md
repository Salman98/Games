# Launch checklist (owner tasks)

Tick boxes as you go (GitHub shows them as checkboxes). Do the sections in order.

## 0. Copy-paste reference

| # | Play Store name | Module (build) | Package / applicationId |
|---|---|---|---|
| 1 | Block Grid Puzzle | `block-puzzle` | `com.techeliteclub.game.blockpuzzle` |
| 2 | Color Sort Puzzle | `color-sort` | `com.techeliteclub.game.colorsort` |
| 3 | Crown Logic | `crown-logic` | `com.techeliteclub.game.crownlogic` |
| 4 | Arrow Escape | `arrow-escape` | `com.techeliteclub.game.arrowescape` |
| 5 | Tile Mahjong | `tile-mahjong` | `com.techeliteclub.game.tilemahjong` |
| 6 | Sudoku Classic | `sudoku` | `com.techeliteclub.game.sudoku` |
| 7 | Word Search | `word-search` | `com.techeliteclub.game.wordsearch` |
| 8 | Solitaire Klondike | `solitaire` | `com.techeliteclub.game.solitaire` |
| 9 | Car Escape | `car-escape` | `com.techeliteclub.game.carescape` |
| 10 | 2048 Merge | `merge-2048` | `com.techeliteclub.game.merge2048` |
| 11 | Mini Sudoku 6x6 | `mini-sudoku` | `com.techeliteclub.game.minisudoku` |
| 12 | Sun & Moon Logic | `sun-moon` | `com.techeliteclub.game.sunmoon` |
| 13 | Number Path | `number-path` | `com.techeliteclub.game.numberpath` |

Package names can never change after the first upload.

Build one game's release bundle: `./gradlew :block-puzzle:bundleRelease` (swap the module name).

## 1. Website (do first, Play and AdMob need the URLs)

- [ ] Upload `website/games/privacy.html` to your site, e.g. `https://YOUR-SITE/games/privacy.html`
- [ ] Open the page on your phone and check it loads over **https**
- [ ] After AdMob step 3: upload `website/app-ads.txt` to the **root** of the domain: `https://YOUR-SITE/app-ads.txt` (not inside /games)

## 2. Play Console, once per game (×10)

- [ ] Create app: name from the table, Game, Free, English default
- [ ] Store settings → **Website** = your site's domain (must be the same domain as app-ads.txt), contact email
- [ ] App content → Privacy policy = your privacy.html URL
- [ ] App content → Ads = **Yes, contains ads**
- [ ] App content → Target audience = **13–15, 16–17, 18+** (no under-13 boxes); "Appeals to children" = No
- [ ] App content → Content rating questionnaire (puzzle, no violence/gambling) → expect Everyone / PEGI 3
- [ ] App content → Data safety: Play Console → your app → **Policy → App content** → **Data safety** → fill with the answers below (once per app)
- [ ] App content → Government app No, Financial features No, Health No, News No
- [ ] App content → Advertising ID: **Yes, used for advertising** (AdMob)
- [ ] Store listing: name, short and full description from `store/LISTING.md`; icon 512×512 and feature graphic 1024×500 from `store/<module>/`; 4 phone screenshots made with `store/screenshots.py`
- [ ] Category: Puzzle (Solitaire: Card, Word Search: Word)
- [ ] Bump `versionCode` in `<module>/build.gradle.kts` before every new upload
- [ ] Upload first AAB to **Internal testing**, install from Play on your phone, then promote to **Production**

Data safety answers (current code):
- Collected by the AdMob SDK (per Google's AdMob data-disclosure guide): **Approximate location** (from IP), **Device or other IDs** (advertising ID), **App interactions**, **Crash logs**, **Diagnostics**. Purpose: Advertising, Analytics, Fraud prevention/security. Shared with Google (AdMob). Re-check the guide when you fill the form.
- Encrypted in transit: Yes. No account exists; progress stays on the device.
- Not collected: name, email, precise location, contacts, photos, payments.
- No accounts in the games, so the account-deletion question is "No, users cannot create an account".
- Firebase Analytics + Crashlytics: same types as above (app interactions, crash logs, diagnostics, approximate location, **Device or other IDs** = Firebase instance ID). Purpose: Analytics, App functionality. Not shared.

## 3. AdMob

- [x] Create AdMob account (same Google account as Play), add payment + tax info (Malaysia: W-8BEN)
- [x] Add 10 apps (Android, "not published yet"); link each to Play after it goes live
- [x] Per app create 3 ad units: **Banner**, **Interstitial**, **Rewarded** (rewarded: 1 reward, item "hint")
- [x] **G-rated ads only** (backup; the code already asks for G):
  - AdMob → **Blocking controls** → **All apps** → **Max ad content rating** → choose **G** → Save
  - Same page → **Sensitive categories** → block gambling, dating, alcohol, politics, religion
- [ ] **Consent message** (the "Allow personalised ads?" popup for EU/UK players; the app shows it only after you publish it here):
  - AdMob → **Privacy & messaging** → **European regulations** → **Create message**
  - Select all 10 apps, keep the default text, add your privacy page URL → **Publish**
  - Repeat under **US state regulations** → Create message → all 10 apps → **Publish**
  - Menu names may differ slightly if Google renames them; look for the closest match
- [x] Settings → copy your **publisher ID** (`pub-XXXXXXXXXXXXXXXX`) into `website/app-ads.txt`, then upload it (section 1)
- [x] Never tap your own live ads; use test ads while developing
- [x] Fill this table and send it to me (done, ids are in each `<module>/src/main/res/values/ads.xml`) (I put each row into `<module>/src/main/res/values/ads.xml`):

| Module | App ID (`~`) | Banner (`/`) | Interstitial (`/`) | Rewarded (`/`) |
|---|---|---|---|---|
| block-puzzle | | | | |
| color-sort | | | | |
| crown-logic | | | | |
| arrow-escape | | | | |
| tile-mahjong | | | | |
| sudoku | | | | |
| word-search | | | | |
| solitaire | | | | |
| car-escape | | | | |
| merge-2048 | | | | |

Or do it yourself: copy `core/src/main/res/values/ads.xml` to `<module>/src/main/res/values/ads.xml` and replace the 4 test IDs.

## 4. App signing

- [ ] Create one upload key (keep the file + passwords in 2 safe places, losing it blocks updates):
      `keytool -genkeypair -v -keystore techeliteclub-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000`
- [ ] Put `keystore.properties` in the repo root (git ignores it), then `./gradlew :<module>:bundleRelease` gives a signed AAB in `<module>/build/outputs/bundle/release/`
- [ ] Accept **Play App Signing** on the first upload of each app
- [ ] Never commit the `.jks` file to git

## 5. Optional, later (tell me when done, I wire the code)

- [x] **Firebase** project `techeliteclub-games`, 10 apps, `google-services.json` in each game folder (no database or sign-in: not needed)
- [x] Analytics + Crashlytics added (data shows in Firebase console → Analytics / Crashlytics after the first runs)
- [ ] Re-upload `privacy.html` to your site (now covers Analytics + Crashlytics)

## 6. Before pressing Publish (per game)

- [ ] Release build (minified) runs on a real phone: start, play, win, hint (free + ad), settings, daily, themes
- [ ] Consent form appears when using a VPN set to an EU country
- [ ] Notification permission asked on second launch; reminder arrives
- [ ] Real ad IDs in that game's `ads.xml` (not the `3940256099942544` test IDs)
- [x] Font: `core/src/main/assets/fonts/LilitaOne-Regular.ttf` present (see the README in that folder)
