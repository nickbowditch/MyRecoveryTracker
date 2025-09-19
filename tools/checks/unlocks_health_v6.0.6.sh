#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-120}"
LOG="evidence/v6.0/unlocks/health_unlocks.6.txt"

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

per_timeout() {
  b="$(basename "$1")"
  case "$b" in
    tc2_v6.0*.sh) echo "${HC_TIMEOUT_TC2:-180}" ;;
    at2_v6.0*.sh) echo "${HC_TIMEOUT_AT2:-180}" ;;
    *) echo "$T" ;;
  esac
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
    s="$(per_timeout "$f")"
    run_with_timeout "$s" bash "$f" >"$tmpo" 2>&1
    rc=$?
    cat "$tmpo" | tee -a "$LOG"

    res_line="$(grep -E 'RESULT[:=](PASS|FAIL)' "$tmpo" | tail -n1)"
    label=""
    verdict=""
    if [ -n "$res_line" ]; then
      label="$(echo "$res_line" | awk '{print $1}')"
      verdict="$(echo "$res_line" | grep -Eo '(PASS|FAIL)' | tail -n1)"
    fi
    [ -n "$label" ] || label="$(basename "$f")"
    if [ -z "$verdict" ]; then
      if [ $rc -gt 128 ]; then verdict="TIMEOUT"; else verdict="UNKNOWN"; fi
    fi
    printf "%s RESULT=%s\n" "$label" "$verdict" >> "$SUM"

    if [ $rc -ne 0 ]; then
      echo "--> $(ts) FAIL $f (rc=$rc)" | tee -a "$LOG"
      fail=1
    else
      echo "--> $(ts) PASS $f" | tee -a "$LOG"
    fi
    rm -f "$tmpo"
  done
}

run_block "tools/checks/ee*_v6.0*.sh"
run_block "tools/fixtures/fix*_v6.0*.sh"
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
