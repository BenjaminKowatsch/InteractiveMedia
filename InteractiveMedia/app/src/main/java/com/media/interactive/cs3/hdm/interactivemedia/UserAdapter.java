package com.media.interactive.cs3.hdm.interactivemedia;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
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
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.GroupUserTable;
import com.media.interactive.cs3.hdm.interactivemedia.contentprovider.tables.UserTable;
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

/**
 * Created by benny on 04.01.18.
 */

public class UserAdapter extends CursorAdapter {

    private LayoutInflater mLayoutInflater;
    private Context context;
    private int layout;

    public UserAdapter(Context context, int layout, Cursor c) {
        super(context, c, 0);
        this.context = context;
        this.layout = layout;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public class ViewHolder {
        ImageView userIcon;
        TextView userTitle;
        TextView userEmail;
        TextView userCreationDate;
        private User user;
        ViewHolder(View view) {
            userIcon = (ImageView) view.findViewById(R.id.user_icon);
            userTitle = (TextView) view.findViewById(R.id.user_name);
            userEmail = (TextView) view.findViewById(R.id.user_email);
            userCreationDate = (TextView) view.findViewById(R.id.user_creation_date);

            user = new User();
        }

        public User getUser() {
            return user;
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final View view = mLayoutInflater.inflate(layout, viewGroup, false);
        view.setTag( new ViewHolder(view) );
        // no need to bind data here. you do in later
        return view;
    }

    @Override
    public void bindView(View view,final Context context, Cursor cursor) {
        // Extract properties from cursor
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final User user = viewHolder.getUser();
        user.setId(cursor.getLong(0));
        user.setUsername(cursor.getString(1));
        user.setImageUrl(cursor.getString(2));
        user.setEmail(cursor.getString(3));
        user.setCreatedAt(cursor.getString(4));
        user.setUserId(cursor.getString(5));

        Log.d(Group.class.getSimpleName(), user.toString());
        // Populate fields with extracted properties
        viewHolder.userTitle.setText(user.getUsername());
        viewHolder.userEmail.setText(user.getEmail());
        viewHolder.userCreationDate.setText(user.getCreatedAt());

        final String imageUrl = user.getImageUrl();
        if(imageUrl != null) {
            LazyHeaders.Builder builder = new LazyHeaders.Builder();

            if (imageUrl.startsWith(context.getResources().getString(R.string.web_service_url))) {
                builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue() + " " + Login.getInstance().getAccessToken());
            }
            final GlideUrl glideUrl = new GlideUrl(imageUrl, builder.build());

            Glide.with(context).load(glideUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(ContextCompat.getDrawable(context, R.drawable.anonymoususer))
                .into(viewHolder.userIcon);
        }
    }
}