package com.media.interactive.cs3.hdm.interactivemedia.data.settlement;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.PaymentTable;

import java.text.NumberFormat;
import java.util.Locale;


public class PaymentAdapter extends CursorAdapter {
    private static final String TAG = PaymentAdapter.class.getSimpleName();
    private static final String FROM_USER = DatabaseHelper.PAYMENT_USER_JOIN_COLUMN_FROM_USER;
    private static final String TO_USER = DatabaseHelper.PAYMENT_USER_JOIN_COLUMN_TO_USER;
    private final NumberFormat formatter;
    private final LayoutInflater mLayoutInflater;
    private final Context mContext;

    public PaymentAdapter(Context context, Cursor c, int flags, NumberFormat formatter) {
        super(context, c, flags);
        this.formatter = formatter;
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public PaymentAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mLayoutInflater.inflate(R.layout.payment, parent, false);
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        double amount = cursor.getDouble(cursor.getColumnIndex(PaymentTable.COLUMN_AMOUNT));
        String fromUserName = cursor.getString(cursor.getColumnIndex(FROM_USER));
        String toUserName = cursor.getString(cursor.getColumnIndex(TO_USER));

        TextView amountView = view.findViewById(R.id.payment_amount);
        if (amountView != null) {
            amountView.setText(formatter.format(amount));
        } else {
            Log.e(TAG, "Could not find amountView");
        }

        TextView fromUserNameView = view.findViewById(R.id.payment_from_user_name);
        if (fromUserNameView != null) {
            fromUserNameView.setText(fromUserName);
        } else {
            Log.e(TAG, "Could not find fromUserNameView");
        }

        TextView toUserNameView = view.findViewById(R.id.payment_to_user_name);
        if (toUserNameView != null) {
            toUserNameView.setText(toUserName);
        } else {
            Log.e(TAG, "Could not find toUserNameView");
        }
    }
}
