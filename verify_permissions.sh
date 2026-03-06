#!/bin/bash
# verify_permissions.sh - Comprehensive Category 8 verification

set -eu

PKG="com.nick.myrecoverytracker"

echo "=========================================="
echo "CATEGORY 8: PERMISSIONS & SETTINGS CHECK"
echo "=========================================="
echo ""

# 1. Battery optimization disabled
echo -n "1. Battery optimization disabled: "
if adb shell dumpsys deviceidle | grep -q "$PKG"; then
    echo "✅ MYRA unrestricted"
else
    echo "❌ MYRA may be restricted - check battery settings"
fi

# 2. Usage access permission
echo -n "2. Usage access permission: "
if adb shell appops get "$PKG" GET_USAGE_STATS | grep -q "allow"; then
    echo "✅ MYRA listed and allowed"
else
    echo "❌ MISSING - grant usage access"
fi

# 3. Location permission (only if participant opts in)
echo -n "3. Location permission: "
BG_LOC=$(adb shell dumpsys package "$PKG" | grep -A1 ACCESS_BACKGROUND_LOCATION | grep granted=true || echo "")
if [ -n "$BG_LOC" ]; then
    echo "✅ Background location enabled (opted in)"
else
    echo "⚠️  Background location disabled (default/not opted in)"
fi

# 4. Notification permissions
echo -n "4. Notification permissions: "
if adb shell dumpsys package "$PKG" | grep -A1 POST_NOTIFICATIONS | grep -q granted=true; then
    echo "✅ Not blocked or silenced"
else
    echo "❌ MISSING - grant notification permission"
fi

# 5. Background activity unrestricted
echo -n "5. Background activity: "
BATTERY_RESTRICTED=$(adb shell dumpsys battery | grep -i restricted || echo "")
if [ -z "$BATTERY_RESTRICTED" ]; then
    echo "✅ MYRA unrestricted"
else
    echo "❌ May be restricted - check data usage/background limits"
fi

# 6. Auto-start permission (device-specific - Xiaomi example)
echo -n "6. Auto-start permission: "
echo "⚠️  MANUAL CHECK REQUIRED (device-specific setting)"

# 7. Protected apps (device-specific)
echo -n "7. Protected apps setting: "
echo "⚠️  MANUAL CHECK REQUIRED (device-specific setting)"

echo ""
echo "=========================================="
echo "SUMMARY"
echo "=========================================="
echo "Items requiring manual verification:"
echo "  - Auto-start permission (Xiaomi, Huawei, etc.)"
echo "  - Protected apps (some manufacturers)"
echo "  - Data usage/background limits in Android settings"
echo ""
echo "To manually verify:"
echo "  Settings → Apps → MYRA → Battery → Unrestricted"
echo "  Settings → Apps → MYRA → Mobile data → Unrestricted"
echo "  Settings → Apps → MYRA → [Device-specific auto-start]"
