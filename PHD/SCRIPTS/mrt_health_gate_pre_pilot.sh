cat > mrt_health_gate_pre_pilot.sh <<'EOF'
#!/bin/bash
set -euo pipefail

# MyRecoveryTracker Health Gate (Pre-Pilot v2.0)
MODE="${1:-artefact}"
PID="${2:-P_PREPILOT_PIXEL}"
INGEST_ROOT="$HOME/Documents/PHD/INGEST/raw"
DEST="${INGEST_ROOT}/${PID}"
DEVICE_PATH="/sdcard/Android/data/com.myra/files"

echo "== MyRecoveryTracker Health Gate (Pre-Pilot v2.0) =="
echo "Mode    : $MODE"
echo "PID     : $PID"
echo

FAIL=0
WARN=0
UNKN=0

fail(){ echo "FAIL: $*"; FAIL=$((FAIL+1)); }
ok(){ echo "OK  : $*"; }
warn(){ echo "WARN: $*"; WARN=$((WARN+1)); }
unknown(){ echo "UNKN: $*"; UNKN=$((UNKN+1)); }

csv() { printf '%s/%s\n' "$DEST" "$1"; }

check_file_exists_with_rows(){
  local f="$1" l="$2" p n
  p="$(csv "$f")"
  [ -s "$p" ] || { fail "$l falsified total write failure (missing $f)"; return 1; }
  n="$(wc -l < "$p" | tr -d ' ')"
  [ "$n" -gt 1 ] && ok "$l falsified total write failure ($((n-1)) rows)" || { fail "$l falsified total write failure (0 rows)"; return 1; }
  return 0
}

check_csv_structure(){
  local f="$1" l="$2" p
  p="$(csv "$f")"
  [ -s "$p" ] || return
  head -2 "$p" | awk -F',' 'NR==1{h=NF} NR==2{if(NF!=h){exit 1}}' \
    && ok "$l falsified CSV structural corruption" \
    || fail "$l has malformed CSV structure"
}

check_column_count(){
  local f="$1" l="$2" expected="$3" p actual
  p="$(csv "$f")"
  [ -s "$p" ] || return
  actual="$(head -1 "$p" | awk -F',' '{print NF}')"
  [ "$actual" -eq "$expected" ] \
    && ok "$l falsified schema drift ($actual cols)" \
    || fail "$l schema mismatch (expected $expected, got $actual)"
}

check_numeric_column(){
  local f="$1" l="$2" col="$3" p v
  p="$(csv "$f")"
  [ -s "$p" ] || return
  v="$(tail -1 "$p" | awk -F',' -v c="$col" '{print $c}' 2>/dev/null || echo "NaN")"
  v="${v%\"}"; v="${v#\"}"
  awk -v val="$v" 'BEGIN{exit !(val+0==val && val>=0)}' 2>/dev/null \
    && ok "$l '$col' falsified non-numeric ($v)" \
    || fail "$l '$col' is non-numeric or negative ($v)"
}

check_timestamps_monotonic(){
  local f="$1" l="$2" ts_col="$3" p result
  p="$(csv "$f")"
  [ -s "$p" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    df = pd.read_csv('$p')
    if len(df) < 2:
        print("OK")
    else:
        df['ts_parsed'] = pd.to_datetime(df['$ts_col'], errors='coerce')
        if df['ts_parsed'].isna().any():
            print("UNPARSEABLE")
        elif (df['ts_parsed'].diff().dropna() < pd.Timedelta(0)).any():
            print("NON_MONOTONIC")
        else:
            print("OK")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK) ok "$l timestamps falsified non-monotonic ordering" ;;
    NON_MONOTONIC) fail "$l has timestamps out of order" ;;
    UNPARSEABLE) fail "$l has unparseable timestamps" ;;
    *) fail "$l timestamp check failed: $result" ;;
  esac
}

