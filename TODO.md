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
- [ ] App content → Data safety (answers below)
- [ ] App content → Government app No, Financial features No, Health No, News No
- [ ] App content → Advertising ID: **Yes, used for advertising** (AdMob)
- [ ] Store listing: short description (≤80 chars), full description, icon 512×512 PNG, feature graphic 1024×500, at least 4 phone screenshots
- [ ] Category: Puzzle (Solitaire: Card, Word Search: Word)
- [ ] Bump `versionCode` in `<module>/build.gradle.kts` before every new upload
- [ ] Upload first AAB to **Internal testing**, install from Play on your phone, then promote to **Production**

Data safety answers (current code):
- Collected by the AdMob SDK (per Google's AdMob data-disclosure guide): **Approximate location** (from IP), **Device or other IDs** (advertising ID), **App interactions**, **Crash logs**, **Diagnostics**. Purpose: Advertising, Analytics, Fraud prevention/security. Shared with Google (AdMob). Re-check the guide when you fill the form.
- Encrypted in transit: Yes. Users can request deletion: No account exists; progress is only on the device (uninstall deletes it).
- Not collected: name, email, location (precise), contacts, photos, payments.
- If you add Firebase later (section 5), add Analytics + Crash logs here.

## 3. AdMob

- [ ] Create AdMob account (same Google account as Play), add payment + tax info (Malaysia: W-8BEN)
- [ ] Add 10 apps (Android, "not published yet"); link each to Play after it goes live
- [ ] Per app create 3 ad units: **Banner**, **Interstitial**, **Rewarded** (rewarded: 1 reward, item "hint")
- [ ] Blocking controls → **Max ad content rating: G**; block sensitive categories (gambling, dating, alcohol, politics, religion)
- [ ] Privacy & messaging → create & **publish** the GDPR consent message and the US states message for all 10 apps (the app already shows them via UMP)
- [ ] Settings → copy your **publisher ID** (`pub-XXXXXXXXXXXXXXXX`) into `website/app-ads.txt`, then upload it (section 1)
- [ ] Never tap your own live ads; use test ads while developing
- [ ] Fill this table and send it to me (I put each row into `<module>/src/main/res/values/ads.xml`):

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
- [ ] Build signed AABs in Android Studio: Build → Generate Signed App Bundle (or ask me to add a `keystore.properties` signing config)
- [ ] Accept **Play App Signing** on the first upload of each app
- [ ] Never commit the `.jks` file to git

## 5. Optional, later (tell me when done, I wire the code)

- [ ] **Firebase** (Analytics + Crashlytics): create project "TechEliteClub Games", add 10 Android apps with the package names above, download each `google-services.json` into `<module>/` → tell me
- [ ] **Play Games Services** (leaderboards / sign-in): in Play Console → Play Games Services → create per game, add leaderboards (Block Grid: Best score, 2048: Best score, others: Stars), send me the game IDs + leaderboard IDs
- [ ] Update `privacy.html` when Firebase or Play Games is added

## 6. Before pressing Publish (per game)

- [ ] Release build (minified) runs on a real phone: start, play, win, hint (free + ad), settings, daily, themes
- [ ] Consent form appears when using a VPN set to an EU country
- [ ] Notification permission asked on second launch; reminder arrives
- [ ] Real ad IDs in that game's `ads.xml` (not the `3940256099942544` test IDs)
- [ ] Font: `core/src/main/assets/fonts/LilitaOne-Regular.ttf` present (see the README in that folder)
