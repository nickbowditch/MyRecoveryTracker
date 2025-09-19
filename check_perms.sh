#!/usr/bin/env bash
set -euo pipefail

decide() {
  perm="$1"; shift
  echo -n "$perm: "
  if grep -R -n -E "$*" app/src/main/java >/dev/null 2>&1; then
    echo "KEEP"
  else
    echo "REMOVE"
  fi
}

decide "android.permission.ACCESS_COARSE_LOCATION"        '\b(LocationManager|FusedLocationProviderClient)\b'
decide "android.permission.ACCESS_BACKGROUND_LOCATION"    'ACCESS_BACKGROUND_LOCATION|startForeground\('

decide "android.permission.INTERNET"                      '\bokhttp3|HttpURLConnection|URL\('
decide "android.permission.ACCESS_NETWORK_STATE"          '\bConnectivityManager|NetworkCapabilities\b'
decide "android.permission.ACCESS_WIFI_STATE"             '\b(WifiManager|WifiInfo)\b'
decide "android.permission.CHANGE_WIFI_STATE"             '\b(WifiManager\.setWifiEnabled|addNetwork|enableNetwork)\b'
decide "android.permission.NEARBY_WIFI_DEVICES"           '\b(WifiManager|WifiScanner|WifiNetworkSpecifier)\b'

decide "android.permission.FOREGROUND_SERVICE"            'startForeground\('
decide "android.permission.FOREGROUND_SERVICE_LOCATION"   'startForeground\(' 'Location'
decide "android.permission.FOREGROUND_SERVICE_DATA_SYNC"  'startForeground\(' 'OkHttp|upload|RED?CAP'

decide "android.permission.POST_NOTIFICATIONS"            '\bNotificationCompat\.Builder|startForeground\('

decide "android.permission.PACKAGE_USAGE_STATS"           '\b(UsageStatsManager|UsageEvents)\b'
decide "android.permission.READ_CALL_LOG"                 '\bandroid\.provider\.CallLog\b'
decide "android.permission.READ_SMS"                      '\b(android\.provider\.Telephony\.Sms|content://sms|SmsManager)\b'

decide "android.permission.ACTIVITY_RECOGNITION"          '\b(ActivityRecognitionClient|ActivityTransitionRequest)\b'
decide "android.permission.BODY_SENSORS"                  '\b(TYPE_HEART_RATE|TYPE_HEART_BEAT)\b'
decide "android.permission.BODY_SENSORS_BACKGROUND"       '\b(TYPE_HEART_RATE|TYPE_HEART_BEAT)\b'

decide "android.permission.BLUETOOTH"                     '\bBluetoothAdapter\b' 
decide "android.permission.BLUETOOTH_CONNECT"             '\b(BluetoothAdapter|BluetoothDevice)\b'
decide "android.permission.BLUETOOTH_SCAN"                '\b(BluetoothLeScanner|ScanResult|SCAN_MODE)\b'

decide "android.permission.RECEIVE_BOOT_COMPLETED"        'BOOT_COMPLETED|MY_PACKAGE_REPLACED'
