#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/shape_guard.1.txt"
adb get-state >/dev/null 2>&1 || { echo "SHAPE-GUARD RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "SHAPE-GUARD RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

fail=0
echo "LOCKS_DIR: app/locks" | tee "$OUT"
for LOCKF in app/locks/*.header; do
  [ -f "$LOCKF" ] || continue
  BASE="$(basename "$LOCKF" .header)"
  CSVF="files/${BASE}.csv"
  HDR="$(tr -d $'\r' < "$LOCKF")"
  COLS="$(awk -F, '{print NF}' <<<"$HDR")"

  echo "FEATURE=$BASE" | tee -a "$OUT"
  echo "  CSV_FILE=$CSVF" | tee -a "$OUT"
  echo "  COLS_EXPECT=$COLS" | tee -a "$OUT"

  BODY="$(adb exec-out run-as "$PKG" sh -c 'tail -n +2 "'"$CSVF"'" 2>/dev/null' | tr -d $'\r')"
  if [ -z "$BODY" ]; then
    echo "  RESULT=PASS (empty body)" | tee -a "$OUT"
    continue
  fi

  BAD_COLS="$(printf "%s\n" "$BODY" | awk -F, -v N="$COLS" 'NF>0&&NF!=N{print NR":"$0}')"
  if [ -n "$BAD_COLS" ]; then
    echo "  BAD_ROWS_COLCOUNT=" | tee -a "$OUT"
    printf "%s\n" "$BAD_COLS" | tee -a "$OUT"
    fail=1
  fi

  FIRSTCOL="$(cut -d, -f1 <<<"$HDR")"
  if [ "$FIRSTCOL" = "date" ]; then
    BAD_DATE="$(printf "%s\n" "$BODY" | awk -F, '!($1 ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}$/){print NR":"$0}')"
    if [ -n "$BAD_DATE" ]; then
      echo "  BAD_ROWS_DATEFMT=" | tee -a "$OUT"
      printf "%s\n" "$BAD_DATE" | tee -a "$OUT"
      fail=1
    fi
  fi

  if [ "$fail" -eq 0 ]; then
    echo "  RESULT=PASS" | tee -a "$OUT"
  fi
done

if [ "$fail" -eq 0 ]; then
  echo "SHAPE-GUARD RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "SHAPE-GUARD RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
