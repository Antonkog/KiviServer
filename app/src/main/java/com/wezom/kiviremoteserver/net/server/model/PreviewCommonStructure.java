package com.wezom.kiviremoteserver.net.server.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class PreviewCommonStructure implements Parcelable {
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public HashMap<String, String> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(HashMap<String, String> additionalData) {
        this.additionalData = additionalData;
    }

    private String type;
    private String id;  // portNum as int, other as string app - packageName
    private String name; // title for Recommendation, portName for inputs, name for channel and app
    private String imageUrl;  // for channels and recommendations exist, for app's currently will be base64.
    private Boolean is_active; //for channels ports and
    private HashMap<String, String> additionalData;


    public PreviewCommonStructure(String type, String id, String name, String imageUrl, Boolean is_active, HashMap<String, String> additionalData) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.is_active = is_active;
        this.additionalData = additionalData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.imageUrl);
        dest.writeValue(this.is_active);
        dest.writeSerializable(this.additionalData);
    }

    protected PreviewCommonStructure(Parcel in) {
        this.type = in.readString();
        this.id = in.readString();
        this.name = in.readString();
        this.imageUrl = in.readString();
        this.is_active = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.additionalData = (HashMap<String, String>) in.readSerializable();
    }

    public static final Creator<PreviewCommonStructure> CREATOR = new Creator<PreviewCommonStructure>() {
        @Override
        public PreviewCommonStructure createFromParcel(Parcel source) {
            return new PreviewCommonStructure(source);
        }

        @Override
        public PreviewCommonStructure[] newArray(int size) {
            return new PreviewCommonStructure[size];
        }
    };
}