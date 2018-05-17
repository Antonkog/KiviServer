package com.wezom.kiviremoteserver.mvp.view;

import android.content.ServiceConnection;

/**
 * Created by andre on 02.06.2017.
 */

public interface HomeFragmentView extends BaseMvpView {

    void printLog(String t);

    void setIpAdress(String adress);

    void bindService(ServiceConnection connection);

    void unbindService(ServiceConnection connection);

    void networkNoAvailable();

    void networkAvailable();
}
