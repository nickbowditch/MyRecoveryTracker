#!/bin/sh
set -eu
OUT="evidence/v6.0/lnsu/gv1.txt"
REQ="tools/checks tools/fixtures tools/env evidence/v6.0 app/locks"
MISS=""
for p in $REQ; do [ -d "$p" ] || MISS="$MISS $p"; done
if [ -n "$MISS" ]; then echo "GV1 RESULT=FAIL missing:$MISS" | tee "$OUT"; exit 1; fi
echo "GV1 RESULT=PASS" | tee "$OUT"
