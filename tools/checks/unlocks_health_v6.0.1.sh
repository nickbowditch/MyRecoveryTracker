#!/bin/bash
set -e
dts="$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
p=0
tools/checks/ee1_v6.0.sh    | tee -a evidence/v6.0/unlocks/ee1.latest.txt || p=1
tools/checks/ee2_v6.0.1.sh  | tee -a evidence/v6.0/unlocks/ee2.latest.txt || p=1
tools/checks/ee3_v6.0.1.sh  | tee -a evidence/v6.0/unlocks/ee3.latest.txt || p=1
echo "$dts UNLOCKS_HEALTH: $([ $p -eq 0 ] && echo PASS || echo FAIL)"
exit $p
