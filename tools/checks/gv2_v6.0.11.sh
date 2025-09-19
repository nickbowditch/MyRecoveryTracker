#!/bin/bash
./gradlew -q :app:tasks --all | grep -qE '(^|:)qaCheck\b' || { echo "GV-2 RESULT=FAIL (no qaCheck task)"; exit 1; }

tmp="$(mktemp)"; trap 'rm -f "$tmp"' EXIT
QA_TIMEOUT=90 ./gradlew :app:qaCheck --console=plain >"$tmp" 2>&1 || true

ok=1
shopt -s nullglob
for f in tools/checks/*_v6.0*.sh; do
  grep -Fq "==> $f" "$tmp" || { echo "MISSING:$f"; ok=0; }
done

if [ "$ok" -eq 1 ]; then
  echo "GV-2 RESULT=PASS"
  exit 0
else
  echo "GV-2 RESULT=FAIL"
  exit 2
fi
