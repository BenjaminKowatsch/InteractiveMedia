package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.text.InputFilter;
import android.text.Spanned;



/**
 * Created by benny on 22.01.18.
 */

public class DecimalDigitsInputFilter implements InputFilter {

    /**
     * The max digits before decimal point.
     */
    private int maxDigitsBeforeDecimalPoint;

    /**
     * The max digits after decimal point.
     */
    private int maxDigitsAfterDecimalPoint;

    /**
     * Instantiates a new decimal digits input filter.
     *
     * @param maxDigitsBeforeDecimalPoint the max digits before decimal point
     * @param maxDigitsAfterDecimalPoint  the max digits after decimal point
     */
    public DecimalDigitsInputFilter(int maxDigitsBeforeDecimalPoint, int maxDigitsAfterDecimalPoint) {
        this.maxDigitsAfterDecimalPoint = maxDigitsAfterDecimalPoint;
        this.maxDigitsBeforeDecimalPoint = maxDigitsBeforeDecimalPoint;
    }

    /* (non-Javadoc)
     * @see android.text.InputFilter#filter(java.lang.CharSequence, int, int, android.text.Spanned, int, int)
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        final StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source
            .subSequence(start, end).toString());
        if (!builder.toString().matches(
            "(([1-9]{1})([0-9]{0," + (maxDigitsBeforeDecimalPoint - 1) + "})?)?(\\.[0-9]{0," + maxDigitsAfterDecimalPoint + "})?"

        )) {
            if (source.length() == 0) {
                return dest.subSequence(dstart, dend);
            }
            return "";
        }

        return null;

    }
}