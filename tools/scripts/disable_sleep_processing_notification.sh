#!/bin/sh
set -eu

OUT="evidence/v6.0/_repo/remove_sleep_processing_notification.txt"
TEXT='Sleep processing active'
mkdir -p "$(dirname "$OUT")"

find . \( -name '*.kt' -o -name '*.java' -o -name '*.xml' \) -type f -print0 \
| xargs -0 grep -nH -I -e "$TEXT" 2>/dev/null | tee "$OUT" || true

find . -name 'strings.xml' -type f -print0 \
| xargs -0 grep -nH -I -e 'name="sleep_processing_active"' 2>/dev/null | tee -a "$OUT" || true

[ "${REMOVE:-0}" = "1" ] || exit 0

sed_inplace () {
  sed -i '' -E "$1" "$2" 2>/dev/null || sed -i -E "$1" "$2"
}

CODE_FILES="$(find . \( -name '*.kt' -o -name '*.java' \) -type f -print0 \
  | xargs -0 grep -Il -e "$TEXT" -e 'R\.string\.sleep_processing_active' 2>/dev/null || true)"

if [ -n "${CODE_FILES:-}" ]; then
  for f in $CODE_FILES; do
    cp "$f" "$f.bak" 2>/dev/null || true
    sed_inplace 's/(\.setContentText\()\s*"Sleep processing active"\s*(\))/\1""\2/g' "$f"
    sed_inplace 's/(\.setContentTitle\()\s*"Sleep processing active"\s*(\))/\1""\2/g' "$f"
    sed_inplace 's/(\.setContentText\()\s*getString\(\s*R\.string\.sleep_processing_active\s*\)\s*(\))/\1""\2/g' "$f"
    sed_inplace 's/(\.setContentTitle\()\s*getString\(\s*R\.string\.sleep_processing_active\s*\)\s*(\))/\1""\2/g' "$f"
    sed_inplace 's/\bIMPORTANCE_(HIGH|DEFAULT|LOW)\b/IMPORTANCE_MIN/g' "$f"
    sed_inplace 's/\.setOngoing\(\s*true\s*\)/.setOngoing(false)/g' "$f"
    sed_inplace 's/^([[:space:]]*)startForeground\(/\1\/\/startForeground(/g' "$f"
  done
fi

STRINGS_FILES="$(find . -name 'strings.xml' -type f 2>/dev/null || true)"
if [ -n "${STRINGS_FILES:-}" ]; then
  for xf in $STRINGS_FILES; do
    cp "$xf" "$xf.bak" 2>/dev/null || true
    sed_inplace 's#(<string[^>]*name="sleep_processing_active"[^>]*>)[^<]*(</string>)#\1\2#g' "$xf"
  done
fi

XML_FILES="$(find . -name '*.xml' -type f 2>/dev/null || true)"
if [ -n "${XML_FILES:-}" ]; then
  for xf in $XML_FILES; do
    cp "$xf" "$xf.bak" 2>/dev/null || true
    sed_inplace 's/(android:importance=")(high|default|low)"/\1min"/g' "$xf"
  done
fi

git add -A >/dev/null 2>&1 || true
git commit -m "Disable/neutralize 'Sleep processing active' notification" >/dev/null 2>&1 || true
