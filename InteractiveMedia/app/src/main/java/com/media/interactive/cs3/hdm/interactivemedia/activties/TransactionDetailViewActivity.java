package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.database.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import java.util.ArrayList;

import static com.media.interactive.cs3.hdm.interactivemedia.activties.AddTransactionActivity.CURRENCY_FORMAT;



/**
 * Created by benny on 04.01.18.
 */

public class TransactionDetailViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The Constant TAG.
     */
    private static final String TAG = TransactionDetailViewActivity.class.getSimpleName();

    /**
     * The transaction image.
     */
    private ImageView transactionImage;

    /**
     * The transaction name.
     */
    private TextView transactionName;

    /**
     * The transaction created at.
     */
    private TextView transactionCreatedAt;

    /**
     * The transaction amount.
     */
    private TextView transactionAmount;

    /**
     * The pie chart.
     */
    private PieChart pieChart;

    /**
     * The amount value.
     */
    private double amountValue;

    /**
     * The transaction id.
     */
    private long transactionId;

    /**
     * The paid by username.
     */
    private String paidByUsername;

    /**
     * On create.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_transaction);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        transactionImage = (ImageView) findViewById(R.id.detail_transaction_image);
        transactionName = (TextView) findViewById(R.id.detail_transaction_name);
        transactionCreatedAt = (TextView) findViewById(R.id.detail_transaction_created_at);
        transactionAmount = (TextView) findViewById(R.id.detail_transaction_amount);

        final Bundle extras = getIntent().getExtras();
        transactionId = extras.getLong("id");
        final String imageUrl = extras.getString("imageUrl");
        final String name = extras.getString("name");
        final String createdAt = extras.getString("createdAt");
        amountValue = extras.getDouble("amount");
        paidByUsername = extras.getString("paidByUsername");
        final boolean sync = extras.getBoolean("sync");

        final TextView groupSynched = (TextView) findViewById(R.id.detail_transaction_synchronized);
        groupSynched.setText(sync ? "Synchronized" : "Not synchronized");

        transactionName.setText(name);
        transactionCreatedAt.setText(Helper.READABLE_DATE_FORMAT.format(Helper.parseDateString(createdAt)));
        transactionAmount.setText(CURRENCY_FORMAT.format(amountValue));

        pieChart = (PieChart) findViewById(R.id.chart);

        if (imageUrl != null) {
            LazyHeaders.Builder builder = null;

            if (imageUrl.startsWith(getResources().getString(R.string.web_service_url))) {
                builder = new LazyHeaders.Builder().addHeader("Authorization", Login.getInstance().getUserType().getValue()
                    + " " + Login.getInstance().getAccessToken());
            } else {
                builder = new LazyHeaders.Builder();
            }
            final GlideUrl glideUrl = new GlideUrl(imageUrl, builder.build());

            Glide.with(this).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.anonymoustransaction))
                .into(transactionImage);
        }
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * On create loader.
     *
     * @param i      the i
     * @param bundle the bundle
     * @return the loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        final String[] projection = {"b." + DebtTable.COLUMN_AMOUNT, "a." + UserTable.COLUMN_USERNAME, "b." + UserTable.COLUMN_USERNAME};
        final String selection = " b.from_user = a._id AND b." + DebtTable.COLUMN_TRANSACTION_ID + " = ? ";
        final String[] selectionArgs = {String.valueOf(transactionId)};

        return new CursorLoader(TransactionDetailViewActivity.this, DatabaseProvider.USER_DEBT_JOIN_URI, projection, selection, selectionArgs, null);
    }

    /**
     * On load finished.
     *
     * @param loader the loader
     * @param cursor the cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "Debts for transaction: " + transactionId + " " + cursor.getCount());
        setPieChartData(pieChart, cursor);

    }

    /**
     * On loader reset.
     *
     * @param loader the loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Sets the pie chart data.
     *
     * @param pieChart the pie chart
     * @param cursor   the cursor
     */
    private void setPieChartData(PieChart pieChart, Cursor cursor) {
        final PieDataSet dataSet = new PieDataSet(getEntries(cursor), "");
        //Set the data
        final PieData data = new PieData(dataSet); // initialize PieData

        pieChart.setData(data); //set data into chart
        pieChart.invalidate();

        pieChart.getDescription().setEnabled(false);

        final Legend legend = pieChart.getLegend();
        legend.setTextColor(getResources().getColor(android.R.color.white));
        legend.setTextSize(14);

        pieChart.setTransparentCircleColor(R.color.colorPrimary);
        pieChart.setHoleColor(R.color.colorPrimary);

        dataSet.setColors(ColorTemplate.MATERIAL_COLORS); // set the color
        dataSet.setValueTextSize(16);

    }

    /**
     * Gets the entries.
     *
     * @param cursor the cursor
     * @return the entries
     */
    private ArrayList<PieEntry> getEntries(Cursor cursor) {
        // creating data values

        final ArrayList<PieEntry> entries = new ArrayList<>();
        double sum = 0;
        while (cursor.moveToNext()) {
            final double amount = cursor.getDouble(0);
            final String fromUsername = cursor.getString(1);
            final String toUsername = cursor.getString(2);
            sum += amount;
            final PieEntry pieEntry = new PieEntry((float) amount, fromUsername + " to " + toUsername);
            entries.add(pieEntry);
        }
        final PieEntry pieEntry = new PieEntry((float) (amountValue - sum), paidByUsername);
        entries.add(pieEntry);
        return entries;
    }

}
