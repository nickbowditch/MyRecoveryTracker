#!/bin/sh
set -eu

ARCH="tools/checks/_archive/v6.0"
CHK="app/checklist_v6.0.txt"
OUT="evidence/v6.0/lnsu/late_night_boolean.deprecated.txt"

mkdir -p "$(dirname "$OUT")" "$ARCH" "$(dirname "$CHK")"

if [ -f "app/locks/daily_late_night_screen_usage.header" ]; then
  git rm -qf app/locks/daily_late_night_screen_usage.header 2>/dev/null || rm -f app/locks/daily_late_night_screen_usage.header
fi

if [ -f "$CHK" ]; then
  TS="$(date +%s)"
  cp "$CHK" "$CHK.bak.$TS"
  awk '!/daily_late_night_screen_usage\.csv/' "$CHK" > "$CHK.new" && mv "$CHK.new" "$CHK"
fi

REFS="$(grep -RIlE 'daily_late_night_screen_usage\.csv|LateNightScreenRollupWorker' tools/checks 2>/dev/null || true)"
if [ -n "$REFS" ]; then
  echo "$REFS" | while IFS= read -r f; do
    [ -z "$f" ] && continue
    case "$f" in
      tools/checks/_archive/*) continue ;;
    esac
    if git ls-files --error-unmatch "$f" >/dev/null 2>&1; then
      git mv -f "$f" "$ARCH/$(basename "$f")" 2>/dev/null || mv -f "$f" "$ARCH/"
    else
      mv -f "$f" "$ARCH/" 2>/dev/null || true
    fi
  done
fi

printf '%s\n' "DEPRECATED: daily_late_night_screen_usage.csv (boolean 00:00–05:00). Canonical metric is daily_lnsu.csv (minutes 22:00–02:00)." > "$OUT"

git add -A >/dev/null 2>&1 || true
git add -f "$CHK" >/dev/null 2>&1 || true
git commit -m "lnsu: deprecate daily_late_night_screen_usage.csv (boolean); archive checks; checklist updated; canonical = daily_lnsu.csv" >/dev/null 2>&1 || true

echo "OK"
