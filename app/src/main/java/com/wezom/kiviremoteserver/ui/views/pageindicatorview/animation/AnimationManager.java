package com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation;


import com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation.controller.AnimationController;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation.controller.ValueController;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.draw.data.Indicator;

public class AnimationManager {

    private AnimationController animationController;

    public AnimationManager(Indicator indicator, ValueController.UpdateListener listener) {
        this.animationController = new AnimationController(indicator, listener);
    }

    public void basic() {
        if (animationController != null) {
            animationController.end();
            animationController.basic();
        }
    }

    public void interactive(float progress) {
        if (animationController != null) {
            animationController.interactive(progress);
        }
    }

    public void end() {
        if (animationController != null) {
            animationController.end();
        }
    }
}
