package com.media.interactive.cs3.hdm.interactivemedia.fragments;

import android.view.View;



/**
 * Created by Pirmin Rehm on 30.11.2017.
 */

public interface IFragment {

    /**
     * Gets the on fab click listener.
     *
     * @return the on fab click listener
     */
    public View.OnClickListener getOnFabClickListener();
}
