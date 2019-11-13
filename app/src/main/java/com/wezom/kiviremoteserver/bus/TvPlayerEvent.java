package com.wezom.kiviremoteserver.bus;

import android.os.Parcel;
import android.os.Parcelable;

import com.wezom.kiviremoteserver.common.KiviProtocolStructure;
import com.wezom.kiviremoteserver.net.server.model.PreviewCommonStructure;

public class TvPlayerEvent implements Parcelable {

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.playerAction == null ? -1 : this.playerAction.ordinal());
        dest.writeInt(this.newState);
        dest.writeParcelable(this.playerPreview, flags);
    }

    protected TvPlayerEvent(Parcel in) {
        int tmpPlayerAction = in.readInt();
        this.playerAction = tmpPlayerAction == -1 ? null : KiviProtocolStructure.ServerEventType.values()[tmpPlayerAction];
        this.newState = in.readInt();
        this.playerPreview = in.readParcelable(PreviewCommonStructure.class.getClassLoader());
    }

    public static final Creator<TvPlayerEvent> CREATOR = new Creator<TvPlayerEvent>() {
        @Override
        public TvPlayerEvent createFromParcel(Parcel source) {
            return new TvPlayerEvent(source);
        }

        @Override
        public TvPlayerEvent[] newArray(int size) {
            return new TvPlayerEvent[size];
        }
    };
}
