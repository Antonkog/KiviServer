package com.wezom.kiviremoteserver.service.aspect.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wezom.kiviremoteserver.R;

public class CircleListView extends FrameLayout {
    private Paint paint;
    private Paint paintW;

    public CircleListView(Context context) {
        super(context);
        init();
    }


    public CircleListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CircleListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        recycler_view.getChildAt(0).requestFocus();
        return true;
    }

    RecyclerView recycler_view;

    private void init() {
        inflate(getContext(), R.layout.circle_list, this);
        recycler_view = findViewById(R.id.recycler_view);
        ProgressBar progress_bar = findViewById(R.id.progress_bar);
        TextView progress_value = findViewById(R.id.progress_value);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recycler_view.setLayoutManager(manager);
        recycler_view.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (oldBottom == oldTop && oldTop == 0) {
                View child = recycler_view.getChildAt(0);
                if (child != null)
                    recycler_view.setPadding(
                            recycler_view.getPaddingLeft(),
                            (recycler_view.getHeight() - child.getHeight()) / 2,
                            recycler_view.getPaddingRight(),
                            (recycler_view.getHeight() - child.getHeight()) / 2);
            }
        });
        // view.getLayoutManager();

        recycler_view.setAdapter(new RecyclerView.Adapter() {
//            Handler handler = new Handler();
//            int position;
//
//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    View next = manager.findViewByPosition(position);
//                    if (next != null) {
//                        next.animate().alpha(1).scaleY(1).scaleX(1).setDuration(50).start();
//                    }
//                    next = manager.findViewByPosition(position + 1);
//                    if (next != null) {
//                        next.animate().alpha(0.7f).scaleY(0.7f).scaleX(0.7f).setDuration(50).start();
//                    }
//                    next = manager.findViewByPosition(position - 1);
//                    if (next != null) {
//                        next.animate().alpha(0.7f).scaleY(0.7f).scaleX(0.7f).setDuration(50).start();
//                    }
//                    next = manager.findViewByPosition(position - 2);
//                    if (next != null) {
//                        next.animate().alpha(0.4f).scaleY(0.4f).scaleX(0.4f).setDuration(50).start();
//                    }
//                    next = manager.findViewByPosition(position + 2);
//                    if (next != null) {
//                        next.animate().alpha(0.4f).scaleY(0.4f).scaleX(0.4f).setDuration(50).start();
//                    }
//                }
//            };

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view1 = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.circle_item, viewGroup, false);
                RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view1) {
                    @Override
                    public String toString() {
                        return super.toString();
                    }
                };
                view1.setPivotX(0);
                holder.itemView.setPivotY(holder.itemView.getHeight() / 3.3f);
                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                ((TextView) viewHolder.itemView.findViewById(R.id.text)).setText("Brightness " + i);

                viewHolder.itemView.setOnKeyListener((v, keyCode, event) -> {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        progress_bar.setProgress(progress_bar.getProgress() + 1);
                        progress_value.setText("" + progress_bar.getProgress());
                        return true;
                    }
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        progress_bar.setProgress(progress_bar.getProgress() - 1);
                        progress_value.setText("" + progress_bar.getProgress());
                        return true;
                    }
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                        backListener.onClick(CircleListView.this);
                        return true;
                    }
                    return false;
                });
                viewHolder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
//      TODO                  {
//                            position = i;
//                            handler.removeCallbacks(runnable);
//                            handler.postDelayed(runnable, 40);
//                        }

                        progress_value.setText("" + (i * 10) % 100);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            progress_bar.setProgress((i * 10) % 100, true);
                        } else {
                            progress_bar.setProgress((i * 10) % 10);

                        }


                    }
                });
            }

            @Override
            public int getItemCount() {
                return 1000;
            }
        });

        manager.scrollToPosition(100);


        if (isInEditMode()) {

        }
    }

    OnClickListener backListener;

    public void setOnBackListener(OnClickListener onClickListener) {
        backListener = onClickListener;
    }

//    class CircleLayoutManager extends LinearLayoutManager {
//        public CircleLayoutManager(Context context) {
//            super(context);
//        }
//
//        long time;
//
//        @Override
//        public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
//            int orientation = getOrientation();
//
//            if (orientation == VERTICAL) {
//                int scrolled = super.scrollVerticallyBy(dy, recycler, state);
//                if ((System.currentTimeMillis() - time > 40)) {
//                    time = System.currentTimeMillis();
//                    float midpoint = getHeight() / 2.f;
//                    for (int i = 0; i < getChildCount(); i++) {
//                        View child = getChildAt(i);
//                        float childMidpoint =
//                                (getDecoratedBottom(child) + getDecoratedTop(child)) / 2.f;
//                        float scale = (float) (1 - Math.abs(childMidpoint - midpoint) / midpoint);
//                        child.setScaleX(scale);
//                        child.setScaleY(scale);
//                        child.setAlpha(scale);
//                    }
//                }
//                return scrolled;
//            } else {
//                return 0;
//            }
//        }
//    }
}
