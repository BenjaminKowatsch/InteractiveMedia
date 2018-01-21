package com.media.interactive.cs3.hdm.interactivemedia.activties;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.media.interactive.cs3.hdm.interactivemedia.NonScrollListView;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.UserAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by benny on 04.01.18.
 */

public class TransactionDetailViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final static String TAG = TransactionDetailViewActivity.class.getSimpleName();

    private ImageView transactionImage;
    private TextView transactionName;
    private TextView transactionCreatedAt;
    private TextView transactionAmount;

    private UserAdapter userAdapter;
    private PieChart pieChart;

    private long transactionId;

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
        final double amount = extras.getDouble("amount");

        transactionName.setText(name);
        transactionCreatedAt.setText(createdAt);
        transactionAmount.setText(String.valueOf(amount));

        pieChart = (PieChart) findViewById(R.id.chart);

        setPieChartData(pieChart);

        if(imageUrl != null) {
            LazyHeaders.Builder builder = new LazyHeaders.Builder();

            if (imageUrl.startsWith(getResources().getString(R.string.web_service_url))) {
                builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            }
            final GlideUrl glideUrl = new GlideUrl(imageUrl, builder.build());

            Glide.with(this).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.anonymoususer))
                .into(transactionImage);
        }
        // TODO: link to transactions

        getLoaderManager().initLoader(0, null, this);
    }

    private void setPieChartData(PieChart pieChart) {
        final PieDataSet dataset = new PieDataSet(getEntries(), "Time spent");
        //Set the data
        PieData data = new PieData(dataset); // initialize PieData

        pieChart.setData(data); //set data into chart
        pieChart.invalidate();

        dataset.setColors(ColorTemplate.COLORFUL_COLORS); // set the color
        dataset.setValueTextSize(16);
    }

    private ArrayList<PieEntry> getEntries() {
        // creating data values

        ArrayList<PieEntry> entries = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            entries.add(new PieEntry(i, i));
        }

        return entries;
    }

    private ArrayList<String> getLabels() {
        // creating labels
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            labels.add("Username: "+ i);
        }
        return labels;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        final String[] projection = { UserTable.TABLE_NAME + ".*" };
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_ID + " = ? ";
        final String[] selectionArgs = {String.valueOf(transactionId)};
        return new CursorLoader(TransactionDetailViewActivity.this, DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        if(userAdapter == null) {
            userAdapter = new UserAdapter(this, R.layout.detail_group_user, cursor);
        } else {
            userAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        userAdapter.swapCursor(null);
    }
}
