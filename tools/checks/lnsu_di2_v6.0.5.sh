#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/lnsu/di2.5.txt"
CSV="files/daily_lnslu.csv"
DIR="files"
LOCK="app/locks/daily_lnslu.header"
mkdir -p "$(dirname "$OUT")"

adb get-state >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI-2 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$CSV"'"; d="'"$DIR"'"
[ -f "$f" ] || exit 4
chmod 600 "$f"
chmod 700 "$d"
ls -ln "$f"
ls -ldn "$d"
' | tee evidence/v6.0/lnsu/di2.perms.5.txt

EXP="$(tr -d '\r' < "$LOCK" 2>/dev/null || true)"
[ -n "$EXP" ] || { echo "DI-2 RESULT=FAIL (missing lock)" | tee "$OUT"; exit 4; }

HDR="$(adb exec-out run-as "$PKG" head -n1 "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || { echo "DI-2 RESULT=FAIL (missing csv)" | tee "$OUT"; exit 5; }
[ "$HDR" = "$EXP" ] || { echo "DI-2 RESULT=FAIL (bad header)" | tee "$OUT"; exit 6; }

PKG_UID="$(adb exec-out run-as "$PKG" id -u 2>/dev/null | tr -d '\r' || true)"
[ -n "$PKG_UID" ] || { echo "DI-2 RESULT=FAIL (uid unreadable)" | tee "$OUT"; exit 7; }

FILE_INFO="$(adb exec-out run-as "$PKG" sh -c 'ls -ln "'"$CSV"'" 2>/dev/null | awk "{print \$1,\$3,\$4}"' 2>/dev/null | tr -d '\r' || true)"
[ -n "$FILE_INFO" ] || { echo "DI-2 RESULT=FAIL (csv not found)" | tee "$OUT"; exit 8; }
FILE_PERM="$(echo "$FILE_INFO" | awk '{print $1}')"
FILE_UID="$(echo "$FILE_INFO" | awk '{print $2}')"
FILE_GID="$(echo "$FILE_INFO" | awk '{print $3}')"

DIR_INFO="$(adb exec-out run-as "$PKG" sh -c 'ls -ldn "'"$DIR"'" 2>/dev/null | awk "{print \$1,\$3,\$4}"' 2>/dev/null | tr -d '\r' || true)"
[ -n "$DIR_INFO" ] || { echo "DI-2 RESULT=FAIL (files dir missing)" | tee "$OUT"; exit 9; }
DIR_PERM="$(echo "$DIR_INFO" | awk '{print $1}')"
DIR_UID="$(echo "$DIR_INFO" | awk '{print $2}')"
DIR_GID="$(echo "$DIR_INFO" | awk '{print $3}')"

[ "$FILE_UID" = "$PKG_UID" ] && [ "$FILE_GID" = "$PKG_UID" ] || { echo "DI-2 RESULT=FAIL (csv owner $FILE_UID:$FILE_GID != $PKG_UID)" | tee "$OUT"; exit 10; }
[ "$DIR_UID" = "$PKG_UID" ] && [ "$DIR_GID" = "$PKG_UID" ] || { echo "DI-2 RESULT=FAIL (files dir owner $DIR_UID:$DIR_GID != $PKG_UID)" | tee "$OUT"; exit 11; }

[ "$FILE_PERM" = "-rw-------" ] || { echo "DI-2 RESULT=FAIL (csv perms $FILE_PERM != -rw-------)" | tee "$OUT"; exit 12; }
[ "$DIR_PERM" = "drwx------" ] || { echo "DI-2 RESULT=FAIL (files dir perms $DIR_PERM != drwx------)" | tee "$OUT"; exit 13; }

echo "DI-2 RESULT=PASS" | tee "$OUT"
exit 0
