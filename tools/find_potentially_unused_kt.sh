#!/usr/bin/env bash
set -eu

ROOT="app/src/main"
JAVA_DIR="$ROOT/java/com/nick/myrecoverytracker"
MAIN_MANIFEST="$ROOT/AndroidManifest.xml"
MERGED_MANIFEST="app/build/intermediates/merged_manifests/debug/AndroidManifest.xml"

echo "ClassFile,RefsInKt,RefsInManifest"

find "$JAVA_DIR" -type f -name '*.kt' | sort | while read -r file; do
  base="$(basename "$file" .kt)"

  kt_refs="$(grep -R -n "\b$base\b" "$JAVA_DIR" \
    --exclude="$(basename "$file")" 2>/dev/null || true | wc -l | tr -d ' ')"

  man_refs=0
  if [[ -f "$MAIN_MANIFEST" ]]; then
    man_refs=$(( man_refs + $(grep -n "$base" "$MAIN_MANIFEST" 2>/dev/null | wc -l | tr -d ' ') ))
  fi
  if [[ -f "$MERGED_MANIFEST" ]]; then
    man_refs=$(( man_refs + $(grep -n "$base" "$MERGED_MANIFEST" 2>/dev/null | wc -l | tr -d ' ') ))
  fi

  echo "$file,$kt_refs,$man_refs"
done
