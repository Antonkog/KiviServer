#!/bin/bash
# CHANGE THESE FOR YOUR APP
app_package="com.wezom.kiviremoteserver"
MAIN_ACTIVITY="com.wezom.kiviremoteserver.ui.activity.HomeActivity"

ADB="adb"
ADB_SH="$ADB shell"
#/system/priv-app/KiviLauncher/KiviLauncher.apk

signPk8="/Users/antonkogan/Documents/Auto-Sign/realtek/platform.pk8"
sign509="/Users/antonkogan/Documents/Auto-Sign/realtek/platform.x509.pem"
autoSign="/Users/antonkogan/Documents/Auto-Sign/signapk.jar"
apk_host="./app/build/outputs/apk/realtek_7_/debug/app-realtek_7_-debug.apk"
apk_host2="./app/build/outputs/apk/realtek_7_/debug/server_v2rtk.apk"
path_sysapp="/system/priv-app/KiviServer/KiviServer.apk" # assuming the app is priviledged

./gradlew assembleRealtek_7_Debug || exit -1 # exit on failure

$ADB disconnect
$ADB connect 192.168.0.182:5555 || exit -1

java -jar ${autoSign} ${sign509} ${signPk8} ${apk_host}  ${apk_host2}

$ADB devices -l
$ADB root 2> /dev/null

$ADB connect 192.168.0.182:5555 || exit -1

$ADB remount # mount system

$ADB push -p ${apk_host2} ${path_sysapp}|| exit -1

$ADB_SH chmod 755 ${path_sysapp}
$ADB_SH chmod 644 ${path_sysapp}


$ADB_SH pm install -r -t ${path_sysapp} || exit -1

$ADB_SH  dumpsys package  ${app_package} | grep version


#$ADB_SH settings put global install_non_market_apps 1

#$ADB_SH chmod 644 ${path_sysapp}

#Unmount system
#$ADB_SH "mount -o remount,ro /"
#$ADB reboot
#$ADB_SH pm disable com.kivi.launcher
#$ADB_SH pm enable com.kivi.launcher

# Stop the app
#$ADB shell "am force-stop $app_package"

## Re execute the app
#$ADB shell "am start -n \"$app_package/$MAIN_ACTIVITY\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"
#
exit -0