check_date_range(){
  local f="$1" l="$2" date_col="$3" min_date="$4" max_date="$5" p result
  p="$(csv "$f")"
  [ -s "$p" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    df = pd.read_csv('$p')
    df['date_parsed'] = pd.to_datetime(df['$date_col'], errors='coerce').dt.date
    min_allowed = pd.to_datetime('$min_date').date()
    max_allowed = pd.to_datetime('$max_date').date()
    if (df['date_parsed'] < min_allowed).any() or (df['date_parsed'] > max_allowed).any():
        print("OUT_OF_RANGE")
    else:
        print("OK")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK) ok "$l timestamps falsified range violation ($min_date to $max_date)" ;;
    OUT_OF_RANGE) fail "$l has dates outside expected range" ;;
    *) fail "$l date range check failed: $result" ;;
  esac
}

check_heartbeat_gaps(){
  local f="$1" l="$2" max_gap="$3" p result
  p="$(csv "$f")"
  [ -s "$p" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    df = pd.read_csv('$p')
    df['ts_parsed'] = pd.to_datetime(df.iloc[:, 0], errors='coerce')
    gaps = df['ts_parsed'].diff().dt.total_seconds().dropna()
    if (gaps > $max_gap).any():
        print("GAPS_FOUND")
    else:
        print("OK")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK) ok "$l falsified collection gaps >$max_gap s" ;;
    GAPS_FOUND) fail "$l has collection gaps exceeding $max_gap seconds" ;;
    *) fail "$l heartbeat gap check failed: $result" ;;
  esac
}

check_unlock_count_with_diag(){
  local f_log="$1" f_diag="$2" f_summary="$3" p_log p_diag p_summary result
  p_log="$(csv "$f_log")"
  p_diag="$(csv "$f_diag")"
  p_summary="$(csv "$f_summary")"
  [ -s "$p_log" ] && [ -s "$p_diag" ] && [ -s "$p_summary" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    log = pd.read_csv('$p_log')
    diag = pd.read_csv('$p_diag')
    summary = pd.read_csv('$p_summary')
    
    log_count = len(log)
    diag_unlock_count = len(diag[diag['tag'] == 'UNLOCK'])
    summary_count = int(summary['total_unlocks'].sum())
    
    if summary_count == diag_unlock_count:
        print(f"OK:{summary_count}:{diag_unlock_count}:{log_count}")
    else:
        print(f"MISMATCH:{summary_count}:{diag_unlock_count}:{log_count}")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK:*)
      IFS=':' read -r _ sum_cnt diag_cnt log_cnt <<< "$result"
      ok "Unlock count falsified mismatch (daily_summary=$sum_cnt matches unlock_diag=$diag_cnt)"
      if [ "$log_cnt" -lt "$diag_cnt" ]; then
        warn "unlock_log has $log_cnt raw events vs $diag_cnt counted (likely includes rapid re-unlocks <60s)"
      fi
      ;;
    MISMATCH:*)
      IFS=':' read -r _ sum_cnt diag_cnt log_cnt <<< "$result"
      fail "Unlock count mismatch: daily_summary=$sum_cnt, unlock_diag=$diag_cnt, unlock_log=$log_cnt"
      ;;
    *) fail "Unlock count check failed: $result" ;;
  esac
}

