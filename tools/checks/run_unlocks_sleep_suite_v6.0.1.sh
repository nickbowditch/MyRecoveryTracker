#!/bin/bash
OUT="evidence/v6.0/_repo/run_suite.1.txt"
rc=0
{
  echo "RUN:SCH_GUARD"; tools/checks/schema_guard_v6.0.2.sh || rc=1
  echo "RUN:SHAPE_GUARD"; tools/checks/shape_guard_v6.0.1.sh || rc=1
  echo "RUN:WRITERS_ALLOW"; tools/checks/writers_allowlist_v6.0.1.sh || rc=1
} | tee "$OUT"
exit $rc
