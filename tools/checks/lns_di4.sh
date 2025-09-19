#!/usr/bin/env bash
PKG="com.nick.myrecoverytracker"
F="files/daily_late_screen.csv"
ALLOW_CSV="${ALLOW_GAPS:-}"

allow_hit() {
  local d="$1"; local list="$2"
  [ -z "$list" ] && return 1
  IFS=',' read -r -a arr <<< "$list"
  for g in "${arr[@]}"; do [ "$g" = "$d" ] && return 0; done
  return 1
}

E_NOW="$(adb shell 'toybox date +%s' | tr -d '\r')"
MISSING=()
for i in $(seq 0 6); do
  E=$(( E_NOW - 86400*i ))
  D="$(adb shell "toybox date -d '@$E' +%F" | tr -d '\r')"
  if allow_hit "$D" "$ALLOW_CSV"; then continue; fi
  HIT="$(adb exec-out run-as "$PKG" awk -F, -v dd="$D" 'NR>1&&$1==dd{f=1} END{print (f?1:0)}' "$F" 2>/dev/null)"
  [ "$HIT" = "1" ] || MISSING+=("$D")
done

if [ ${#MISSING[@]} -eq 0 ]; then
  echo "LNS DI-4 RESULT=PASS"
  exit 0
else
  echo "LNS DI-4 RESULT=FAIL (missing: ${MISSING[*]})"
  exit 1
fi
