#!/usr/bin/env bash
set -euo pipefail
./gradlew :app:qaSealMetrics --no-daemon --console=plain
