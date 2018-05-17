package com.wezom.kiviremoteserver.keyboardsample.softkeyboard;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class SkbContainer extends RelativeLayout implements View.OnTouchListener {
    public SkbContainer(Context context) {
        super(context);
    }

    public SkbContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SkbContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
