package com.wezom.kiviremoteserver.service.inputs;


import com.wezom.kiviremoteserver.R;

/**
 * Created by sergiigudym on 1/14/18.
 */

public class InputSourceItem {

    public static final int USER_DEFINE_ITEM_TYPE_NONE = 0;

    public static final int USER_DEFINE_ITEM_TYPE_FILEBROWSER = 1;

    private String inputSourceName = null;

    private String ttsSourceName = null;

    private boolean signalFlag = true;

    private boolean selectFlag = false;

    // 0 means not tv signal ,1means tv signal,other
    private int typeFlag = 0;

    // number obligate for later.
    private int positon = 0;

    private int userDefineItemType = USER_DEFINE_ITEM_TYPE_NONE;

    private int mSelectedResId = R.drawable.no_preview;

    private int mUnselectedResId = R.drawable.no_preview;

    int bottomColor;

    public int getBottomColor() {
        return bottomColor;
    }

    public void setBottomColor(int bottomColor) {
        this.bottomColor = bottomColor;
    }

    public boolean isSelectFlag() {
        return selectFlag;
    }

    public void setSelectFlag(boolean selectFlag) {
        this.selectFlag = selectFlag;
    }

    public int getPositon() {
        return positon;
    }

    public void setPositon(int positon) {
        this.positon = positon;
    }

    public String getInputSourceName() {
        return inputSourceName;
    }

    public void setInputSourceName(String inputSourceName) {
        this.inputSourceName = inputSourceName;
    }

    public String getTtsInputSourceName() {
        return ttsSourceName;
    }

    public void setTtsInputSourceName(String name) {
        this.ttsSourceName = name;
    }

    public boolean isSignalFlag() {
        return signalFlag;
    }

    public void setSignalFlag(boolean signalFlag) {
        this.signalFlag = signalFlag;
    }

    public int getTypeFlag() {
        return typeFlag;
    }

    public void setTypeFlag(int typeFlag) {
        this.typeFlag = typeFlag;
    }

    public void setUserDefineItemType(int type) {
        this.userDefineItemType = type;
    }

    public int getUserDefineItemType() {
        return userDefineItemType;
    }

    public boolean isUserDefineItem() {
        if (userDefineItemType != USER_DEFINE_ITEM_TYPE_NONE) {
            return true;
        } else {
            return false;
        }
    }

    public void setSelectedResId(int id) {
        mSelectedResId = id;
    }

    public int getSelectedResId() {
        return mSelectedResId;
    }

    public void setUnselectedResId(int id) {
        mUnselectedResId = id;
    }

    public int getUnselectedResId() {
        return mUnselectedResId;
    }
}
