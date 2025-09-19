#!/bin/bash
set -e
TASK="$(./gradlew :app:tasks --all --no-daemon --console=plain 2>/dev/null | awk '
  /^qaCheckLateScreen($| )/ {print "qaCheckLateScreen"; exit}
  /^qaCheck($| )/           {print "qaCheck"; exit}
')"
[ -n "$TASK" ] || { echo "Late-Screen GV-2 RESULT=FAIL (no qaCheck task found)"; exit 1; }

OUT="$(mktemp)"
./gradlew :app:"$TASK" --no-daemon --console=plain >"$OUT" 2>&1 || {
  echo "task: $TASK"
  cat "$OUT"
  echo "Late-Screen GV-2 RESULT=FAIL"
  exit 1
}

if grep -qE 'OK:.*daily_late_screen\.csv' "$OUT"; then
  echo "task: $TASK"
  echo "Late-Screen GV-2 RESULT=PASS"
else
  echo "task: $TASK"
  grep -n 'late_screen\|late-night\|daily_late_screen' "$OUT" || true
  echo "Late-Screen GV-2 RESULT=FAIL (missing OK for daily_late_screen.csv)"
  exit 1
fi
