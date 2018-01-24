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
import com.media.interactive.cs3.hdm.interactivemedia.database.DatabaseHelper;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.PaymentTable;

import java.text.NumberFormat;
import java.util.Locale;




/**
 * The Class PaymentAdapter.
 */
public class PaymentAdapter extends CursorAdapter {

    /**
     * The Constant TAG.
     */
    private static final String TAG = PaymentAdapter.class.getSimpleName();

    /**
     * The Constant FROM_USER.
     */
    private static final String FROM_USER = DatabaseHelper.PAYMENT_USER_JOIN_COLUMN_FROM_USER;

    /**
     * The Constant TO_USER.
     */
    private static final String TO_USER = DatabaseHelper.PAYMENT_USER_JOIN_COLUMN_TO_USER;

    /**
     * The formatter.
     */
    private final NumberFormat formatter;

    /**
     * The layout inflater.
     */
    private final LayoutInflater layoutInflater;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Instantiates a new payment adapter.
     *
     * @param context   the context
     * @param c         the c
     * @param flags     the flags
     * @param formatter the formatter
     */
    public PaymentAdapter(Context context, Cursor c, int flags, NumberFormat formatter) {
        super(context, c, flags);
        this.formatter = formatter;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    /**
     * Instantiates a new payment adapter.
     *
     * @param context the context
     * @param c       the c
     */
    public PaymentAdapter(Context context, Cursor c) {
        super(context, c);
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
    }

    /* (non-Javadoc)
     * @see android.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.payment, parent, false);
    }


    /* (non-Javadoc)
     * @see android.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final double amount = cursor.getDouble(cursor.getColumnIndex(PaymentTable.COLUMN_AMOUNT));
        final String fromUserName = cursor.getString(cursor.getColumnIndex(FROM_USER));
        final String toUserName = cursor.getString(cursor.getColumnIndex(TO_USER));

        final TextView amountView = view.findViewById(R.id.payment_amount);
        if (amountView != null) {
            amountView.setText(formatter.format(amount));
        } else {
            Log.e(TAG, "Could not find amountView");
        }

        final TextView fromUserNameView = view.findViewById(R.id.payment_from_user_name);
        if (fromUserNameView != null) {
            fromUserNameView.setText(fromUserName);
        } else {
            Log.e(TAG, "Could not find fromUserNameView");
        }

        final TextView toUserNameView = view.findViewById(R.id.payment_to_user_name);
        if (toUserNameView != null) {
            toUserNameView.setText(toUserName);
        } else {
            Log.e(TAG, "Could not find toUserNameView");
        }
    }
}
