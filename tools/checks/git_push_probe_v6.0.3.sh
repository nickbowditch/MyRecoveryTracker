#!/bin/bash
OUT="evidence/v6.0/_repo/git_push_probe.3.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

echo "REMOTE_BEGIN"
git remote -v
git remote show origin || true
echo "REMOTE_END"

echo "FETCH_BEGIN"
git fetch -v origin && echo "FETCH_OK" || echo "FETCH_FAIL"
echo "FETCH_END"

echo "DIVERGENCE_BEGIN"
git rev-list --left-right --count origin/main...HEAD || true
echo "DIVERGENCE_END"

ts=$(date +%s)
tmp_branch="ci/probe-$ts"
tmp_tag="probe-tag-$ts"

echo "PUSH_TMP_BRANCH_BEGIN"
git push -v origin HEAD:refs/heads/$tmp_branch && echo "PUSH_TMP_BRANCH_OK" || echo "PUSH_TMP_BRANCH_FAIL"
echo "PUSH_TMP_BRANCH_END"

echo "DEL_TMP_BRANCH_BEGIN"
git push -v origin :refs/heads/$tmp_branch >/dev/null 2>&1 || true
echo "DEL_TMP_BRANCH_END"

echo "PUSH_TMP_TAG_BEGIN"
git tag -f "$tmp_tag" >/dev/null 2>&1 || true
git push -v origin refs/tags/$tmp_tag && echo "PUSH_TMP_TAG_OK" || echo "PUSH_TMP_TAG_FAIL"
echo "PUSH_TMP_TAG_END"

echo "DEL_TMP_TAG_BEGIN"
git push -v origin :refs/tags/$tmp_tag >/dev/null 2>&1 || true
git tag -d "$tmp_tag" >/dev/null 2>&1 || true
echo "DEL_TMP_TAG_END"

echo "PUSH_MAIN_BEGIN"
git push -v origin HEAD:refs/heads/main && echo "PUSH_MAIN_OK" || echo "PUSH_MAIN_FAIL"
echo "PUSH_MAIN_END"

probe_branch_ok=$(grep -c 'PUSH_TMP_BRANCH_OK' "$OUT" || true)
probe_tag_ok=$(grep -c 'PUSH_TMP_TAG_OK' "$OUT" || true)
push_main_ok=$(grep -c 'PUSH_MAIN_OK' "$OUT" || true)

if [ "$probe_branch_ok" -eq 1 ] && [ "$probe_tag_ok" -eq 1 ] && [ "$push_main_ok" -eq 1 ]; then
  echo "GIT-PUSH-PROBE RESULT=PASS"
  exit 0
fi

if [ "$probe_branch_ok" -eq 1 ] && [ "$probe_tag_ok" -eq 1 ] && [ "$push_main_ok" -eq 0 ]; then
  echo "MAIN_PROTECTED=1"
  echo "GIT-PUSH-PROBE RESULT=FAIL"
  exit 1
fi

echo "GIT-PUSH-PROBE RESULT=FAIL"
exit 1
