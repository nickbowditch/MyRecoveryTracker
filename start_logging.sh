#!/bin/sh
PKG=com.nick.myrecoverytracker

adb shell am force-stop $PKG
adb logcat -c
adb shell am start -n $PKG/.MainActivity
date '+START %F %T'
