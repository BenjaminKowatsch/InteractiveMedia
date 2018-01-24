package com.media.interactive.cs3.hdm.interactivemedia.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;



/**
 * Created by benny on 31.12.17.
 */

public class NonScrollRecyclerView extends RecyclerView {

    /**
     * Instantiates a new non scroll recycler view.
     *
     * @param context the context
     */
    public NonScrollRecyclerView(Context context) {
        super(context);
    }

    /**
     * Instantiates a new non scroll recycler view.
     *
     * @param context the context
     * @param attrs   the attrs
     */
    public NonScrollRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Instantiates a new non scroll recycler view.
     *
     * @param context  the context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public NonScrollRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * On measure.
     *
     * @param widthMeasureSpec  the width measure spec
     * @param heightMeasureSpec the height measure spec
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
