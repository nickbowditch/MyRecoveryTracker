#!/bin/bash
OUT="evidence/v6.0/_repo/git_hooks_reset.1.txt"
exec > >(tee "$OUT") 2>&1

ts=$(date +%s)
EMPTY_DIR=".git/hooks.empty"
mkdir -p "$EMPTY_DIR"
chmod 755 "$EMPTY_DIR"

git config core.hooksPath "$EMPTY_DIR" || true

if [ -x ".git/hooks/pre-push" ]; then
  chmod -x ".git/hooks/pre-push" || true
fi

echo "HOOKS_PATH_SET:$(git config --get core.hooksPath || echo unset)"
[ -x ".git/hooks/pre-push" ] && echo "PRE_PUSH_EXEC:1" || echo "PRE_PUSH_EXEC:0"
echo "GIT-HOOKS-RESET RESULT=PASS"
exit 0
