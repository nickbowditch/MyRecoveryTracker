#!/bin/bash
PKG="com.nick.myrecoverytracker"
T="${HC_TIMEOUT:-30}"
LOG="evidence/v6.0/unlocks/health_unlocks.2.txt"

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

choose_latest() {
  ls tools/checks/*_v6.0*.sh 2>/dev/null | awk '
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

fail=0

for f in $(choose_latest | sort); do
  case "$f" in
    tools/checks/ee*_v6.0*.sh|tools/checks/tc*_v6.0*.sh|tools/checks/di*_v6.0*.sh|tools/checks/at*_v6.0*.sh|tools/checks/gv*_v6.0*.sh)
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
    ;;
  esac
done

if [ "$fail" -eq 0 ]; then
  echo "UNLOCKS-HEALTH RESULT=PASS" | tee -a "$LOG"
  exit 0
else
  echo "UNLOCKS-HEALTH RESULT=FAIL" | tee -a "$LOG"
  exit 1
fi
