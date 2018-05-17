package com.wezom.kiviremoteserver.interfaces;

/**
 * Created by andre on 05.06.2017.
 */

public interface ThreadServerHandler<V> {
    void newMessageRead(V s);
}