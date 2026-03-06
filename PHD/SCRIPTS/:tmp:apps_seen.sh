# save as: /tmp/apps_seen.sh
# usage: bash /tmp/apps_seen.sh
#!/usr/bin/env bash
set -euo pipefail

PKG_USER=0   # change if you need a different Android user

# --- compute time ranges on HOST (local time) and pass as epoch ms ---
read -r Y_BEGIN T_BEGIN NOW_MS <<'PYDONE'
python3 - <<'PY'
from datetime import datetime, timedelta
now = datetime.now()
today = datetime(now.year, now.month, now.day)
yest  = today - timedelta(days=1)
ms = lambda dt: str(int(dt.timestamp()*1000))
print(ms(yest), ms(today), ms(now))
PY
PYDONE

# helper: label for a package (best-effort)
app_label() {
  local p="$1"
  adb shell "dumpsys package $p 2>/dev/null | sed -n 's/^[[:space:]]*application-label:\\(.*\\)/\\1/p' | head -n1" | tr -d '\r'
}

# helper: list unique packages from usagestats events in [BEGIN,END)
list_pkgs() {
  local BEGIN="$1" END="$2"
  adb shell "cmd usagestats query-events --user $PKG_USER --begin $BEGIN --end $END 2>/dev/null" \
  | grep -E 'EventType=(MOVE_TO_FOREGROUND|ACTIVITY_RESUMED)|eventType=(1|7)' \
  | sed -n 's/.*package=\\([^[:space:]]*\\).*/\\1/p' \
  | sed '/^$/d' \
  | sort | uniq -c | sort -nr
}

# print section
print_section() {
  local title="$1" begin="$2" end="$3"
  echo "== $title =="
  # packages with counts
  if ! list_pkgs "$begin" "$end" | sed -n '1p' >/dev/null; then
    echo "(no events)"
    echo
    return
  fi
  # show count, package, and best-effort label
  while read -r cnt pkg; do
    lbl="$(app_label "$pkg")"
    if [ -n "$lbl" ]; then
      printf "%5s  %-50s  %s\n" "$cnt" "$pkg" "$lbl"
    else
      printf "%5s  %s\n" "$cnt" "$pkg"
    fi
  done < <(list_pkgs "$begin" "$end")
  echo
}

# --- run ---
print_section "Yesterday" "$Y_BEGIN" "$T_BEGIN"
print_section "Today (so far)" "$T_BEGIN" "$NOW_MS"