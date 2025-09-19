#!/bin/sh
set -e

MAN=app/src/main/AndroidManifest.xml
cp "$MAN" "$MAN.bak"

perl -0777 -i -pe '
  s|(<uses-permission[^>]*BLUETOOTH_CONNECT[^>]*/>\s*)|
    $1 . qq(    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" tools:targetApi="s"/>\n)
  |se unless /android\.permission\.BLUETOOTH_SCAN/;

  s|(<uses-permission[^>]*FOREGROUND_SERVICE[^>]*/>\s*)|
    $1 . qq(    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE"/>\n)
  |se unless /android\.permission\.FOREGROUND_SERVICE_CONNECTED_DEVICE/;

  if (!/android:name="\.ClassicBtService"/s) {
    s|</application>|    <service android:name=".ClassicBtService" android:exported="true" android:foregroundServiceType="connectedDevice" />\n  </application>|s;
  }

  if (!/android:name="\.BluetoothScanService"/s) {
    s|</application>|    <service android:name=".BluetoothScanService" android:exported="true" android:foregroundServiceType="connectedDevice" />\n  </application>|s;
  }

  if (!/android:name="\.BleScanReceiver"/s) {
    s|</application>|    <receiver android:name=".BleScanReceiver" android:exported="true" />\n  </application>|s;
  }
' "$MAN"

./gradlew :app:installDebug
