package com.wezom.kiviremoteserver.interfaces;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.wezom.kiviremoteserver.common.KiviProtocolStructure;

import java.util.List;

/**
 * Created by andre on 02.06.2017.
 */
public class DataStructure {

    @Deprecated
    @SerializedName("internal")
    @Expose
    private String internal;

    @SerializedName("action")
    @Expose
    private KiviProtocolStructure.ExecActionEnum action;

    @SerializedName("args")
    @Expose
    private List<String> args;

    @SerializedName("motion")
    @Expose
    private List<Float> motion;

    @SerializedName("mouseEvent")
    @Expose
    private int mouseEvent;

    @SerializedName("package_name")
    private String packageName;

    public DataStructure(KiviProtocolStructure.ExecActionEnum action, List<String> args, List<Float> motion, int mouseEvent, String packageName) {
        this.action = action;
        this.args = args;
        this.motion = motion;
        this.mouseEvent = mouseEvent;
        this.packageName = packageName;
    }

    public KiviProtocolStructure.ExecActionEnum getAction() {
        return action;
    }

    @Deprecated
    public String getInternal() {
        return internal;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<Float> getMotion() {
        return motion;
    }

    public String getPackageName() {
        return packageName;
    }
}
