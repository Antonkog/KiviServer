package com.wezom.kiviremoteserver.bus;

/**
 * Created by andre on 26.06.2017.
 */
public class NetworkStateEvent {

    private boolean isNetworkAvailable;

    public NetworkStateEvent(boolean isNetworkAvailable) {
        this.isNetworkAvailable = isNetworkAvailable;
    }

    public boolean isNetworkAvailable() {
        return isNetworkAvailable;
    }
}
