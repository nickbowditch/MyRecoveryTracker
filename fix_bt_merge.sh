#!/bin/sh
set -e

MAIN=app/src/main/AndroidManifest.xml
DBG=app/src/debug/AndroidManifest.xml

cp "$MAIN" "$MAIN.bak"
cp "$DBG" "$DBG.bak"

perl -0777 -i -pe '
  s|<receiver\b[^>]*name\s*=\s*["'\'']\.BleScanReceiver["'\''][^>]*/>\s*||gis;
  s|<receiver\b.*?name\s*=\s*["'\'']\.BleScanReceiver["'\''].*?</receiver>\s*||gis;
  s|<service\b[^>]*name\s*=\s*["'\'']\.ClassicBtService["'\''][^>]*/>\s*||gis;
  s|<service\b.*?name\s*=\s*["'\'']\.ClassicBtService["'\''].*?</service>\s*||gis;
  s|<service\b[^>]*name\s*=\s*["'\'']\.BluetoothScanService["'\''][^>]*/>\s*||gis;
  s|<service\b.*?name\s*=\s*["'\'']\.BluetoothScanService["'\''].*?</service>\s*||gis;
' "$DBG"

perl -0777 -i -pe '
  if (!/android\.permission\.BLUETOOTH_SCAN/) {
    s|(<uses-permission[^>]*BLUETOOTH_CONNECT[^>]*/>\s*)|
      $1 . qq(    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" tools:targetApi="s"/>\n)
    |se;
  }
  if (!/android\.permission\.FOREGROUND_SERVICE_CONNECTED_DEVICE/) {
    s|(<uses-permission[^>]*FOREGROUND_SERVICE[^>]*/>\s*)|
      $1 . qq(    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE"/>\n)
    |se;
  }

  if (!/android:name="\.ClassicBtService"/s) {
    s|</application>|    <service android:name=".ClassicBtService" android:exported="true" android:foregroundServiceType="connectedDevice" />\n  </application>|s;
  }

  if (!/android:name="\.BluetoothScanService"/s) {
    s|</application>|    <service android:name=".BluetoothScanService" android:exported="true" android:foregroundServiceType="connectedDevice" />\n  </application>|s;
  }

  if (!/android:name="\.BleScanReceiver"/s) {
    s|</application>|    <receiver android:name=".BleScanReceiver" android:exported="false" />\n  </application>|s;
  }
' "$MAIN"

./gradlew :app:installDebug
