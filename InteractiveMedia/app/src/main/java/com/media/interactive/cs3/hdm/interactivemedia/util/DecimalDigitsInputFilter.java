package com.media.interactive.cs3.hdm.interactivemedia.util;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by benny on 22.01.18.
 */

public class DecimalDigitsInputFilter implements InputFilter {

    private int maxDigitsBeforeDecimalPoint;
    private int maxDigitsAfterDecimalPoint;

    public DecimalDigitsInputFilter(int maxDigitsBeforeDecimalPoint, int maxDigitsAfterDecimalPoint) {
        this.maxDigitsAfterDecimalPoint = maxDigitsAfterDecimalPoint;
        this.maxDigitsBeforeDecimalPoint = maxDigitsBeforeDecimalPoint;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        StringBuilder builder = new StringBuilder(dest);
        builder.replace(dstart, dend, source
            .subSequence(start, end).toString());
        if (!builder.toString().matches(
            "(([1-9]{1})([0-9]{0,"+(maxDigitsBeforeDecimalPoint-1)+"})?)?(\\.[0-9]{0,"+maxDigitsAfterDecimalPoint+"})?"

        )) {
            if(source.length()==0)
                return dest.subSequence(dstart, dend);
            return "";
        }

        return null;

    }
}