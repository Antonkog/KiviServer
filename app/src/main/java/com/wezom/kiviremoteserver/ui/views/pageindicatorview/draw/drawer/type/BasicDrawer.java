package com.wezom.kiviremoteserver.ui.views.pageindicatorview.draw.drawer.type;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.NonNull;

import com.wezom.kiviremoteserver.ui.views.pageindicatorview.animation.type.AnimationType;
import com.wezom.kiviremoteserver.ui.views.pageindicatorview.draw.data.Indicator;

public class BasicDrawer extends BaseDrawer {

    private Paint strokePaint;
    Paint paintW = new Paint();

    public BasicDrawer(@NonNull Paint paint, @NonNull Indicator indicator) {
        super(paint, indicator);

        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(indicator.getStroke());

        paintW.setColor(Color.WHITE);
        paintW.setStyle(Paint.Style.FILL_AND_STROKE);
        paintW.setAntiAlias(true);
        paintW.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void draw(
            @NonNull Canvas canvas,
            int position,
            boolean isSelectedItem,
            int coordinateX,
            int coordinateY) {

        float radius = indicator.getRadius();
        int strokePx = indicator.getStroke();
        float scaleFactor = indicator.getScaleFactor();

        int selectedColor = indicator.getSelectedColor();
        int unselectedColor = indicator.getUnselectedColor();
        int selectedPosition = indicator.getSelectedPosition();
        AnimationType animationType = indicator.getAnimationType();

		if (animationType == AnimationType.SCALE && !isSelectedItem) {
			radius *= scaleFactor;

		} else if (animationType == AnimationType.SCALE_DOWN && isSelectedItem) {
			radius *= scaleFactor;
		}

        int color = unselectedColor;
        if (position == selectedPosition) {
            color = selectedColor;
        }

        Paint paint;
        if (animationType == AnimationType.FILL && position != selectedPosition) {
            paint = strokePaint;
            paint.setStrokeWidth(strokePx);
        } else {
            paint = this.paint;
        }

        paint.setColor(color);

        Log.d("Draw", "Base: Position = " + position + " SelectedPos = " + selectedPosition + " IsSelected = " + isSelectedItem);

//        if (position == selectedPosition) {
//            canvas.drawCircle(coordinateX, coordinateY, radius * 1.5f, paintW);
//            canvas.drawCircle(coordinateX, coordinateY, radius * 1.3f, paint);
//        } else {
//            canvas.drawCircle(coordinateX, coordinateY, radius, paint);
//        }

        canvas.drawCircle(coordinateX, coordinateY, radius, paint);


    }
}
