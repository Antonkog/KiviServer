// IPlayerControl.aidl
package com.kivi.launcher_v2;


interface IPlayerControl {
     void play();
     void pause();
     void seekTo(int progress);
     void close();
     void reloadState();//retunt through IPlayerListener.changeState
     void requeestConetentInfo();//retunt through IPlayerListener.launchPlayer
 }
