package com.wezom.kiviremoteserver.service.aspect.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class IndicatorView extends View {
    private Paint paint;
    private Paint paintW;

    public IndicatorView(Context context) {
        super(context);
        init();
    }


    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (isInEditMode()) {
            setNumber(5);
            setSelected(2);
        }
    }


    float radius = 2.5f;
    int step = 12;
    int selected = 10;
    int number;

    public void setNumber(int number) {
        paint = new Paint();
        paint.setColor(0XFF0F6BFB);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paintW = new Paint();

        paintW.setColor(Color.WHITE);
        paintW.setStyle(Paint.Style.FILL_AND_STROKE);
        paintW.setAntiAlias(true);
        paintW.setFlags(Paint.ANTI_ALIAS_FLAG);
//        paint.setFlags(Paint.DITHER_FLAG);
//        paintW.setFlags(Paint.DITHER_FLAG);
        this.number = number;
        postInvalidate();
    }

    public int getNumber() {
        return number;
    }

    public void setSelected(int selected) {
        if (selected > number) {
            selected %= number;
        }
        this.selected = selected;
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (number > 0) {
            //canvas.drawColor(Color.RED);
            float first = (getWidth() - radius - step * (number - 1)) / 2;
            for (int i = 0; i < number; i++)
                if (i == selected) {
                    canvas.drawCircle(step * i + first, getHeight() / 2, radius * 2.0f, paintW);
                    canvas.drawCircle(step * i + first, getHeight() / 2, radius * 1.7f, paint);
                } else {
                    canvas.drawCircle(step * i + first, getHeight() / 2, radius, paintW);
                }
        }
    }
}
