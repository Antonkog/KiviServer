package com.wezom.kiviremoteserver.net.server.model;

import android.os.Parcel;

import java.util.HashMap;

public class AppVisibility implements LauncherBasedData {
    private String isVisible;

    private String packageName;

    public String isVisible() {
        return isVisible;
    }

    public void setVisiblilty(String isVisible) {
        this.isVisible = isVisible;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String toString() {
        return "AppVisibility [isVisible = " + isVisible + ", packageName = " + packageName + "]";
    }

    @Override
    public String getID() {
        return packageName;
    }

    @Override
    public String getName() {
        return packageName;
    }

    @Override
    public String getImageUrl() {
        return null;
    }

    @Override
    public Boolean isActive() {
        return ((isVisible != null) && isVisible.equalsIgnoreCase("true"));
    }

    @Override
    public TYPE getType() {
        return TYPE.APPLICATION;
    }

    @Override
    public HashMap<String, String> getAdditionalData() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.isVisible);
        dest.writeString(this.packageName);
    }

    public AppVisibility() {
    }

    protected AppVisibility(Parcel in) {
        this.isVisible = in.readString();
        this.packageName = in.readString();
    }

    public static final Creator<AppVisibility> CREATOR = new Creator<AppVisibility>() {
        @Override
        public AppVisibility createFromParcel(Parcel source) {
            return new AppVisibility(source);
        }

        @Override
        public AppVisibility[] newArray(int size) {
            return new AppVisibility[size];
        }
    };
}
			
		