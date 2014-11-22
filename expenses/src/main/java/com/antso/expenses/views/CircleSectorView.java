package com.antso.expenses.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.antso.expenses.R;
import com.antso.expenses.utils.Utils;

public class CircleSectorView extends View {

    private Paint circleFill;
    private Paint circleStroke;
    private RectF circleArc;
    private RectF circleStrokeArc;

    // Attrs
    private int circleRadius;
    private int circleFillColor;
    private int circleStartAngle;
    private int circleSweepAngle;
    private int strokeWidth;

    public CircleSectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs); // Read all attributes

        circleFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleFill.setStyle(Paint.Style.FILL);
        circleStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        circleStroke.setStyle(Paint.Style.STROKE);
        circleStroke.setStrokeWidth(strokeWidth);
    }

    public void init(AttributeSet attrs) {
        TypedArray attrsArray = getContext().obtainStyledAttributes(attrs, R.styleable.circleSectorView);
        circleRadius = Utils.dpToPx(attrsArray.getInteger(R.styleable.circleSectorView_cRadiusDp, 0), getContext());
        circleFillColor = attrsArray.getColor(R.styleable.circleSectorView_cFillColor, 16777215);
        circleStartAngle = attrsArray.getInteger(R.styleable.circleSectorView_cAngleStart, 0);
        circleSweepAngle = attrsArray.getInteger(R.styleable.circleSectorView_cAngleSweep, 360);
        strokeWidth = attrsArray.getInteger(R.styleable.circleSectorView_cStrokeWidth, 0);

        attrsArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        circleFill.setColor(circleFillColor);
        circleStroke.setColor(circleFillColor);
        canvas.drawArc(circleArc, circleStartAngle, circleSweepAngle, true, circleFill);
        if (strokeWidth > 0) {
            canvas.drawArc(circleStrokeArc, 0, 360, true, circleStroke);
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureWidth(widthMeasureSpec);
        int circleDiameter = circleRadius * 2;
        circleArc = new RectF(0, 0, circleDiameter, circleDiameter);
        if (strokeWidth > 0) {
            circleStrokeArc = new RectF(strokeWidth / 2, strokeWidth / 2, circleDiameter - strokeWidth / 2,
                    circleDiameter - strokeWidth / 2);
        }
        int measuredHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measureHeight(int measureSpec) {
        int result = circleRadius * 2;
        return result;
    }

    private int measureWidth(int measureSpec) {
        int result = circleRadius * 2;
        return result;
    }

    public void setColor(int color) {
        this.circleFillColor = color;
    }

    public void setCirclePercentage(int value) {
        if (value == 0) {
            this.circleSweepAngle = 1;
            return;
        }
        if (value == 100) {
            this.circleSweepAngle = 360;
            return;
        }

        this.circleSweepAngle = 360 * value / 100;
    }

    public int getColor() {
        return circleFillColor;
    }
}