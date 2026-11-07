# Games

10 Android puzzle games in one repo, sharing **one `gradlew`** and one `core` module.
Kotlin + Jetpack Compose, AdMob ads (banner, rewarded, interstitial).

| Module | Game | Rewarded ad gives |
|---|---|---|
| `block-puzzle` | Block Grid Puzzle (8×8, clear rows/columns) | New set of pieces |
| `color-sort` | Color Sort (pour balls between tubes) | Extra empty tube |
| `crown-logic` | Crown Logic (one crown per row/column/colour) | Places a correct crown |
| `arrow-escape` | Arrow Escape (tap arrows off the board) | Shows a free arrow / +1 life |
| `tile-mahjong` | Tile Mahjong (2 layers, match free pairs) | Shows a pair / shuffle |
| `sudoku` | Sudoku (unique answer) | Fills a cell |
| `word-search` | Word Search | Shows a word's first letter |
| `solitaire` | Solitaire Klondike (draw 1) | Shows a move |
| `car-escape` | Car Escape (slide cars, free the red car) | Plays the next best move |
| `merge-2048` | 2048 Merge | Undo |

## Build

Open the folder in Android Studio, or from a terminal:

```bash
./gradlew :block-puzzle:installDebug      # install one game on a phone/emulator
./gradlew installDebug                    # install all 10 games
./gradlew installDebug --continue         # all games; keep going if one fails, list failures at the end
./gradlew :sudoku:installDebug :solitaire:installDebug   # only the games you name
./gradlew :block-puzzle:bundleRelease     # Play Store bundle (.aab) for one game
./gradlew bundleRelease                   # Play Store bundles for all games
./gradlew test                            # game-logic unit tests for all games
```

On Windows PowerShell use `.\gradlew` instead of `./gradlew`.

Open a game on the phone right after installing it:

```bash
adb shell monkey -p com.techeliteclub.game.blockpuzzle -c android.intent.category.LAUNCHER 1
```

Uninstall: `adb uninstall com.techeliteclub.game.blockpuzzle`. Module names and package names:

| Module | Package |
|---|---|
| `block-puzzle` | `com.techeliteclub.game.blockpuzzle` |
| `color-sort` | `com.techeliteclub.game.colorsort` |
| `crown-logic` | `com.techeliteclub.game.crownlogic` |
| `arrow-escape` | `com.techeliteclub.game.arrowescape` |
| `tile-mahjong` | `com.techeliteclub.game.tilemahjong` |
| `sudoku` | `com.techeliteclub.game.sudoku` |
| `word-search` | `com.techeliteclub.game.wordsearch` |
| `solitaire` | `com.techeliteclub.game.solitaire` |
| `car-escape` | `com.techeliteclub.game.carescape` |
| `merge-2048` | `com.techeliteclub.game.merge2048` |

## Layout

```
gradlew, settings.gradle.kts      one Gradle setup for everything
build.gradle.kts                  shared Android settings (SDK 36, minSdk 24, release shrinking)
gradle/libs.versions.toml         all library versions in one place
core/                             ads (consent + G-rated ads), shared screen, save data, test ad ids
<game>/                           Logic.kt (rules, tested) + <Name>Game.kt (screen)
```

Add a new game: copy a game folder, rename its package and `applicationId`, add one line in `settings.gradle.kts`.

## Look & sound

- Every game has a home screen, its own colour world (`core/.../Theme.kt` → `Themes`), glass buttons, a framed board and an animated win screen. Design mockups: the "Puzzle Games – Screen Mockups" canvas.
- **Logo font:** download [Lilita One](https://fonts.google.com/specimen/Lilita+One) (free, OFL) and save it as `core/src/main/assets/fonts/LilitaOne-Regular.ttf`. Without it a heavy system font is used.
- **Sounds:** `core/src/main/res/raw/` (`sfx_tap`, `sfx_place`, `sfx_clear`, `sfx_win`, `sfx_error`, `music_loop`). They are simple synthesised placeholders; replace a file with the same name to change a sound.
- Players can switch sound, music and vibration off in Settings (gear icon).
- Hints: the first hint in each level is free, after that it plays a rewarded ad (`GameScaffold` in `core/.../GameUi.kt`).

## Ads

- Ads are limited to **G rating** (`core/.../Ads.kt`), because children may play even though the apps target 13+.
- The Google consent form (UMP) shows automatically to EU/UK users; a "Privacy" button appears when required.
- Interstitials: at most every 3rd finished level and 60s apart. Rewarded ads are always the player's choice.
- `core/src/main/res/values/ads.xml` holds Google's **test** ids. For each game you publish, copy that file to
  `<game>/src/main/res/values/ads.xml` and put the game's real AdMob app id and ad unit ids in it.

## Firebase (Google Sign-In + cloud save)

Project `techeliteclub-games`. Players can sign in with Google in **Settings**; their progress is saved to
Firestore at `users/{uid}/games/{packageName}` (one account across all games) and restored on a new phone.

One-time setup, on your machine: `bash scripts/firebase-setup.sh`. It registers the 7 games as Android apps,
adds your debug SHA-1, enables Google Sign-In, creates the Firestore database (asks for its location), deploys
`firestore.rules`, and writes each game's `google-services.json`. A game without that file still builds; the
sign-in row just stays hidden (block-puzzle, color-sort and crown-logic for now).

Before release, add the **release** and **Play App Signing** SHA-1s (Play Console → Test and release → App integrity),
or Google Sign-In fails for Play installs:
`npx -y firebase-tools@latest apps:android:sha:create <APP_ID> <SHA1> --project techeliteclub-games`

Play also needs an account-deletion link in the Data safety form (in-app: Settings → Delete account).

## Before publishing a game

1. Check `applicationId` in `<game>/build.gradle.kts` (`com.techeliteclub.game.<game>`; cannot change after first upload).
2. Real AdMob ids (above) and an `app-ads.txt` on your developer website.
3. Replace the placeholder icon (`core/src/main/res/drawable/ic_launcher_fg.xml`) and colour (`<game>/src/main/res/values/game.xml`).
4. Release signing key (Android Studio → Build → Generate Signed Bundle).
5. Play Console: privacy policy URL, Data safety form (AdMob collects device ids), target audience 13+, content rating.
