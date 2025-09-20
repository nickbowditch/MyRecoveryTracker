#!/bin/bash
PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/bootstrap_all.1.txt"

adb get-state >/dev/null 2>&1 || { echo "BOOTSTRAP RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "BOOTSTRAP RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

echo "LOCKS_DIR: app/locks" | tee "$OUT"
ret=0

for LOCKF in app/locks/*.header; do
  [ -f "$LOCKF" ] || continue
  BASE="$(basename "$LOCKF" .header)"
  CSVF="files/${BASE}.csv"
  LOCK_HDR="$(tr -d $'\r' < "$LOCKF" 2>/dev/null)"
  DEV_HDR="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 "'"$CSVF"'" 2>/dev/null' | tr -d $'\r')"

  echo "FEATURE=$BASE" | tee -a "$OUT"
  echo "  CSV_FILE=$CSVF" | tee -a "$OUT"
  echo "  LOCK_HEADER=$LOCK_HDR" | tee -a "$OUT"
  echo "  CSV_HEADER=$DEV_HDR" | tee -a "$OUT"

  if [ -z "$DEV_HDR" ]; then
    adb shell run-as "$PKG" "mkdir -p files && printf '%s\n' '$LOCK_HDR' > '$CSVF'"
    echo "  ACTION=create_csv_with_lock_header" | tee -a "$OUT"
    continue
  fi

  if [ "$DEV_HDR" = "$LOCK_HDR" ]; then
    echo "  ACTION=ok_aligned" | tee -a "$OUT"
    continue
  fi

  if [ "$BASE" = "daily_metrics" ] && [ "$DEV_HDR" = "date,unlocks" ] && [ "$LOCK_HDR" = "date,feature_schema_version,daily_unlocks" ]; then
    adb exec-out run-as "$PKG" cat "$CSVF" | tr -d $'\r' > /tmp/_dev_unl_$$.csv
    awk -F, -v OFS=',' '
      NR==1 { print "date,feature_schema_version,daily_unlocks"; next }
      NF>=2 { print $1,"v6.0",$2 }
    ' /tmp/_dev_unl_$$.csv > /tmp/_dev_unl_$$.migrated.csv
    adb shell run-as "$PKG" "mkdir -p files"
    adb push /tmp/_dev_unl_$$.migrated.csv /sdcard/_tmp_daily_unlocks.csv >/dev/null
    adb shell run-as "$PKG" "cat /sdcard/_tmp_daily_unlocks.csv > '$CSVF'.tmp && rm -f '$CSVF' && mv '$CSVF'.tmp '$CSVF' && rm -f /sdcard/_tmp_daily_unlocks.csv"
    rm -f /tmp/_dev_unl_$$.csv /tmp/_dev_unl_$$.migrated.csv
    NEW_HDR="$(adb exec-out run-as "$PKG" sh -c 'head -n 1 "'"$CSVF"'"' | tr -d $'\r')"
    echo "  ACTION=migrated_unlocks" | tee -a "$OUT"
    echo "  NEW_HEADER=$NEW_HDR" | tee -a "$OUT"
    if [ "$NEW_HDR" != "$LOCK_HDR" ]; then
      echo "  ERROR=post_migration_mismatch" | tee -a "$OUT"
      ret=1
    fi
    continue
  fi

  ROW2="$(adb exec-out run-as "$PKG" sh -c 'sed -n 2p "'"$CSVF"'" 2>/dev/null' | tr -d $'\r')"
  if [ -z "$ROW2" ]; then
    adb shell run-as "$PKG" "printf '%s\n' '$LOCK_HDR' > '$CSVF'"
    echo "  ACTION=reheader_empty_file" | tee -a "$OUT"
  else
    echo "  ACTION=flag_header_drift" | tee -a "$OUT"
    ret=1
  fi
done

if [ "$ret" -eq 0 ]; then
  echo "BOOTSTRAP RESULT=PASS" | tee -a "$OUT"; exit 0
else
  echo "BOOTSTRAP RESULT=FAIL" | tee -a "$OUT"; exit 1
fi