check_notification_count(){
  local f_log="$1" f_summary="$2" p_log p_summary result
  p_log="$(csv "$f_log")"
  p_summary="$(csv "$f_summary")"
  [ -s "$p_log" ] && [ -s "$p_summary" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    log = pd.read_csv('$p_log')
    summary = pd.read_csv('$p_summary')
    
    log_count = len(log)
    posted_count = len(log[log['event_type'] == 'POSTED'])
    summary_count = int(summary['notification_count'].sum())
    
    if summary_count == posted_count:
        print(f"OK:{summary_count}:{posted_count}")
    elif summary_count == log_count:
        print(f"WARN_ALL_EVENTS:{summary_count}:{log_count}:{posted_count}")
    else:
        print(f"MISMATCH:{summary_count}:{log_count}:{posted_count}")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK:*)
      IFS=':' read -r _ sum_cnt posted_cnt <<< "$result"
      ok "Notification count falsified mismatch (daily_summary=$sum_cnt matches POSTED events=$posted_cnt)"
      ;;
    WARN_ALL_EVENTS:*)
      IFS=':' read -r _ sum_cnt log_cnt posted_cnt <<< "$result"
      warn "Notification count uses all events ($sum_cnt) not just POSTED ($posted_cnt)"
      ;;
    MISMATCH:*)
      IFS=':' read -r _ sum_cnt log_cnt posted_cnt <<< "$result"
      fail "Notification count mismatch: daily_summary=$sum_cnt, total_events=$log_cnt, posted_events=$posted_cnt"
      ;;
    *) fail "Notification count check failed: $result" ;;
  esac
}

check_app_usage_sum_matches(){
  local f1="$1" f2="$2" col="$3" p1 p2 result
  p1="$(csv "$f1")"
  p2="$(csv "$f2")"
  [ -s "$p1" ] && [ -s "$p2" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    df1 = pd.read_csv('$p1')
    df2 = pd.read_csv('$p2')
    val1 = float(df1['app_min_total'].sum())
    val2 = float(df2['$col'].sum())
    diff_pct = abs(val1 - val2) / max(val1, val2) * 100 if max(val1, val2) > 0 else 0
    if diff_pct < 1:
        print(f"OK:{val1:.2f}:{val2:.2f}")
    else:
        print(f"MISMATCH:{val1:.2f}:{val2:.2f}:{diff_pct:.1f}")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK:*)
      IFS=':' read -r _ sum1 sum2 <<< "$result"
      ok "App usage sum falsified mismatch (detailed=$sum1, summary=$sum2)"
      ;;
    MISMATCH:*)
      IFS=':' read -r _ sum1 sum2 pct <<< "$result"
      fail "App usage sum differs by $pct% (detailed=$sum1, summary=$sum2)"
      ;;
    *) fail "App usage check failed: $result" ;;
  esac
}

check_value_range(){
  local f="$1" col="$2" min="$3" max="$4" l="$5" p result
  p="$(csv "$f")"
  [ -s "$p" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
try:
    df = pd.read_csv('$p')
    val = float(df['$col'].iloc[-1])
    if $min <= val <= $max:
        print(f"OK:{val}")
    else:
        print(f"OUT_OF_RANGE:{val}")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK:*)
      IFS=':' read -r _ val <<< "$result"
      ok "$l falsified implausible range ($val within [$min, $max])"
      ;;
    OUT_OF_RANGE:*)
      IFS=':' read -r _ val <<< "$result"
      fail "$l outside plausible range ($val not in [$min, $max])"
      ;;
    *) fail "$l range check failed: $result" ;;
  esac
}

check_statistical_outliers(){
  local f="$1" col="$2" l="$3" p result
  p="$(csv "$f")"
  [ -s "$p" ] || return
  result=$(python3 <<PYEOF 2>/dev/null || echo "ERROR"
import pandas as pd
import numpy as np
try:
    df = pd.read_csv('$p')
    vals = df['$col'].dropna()
    if len(vals) < 3:
        print("OK:0.00")
    else:
        mean = vals.mean()
        std = vals.std()
        last = float(vals.iloc[-1])
        z = abs((last - mean) / std) if std > 0 else 0
        if z > 3:
            print(f"OUTLIER:{z:.2f}")
        else:
            print(f"OK:{z:.2f}")
except Exception as e:
    print(f"ERROR:{e}")
PYEOF
)
  case "$result" in
    OK:*)
      IFS=':' read -r _ z <<< "$result"
      ok "$l falsified statistical outlier (z=$z)"
      ;;
    OUTLIER:*)
      IFS=':' read -r _ z <<< "$result"
      warn "$l is statistical outlier (z=$z > 3)"
      ;;
    *) fail "$l outlier check failed: $result" ;;
  esac
}

