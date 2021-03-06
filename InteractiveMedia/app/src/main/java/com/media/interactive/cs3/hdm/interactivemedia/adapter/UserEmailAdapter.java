/*
 * 
 */

package com.media.interactive.cs3.hdm.interactivemedia.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.media.interactive.cs3.hdm.interactivemedia.R;
import com.media.interactive.cs3.hdm.interactivemedia.data.User;

import java.util.List;



/**
 * Created by benny on 29.12.17.
 */

public class UserEmailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * The Constant NO_SWIPE_ITEM_TYPE.
     */
    private static final int NO_SWIPE_ITEM_TYPE = 0;
    /**
     * The Constant SWIPE_ITEM_TYPE.
     */
    private static final int SWIPE_ITEM_TYPE = 1;
    /**
     * The context.
     */
    private Context context;
    /**
     * The list.
     */
    private List<User> list;

    /**
     * Instantiates a new user email adapter.
     *
     * @param context the context
     * @param list    the list
     */
    public UserEmailAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
    }

    /**
     * Gets the item view type.
     *
     * @param position the position
     * @return the item view type
     */
    @Override
    public int getItemViewType(int position) {
        // Choose first item to be not swipeable
        if (position == 0) {
            return NO_SWIPE_ITEM_TYPE;
        }
        return SWIPE_ITEM_TYPE;
    }

    /**
     * On create view holder.
     *
     * @param parent   the parent
     * @param viewType the view type
     * @return the recycler view. view holder
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == NO_SWIPE_ITEM_TYPE) {
            final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.no_swipe_recycler_view_item, parent, false);
            return new NoSwipeViewHolder(itemView);
        } else {
            final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_recycler_view_item, parent, false);
            return new SimpleViewHolder(itemView);
        }
    }


    /**
     * On bind view holder.
     *
     * @param holder   the holder
     * @param position the position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final User user = list.get(position);
        if (holder.getItemViewType() == NO_SWIPE_ITEM_TYPE) {
            final NoSwipeViewHolder rootMyViewHolder = (NoSwipeViewHolder) holder;
            rootMyViewHolder.name.setText(user.getEmail());
        } else {
            final SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            simpleViewHolder.name.setText(user.getEmail());
        }
    }

    /**
     * Gets the item count.
     *
     * @return the item count
     */
    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    /**
     * Removes the user.
     *
     * @param position the position
     */
    public void removeUser(int position) {
        list.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    /**
     * Restore user.
     *
     * @param user     the user
     * @param position the position
     */
    public void restoreUser(User user, int position) {
        list.add(position, user);
        // notify item added by position
        notifyItemInserted(position);
    }

    /**
     * The Class SimpleViewHolder.
     */
    public class SimpleViewHolder extends RecyclerView.ViewHolder {

        /**
         * The name.
         */
        public TextView name;

        /**
         * The view background.
         */
        public RelativeLayout viewBackground;

        /**
         * The view foreground.
         */
        public RelativeLayout viewForeground;

        /**
         * Instantiates a new simple view holder.
         *
         * @param view the view
         */
        public SimpleViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.recycler_view_text);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }

    /**
     * The Class NoSwipeViewHolder.
     */
    public class NoSwipeViewHolder extends SimpleViewHolder {

        /**
         * Instantiates a new no swipe view holder.
         *
         * @param view the view
         */
        public NoSwipeViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.recycler_view_text);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }
}
