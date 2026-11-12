# Play Store publish checklist (all Tech Elite Club games)

Answers used for Crown Logic on 12 Nov 2026. They are the same for every game **as long as the build has only AdMob + Firebase (Crashlytics/Analytics), no login, no in-app purchases**. If a game adds anything else, redo Data safety first.

## What changes per game

| Item | Where |
|---|---|
| Store name (max 30 chars, e.g. `Crown Logic: Queens Puzzle`) | Main store listing |
| Short description (80 chars), full description | Main store listing |
| Icon 512×512, feature graphic 1024×500, 2+ phone screenshots | Main store listing |
| AI label: tick only assets made or edited with an AI tool | Main store listing upload |
| Tags (closest to the game) | Store settings |
| AdMob app ID + ad unit IDs (real ones, not test) | Build |
| Add the game to the table in `privacy.html` if missing | techeliteclub.com |

Launcher name under the icon can stay short (`Crown Logic`); it does not need to match the store name. Package names never change.

## Same for every game

### Store settings
- App or game: **Game**. Category: **Puzzle** (change only if the game is not a puzzle).
- Contact email: support@techeliteclub.com

### App content
| Declaration | Answer |
|---|---|
| Privacy policy | URL of `privacy.html` on techeliteclub.com |
| App access | All functionality available without restrictions |
| Ads | Yes, contains ads |
| Content rating | Fill the IARC questionnaire truthfully (Crown Logic came out Everyone / PEGI 3) |
| Target audience | **13-15, 16-17, 18 and over** only. Never tick under 13 (triggers Families policy) |
| Appeals to children? | Answer truthfully; "Yes" only adds a "Not designed for children" label |
| Advertising ID | **Yes**; purposes: Advertising or marketing, Analytics |
| Health apps | My app does not have any health features |
| News, financial features, government app | No |

### Data safety
- Collects or shares required data types: **Yes**
- Encrypted in transit: **Yes**
- Account creation: **My app does not allow users to create an account**
- Login with outside accounts: **No**
- Data deletion request: **No** (do not pick "deleted within 90 days")
- Data types to tick: Approximate location, App interactions, Crash logs, Diagnostics, Device or other IDs

For every ticked type: **not ephemeral**, **collection is required**.

| Data type | Collected | Shared | Purposes (same for collected and shared) |
|---|---|---|---|
| Approximate location | Yes | Yes | Analytics, Advertising or marketing, Fraud prevention |
| App interactions | Yes | Yes | Analytics, Advertising or marketing |
| Crash logs | Yes | No | Analytics |
| Diagnostics | Yes | Yes | Analytics, Advertising or marketing |
| Device or other IDs | Yes | Yes | Analytics, Advertising or marketing, Fraud prevention |

Shortcut: use **Export to CSV** on Crown Logic's Data safety page and **Import from CSV** on the next game. Export again first: the copy exported at 18:49 on 12 Nov 2026 had the App interactions row wrong (it must be ephemeral = false, control = REQUIRED); it has since been corrected in Play Console.

### Build
- `targetSdk` 36 or higher (Play requirement since 31 Aug 2026; rises every August).
- `./gradlew :<game>:bundleRelease`, upload the `.aab`.
- Accept Play App Signing. Back up the upload keystore and passwords.
- "No debug symbols" warning is harmless. To remove it for all games at once, edit **`build.gradle.kts` in the repo root** (github.com/Salman98/Games), inside `buildTypes.getByName("release") { ... }`, and add:
  `ndk { debugSymbolLevel = "SYMBOL_TABLE" }`
- Build must contain the consent form (Google UMP) and a **Settings → Privacy options** entry, because the privacy policy promises it and Europe needs it for ads.
- Max ad content rating **G** in AdMob (app settings → Blocking controls).

### Release
1. Internal testing first: install from Play, check ads and a test crash in Crashlytics.
2. Production → Countries/regions → add all. Leave out EEA, UK and Switzerland only if the build has no consent form yet.
3. Send for review. The 2018 account is exempt from the 12-tester closed test rule.

### After it is live
- Link the app to its Play listing in AdMob.
- `app-ads.txt` on techeliteclub.com (once, covers all games).

## Consent form for EEA, UK and Switzerland (AdMob)

Done in AdMob, not in Play Console. No app release needed as long as the build has the UMP SDK.

1. AdMob → **Privacy & messaging** → **European regulations** → **Create** (or **Manage → Create message** if one exists).
2. **Select apps** → tick the game → Confirm. Add the `privacy.html` URL for the app when asked (required).
3. Languages: keep English as default; add others if wanted.
4. User choices: Consent On, Manage options On, **Do not consent: On for every country in the list** (including Everywhere else), Close: Off.
5. Targeting: **Countries subject to GDPR (EEA, UK and Switzerland)**, not Everywhere.
6. **Publish** (not Save draft).
7. **Settings** on the European regulations card: keep "commonly used ad partners".
8. Test it on a phone: see "Testing the consent form" below.

For the next games: open the same message and add the new app under Select apps instead of creating a new message.

## Opt-out form for US states (AdMob)

1. AdMob → **Privacy & messaging** → **US state regulations** → Create message.
2. **Select apps** → tick the game. Language English (en-US). **Opt out: On**.
3. Targeting and Entry point ("Do not sell or share my data"): keep defaults.
4. **Publish**. For the next games, add the app to this same message.

## Testing the consent form

No code change needed. The debug build uses the game's real AdMob app ID (only the ad units are test units), so it fetches the same form as the release.

1. Both AdMob messages must show **Published**; allow about 30 minutes after publishing.
2. Install and open the debug build from the repo root:
   `.\gradlew :crown-logic:installDebug`
   `adb shell monkey -p com.techeliteclub.game.crownlogic 1`
3. Turn on a VPN with a server in the EEA, UK or Switzerland. Canada, Mexico and other countries are not covered, although a free VPN's server may still be located in Europe whatever its label says.
4. Wipe the game's data so it forgets earlier consent: phone Settings → Apps → the game → Storage → Clear data, or `adb uninstall <package>` and install again. (`adb shell pm clear` is blocked on some phones.)
5. Open the game: the form appears with Consent, Do not consent and Manage options. In the game's Settings, **Privacy options** reopens it.
6. VPN off, wipe data, open again: no form, ads load.

Result for Crown Logic on 12 Nov 2026: form shown through the VPN, no form from Malaysia with the VPN off.

## Verified and not verified

Verified on 12 Nov 2026:
- Repo: `core` ships AdMob, UMP (consent), Firebase Analytics + Crashlytics, Play in-app review and WorkManager; `targetSdk` is 36.
- Code: `core/Ads.kt` requests consent at startup and limits ads to the G rating; `core/Screens.kt` shows the Privacy options button when required.
- Phone: the European consent form appears (see the test above).

Not verified:
- The Data safety table follows Google's published AdMob and Firebase disclosures from memory; compare with the Play SDK Index pages for the SDK versions in use.
- The US opt-out form on a phone (needs a VPN server in a covered US state, e.g. California).
- The Privacy options button reopening the form, and ads loading after a choice, were not reported back.

Sources:
- Target API rule: https://support.google.com/googleplay/android-developer/answer/11926878
- Consent form steps: https://support.google.com/admob/answer/10113207
