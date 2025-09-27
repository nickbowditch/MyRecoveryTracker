#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
FILE="files/daily_lnslu.csv"
DIR="files"

adb get-state >/dev/null 2>&1
adb shell pm path "$PKG" >/dev/null 2>&1

adb exec-out run-as "$PKG" sh -c '
set -eu
f="'"$FILE"'"; d="'"$DIR"'"
[ -f "$f" ] || exit 4
chmod 600 "$f"
chmod 700 "$d"
# proof
ls -ln "$f"
ls -ldn "$d"
'
