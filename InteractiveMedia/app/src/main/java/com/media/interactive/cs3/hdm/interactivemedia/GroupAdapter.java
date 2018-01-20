package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
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
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.util.Helper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by benny on 04.01.18.
 */

public class GroupAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;
    private int layout;

    public GroupAdapter(Context context, int layout, Cursor c) {
        super(context, c, 0);
        this.layout = layout;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View view = layoutInflater.inflate(layout, viewGroup, false);
        view.setTag(new ViewHolder(view));
        // no need to bind data here. you do in later
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        if(cursor.getPosition() % 2 == 0) {
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        }else{
            view.setBackgroundColor(context.getResources().getColor(R.color.colorPrimaryLight));
        }

        // Extract properties from cursor
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final Group group = viewHolder.getGroup();

        group.setId(cursor.getLong(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_ID)));
        group.setName(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_NAME)));
        group.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_IMAGE_URL)));
        group.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_CREATED_AT)));
        int synced = cursor.getInt(cursor.getColumnIndexOrThrow(GroupTable.COLUMN_SYNCHRONIZED));
        group.setSync(synced > 0);
        // Populate fields with extracted properties
        Log.d(Group.class.getSimpleName(), "ImageUrl: " + group.getImageUrl());
        viewHolder.groupTitle.setText(group.getName());
        final Date date = Helper.parseDateString(group.getCreatedAt());
        viewHolder.groupCreationDate.setText(Helper.READABLE_DATE_FORMAT.format(date));
        if (group.getSync()) {
            viewHolder.syncedIcon.setVisibility(View.GONE);
        } else {
            viewHolder.syncedIcon.setVisibility(View.VISIBLE);
        }
        final String imageUrl = group.getImageUrl();
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        GlideUrl glideUrl = null;

        if (imageUrl != null && imageUrl.startsWith(context.getResources().getString(R.string.web_service_url))) {
            builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            glideUrl = new GlideUrl(imageUrl, builder.build());
        }
        if(group.getSync() && group.getImageUrl() != null) {
            Glide.with(context).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fallback(R.drawable.anonymoususer)
                .placeholder(R.drawable.anonymoususer)
                .into(viewHolder.groupIcon);
        } else if(group.getImageUrl() != null){
            Glide.with(context).load(new File(group.getImageUrl()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .fallback(R.drawable.anonymoususer)
                .placeholder(R.drawable.anonymoususer)
                .into(viewHolder.groupIcon);
        } else {
            Glide.with(context)
                .load(R.drawable.anonymoususer)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(viewHolder.groupIcon);
        }
    }

    public class ViewHolder {
        ImageView groupIcon;
        TextView groupTitle;
        TextView groupCreationDate;
        ImageView syncedIcon;
        private Group group;

        ViewHolder(View view) {
            groupIcon = (ImageView) view.findViewById(R.id.group_icon);
            groupTitle = (TextView) view.findViewById(R.id.group_title);
            groupCreationDate = (TextView) view.findViewById(R.id.group_creation_date);
            syncedIcon = (ImageView) view.findViewById(R.id.synced_icon);
            group = new Group();
        }

        public Group getGroup() {
            return group;
        }
    }

}
