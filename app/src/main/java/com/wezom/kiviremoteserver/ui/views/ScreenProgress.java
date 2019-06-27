package com.wezom.kiviremoteserver.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.service.AspectLayoutService;

public class ScreenProgress extends FrameLayout {
    UpdateListener listener;
    int progress;

    public ScreenProgress(Context context) {
        super(context);
        init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        AspectLayoutService.lastUpdate = System.currentTimeMillis();

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                setProgress(progress + 1);
                hideOther(true);

                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                setProgress(progress - 1);
                hideOther(true);

                return true;
            default:
                hideOther(false);

                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void hideOther(boolean b) {
//        if (parent == null) {
//            parent = ((ViewGroup) getParent());
//        }
//        if (parent != null) {
//            if (b) {
//
//                ((View) parent.getParent()).setBackgroundColor(Color.TRANSPARENT);
//                parent.setBackgroundColor(Color.TRANSPARENT);
//            } else {
//                ((View) parent.getParent()).setBackgroundColor(Color.GRAY);
//                parent.setBackgroundColor(Color.GRAY);
//            }
//
//            for (int i = 0; i < parent.getChildCount(); i++) {
//                View view = parent.getChildAt(i);
//                view.setVisibility(view != this && b ? INVISIBLE : VISIBLE);
//
//            }
//        }
    }

//    @Override
//    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
//        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
//        if (!gainFocus) {
//            hideOther(false);
//            animate().scaleY(1).start();
//        } else {
//            animate().scaleY(1.3f).start();
//        }
//        AspectLayoutService.lastUpdate = System.currentTimeMillis();
//
//    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_TV_ZOOM_MODE) {
            getContext().stopService(new Intent(getContext(), AspectLayoutService.class));
            return true;
        }
        AspectLayoutService.lastUpdate = System.currentTimeMillis();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return true;
            default:
                break;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_TV_ZOOM_MODE) {
            getContext().stopService(new Intent(getContext(), AspectLayoutService.class));
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    public ScreenProgress(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScreenProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ScreenProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    TextView textView;
    ImageView image;
    TextView prValue;
    ProgressBar progressBar;

    public void setIcon(@DrawableRes int resource) {
        image.setImageResource(resource);
    }

    public void setLable(@StringRes int resource) {
        textView.setText(resource);
    }

    ViewGroup parent;

    private void init() {
        parent = ((ViewGroup) getParent());
        final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        inflate(getContext(), R.layout.progress, this);
        setFocusable(true);
        textView = findViewById(R.id.label);
        prValue = findViewById(R.id.progress_value);
        image = findViewById(R.id.image);
        progressBar = findViewById(R.id.progress_bar);
        setBackgroundResource(R.drawable.aspect_header_background);
//        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//        setLayoutParams(layoutParams);
    }

    public void setProgressListener(UpdateListener listener) {
        this.listener = listener;
    }

    public void setProgress(int progress) {
        if (progress > 100)
            progress = 100;
        if (progress < 0)
            progress = 0;
        this.progress = progress;
        prValue.setText("" + progress);
        progressBar.setProgress(progress);
        if (listener != null) {
            listener.progressUpdate(progress);
        }
    }
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
