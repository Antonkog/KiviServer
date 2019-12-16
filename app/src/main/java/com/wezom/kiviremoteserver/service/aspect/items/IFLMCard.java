package com.wezom.kiviremoteserver.service.aspect.items;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.KeyEvent;
import android.view.View;

import com.wezom.kiviremoteserver.service.aspect.AspectLayoutService_NEW;

import java.util.List;

public abstract class IFLMCard {
    AspectLayoutService_NEW.MainItemViewHolder holder;

    @DrawableRes
    protected int image;
    @StringRes
    protected int mainText;
    @StringRes
    protected int secondText;
    List<? extends IFLMItems> values;

    public IFLMCard() {

    }

    int i = 0;

    public void setViewHolder(AspectLayoutService_NEW.MainItemViewHolder holder) {
        this.holder = holder;
        holder.mainText.setText(mainText);

        holder.imageView.setImageResource(image);

        if (values != null) {
            holder.secondText.setText(values.get(2 % values.size()).getStringRes());
            holder.card.setOnClickListener(v -> {
                holder.progressIndicator.setSelected(i++);
                holder.secondText.setText(values.get(i % values.size()).getStringRes());
            });
            holder.progressIndicator.setVisibility(View.VISIBLE);
            holder.progressIndicator.setNumber(values.size());
            holder.progressIndicator.setSelected(2);
        } else {
            holder.secondText.setText(secondText);
            holder.card.setOnClickListener(null);
            holder.progressIndicator.setVisibility(View.INVISIBLE);
        }
    }

    public abstract void onClick();

    public boolean onKey(KeyEvent event) {
        return false;
    }
}
