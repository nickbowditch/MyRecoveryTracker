#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-120}"
LOG="evidence/v6.0/unlocks/health_unlocks.8.txt"

adb get-state >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (app not installed)"; exit 3; }

run_with_timeout() { s="$1"; shift; ( "$@" & c=$!; ( sleep "$s"; kill -TERM "$c" 2>/dev/null ) & w=$!; wait "$c"; r=$?; kill "$w" 2>/dev/null || true; exit "$r"; ); }
ts(){ date +"%F %T"; }

mkdir -p "$(dirname "$LOG")"; : > "$LOG"

declare -A S
recap() {
  awk '
    match($0, /^([A-Z0-9-]+)[[:space:]]+RESULT[:=][[:space:]]*(PASS|FAIL)/, m){ last[m[1]]=m[2] }
    END{ for(k in last) printf "%s=%s\n", k, last[k] }
  ' "$1" | while IFS='=' read -r k v; do
    case "${S[$k]}" in
      FAIL) : ;;
      PASS) [ "$v" = "FAIL" ] && S[$k]="FAIL" ;;
      *) S[$k]="$v" ;;
    esac
  done
}

run_block() {
  pat="$1"
  for f in $(ls $pat 2>/dev/null | sort -V); do
    [ -f "$f" ] || continue
    name="$(basename "$f")"
    base="${name%%_v6.0*}"
    latest="$(ls "tools/checks/${base}"_v6.0*.sh 2>/dev/null | sort -V | tail -n1)"
    [ "$f" = "$latest" ] || continue
    chmod +x "$f" 2>/dev/null || true
    echo "==> $(ts) RUN $f" | tee -a "$LOG"
    tmp="$(mktemp)"; trap 'rm -f "$tmp"' EXIT
    run_with_timeout "$T" bash "$f" | tee -a "$LOG" | tee "$tmp" >/dev/null
    rc=${PIPESTATUS[0]}
    recap "$tmp"
    if [ $rc -ne 0 ]; then
      echo "--> $(ts) FAIL $f (rc=$rc)" | tee -a "$LOG"
    else
      echo "--> $(ts) PASS $f" | tee -a "$LOG"
    fi
    rm -f "$tmp"
    trap - EXIT
  done
}

run_block "tools/checks/ee*_v6.0*.sh"
run_block "tools/checks/tc*_v6.0*.sh"
run_block "tools/checks/di*_v6.0*.sh"
run_block "tools/checks/at*_v6.0*.sh"
run_block "tools/checks/gv*_v6.0*.sh"

order=(
  "EE-1" "EE-2" "EE-3"
  "DI-1" "DI-1-FIX" "DI-2" "DI-3" "DI-4"
  "TC-1" "TC-2" "TC-3" "TC-4"
  "AT-1" "AT-2" "AT-3"
  "GV-1" "GV-2" "GV-3" "GV-4" "GV-5"
)

echo "===== SUMMARY =====" | tee -a "$LOG"
fail=0
for k in "${order[@]}"; do
  v="${S[$k]}"
  [ -z "$v" ] && v="UNKNOWN"
  echo "$k RESULT=$v" | tee -a "$LOG"
  [ "$v" = "FAIL" ] && fail=1
done

if [ $fail -eq 0 ]; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$LOG"
  exit 0
else
  echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$LOG"
  exit 1
fi
