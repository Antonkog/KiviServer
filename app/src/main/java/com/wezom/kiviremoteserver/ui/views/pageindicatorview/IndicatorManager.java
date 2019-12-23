package com.wezom.kiviremoteserver.ui.views.pageindicatorview;


import android.support.annotation.Nullable;

import com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation.AnimationManager;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation.controller.ValueController;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation.data.Value;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.draw.DrawManager;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.draw.data.Indicator;

public class IndicatorManager implements ValueController.UpdateListener {

    private DrawManager drawManager;
    private AnimationManager animationManager;
    private Listener listener;

    interface Listener {
        void onIndicatorUpdated();
    }

    IndicatorManager(@Nullable Listener listener) {
        this.listener = listener;
        this.drawManager = new DrawManager();
        this.animationManager = new AnimationManager(drawManager.indicator(), this);
    }

    public AnimationManager animate() {
        return animationManager;
    }

    public Indicator indicator() {
        return drawManager.indicator();
    }

    public DrawManager drawer() {
        return drawManager;
    }

    @Override
    public void onValueUpdated(@Nullable Value value) {
        drawManager.updateValue(value);
        if (listener != null) {
            listener.onIndicatorUpdated();
        }
    }
}
