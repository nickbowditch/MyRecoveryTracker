#!/bin/bash
OUT="evidence/v6.0/_repo/run_git_push_suite.1.txt"
rc=0
{
  echo "RUN:PRE_PUSH_GUARD"; tools/checks/git_pre_push_guard_v6.0.2.sh || rc=1
  if [ "$rc" -eq 0 ]; then
    echo "RUN:PUSH_MAIN_AND_TAG"; tools/checks/git_push_main_and_tag_v6.0.2.sh || rc=1
  fi
} | tee "$OUT"
exit $rc