calculate_completeness_score(){
  local total=0 present=0
  for f in unlock_log.csv unlock_diag.csv screen_log.csv daily_summary.csv \
           notification_log.csv heartbeat.csv distance_today.csv \
           daily_distance_log.csv log_export.csv log_retention.csv \
           daily_app_usage_minutes.csv app_category_daily.csv; do
    total=$((total+1))
    [ -s "$(csv "$f")" ] && present=$((present+1))
  done
  local score
  score=$(awk -v p="$present" -v t="$total" 'BEGIN{printf "%.1f", (p/t)*100}')
  ok "Data completeness score: $score%"
}

check_redcap_upload(){
  local rc p result
  rc="redcap_receipts.csv"
  p="$(csv "$rc")"
  [ -s "$p" ] || { fail "REDCap receipts missing"; return; }
  result=$(tail -1 "$p" | awk -F',' '{code=$4; rows=$3; gsub(/"/, "", code); gsub(/"/, "", rows); print code":"rows}')
  IFS=':' read -r code rows <<< "$result"
  [ "$code" = "200" ] && ok "REDCap HTTP falsified connection failure (code $code)" || fail "REDCap HTTP returned code $code"
  [ "$rows" -gt 0 ] && ok "REDCap upload falsified zero-row success ($rows rows)" || fail "REDCap uploaded 0 rows"
}

