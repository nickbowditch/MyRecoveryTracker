#!/bin/bash
set -euo pipefail
PKG="com.nick.myrecoverytracker"
CSV="files/daily_movement_intensity.csv"
OUT="evidence/v6.0/movement_intensity/di3.txt"

mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1
fail(){ echo "DI3 RESULT=FAIL ($1)"; exit 1; }

adb get-state >/dev/null 2>&1 || { echo "DI3 RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "DI3 RESULT=FAIL (app not installed)"; exit 3; }

HDR="$(adb exec-out run-as "$PKG" sed -n '1p' "$CSV" 2>/dev/null | tr -d '\r' || true)"
[ -n "$HDR" ] || fail "CSV missing or unreadable"

echo "header=$HDR"

if [ "$HDR" = "date,light_min,moderate_min,vigorous_min,total_min" ]; then
  BAD_DATE="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1!~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/{print NR":"$0}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  BAD_INT="$(adb exec-out run-as "$PKG" awk -F, '
    NR==1{next}
    {
      ok=1
      for(i=2;i<=5;i++){
        if($i !~ /^-?[0-9]+$/) ok=0
        else if($i+0<0 || $i+0>1440) ok=0
      }
      if(!ok) print NR":"$0
    }' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  BAD_TOTAL="$(adb exec-out run-as "$PKG" awk -F, '
    NR==1{next}
    {
      l=$2+0;m=$3+0;v=$4+0;t=$5+0;
      sum=l+m+v;
      if ( (t-sum>1) || (sum-t>1) ) print NR":"$0" sum="sum" total="t
    }' "$CSV" 2>/dev/null | tr -d '\r' || true)"

  CNT_BAD_DATE="$(printf "%s\n" "${BAD_DATE:-}" | awk 'NF{n++} END{print n+0}')"
  CNT_BAD_INT="$(printf "%s\n" "${BAD_INT:-}"  | awk 'NF{n++} END{print n+0}')"
  CNT_BAD_TOTAL="$(printf "%s\n" "${BAD_TOTAL:-}"| awk 'NF{n++} END{print n+0}')"

  echo "bad_date_rows=$CNT_BAD_DATE"
  echo "bad_min_bounds_or_types_rows=$CNT_BAD_INT"
  echo "bad_total_mismatch_rows=$CNT_BAD_TOTAL"

  echo "--- DEBUG: bad date samples ---"
  [ -n "$BAD_DATE" ] && printf "%s\n" "$BAD_DATE" | head -n 10 || echo "<none>"
  echo "--- DEBUG: bad min bounds/type samples ---"
  [ -n "$BAD_INT" ] && printf "%s\n" "$BAD_INT" | head -n 10 || echo "<none>"
  echo "--- DEBUG: bad total mismatch samples ---"
  [ -n "$BAD_TOTAL" ] && printf "%s\n" "$BAD_TOTAL" | head -n 10 || echo "<none>"

  echo "--- DEBUG: CSV head ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 15 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
  echo "--- DEBUG: CSV tail ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 15 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true

  if [ "$CNT_BAD_DATE" -gt 0 ] || [ "$CNT_BAD_INT" -gt 0 ] || [ "$CNT_BAD_TOTAL" -gt 0 ]; then
    fail "types/bounds/total validations failed"
  fi

elif [ "$HDR" = "date,intensity" ]; then
  BAD_DATE="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $1!~/^[0-9]{4}-[0-9]{2}-[0-9]{2}$/{print NR":"$0}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  BAD_INTENSITY_TYPE="$(adb exec-out run-as "$PKG" awk -F, 'NR>1 && $2!~/^-?[0-9]+(\.[0-9]+)?$/{print NR":"$0}' "$CSV" 2>/dev/null | tr -d '\r' || true)"
  BAD_INTENSITY_RANGE="$(adb exec-out run-as "$PKG" awk -F, '
    NR>1 {
      if($2 ~ /^-?[0-9]+(\.[0-9]+)?$/){
        v=$2+0; if(v<0 || v>100) print NR":"$0
      } else {
        # non-numeric also considered bad, but already counted above
      }
    }' "$CSV" 2>/dev/null | tr -d '\r' || true)"

  CNT_BAD_DATE="$(printf "%s\n" "${BAD_DATE:-}" | awk 'NF{n++} END{print n+0}')"
  CNT_BAD_TYPE="$(printf "%s\n" "${BAD_INTENSITY_TYPE:-}" | awk 'NF{n++} END{print n+0}')"
  CNT_BAD_RANGE="$(printf "%s\n" "${BAD_INTENSITY_RANGE:-}" | awk 'NF{n++} END{print n+0}')"

  echo "bad_date_rows=$CNT_BAD_DATE"
  echo "bad_intensity_type_rows=$CNT_BAD_TYPE"
  echo "bad_intensity_range_rows=$CNT_BAD_RANGE"

  echo "--- DEBUG: bad date samples ---"
  [ -n "$BAD_DATE" ] && printf "%s\n" "$BAD_DATE" | head -n 10 || echo "<none>"
  echo "--- DEBUG: bad intensity type samples ---"
  [ -n "$BAD_INTENSITY_TYPE" ] && printf "%s\n" "$BAD_INTENSITY_TYPE" | head -n 10 || echo "<none>"
  echo "--- DEBUG: bad intensity range samples ---"
  [ -n "$BAD_INTENSITY_RANGE" ] && printf "%s\n" "$BAD_INTENSITY_RANGE" | head -n 10 || echo "<none>"

  echo "--- DEBUG: CSV head ---"
  adb exec-out run-as "$PKG" sh -c 'head -n 15 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true
  echo "--- DEBUG: CSV tail ---"
  adb exec-out run-as "$PKG" sh -c 'tail -n 15 "'"$CSV"'" 2>/dev/null | tr -d "\r"' || true

  if [ "$CNT_BAD_DATE" -gt 0 ] || [ "$CNT_BAD_TYPE" -gt 0 ] || [ "$CNT_BAD_RANGE" -gt 0 ]; then
    fail "types/bounds validations failed"
  fi

else
  fail "unsupported header '$HDR'"
fi

echo "DI3 RESULT=PASS"
exit 0
