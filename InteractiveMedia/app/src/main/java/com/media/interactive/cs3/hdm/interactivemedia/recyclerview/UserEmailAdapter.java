package com.media.interactive.cs3.hdm.interactivemedia.recyclerview;

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
    private Context context;
    private List<User> list;

    private final static int NO_SWIPE_ITEM_TYPE = 0;
    private final static int SWIPE_ITEM_TYPE = 1;

    public UserEmailAdapter(Context context, List<User> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        // Choose first item to be not swipeable
        if(position == 0) {
            return NO_SWIPE_ITEM_TYPE;
        }
        return SWIPE_ITEM_TYPE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == NO_SWIPE_ITEM_TYPE) {
            final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.no_swipe_recycler_view_item, parent, false);
            return new NoSwipeViewHolder(itemView);
        } else {
            final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_recycler_view_item, parent, false);
            return new SimpleViewHolder(itemView);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final User user = list.get(position);
        if(holder.getItemViewType() == NO_SWIPE_ITEM_TYPE) {
            final NoSwipeViewHolder rootMyViewHolder = (NoSwipeViewHolder)holder;
            rootMyViewHolder.name.setText(user.getEmail());
        } else {
            final SimpleViewHolder simpleViewHolder = (SimpleViewHolder)holder;
            simpleViewHolder.name.setText(user.getEmail());
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public void removeUser(int position) {
        list.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreUser(User user, int position) {
        list.add(position, user);
        // notify item added by position
        notifyItemInserted(position);
    }

    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public RelativeLayout viewBackground, viewForeground;

        public SimpleViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.recycler_view_text);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }
    public class NoSwipeViewHolder extends SimpleViewHolder {
        public NoSwipeViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.recycler_view_text);
            viewBackground = view.findViewById(R.id.view_background);
            viewForeground = view.findViewById(R.id.view_foreground);
        }
    }
}
