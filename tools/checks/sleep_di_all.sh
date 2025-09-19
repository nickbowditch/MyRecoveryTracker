#!/usr/bin/env bash
S=0
tools/checks/sleep_di1.sh || S=1
tools/checks/sleep_di2.sh || S=1
tools/checks/sleep_di3.sh || S=1
tools/checks/sleep_di4.sh || S=1
tools/checks/sleep_di5.sh || S=1
if [ $S -eq 0 ]; then
  echo "Sleep DI RESULT=PASS (DI-1..DI-5)"
  exit 0
else
  echo "Sleep DI RESULT=FAIL"
  exit 1
fi
