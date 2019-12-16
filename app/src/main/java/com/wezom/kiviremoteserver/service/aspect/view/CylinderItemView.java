package com.wezom.kiviremoteserver.service.aspect.view;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class CylinderItemView extends RelativeLayout {


    public CylinderItemView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("CylinderItemView", "1");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.e("CylinderItemView", "2" + ":" + t);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        Log.e("CylinderItemView", "3" + ":" + scrollY);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.e("CylinderItemView", "4");
        return super.onNestedFling(target, velocityX, velocityY, consumed);

    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.e("CylinderItemView", "5");
        return super.onStartNestedScroll(child, target, nestedScrollAxes);
    }


    public CylinderItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CylinderItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CylinderItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
                -> Log.e("CylinderItemView", "top " + top + " : " + CylinderItemView.this.getId()));
//        if (isInEditMode()) {
//            setNumber(5);
//            setSelected(2);
//        }
    }


}
