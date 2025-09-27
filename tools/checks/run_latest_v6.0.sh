#!/bin/sh
set -eu
TIMEOUT="${TIMEOUT:-60}"
FEATURE_FILTER="${FEATURE:-}"

set +f
SCRIPTS=$(printf '%s\n' tools/checks/*_v6.0.*.sh 2>/dev/null | grep -E '\.sh$' || true)
set -f
[ -n "$SCRIPTS" ] || { echo "No versioned v6.0 scripts found"; exit 2; }

LATEST=$(
  printf '%s\n' "$SCRIPTS" \
  | awk '
      function fam(p){ sub(/_v6\.0\..*\.sh$/,"",p); return p }
      { f=fam($0); print f "\t" $0 }
    ' \
  | sort -k1,1 -k2,2V \
  | awk -F '\t' '{ last[$1]=$2 } END{ for (k in last) print last[k] }'
)

if [ -n "$FEATURE_FILTER" ]; then
  LATEST=$(printf '%s\n' "$LATEST" | grep -i "$FEATURE_FILTER" || true)
fi
[ -n "$LATEST" ] || { echo "No matching scripts"; exit 3; }

RUN_LOG="evidence/v6.0/_runner/latest.run.txt"
mkdir -p "$(dirname "$RUN_LOG")"
printf '%s\n' "$LATEST" > "$RUN_LOG"

if [ "${DRYRUN:-0}" = "1" ]; then
  cat "$RUN_LOG"
  exit 0
fi

rc=0
printf '%s\n' "$LATEST" | while IFS= read -r script; do
  if command -v timeout >/dev/null 2>&1; then
    timeout "${TIMEOUT}s" sh -c "\"$script\""
  else
    "$script"
  fi
  r=$?
  [ $r -eq 0 ] || rc=$r
done | tee -a "$RUN_LOG"

exit ${rc:-0}
