#!/bin/sh
set -eu

GOLDEN_HEADER='date,feature_schema_version,delivered,opened,open_rate'
GOLDEN_TARGETS='
files/daily_notification_engagement.csv
app/locks/daily_notif_engagement.head
app/locks/daily_notif_engagement.header
'

LEGACY_HEADERS='
date,posted,removed,clicked
date,engagement_rate,posted,engaged
timestamp,package,title,text,event,reason
ts,event,notif_id
'
LEGACY_TOKENS_REGEX='(^|[^[:alnum:]_])(engagement_rate|posted|removed|clicked)($|[^[:alnum:]_])'

FAIL=0
SRC_LIST="$(find app/src -type f \( -name '*.kt' -o -name '*.java' \) 2>/dev/null || true)"

echo "$SRC_LIST" | while IFS= read -r f; do
  [ -n "$f" ] || continue
  if grep -qiE 'daily_notification_engagement\.csv|daily_notif_engagement|Notification.*(Engagement|Rollup)' "$f" 2>/dev/null; then
    grep -noE '"date,[^"]*"' "$f" 2>/dev/null | while IFS= read -r hit; do
      ln="${hit%%:*}"
      lit=$(printf '%s' "$hit" | sed -E 's/^[^:]*:"([^"]+)".*$/\1/')
      if [ "$lit" != "$GOLDEN_HEADER" ]; then
        echo "BAD_HEADER $f:$ln expected=\"$GOLDEN_HEADER\" found=\"$lit\""
        FAIL=$((FAIL+1))
      fi
    done
  fi
done

echo "$SRC_LIST" | while IFS= read -r f; do
  [ -n "$f" ] || continue
  echo "$LEGACY_HEADERS" | while IFS= read -r hdr; do
    [ -n "$hdr" ] || continue
    if grep -nF "\"$hdr\"" "$f" >/dev/null 2>&1; then
      echo "LEGACY_HEADER_LITERAL $f -> \"$hdr\""
      FAIL=$((FAIL+1))
    fi
  done
done

echo "$SRC_LIST" | while IFS= read -r f; do
  [ -n "$f" ] || continue
  if grep -qiE 'daily_notification_engagement\.csv|daily_notif_engagement|Notification.*(Engagement|Rollup)' "$f" 2>/dev/null; then
    if grep -nE "$LEGACY_TOKENS_REGEX" "$f" >/dev/null 2>&1; then
      echo "LEGACY_TOKENS $f"
      FAIL=$((FAIL+1))
    fi
  fi
done

echo "$GOLDEN_TARGETS" | while IFS= read -r tgt; do
  [ -n "$tgt" ] || continue
  grep -RIl -- "$tgt" app/src 2>/dev/null | while IFS= read -r f; do
    grep -noE '"date,[^"]*"' "$f" 2>/dev/null | while IFS= read -r hit; do
      ln="${hit%%:*}"
      lit=$(printf '%s' "$hit" | sed -E 's/^[^:]*:"([^"]+)".*$/\1/')
      if [ "$lit" != "$GOLDEN_HEADER" ]; then
        echo "DRIFT_REF $f:$ln target=\"$tgt\" expected=\"$GOLDEN_HEADER\" found=\"$lit\""
        FAIL=$((FAIL+1))
      fi
    done
  done
done

exit $([ "$FAIL" -gt 0 ] && echo 1 || echo 0)
