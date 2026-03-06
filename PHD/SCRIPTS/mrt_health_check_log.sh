#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$HOME/PHD/QA/Test72h"
mkdir -p "$LOG_DIR"

STAMP="$(date '+%Y-%m-%d_%H-%M-%S')"
echo "=== mrt_health_check.sh @ $STAMP ===" | tee -a "$LOG_DIR/72h_health_log.txt"
"$ROOT/tools/mrt_health_check.sh" | tee -a "$LOG_DIR/72h_health_log.txt"
echo >> "$LOG_DIR/72h_health_log.txt"
