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
import com.media.interactive.cs3.hdm.interactivemedia.data.Group;
import com.media.interactive.cs3.hdm.interactivemedia.data.Login;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;
import com.media.interactive.cs3.hdm.interactivemedia.database.tables.UserTable;



/**
 * Created by benny on 04.01.18.
 */

public class UserAdapter extends CursorAdapter {

    /**
     * The layout inflater.
     */
    private LayoutInflater layoutInflater;

    /**
     * The layout.
     */
    private int layout;

    /**
     * Instantiates a new user adapter.
     *
     * @param context the context
     * @param layout  the layout
     * @param c       the c
     */
    public UserAdapter(Context context, int layout, Cursor c) {
        super(context, c, 0);
        this.layout = layout;
        layoutInflater = LayoutInflater.from(context);
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
     * Bind the view with the user data from the cursor using the ViewHolder pattern.
     *
     * @param view    the view
     * @param context the context
     * @param cursor  the cursor
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Extract properties from cursor
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        final User user = viewHolder.getUser();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(UserTable.COLUMN_ID)));
        user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_USERNAME)));
        user.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_IMAGE_URL)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_EMAIL)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_CREATED_AT)));
        user.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(UserTable.COLUMN_USER_ID)));

        Log.d(Group.class.getSimpleName(), user.toString());
        // Populate fields with extracted properties
        viewHolder.userTitle.setText(user.getUsername());
        viewHolder.userEmail.setText(user.getEmail());
        viewHolder.userCreationDate.setText(user.getCreatedAt());

        final String imageUrl = user.getImageUrl();
        LazyHeaders.Builder builder = new LazyHeaders.Builder();
        GlideUrl glideUrl = null;
        if (imageUrl != null) {
            if (imageUrl.startsWith(context.getResources().getString(R.string.web_service_url))) {
                builder = builder.addHeader("Authorization", Login.getInstance().getUserType().getValue()
                    + " " + Login.getInstance().getAccessToken());
                glideUrl = new GlideUrl(imageUrl, builder.build());
            } else {
                glideUrl = new GlideUrl(imageUrl, builder.build());
            }
        }

        Glide.with(context).load(glideUrl)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .fallback(R.drawable.anonymoususer)
            .placeholder(R.drawable.anonymoususer)
            .into(viewHolder.userIcon);


    }

    /**
     * The Class ViewHolder.
     *
     * @author benny
     */
    public class ViewHolder {

        /**
         * The user icon.
         */
        ImageView userIcon;

        /**
         * The user title.
         */
        TextView userTitle;

        /**
         * The user email.
         */
        TextView userEmail;

        /**
         * The user creation date.
         */
        TextView userCreationDate;

        /**
         * The user.
         */
        private User user;

        /**
         * Instantiates a new view holder.
         *
         * @param view the view
         */
        public ViewHolder(View view) {
            userIcon = (ImageView) view.findViewById(R.id.user_icon);
            userTitle = (TextView) view.findViewById(R.id.user_name);
            userEmail = (TextView) view.findViewById(R.id.user_email);
            userCreationDate = (TextView) view.findViewById(R.id.user_creation_date);

            user = new User();
        }

        /**
         * Gets the user.
         *
         * @return the user
         */
        public User getUser() {
            return user;
        }
    }
}