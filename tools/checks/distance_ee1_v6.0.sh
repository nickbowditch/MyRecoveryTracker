#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/distance/ee1.txt"
mkdir -p "$(dirname "$OUT")"
adb get-state >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (no device/emulator)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "EE-1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }
decl() { p="$1"; adb shell dumpsys package "$PKG" 2>/dev/null | tr -d '\r' | grep -Fq "$p" && echo yes || echo no; }
appop_mode() { op="$1"; adb shell cmd appops get "$PKG" "$op" 2>/dev/null | tr -d '\r' | awk -F': ' '/mode=|[[:space:]]mode=/{m=$2} END{gsub(/;.*$/,"",m); gsub(/^mode=/,"",m); print (m!=""?m:"none")}'; }
is_whitelisted_idle() { adb shell dumpsys deviceidle whitelist 2>/dev/null | tr -d '\r' | grep -qw "$PKG" && echo yes || echo no; }
FINE_DECL="$(decl "android.permission.ACCESS_FINE_LOCATION")"
COARSE_DECL="$(decl "android.permission.ACCESS_COARSE_LOCATION")"
BKG_DECL="$(decl "android.permission.ACCESS_BACKGROUND_LOCATION")"
FINE_MODE="$(appop_mode "android:fine_location")"
COARSE_MODE="$(appop_mode "android:coarse_location")"
BKG_MODE="$(appop_mode "android:access_background_location")"
WHITELIST_IDLE="$(is_whitelisted_idle)"
ok=0
if echo "$FINE_MODE" | grep -Eq '^(allow|foreground)$' || echo "$COARSE_MODE" | grep -Eq '^(allow|foreground)$'; then ok=1; else ok=0; fi
if [ "$BKG_DECL" = "yes" ]; then echo "$BKG_MODE" | grep -Eq '^allow$' || ok=0; fi
{
echo "DECLARED: FINE=$FINE_DECL COARSE=$COARSE_DECL BACKGROUND=$BKG_DECL"
echo "APP-OPS: fine_location=$FINE_MODE coarse_location=$COARSE_MODE access_background_location=$BKG_MODE"
echo "IDLE: whitelisted=$WHITELIST_IDLE"
} | tee "$OUT" >/dev/null
if [ "$ok" -eq 1 ]; then echo "EE-1 RESULT=PASS" | tee -a "$OUT"; exit 0; else echo "EE-1 RESULT=FAIL" | tee -a "$OUT"; exit 1; fi
