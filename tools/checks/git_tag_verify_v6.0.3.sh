#!/bin/sh
OUT="evidence/v6.0/_repo/git_tag_verify.3.txt"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"
say(){ printf '%s\n' "$*" | tee -a "$OUT"; }

TAG="${1:-qa-pass/unlocks-sleep-v6.0}"
say "TAG:$TAG"

if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null 2>&1; then
  say "LOCAL_TAG:present"
else
  say "LOCAL_TAG:absent"
  say "GIT-TAG-VERIFY RESULT=FAIL"
  exit 1
fi

LOCAL_OBJ="$(git rev-parse -q "$TAG^{tag}" 2>/dev/null || true)"
LOCAL_PEELED="$(git rev-parse -q "$TAG^{}" 2>/dev/null || true)"
[ -n "$LOCAL_PEELED" ] || LOCAL_PEELED="$(git rev-parse -q "$TAG" 2>/dev/null || true)"

REMOTE_OBJ="$(git ls-remote --tags origin "refs/tags/$TAG" 2>/dev/null | awk '{print $1}' | head -n1)"
REMOTE_PEELED="$(git ls-remote --tags origin "refs/tags/$TAG^{}" 2>/dev/null | awk '{print $1}' | head -n1)"
if [ -n "$REMOTE_OBJ" ]; then
  say "REMOTE_TAG:present"
else
  say "REMOTE_TAG:absent"
  say "GIT-TAG-VERIFY RESULT=FAIL"
  exit 1
fi
[ -n "$REMOTE_PEELED" ] || REMOTE_PEELED="$REMOTE_OBJ"

say "LOCAL_OBJ:${LOCAL_OBJ:--}"
say "LOCAL_PEELED:${LOCAL_PEELED:--}"
say "REMOTE_OBJ:${REMOTE_OBJ:--}"
say "REMOTE_PEELED:${REMOTE_PEELED:--}"

if [ -n "$LOCAL_PEELED" ] && [ -n "$REMOTE_PEELED" ] && [ "$LOCAL_PEELED" = "$REMOTE_PEELED" ]; then
  say "MATCH:1"
  say "GIT-TAG-VERIFY RESULT=PASS"
  exit 0
fi

say "MATCH:0"
say "GIT-TAG-VERIFY RESULT=FAIL"
exit 1
