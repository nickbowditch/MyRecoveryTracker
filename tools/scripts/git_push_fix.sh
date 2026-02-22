#!/bin/sh
set -euo pipefail

BRANCH="$(git rev-parse --abbrev-ref HEAD)"
REMOTE="origin"

git fetch --prune "$REMOTE"
git branch --set-upstream-to="$REMOTE/$BRANCH" "$BRANCH" 2>/dev/null || true
git rebase "$REMOTE/$BRANCH" || git rebase --abort
git push "$REMOTE" "HEAD:refs/heads/$BRANCH" || {
  git pull --rebase "$REMOTE" "$BRANCH" || true
  git push --force-with-lease "$REMOTE" "HEAD:refs/heads/$BRANCH"
}
git push "$REMOTE" --tags || true

echo "✅ Synced branch + tags: $BRANCH"
