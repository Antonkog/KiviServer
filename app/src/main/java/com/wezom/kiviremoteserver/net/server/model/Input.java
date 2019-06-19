package com.wezom.kiviremoteserver.net.server.model;

import android.os.Parcel;

import java.util.HashMap;

public class Input implements LauncherBasedData {
    int portNum;
    private String portName;
    private String imageUrl;
    private String uri;
    private Boolean active;

    public Input addImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }


    public Input addPortNum(int portNum) {
        this.portNum = portNum;
        return this;
    }

    public Input addPortName(String portName) {
        this.portName = portName;
        return this;
    }

    public Input addUri(String uri) {
        this.uri = uri;
        return this;
    }

    public Input addActive(Boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public String getID() {
        return "" + portNum;
    }

    @Override
    public String getName() {
        return portName;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String getLocalUri() {
        return uri;
    }

    @Override
    public String getPackageName() {
        return null;
    }

    @Override
    public Boolean isActive() {
        return active;
    }


    @Override
    public HashMap<String, String> getAdditionalData() {
        return null;
    }

    @Override
    public TYPE getType() {
                 return TYPE.INPUT;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.portNum);
        dest.writeString(this.portName);
        dest.writeString(this.imageUrl);
        dest.writeString(this.uri);
        dest.writeValue(this.active);
    }

    public Input() {
    }

    protected Input(Parcel in) {
        this.portNum = in.readInt();
        this.portName = in.readString();
        this.imageUrl = in.readString();
        this.uri = in.readString();
        this.active = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Input> CREATOR = new Creator<Input>() {
        @Override
        public Input createFromParcel(Parcel source) {
            return new Input(source);
        }

        @Override
        public Input[] newArray(int size) {
            return new Input[size];
        }
    };
}