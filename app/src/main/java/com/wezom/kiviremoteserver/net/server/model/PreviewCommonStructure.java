package com.wezom.kiviremoteserver.net.server.model;

import java.util.HashMap;

public class PreviewCommonStructure {
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
    public String toString() {
        return "PreviewCommonStructure{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", is_active=" + is_active +
                ", additionalData=" + additionalData +
                '}';
    }
}