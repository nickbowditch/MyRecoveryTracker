#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"

# Remove lock/sentinel files that can block same-day reruns (keep the header!)
adb shell run-as "$PKG" sh <<'IN'
set -eu
mkdir -p app/locks
for f in app/locks/*; do
  [ -e "$f" ] || continue
  case "$f" in
    app/locks/daily_notif_engagement.header|app/locks/daily_notif_engagement.head) : ;; # keep
    *notif*|*engage*|*rollup*|*daily*|*done*|*stamp*|*processed*) rm -f "$f" || true ;;
  esac
done

# Scrub shared_prefs keys that look like “done today” flags for notif engagement
mkdir -p shared_prefs
for x in shared_prefs/*.xml; do
  [ -f "$x" ] || continue
  # Remove lines that look like last-run / done-today markers scoped to notif/engagement/rollup
  toybox sed -i \
    -e '/notif\|engage\|rollup/{
           /last\|done\|today\|stamp\|processed/d
         }' "$x" || true
done
IN
