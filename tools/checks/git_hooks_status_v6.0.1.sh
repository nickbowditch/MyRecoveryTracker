#!/bin/bash
OUT="evidence/v6.0/_repo/git_hooks_status.1.txt"
exec > >(tee "$OUT") 2>&1

echo "HOOKS_DIR:.git/hooks"
if [ -f ".git/hooks/pre-push" ]; then
  if [ -x ".git/hooks/pre-push" ]; then
    echo "PRE_PUSH:present+executable"
  else
    echo "PRE_PUSH:present+not-exec"
  fi
else
  echo "PRE_PUSH:absent"
fi

hp="$(git config --get core.hooksPath || echo unset)"
echo "CONFIG_hooksPath:$hp"

echo "GIT-HOOKS-STATUS RESULT=PASS"
exit 0
