#!/bin/sh
set -eu
OUT="evidence/v6.0/notification_engagement/gv1.txt"

REQ="tools/checks tools/fixtures tools/env evidence/v6.0 app/locks"
MISS=""
for p in $REQ; do [ -d "$p" ] || MISS="$MISS $p"; done

NE=""
if ls tools/checks/.sh >/dev/null 2>&1; then
for f in tools/checks/.sh; do
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
