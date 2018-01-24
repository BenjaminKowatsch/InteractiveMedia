package com.media.interactive.cs3.hdm.interactivemedia.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;



/**
 * Created by benny on 04.01.18.
 */

public class NonScrollListView extends ListView {

    /**
     * Instantiates a new non scroll list view.
     *
     * @param context the context
     */
    public NonScrollListView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new non scroll list view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public NonScrollListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new non scroll list view.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public NonScrollListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* (non-Javadoc)
     * @see android.widget.ListView#onMeasure(int, int)
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpeCustom = View.MeasureSpec.makeMeasureSpec(
            Integer.MAX_VALUE >> 2, View.MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpeCustom);
        final ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}
