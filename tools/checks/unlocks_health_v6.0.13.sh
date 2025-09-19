#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-20}"
LOG="evidence/v6.0/unlocks/health_unlocks.13.txt"
export LC_ALL=C

ensure_device() {
  adb start-server >/dev/null 2>&1
  for i in {1..30}; do
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

latest_for_base_in_dir() {
  d="$1"; base="$2"
  ls -1 "$d/${base}"_v6.0*.sh 2>/dev/null | sort -V | tail -n1
}

mkdir -p "$(dirname "$LOG")"
: > "$LOG"

ensure_device || { echo "UNLOCKS-HEALTH RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (app not installed)"; exit 3; }

SUM="$(mktemp)"; trap 'rm -f "$SUM"' EXIT

recap() {
  awk '
    {
      if (match($0, /(^|[^A-Z0-9-])([A-Z0-9-]+)[[:space:]]+RESULT[[:space:]]*[:=][[:space:]]*(PASS|FAIL)/, m)) {
        print m[2] "=" m[3]
      }
    }
  ' "$1" >> "$SUM"
}

run_block_dir() {
  dir="$1"; pat="$2"
  for f in $(ls "$dir"/$pat 2>/dev/null | sort -V); do
    [ -f "$f" ] || continue
    name="$(basename "$f")"
    base="${name%%_v6.0*}"
    latest="$(latest_for_base_in_dir "$dir" "$base")"
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

run_block_dir "tools/fixtures" "*_v6.0*.sh"
run_block_dir "tools/checks"   "ee*_v6.0*.sh"
run_block_dir "tools/checks"   "tc*_v6.0*.sh"
run_block_dir "tools/checks"   "di*_v6.0*.sh"
run_block_dir "tools/checks"   "at*_v6.0*.sh"
run_block_dir "tools/checks"   "gv*_v6.0*.sh"

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
      END{
        if(f) print "FAIL";
        else if(p) print "PASS";
        else print "UNKNOWN";
      }
    ' "$SUM"
  )"
  echo "$k RESULT=$v" | tee -a "$LOG"
  [ "$v" = "FAIL" ] && fail=1
done

if [ $fail -eq 0 ] && ! grep -q "^.*=UNKNOWN$" "$SUM"; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$LOG"; exit 0
fi
echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$LOG"; exit 1
