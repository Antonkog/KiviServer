package com.wezom.kiviremoteserver.bus;

import com.wezom.kiviremoteserver.service.RemoteMessengerService;

import java.util.List;

import timber.log.Timber;

/**
 * Created by andre on 06.06.2017.
 */
public class ExecutorPlayerEvent {
    private String playerAction;
    private int num;
    private List<Float> args;

    public ExecutorPlayerEvent(String playerAction, List<Float> args) {
        this.args = args;
        this.playerAction = playerAction;
        switch (playerAction) {
            case "CLOSE":
                this.num = RemoteMessengerService.CLOSE;
                break;
            case "PLAY":
                this.num = RemoteMessengerService.PLAY;
                break;
            case "PAUSE":
                this.num = RemoteMessengerService.PAUSE;
                break;
            case "SEEK_TO":
                this.num = RemoteMessengerService.SEEK_TO;
                break;
            case "REQUEST_STATE":
                this.num = RemoteMessengerService.REQUEST_STATE;
                break;
            case "REQUEST_CONTENT":
                this.num = RemoteMessengerService.REQUEST_CONTENT;
                break;
            default:
                Timber.e("unlnown player action: " + playerAction);
                break;
        }
    }

    public String getPlayerAction() {
        return playerAction;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setPlayerAction(String playerAction) {
        this.playerAction = playerAction;
    }

    public List<Float> getArgs() {
        return args;
    }

    public void setArgs(List<Float> args) {
        this.args = args;
    }

    @Override
    public String toString() {
        return "RemotePlayerEvent{" +
                "playerAction=" + playerAction +
                ", args=" + args +
                '}';
    }
}
