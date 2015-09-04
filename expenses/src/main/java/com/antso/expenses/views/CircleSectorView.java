package com.antso.expenses.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.antso.expenses.R;
import com.antso.expenses.utils.MaterialColours;
import com.antso.expenses.utils.Utils;

public class CircleSectorView extends View {

    private Paint fill;
    private Paint stroke;
    private Paint text;
    private RectF arc;
    private RectF strokeArc;


    // Attrs
    private boolean showText;
    private boolean showTextOrig;
    private int radius;
    private int fillColor;
    private int fillColorOrig;
    private int selectionColor;
    private int startAngle;
    private int sweepAngle;
    private int strokeWidth;
    private int percentage = 0;
    private boolean hasPercentage = false;

    public CircleSectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs); // Read all attributes

        fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setStyle(Paint.Style.FILL);
        stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(strokeWidth);
        text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTextAlign(Paint.Align.CENTER);
        text.setColor(Color.BLACK);
        float textSize = (float) (radius * 0.7);
        text.setTextSize(textSize);
    }

    public void init(AttributeSet attrs) {
        TypedArray attrsArray = getContext().obtainStyledAttributes(attrs, R.styleable.circleSectorView);
        radius = Utils.dpToPx(attrsArray.getInteger(R.styleable.circleSectorView_radiusDp, 0), getContext());
        fillColorOrig = attrsArray.getColor(R.styleable.circleSectorView_fillColor, 16777215);
        fillColor = fillColorOrig;
        selectionColor = attrsArray.getColor(R.styleable.circleSectorView_selectionColor, MaterialColours.GREY_500);
        startAngle = attrsArray.getInteger(R.styleable.circleSectorView_angleStart, 0);
        sweepAngle = attrsArray.getInteger(R.styleable.circleSectorView_angleSweep, 360);
        strokeWidth = attrsArray.getInteger(R.styleable.circleSectorView_strokeWidth, 0);
        showTextOrig = attrsArray.getBoolean(R.styleable.circleSectorView_hasText, false);
        showText = showTextOrig;

        attrsArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        fill.setColor(fillColor);
        stroke.setColor(fillColor);
        canvas.drawArc(arc, startAngle, sweepAngle, true, fill);
        if (strokeWidth > 0) {
            canvas.drawArc(strokeArc, 0, 360, true, stroke);
        }

        if (showText) {
            String text = String.valueOf(percentage) + "%";
            canvas.drawText(text,
                    arc.centerX(), arc.centerY() + this.text.getTextSize() / 2 - 8,
                    this.text);
        }

        if (isSelected() && !isInEditMode()) {
            Drawable d = getResources().getDrawable(R.drawable.ic_done);
            int margin = canvas.getWidth() / 4;
            d.setBounds(margin, margin, canvas.getWidth() - margin, canvas.getHeight() - margin);
            d.draw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureWidth();
        int circleDiameter = radius * 2;
        arc = new RectF(0, 0, circleDiameter, circleDiameter);
        if (strokeWidth > 0) {
            strokeArc = new RectF(strokeWidth / 2, strokeWidth / 2, circleDiameter - strokeWidth / 2,
                    circleDiameter - strokeWidth / 2);
        }
        int measuredHeight = measureHeight();
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private int measureHeight() {
        return radius * 2;
    }

    private int measureWidth() {
        return radius * 2;
    }

    public void setColor(int color) {
        this.fillColor = color;
        this.fillColorOrig = color;
    }

    public void setCirclePercentage(int value) {
        hasPercentage = true;
        percentage = value;
        if (percentage == 0) {
            this.sweepAngle = 1;
            return;
        }
        if (percentage == 100) {
            this.sweepAngle = 360;
            return;
        }

        this.sweepAngle = 360 * percentage / 100;
    }

    public int getColor() {
        return fillColor;
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            fillColor = selectionColor;
            showText = false;
            sweepAngle = 360;
        } else {
            fillColor = fillColorOrig;
            showText = showTextOrig;
            if (hasPercentage) {
                setCirclePercentage(percentage);
            }
        }
        super.setSelected(selected);
    }
}