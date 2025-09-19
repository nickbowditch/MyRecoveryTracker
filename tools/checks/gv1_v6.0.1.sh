#!/bin/bash
S=0
for d in tools/checks tools/fixtures tools/env evidence/v6.0 app/locks; do
  [ -d "$d" ] || { echo "GV-1 RESULT=FAIL (missing dir $d)"; exit 1; }
done
echo "GV-1 RESULT=PASS"
exit 0
