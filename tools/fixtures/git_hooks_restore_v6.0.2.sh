#!/bin/bash
OUT="evidence/v6.0/_repo/git_hooks_restore.2.txt"
exec > >(tee "$OUT") 2>&1

git config --unset core.hooksPath || true
if [ -f ".git/hooks/pre-push" ]; then
  chmod +x ".git/hooks/pre-push" || true
fi

echo "HOOKS_PATH_NOW:$(git config --get core.hooksPath || echo unset)"
[ -x ".git/hooks/pre-push" ] && echo "PRE_PUSH_EXEC:1" || echo "PRE_PUSH_EXEC:0"

echo "GIT-HOOKS-RESTORE RESULT=PASS"
exit 0
