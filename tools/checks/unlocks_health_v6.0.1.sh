#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-90}"
LOG="evidence/v6.0/unlocks/health_unlocks.1.txt"

adb get-state >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (app not installed)"; exit 3; }

run_with_timeout() {
  secs="$1"; shift
  ( "$@" & c=$!
    ( sleep "$secs"; kill -TERM "$c" 2>/dev/null ) & w=$!
    wait "$c"; r=$?
    kill "$w" 2>/dev/null || true
    exit "$r"
  )
}

ts(){ date +"%F %T"; }

mkdir -p "$(dirname "$LOG")"
: > "$LOG"

fail=0

run_block() {
  pat="$1"
  for f in $(ls $pat 2>/dev/null | sort); do
    [ -f "$f" ] || continue
    chmod +x "$f" 2>/dev/null || true
    echo "==> $(ts) RUN $f" | tee -a "$LOG"
    run_with_timeout "$T" bash "$f" | tee -a "$LOG"
    rc=${PIPESTATUS[0]}
    if [ $rc -ne 0 ]; then
      echo "--> $(ts) FAIL $f (rc=$rc)" | tee -a "$LOG"
      fail=1
    else
      echo "--> $(ts) PASS $f" | tee -a "$LOG"
    fi
  done
}

run_block "tools/checks/ee*_v6.0*.sh"
run_block "tools/checks/tc*_v6.0*.sh"
run_block "tools/checks/di*_v6.0*.sh"
run_block "tools/checks/at*_v6.0*.sh"
run_block "tools/checks/gv*_v6.0*.sh"

if [ "$fail" -eq 0 ]; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$LOG"
  exit 0
else
  echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$LOG"
  exit 1
fi
