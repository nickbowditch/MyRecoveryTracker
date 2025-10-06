#!/bin/sh
set -eu
APP="com.nick.myrecoverytracker"
RAW="files/usage_events.csv"
DAILY="files/daily_usage_events.csv"
OUT="evidence/v6.0/usage_events/fixschema.txt"
mkdir -p "$(dirname "$OUT")"

fail(){ echo "FIXSCHEMA RESULT=FAIL $1" | tee "$OUT"; exit 1; }

adb get-state >/dev/null 2>&1 || fail "(no device)"
adb shell pm path "$APP" >/dev/null 2>&1 || fail "(app not installed)"

HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"

if [ "$HDR" = "date,time,event_type,package" ]; then
  {
    echo "RAW_HEADER_OK=date,time,event_type,package"
    echo "NO_MIGRATION_NEEDED"
  } | tee "$OUT" >/dev/null
else
  TMP="/data/data/$APP/cache/_ue_migrate.$$"
  adb exec-out run-as "$APP" sh -c '
set -eu
raw="'"$RAW"'"
tmp="'"$TMP"'"
[ -f "$raw" ] || { echo "no_raw"; exit 0; }
hdr="$(sed -n "1p" "$raw" | tr -d "\r")"
tail -n +2 "$raw" > "$tmp.body" || : 
{
  echo "date,time,event_type,package"
  awk -F, "
  function trim(s){gsub(/^[[:space:]]+|[[:space:]]+$/,\"\",s); return s}
  function to_dt(ts,   d,tm,sec,ms,cmd,out){
    ts=trim(ts)
    if(ts ~ /^[0-9]{4}-[0-9]{2}-[0-9]{2}[ T][0-9]{2}:[0-9]{2}:[0-9]{2}/){
      gsub(\"T\",\" \",ts)
      d=substr(ts,1,10); tm=substr(ts,12,8); print d \",\" tm; return
    }
    if(ts ~ /^[0-9]+$/){
      ms=ts+0
      if(length(ts)>10){ sec=int(ms/1000) } else { sec=ms }
      cmd=\"toybox date -d @\" sec \" +%F,%T\"
      cmd | getline out; close(cmd)
      if(out==\"\") out=\",,\"
      print out; return
    }
    print \",,\"
  }
  NR==1{next}
  {
    ts=$1; pkg=$2; ev=$3
    split(to_dt(ts), parts, /,/)
    d=parts[1]; tm=parts[2]
    ev=trim(ev); pkg=trim(pkg)
    if(d!=\"\" && tm!=\"\" && ev!=\"\" && pkg!=\"\"){
      print d \",\" tm \",\" ev \",\" pkg
    }
  }" "$tmp.body"
} > "$tmp.out"

mv "$tmp.out" "$raw"
rm -f "$tmp.body"
echo "migrated"
' | tr -d '\r' > /tmp/_ue_migrate_status.txt 2>/dev/null || true

  STAT="$(cat /tmp/_ue_migrate_status.txt 2>/dev/null || true)"
  NEW_HDR="$(adb exec-out run-as "$APP" sed -n '1p' "$RAW" 2>/dev/null | tr -d '\r' || true)"
  {
    echo "RAW_HEADER_BEFORE=${HDR:-MISSING}"
    echo "MIGRATION_STATUS=${STAT:-unknown}"
    echo "RAW_HEADER_AFTER=${NEW_HDR:-MISSING}"
  } | tee "$OUT" >/dev/null

  [ "$NEW_HDR" = "date,time,event_type,package" ] || fail "(migration did not produce golden header)"
fi

DHDR="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
if [ -n "$DHDR" ] && [ "$DHDR" != "date,event_count" ]; then
  adb exec-out run-as "$APP" sh -c '
set -eu
daily="'"$DAILY"'"
tmp="$daily.tmp"
echo "date,event_count" > "$tmp"
tail -n +2 "$daily" 2>/dev/null >> "$tmp" || :
mv "$tmp" "$daily"
' >/dev/null 2>&1 || true
fi

DHDR_NEW="$(adb exec-out run-as "$APP" sed -n '1p' "$DAILY" 2>/dev/null | tr -d '\r' || true)"
[ -z "$DHDR_NEW" ] || [ "$DHDR_NEW" = "date,event_count" ] || fail "(daily header not golden)"

echo "FIXSCHEMA RESULT=PASS" | tee -a "$OUT"
exit 0
