package com.wezom.kiviremoteserver.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.wezom.kiviremoteserver.R;
import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
import com.wezom.kiviremoteserver.service.AspectLayoutService;

import java.util.ArrayList;
import java.util.List;


public class NumberDialing extends FrameLayout implements View.OnKeyListener {
    public NumberDialing(@NonNull Context context) {
        super(context);
        init();
    }

    public NumberDialing(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NumberDialing(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NumberDialing(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    TextView channelText;
    List<View> list = new ArrayList<>();

    private void init() {
        final LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        inflate(getContext(), R.layout.channel_selecter, this);
        setFocusable(false);
        channelText = findViewById(R.id.value);
        initClickListeners(this);
        findViewById(R.id.change_channel).setOnClickListener(v -> {
            if (channelText.getText().length() > 0) {
                new EnvironmentInputsHelper().changeProgram(Integer.parseInt(channelText.getText().toString()), getContext());
                getContext().stopService(new Intent(getContext(), AspectLayoutService.class));
            }
        });
        findViewById(R.id.delete).setOnClickListener(v -> {
            try {
                AspectLayoutService.lastUpdate = System.currentTimeMillis();
                CharSequence text = channelText.getText();
                if (text.length() > 0) {
                    channelText.setText(text.subSequence(0, text.length() - 1));
                }
            } catch (Exception e) {

            }
        });

    }

    private void initClickListeners(ViewGroup group) {
        if (group != null)
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    if (textView.isClickable()) {
                        textView.setOnClickListener(v -> {
                            CharSequence charSequence = channelText.getText();
                            if (charSequence == null)
                                charSequence = "";
                            String text = charSequence.toString();
                            if (text.length() > 2) {
                                text = "";
                            }
                            text += textView.getText();
                            channelText.setText(text);
                        });
                        textView.setOnKeyListener(this);
                        list.add(textView);
                    }
                } else if (child instanceof ImageView) {
                    child.setOnKeyListener(this);
                    list.add(child);
                } else if (child instanceof ViewGroup) {
                    initClickListeners((ViewGroup) child);
                }
            }
    }

    public void request() {
        findViewById(R.id.zero).requestFocus();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        AspectLayoutService.lastUpdate = System.currentTimeMillis();
        int i = list.indexOf(v);
        if (i < 0) {
            i = -10;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                i += 1;
                if (i >= 0 && i < list.size()) {
                    View view = list.get(i);
                    if (view != null) view.requestFocus();
                }
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                i -= 1;
                if (i >= 0 && i < list.size()) {
                    View view = list.get(i);
                    if (view != null) view.requestFocus();
                }
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                i -= 3;
                if (i >= 0 && i < list.size()) {
                    View view = list.get(i);
                    if (view != null) view.requestFocus();
                }
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                i += 3;
                if (i >= 0 && i < list.size()) {
                    View view = list.get(i);
                    if (view != null) view.requestFocus();
                }
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || keyCode == KeyEvent.KEYCODE_TV_ZOOM_MODE) {
            if (event.getAction() == KeyEvent.ACTION_UP)
                getContext().stopService(new Intent(getContext(), AspectLayoutService.class));
            return true;
        }
        return false;
    }
}
