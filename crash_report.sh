#!/bin/bash
PKG="com.nick.myrecoverytracker"

adb logcat -d \
  | grep "$PKG" \
  | awk '
    /FATAL EXCEPTION/ {inblock=1; reason=""; next}
    inblock && /Exception/ && reason=="" {reason=$0}
    inblock && /^$/ {inblock=0; if(reason!=""){print reason; reason=""}}
  ' \
  | sort | uniq -c
