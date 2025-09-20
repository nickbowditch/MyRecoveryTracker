#!/bin/bash
OUT="evidence/v6.0/_repo/git_push_main_and_tag.1.txt"
exec > >(tee "$OUT") 2>&1

TAG="qa-pass/unlocks-sleep-v6.0"
HOOKS_EMPTY=".git/hooks.empty"
mkdir -p "$HOOKS_EMPTY"
git config core.hooksPath "$HOOKS_EMPTY" || true

echo "VERIFY_LOCAL_TAG_BEGIN"
if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null; then
  echo "LOCAL_TAG:present"
else
  git tag -a "$TAG" -m "Unlocks+Sleep: schema/shape/writers PASS (v6.0)" && echo "LOCAL_TAG:created" || echo "LOCAL_TAG:create_fail"
fi
echo "VERIFY_LOCAL_TAG_END"

echo "PUSH_MAIN_BEGIN"
git -c core.hooksPath="$HOOKS_EMPTY" push -v origin HEAD:refs/heads/main && echo "PUSH_MAIN_OK" || echo "PUSH_MAIN_FAIL"
echo "PUSH_MAIN_END"

echo "PUSH_TAG_BEGIN"
git -c core.hooksPath="$HOOKS_EMPTY" push -v origin refs/tags/"$TAG" && echo "PUSH_TAG_OK" || echo "PUSH_TAG_FAIL"
echo "PUSH_TAG_END"

echo "REMOTE_VERIFY_BEGIN"
git ls-remote --heads origin | grep -F -q "$(git rev-parse HEAD)" && echo "REMOTE_MAIN_HAS_HEAD"
git ls-remote --tags origin | grep -F -q "refs/tags/$TAG" && echo "REMOTE_HAS_TAG"
echo "REMOTE_VERIFY_END"

ok_main=$(grep -c 'PUSH_MAIN_OK' "$OUT" || true)
ok_tag=$(grep -c 'PUSH_TAG_OK' "$OUT" || true)
ok_tag_remote=$(grep -c 'REMOTE_HAS_TAG' "$OUT" || true)

if [ "$ok_main" -eq 1 ] && [ "$ok_tag" -eq 1 ] && [ "$ok_tag_remote" -eq 1 ]; then
  echo "GIT-PUSH-MAIN-AND-TAG RESULT=PASS"
  exit 0
else
  echo "GIT-PUSH-MAIN-AND-TAG RESULT=FAIL"
  exit 1
fi
