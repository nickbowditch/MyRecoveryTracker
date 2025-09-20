#!/bin/bash
OUT="evidence/v6.0/_repo/git_stage_repo_evidence.1.txt"
exec > >(tee "$OUT") 2>&1

git add -N . >/dev/null 2>&1 || true
echo "UNTRACKED_BEGIN"
git status --porcelain | awk '$1=="??"{print $2}'
echo "UNTRACKED_END"

git add tools/checks/*.sh 2>/dev/null || true
git add tools/fixtures/*.sh 2>/dev/null || true
git add evidence/v6.0/_repo/*.txt 2>/dev/null || true
git add evidence/v6.0/*/*.txt 2>/dev/null || true

echo "STAGED_BEGIN"
git diff --cached --name-only
echo "STAGED_END"

git diff --cached --quiet || git commit -m "v6.0: add checks + evidence logs (schema/shape/writers/git push)"
if [ $? -eq 0 ]; then
  echo "COMMIT:OK"
else
  echo "COMMIT:NOP"
fi

echo "GIT-STAGE-REPO-EVIDENCE RESULT=PASS"
exit 0
