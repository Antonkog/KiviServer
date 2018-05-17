package com.wezom.kiviremoteserver.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.wezom.kiviremoteserver.R;

import timber.log.Timber;

import static com.wezom.kiviremoteserver.common.Utils.getBitmapFromVectorDrawable;

/**
 * Created by andre on 12.06.2017.
 */

public class OverlayView extends ViewGroup {
    private boolean showCursor;

    private Bitmap cursor;
    private float x = 0, y = 0;

    private int maxWidth = 1920;
    private int maxHeight = 720;

    public void update(float nx, float ny) {
        x = nx;
        y = ny;
    }

    public void showCursor(boolean status) {
        showCursor = status;
    }

    public OverlayView(Context context) {
        super(context);
        cursor = getBitmapFromVectorDrawable(context, R.drawable.ic_cursor);
        setBackgroundColor(Color.TRANSPARENT);

        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        maxWidth = size.x;
        maxHeight = size.y;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (maxHeight <= y) {
            return;
        }
        if (maxWidth <= x) {
            return;
        }
        if (showCursor) {
            canvas.drawBitmap(cursor, x, y, null);
        }
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Timber.d("onTouchEvent %s  %s  %s  %s", event.getY(), event.getX(), event.getAction(), event.getActionMasked());
        super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        Timber.d("onInterceptTouchEvent %s %s  %s  %s", e.getY(), e.getX(), e.getAction(), e.getActionMasked());
        onTouchEvent(e);
        return false;
    }
}