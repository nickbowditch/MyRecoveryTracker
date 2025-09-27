#!/bin/sh
set -eu

latest_by_triplet() {
  dir="$1"; prefix="$2"; suffix="$3"
  find "$dir" -maxdepth 1 -type f -name "${prefix}_v*.${suffix}" -print |
  awk -v pfx="${prefix}_v" -v sfx=".${suffix}" '
  function ver_score(v,  a,b,c) {
    if (split(v, a, /\./) == 3) { b=a[1]+0; c=a[2]+0; d=a[3]+0; return b*1000000 + c*1000 + d }
    if (split(v, a, /\./) == 2) { b=a[1]+0; c=a[2]+0; return b*1000000 + c*1000 }
    return 0
  }
  {
    f=$0
    g=f
    sub(/^.*_v/, "", g)
    sub(/\.[^.]*$/, "", g)
    score=ver_score(g)
    if(score>=best){best=score;bestf=f}
  }
  END{ if(bestf!="") print bestf; else exit 1 }
  '
}

run_latest_sleep_di3() {
  f="$(latest_by_triplet "tools/checks" "sleep_di3" "sh")"
  exec "$f"
}

run_latest_notif_engagement_at2() {
  f="$(latest_by_triplet "tools/checks" "notif_engagement_at2" "sh")"
  exec "$f"
}

case "${1:-}" in
  sleep_di3) run_latest_sleep_di3 ;;
  notif_at2) run_latest_notif_engagement_at2 ;;
  *) echo "USAGE: $0 {sleep_di3|notif_at2}" >&2; exit 2 ;;
esac
