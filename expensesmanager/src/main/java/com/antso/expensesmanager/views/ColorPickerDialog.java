package com.antso.expensesmanager.views;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.antso.expensesmanager.R;
import com.antso.expensesmanager.utils.MaterialColours;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ColorPickerDialog extends Dialog implements View.OnClickListener {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener mListener;

    public ColorPickerDialog(Context context, OnColorChangedListener listener) {
        super(context);
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.color_picker);

        List<CircleSectorView> colors = new ArrayList<CircleSectorView>();
        colors.add((CircleSectorView) findViewById(R.id.color1));
        colors.add((CircleSectorView) findViewById(R.id.color2));
        colors.add((CircleSectorView) findViewById(R.id.color3));
        colors.add((CircleSectorView) findViewById(R.id.color4));
        colors.add((CircleSectorView) findViewById(R.id.color5));
        colors.add((CircleSectorView) findViewById(R.id.color6));
        colors.add((CircleSectorView) findViewById(R.id.color7));
        colors.add((CircleSectorView) findViewById(R.id.color8));
        colors.add((CircleSectorView) findViewById(R.id.color9));
        colors.add((CircleSectorView) findViewById(R.id.color10));
        colors.add((CircleSectorView) findViewById(R.id.color11));
        colors.add((CircleSectorView) findViewById(R.id.color12));

        colors.get(0).setColor(MaterialColours.PINK_500);
        colors.get(1).setColor(MaterialColours.PURPLE_500);
        colors.get(2).setColor(MaterialColours.INDIGO_500);
        colors.get(3).setColor(MaterialColours.BLUE_500);
        colors.get(4).setColor(MaterialColours.LIGHT_BLUE_500);
        colors.get(5).setColor(MaterialColours.CYAN_500);
        colors.get(6).setColor(MaterialColours.TEAL_500);
        colors.get(7).setColor(MaterialColours.LIGHT_GREEN_500);
        colors.get(8).setColor(MaterialColours.LIME_500);
        colors.get(9).setColor(MaterialColours.AMBER_500);
        colors.get(10).setColor(MaterialColours.ORANGE_500);
        colors.get(11).setColor(MaterialColours.BROWN_500);

        for (CircleSectorView v : colors) {
            v.setOnClickListener(this);
        }

        //static final int LIME_500 = Color.parseColor("#cddc39");
        //static final int AMBER_500 = Color.parseColor("#ffc107");
        //static final int ORANGE_500 = Color.parseColor("#ff9800");
        //static final int BROWN_500 = Color.parseColor("#795548");



//        setContentView(new ColorPickerView2(getContext(), listener));

        setTitle("Pick a Color");
    }

    @Override
    public void onClick(View v) {
        CircleSectorView selected = (CircleSectorView)v;
        mListener.colorChanged(selected.getColor());
        dismiss();
    }

}