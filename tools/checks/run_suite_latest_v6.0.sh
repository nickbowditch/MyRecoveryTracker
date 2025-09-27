#!/bin/sh
set -eu

TIMEOUT="${TIMEOUT:-60}"
FEATURE_FILTER="${FEATURE:-}"
OUT="evidence/v6.0/_runner/suite_latest.run.txt"

FEATURES="unlocks sleep lnsu notif_engagement notif_latency"
[ -n "$FEATURE_FILTER" ] && FEATURES="$(printf '%s\n' $FEATURES | grep -i "$FEATURE_FILTER" || true)"

list_latest_for_feature() {
  feat="$1"
  # portable: use find -print, then awk to derive check name and version
  find tools/checks -maxdepth 1 -type f -name "${feat}_*_v6.0.*.sh" -print 2>/dev/null \
  | awk -F/ '
      {
        path=$0; base=$NF
        n=split(base,parts,"_")
        if(n<3) next
        check=parts[2]
        print check "\t" base "\t" path
      }
    ' \
  | sort -t "$(printf '\t')" -k1,1 -k2,2V \
  | awk -F "$(printf '\t')" '{ latest[$1]=$3 } END { for (k in latest) print latest[k] }'
}

rank_script() {
  p="$1"
  b=$(basename "$p")
  feat="${b%%_*}"
  rest="${b#*_}"
  check="${rest%%_*}"

  case "$feat" in
    unlocks) FR=1 ;;
    sleep) FR=2 ;;
    lnsu) FR=3 ;;
    notif_engagement) FR=4 ;;
    notif_latency) FR=5 ;;
    *) FR=9 ;;
  esac

  prefix=$(printf '%s' "$check" | sed 's/[0-9].*$//')
  num=$(printf '%s' "$check" | sed 's/^[^0-9]*//')
  case "$prefix" in
    ee) CR=1 ;;
    tc) CR=2 ;;
    di) CR=3 ;;
    at) CR=4 ;;
    gv) CR=5 ;;
    *)  CR=9 ;;
  esac
  [ -n "$num" ] || num=9999
  printf '%d %d %04d\t%s\n' "$FR" "$CR" "$num" "$p"
}

PLAN=$(
  for f in $FEATURES; do
    list_latest_for_feature "$f"
  done | while IFS= read -r s; do
         [ -n "$s" ] && rank_script "$s"
       done | sort -k1,1n -k2,2n -k3,3n | cut -f2-
)

[ -n "$PLAN" ] || { echo "No matching scripts"; exit 3; }

mkdir -p "$(dirname "$OUT")"
printf '%s\n' "$PLAN" > "$OUT"

rc=0
printf '%s\n' "$PLAN" | while IFS= read -r script; do
  if command -v timeout >/dev/null 2>&1; then
    timeout "${TIMEOUT}s" sh "$script"
  else
    sh "$script"
  fi
  r=$?
  [ $r -eq 0 ] || rc=$r
done | tee -a "$OUT"

exit ${rc:-0}
