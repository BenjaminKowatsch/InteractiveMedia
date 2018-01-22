package com.media.interactive.cs3.hdm.interactivemedia.util;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

//Adjusted from https://stackoverflow.com/a/24621325/5391826

public class MoneyTextWatcher implements TextWatcher {

    private final DecimalFormat df;
    private final DecimalFormat dfnd;
    private final EditText et;
    private boolean hasFractionalPart;
    private int trailingZeroCount;

    public MoneyTextWatcher(EditText editText, String pattern) {
        df = new DecimalFormat(pattern);
        df.setDecimalSeparatorAlwaysShown(true);
        dfnd = new DecimalFormat("#,###.00");
        this.et = editText;
        hasFractionalPart = false;
    }

    @Override
    public void afterTextChanged(Editable s) {
        et.removeTextChangedListener(this);

        if (s != null && !s.toString().isEmpty()) {
            try {
                int inilen, endlen;
                inilen = et.getText().length();
                String v = s.toString().replace(String.valueOf(df.getDecimalFormatSymbols().getGroupingSeparator()), "").replace("$","");
                Number n = df.parse(v);
                int cp = et.getSelectionStart();
                if (hasFractionalPart) {
                    StringBuilder trailingZeros = new StringBuilder();
                    while (trailingZeroCount-- > 0)
                        trailingZeros.append('0');
                    et.setText(df.format(n) + trailingZeros.toString());
                } else {
                    et.setText(dfnd.format(n));
                }
                et.setText("€".concat(et.getText().toString()));
                endlen = et.getText().length();
                int sel = (cp + (endlen - inilen));
                if (sel > 0 && sel < et.getText().length()) {
                    et.setSelection(sel);
                } else if (trailingZeroCount > -1) {
                    et.setSelection(et.getText().length() - 3);
                } else {
                    et.setSelection(et.getText().length());
                }
            } catch (NumberFormatException | ParseException e) {
                e.printStackTrace();
            }
        }

        et.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        int index = s.toString().indexOf(String.valueOf(df.getDecimalFormatSymbols().getDecimalSeparator()));
        trailingZeroCount = 0;
        if (index > -1) {
            for (index++; index < s.length(); index++) {
                if (s.charAt(index) == '0')
                    trailingZeroCount++;
                else {
                    trailingZeroCount = 0;
                }
            }
            hasFractionalPart = true;
        } else {
            hasFractionalPart = false;
        }
    }
}