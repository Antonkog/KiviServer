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

$ADB connect 192.168.0.101:5555 || exit -1

java -jar ${autoSign} ${sign509} ${signPk8} ${apk_host}  ${apk_host2} || exit -1

#172.20.10.4:5555
$ADB devices -l
$ADB root 2> /dev/null

$ADB connect 192.168.0.101:5555 || exit -1

#$ADB install -r /Users/antonkogan/Desktop/maxTest/launcher2.apk
#$ADB install -r /Users/antonkogan/Desktop/maxTest/auth.apk

$ADB remount # mount system

$ADB push -p ${apk_host2} ${path_sysapp}|| exit -1

$ADB reboot

exit -0