if [ "$MODE" = "artefact" ]; then
  [ -d "$DEST" ] || { fail "Ingest directory missing: $DEST"; exit 1; }
  
  echo "=== STRUCTURAL VALIDATION ==="
  check_file_exists_with_rows "unlock_log.csv" "Unlock events"
  check_csv_structure "unlock_log.csv" "Unlock events"
  check_column_count "unlock_log.csv" "Unlock events" 2
  
  check_file_exists_with_rows "unlock_diag.csv" "Unlock diagnostics"
  check_csv_structure "unlock_diag.csv" "Unlock diagnostics"
  check_column_count "unlock_diag.csv" "Unlock diagnostics" 3
  
  check_file_exists_with_rows "screen_log.csv" "Screen events"
  check_csv_structure "screen_log.csv" "Screen events"
  check_column_count "screen_log.csv" "Screen events" 2
  
  check_file_exists_with_rows "daily_summary.csv" "Daily summary"
  check_csv_structure "daily_summary.csv" "Daily summary"
  check_column_count "daily_summary.csv" "Daily summary" 6
  check_numeric_column "daily_summary.csv" "Screen usage 'screen_usage_min'" 3
  check_numeric_column "daily_summary.csv" "App usage 'app_usage_min'" 6
  check_numeric_column "daily_summary.csv" "Total unlocks 'total_unlocks'" 2
  check_numeric_column "daily_summary.csv" "Notification count 'notification_count'" 5
  
  check_file_exists_with_rows "daily_late_night_screen_usage.csv" "Late-night screen usage"
  check_csv_structure "daily_late_night_screen_usage.csv" "Late-night screen usage"
  
  check_file_exists_with_rows "daily_usage_entropy.csv" "Usage entropy"
  check_csv_structure "daily_usage_entropy.csv" "Usage entropy"
  check_column_count "daily_usage_entropy.csv" "Usage entropy" 2
  
  check_file_exists_with_rows "daily_app_usage_minutes.csv" "Daily app usage"
  check_csv_structure "daily_app_usage_minutes.csv" "Daily app usage"
  
  check_file_exists_with_rows "daily_app_starts_by_package.csv" "App starts by package"
  check_csv_structure "daily_app_starts_by_package.csv" "App starts by package"
  
  check_file_exists_with_rows "app_category_daily.csv" "App category totals"
  check_csv_structure "app_category_daily.csv" "App category totals"
  
  check_file_exists_with_rows "notification_log.csv" "Notification events"
  check_csv_structure "notification_log.csv" "Notification events"
  check_column_count "notification_log.csv" "Notification events" 6
  
  check_file_exists_with_rows "daily_notification_engagement.csv" "Notification engagement"
  check_csv_structure "daily_notification_engagement.csv" "Notification engagement"
  
  check_file_exists_with_rows "notification_heartbeat.csv" "Notification heartbeat"
  check_csv_structure "notification_heartbeat.csv" "Notification heartbeat"
  
  check_file_exists_with_rows "distance_today.csv" "Distance today"
  check_csv_structure "distance_today.csv" "Distance today"
  check_column_count "distance_today.csv" "Distance today" 2
  
  check_file_exists_with_rows "daily_distance_log.csv" "Daily distance log"
  check_csv_structure "daily_distance_log.csv" "Daily distance log"
  check_column_count "daily_distance_log.csv" "Daily distance log" 2
  
  check_file_exists_with_rows "heartbeat.csv" "Heartbeat"
  check_csv_structure "heartbeat.csv" "Heartbeat"
  check_column_count "heartbeat.csv" "Heartbeat" 1
  
  check_file_exists_with_rows "log_export.csv" "Export log"
  check_csv_structure "log_export.csv" "Export log"
  
  check_file_exists_with_rows "log_retention.csv" "Retention log"
  check_csv_structure "log_retention.csv" "Retention log"
  
  echo
  echo "=== TEMPORAL INTEGRITY ==="
  check_timestamps_monotonic "unlock_log.csv" "Unlock events" "ts"
  check_timestamps_monotonic "notification_log.csv" "Notification events" "timestamp"
  check_date_range "daily_summary.csv" "Daily summary" "date" "2024-01-01" "2027-12-31"
  check_heartbeat_gaps "heartbeat.csv" "Heartbeat" 300
  
  echo
  echo "=== LOGICAL CONSISTENCY ==="
  check_unlock_count_with_diag "unlock_log.csv" "unlock_diag.csv" "daily_summary.csv"
  check_notification_count "notification_log.csv" "daily_summary.csv"
  check_app_usage_sum_matches "daily_app_usage_minutes.csv" "daily_summary.csv" "app_usage_min"
  
  echo
  echo "=== STATISTICAL PLAUSIBILITY ==="
  check_value_range "daily_summary.csv" "screen_usage_min" 0 1440 "Screen usage (24hr max)"
  check_value_range "daily_summary.csv" "total_unlocks" 0 500 "Daily unlocks (500 max)"
  check_value_range "daily_distance_log.csv" "distance_km" 0 100 "Daily distance (100km max)"
  check_statistical_outliers "daily_summary.csv" "screen_usage_min" "Screen usage"
  check_statistical_outliers "daily_summary.csv" "total_unlocks" "Unlock frequency"
  
  echo
  echo "=== DATA COMPLETENESS ==="
  calculate_completeness_score
  
  echo
  echo "=== REDCAP UPLOAD ==="
  check_redcap_upload
  
  echo
  echo "=== KNOWN LIMITATIONS ==="
  unknown "Data collection recency (cannot verify timestamp freshness from artefact)"
  unknown "Worker execution state (cannot verify active collection)"
  unknown "Sensor functional status (cannot verify hardware availability)"
  unknown "Future write capability (cannot predict system behavior)"
  
elif [ "$MODE" = "live" ]; then
  echo "Live mode not yet implemented in v2.0"
  exit 1
else
  echo "Usage: $0 {live|artefact} [PID]"
  exit 1
fi

echo
echo "=== GATE SUMMARY ==="
echo "Failures : $FAIL"
echo "Warnings : $WARN"
echo "Unknowns : $UNKN"
[ "$FAIL" -eq 0 ] && { echo "Result: PASS"; exit 0; } || { echo "Result: FAIL"; exit 1; }
EOF

chmod +x mrt_health_gate_pre_pilot.sh

./mrt_health_gate_pre_pilot.sh artefact