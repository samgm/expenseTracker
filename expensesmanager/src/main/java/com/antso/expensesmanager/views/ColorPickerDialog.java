package com.antso.expensesmanager.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.antso.expensesmanager.R;

import java.util.ArrayList;
import java.util.List;

public class ColorPickerDialog extends Dialog implements View.OnClickListener {
    private ArrayList<Integer> colors;

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener mListener;

    public ColorPickerDialog(Context context, OnColorChangedListener listener,
                             ArrayList<Integer> colors) {
        super(context);
        this.mListener = listener;
        this.colors = colors;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.color_picker);

        List<CircleSectorView> colorsView = new ArrayList<CircleSectorView>();
        colorsView.add((CircleSectorView) findViewById(R.id.color1));
        colorsView.add((CircleSectorView) findViewById(R.id.color2));
        colorsView.add((CircleSectorView) findViewById(R.id.color3));
        colorsView.add((CircleSectorView) findViewById(R.id.color4));
        colorsView.add((CircleSectorView) findViewById(R.id.color5));
        colorsView.add((CircleSectorView) findViewById(R.id.color6));
        colorsView.add((CircleSectorView) findViewById(R.id.color7));
        colorsView.add((CircleSectorView) findViewById(R.id.color8));
        colorsView.add((CircleSectorView) findViewById(R.id.color9));
        colorsView.add((CircleSectorView) findViewById(R.id.color10));
        colorsView.add((CircleSectorView) findViewById(R.id.color11));
        colorsView.add((CircleSectorView) findViewById(R.id.color12));

        int i = 0;
        for (Integer color : colors) {
            colorsView.get(i).setColor(color);
            i++;
        }
        for (CircleSectorView v : colorsView) {
            v.setOnClickListener(this);
        }

        setTitle(R.string.title_color_picker_dialog);
    }

    @Override
    public void onClick(View v) {
        CircleSectorView selected = (CircleSectorView)v;
        mListener.colorChanged(selected.getColor());
        dismiss();
    }

}