package com.wezom.kiviremoteserver.net.server.model;


import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class ServerApplicationInfo implements LauncherBasedData {
    @SerializedName("application_name")
    private String applicationName;
    @SerializedName("package_name")
    private String applicationPackage;
    @SerializedName("baseIcon")
    private String baseIcon;
    @SerializedName("app_icon")
    private byte[] applicationIcon; // for old remote Control
    @SerializedName("uri")
    private String imageUri;

    public String getApplicationName() {
        return applicationName;
    }

    public ServerApplicationInfo setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public ServerApplicationInfo setApplicationPackage(String applicationPackage) {
        this.applicationPackage = applicationPackage;
        return this;
    }

    public ServerApplicationInfo setApplicationIcon(byte[] applicationIcon) {
        this.applicationIcon = applicationIcon;
        return this;
    }

    public ServerApplicationInfo setUri(String iconUri) {
        this.imageUri = iconUri;
        return this;
    }

    public ServerApplicationInfo setBaseIcon(String baseIcon) {
        this.baseIcon = baseIcon;
        return this;
    }

    @Override
    public String getID() {
        return applicationPackage;
    }

    @Override
    public String getName() {
        return applicationName;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public String getBaseIcon() {
        return baseIcon;
    }

    @Override
    public Boolean isActive() {
        return null;
    }

    @Override
    public HashMap<String, String> getAdditionalData() {
        return null;
    }

    @Override
    public TYPE getType() {
        return TYPE.APPLICATION;
    }


    public ServerApplicationInfo() {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.applicationName);
        dest.writeString(this.applicationPackage);
        dest.writeString(this.baseIcon);
        dest.writeByteArray(this.applicationIcon);
        dest.writeString(this.imageUri);
    }

    protected ServerApplicationInfo(Parcel in) {
        this.applicationName = in.readString();
        this.applicationPackage = in.readString();
        this.baseIcon = in.readString();
        this.applicationIcon = in.createByteArray();
        this.imageUri = in.readString();
    }

    public static final Creator<ServerApplicationInfo> CREATOR = new Creator<ServerApplicationInfo>() {
        @Override
        public ServerApplicationInfo createFromParcel(Parcel source) {
            return new ServerApplicationInfo(source);
        }

        @Override
        public ServerApplicationInfo[] newArray(int size) {
            return new ServerApplicationInfo[size];
        }
    };
}
