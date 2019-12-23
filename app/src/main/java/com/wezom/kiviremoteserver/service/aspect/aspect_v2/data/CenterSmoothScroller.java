package com.wezom.kiviremoteserver.service.aspect.aspect_v2.data;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;

public class CenterSmoothScroller extends LinearSmoothScroller {

    public CenterSmoothScroller(Context context) {
        super(context);
    }

    @Override
    public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
        return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
    }
}