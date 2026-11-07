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
./gradlew :block-puzzle:installDebug      # run one game on a phone/emulator
./gradlew :block-puzzle:bundleRelease     # Play Store bundle (.aab)
./gradlew test                            # game-logic unit tests for all games
```

## Layout

```
gradlew, settings.gradle.kts      one Gradle setup for everything
build.gradle.kts                  shared Android settings (SDK 36, minSdk 24, release shrinking)
gradle/libs.versions.toml         all library versions in one place
core/                             ads (consent + G-rated ads), shared screen, save data, test ad ids
<game>/                           Logic.kt (rules, tested) + <Name>Game.kt (screen)
```

Add a new game: copy a game folder, rename its package and `applicationId`, add one line in `settings.gradle.kts`.

## Ads

- Ads are limited to **G rating** (`core/.../Ads.kt`), because children may play even though the apps target 13+.
- The Google consent form (UMP) shows automatically to EU/UK users; a "Privacy" button appears when required.
- Interstitials: at most every 3rd finished level and 60s apart. Rewarded ads are always the player's choice.
- `core/src/main/res/values/ads.xml` holds Google's **test** ids. For each game you publish, copy that file to
  `<game>/src/main/res/values/ads.xml` and put the game's real AdMob app id and ad unit ids in it.

## Before publishing a game

1. Check `applicationId` in `<game>/build.gradle.kts` (`com.techeliteclub.game.<game>`; cannot change after first upload).
2. Real AdMob ids (above) and an `app-ads.txt` on your developer website.
3. Replace the placeholder icon (`core/src/main/res/drawable/ic_launcher_fg.xml`) and colour (`<game>/src/main/res/values/game.xml`).
4. Release signing key (Android Studio → Build → Generate Signed Bundle).
5. Play Console: privacy policy URL, Data safety form (AdMob collects device ids), target audience 13+, content rating.
