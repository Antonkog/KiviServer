package com.wezom.kiviremoteserver.service.aspect.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class CylinderContainerView extends RecyclerView {


    public CylinderContainerView(Context context) {
        super(context);
    }

    public CylinderContainerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CylinderContainerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
    }
}
