package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

/**
 * Created by benny on 04.01.18.
 */

public class TransactionAdapter extends CursorAdapter {

    private LayoutInflater mLayoutInflater;
    private DatabaseProviderHelper helper;
    private int layout;

    public TransactionAdapter(Context context, int layout, Cursor c) {
        super(context, c, 0);
        this.layout = layout;
        mLayoutInflater = LayoutInflater.from(context);
        helper = new DatabaseProviderHelper(context.getContentResolver());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View view = mLayoutInflater.inflate(layout, viewGroup, false);
        view.setTag(new ViewHolder(view));
        // no need to bind data here. you do in later
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Extract properties from cursor
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final Transaction transaction = viewHolder.getTransaction();

        transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_ID)));
        transaction.setInfoName(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_NAME)));
        transaction.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_IMAGE_URL)));
        transaction.setDateTime(Helper.ParseDateString(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_CREATED_AT))));
        transaction.setPaidByUserId(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_PAID_BY)));
        transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_AMOUNT)));

        Log.d(Transaction.class.getSimpleName(), transaction.toString());
        // Populate fields with extracted properties
        viewHolder.transactionTitle.setText(transaction.getInfoName());
        viewHolder.transactionCreationDate.setText(Helper.formatDate(transaction.getDateTime()));
        viewHolder.transactionPaidBy.setText("Paid by: "+cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_USERNAME)));
        viewHolder.transactionAmount.setText(String.valueOf(transaction.getAmount()));

        final String imageUrl = transaction.getImageUrl();
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        GlideUrl glideUrl = null;
        if (imageUrl != null && imageUrl.startsWith(context.getResources().getString(R.string.web_service_url))) {
            builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            glideUrl = new GlideUrl(imageUrl, builder.build());
        }

        Glide.with(context).load(glideUrl)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .fallback(R.drawable.anonymoususer)
            .placeholder(R.drawable.anonymoususer)
            .into(viewHolder.transactionIcon);

    }

    public class ViewHolder {
        ImageView transactionIcon;
        TextView transactionTitle;
        TextView transactionCreationDate;
        TextView transactionPaidBy;
        TextView transactionAmount;
        private Transaction transaction;

        ViewHolder(View view) {
            transactionIcon = (ImageView) view.findViewById(R.id.transaction_icon);
            transactionTitle = (TextView) view.findViewById(R.id.transaction_title);
            transactionCreationDate = (TextView) view.findViewById(R.id.transaction_creation_date);
            transactionPaidBy = (TextView) view.findViewById(R.id.transaction_payed_by);
            transactionAmount = (TextView) view.findViewById(R.id.transaction_amount);

            transaction = new Transaction();
        }

        public Transaction getTransaction() {
            return transaction;
        }
    }
}