package com.wezom.kiviremoteserver.interfaces;


/**
 * Created by andre on 07.06.2017.
 */

public interface EventProtocolExecutor  {
    void executeTextCommand(String string);
    void executeCommand(int keyEvent);
    void executeClickCommand();
}
