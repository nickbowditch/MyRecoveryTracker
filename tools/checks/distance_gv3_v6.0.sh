#!/bin/bash
set -euo pipefail
OUT_DEF="evidence/v6.0/distance/gv3.txt"
OUT="$OUT_DEF"
mkdir -p "$(dirname "$OUT")"
fail(){ echo "GV3 RESULT=FAIL ($1)"|tee "$OUT";echo "--- DEBUG: target_version=$TARGET_VERSION ---"|tee -a "$OUT";echo "--- DEBUG: script version summary ---"|tee -a "$OUT";cat "$TMP_SUMMARY" 2>/dev/null|tee -a "$OUT"||true;echo "--- DEBUG: latest evidence ---"|tee -a "$OUT";echo "latest_any=${LATEST_ANY:-<none>}"|tee -a "$OUT";echo "latest_target=${LATEST_TGT:-<none>}"|tee -a "$OUT";rm -f "$TMP_LIST" "$TMP_SUMMARY" 2>/dev/null||true;exit 1;}
TARGET_VERSION="${QA_VERSION:-}"
if [ -z "$TARGET_VERSION" ]&&[ -f "evidence/VERSION" ];then TARGET_VERSION="$(sed -n '1s/[[:space:]]//gp' evidence/VERSION|tr -d '\r'||true)";fi
if [ -z "$TARGET_VERSION" ];then TARGET_VERSION="$(grep -RIEho 'qaVersion[[:space:]]*[:=][[:space:]]*"v[0-9]+\.[0-9]+(\.[0-9]+)?' app/ build.gradle* settings.gradle* settings.gradle.kts gradle/ buildSrc/ 2>/dev/null|head -n1|sed -E 's/.*"([^"]+)".*/\1/'||true)";fi
[ -n "$TARGET_VERSION" ]||TARGET_VERSION="v6.0"
OUT="evidence/${TARGET_VERSION}/distance/gv3.txt"
mkdir -p "$(dirname "$OUT")"
TMP_LIST="$(mktemp)"
TMP_SUMMARY="$(mktemp)"
find tools/checks -type f -name 'distance_*.sh' 2>/dev/null|sort>"$TMP_LIST"||true
TOTAL="$(wc -l<"$TMP_LIST"|tr -d '[:space:]')"
[ "${TOTAL:-0}" -gt 0 ]||fail "no distance scripts found"
norm_mm(){ printf "%s" "$1"|sed -E 's/^v([0-9]+)\.([0-9]+).*$/\1.\2/';}
compat_mm(){ local tmm="$1" dmm="$2"; [ "$tmm" = "$dmm" ] && echo OK || echo NO; }
file_ver(){ local b v; b="$(basename "$1")"; v="$(printf "%s" "$b"|sed -nE 's/.*_v([0-9]+(\.[0-9]+){1,2})\.sh$/v\1/p')"; [ -n "$v" ]&&{ echo "$v"; return; }; v="$(printf "%s" "$b"|sed -nE 's/.*-(v[0-9]+(\.[0-9]+){1,2})\.sh$/\1/p')"; [ -n "$v" ]&&{ echo "$v"; return; }; echo ""; }
compat_dec(){ local tgt="$1" dec="$2"; if printf "%s" "$dec"|grep -qE '\.x$'; then [ "$(norm_mm "$tgt")" = "$(printf "%s" "$dec"|sed -E 's/^v?([0-9]+)\.([0-9]+)\.x$/\1.\2/')" ]&&echo OK||echo NO; else compat_mm "$(norm_mm "$tgt")" "$(norm_mm "$dec")"; fi; }
BAD=0
{ echo "SCRIPT,DECLARED,FROM,TYPE,COMPAT"; while IFS= read -r f; do
  decl_line="$(grep -E '^(VERSION_TARGET|COMPAT_RANGE)=' "$f" 2>/dev/null | head -n1 || true)"
  if [ -n "$decl_line" ]; then
    key="${decl_line%%=*}"; val="${decl_line#*=}"; val="$(printf "%s" "$val"|sed -E 's/^["'"'"']?|["'"'"']?$//g')"
    if [ -z "$val" ]; then echo "$f,<empty>,decl,missing,NO"; BAD=$((BAD+1)); continue; fi
    case "$key" in
      VERSION_TARGET) comp="$(compat_dec "$TARGET_VERSION" "$val")"; [ "$comp" = "OK" ] || BAD=$((BAD+1)); echo "$f,$val,decl,exact,$comp" ;;
      COMPAT_RANGE)   if printf "%s" "$val"|grep -qE '\.x$'; then comp="$(compat_dec "$TARGET_VERSION" "$val")"; [ "$comp" = "OK" ] || BAD=$((BAD+1)); echo "$f,$val,decl,range,$comp"; else if printf "%s" "$TARGET_VERSION"|grep -qE "$val"; then echo "$f,$val,decl,regex,OK"; else echo "$f,$val,decl,regex,NO"; BAD=$((BAD+1)); fi; fi ;;
      *) echo "$f,$val,decl,unknown,NO"; BAD=$((BAD+1));;
    esac
  else
    fv="$(file_ver "$f")"
    if [ -n "$fv" ]; then
      comp="$(compat_dec "$TARGET_VERSION" "$fv")"; [ "$comp" = "OK" ] || BAD=$((BAD+1)); echo "$f,$fv,filename,exact,$comp"
    else
      echo "$f,<generic>,generic,none,OK"
    fi
  fi
done<"$TMP_LIST"; } > "$TMP_SUMMARY"
LATEST_ANY="$(ls -t evidence/*/distance/*.txt 2>/dev/null|head -n1||true)"
LATEST_TGT="$(ls -t "evidence/${TARGET_VERSION}/distance/"*.txt 2>/dev/null|head -n1||true)"
[ -n "${LATEST_TGT// /}" ]||fail "no evidence under evidence/${TARGET_VERSION}/distance"
{ echo "GV3 CHECK: Version matched"; echo "target_version=$TARGET_VERSION"; echo "scripts_total=$TOTAL"; echo "incompatible_or_missing=$BAD"; echo "latest_any=${LATEST_ANY:-<none>}"; echo "latest_target=${LATEST_TGT:-<none>}"; echo "--- SCRIPT MATRIX ---"; cat "$TMP_SUMMARY"; } | tee "$OUT" >/dev/null
case "$LATEST_ANY" in "") : ;; evidence/${TARGET_VERSION}/distance/*) : ;; *) fail "most recent evidence not in ${TARGET_VERSION}" ;; esac
[ "$BAD" -eq 0 ]||fail "found $BAD script(s) without compatible version"
echo "GV3 RESULT=PASS" | tee -a "$OUT"
rm -f "$TMP_LIST" "$TMP_SUMMARY" 2>/dev/null || true
exit 0
