package com.media.interactive.cs3.hdm.interactivemedia.data;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.text.NumberFormat;

//Adjusted from https://stackoverflow.com/a/24621325/5391826

public class MoneyTextWatcher implements TextWatcher {
    private final WeakReference<EditText> editTextWeakReference;
    private final NumberFormat currencyFormat;

    public MoneyTextWatcher(EditText editText, NumberFormat currencyFormat) {
        editTextWeakReference = new WeakReference<>(editText);
        this.currencyFormat = currencyFormat;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        EditText editText = editTextWeakReference.get();
        if (editText == null) return;
        String s = editable.toString();
        editText.removeTextChangedListener(this);
        String cleanString = s.replaceAll("[^0-9]", "");
        BigDecimal parsed = new BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
        String formatted = currencyFormat.format(parsed);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
        editText.addTextChangedListener(this);
    }
}