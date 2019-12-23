//package com.wezom.kiviremoteserver.service.aspect.items;
//
//import android.support.annotation.DrawableRes;
//import android.support.annotation.StringRes;
//import android.view.KeyEvent;
//import android.view.View;
//
//import com.wezom.kiviremoteserver.service.aspect.aspect_v2.CardsMainAdapter;
//
//import java.util.List;
//
//public abstract class IFLMCard {
//    public CardsMainAdapter.CardMainViewHolder holder;
//
//    @DrawableRes
//    public int image;
//    @StringRes
//    public int mainText;
//    @StringRes
//    public int secondText;
//    public List<? extends IFLMItems> values;
//
//    public IFLMCard() {
//
//    }
//
//    int i = 0;
//
////    public void setViewHolder(CardsMainAdapter.CardMainViewHolder holder) {
////        this.holder = holder;
////        holder.getTitle().setText("Hello1");
////
////        holder.getImageView().setImageResource(image);
////
////        if (values != null) {
////            holder.getSubTitle().setText(values.get(2 % values.size()).getStringRes());
////            holder.getCard().setOnClickListener(v -> {
////                holder.getProgressIndicator().setSelected(i++);
////                holder.getSubTitle().setText(values.get(i % values.size()).getStringRes());
////            });
////            holder.getProgressIndicator().setVisibility(View.VISIBLE);
////            holder.getProgressIndicator().setNumber(values.size());
////            holder.getProgressIndicator().setSelected(2);
////        } else {
////            holder.getSubTitle().setText(subTitle);
////            holder.getCard().setOnClickListener(null);
////            holder.getProgressIndicator().setVisibility(View.INVISIBLE);
////        }
////    }
//
//
//    public abstract void onClick();
//
//    public boolean onKey(KeyEvent event) {
//        return false;
//    }
//}
