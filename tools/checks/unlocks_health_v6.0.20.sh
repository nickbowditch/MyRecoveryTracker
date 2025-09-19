#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-25}"
LOG="evidence/v6.0/unlocks/health_unlocks.20.txt"
export LC_ALL=C

ensure_device() {
  adb start-server >/dev/null 2>&1
  for i in {1..20}; do
    st="$(adb get-state 2>/dev/null | tr -d $'\r')"
    [ "$st" = "device" ] && { adb shell true >/dev/null 2>&1; return 0; }
    sleep 1
  done
  return 1
}

run_with_timeout() {
  s="$1"; shift
  ( "$@" & c=$!
    ( sleep "$s"; kill -TERM "$c" 2>/dev/null ) & w=$!
    wait "$c"; r=$?
    kill "$w" 2>/dev/null || true
    exit "$r"
  )
}

ts(){ date +"%F %T"; }

latest_for_base() {
  name="$(basename "$1")"
  base="${name%%_v6.0*}"
  ls -1 "tools/checks/${base}"_v6.0*.sh 2>/dev/null | sort -V | tail -n1
}

mkdir -p "$(dirname "$LOG")"
: > "$LOG"

ensure_device || { echo "UNLOCKS-HEALTH RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (app not installed)"; exit 3; }

SUM="$(mktemp)"; trap 'rm -f "$SUM"' EXIT

recap() {
  grep -E '^[A-Z0-9-]+[[:space:]]+RESULT[[:space:]]*[:=][[:space:]]*(PASS|FAIL)' "$1" | \
  awk '{k=$1; if ($0 ~ /RESULT[[:space:]]*[:=][[:space:]]*PASS/) v="PASS"; else v="FAIL"; print k "=" v}' >> "$SUM"
}

run_block() {
  pat="$1"
  for f in $(ls $pat 2>/dev/null | sort -V); do
    [ -f "$f" ] || continue
    latest="$(latest_for_base "$f")"
    [ -n "$latest" ] || continue
    [ "$f" = "$latest" ] || continue
    chmod +x "$f" 2>/dev/null || true
    echo "==> $(ts) RUN $f" | tee -a "$LOG"
    tmp="$(mktemp)"
    run_with_timeout "$T" bash "$f" | tee -a "$LOG" | tee "$tmp" >/dev/null
    rc=${PIPESTATUS[0]}
    recap "$tmp"
    if [ $rc -ne 0 ]; then
      echo "--> $(ts) FAIL $f (rc=$rc)" | tee -a "$LOG"
    else
      echo "--> $(ts) PASS $f" | tee -a "$LOG"
    fi
    rm -f "$tmp"
  done
}

run_block "tools/fixtures/*_v6.0*.sh"
run_block "tools/checks/di*_v6.0*.sh"
run_block "tools/checks/ee*_v6.0*.sh"
run_block "tools/checks/tc*_v6.0*.sh"
run_block "tools/checks/at*_v6.0*.sh"
run_block "tools/checks/gv*_v6.0*.sh"

order=(
  "EE-1" "EE-2" "EE-3"
  "DI-1" "DI-1-FIX" "DI-2" "DI-3" "DI-4"
  "TC-1" "TC-2" "TC-3" "TC-4" "TC-5" "TC-6"
  "AT-1" "AT-2" "AT-3"
  "GV-1" "GV-2" "GV-3" "GV-4" "GV-5"
)

echo "===== SUMMARY =====" | tee -a "$LOG"
fail=0
for k in "${order[@]}"; do
  v="$(
    awk -F= -v key="$k" '
      $1==key { if($2=="FAIL") f=1; if($2=="PASS") p=1 }
      END { if(f) print "FAIL"; else if(p) print "PASS"; else print "UNKNOWN" }
    ' "$SUM"
  )"
  echo "$k RESULT=$v" | tee -a "$LOG"
  [ "$v" = "FAIL" ] && fail=1
done

if [ $fail -eq 0 ]; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$LOG"; exit 0
else
  echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$LOG"; exit 1
fi
