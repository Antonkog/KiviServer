package com.wezom.kiviremoteserver.bus;

import com.wezom.kiviremoteserver.interfaces.DataStructure;

/**
 * Created by andre on 06.06.2017.
 */
public class NewDataEvent {
    private DataStructure dataEvent;

    public NewDataEvent(DataStructure dataEvent) {
        this.dataEvent = dataEvent;
    }

    public DataStructure getDataEvent() {
        return dataEvent;
    }
}
