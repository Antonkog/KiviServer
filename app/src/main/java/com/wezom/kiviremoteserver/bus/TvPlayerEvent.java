package com.wezom.kiviremoteserver.bus;

import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;

public class TvPlayerEvent  {

    private KiviProtocolStructure.ServerEventType playerAction;
    private int newState;
    private PreviewCommonStructure playerPreview;


    public TvPlayerEvent(KiviProtocolStructure.ServerEventType playerAction, int newState, PreviewCommonStructure playerPreview) {
        this.playerAction = playerAction;
        this.newState = newState;
        this.playerPreview = playerPreview;
    }

    public KiviProtocolStructure.ServerEventType getPlayerAction() {
        return playerAction;
    }

    public void setPlayerAction(KiviProtocolStructure.ServerEventType playerAction) {
        this.playerAction = playerAction;
    }

    public int getNewState() {
        return newState;
    }

    public void setNewState(int newState) {
        this.newState = newState;
    }

    public PreviewCommonStructure getPlayerPreview() {
        return playerPreview;
    }

    public void setPlayerPreview(PreviewCommonStructure playerPreview) {
        this.playerPreview = playerPreview;
    }

    @Override
    public String toString() {
        return "TvPlayerEvent{" +
                "playerAction=" + playerAction +
                ", newState=" + newState +
                ", playerPreview=" + playerPreview +
                '}';
    }
}
