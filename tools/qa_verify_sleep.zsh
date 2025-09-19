#!/usr/bin/env zsh
PKG="${1:-com.nick.myrecoverytracker}"
S=0
tools/qa_schema_sleep.zsh "$PKG"   >/tmp/qa_schema_sleep.out   2>&1 || S=1
grep -q '^SCHEMA: OK$'              /tmp/qa_schema_sleep.out        || S=1
tools/qa_time_sleep.zsh "$PKG"     >/tmp/qa_time_sleep.out    2>&1 || S=1
grep -q '^PASS: sleep time-correctness$' /tmp/qa_time_sleep.out     || S=1
tools/qa_consistency_sleep.zsh "$PKG" >/tmp/qa_consistency.out 2>&1 || true
grep -q '^CONSISTENCY: OK$'         /tmp/qa_consistency.out         || S=1
tools/qa_dst_sleep.zsh "$PKG"      >/tmp/qa_dst_sleep.out     2>&1 || true
grep -Eq '^(DST: OK|DST: SKIP)'     /tmp/qa_dst_sleep.out           || S=1
tools/qa_guardrails_sleep.zsh "$PKG" >/tmp/qa_guardrails.out   2>&1 || S=1
grep -q '^GUARDRAILS: OK$'          /tmp/qa_guardrails.out          || S=1
[ $S -eq 0 ] && echo "VERIFY: OK" || { echo "VERIFY: FAIL"; cat /tmp/qa_schema_sleep.out /tmp/qa_time_sleep.out /tmp/qa_consistency.out /tmp/qa_dst_sleep.out /tmp/qa_guardrails.out; }
exit $S
