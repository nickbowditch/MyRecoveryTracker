#!/bin/bash
set -euo pipefail

PKG="com.nick.myrecoverytracker"
OUT="evidence/v6.0/_repo/csv_relevance.txt"
SRC_DIR="app/src"
LOCKS_DIR="app/locks"
mkdir -p "$(dirname "$OUT")"
: > "$OUT"

log(){ echo "$@" | tee -a "$OUT"; }

TARGETS="
files/daily_movement.csv
files/notifications.csv
files/redcap_queue.csv
files/app_switches.csv
files/notification_events.csv
files/notification_latency_log.csv
files/daily_usage_entropy.csv
"

adb get-state >/dev/null 2>&1 || { log "FAIL: no device"; exit 2; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { log "FAIL: app not installed"; exit 3; }

TODAY="$(adb shell 'toybox date +%F 2>/dev/null || date +%F' 2>/dev/null | tr -d $'\r')"
log "CSV_RELEVANCE v6.0"
log "today=$TODAY"
log ""

count_last_days() {
  local f="$1" days="$2"
  local ds
  ds="$(adb shell 'toybox date -d "-'"$days"' day" +%F 2>/dev/null || date -v-'"$days"'d +%F' 2>/dev/null | tr -d $'\r')"
  adb exec-out run-as "$PKG" awk -F, -v ds="$ds" 'NR>1 && $1>=ds {n++} END{print n+0}' "$f" 2>/dev/null || echo 0
}

last_data_date() {
  local f="$1"
  adb exec-out run-as "$PKG" tail -n +2 "$f" 2>/dev/null \
    | tr -d $'\r' \
    | awk -F, 'NF>0{ld=$1} END{print (ld?ld:"")}'
}

list_jobs(){
  adb shell dumpsys jobscheduler 2>/dev/null | sed -n "/u0a.*$PKG/p"
}
list_work(){
  adb shell dumpsys activity service WorkManager 2>/dev/null | sed -n "/$PKG/p"
}

log "== SCHEDULING SNAPSHOT =="
log "-- JobScheduler --"
list_jobs | tee -a "$OUT" >/dev/null || true
log "-- WorkManager --"
list_work | tee -a "$OUT" >/dev/null || true
log ""

while IFS= read -r f; do
  [ -n "$f" ] || continue
  b="$(basename "$f" .csv)"
  lock="$LOCKS_DIR/$b.header"

  log "---- $f ----"
  if command -v rg >/dev/null 2>&1; then
    REF=$(rg -n --hidden -S -- "$f" "$SRC_DIR" 2>/dev/null | head -n 5 || true)
  else
    REF=$(grep -RIn -- "$f" "$SRC_DIR" 2>/dev/null | head -n 5 || true)
  fi
  if [ -n "$REF" ]; then
    log "code_refs:"
    printf "%s\n" "$REF" | tee -a "$OUT" >/dev/null
  else
    log "code_refs: <none>"
  fi

  if [ -s "$lock" ]; then
    log "lock_header: $lock (present)"
  else
    log "lock_header: $lock (missing)"
  fi

  if adb exec-out run-as "$PKG" sh -c '[ -f "'"$f"'" ]' >/dev/null 2>&1; then
    SIZE=$(adb exec-out run-as "$PKG" stat -c %s "$f" 2>/dev/null || echo 0)
    log "device_file: present (size=${SIZE}B)"
    TODAY_N=$(adb exec-out run-as "$PKG" grep -a -c "^$TODAY" "$f" 2>/dev/null || echo 0)
    [ "${TODAY_N:-0}" -eq 0 ] && TODAY_N=$(adb exec-out run-as "$PKG" grep -a -c "$TODAY" "$f" 2>/dev/null || echo 0)
    LAST7_N=$(count_last_days "$f" 7)
    LAST_DATE=$(last_data_date "$f")
    log "rows_today=$TODAY_N last7_days=$LAST7_N last_data_date=${LAST_DATE:-<none>}"
    log "-- tail --"
    adb exec-out run-as "$PKG" tail -n 3 "$f" 2>/dev/null | tr -d $'\r' | tee -a "$OUT" >/dev/null || true
  else
    log "device_file: <missing>"
  fi

  log ""
done <<< "$TARGETS"

log "DONE"
