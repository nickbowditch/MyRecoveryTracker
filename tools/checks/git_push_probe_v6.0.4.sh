#!/bin/bash
OUT="evidence/v6.0/_repo/git_push_probe.4.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

echo "REMOTE_BEGIN"
git remote -v
git remote show origin || true
echo "REMOTE_END"

echo "FETCH_BEGIN"
git fetch -v origin && echo "FETCH_OK" || echo "FETCH_FAIL"
echo "FETCH_END"

echo "STATUS_BEGIN"
git status -sb || true
echo "STATUS_END"

echo "DIVERGENCE_BEGIN"
git rev-list --left-right --count origin/main...HEAD || true
echo "DIVERGENCE_END"

ts=$(date +%s)
tmp_branch="ci/probe-$ts"
tmp_tag="probe-tag-$ts"

echo "TRACE_ENV_BEGIN"
echo "GIT_TRACE=1"
echo "GIT_TRACE_PACKET=1"
echo "GIT_SSH_COMMAND=ssh -vvv"
echo "TRACE_ENV_END"

echo "PUSH_TMP_BRANCH_BEGIN"
GIT_TRACE=1 GIT_TRACE_PACKET=1 GIT_SSH_COMMAND="ssh -vvv" git push -v origin HEAD:refs/heads/$tmp_branch && echo "PUSH_TMP_BRANCH_OK" || echo "PUSH_TMP_BRANCH_FAIL"
echo "PUSH_TMP_BRANCH_END"

echo "DEL_TMP_BRANCH_BEGIN"
GIT_SSH_COMMAND="ssh -vvv" git push -v origin :refs/heads/$tmp_branch >/dev/null 2>&1 || true
echo "DEL_TMP_BRANCH_END"

echo "PUSH_TMP_TAG_BEGIN"
git tag -f "$tmp_tag" >/dev/null 2>&1 || true
GIT_TRACE=1 GIT_TRACE_PACKET=1 GIT_SSH_COMMAND="ssh -vvv" git push -v origin refs/tags/$tmp_tag && echo "PUSH_TMP_TAG_OK" || echo "PUSH_TMP_TAG_FAIL"
echo "PUSH_TMP_TAG_END"

echo "DEL_TMP_TAG_BEGIN"
GIT_SSH_COMMAND="ssh -vvv" git push -v origin :refs/tags/$tmp_tag >/dev/null 2>&1 || true
git tag -d "$tmp_tag" >/dev/null 2>&1 || true
echo "DEL_TMP_TAG_END"

echo "PUSH_MAIN_BEGIN"
GIT_TRACE=1 GIT_TRACE_PACKET=1 GIT_SSH_COMMAND="ssh -vvv" git push -v origin HEAD:refs/heads/main && echo "PUSH_MAIN_OK" || echo "PUSH_MAIN_FAIL"
echo "PUSH_MAIN_END"

echo "CONFIG_BEGIN"
git config --list --show-origin | grep -E 'sign|gpg|push|receive\.deny' || true
echo "CONFIG_END"

echo "HOOKS_BEGIN"
git config --get core.hooksPath || true
ls -la .git/hooks 2>/dev/null || true
echo "HOOKS_END"

echo "GIT-PUSH-PROBE RESULT=FAIL"
exit 1
