#!/bin/sh
set -eu
OUT="evidence/v6.0/usage_events_daily/gv1.txt"

REQ="tools/checks tools/fixtures tools/env evidence/v6.0 app/locks"
MISS=""
for p in $REQ; do [ -d "$p" ] || MISS="$MISS $p"; done

NE=""
if [ -d tools/checks ]; then
for f in $(find tools/checks -maxdepth 1 -type f -name '*.sh' 2>/dev/null | sort); do
[ -x "$f" ] || NE="$NE $f"
done
fi

if [ -n "$MISS" ] || [ -n "$NE" ]; then
msg="GV1 RESULT=FAIL"
[ -n "$MISS" ] && msg="$msg missing:$MISS"
[ -n "$NE" ] && msg="$msg non-exec:$NE"
echo "$msg" | tee "$OUT"
exit 1
fi

echo "GV1 RESULT=PASS" | tee "$OUT"
exit 0
