#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/daily_unlocks.csv"
OUT="evidence/v6.0/unlocks/midnight_watch.1.txt"
mkdir -p "$(dirname "$OUT")"; : >"$OUT"
say(){ printf '%s\n' "$*" | tee -a "$OUT"; }

adb get-state >/dev/null 2>&1 || { say "MIDNIGHT-WATCH RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { say "MIDNIGHT-WATCH RESULT=FAIL (app not installed)"; exit 3; }

snap(){ tag="$1"; adb exec-out run-as "$PKG" cat "$CSV" 2>/dev/null | tee "evidence/v6.0/unlocks/midnight_${tag}.csv" >/dev/null; }

while [ "$(date +%H:%M)" != "23:55" ]; do sleep 20; done
snap before

while [ "$(date +%H:%M)" != "00:05" ]; do sleep 20; done
snap after

diff -u evidence/v6.0/unlocks/midnight_before.csv evidence/v6.0/unlocks/midnight_after.csv > evidence/v6.0/unlocks/midnight_diff.txt || true

if [ -s evidence/v6.0/unlocks/midnight_diff.txt ]; then
  say "MIDNIGHT-WATCH RESULT=FAIL (mutation detected)"
  exit 1
else
  say "MIDNIGHT-WATCH RESULT=PASS (no mutation)"
  exit 0
fi
