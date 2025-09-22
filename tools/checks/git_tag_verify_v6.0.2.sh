#!/bin/sh
OUT="evidence/v6.0/_repo/git_tag_verify.2.txt"
mkdir -p "$(dirname "$OUT")"
exec > >(tee "$OUT") 2>&1

TAG="${1:-qa-pass/unlocks-sleep-v6.0}"
echo "TAG:$TAG"

if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null 2>&1; then
  echo "LOCAL_TAG:present"
else
  echo "LOCAL_TAG:absent"
  echo "GIT-TAG-VERIFY RESULT=FAIL"
  exit 1
fi

LOCAL_OBJ="$(git rev-parse -q "$TAG^{tag}" 2>/dev/null || true)"
LOCAL_PEELED="$(git rev-parse -q "$TAG^{}" 2>/dev/null || true)"
[ -n "$LOCAL_PEELED" ] || LOCAL_PEELED="$(git rev-parse -q "$TAG" 2>/dev/null || true)"

REMOTE_OBJ="$(git ls-remote --tags origin "refs/tags/$TAG" 2>/dev/null | awk '{print $1}' | head -n1)"
REMOTE_PEELED="$(git ls-remote --tags origin "refs/tags/$TAG^{}" 2>/dev/null | awk '{print $1}' | head -n1)"
if [ -n "$REMOTE_OBJ" ]; then
  echo "REMOTE_TAG:present"
else
  echo "REMOTE_TAG:absent"
  echo "GIT-TAG-VERIFY RESULT=FAIL"
  exit 1
fi
[ -n "$REMOTE_PEELED" ] || REMOTE_PEELED="$REMOTE_OBJ"

echo "LOCAL_OBJ:${LOCAL_OBJ:--}"
echo "LOCAL_PEELED:${LOCAL_PEELED:--}"
echo "REMOTE_OBJ:${REMOTE_OBJ:--}"
echo "REMOTE_PEELED:${REMOTE_PEELED:--}"

if [ -n "$LOCAL_PEELED" ] && [ -n "$REMOTE_PEELED" ] && [ "$LOCAL_PEELED" = "$REMOTE_PEELED" ]; then
  echo "MATCH:1"
  echo "GIT-TAG-VERIFY RESULT=PASS"
  exit 0
fi

echo "MATCH:0"
echo "GIT-TAG-VERIFY RESULT=FAIL"
exit 1
