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
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.DebtTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import java.util.ArrayList;

/**
 * Created by benny on 04.01.18.
 */

public class TransactionDetailViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final static String TAG = TransactionDetailViewActivity.class.getSimpleName();

    private ImageView transactionImage;
    private TextView transactionName;
    private TextView transactionCreatedAt;
    private TextView transactionAmount;

   // private UserAdapter userAdapter;
    private PieChart pieChart;

    private double amountValue;
    private long transactionId;
    private String paidByUsername;

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
        groupSynched.setText(sync ? "Synchronized":"Not synchronized");

        transactionName.setText(name);
        transactionCreatedAt.setText(Helper.READABLE_DATE_FORMAT.format(Helper.parseDateString(createdAt)));
        transactionAmount.setText(String.valueOf(amountValue));

        pieChart = (PieChart) findViewById(R.id.chart);

        if(imageUrl != null) {
            LazyHeaders.Builder builder = new LazyHeaders.Builder();

            if (imageUrl.startsWith(getResources().getString(R.string.web_service_url))) {
                builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            }
            final GlideUrl glideUrl = new GlideUrl(imageUrl, builder.build());

            Glide.with(this).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.anonymoustransaction))
                .into(transactionImage);
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private void setPieChartData(PieChart pieChart, Cursor cursor) {
        final PieDataSet dataset = new PieDataSet(getEntries(cursor), "");
        //Set the data
        PieData data = new PieData(dataset); // initialize PieData

        pieChart.setData(data); //set data into chart
        pieChart.invalidate();

        pieChart.getDescription().setEnabled(false);

        final Legend legend= pieChart.getLegend();
        legend.setTextColor(getResources().getColor(android.R.color.white));
        legend.setTextSize(14);

        pieChart.setTransparentCircleColor(R.color.colorPrimary);
        pieChart.setHoleColor(R.color.colorPrimary);

        dataset.setColors(ColorTemplate.MATERIAL_COLORS); // set the color
        dataset.setValueTextSize(16);

    }

    private ArrayList<PieEntry> getEntries(Cursor cursor) {
        // creating data values

        ArrayList<PieEntry> entries = new ArrayList<>();
        double sum = 0;
        while(cursor.moveToNext()){
            double amount = cursor.getDouble(cursor.getColumnIndex(DebtTable.COLUMN_AMOUNT));
            String username = cursor.getString(cursor.getColumnIndex(UserTable.COLUMN_USERNAME));
            sum += amount;
            final PieEntry pieEntry = new PieEntry((float)amount, "To: "+username);
            entries.add(pieEntry);
        }
        final PieEntry pieEntry = new PieEntry((float)(amountValue-sum), paidByUsername);
        entries.add(pieEntry);
        return entries;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        final String[] projection = { DebtTable.TABLE_NAME + ".*", UserTable.TABLE_NAME+".*" };
        final String selection = DebtTable.TABLE_NAME + "." + DebtTable.COLUMN_TRANSACTION_ID + " = ? ";
        final String[] selectionArgs = {String.valueOf(transactionId)};
        return new CursorLoader(TransactionDetailViewActivity.this, DatabaseProvider.USER_DEBT_JOIN_URI, projection, selection, selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG,"Debts for transaction: "+transactionId+" "+cursor.getCount());
        setPieChartData(pieChart, cursor);

       /* if(userAdapter == null) {
            userAdapter = new UserAdapter(this, R.layout.detail_group_user, cursor);
        } else {
            userAdapter.swapCursor(cursor);
        }*/
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //userAdapter.swapCursor(null);
    }
}
