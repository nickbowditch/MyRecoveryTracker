#!/bin/bash
PKG="com.nick.myrecoverytracker"
N="${N:-7}"
ALLOW="${ALLOW_GAPS:-}"  # comma-separated YYYY-MM-DD list
CSV="$(adb exec-out run-as "$PKG" cat files/daily_sleep_summary.csv 2>/dev/null || printf "")"
NOW="$(adb shell 'date +%s' | tr -d '\r')"

miss=""
i=0
while [ $i -lt $N ]; do
  E=$(( NOW - 86400*i ))
  D="$(adb shell "toybox date -d '@$E' +%F" | tr -d '\r')"
  case ",$ALLOW," in
    *,"$D",*) : ;;
    *)
      present="$(printf "%s\n" "$CSV" | awk -F, -v d="$D" 'NR>1&&$1==d{f=1} END{print (f?1:0)}')"
      [ "$present" -eq 1 ] || miss="${miss}${D}\n"
      ;;
  esac
  i=$((i+1))
done

if [ -z "$miss" ]; then
  echo "Sleep DI-4 RESULT=PASS"
  exit 0
else
  echo "Sleep DI-4 RESULT=FAIL"
  printf "%b" "$miss"
  exit 1
fi
