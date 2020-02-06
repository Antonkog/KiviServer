package com.wezom.kiviremoteserver.ui.views;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.service.AspectLayoutService;
import com.wezom.kiviremoteserver.service.aspect.values.TextTypedValues;

import java.util.Arrays;
import java.util.List;

public class LRTextSwitcher extends FrameLayout {
    UpdateListener listener;
    int progress;
    TextView textView;
    ImageView image;
    //TextView prValue;
    TextSwitcher textSwitcher;

    public LRTextSwitcher(Context context) {
        super(context);
        init();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

//        AspectLayoutService.lastUpdate = System.currentTimeMillis();
        AspectLayoutService.Companion.setLastUpdate(System.currentTimeMillis());
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                textSwitcher.setInAnimation(getContext(), android.R.anim.slide_in_left);
                textSwitcher.setOutAnimation(getContext(), android.R.anim.slide_out_right);
                setValue(progress + 1);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                textSwitcher.setInAnimation(getContext(), R.anim.slide_in_right);
                textSwitcher.setOutAnimation(getContext(), R.anim.slide_out_left);
                setValue(progress - 1);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        AspectLayoutService.lastUpdate = System.currentTimeMillis();
        AspectLayoutService.Companion.setLastUpdate(System.currentTimeMillis());
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


    public LRTextSwitcher(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LRTextSwitcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LRTextSwitcher(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    public void setIcon(@DrawableRes int resource) {
        image.setImageResource(resource);
    }

    public void setLable(@StringRes int resource) {
        textView.setText(resource);
    }


    private void init() {
        final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        inflate(getContext(), R.layout.typed_text, this);
        setFocusable(true);
        textView = findViewById(R.id.label);
        image = findViewById(R.id.image);
        textSwitcher = findViewById(R.id.text_switcher);

        setBackgroundResource(R.drawable.aspect_header_background);
//        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//        setLayoutParams(layoutParams);
    }

    List<TextTypedValues> values;

    public void setUpValues(TextTypedValues... values) {
        this.values = Arrays.asList(values);
    }

    public void setProgressListener(UpdateListener listener) {
        this.listener = listener;
    }

    public void setValue(TextTypedValues item) {
        setValue(values.indexOf(item));
    }

    public void setValue(int progress) {
        if (progress < 0) {
            progress = values.size() - 1;
        }
        if (progress >= values.size()) {
            progress = 0;
        }
        this.progress = progress;

        if (textSwitcher == null) {
            textSwitcher = findViewById(R.id.text_switcher);
        }
        if (textSwitcher != null)
            textSwitcher.setText(" " + getResources().getString(values.get(progress).getStringResourceID()));

        if (listener != null) {
            listener.progressUpdate(values.get(progress).getID());
        }
//        if (progress > 100)
//            progress = 100;
//        if (progress < 0)
//            progress = 0;
//        this.progress = progress;
//        prValue.setText("" + progress);
//        progressBar.setProgress(progress);
//        if (listener != null) {
//            listener.progressUpdate(progress);
//        }
    }
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
