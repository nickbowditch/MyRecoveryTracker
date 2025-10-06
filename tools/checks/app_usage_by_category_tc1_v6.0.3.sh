#!/bin/sh
set -eu
PKG="com.nick.myrecoverytracker"
CSV="files/app_category_daily.csv"
OUT="evidence/v6.0/app_usage_by_category/tc1.3.txt"
RCV="$PKG/.TriggerReceiver"
ACT="$PKG.ACTION_RUN_APP_CATEGORY_DAILY"
mkdir -p "$(dirname "$OUT")"

cleanup() {
  adb shell run-as "$PKG" sh <<'IN' >/dev/null 2>&1 || true
set -eu
f="files/app_category_daily.csv"
b="files/app_category_daily.csv.bak.tc1"
e="files/app_category_daily.csv.bak.tc1.empty"
if [ -f "$b" ]; then
  mv -f "$b" "$f"
elif [ -f "$e" ]; then
  rm -f "$f" "$e"
fi
IN
}
trap cleanup EXIT

adb get-state >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (no device)" | tee "$OUT"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "TC1 RESULT=FAIL (app not installed)" | tee "$OUT"; exit 3; }

adb shell run-as "$PKG" sh <<'IN'
set -eu
f="files/app_category_daily.csv"
b="files/app_category_daily.csv.bak.tc1"
mkdir -p "$(dirname "$f")"
if [ -f "$f" ]; then
  cat "$f" >"$b"
else
  : >"files/app_category_daily.csv.bak.tc1.empty"
fi
printf "date,category,minutes\n" >"$f"
IN

T="$(adb shell toybox date +%F | tr -d '\r')"
Y="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)-86400 ))" +%F | tr -d '\r')"
TM="$(adb shell toybox date -d "@$(( $(adb shell toybox date +%s)+86400 ))" +%F | tr -d '\r')"

deadline=$(( $(date +%s) + 30 ))
while :; do
  adb shell cmd activity broadcast -n "$RCV" -a "$ACT" --receiver-foreground --user 0 >/dev/null 2>&1 || true
  sleep 2
  HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  [ "$HDR" = "date,category,minutes" ] || { echo "TC1 RESULT=FAIL (bad header)" | tee "$OUT"; exit 5; }

  COUNTS="$(adb exec-out run-as "$PKG" awk -F, '
  NR==1{next}
  {d=$1; gsub(/^[[:space:]]+|[[:space:]]+$/,"",d); if(d!="") c[d]++}
  END{for(k in c) print k","c[k]}
  ' "$CSV" 2>/dev/null | tr -d '\r' || true)"

  cT=0; cY=0; cTM=0; other=0
  if [ -n "$COUNTS" ]; then
    while IFS=, read -r d n; do
      [ -z "$d" ] && continue
      if   [ "$d" = "$T" ];  then cT="$n"
      elif [ "$d" = "$Y" ];  then cY="$n"
      elif [ "$d" = "$TM" ]; then cTM="$n"
      else other=$((other+1))
      fi
    done <<EOF2
$COUNTS
EOF2
  fi

  if [ "$other" -eq 0 ] && { { [ "$cT" -gt 0 ] && [ "$cY" -eq 0 ] && [ "$cTM" -eq 0 ]; } || { [ "$cT" -eq 0 ] && [ "$cY" -gt 0 ] && [ "$cTM" -eq 0 ]; } || { [ "$cT" -eq 0 ] && [ "$cY" -eq 0 ] && [ "$cTM" -gt 0 ]; }; }; then
    {
      echo "TC1 RESULT=PASS"
      echo "counts: Y=$cY T=$cT TM=$cTM other_dates=$other"
    } | tee "$OUT"
    exit 0
  fi

  [ "$(date +%s)" -ge "$deadline" ] && break
done

{
  echo "TC1 RESULT=FAIL (counts: Y=$cY T=$cT TM=$cTM other_dates=$other)"
  echo "--- DEBUG HEAD ---"
  adb exec-out run-as "$PKG" head -n10 "$CSV" 2>/dev/null || echo "[no file]"
} | tee "$OUT"
exit 1
