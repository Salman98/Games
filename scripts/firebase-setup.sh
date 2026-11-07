#!/usr/bin/env bash
# One-time Firebase setup for the games. Run from the repo root on your own machine:
#   bash scripts/firebase-setup.sh
# Needs Node (for npx) and, for SHA-1 fingerprints, the JDK's keytool.
set -euo pipefail
cd "$(dirname "$0")/.."
PROJECT=techeliteclub-games
FB="npx -y firebase-tools@latest"

# module dir | display name | package
APPS="arrow-escape|Arrow Escape|com.techeliteclub.game.arrowescape
tile-mahjong|Tile Mahjong|com.techeliteclub.game.tilemahjong
sudoku|Sudoku Classic|com.techeliteclub.game.sudoku
word-search|Word Search|com.techeliteclub.game.wordsearch
solitaire|Solitaire Klondike|com.techeliteclub.game.solitaire
car-escape|Car Escape|com.techeliteclub.game.carescape
merge-2048|2048 Merge|com.techeliteclub.game.merge2048"

$FB login:list | grep -q "@" || $FB login
$FB use "$PROJECT"

# Debug keystore SHA-1, so Google Sign-In works in debug builds. Add release + Play App Signing SHA-1s later (see README).
DEBUG_SHA1=$(keytool -list -v -alias androiddebugkey -keystore "$HOME/.android/debug.keystore" -storepass android 2>/dev/null | awk '/SHA1:/ {print $2}')

EXISTING=$($FB apps:list ANDROID --project "$PROJECT" --json)
while IFS='|' read -r dir name pkg; do
  APP_ID=$(echo "$EXISTING" | node -e "let s='';process.stdin.on('data',d=>s+=d).on('end',()=>{const a=(JSON.parse(s).result||[]).find(x=>x.packageName==='$pkg');console.log(a?a.appId:'')})")
  if [ -z "$APP_ID" ]; then
    echo "Registering $name ($pkg)"
    APP_ID=$($FB apps:create ANDROID "$name" --package-name "$pkg" --project "$PROJECT" --json \
      | node -e "let s='';process.stdin.on('data',d=>s+=d).on('end',()=>console.log(JSON.parse(s).result.appId))")
  else
    echo "$name already registered ($APP_ID)"
  fi
  if [ -n "$DEBUG_SHA1" ]; then
    $FB apps:android:sha:create "$APP_ID" "$DEBUG_SHA1" --project "$PROJECT" || true # already added is fine
  fi
  echo "$APP_ID" > "$dir/.firebase-app-id"
done <<< "$APPS"

# Firestore database. Its location can never be changed, so you choose it.
if ! $FB firestore:databases:list --project "$PROJECT" | grep -q "(default)"; then
  $FB firestore:locations --project "$PROJECT"
  read -rp "Firestore location for the (default) database (e.g. nam5, eur3, asia-southeast1): " LOC
  $FB firestore:databases:create "(default)" --edition=enterprise --location="$LOC" --project "$PROJECT"
fi

# Google Sign-In provider + Firestore rules. Deploying auth creates the OAuth clients Google Sign-In needs.
$FB deploy --only auth,firestore:rules --project "$PROJECT"

# Download config last, so it includes the OAuth client created above.
while IFS='|' read -r dir name pkg; do
  $FB apps:sdkconfig ANDROID "$(cat "$dir/.firebase-app-id")" --project "$PROJECT" --out "$dir/google-services.json"
  rm "$dir/.firebase-app-id"
  grep -q '"client_type": 3' "$dir/google-services.json" || echo "WARNING: $dir/google-services.json has no web OAuth client yet; Google Sign-In will fail until it does."
done <<< "$APPS"

echo "Done. Build one with: ./gradlew :arrow-escape:assembleDebug"
