package com.antso.expensesmanager.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.antso.expensesmanager.R;

public class CircleSectorView extends View {

    private Paint circlePaint;
    private RectF circleArc;

    // Attrs
    private int circleRadius;
    private int circleFillColor;
    private int circleStartAngle;
    private int circleEndAngle;

    public CircleSectorView(Context context, AttributeSet attrs) {

        super(context, attrs);
        init(attrs); // Read all attributes

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    public void init(AttributeSet attrs)
    {
        TypedArray attrsArray = getContext().obtainStyledAttributes(attrs, R.styleable.circleSectorView);
        circleRadius = dpToPx(attrsArray.getInteger(R.styleable.circleSectorView_cRadiusDp, 0));
        circleFillColor = attrsArray.getColor(R.styleable.circleSectorView_cFillColor, 16777215);
        circleStartAngle = attrsArray.getInteger(R.styleable.circleSectorView_cAngleStart, 0);
        circleEndAngle = attrsArray.getInteger(R.styleable.circleSectorView_cAngleEnd, 360);
        attrsArray.recycle();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        circlePaint.setColor(circleFillColor);
        canvas.drawArc(circleArc, circleStartAngle, circleEndAngle, true, circlePaint);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {

        int measuredWidth = measureWidth(widthMeasureSpec);
//        if(circleRadius == 0) // No radius specified.
//        {                     // Lets see what we can make.
//            // Check width size. Make radius half of available.
//            circleRadius = measuredWidth / 2;
//            int tempRadiusHeight = measureHeight(heightMeasureSpec) / 2;
//            if(tempRadiusHeight < circleRadius)
//                // Check height, if height is smaller than
//                // width, then go half height as radius.
//                circleRadius = tempRadiusHeight;
//        }
        int circleDiameter = circleRadius * 2;
        circleArc = new RectF(0, 0, circleDiameter, circleDiameter);
        int measuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);

        int wspecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wspecSize = MeasureSpec.getSize(widthMeasureSpec);
        int hspecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hspecSize = MeasureSpec.getSize(heightMeasureSpec);
        Log.d("onMeasure() O::", "measuredHeight =>" + hspecMode + "-" + String.valueOf(hspecSize)
                + "px measuredWidth => " + wspecMode + "-" + String.valueOf(wspecSize) + "px");
        Log.d("onMeasure() C::", "measuredHeight =>" + String.valueOf(measuredHeight) + "px measuredWidth => " + String.valueOf(measuredWidth) + "px");
    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = circleRadius * 2;
//        if (specMode == MeasureSpec.AT_MOST) {
//            result = circleRadius * 2;
//        } else if (specMode == MeasureSpec.EXACTLY) {
//            result = specSize;
//        }
        return result;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = circleRadius * 2;
//        if (specMode == MeasureSpec.AT_MOST) {
//            result = circleRadius * 2;
//        } else if (specMode == MeasureSpec.EXACTLY) {
//            result = specSize;
//        }
        return result;
    }
}