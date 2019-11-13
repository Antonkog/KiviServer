package com.wezom.kiviremoteserver.bus;

/**
 * Created by andre on 06.06.2017.
 */
public class RemotePlayerEvent {
    public int num;
    public int progress;

    public RemotePlayerEvent(int num) {
        this.num = num;
    }

    public RemotePlayerEvent(int num, int progress) {
        this.num = num;
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "RemotePlayerEvent{" +
                "num=" + num +
                ", progress=" + progress +
                '}';
    }
}
