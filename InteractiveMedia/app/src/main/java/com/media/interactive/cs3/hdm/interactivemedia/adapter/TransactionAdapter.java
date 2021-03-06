package com.media.interactive.cs3.hdm.interactivemedia.adapter;

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
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.DatabaseProviderHelper;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.Transaction;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.TransactionTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import java.io.File;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.CURRENCY_FORMAT;



/**
 * Created by benny on 04.01.18.
 */

public class TransactionAdapter extends CursorAdapter {

    /**
     * The layout inflater.
     */
    private LayoutInflater layoutInflater;

    /**
     * The helper.
     */
    private DatabaseProviderHelper helper;

    /**
     * The layout.
     */
    private int layout;

    /**
     * Instantiates a new transaction adapter.
     *
     * @param context the context
     * @param layout  the layout
     * @param c       the c
     */
    public TransactionAdapter(Context context, int layout, Cursor c) {
        super(context, c, 0);
        this.layout = layout;
        layoutInflater = LayoutInflater.from(context);
        helper = new DatabaseProviderHelper(context.getContentResolver());
    }

    /**
     * New view.
     *
     * @param context   the context
     * @param cursor    the cursor
     * @param viewGroup the view group
     * @return the view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View view = layoutInflater.inflate(layout, viewGroup, false);
        view.setTag(new ViewHolder(view));
        // no need to bind data here. you do in later
        return view;
    }

    /**
     * Bind the view with the transaction data from the cursor using the ViewHolder pattern.
     *
     * @param view    the view
     * @param context the context
     * @param cursor  the cursor
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Extract properties from cursor
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final Transaction transaction = viewHolder.getTransaction();

        transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_ID)));
        transaction.setInfoName(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_NAME)));
        transaction.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_IMAGE_URL)));
        transaction.setDateTime(Helper.parseDateString(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_INFO_CREATED_AT))));
        transaction.setPaidBy(cursor.getString(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_PAID_BY)));
        transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_AMOUNT)));
        final int synced = cursor.getInt(cursor.getColumnIndexOrThrow(TransactionTable.COLUMN_SYNCHRONIZED));
        transaction.setSynched(synced > 0);

        if (transaction.isSynched()) {
            viewHolder.transactionSynced.setVisibility(View.GONE);
        } else {
            viewHolder.transactionSynced.setVisibility(View.VISIBLE);
        }

        Log.d(Transaction.class.getSimpleName(), transaction.toString());
        // Populate fields with extracted properties
        viewHolder.transactionTitle.setText(transaction.getInfoName());
        viewHolder.transactionCreationDate.setText(Helper.READABLE_DATE_FORMAT.format(transaction.getDateTime()));
        viewHolder.transactionPaidBy.setText("Paid by: " + cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_USERNAME)));
        viewHolder.transactionAmount.setText(CURRENCY_FORMAT.format(transaction.getAmount()));

        final String imageUrl = transaction.getImageUrl();
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        GlideUrl glideUrl = null;
        if (imageUrl != null && imageUrl.startsWith(context.getResources().getString(R.string.web_service_url))) {
            builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            glideUrl = new GlideUrl(imageUrl, builder.build());
        }

        if (transaction.isSynched() && transaction.getImageUrl() != null) {
            Glide.with(context).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fallback(R.drawable.anonymoustransaction)
                .placeholder(R.drawable.anonymoustransaction)
                .into(viewHolder.transactionIcon);
        } else if (transaction.getImageUrl() != null) {
            Glide.with(context).load(new File(transaction.getImageUrl()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fallback(R.drawable.anonymoustransaction)
                .placeholder(R.drawable.anonymoustransaction)
                .into(viewHolder.transactionIcon);
        } else {
            Glide.with(context)
                .load(R.drawable.anonymoustransaction)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(viewHolder.transactionIcon);
        }
    }

    /**
     * The Class ViewHolder.
     */
    public class ViewHolder {

        /**
         * The transaction icon.
         */
        ImageView transactionIcon;

        /**
         * The transaction title.
         */
        TextView transactionTitle;

        /**
         * The transaction creation date.
         */
        TextView transactionCreationDate;

        /**
         * The transaction paid by.
         */
        TextView transactionPaidBy;

        /**
         * The transaction amount.
         */
        TextView transactionAmount;

        /**
         * The transaction synced.
         */
        ImageView transactionSynced;

        /**
         * The transaction.
         */
        private Transaction transaction;

        /**
         * Instantiates a new view holder.
         *
         * @param view the view
         */
        public ViewHolder(View view) {
            transactionIcon = (ImageView) view.findViewById(R.id.transaction_icon);
            transactionTitle = (TextView) view.findViewById(R.id.transaction_title);
            transactionCreationDate = (TextView) view.findViewById(R.id.transaction_creation_date);
            transactionPaidBy = (TextView) view.findViewById(R.id.transaction_payed_by);
            transactionAmount = (TextView) view.findViewById(R.id.transaction_amount);
            transactionSynced = (ImageView) view.findViewById(R.id.transaction_synced_icon);

            transaction = new Transaction();
        }

        /**
         * Gets the paid by username.
         *
         * @return the paid by username
         */
        public String getPaidByUsername() {
            return transactionPaidBy.getText().toString();
        }

        /**
         * Gets the transaction.
         *
         * @return the transaction
         */
        public Transaction getTransaction() {
            return transaction;
        }
    }
}