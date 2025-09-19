#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-90}"
LOG="evidence/v6.0/unlocks/health_unlocks.4.txt"

adb get-state >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (no device)"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "UNLOCKS-HEALTH RESULT=FAIL (app not installed)"; exit 3; }

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

pick_latest() {
  pat="$1"
  ls $pat 2>/dev/null | awk '
  {
    f=$0
    n=split(f,a,"/"); b=a[n]
    if (b ~ /^unlocks_health_v6\.0(\.[0-9]+)?\.sh$/) next
    stem=b
    sub(/_v6\.0(\.[0-9]+)?\.sh$/,"",stem)
    m=0
    if (b ~ /_v6\.0\.[0-9]+\.sh$/) {
      t=b; sub(/.*_v6\.0\./,"",t); sub(/\.sh$/,"",t); m=t+0
    }
    if (!(stem in max) || m>=max[stem]) { max[stem]=m; file[stem]=f }
  }
  END{ for (s in file) print file[s] }'
}

mkdir -p "$(dirname "$LOG")"
: > "$LOG"
SUM="$(mktemp)"; trap 'rm -f "$SUM"' EXIT

fail=0

run_block() {
  pat="$1"
  for f in $(pick_latest "$pat" | sort); do
    [ -f "$f" ] || continue
    chmod +x "$f" 2>/dev/null || true
    echo "==> $(ts) RUN $f" | tee -a "$LOG"
    tmpo="$(mktemp)"
    run_with_timeout "$T" bash "$f" >"$tmpo" 2>&1
    rc=$?
    cat "$tmpo" | tee -a "$LOG"
    res_line="$(grep -E 'RESULT[:=](PASS|FAIL)' "$tmpo" | tail -n1)"
    rm -f "$tmpo"
    [ -n "$res_line" ] || res_line="RESULT=UNKNOWN"
    label="$(echo "$res_line" | awk '{print $1}')"
    verdict="$(echo "$res_line" | grep -Eo '(PASS|FAIL)' | tail -n1)"
    [ -n "$label" ] || label="$(basename "$f")"
    [ -n "$verdict" ] || verdict="UNKNOWN"
    printf "%s RESULT=%s\n" "$label" "$verdict" >> "$SUM"
    [ $rc -eq 0 ] || fail=1
  done
}

run_block "tools/checks/ee*_v6.0*.sh"
run_block "tools/checks/tc*_v6.0*.sh"
run_block "tools/checks/di*_v6.0*.sh"
run_block "tools/checks/at*_v6.0*.sh"
run_block "tools/checks/gv*_v6.0*.sh"

echo "" | tee -a "$LOG"
echo "===== SUMMARY =====" | tee -a "$LOG"
sort "$SUM" | tee -a "$LOG"

if [ "$fail" -eq 0 ]; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$LOG"
  exit 0
else
  echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$LOG"
  exit 1
fi
