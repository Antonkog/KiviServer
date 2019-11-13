#!/bin/bash
# CHANGE THESE FOR YOUR APP
app_package="com.wezom.kiviremoteserver"
MAIN_ACTIVITY="com.wezom.kiviremoteserver.ui.activity.HomeActivity"

ADB="adb"
ADB_SH="$ADB shell"
#/system/priv-app/KiviLauncher/KiviLauncher.apk

signPk8="/Users/antonkogan/Documents/Auto-Sign/realtek9/platform.pk8"
sign509="/Users/antonkogan/Documents/Auto-Sign/realtek9/platform.x509.pem"
autoSign="/Users/antonkogan/Documents/Auto-Sign/signapk.jar"

apk_host="./app/build/outputs/apk/realtek_9_/debug/app-realtek_9_-debug.apk"
apk_host2="./app/build/outputs/apk/realtek_9_/debug/kiviserver_9.apk"
path_sysapp="/system/priv-app/kiviserver_9/kiviserver_9.apk" # assuming the app is priviledged

./gradlew assembleRealtek_9_Debug || exit -1 # exit on failure
$ADB disconnect

$ADB connect 192.168.0.171:5555 || exit -1

java -jar ${autoSign} ${sign509} ${signPk8} ${apk_host}  ${apk_host2} || exit -1

#172.20.10.4:5555
$ADB devices -l
$ADB root 2> /dev/null

$ADB connect 192.168.0.171:5555 || exit -1


$ADB remount # mount system

$ADB push -p ${apk_host2} ${path_sysapp}|| exit -1

$ADB reboot

#$ADB shell dumpsys package com.wezom.kiviremoteserver | grep version

#$ADB_SH chmod 755 ${path_sysapp}
#$ADB_SH chmod 644 ${path_sysapp}

#$ADB_SH settings put global install_non_market_apps 1

#$ADB_SH chmod 644 ${path_sysapp}

#Unmount system
#$ADB_SH "mount -o remount,ro /"

#$ADB_SH pm install -r -t ${path_sysapp} || exit -1
#
#$ADB reboot
#$ADB_SH pm disable com.kivi.launcher
#$ADB_SH pm enable com.kivi.launcher

# Stop the app
#$ADB shell "am force-stop $app_package"

## Re execute the app
#$ADB shell "am start -n \"$app_package/$MAIN_ACTIVITY\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
#
#$ADB_SH  dumpsys package  ${app_package} | grep version

exit -0