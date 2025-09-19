#!/bin/sh
set -e
PKG=com.nick.myrecoverytracker
adb shell pm grant $PKG android.permission.ACCESS_FINE_LOCATION
adb shell pm grant $PKG android.permission.BLUETOOTH_SCAN
adb shell pm grant $PKG android.permission.BLUETOOTH_CONNECT
adb shell run-as $PKG rm -f files/bluetooth_log.csv 2>/dev/null || true
adb shell am start-foreground-service -n $PKG/.ClassicBtService -a com.nick.myrecoverytracker.bt.CLASSIC_START
sleep 35
adb exec-out run-as $PKG tail -n 50 files/bluetooth_log.csv || true
adb shell am startservice -n $PKG/.ClassicBtService -a com.nick.myrecoverytracker.bt.CLASSIC_STOP
sleep 3
adb shell am start-foreground-service -n $PKG/.BluetoothScanService
sleep 35
adb exec-out run-as $PKG tail -n 50 files/bluetooth_log.csv || true
adb shell am stopservice -n $PKG/.BluetoothScanService
