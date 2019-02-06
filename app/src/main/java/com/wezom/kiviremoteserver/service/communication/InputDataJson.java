package com.wezom.kiviremoteserver.service.communication;

import android.content.Context;

import com.wezom.kiviremoteserver.service.inputs.InputSourceHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InputDataJson implements Serializable {
    private List<InputItemJson> list;
    private long createTime;

    protected InputDataJson(List<InputItemJson> list) {
        this.list = new ArrayList<>(list);
        createTime = System.currentTimeMillis();
    }

    public List<InputItemJson> getList() {
        return list;
    }

    public long getCreateTime() {
        return createTime;
    }
}
