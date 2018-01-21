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
import com.media.interactive.cs3.hdm.interactivemedia.NonScrollListView;
import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.UserAdapter;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.DatabaseProvider;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

/**
 * Created by benny on 04.01.18.
 */

public class GroupDetailViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private final static String TAG = GroupDetailViewActivity.class.getSimpleName();

    private ImageView groupImage;
    private TextView groupName;
    private TextView groupCreatedAt;
    private NonScrollListView listView;

    private UserAdapter userAdapter;

    private long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_group);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        groupImage = (ImageView) findViewById(R.id.detail_group_image);
        groupName = (TextView) findViewById(R.id.detail_group_name);
        groupCreatedAt = (TextView) findViewById(R.id.detail_group_created_at);
        listView = (NonScrollListView) findViewById(R.id.detail_list_view);

        final Bundle extras = getIntent().getExtras();
        groupId = extras.getLong("id");
        final String imageUrl = extras.getString("imageUrl");
        final String name = extras.getString("name");
        final String createdAt = extras.getString("createdAt");
        final boolean sync = extras.getBoolean("sync");

        final TextView groupSynched = (TextView) findViewById(R.id.detail_group_synchronized);
        groupSynched.setText(sync ? "Synchronized":"Not synchronized");

        groupName.setText(name);
        groupCreatedAt.setText(Helper.READABLE_DATE_FORMAT.format(Helper.parseDateString(createdAt)));

        if(imageUrl != null) {
            LazyHeaders.Builder builder = new LazyHeaders.Builder();

            if (imageUrl.startsWith(getResources().getString(R.string.web_service_url))) {
                builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            }
            final GlideUrl glideUrl = new GlideUrl(imageUrl, builder.build());

            Glide.with(this).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(ContextCompat.getDrawable(this, R.drawable.anonymousgroup))
                .into(groupImage);
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        final String[] projection = { UserTable.TABLE_NAME + ".*" };
        final String selection = GroupTable.TABLE_NAME + "." + GroupTable.COLUMN_ID + " = ? ";
        final String[] selectionArgs = {String.valueOf(groupId)};
        return new CursorLoader(GroupDetailViewActivity.this, DatabaseProvider.CONTENT_GROUP_USER_JOIN_URI, projection, selection, selectionArgs,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursor.moveToFirst();
        if(userAdapter == null) {
            userAdapter = new UserAdapter(this, R.layout.detail_group_user, cursor);
        } else {
            userAdapter.swapCursor(cursor);
        }
        listView.setAdapter(userAdapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        userAdapter.swapCursor(null);
    }
}
