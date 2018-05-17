package com.wezom.kiviremoteserver.net.server.model;


import com.google.gson.annotations.SerializedName;

public class ServerApplicationInfo {
    @SerializedName("application_name")
    private String applicationName;
    @SerializedName("package_name")
    private String applicationPackage;
    @SerializedName("app_icon")
    private byte[] applicationIcon;

    public String getApplicationName() {
        return applicationName;
    }

    public ServerApplicationInfo setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public String getApplicationPackage() {
        return applicationPackage;
    }

    public ServerApplicationInfo setApplicationPackage(String applicationPackage) {
        this.applicationPackage = applicationPackage;
        return this;
    }

    public byte[] getApplicationIcon() {
        return applicationIcon;
    }

    public ServerApplicationInfo setApplicationIcon(byte[] applicationIcon) {
        this.applicationIcon = applicationIcon;
        return this;
    }
}
