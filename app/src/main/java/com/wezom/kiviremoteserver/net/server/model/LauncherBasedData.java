package com.wezom.kiviremoteserver.net.server.model;

import android.os.Parcelable;

import java.util.HashMap;

public interface LauncherBasedData extends Parcelable {
    String getID ();
    String getName();
    String getImageUrl();
    Boolean isActive();
    TYPE getType();
    HashMap<String,String> getAdditionalData();

    enum TYPE  {
        RECOMMENDATION,
        CHANNEL,
        FAVOURITE,
        APPLICATION,
        INPUT,
        TV_PREVIEW
    }
}
