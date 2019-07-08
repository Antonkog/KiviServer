#!/bin/bash

# CHANGE THESE FOR YOUR APP
app_package="com.wezom.kiviremoteserver"
MAIN_ACTIVITY="com.wezom.kiviremoteserver.ui.activity.HomeActivity"

ADB="adb"
ADB_SH="$ADB shell"

path_sysapp="/system/priv-app/KiviServer/KiviServer.apk" # assuming the app is priviledged
apk_host="./app/build/outputs/apk/mstar/debug/app-mstar-debug.apk"
apk_host2="./app/build/outputs/apk/mstar/debug/server_mtc.apk"


sign509="/Users/antonio/Documents/Auto-Sign/mtc6/platform.x509.pem"
signPk8="/Users/antonio/Documents/Auto-Sign/mtc6/platform.pk8"
autoSign="/Users/antonio/Documents/Auto-Sign/signapk.jar"



#./gradlew assembleDebug || exit -1 # exit on failure

$ADB connect 192.168.0.152:5555 || exit -1

$ADB devices -l

$ADB root 2> /dev/null

$ADB connect 192.168.0.152:5555 || exit -1

$ADB remount # mount system

java -jar ${autoSign} ${sign509} ${signPk8} ${apk_host}  ${apk_host2}
#
#$ADB_SH chmod 755 ${path_sysapp}
#$ADB_SH chmod 644 ${path_sysapp}

$ADB push -p ${apk_host2} ${path_sysapp}|| exit -1

$ADB_SH pm install -r -t ${path_sysapp} || exit -1

$ADB shell "am start -n \"$app_package/$MAIN_ACTIVITY\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"

$ADB_SH  dumpsys package  ${app_package} | grep version

exit -0
