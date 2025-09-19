#!/bin/sh
PKG="com.nick.myrecoverytracker"
DAY="$1"
[ -z "$DAY" ] && exit 2

FIX="qa/golden/$DAY/daily_unlocks.csv"
ROW="$(adb exec-out run-as "$PKG" sh -c 'grep "^'"$DAY"'," files/daily_unlocks.csv 2>/dev/null || true')"

mkdir -p qa/tmp
printf "%s\n" "$ROW" > "qa/tmp/daily_unlocks.$DAY.actual.csv"
diff -u "$FIX" "qa/tmp/daily_unlocks.$DAY.actual.csv" >/dev/null
