#!/bin/sh
set -eu
OUT="evidence/v6.0/notification_engagement/at2.run.txt"
mkdir -p "$(dirname "$OUT")"
exec tools/checks/notif_engagement_at2_v6.0.5.sh | tee "$OUT"
