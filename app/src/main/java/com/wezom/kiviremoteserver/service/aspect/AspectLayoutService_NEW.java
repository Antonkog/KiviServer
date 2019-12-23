//package com.wezom.kiviremoteserver.service.aspect;
//
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.graphics.PixelFormat;
//import android.os.Build;
//import android.os.Handler;
//import android.os.IBinder;
//import android.provider.Settings;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.widget.CardView;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.wezom.kiviremoteserver.App;
//import com.wezom.kiviremoteserver.R;
//import com.wezom.kiviremoteserver.environment.EnvironmentInputsHelper;
//import com.wezom.kiviremoteserver.environment.EnvironmentPictureSettings;
//import com.wezom.kiviremoteserver.service.aspect.items.IFLMCard;
//import com.wezom.kiviremoteserver.service.aspect.items.InputsCardItem;
//import com.wezom.kiviremoteserver.service.aspect.items.NumericCardItem;
//import com.wezom.kiviremoteserver.service.aspect.items.PictureCardItem;
//import com.wezom.kiviremoteserver.service.aspect.items.RatioCardItem;
//import com.wezom.kiviremoteserver.service.aspect.items.SettingCardItem;
//import com.wezom.kiviremoteserver.service.aspect.items.SoundCardItem;
//import com.wezom.kiviremoteserver.service.aspect.items.TimerCardItem;
//import com.wezom.kiviremoteserver.service.aspect.view.CircleListView;
//import com.wezom.kiviremoteserver.service.aspect.view.IndicatorView;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import wezom.kiviremoteserver.environment.bridge.BridgePicture;
//import wezom.kiviremoteserver.environment.bridge.driver_set.PictureMode;
//import wezom.kiviremoteserver.environment.bridge.driver_set.Ratio;
//import wezom.kiviremoteserver.environment.bridge.driver_set.SoundValues;
//
//
//public class AspectLayoutService_NEW extends Service {
//    public static volatile long lastUpdate;
//    private WindowManager windowManager;
//    private static int mainColor = Color.BLUE;
//    private static final String OSD_TIME = "OSD_TIME";
//
//    private EnvironmentPictureSettings pictureSettings;
//    private EnvironmentInputsHelper inputsHelper;
//
//    private int autoCloseTime = 10;
//    private Handler timer = new Handler();
//    private int generalType = BridgePicture.LAYER_TYPE;//WindowManager.LayoutParams.TYPE_TOAST;
//    private RelativeLayout generalView;
//    RecyclerView mainMenu;
//    CircleListView secondaryMenu;
//    private Runnable updateSleepTime = new Runnable() {
//        @Override
//        public void run() {
////            if (sleepFocused) {
//            // updateSleepText();
////            }
//
//
//            timer.postDelayed(updateSleepTime, 1000);
//            if (autoCloseTime > 0)
//                if (System.currentTimeMillis() - autoCloseTime > lastUpdate) {
//                    stopSelf();
//                }
//        }
//
//
//    };
//
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (App.checkWizard(this)) {
//                return;
//            }
//        }
//        autoCloseTime = Settings.Global.getInt(getContentResolver(), OSD_TIME, 10);
//
//        if (autoCloseTime < 10 && autoCloseTime > 0) {
//            autoCloseTime = 10;
//        }
//
//        //TODO remove autoCloseTime *= 10;
//        autoCloseTime *= 1000000;
//        autoCloseTime *= 1000;
//        mainColor = getResources().getColor(R.color.colorPrimary);
//
//        lastUpdate = System.currentTimeMillis();
//        pictureSettings = new EnvironmentPictureSettings();
//        inputsHelper = new EnvironmentInputsHelper();
//        createLayout(getBaseContext());
//        timer.postDelayed(updateSleepTime, autoCloseTime);
//
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//
//    public void createLayout(Context context) {
//        LayoutInflater layoutInflater = (LayoutInflater)
//                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        windowManager = (WindowManager) context.getApplicationContext()
//                .getSystemService(Context.WINDOW_SERVICE);
//        generalView = (RelativeLayout) View.inflate(context, R.layout.layout_aspect_v2, null);
//        final WindowManager.LayoutParams param = new WindowManager.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT,
//                generalType,//TYPE_SYSTEM_ALERT
//                WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                PixelFormat.TRANSLUCENT);
//        windowManager.addView(generalView, param);
//        generalView.setVisibility(View.VISIBLE);
//        generalView.clearAnimation();
//
//        final Animation anDesk = AnimationUtils.loadAnimation(this, R.anim.outside_bottom);
//        anDesk.setDuration(300);
//        //TODO Animation start
//
//
//        mainMenu = generalView.findViewById(R.id.main_menu);
//        secondaryMenu = generalView.findViewById(R.id.secondary_menu);
//        secondaryMenu.setOnBackListener(v -> {
//            setModeUp(false);
//        });
//        mainMenu.setLayoutManager(new LinearLayoutManager(
//                this, LinearLayoutManager.HORIZONTAL, false
//        ));
//        ArrayList<IFLMCard> items = new ArrayList<>();
//        items.add(new PictureCardItem());
//        items.add(new SoundCardItem());
//        items.add(new InputsCardItem());
//        items.add(new RatioCardItem());
//        items.add(new TimerCardItem());
//        items.add(new NumericCardItem());
//        items.add(new SettingCardItem());
//        MainItemAdapter adapter = new MainItemAdapter(items);
//        mainMenu.setAdapter(adapter);
//
//    }
//
//    public class MainItemViewHolder extends RecyclerView.ViewHolder {
//        public final CardView card;
//        public final View actionsView;
//        public final ImageView imageView;
//        public final TextView title;
//        public final TextView subTitle;
//        public final IndicatorView progressIndicator;
//
//        MainItemViewHolder(View view) {
//            super(view);
//            card = view.findViewById(R.id.card);
//            actionsView = view.findViewById(R.id.actions_view);
//            imageView = view.findViewById(R.id.imageView);
//            title = view.findViewById(R.id.title);
//            subTitle = view.findViewById(R.id.subTitle);
//            progressIndicator = view.findViewById(R.id.progressIndicator);
//
//
////            title.getPaint().setDither(false);
////            title.getPaint().setLinearText(true);
////            title.getPaint().setStrokeWidth(0.2f);
////            title.getPaint().setStyle(Paint.Style.STROKE);
//        }
//    }
//
//    class MainItemAdapter extends RecyclerView.Adapter<MainItemViewHolder> {
//        List<IFLMCard> items;
//
//        MainItemAdapter(List<IFLMCard> items) {
//            this.items = items;
//        }
//
//        @NonNull
//        @Override
//        public MainItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
//            MainItemViewHolder holder = new MainItemViewHolder(view);
//
//            holder.card.setOnKeyListener((v, keyCode, event) -> {
//                if (event.getAction() == KeyEvent.ACTION_UP
//                        && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
//                    setModeUp(true);
//                    return true;
//                }
//                if (event.getAction() == KeyEvent.ACTION_UP
//                        && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
//                    stopSelf();
//                    return true;
//                }
//                return
//                        event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP ||
//                                event.getKeyCode() == KeyEvent.KEYCODE_BACK;
//            });
//            holder.card.setOnFocusChangeListener((v, hasFocus) -> {
//                holder.card.setScaleY(hasFocus ? 1.1f : 1);
//                holder.card.setScaleX(hasFocus ? 1.1f : 1);
//                holder.actionsView.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
//                holder.itemView.setTranslationZ(hasFocus ? 5 : 0);
//                holder.card.setCardBackgroundColor(hasFocus ?
//                        0xFF444444 : 0xFF333333
//                );
//
//            });
//            holder.card.requestFocus();
//            return holder;
//
//        }
//
//        int i = 0;
//
//        @Override
//        public void onBindViewHolder(@NonNull MainItemViewHolder holder, int position) {
//            items.get(position).setViewHolder(holder);
//        }
//
//        @Override
//        public int getItemCount() {
//            return items.size();
//        }
//    }
//
//    private void setModeUp(boolean isTop) {
//        if (isTop) {
//            secondaryMenu.requestFocus(View.FOCUS_DOWN, null);
//        } else {
//            mainMenu.getChildAt(0).requestFocus();
//        }
//        secondaryMenu.animate().alpha(isTop ? 1 : 0).translationY(isTop ? 0 : 300).setDuration(400).start();
//        mainMenu.animate()/*.alpha(isTop ? 0.9f : 1)*/.translationY(!isTop ? 0 : 200).setDuration(400).start();
//    }
//
//
//    @Override
//    public void onDestroy() {
//        try {
//            timer.removeCallbacks(updateSleepTime);
//        } catch (Exception e) {
//
//        }
//        if (windowManager != null)
//            windowManager.removeView(generalView);
//        super.onDestroy();
//    }
//}
