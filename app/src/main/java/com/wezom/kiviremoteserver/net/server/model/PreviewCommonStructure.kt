package com.wezom.kiviremoteserver.net.server.model

import java.util.HashMap

data class PreviewCommonStructure(
        val type: String,
        val id: String, // portNum as int, other as string app - packageName
        val name: String?,  // title for Recommendation, portName for inputs, name for channel and app
        val icon: String?, // if no url (mutable instance) - we send bitmap as base64 encoded string
        val imageUrl: String?,  // for channels and recommendations exist, for app's currently will be base64.
        val is_active: Boolean?,  //for channels ports and
        val additionalData: HashMap<String, String>?)

/*
    enum LauncherBasedData.TYPE  {
        RECOMMENDATION,
        CHANNEL,
        FAVOURITE,
        APPLICATION,
        INPUT
    }

    additionalData  contains next values that in not used for now, but we may need them in future.
 What will be in additionalData:

    private Boolean adult_content;
    private float imdb;
    private String created_at;
    private String edited_at;
    private Boolean has_timeshift;
    private String monetizationType;
    private String subTitle;
    private String description;

 */

