#!/bin/sh
OUT="evidence/v6.0/_repo/git_tag_verify.1.txt"

mkdir -p "$(dirname "$OUT")"
: > "$OUT"
log() { printf '%s\n' "$*" | tee -a "$OUT" >/dev/null; }

TAG="${1:-qa-pass/unlocks-sleep-v6.0}"
log "TAG:$TAG"

if git rev-parse -q --verify "refs/tags/$TAG" >/dev/null 2>&1; then
  log "LOCAL_TAG:present"
else
  log "LOCAL_TAG:absent"
  log "GIT-TAG-VERIFY RESULT=FAIL"
  exit 1
fi

LOCAL_OBJ="$(git rev-parse -q "$TAG^{tag}" 2>/dev/null || true)"
LOCAL_PEELED="$(git rev-parse -q "$TAG^{}" 2>/dev/null || true)"
[ -n "$LOCAL_PEELED" ] || LOCAL_PEELED="$(git rev-parse -q "$TAG" 2>/dev/null || true)"

REMOTE_OBJ="$(git ls-remote --tags origin "refs/tags/$TAG" 2>/dev/null | awk '{print $1}' | head -n1)"
REMOTE_PEELED="$(git ls-remote --tags origin "refs/tags/$TAG^{}" 2>/dev/null | awk '{print $1}' | head -n1)"
if [ -n "$REMOTE_OBJ" ]; then
  log "REMOTE_TAG:present"
else
  log "REMOTE_TAG:absent"
  log "GIT-TAG-VERIFY RESULT=FAIL"
  exit 1
fi
[ -n "$REMOTE_PEELED" ] || REMOTE_PEELED="$REMOTE_OBJ"

log "LOCAL_OBJ:${LOCAL_OBJ:--}"
log "LOCAL_PEELED:${LOCAL_PEELED:--}"
log "REMOTE_OBJ:${REMOTE_OBJ:--}"
log "REMOTE_PEELED:${REMOTE_PEELED:--}"

if [ -n "$LOCAL_PEELED" ] && [ -n "$REMOTE_PEELED" ] && [ "$LOCAL_PEELED" = "$REMOTE_PEELED" ]; then
  log "MATCH:1"
  log "GIT-TAG-VERIFY RESULT=PASS"
  exit 0
fi

log "MATCH:0"
log "GIT-TAG-VERIFY RESULT=FAIL"
exit 1
