package com.media.interactive.cs3.hdm.interactivemedia.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;



/**
 * Created by benny on 21.01.18.
 */

public class ArrayAdapterWithIcon extends ArrayAdapter<String> {

    /**
     * The images.
     */
    private List<Integer> images;

    /**
     * Instantiates a new array adapter with icon.
     *
     * @param context the context
     * @param items   the items
     * @param images  the images
     */
    public ArrayAdapterWithIcon(Context context, String[] items, Integer[] images) {
        super(context, android.R.layout.select_dialog_item, items);
        this.images = Arrays.asList(images);
    }

    /**
     * Gets the view.
     *
     * @param position    the position
     * @param convertView the convert view
     * @param parent      the parent
     * @return the view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);
        final TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setTextColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(images.get(position), 0, 0, 0);
            textView.getCompoundDrawablesRelative()[0].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(images.get(position), 0, 0, 0);
            textView.getCompoundDrawables()[0].setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        textView.setCompoundDrawablePadding(
            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics()));
        return view;
    }

}
